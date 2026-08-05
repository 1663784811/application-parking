<template>
  <!-- 右中：车辆通行信息 -->
  <div class="card info-card">
    <div class="card-header">
      <h3><i class="fas fa-info-circle"></i> 车辆通行信息</h3>
      <span class="status-badge"><i class="fas fa-car"></i> 当前车辆</span>
    </div>
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
      <span v-for="bill in state.billing" :key="bill.label">
        <strong>{{ bill.label }}</strong> {{ bill.value }}
      </span>
    </div>
  </div>
</template>

<script setup>
import {reactive} from 'vue'

const state = reactive({
  // 车辆通行信息字段
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
  // 计费详情
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
// 车辆通行信息模块
// ============================================
.info-card {
  flex: 1;

  .info-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: 12px 20px;
    margin-bottom: 12px;

    .info-item {
      display: flex;
      flex-direction: column;
      gap: 2px;

      .label {
        font-size: 10px;
        text-transform: uppercase;
        color: #627b94;
        letter-spacing: 0.3px;
      }

      .value {
        font-weight: 600;
        color: #0b2b44;
        font-size: 15px;
        display: flex;
        align-items: center;
        gap: 6px;
      }

      .type-tag {
        color: #a65f2b;
        background: #f0e2d4;
        padding: 0 8px;
        border-radius: 40px;
        font-size: 11px;
      }

      .status-tag {
        padding: 0 12px;
        border-radius: 40px;
        font-size: 11px;
        font-weight: 600;
        display: inline-block;

        &.normal {
          background: #d4f0e3;
          color: #0f6d4a;
        }

        &.pending {
          background: #f0e2d4;
          color: #a65f2b;
        }
      }
    }
  }

  .billing-detail {
    background: #f0f7fe;
    border-radius: 20px;
    padding: 10px 16px;
    display: flex;
    flex-wrap: wrap;
    gap: 10px 18px;
    font-size: 13px;

    span {
      strong {
        margin-right: 4px;
      }
    }
  }
}
</style>
