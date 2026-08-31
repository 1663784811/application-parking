<template>
  <div class="card-package-page">
    <van-nav-bar title="卡包" left-arrow @click-left="router.back()" />

    <div class="card-list">
      <div
        v-for="item in state.cardList"
        :key="item.id"
        class="card-item"
        :style="{ '--card-color': item.color }"
      >
        <div class="card-header">
          <span class="card-name">{{ item.name }}</span>
          <van-tag :type="getStatusType(item.status)" size="medium">
            {{ getStatusText(item.status) }}
          </van-tag>
        </div>
        <div class="card-body">
          <p class="parking-name">
            <van-icon name="location-o" />
            {{ item.parkingName }}
          </p>
          <p class="expire-time">
            <van-icon name="clock-o" />
            有效期至 {{ item.expireTime }}
          </p>
          <p v-if="item.totalTimes" class="times-info">
            <van-icon name="orders-o" />
            剩余 {{ item.remainTimes }} / {{ item.totalTimes }} 次
          </p>
        </div>
      </div>

      <van-empty v-if="state.cardList.length === 0" description="暂无卡包" />
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'

const state = reactive({
  // TODO: 接口获取卡包列表（getCardList），含次卡/月卡/季卡/年卡
  cardList: [
    { id: '1', name: '次卡A', type: 'ticket', parkingName: '万达广场停车场', expireTime: '2024-12-31', status: 0, totalTimes: 50, usedTimes: 12, remainTimes: 38, color: '#10b981' },
    { id: '2', name: '月卡B', type: 'monthly', parkingName: '国贸中心停车场', expireTime: '2024-06-30', status: 0, totalTimes: 0, usedTimes: 0, remainTimes: 0, color: '#3b82f6' },
    { id: '3', name: '季卡C', type: 'quarterly', parkingName: '银泰中心停车场', expireTime: '2024-09-15', status: 1, totalTimes: 0, usedTimes: 0, remainTimes: 0, color: '#f59e0b' },
    { id: '4', name: '年卡D', type: 'yearly', parkingName: '华贸中心停车场', expireTime: '2025-01-01', status: 0, totalTimes: 0, usedTimes: 0, remainTimes: 0, color: '#8b5cf6' },
    { id: '5', name: '次卡E', type: 'ticket', parkingName: 'SKP-S停车场', expireTime: '2024-08-20', status: 0, totalTimes: 100, usedTimes: 45, remainTimes: 55, color: '#10b981' },
    { id: '6', name: '月卡F', type: 'monthly', parkingName: '华润大厦停车场', expireTime: '2024-05-10', status: 2, totalTimes: 0, usedTimes: 0, remainTimes: 0, color: '#3b82f6' },
    { id: '7', name: '次卡G', type: 'ticket', parkingName: '东方新天地停车场', expireTime: '2024-11-30', status: 0, totalTimes: 30, usedTimes: 8, remainTimes: 22, color: '#10b981' },
    { id: '8', name: '季卡H', type: 'quarterly', parkingName: '来福士广场停车场', expireTime: '2024-07-25', status: 1, totalTimes: 0, usedTimes: 0, remainTimes: 0, color: '#f59e0b' },
  ],
})

const getStatusText = (status) => {
  const map = { 0: '有效', 1: '即将过期', 2: '已过期' }
  return map[status] || '未知'
}

const getStatusType = (status) => {
  const map = { 0: 'success', 1: 'warning', 2: 'default' }
  return map[status] || 'default'
}
</script>

<style scoped lang="less">
.card-package-page {
  display: flex;
  flex-direction: column;
  flex: 1;
  overflow: hidden;

  .card-list {
    flex: 1;
    padding: 16px;
    overflow: auto;
  }

  .card-item {
    padding: 16px;
    margin-bottom: 12px;
    background: linear-gradient(135deg, var(--card-color), var(--text-tertiary) 80%);
    border-radius: var(--radius-md);
    color: var(--on-brand-primary);

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 12px;

      .card-name {
        font-size: 18px;
        font-weight: 600;
      }
    }

    .card-body {
      .parking-name,
      .expire-time,
      .times-info {
        display: flex;
        align-items: center;
        gap: 6px;
        font-size: 12px;
        opacity: 0.9;
        margin-bottom: 6px;
      }

      .times-info {
        margin-bottom: 0;
      }
    }
  }
}
</style>
