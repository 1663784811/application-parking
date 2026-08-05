<template>
  <!-- 右下：1.2.5 道闸操作 -->
  <div class="card">
    <div class="card-header">
      <h3><i class="fas fa-remote"></i> 道闸操作 · 应急值守</h3>
      <span class="status-badge"><i class="fas fa-hand-pointer"></i> 远程控制</span>
    </div>
    <div class="operation-grid">
      <!-- 查看应缴费用 -->
      <div class="op-card">
        <div class="op-title"><i class="fas fa-receipt" style="color:#1f6e9c;"></i> 应缴费用</div>
        <div class="op-detail">
          <span>{{ state.fee.plate }}</span>
          <span><strong>¥{{ state.fee.amount.toFixed(2) }}</strong></span>
        </div>
        <div class="op-info">
          <span>停放 {{ state.fee.duration }}</span>
          <span>单价 ¥{{ state.fee.unitPrice }}/h</span>
        </div>
        <button class="op-btn secondary" @click="viewFeeDetail">
          <i class="fas fa-eye"></i> 查看完整计费
        </button>
      </div>
      <!-- 现金支付结算 -->
      <div class="op-card">
        <div class="op-title"><i class="fas fa-money-bill-wave" style="color:#1f6e9c;"></i> 现金支付结算</div>
        <div class="op-cash-input">
          <span class="op-cash-label">实收</span>
          <span class="op-cash-amount">¥{{ state.cash.received.toFixed(2) }}</span>
          <span class="op-cash-change">找零 ¥{{ state.cash.change.toFixed(2) }}</span>
        </div>
        <button class="op-btn" @click="handleCashPayment">
          <i class="fas fa-check-circle"></i> 核销账单 · 已支付
        </button>
        <div class="op-cash-record"><i class="far fa-clock"></i> 留存现金缴费记录</div>
      </div>
    </div>
    <div class="gate-status-bar">
      <span v-for="g in state.gateStatus" :key="g.name">
        <i class="fas fa-circle" :style="{ color: g.color }"></i> {{ g.name }} {{ g.state }}
      </span>
    </div>
  </div>
</template>

<script setup>
import {reactive} from 'vue'

const state = reactive({
  // 应缴费用
  fee: {
    plate: '粤A·8K92F',
    amount: 15.00,
    duration: '2h18m',
    unitPrice: 5
  },
  // 现金支付信息
  cash: {
    received: 20.00,
    change: 5.00
  },
  // 道闸状态栏
  gateStatus: [
    {name: '主入口', state: '开启', color: '#1f9d6a'},
    {name: '主出口', state: '关闭', color: '#1f9d6a'},
    {name: '应急通道', state: '故障', color: '#d45a5a'}
  ]
})

// 查看完整计费
const viewFeeDetail = () => {
  alert('查看完整计费详情：\n停放时长：2小时18分\n计费规则：首小时¥5，后续¥5/小时\n免费时长：30分钟\n抵扣时长：0分钟\n应付金额：¥15.00')
}

// 现金支付结算
const handleCashPayment = () => {
  alert('现金支付结算完成\n实收：¥20.00\n找零：¥5.00\n账单已核销，状态更新为「已支付」')
}
</script>

<style lang="less" scoped>
// ============================================
// 卡片通用样式
// ============================================
.card {
  background: #ffffff;
  border-radius: 24px;
  padding: 20px 22px;
  box-shadow: 0 8px 20px -8px rgba(0, 20, 30, 0.08);
  display: flex;
  flex-direction: column;

  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 16px;
    border-bottom: 1px solid #ecf1f7;
    padding-bottom: 12px;

    h3 {
      font-weight: 600;
      font-size: 18px;
      color: #1c3b57;
      display: flex;
      align-items: center;
      gap: 8px;

      i {
        color: #1f6e9c;
        font-size: 16px;
        width: 24px;
      }
    }

    .status-badge {
      font-size: 12px;
      background: #e7edf5;
      padding: 4px 12px;
      border-radius: 40px;
      color: #2a4d6e;
    }
  }
}

// ============================================
// 道闸操作模块
// ============================================
.operation-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 16px;

  .op-card {
    background: #f8faff;
    border-radius: 20px;
    padding: 16px 14px;
    display: flex;
    flex-direction: column;
    gap: 8px;
    min-width: 0;

    .op-title {
      font-weight: 600;
      color: #1c3b57;
      font-size: 14px;
      display: flex;
      gap: 8px;
      align-items: center;
    }

    .op-detail {
      font-size: 13px;
      color: #2f5577;
      background: #eef4fa;
      padding: 8px 12px;
      border-radius: 40px;
      display: flex;
      justify-content: space-between;
    }

    .op-info {
      font-size: 11px;
      color: #3e6180;
      display: flex;
      gap: 8px;
    }

    .op-cash-input {
      display: flex;
      gap: 8px;
      align-items: center;
      flex-wrap: wrap;

      .op-cash-label {
        font-weight: 500;
        color: #1a3e5c;
      }

      .op-cash-amount {
        background: white;
        padding: 4px 12px;
        border-radius: 40px;
        border: 1px solid #d9e2ec;
      }

      .op-cash-change {
        font-size: 11px;
        color: #2f5577;
      }
    }

    .op-cash-record {
      font-size: 11px;
      color: #4f7292;
      margin-top: 4px;
    }

    .op-btn {
      background: #0f3b5c;
      border: none;
      border-radius: 60px;
      padding: 12px 0;
      color: white;
      font-weight: 600;
      font-size: 14px;
      cursor: pointer;
      transition: 0.15s;
      display: flex;
      align-items: center;
      justify-content: center;
      gap: 6px;
      box-shadow: 0 4px 10px rgba(15, 59, 92, 0.15);
      margin-top: 4px;

      &:hover {
        background: #134b73;
        transform: scale(1.01);
      }

      &.secondary {
        background: #eaf1fa;
        color: #1a3e5c;
        box-shadow: none;

        &:hover {
          background: #d6e2f0;
        }
      }
    }
  }
}

.gate-status-bar {
  margin-top: 10px;
  background: #ecf3fa;
  border-radius: 40px;
  padding: 6px 16px;
  font-size: 12px;
  color: #1d4b6e;
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}
</style>
