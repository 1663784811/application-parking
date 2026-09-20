<template>
  <div class="parking-exit-page">
    <van-nav-bar title="停车出场" left-arrow @click-left="router.back()"/>

    <div class="exit-content">
      <!-- 本页只能从二维码带参进入（parkingId 必带），缺参说明链接不完整，
           此时给车牌输入框也查不出费用，直接提示，别让车主白输一遍 -->
      <van-empty
        v-if="!state.parkingId"
        description="请扫描停车场或出口通道的二维码进入"
      />

      <div v-else class="exit-card">

        <PlateInput v-model:prefix="state.platePrefix" v-model:number="state.plateNumber"/>

        <!-- 车牌输入完整（车身 6 位，新能源 7 位）后才出现查询按钮，由车主手动触发计费查询 -->
        <van-button
          v-if="state.plateNumber.length >= 6"
          class="query-button"
          type="default"
          block
          :loading="state.loading"
          @click="queryOrderInfo"
        >
          查询停车费用
        </van-button>

        <!-- 查到订单才显示停车场/入场时间/时长/费用，没查到就别摆一堆 '-' -->
        <div v-if="state.orderId" class="parking-info">
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
          <div class="info-item">
            <span class="label">停车费用</span>
            <span class="value price">¥{{ state.amount }}</span>
          </div>
        </div>
      </div>

      <!-- 查到订单（orderId 有值）才显示支付按钮与缴费提示，避免车牌没查出来就点支付 -->
      <div v-if="state.orderId" class="action-section">
        <van-button type="primary" size="large" block :loading="state.paying" @click="handlePay">
          确认支付 ¥{{ state.amount }}
        </van-button>
      </div>
      <div v-if="state.orderId" class="tips">
        <van-icon name="info-o"/>
        <span>请在{{ state.expiredTime || '30分钟' }}内完成支付</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import {onMounted, onUnmounted, reactive, watch} from 'vue'
import {useRoute, useRouter} from 'vue-router'
import {showLoadingToast, closeToast, showToast} from 'vant'
import PlateInput from '@/components/PlateInput.vue'
import {getChannelVehicle, getExitOrder, payExitOrder} from '@/api/parkingExit'

const route = useRoute()
const router = useRouter()

const state = reactive({
  parkingId: '',
  channelId: '',
  orderId: '',
  platePrefix: '京',
  plateNumber: '',
  parkingName: '',
  entryTime: '',
  duration: '',
  amount: '0.00',
  expiredTime: '',
  loading: false,
  paying: false,
  // 计时器句柄与支付截止时间戳（页面状态一律放 state）
  countdownTimer: null,
  expireAt: 0,
})

// 计费接口由「查询停车费用」按钮手动触发，车牌输入完整（车身 6 位、新能源 7 位）才显示该按钮
const fullPlate = () => `${state.platePrefix || ''}${state.plateNumber || ''}`

const clearOrder = () => {
  state.orderId = ''
  state.parkingName = ''
  state.entryTime = ''
  state.duration = ''
  state.amount = '0.00'
  state.expiredTime = ''
  state.expireAt = 0
}

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

// 接口2：车牌完整时查询停车费用
const queryOrderInfo = () => {
  if (!state.parkingId) {
    showToast('缺少停车场参数')
    return
  }
  state.loading = true
  getExitOrder({parkingId: state.parkingId, carNumber: fullPlate()}).then((res) => {
    const order = res.data || {}
    state.orderId = order.orderId || ''
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

// 接口1：页面加载时带出该通道当前要出场的车辆
const loadChannelVehicle = () => {
  if (!state.channelId) {
    // 停车场二维码（不带通道）：由车主手动输入车牌
    return
  }
  getChannelVehicle({parkingId: state.parkingId, channelId: state.channelId}).then((res) => {
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
  if (state.plateNumber.length < 6) {
    showToast('请输入完整车牌号')
    return
  }
  if (!state.orderId) {
    showToast('请先查询停车费用')
    return
  }
  state.paying = true
  showLoadingToast({message: '支付中...', forbidClick: true})
  payExitOrder({orderId: state.orderId, payType: 1}).then(() => {
    closeToast()
    showToast({
      message: '支付成功，请通行',
      onClose: () => {
        router.replace({name: 'mainIndex', params: {appId: route.params.appId}})
      },
    })
  }).catch((err) => {
    closeToast()
    showToast(err?.msg || '支付失败')
  }).finally(() => {
    state.paying = false
  })
}

// 车牌被改残或清空时，之前查出来的订单信息作废，避免旧金额残留
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
  }
})
</script>

<style scoped lang="less">
.parking-exit-page {
  display: flex;
  flex-direction: column;
  flex: 1;
  overflow: hidden;
  background: var(--bg-secondary);

  .exit-content {
    flex: 1;
    overflow: auto;
    padding: 16px;
  }

  .exit-card {
    padding: 20px;
    margin-bottom: 16px;
    background: var(--bg-primary);
    border-radius: 12px;

    /* 查询是次要动作：品牌绿描边款，和下方实心的支付按钮拉开主次。
       Vant 的 primary 是蓝色，与品牌绿不符，所以走 default 类型 + 品牌色变量覆盖。
       这里只覆盖 CSS 变量，不跟 Vant 的类选择器抢优先级，跨版本改名风险也小。 */
    .query-button {
      margin-top: 16px;
      border-radius: var(--radius-sm);
      font-weight: 600;
      --van-button-default-height: 44px;
      --van-button-default-background: var(--brand-primary-5);
      --van-button-default-color: var(--brand-primary-1);
      --van-button-default-border-color: var(--brand-primary-2);
    }

    .parking-info {
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

        .price {
          font-size: 20px;
          font-weight: 600;
          color: var(--color-danger);
        }
      }
    }
  }

  .action-section {
    padding: 16px;
    background: var(--bg-primary);

    .van-button {
      margin-bottom: 12px;
      border-radius: 8px;

      &:last-child {
        margin-bottom: 0;
      }
    }
  }

  .tips {
    display: flex;
    align-items: flex-start;
    gap: 8px;
    padding: 12px;
    margin-top: 12px;
    font-size: 12px;
    color: var(--text-secondary);

    .van-icon {
      flex-shrink: 0;
      margin-top: 2px;
      color: var(--color-warning);
    }
  }
}
</style>
