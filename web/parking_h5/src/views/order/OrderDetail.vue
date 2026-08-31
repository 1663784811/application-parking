<template>
  <div class="order-detail-page">
    <van-nav-bar title="订单详情" left-arrow @click-left="router.back()" />

    <van-skeleton v-if="state.loading" :row="10" />

    <template v-if="state.order">
      <div class="status-card">
        <div class="status-icon">
          <van-icon :name="state.order.status === 1 ? 'passed' : 'clock'" size="48" />
        </div>
        <p class="status-text">{{ state.order.statusText }}</p>
        <p v-if="state.order.status !== 3" class="amount">¥{{ state.order.payAmount || state.order.amount }}</p>
      </div>

      <div class="section">
        <h3 class="section-title">停车信息</h3>
        <div class="info-list">
          <div class="info-item">
            <span class="label">停车场</span>
            <span class="value">{{ state.order.parkingName }}</span>
          </div>
          <div class="info-item">
            <span class="label">地址</span>
            <span class="value">{{ state.order.parkingAddress }}</span>
          </div>
          <div class="info-item">
            <span class="label">车牌号</span>
            <span class="value">{{ state.order.plateNumber }}</span>
          </div>
          <div class="info-item">
            <span class="label">入场时间</span>
            <span class="value">{{ state.order.entryTime }}</span>
          </div>
          <div class="info-item">
            <span class="label">离场时间</span>
            <span class="value">{{ state.order.exitTime || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="label">停车时长</span>
            <span class="value">{{ state.order.duration }}</span>
          </div>
        </div>
      </div>

      <div class="section">
        <h3 class="section-title">费用明细</h3>
        <div class="info-list">
          <div class="info-item">
            <span class="label">停车费用</span>
            <span class="value">¥{{ state.order.amount }}</span>
          </div>
          <div v-if="state.order.discount" class="info-item">
            <span class="label">优惠</span>
            <span class="value">-¥{{ state.order.discount }}</span>
          </div>
          <div class="info-item total">
            <span class="label">应付金额</span>
            <span class="value">¥{{ state.order.payAmount || state.order.amount }}</span>
          </div>
        </div>
      </div>

      <div class="section">
        <h3 class="section-title">订单信息</h3>
        <div class="info-list">
          <div class="info-item">
            <span class="label">订单编号</span>
            <span class="value">{{ state.order.orderNo }}</span>
          </div>
        </div>
      </div>
    </template>

    <div v-if="state.order && state.order.status === 0" class="bottom-action">
      <van-button
        type="primary"
        size="large"
        block
        :loading="state.paying"
        @click="handlePay"
      >
        立即支付
      </van-button>
    </div>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showLoadingToast, closeToast } from 'vant'

const route = useRoute()
const router = useRouter()

const state = reactive({
  order: null,
  loading: false,
  paying: false,
})

const fetchDetail = () => {
  state.loading = true
  showLoadingToast({ message: '加载中...', forbidClick: true })
  // TODO: 调用订单详情接口（getOrderDetail），参数 orderId
  const orderId = route.params.orderId
  setTimeout(() => {
    state.order = {
      id: orderId,
      orderNo: 'ORD' + orderId + '00001',
      status: 0,
      statusText: '待支付',
      parkingName: '万达广场停车场',
      parkingAddress: '朝阳区建国路93号',
      plateNumber: '京A12345',
      entryTime: '2024-01-15 10:30:00',
      exitTime: '2024-01-15 13:00:00',
      duration: '2小时30分钟',
      amount: '15.00',
      discount: '0.00',
      payAmount: '15.00',
    }
    state.loading = false
    closeToast()
  }, 300)
}

const handlePay = () => {
  if (!state.order) return
  state.paying = true
  showLoadingToast({ message: '支付中...', forbidClick: true })
  // TODO: 调用支付接口（payOrder）
  setTimeout(() => {
    closeToast()
    state.paying = false
    showToast({
      message: '支付成功',
      onClose: () => {
        fetchDetail()
      },
    })
  }, 1500)
}

onMounted(() => {
  fetchDetail()
})
</script>

<style scoped lang="less">
.order-detail-page {
  display: flex;
  flex-direction: column;
  flex: 1;
  overflow: auto;

  .status-card {
    padding: 32px 16px;
    background: linear-gradient(135deg, var(--brand-primary), var(--brand-primary-2));
    color: var(--on-brand-primary);
    text-align: center;
    padding-top: calc(var(--safe-top) + 32px);

    .status-icon {
      margin-bottom: 12px;
    }

    .status-text {
      font-size: 16px;
      margin-bottom: 8px;
    }

    .amount {
      font-size: 32px;
      font-weight: 600;
    }
  }

  .section {
    padding: 16px;
    background: var(--bg-primary);
    margin-bottom: 12px;

    .section-title {
      font-size: 15px;
      font-weight: 600;
      margin-bottom: 12px;
      padding-left: 8px;
      border-left: 3px solid var(--brand-primary);
      color: var(--text-primary);
    }

    .info-list {
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

        &.total {
          border-top: 1px solid var(--border-primary);
          margin-top: 8px;
          padding-top: 16px;

          .value {
            font-size: 18px;
            font-weight: 600;
            color: var(--color-danger);
          }
        }
      }
    }
  }

  .bottom-action {
    padding: 16px;
    background: var(--bg-primary);
    box-shadow: 0 -2px 8px var(--shadow-light);

    .van-button {
      border-radius: 8px;
    }
  }
}
</style>
