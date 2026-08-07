<template>
  <!-- 右下：道闸操作 · 应急值守 -->
  <div class="card gate-op-card">
    <!-- 卡片头部 -->
    <div class="card-header">
      <div class="header-left">
        <i class="fas fa-remote"></i>
        <h3>道闸操作 · 应急值守</h3>
      </div>
      <div class="header-right">
        <span class="remote-badge">
          <i class="fas fa-hand-pointer"></i>
          远程控制
        </span>
      </div>
    </div>

    <!-- 操作网格 -->
    <div class="operation-grid">
      <!-- 查看应缴费用 -->
      <div class="op-card">
        <div class="op-title">
          <i class="fas fa-receipt"></i>
          应缴费用
        </div>
        <div class="op-detail">
          <span class="op-plate">{{ state.fee.plate }}</span>
          <span class="op-amount">¥{{ state.fee.amount.toFixed(2) }}</span>
        </div>
        <div class="op-info">
          <span><i class="far fa-clock"></i> 停放 {{ state.fee.duration }}</span>
          <span class="info-divider"></span>
          <span>单价 ¥{{ state.fee.unitPrice }}/h</span>
        </div>
        <button class="op-btn secondary" @click="viewFeeDetail">
          <i class="fas fa-eye"></i>
          查看完整计费
        </button>
      </div>

      <!-- 现金支付结算 -->
      <div class="op-card">
        <div class="op-title">
          <i class="fas fa-money-bill-wave"></i>
          现金支付结算
        </div>
        <div class="op-cash-row">
          <span class="op-cash-label">实收</span>
          <span class="op-cash-value">¥{{ state.cash.received.toFixed(2) }}</span>
          <span class="op-cash-change">找零 ¥{{ state.cash.change.toFixed(2) }}</span>
        </div>
        <button class="op-btn primary" @click="handleCashPayment">
          <i class="fas fa-check-circle"></i>
          核销账单 · 已支付
        </button>
        <div class="op-cash-note">
          <i class="far fa-clock"></i>
          留存现金缴费记录
        </div>
      </div>
    </div>

    <!-- 道闸状态栏 -->
    <div class="gate-status-bar">
      <span v-for="g in state.gateStatus" :key="g.name">
        <i class="fas fa-circle" :style="{ color: g.color }"></i>
        {{ g.name }}
        <span class="gate-state" :class="g.stateClass">{{ g.state }}</span>
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
    {name: '主入口', state: '开启', stateClass: 'open', color: 'var(--color-success)'},
    {name: '主出口', state: '关闭', stateClass: 'closed', color: 'var(--color-text-secondary)'},
    {name: '应急通道', state: '故障', stateClass: 'fault', color: 'var(--color-danger)'}
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
  background: var(--color-bg-card);
  border-radius: var(--border-radius-xl);
  padding: var(--spacing-xl) var(--spacing-xxl);
  box-shadow: var(--shadow-base);
  display: flex;
  flex-direction: column;

  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: var(--spacing-lg);
    border-bottom: 1px solid var(--color-border-light);
    padding-bottom: var(--spacing-md);

    .header-left {
      display: flex;
      align-items: center;
      gap: var(--spacing-sm);

      > i {
        color: var(--color-primary);
        font-size: var(--font-size-md);
      }

      h3 {
        font-weight: var(--font-weight-bold);
        font-size: var(--font-size-lg);
        color: var(--color-title);
        white-space: nowrap;
      }
    }

    .header-right {
      display: flex;
      align-items: center;
      gap: var(--spacing-sm);
      flex-shrink: 0;

      .remote-badge {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        font-size: 11px;
        padding: 4px 12px;
        border-radius: 40px;
        font-weight: var(--font-weight-medium);
        color: var(--color-primary);
        background: rgba(22, 93, 255, 0.08);

        i {
          font-size: 11px;
        }
      }
    }
  }
}

// ============================================
// 道闸操作模块
// ============================================
.gate-op-card {
  .operation-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: var(--spacing-md);

    .op-card {
      background: var(--color-bg);
      border-radius: var(--border-radius-lg);
      padding: var(--spacing-md) var(--spacing-lg);
      display: flex;
      flex-direction: column;
      gap: var(--spacing-sm);
      min-width: 0;
      border: 1px solid var(--color-border-light);
      transition: border-color var(--transition-fast);

      &:hover {
        border-color: var(--color-border);
      }

      .op-title {
        font-weight: var(--font-weight-bold);
        color: var(--color-title);
        font-size: 14px;
        display: flex;
        gap: var(--spacing-sm);
        align-items: center;

        i {
          color: var(--color-primary);
          font-size: 14px;
          width: 18px;
        }
      }

      .op-detail {
        font-size: 13px;
        color: var(--color-body);
        background: var(--color-bg-card);
        padding: 8px 12px;
        border-radius: 40px;
        display: flex;
        justify-content: space-between;
        border: 1px solid var(--color-border);

        .op-plate {
          font-weight: var(--font-weight-medium);
        }

        .op-amount {
          font-weight: var(--font-weight-bold);
          color: var(--color-primary);
          font-size: 15px;
        }
      }

      .op-info {
        font-size: 11px;
        color: var(--color-text-secondary);
        display: flex;
        align-items: center;
        gap: var(--spacing-sm);

        i {
          font-size: 10px;
        }

        .info-divider {
          width: 1px;
          height: 10px;
          background: var(--color-border);
        }
      }

      .op-cash-row {
        display: flex;
        align-items: center;
        gap: var(--spacing-sm);
        flex-wrap: wrap;

        .op-cash-label {
          font-weight: var(--font-weight-medium);
          color: var(--color-text-secondary);
          font-size: 12px;
        }

        .op-cash-value {
          background: var(--color-bg-card);
          padding: 4px 12px;
          border-radius: 40px;
          border: 1px solid var(--color-border);
          font-weight: var(--font-weight-bold);
          color: var(--color-title);
          font-size: 14px;
        }

        .op-cash-change {
          font-size: 11px;
          color: var(--color-warning);
          font-weight: var(--font-weight-medium);
        }
      }

      .op-cash-note {
        font-size: 11px;
        color: var(--color-text-secondary);
        display: flex;
        align-items: center;
        gap: 4px;

        i {
          font-size: 10px;
        }
      }

      .op-btn {
        border: none;
        border-radius: 60px;
        padding: 10px 0;
        font-weight: var(--font-weight-bold);
        font-size: 13px;
        cursor: pointer;
        transition: all var(--transition-fast);
        display: flex;
        align-items: center;
        justify-content: center;
        gap: 6px;

        i {
          font-size: 13px;
        }

        &.primary {
          background: var(--color-primary);
          color: #ffffff;
          box-shadow: 0 4px 10px rgba(22, 93, 255, 0.2);

          &:hover {
            background: var(--color-primary-dark);
            transform: translateY(-1px);
          }

          &:active {
            transform: translateY(0);
          }
        }

        &.secondary {
          background: var(--color-bg-card);
          color: var(--color-body);
          border: 1px solid var(--color-border);

          &:hover {
            border-color: var(--color-primary);
            color: var(--color-primary);
            background: rgba(22, 93, 255, 0.04);
          }
        }
      }
    }
  }

  // 道闸状态栏
  .gate-status-bar {
    margin-top: var(--spacing-md);
    background: var(--color-bg);
    border-radius: 40px;
    padding: 6px 16px;
    font-size: 12px;
    color: var(--color-body);
    display: flex;
    gap: var(--spacing-lg);
    flex-wrap: wrap;

    i {
      font-size: 7px;
    }

    .gate-state {
      font-weight: var(--font-weight-medium);

      &.open { color: var(--color-success); }
      &.closed { color: var(--color-text-secondary); }
      &.fault { color: var(--color-danger); }
    }
  }
}
</style>