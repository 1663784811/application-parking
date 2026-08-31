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
          <span class="amount">{{ formatAmount(item.amount) }}</span>
          <span class="condition">{{ item.condition }}</span>
        </div>
        <div class="coupon-right">
          <div class="coupon-info">
            <span class="name">{{ item.name }}</span>
            <span class="desc">{{ item.description }}</span>
            <span class="expire">有效期至 {{ item.expireTime }}</span>
          </div>
          <div class="coupon-actions">
            <van-tag :type="getStatusType(item.status)" size="medium">
              {{ getStatusText(item.status) }}
            </van-tag>
            <van-icon
              v-if="item.status !== 1"
              name="delete-o"
              class="delete-icon"
              @click="handleDelete(item.id)"
            />
          </div>
        </div>
      </div>

      <van-empty v-if="state.couponList.length === 0" description="暂无优惠券" />
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'

const router = useRouter()

const state = reactive({
  // TODO: 接口获取优惠券列表（getCouponList）
  couponList: [
    { id: '1', name: '新人专享券', description: '全场通用', amount: 10, condition: '无门槛', expireTime: '2024-12-31', status: 0 },
    { id: '2', name: '停车满减券', description: '停车费用满50可用', amount: 5, condition: '满50可用', expireTime: '2024-06-30', status: 0 },
    { id: '3', name: '7折停车券', description: '最高抵扣20元', amount: 0.7, condition: '7折优惠', expireTime: '2024-09-15', status: 0 },
    { id: '4', name: '平日畅停券', description: '工作日专用', amount: 8, condition: '满30可用', expireTime: '2024-08-20', status: 0 },
    { id: '5', name: '周末特惠券', description: '仅限周末使用', amount: 15, condition: '满100可用', expireTime: '2024-07-25', status: 0 },
    { id: '6', name: '夜间停车券', description: '18:00-次日8:00', amount: 20, condition: '满60可用', expireTime: '2024-10-10', status: 1 },
    { id: '7', name: '会员专享券', description: 'VIP会员专属', amount: 12, condition: '满80可用', expireTime: '2024-11-30', status: 1 },
    { id: '8', name: '新用户礼包', description: '首次停车可用', amount: 5, condition: '无门槛', expireTime: '2024-05-10', status: 2 },
    { id: '9', name: '限时秒杀券', description: '限时5折', amount: 0.5, condition: '5折优惠', expireTime: '2024-06-30', status: 2 },
    { id: '10', name: '老用户回馈', description: '连续使用3次', amount: 10, condition: '满50可用', expireTime: '2024-08-15', status: 0 },
  ],
})

const formatAmount = (amount) => {
  return amount >= 1 ? `¥${amount}` : `${amount * 10}折`
}

const getStatusText = (status) => {
  const map = { 0: '可用', 1: '已使用', 2: '已过期' }
  return map[status] || '未知'
}

const getStatusType = (status) => {
  const map = { 0: 'success', 1: 'default', 2: 'danger' }
  return map[status] || 'default'
}

const handleDelete = (id) => {
  showConfirmDialog({
    title: '提示',
    message: '确定要删除该优惠券吗？',
  }).then(() => {
    // TODO: 调用删除优惠券接口（deleteCoupon）
    state.couponList = state.couponList.filter(item => item.id !== id)
    showToast('删除成功')
  }).catch(() => {})
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
