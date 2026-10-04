<template>
  <div class="coupon-page">
    <van-nav-bar title="优惠券" left-arrow @click-left="router.back()" />

    <div class="coupon-list">
      <div
        v-for="item in state.couponList"
        :key="item.id"
        class="coupon-card"
        :class="`status-${item.status}`"
      >
        <div class="coupon-left" :class="{ disabled: item.status !== 0 }">
          <span class="amount">{{ formatAmount(item) }}</span>
          <span class="condition">{{ item.condition }}</span>
        </div>
        <div class="coupon-right">
          <div class="coupon-info">
            <span class="name">{{ item.name }}</span>
            <span class="desc">{{ item.description }}</span>
            <span class="expire">{{ expireText(item.expireTime) }}</span>
          </div>
          <div class="coupon-actions">
            <van-tag :type="getStatusType(item.status)" size="medium">
              {{ getStatusText(item.status) }}
            </van-tag>
          </div>
        </div>
      </div>

      <van-empty
        v-if="!state.loading && state.couponList.length === 0"
        description="暂无优惠券"
      />
    </div>
  </div>
</template>

<script setup>
import { onMounted, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { showToast } from 'vant'
import { getMeCouponList } from '@/api/appMe'

const router = useRouter()

const state = reactive({
  loading: false,
  couponList: [],
})

const loadCoupons = () => {
  state.loading = true
  getMeCouponList().then((res) => {
    state.couponList = res?.data || []
  }).catch((err) => {
    showToast(err?.msg || '加载优惠券失败')
  }).finally(() => {
    state.loading = false
  })
}

onMounted(() => {
  loadCoupons()
})

// 金额列按券类型显示：满减是元、折扣是折、免费时长是分钟。
// 光看 amount 会显示错 —— 免费时长券的 amount 存的是分钟数，不是金额
const formatAmount = (item) => {
  const amount = Number(item.amount)
  if (item.type === 2) {
    return `${amount * 10}折`
  }
  if (item.type === 3) {
    return `${amount}分钟`
  }
  return amount >= 1 ? `¥${amount}` : `${amount * 10}折`
}

// 有效期从平台发放日起算的近似值，没给有效天数就显示长期有效
const expireText = (expireTime) => {
  return expireTime ? `有效期至 ${expireTime}` : '长期有效'
}

const getStatusText = (status) => {
  const map = { 0: '可用', 1: '已用尽', 2: '已过期' }
  return map[status] || '未知'
}

const getStatusType = (status) => {
  const map = { 0: 'success', 1: 'default', 2: 'danger' }
  return map[status] || 'default'
}
</script>

<style scoped lang="less">
.coupon-page {
  display: flex;
  flex-direction: column;
  flex: 1;
  overflow: hidden;

  .coupon-list {
    flex: 1;
    padding: 16px;
    overflow: auto;
  }

  .coupon-card {
    display: flex;
    margin-bottom: 12px;
    background: var(--bg-primary);
    border-radius: 12px;
    overflow: hidden;

    &.status-1,
    &.status-2 {
      opacity: 0.6;
    }

    .coupon-left {
      display: flex;
      flex-direction: column;
      justify-content: center;
      align-items: center;
      width: 100px;
      padding: 16px;
      color: var(--bg-primary);
      background: var(--color-danger);

      &.disabled {
        background: var(--text-disabled);
      }

      .amount {
        font-size: 22px;
        font-weight: 600;
      }

      .condition {
        font-size: 10px;
        margin-top: 4px;
        opacity: 0.9;
      }
    }

    .coupon-right {
      flex: 1;
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding: 12px 16px;

      .coupon-info {
        display: flex;
        flex-direction: column;
        gap: 4px;

        .name {
          font-size: 14px;
          font-weight: 600;
          color: var(--text-primary);
        }

        .desc {
          font-size: 10px;
          color: var(--text-secondary);
        }

        .expire {
          font-size: 10px;
          color: var(--text-secondary);
        }
      }

      .coupon-actions {
        display: flex;
        flex-direction: column;
        align-items: flex-end;
        gap: 8px;

        .delete-icon {
          font-size: 18px;
          color: var(--text-secondary);
        }
      }
    }
  }
}
</style>
