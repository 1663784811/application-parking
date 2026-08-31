<template>
  <div class="parking-exit-page">
    <van-nav-bar title="停车出场" left-arrow @click-left="router.back()" />

    <div class="exit-content">
      <div class="exit-card">

        <PlateInput v-model:prefix="state.platePrefix" v-model:number="state.plateNumber" />

        <div class="parking-info">
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

      <div class="action-section">
        <van-button type="primary" size="large" block @click="handlePay">
          确认支付 ¥{{ state.amount }}
        </van-button>
        <van-button type="default" size="large" block plain @click="handleUseCoupon">
          使用优惠券/卡包
        </van-button>
      </div>

      <div class="tips">
        <van-icon name="info-o" />
        <span>请在{{ state.expiredTime || '30分钟' }}内完成支付</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showLoadingToast, closeToast } from 'vant'
import PlateInput from '@/components/PlateInput.vue'

const route = useRoute()
const router = useRouter()

const state = reactive({
  orderId: '',
  platePrefix: '京',
  plateNumber: '',
  parkingName: '',
  entryTime: '',
  duration: '',
  amount: '0.00',
  expiredTime: '',
})

// 监听车牌号变化，查询订单信息
watch(() => state.plateNumber, (newVal) => {
  if (newVal && newVal.length >= 5) {
    queryOrderInfo()
  }
})

const queryOrderInfo = () => {
  // TODO: 调用查询订单接口（getOrderDetail），参数车牌号
  const orderId = route.query.orderId
  state.orderId = orderId || 'ORD2024010100001'

  // 模拟查询订单
  setTimeout(() => {
    state.parkingName = '万达广场停车场'
    state.entryTime = '2024-01-15 10:30:00'
    state.duration = '2小时30分钟'
    state.amount = '15.00'
    state.expiredTime = '30分钟'
  }, 300)
}

const handlePay = () => {
  if (!state.plateNumber || state.plateNumber.length < 5) {
    showToast('请输入完整车牌号')
    return
  }

  showLoadingToast({ message: '支付中...', forbidClick: true })

  // TODO: 调用支付接口（payOrder）
  setTimeout(() => {
    closeToast()
    showToast({
      message: '支付成功，请通行',
      onClose: () => {
        router.replace({
          name: 'mainIndex',
          params: { appId: route.params.appId },
        })
      },
    })
  }, 1500)
}

const handleUseCoupon = () => {
  router.push({
    name: 'cardPackage',
    params: { appId: route.params.appId },
  })
}
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
