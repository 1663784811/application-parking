<template>
  <!-- 右下：操作 -->
  <div class="card gate-op-card">
    <!-- 卡片头部 -->
    <div class="card-header">
      <div class="header-left">
        <i class="fas fa-bolt"></i>
        <span class="header-plate">{{ state.plate }}</span>
        <span class="header-fee">¥{{ state.fee.toFixed(2) }}</span>
      </div>
      <div class="header-right">
        <div class="gate-select">
          <span class="gate-label">道闸</span>
          <i class="fas fa-road"></i>
          <Select v-model="state.selectedGate" transfer>
            <Option v-for="g in state.gates" :key="g.value" :value="g.value">{{ g.label }}</Option>
          </Select>
        </div>
      </div>
    </div>

    <!-- 操作按钮 -->
    <div class="action-grid">
      <button class="action-btn pass-btn" @click="handleDirectPass">
        <i class="fas fa-check-circle"></i>
        <span class="btn-text">直接放行</span>
      </button>
      <button class="action-btn cash-btn" @click="handleCashPayment">
        <i class="fas fa-money-bill-wave"></i>
        <span class="btn-text">现金支付</span>
      </button>
    </div>
  </div>
</template>

<script setup>
import {reactive} from 'vue'

const state = reactive({
  selectedGate: 'G02',
  gates: [
    {value: 'G01', label: '#G01 主入口'},
    {value: 'G02', label: '#G02 主出口'},
    {value: 'G03', label: '#G03 地下车库'},
    {value: 'G04', label: '#G04 应急通道'}
  ],
  plate: '粤A·8K92F',
  fee: 15.00
})

const handleDirectPass = () => {
  alert(`道闸已开启，车辆已放行 [${state.selectedGate}]`)
}

const handleCashPayment = () => {
  alert('现金支付结算完成，账单已核销')
}
</script>

<style lang="less" scoped>
// ============================================
// 卡片通用样式
// ============================================
.card {
  background: var(--color-bg-card);
  border-radius: var(--border-radius-xl);
  padding: var(--spacing-lg) var(--spacing-xxl);
  box-shadow: var(--shadow-base);
  display: flex;
  flex-direction: column;

  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 12px;
    border-bottom: 1px solid var(--color-border-light);
    padding-bottom: 10px;

    .header-left {
      display: flex;
      align-items: center;
      gap: var(--spacing-sm);

      > i {
        color: var(--color-primary);
        font-size: var(--font-size-md);
      }

      .header-plate {
        font-size: 15px;
        font-weight: var(--font-weight-bold);
        color: var(--color-title);
        letter-spacing: 0.5px;
      }

      .header-fee {
        font-size: 18px;
        font-weight: var(--font-weight-bold);
        color: var(--color-warning);
        background: rgba(255, 125, 0, 0.1);
        padding: 2px 12px;
        border-radius: 40px;
        line-height: 1.4;
      }
    }

    .header-right {
      display: flex;
      align-items: center;
      gap: var(--spacing-sm);
      flex-shrink: 0;

      .gate-select {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 0;
        border-radius: 8px;
        background: #fff;
        border: 1px solid var(--color-border);
        transition: border-color var(--transition-fast), box-shadow var(--transition-fast);
        box-shadow: 0 2px 6px rgba(0, 0, 0, 0.06);
        overflow: hidden;

        &:hover {
          border-color: var(--color-primary);
          box-shadow: 0 2px 10px rgba(22, 93, 255, 0.15);
        }

        .gate-label {
          font-size: 12px;
          color: var(--color-text-secondary);
          font-weight: var(--font-weight-medium);
          margin-left: 12px;
          flex-shrink: 0;
        }

        > i {
          font-size: 14px;
          color: var(--color-primary);
          flex-shrink: 0;
        }

        :deep(.ivu-select) {
          flex: 1;
        }

        :deep(.ivu-select-selection) {
          border: none !important;
          box-shadow: none !important;
          background: transparent;
          border-radius: 0;
          height: 38px;
        }

        :deep(.ivu-select-placeholder),
        :deep(.ivu-select-selected-value) {
          font-size: 13px;
          font-weight: var(--font-weight-bold);
          color: var(--color-title);
          padding-left: 4px;
        }

        :deep(.ivu-select-arrow) {
          font-size: 10px;
          color: var(--color-text-secondary);
          transition: color var(--transition-fast);
          right: 12px;
        }

        &:hover :deep(.ivu-select-arrow) {
          color: var(--color-primary);
        }
      }
    }
  }
}

// ============================================
// 操作按钮
// ============================================
.gate-op-card {
  .action-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 10px;

    .action-btn {
      border: none;
      border-radius: 10px;
      padding: 0;
      height: 72px;
      cursor: pointer;
      transition: all var(--transition-fast);
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 6px;
      position: relative;
      overflow: hidden;

      i {
        font-size: 18px;
        position: relative;
        z-index: 1;
        transition: transform var(--transition-fast);
      }

      .btn-text {
        font-size: 14px;
        font-weight: var(--font-weight-bold);
        position: relative;
        z-index: 1;
        letter-spacing: 0.5px;
      }

      // 直接放行 - 绿色
      &.pass-btn {
        background: linear-gradient(135deg, #00B42A 0%, #27C24C 100%);
        color: #fff;
        box-shadow: 0 4px 14px rgba(0, 180, 42, 0.3);

        &::before {
          content: '';
          position: absolute;
          inset: 0;
          background: radial-gradient(ellipse at 30% 20%, rgba(255,255,255,0.18) 0%, transparent 60%);
          pointer-events: none;
        }

        &:hover {
          box-shadow: 0 8px 24px rgba(0, 180, 42, 0.45);
          transform: translateY(-2px);

          i {
            transform: scale(1.15);
          }
        }

        &:active {
          box-shadow: 0 2px 6px rgba(0, 180, 42, 0.25);
          transform: translateY(0);
        }
      }

      // 现金支付 - 橙色
      &.cash-btn {
        background: linear-gradient(135deg, #FF7D00 0%, #FF9A2E 100%);
        color: #fff;
        box-shadow: 0 4px 14px rgba(255, 125, 0, 0.25);

        &::before {
          content: '';
          position: absolute;
          inset: 0;
          background: radial-gradient(ellipse at 30% 20%, rgba(255,255,255,0.15) 0%, transparent 60%);
          pointer-events: none;
        }

        &:hover {
          box-shadow: 0 8px 24px rgba(255, 125, 0, 0.4);
          transform: translateY(-2px);

          i {
            transform: scale(1.15);
          }
        }

        &:active {
          box-shadow: 0 2px 6px rgba(255, 125, 0, 0.25);
          transform: translateY(0);
        }
      }
    }
  }
}
</style>