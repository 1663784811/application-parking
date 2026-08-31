<template>
  <div class="order-page">
    <van-nav-bar title="我的订单" left-arrow @click-left="router.back()" />

    <van-tabs v-model:active="state.activeTab" @change="onTabChange">
      <van-tab title="全部" />
      <van-tab title="待支付" />
      <van-tab title="已支付" />
    </van-tabs>

    <div class="order-list">
      <div
        v-for="item in filteredList"
        :key="item.id"
        class="order-card"
        @click="goToDetail(item.id)"
      >
        <div class="order-header">
          <span class="parking-name">{{ item.parkingName }}</span>
          <div class="header-actions">
            <span class="status" :class="`status-${item.status}`">{{ item.statusText }}</span>
            <van-icon name="delete-o" class="delete-icon" @click.stop="handleDelete(item.id)" />
          </div>
        </div>
        <div class="order-body">
          <div class="info-row">
            <span class="label">车牌号</span>
            <span class="value">{{ item.plateNumber }}</span>
          </div>
          <div class="info-row">
            <span class="label">入场时间</span>
            <span class="value">{{ item.entryTime }}</span>
          </div>
          <div class="info-row">
            <span class="label">停车时长</span>
            <span class="value">{{ item.duration }}</span>
          </div>
        </div>
        <div class="order-footer">
          <span class="amount">¥{{ item.amount }}</span>
          <van-button
            v-if="item.status === 0"
            size="small"
            type="primary"
            round
          >
            去支付
          </van-button>
        </div>
      </div>

      <van-empty v-if="filteredList.length === 0" description="暂无订单" />
    </div>
  </div>
</template>

<script setup>
import { reactive, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { showToast, showConfirmDialog } from 'vant'

const route = useRoute()
const router = useRouter()

const state = reactive({
  activeTab: 0,
  // TODO: 接口获取订单列表（getOrderList）
  orderList: [
    { id: '1', parkingName: '万达广场停车场', plateNumber: '京A12345', entryTime: '2024-01-15 10:30:00', duration: '2小时30分钟', amount: '15.00', status: 1, statusText: '已支付' },
    { id: '2', parkingName: '国贸中心地下停车场', plateNumber: '京B67890', entryTime: '2024-01-14 14:20:00', duration: '4小时10分钟', amount: '32.00', status: 1, statusText: '已支付' },
    { id: '3', parkingName: '银泰中心停车场', plateNumber: '京C11111', entryTime: '2024-01-14 09:00:00', duration: '6小时45分钟', amount: '48.00', status: 0, statusText: '待支付' },
    { id: '4', parkingName: '华贸中心停车场', plateNumber: '京D22222', entryTime: '2024-01-13 18:30:00', duration: '8小时20分钟', amount: '56.00', status: 1, statusText: '已支付' },
    { id: '5', parkingName: 'SKP-S停车场', plateNumber: '京E33333', entryTime: '2024-01-13 11:00:00', duration: '3小时15分钟', amount: '25.00', status: 2, statusText: '已完成' },
    { id: '6', parkingName: '华润大厦停车场', plateNumber: '京F44444', entryTime: '2024-01-12 16:45:00', duration: '5小时30分钟', amount: '38.00', status: 0, statusText: '待支付' },
    { id: '7', parkingName: '东方新天地停车场', plateNumber: '京G55555', entryTime: '2024-01-12 10:20:00', duration: '7小时40分钟', amount: '52.00', status: 1, statusText: '已支付' },
    { id: '8', parkingName: '来福士广场停车场', plateNumber: '京H66666', entryTime: '2024-01-11 20:00:00', duration: '2小时00分钟', amount: '14.00', status: 2, statusText: '已完成' },
    { id: '9', parkingName: '三里屯SOHO停车场', plateNumber: '京J77777', entryTime: '2024-01-11 13:30:00', duration: '4小时50分钟', amount: '35.00', status: 0, statusText: '待支付' },
    { id: '10', parkingName: '太古里南区停车场', plateNumber: '京K88888', entryTime: '2024-01-10 15:00:00', duration: '6小时00分钟', amount: '42.00', status: 1, statusText: '已支付' },
  ],
})

const filteredList = computed(() => {
  if (state.activeTab === 0) {
    return state.orderList
  }
  return state.orderList.filter(item => item.status === state.activeTab - 1)
})

const onTabChange = () => {
  // tab 切换筛选（静态数据无需请求）
}

const goToDetail = (id) => {
  router.push({
    name: 'orderDetail',
    params: { appId: route.params.appId, orderId: id },
  })
}

const handleDelete = (id) => {
  showConfirmDialog({
    title: '提示',
    message: '确定要删除该订单吗？',
  }).then(() => {
    // TODO: 调用删除订单接口（deleteOrder）
    state.orderList = state.orderList.filter(item => item.id !== id)
    showToast('删除成功')
  }).catch(() => {})
}
</script>

<style scoped lang="less">
.order-page {
  display: flex;
  flex-direction: column;
  flex: 1;
  overflow: hidden;

  .order-list {
    flex: 1;
    padding: 12px 16px;
    overflow: auto;
  }

  .order-card {
    padding: 16px;
    margin-bottom: 12px;
    background: var(--bg-primary);
    border-radius: 12px;

    .order-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-bottom: 12px;
      border-bottom: 1px solid var(--border-primary);

      .parking-name {
        font-size: 14px;
        font-weight: 600;
        color: var(--text-primary);
      }

      .header-actions {
        display: flex;
        align-items: center;
        gap: 12px;
      }
    }

    .status {
      font-size: 12px;

      &.status-0 {
        color: var(--color-warning);
      }

      &.status-1 {
        color: var(--brand-primary);
      }

      &.status-2 {
        color: var(--color-success);
      }
    }

    .delete-icon {
      font-size: 18px;
      color: var(--text-secondary);
    }

    .order-body {
      padding: 12px 0;

      .info-row {
        display: flex;
        justify-content: space-between;
        margin-bottom: 6px;
        font-size: 12px;

        .label {
          color: var(--text-secondary);
        }

        .value {
          color: var(--text-primary);
        }
      }
    }

    .order-footer {
      display: flex;
      justify-content: space-between;
      align-items: center;
      padding-top: 12px;
      border-top: 1px solid var(--border-primary);

      .amount {
        font-size: 18px;
        font-weight: 600;
        color: var(--color-danger);
      }
    }
  }
}
</style>
