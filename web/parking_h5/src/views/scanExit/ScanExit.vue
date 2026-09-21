<template>
  <div class="scan-exit-page">
    <!-- 本页只能从出口二维码带参进入（parkingId 必带），缺参说明链接不完整，
         此时连停车场都定位不到，直接提示，别让车主白输一遍车牌 -->
    <van-empty
      v-if="!state.parkingId"
      description="请扫描停车场出口的二维码进入"
    />

    <template v-else>
      <!-- 顶部只有返回 + 标题：查到费用后标题跟着变成「车辆信息」 -->
      <van-nav-bar
        :title="state.orderId ? '车辆信息' : '输入车牌'"
        left-arrow
        @click-left="handleBack"
      />

      <!-- 一、还没查到费用：输入车牌 -->
      <div v-if="!state.orderId" class="page-body">
        <!-- 车辆线性图标 -->
        <div class="car-icon">
          <svg
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            stroke-width="1.5"
            stroke-linecap="round"
            stroke-linejoin="round"
            aria-hidden="true"
          >
            <!-- 座舱 -->
            <path d="M6.6 11.4l1.7-3.1a1.7 1.7 0 0 1 1.5-.8h4.4a1.7 1.7 0 0 1 1.5.8l1.7 3.1"/>
            <!-- 车身：底边在轮子处断开，避免压线 -->
            <path d="M4 15.4v-2.5a1.5 1.5 0 0 1 1.5-1.5h13a1.5 1.5 0 0 1 1.5 1.5v2.5"/>
            <path d="M4 15.4h2.2M10 15.4h4M17.8 15.4H20"/>
            <!-- 前后轮 -->
            <circle cx="8.1" cy="15.4" r="1.9"/>
            <circle cx="15.9" cy="15.4" r="1.9"/>
          </svg>
        </div>

        <p class="page-title">请输入您的车牌号</p>

        <PlateInput v-model:prefix="state.platePrefix" v-model:number="state.plateNumber"/>

        <p class="energy-tip">新能源车请在车牌号输入“新”</p>

        <!-- 车牌满 6 位才能查，查询即接口2，金额由服务端算 -->
        <van-button
          class="query-btn"
          block
          round
          :loading="state.loading"
          :disabled="!plateComplete"
          @click="queryOrderInfo"
        >
          查询
        </van-button>
      </div>

      <!-- 二、查到费用：车辆信息 + 支付 -->
      <template v-else>
        <div class="page-body page-body--info">
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
              <span class="label">车牌号</span>
              <span class="value">{{ fullPlate() }}</span>
            </div>
            <div class="info-item">
              <span class="label">停车场</span>
              <span class="value">{{ state.parkingName || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="label">入场时间</span>
              <span class="value">{{ state.entryTime || '-' }}</span>
            </div>
            <div class="info-item">
              <span class="label">停车时长</span>
              <span class="value">{{ state.duration || '-' }}</span>
            </div>
          </div>

          <!-- 车牌输错时退回输入态，保留已输的车牌让车主改 -->
          <div class="reset-line" @click="handleResetPlate">
            <van-icon name="replay"/>
            <span>车牌不对？重新输入</span>
          </div>
        </div>

        <!-- 吸底支付条：查到订单才出现 -->
        <footer class="pay-bar">
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
    </template>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, reactive } from 'vue'
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
  // 车牌
  platePrefix: '京',
  plateNumber: '',
  // 查到的订单
  orderId: '',
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

const fullPlate = () => `${state.platePrefix || ''}${state.plateNumber || ''}`

// 清空订单信息：车牌改动、查询失败、重新输入时都要回到"未查询"状态
const clearOrder = () => {
  state.orderId = ''
  state.parkingName = ''
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

// 接口2：按车牌查询本次停车费用，查到后页面切到"车辆信息 + 支付"形态
const queryOrderInfo = () => {
  if (!state.parkingId) {
    showToast('缺少停车场参数')
    return
  }
  state.loading = true
  getExitOrder({ parkingId: state.parkingId, carNumber: fullPlate() }).then((res) => {
    const order = res.data || {}
    if (!order.orderId) {
      // 成功但没有订单号：当成没查到，别静默留在输入态
      clearOrder()
      showToast('未查询到该车牌的在场记录')
      return
    }
    state.orderId = order.orderId
    state.parkingName = order.parkingName || ''
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

// 接口1：通道二维码进来时带出该通道当前要出场的车辆，省去车主手输车牌
const loadChannelVehicle = () => {
  if (!state.channelId) {
    // 停车场入口二维码（不带通道）：由车主手动输入车牌
    return
  }
  // 只按通道查：停车场由通道反查，不用带 parkingId
  getChannelVehicle({ channelId: state.channelId }).then((res) => {
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
  background: var(--bg-primary);

  .page-body {
    display: flex;
    flex-direction: column;
    align-items: center;
    flex: 1;
    overflow: auto;
    padding: 40px 20px calc(24px + env(safe-area-inset-bottom, 0));

    /* 车辆信息态：内容是一整块信息卡，不需要顶部那圈留白 */
    &.page-body--info {
      padding-top: 16px;
    }
  }

  /* ====== 输入车牌态 ====== */
  .car-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 72px;
    height: 72px;
    color: var(--brand-primary);
    border-radius: 50%;

    svg {
      width: 80px;
      height: 80px;
    }
  }

  .page-title {
    margin-top: 20px;
    font-size: 16px;
    font-weight: 600;
    color: var(--text-primary);
  }

  .plate-input {
    margin-top: 28px;
  }

  .energy-tip {
    margin-top: 12px;
    font-size: 12px;
    color: var(--text-tertiary);
  }

  /* 查询按钮：宽度接近屏幕两侧边距，主色实心圆角 */
  .query-btn {
    margin-top: 36px;
    font-size: 17px;
    font-weight: 600;
    --van-button-default-height: 48px;
    --van-button-default-background: var(--brand-primary);
    --van-button-default-color: var(--on-brand-primary);
    --van-button-default-border-color: transparent;
    /* 禁用态交给 Vant 默认的灰底灰字：主色浅绿上压白字对比度不够 */
  }

  /* ====== 车辆信息态 ====== */
  .amount-block {
    display: flex;
    align-items: flex-end;
    justify-content: space-between;
    width: 100%;

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
    width: 100%;
    height: 1px;
    margin: 16px 0;
    background: var(--border-secondary);
  }

  .info-list {
    width: 100%;

    .info-item {
      display: flex;
      justify-content: space-between;
      padding: 10px 0;
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
    width: 100%;
    margin-top: 20px;
    padding-top: 20px;
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
      --van-button-default-background: var(--brand-primary);
      --van-button-default-color: var(--on-brand-primary);
      --van-button-default-border-color: transparent;
    }
  }
}
</style>
