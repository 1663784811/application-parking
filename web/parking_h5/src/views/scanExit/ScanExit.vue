<template>
  <div class="scan-exit-page">
    <!-- 本页只能从出口二维码带参进入（parkingId 必带），缺参说明链接不完整，
         此时连停车场都定位不到，直接提示，别让车主白输一遍车牌 -->
    <van-empty
      v-if="!state.parkingId"
      description="请扫描停车场出口的二维码进入"
    />

    <template v-else>
      <div class="page-scroll">
        <!-- 品牌头图：停车场名 + 当前车牌 -->
        <header class="hero">
          <div class="hero-glow"></div>

          <div class="hero-bar">
            <div class="back-btn" @click="handleBack">
              <van-icon name="arrow-left"/>
            </div>
            <span class="hero-bar-title">扫码出场</span>
          </div>

          <div class="hero-body">
            <h1 class="parking-name">{{ state.parkingName || '停车场' }}</h1>
            <p class="hero-tip">{{ heroTip }}</p>

            <!-- 车牌徽标：通道识别到的车牌直接展示在这里，车主一眼确认对不对 -->
            <div class="plate-badge">
              <span class="plate-prefix">{{ state.platePrefix }}</span>
              <span class="plate-number">{{ state.plateNumber || '待识别' }}</span>
            </div>
          </div>
        </header>

        <!-- 悬浮卡片：上移压住头图，形成层次 -->
        <section class="card">
          <!-- 一、还没查到订单：核对自己车牌 → 查询费用 -->
          <template v-if="!state.orderId">
            <p class="card-hint">核对车牌号，新能源车牌为 7 位</p>
            <PlateInput v-model:prefix="state.platePrefix" v-model:number="state.plateNumber"/>
            <van-button
              v-if="plateComplete"
              class="query-btn"
              block
              :loading="state.loading"
              @click="queryOrderInfo"
            >
              查询停车费用
            </van-button>
          </template>

          <!-- 二、查到订单：费用与明细 -->
          <template v-else>
            <div class="amount-block">
              <div class="amount-main">
                <span class="amount-label">停车费用</span>
                <div class="amount-value"><em>¥</em>{{ state.amount }}</div>
              </div>
              <span class="duration-chip">{{ state.duration }}</span>
            </div>

            <div class="divider"></div>

            <div class="info-list">
              <div class="info-item">
                <span class="label">停车场</span>
                <span class="value">{{ state.parkingName }}</span>
              </div>
              <div class="info-item">
                <span class="label">车牌号</span>
                <span class="value">{{ fullPlate() }}</span>
              </div>
              <div class="info-item">
                <span class="label">入场时间</span>
                <span class="value">{{ state.entryTime }}</span>
              </div>
              <div class="info-item">
                <span class="label">停车时长</span>
                <span class="value">{{ state.duration }}</span>
              </div>
            </div>

            <div class="reset-line" @click="handleResetPlate">
              <van-icon name="replay"/>
              <span>车牌不对？重新输入</span>
            </div>
          </template>
        </section>
      </div>

      <!-- 吸底支付条：查到订单才出现 -->
      <footer v-if="state.orderId" class="pay-bar">
        <p class="countdown">
          <van-icon name="clock-o"/>
          <span>请在 {{ state.expiredTime || '30分钟' }}内完成支付，超时需重新查询</span>
        </p>
        <van-button
          class="pay-btn"
          block
          round
          :loading="state.paying"
          @click="handlePay"
        >
          立即支付 ¥{{ state.amount }}
        </van-button>
      </footer>
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, reactive, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showLoadingToast, closeToast, showToast } from 'vant'
import PlateInput from '@/components/PlateInput.vue'
import { getChannelVehicle, getExitOrder, payExitOrder } from '@/api/parkingExit'

const route = useRoute()
const router = useRouter()

const state = reactive({
  // 二维码带进来的参数
  parkingId: '',
  channelId: '',
  // 查询到的订单
  orderId: '',
  // 车牌
  platePrefix: '京',
  plateNumber: '',
  // 订单信息
  parkingName: '',
  entryTime: '',
  duration: '',
  amount: '0.00',
  expiredTime: '',
  // 请求状态
  loading: false,
  paying: false,
  // 支付倒计时
  countdownTimer: null,
  expireAt: 0,
})

// 车牌主体满 6 位（车身标准 6 位，新能源补第 7 位）即可查询
const plateComplete = computed(() => state.plateNumber.length >= 6)

// 头图提示语：跟着页面走到哪一步变化
const heroTip = computed(() => {
  if (state.orderId) {
    return '订单已核对，请尽快完成缴费'
  }
  if (plateComplete.value) {
    return '请核对车牌，查询本次停车费用'
  }
  return '请输入要出场车辆的车牌号'
})

const fullPlate = () => `${state.platePrefix || ''}${state.plateNumber || ''}`

// 清空订单信息：车牌改动、查询失败、重新输入时都要回到"未查询"状态。
// 注意 parkingName 不清：它属于"这是哪个停车场"，不属于订单，清掉头图会变空
const clearOrder = () => {
  state.orderId = ''
  state.entryTime = ''
  state.duration = ''
  state.amount = '0.00'
  state.expiredTime = ''
  state.expireAt = 0
  if (state.countdownTimer) {
    clearInterval(state.countdownTimer)
    state.countdownTimer = null
  }
}

// 支付倒计时：后端给的是截止时间点，前端只负责每秒刷新剩余时间
const startCountdown = (expireTime) => {
  if (state.countdownTimer) {
    clearInterval(state.countdownTimer)
    state.countdownTimer = null
  }
  // 后端返回 "yyyy-MM-dd HH:mm:ss"，Safari 不认这种格式，替换成 "/" 再解析
  state.expireAt = expireTime ? new Date(expireTime.replace(/-/g, '/')).getTime() : 0
  if (!state.expireAt) {
    state.expiredTime = '30分钟'
    return
  }
  const tick = () => {
    const rest = state.expireAt - Date.now()
    if (rest <= 0) {
      state.expiredTime = '0分钟'
      clearInterval(state.countdownTimer)
      state.countdownTimer = null
      return
    }
    const minutes = Math.floor(rest / 60000)
    const seconds = Math.floor((rest % 60000) / 1000)
    state.expiredTime = minutes > 0 ? `${minutes}分${seconds}秒` : `${seconds}秒`
  }
  tick()
  state.countdownTimer = setInterval(tick, 1000)
}

// 接口2：按车牌查询本次停车费用，查到后页面切到"费用明细 + 支付"形态
const queryOrderInfo = () => {
  if (!state.parkingId) {
    showToast('缺少停车场参数')
    return
  }
  state.loading = true
  getExitOrder({ parkingId: state.parkingId, carNumber: fullPlate() }).then((res) => {
    const order = res.data || {}
    state.orderId = order.orderId || ''
    state.parkingName = order.parkingName || state.parkingName
    state.entryTime = order.entryTime || ''
    state.duration = order.duration || ''
    state.amount = order.amount == null ? '0.00' : Number(order.amount).toFixed(2)
    startCountdown(order.expireTime)
  }).catch((err) => {
    clearOrder()
    showToast(err?.msg || '查询停车费用失败')
  }).finally(() => {
    state.loading = false
  })
}

// 接口1：通道二维码进来时带出该通道当前要出场的车辆，省去车主输车牌
const loadChannelVehicle = () => {
  if (!state.channelId) {
    // 停车场入口二维码（不带通道）：由车主手动输入车牌
    return
  }
  getChannelVehicle({ parkingId: state.parkingId, channelId: state.channelId }).then((res) => {
    const vehicle = res.data
    if (!vehicle || !vehicle.carNumber) {
      return
    }
    state.parkingName = vehicle.parkingName || ''
    // 拆出省份前缀后填进车牌输入框（首个字符是省份简称）
    const plate = vehicle.carNumber
    state.platePrefix = plate.substring(0, 1)
    state.plateNumber = plate.substring(1)
  }).catch((err) => {
    showToast(err?.msg || '查询通道车辆失败')
  })
}

// 接口3：支付停车费用
const handlePay = () => {
  if (!state.orderId) {
    showToast('请先查询停车费用')
    return
  }
  state.paying = true
  showLoadingToast({ message: '支付中...', forbidClick: true })
  payExitOrder({ orderId: state.orderId, payType: 1 }).then(() => {
    closeToast()
    showToast({
      message: '支付成功，请通行',
      onClose: () => {
        router.replace({ name: 'mainIndex', params: { appId: route.params.appId } })
      },
    })
  }).catch((err) => {
    closeToast()
    showToast(err?.msg || '支付失败')
  }).finally(() => {
    state.paying = false
  })
}

// 车牌输错时退回输入态：清掉订单信息，保留车牌让车主改
const handleResetPlate = () => {
  clearOrder()
}

// 出口二维码多是新开页面进来的，浏览器没有上一页，back() 会直接退出应用，
// 所以统一回应用首页
const handleBack = () => {
  router.replace({ name: 'mainIndex', params: { appId: route.params.appId } })
}

// 车牌被改残或清空时，之前查出来的订单作废，避免旧金额残留在屏幕上
watch(() => state.plateNumber, (newVal) => {
  if (!newVal || newVal.length < 6) {
    clearOrder()
  }
})

onMounted(() => {
  state.parkingId = route.query.parkingId || ''
  state.channelId = route.query.channelId || ''
  loadChannelVehicle()
})

onUnmounted(() => {
  if (state.countdownTimer) {
    clearInterval(state.countdownTimer)
    state.countdownTimer = null
  }
})
</script>

<style scoped lang="less">
.scan-exit-page {
  display: flex;
  flex-direction: column;
  flex: 1;
  overflow: hidden;
  background: var(--bg-secondary);

  .page-scroll {
    flex: 1;
    overflow: auto;
  }

  /* ====== 品牌头图 ====== */
  .hero {
    position: relative;
    padding: calc(var(--safe-top) + 8px) 20px 64px;
    overflow: hidden;
    background: var(--gradient-primary);
    border-radius: 0 0 var(--radius-xl) var(--radius-xl);

    /* 右上角柔光装饰，让渐变不那么平 */
    .hero-glow {
      position: absolute;
      top: -70px;
      right: -50px;
      width: 200px;
      height: 200px;
      background: var(--on-brand-glass);
      border-radius: 50%;
    }

    .hero-bar {
      position: relative;
      display: flex;
      align-items: center;
      gap: 12px;

      .back-btn {
        display: flex;
        align-items: center;
        justify-content: center;
        width: 32px;
        height: 32px;
        font-size: 18px;
        color: var(--on-brand-primary);
        background: var(--on-brand-glass);
        border-radius: 50%;
      }

      .hero-bar-title {
        font-size: 16px;
        font-weight: 600;
        color: var(--on-brand-primary);
      }
    }

    .hero-body {
      position: relative;
      margin-top: 20px;

      .parking-name {
        font-size: 22px;
        font-weight: 700;
        color: var(--on-brand-primary);
      }

      .hero-tip {
        margin-top: 6px;
        font-size: 13px;
        color: var(--on-brand-tertiary);
      }

      .plate-badge {
        display: inline-flex;
        align-items: center;
        gap: 10px;
        margin-top: 18px;
        padding: 8px 16px;
        background: var(--on-brand-glass);
        border: 1px solid var(--on-brand-glass-strong);
        border-radius: var(--radius-sm);

        .plate-prefix {
          font-size: 16px;
          font-weight: 600;
          color: var(--on-brand-tertiary);
        }

        .plate-number {
          font-size: 20px;
          font-weight: 700;
          letter-spacing: 2px;
          color: var(--on-brand-primary);
        }
      }
    }
  }

  /* ====== 悬浮卡片 ====== */
  .card {
    position: relative;
    margin: -48px 16px 16px;
    padding: 20px;
    background: var(--bg-primary);
    border-radius: var(--radius-lg);
    box-shadow: 0 8px 24px var(--shadow-medium);

    .card-hint {
      margin-bottom: 12px;
      font-size: 13px;
      color: var(--text-tertiary);
    }

    /* 查询按钮：品牌绿描边款，与头图的主色呼应又不抢支付按钮的主次 */
    .query-btn {
      margin-top: 4px;
      font-weight: 600;
      border-radius: var(--radius-sm);
      --van-button-default-height: 44px;
      --van-button-default-background: var(--brand-primary-5);
      --van-button-default-color: var(--brand-primary-1);
      --van-button-default-border-color: var(--brand-primary-2);
    }
  }

  /* ====== 费用区 ====== */
  .amount-block {
    display: flex;
    align-items: flex-end;
    justify-content: space-between;

    .amount-label {
      font-size: 13px;
      color: var(--text-tertiary);
    }

    .amount-value {
      margin-top: 4px;
      font-size: 34px;
      font-weight: 800;
      line-height: 1;
      color: var(--color-danger);

      em {
        margin-right: 2px;
        font-size: 18px;
        font-style: normal;
      }
    }

    .duration-chip {
      padding: 4px 10px;
      font-size: 12px;
      color: var(--brand-primary-1);
      background: var(--brand-primary-5);
      border-radius: 999px;
    }
  }

  .divider {
    height: 1px;
    margin: 16px 0;
    background: var(--border-secondary);
  }

  .info-list {
    .info-item {
      display: flex;
      justify-content: space-between;
      padding: 8px 0;
      font-size: 14px;

      .label {
        color: var(--text-secondary);
      }

      .value {
        color: var(--text-primary);
      }
    }
  }

  .reset-line {
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 4px;
    margin-top: 16px;
    padding-top: 16px;
    font-size: 13px;
    color: var(--text-tertiary);
    border-top: 1px dashed var(--border-primary);
  }

  /* ====== 吸底支付条 ====== */
  .pay-bar {
    padding: 12px 16px calc(12px + env(safe-area-inset-bottom, 0));
    background: var(--bg-primary);
    border-top: 1px solid var(--border-primary);

    .countdown {
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 4px;
      margin-bottom: 10px;
      font-size: 12px;
      color: var(--color-warning);
    }

    .pay-btn {
      font-size: 17px;
      font-weight: 600;
      --van-button-default-height: 48px;
      --van-button-default-background: var(--gradient-primary);
      --van-button-default-color: var(--on-brand-primary);
      --van-button-default-border-color: transparent;
    }
  }
}
</style>
