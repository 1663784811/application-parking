<template>
  <!-- 右中：车辆通行信息 -->
  <div class="card info-card">
    <!-- 卡片头部 -->
    <div class="card-header">
      <div class="header-left">
        <i class="fas fa-info-circle"></i>
        <h3>车辆通行信息</h3>
      </div>
      <div class="header-right">
        <span class="current-badge">
          <i class="fas fa-car"></i>
          当前车辆
        </span>
      </div>
    </div>

    <!-- 信息网格 -->
    <div class="info-grid">
      <div class="info-item" v-for="item in state.infoList" :key="item.label">
        <span class="label">{{ item.label }}</span>
        <span class="value" v-if="item.type === 'text'">{{ item.value }}</span>
        <span class="value" v-else-if="item.type === 'passType'">
          {{ item.value }}
          <span class="type-tag">{{ item.tag }}</span>
        </span>
        <span class="value" v-else-if="item.type === 'statusTag'">
          <span class="status-tag" :class="item.tagClass">{{ item.value }}</span>
        </span>
      </div>
    </div>

    <!-- 计费详情 -->
    <div class="billing-detail">
      <div class="billing-header">
        <i class="fas fa-receipt"></i>
        <span>计费明细</span>
      </div>
      <div class="billing-rows">
        <div class="billing-row" v-for="(bill, idx) in state.billing" :key="bill.label"
             :class="{ 'bill-total': idx === state.billing.length - 1 }">
          <span class="bill-label">{{ bill.label }}</span>
          <span class="bill-value">{{ bill.value }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {reactive} from 'vue'

const state = reactive({
  infoList: [
    {label: '车牌号码', value: '粤A·8K92F', type: 'text'},
    {label: '车辆类型', value: '小型轿车', type: 'text'},
    {label: '通行类型', value: '出场', tag: '出口', type: 'passType'},
    {label: '道闸', value: '#G02 主出口', type: 'text'},
    {label: '通行时间', value: '2026-08-06 14:35:42', type: 'text'},
    {label: '在场时长', value: '2小时18分', type: 'text'},
    {label: '通行状态', value: '正常通行', tagClass: 'normal', type: 'statusTag'},
    {label: '计费状态', value: '待支付', tagClass: 'pending', type: 'statusTag'}
  ],
  billing: [
    {label: '计费单价', value: '¥5/小时'},
    {label: '免费时长', value: '30min'},
    {label: '抵扣时长', value: '0min'},
    {label: '应付金额', value: '¥15.00'},
    {label: '实付金额', value: '—'}
  ]
})
</script>

<style lang="less" scoped>
.info-card {
  background: var(--color-bg-card);
  border-radius: var(--border-radius-xl);
  padding: var(--spacing-xl) var(--spacing-xxl);
  box-shadow: var(--shadow-base);
  display: flex;
  flex-direction: column;
  flex: 1;
  overflow: auto;

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

      .current-badge {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        font-size: 12px;
        padding: 4px 12px;
        border-radius: 40px;
        font-weight: var(--font-weight-medium);
        color: var(--color-primary);
        background: rgba(22, 93, 255, 0.08);

        i {
          font-size: 12px;
        }
      }
    }
  }
}

// ============================================
// 车辆通行信息模块
// ============================================
.info-card {
  .info-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: var(--spacing-md) var(--spacing-xl);
    margin-bottom: var(--spacing-md);

    .info-item {
      display: flex;
      flex-direction: column;
      gap: 2px;

      .label {
        font-size: 12px;
        text-transform: uppercase;
        color: var(--color-text-secondary);
        letter-spacing: 0.5px;
        font-weight: var(--font-weight-medium);
      }

      .value {
        font-weight: var(--font-weight-bold);
        color: var(--color-title);
        font-size: 15px;
        display: flex;
        align-items: center;
        gap: 6px;
      }

      .type-tag {
        color: var(--color-warning);
        background: rgba(255, 125, 0, 0.1);
        padding: 0 8px;
        border-radius: 40px;
        font-size: 12px;
        font-weight: var(--font-weight-bold);
      }

      .status-tag {
        padding: 0 10px;
        border-radius: 40px;
        font-size: 12px;
        font-weight: var(--font-weight-bold);
        display: inline-block;
        line-height: 1.8;

        &.normal {
          background: rgba(0, 180, 42, 0.1);
          color: var(--color-success);
        }

        &.pending {
          background: rgba(255, 125, 0, 0.1);
          color: var(--color-warning);
        }
      }
    }
  }

  // ============================================
  // 计费详情（重新设计）
  // ============================================
  .billing-detail {
    background: var(--color-bg);
    border-radius: var(--border-radius-lg);
    padding: var(--spacing-md);
    margin-top: auto;

    .billing-header {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 12px;
      font-weight: var(--font-weight-bold);
      color: var(--color-text-secondary);
      text-transform: uppercase;
      letter-spacing: 0.5px;
      padding-bottom: 8px;
      border-bottom: 1px solid var(--color-border-light);
      margin-bottom: 8px;

      i {
        font-size: 12px;
      }
    }

    .billing-rows {
      display: flex;
      flex-direction: column;
      gap: 4px;
    }

    .billing-row {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 2px 0;
      font-size: 13px;

      .bill-label {
        color: var(--color-text-secondary);
        font-weight: var(--font-weight-medium);
      }

      .bill-value {
        color: var(--color-body);
        font-weight: var(--font-weight-medium);
      }

      // 应付金额行 — 高亮
      &.bill-total {
        margin-top: 4px;
        padding-top: 6px;
        border-top: 1px dashed var(--color-border);

        .bill-label {
          color: var(--color-title);
          font-weight: var(--font-weight-bold);
          font-size: 14px;
        }

        .bill-value {
          color: var(--color-warning);
          font-weight: var(--font-weight-bold);
          font-size: 16px;
          background: rgba(255, 125, 0, 0.1);
          padding: 0 10px;
          border-radius: 40px;
          line-height: 1.6;
        }
      }
    }
  }
}
</style>
