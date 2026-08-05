<template>
  <!-- 左下：1.2.2 车辆通行记录 (flex:1 占满剩余空间) -->
  <div class="card record-card">
    <div class="card-header">
      <h3><i class="fas fa-list-ul"></i> 车辆通行记录</h3>
      <span class="status-badge"><i class="fas fa-clock"></i> 实时更新</span>
    </div>
    <div class="record-list">
      <div class="record-item" v-for="record in state.records" :key="record.id">
        <span class="plate">{{ record.plate }}</span>
        <span class="meta">
          <span>{{ record.gate }}</span>
          <span class="badge-type" :class="record.type">{{ record.typeText }}</span>
          <span>{{ record.time }}</span>
          <span :style="{ color: record.statusColor }">{{ record.status }}</span>
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'

const state = reactive({
  // 车辆通行记录数据
  records: [
    {
      id: 1,
      plate: '粤A·8K92F',
      gate: '#G02 主出口',
      type: 'out',
      typeText: '出场',
      time: '14:35:42',
      status: '正常',
      statusColor: '#1f9d6a'
    },
    {
      id: 2,
      plate: '粤B·3T67H',
      gate: '#G01 主入口',
      type: 'in',
      typeText: '入场',
      time: '14:28:15',
      status: '正常',
      statusColor: '#1f9d6a'
    },
    {
      id: 3,
      plate: '苏C·9M12R',
      gate: '#G03 地下车库',
      type: 'in',
      typeText: '入场',
      time: '14:10:02',
      status: '拦截失败',
      statusColor: '#d45a5a'
    },
    {
      id: 4,
      plate: '粤A·5X88L',
      gate: '#G02 主出口',
      type: 'out',
      typeText: '出场',
      time: '13:55:20',
      status: '人工放行',
      statusColor: '#e8a83a'
    },
    {
      id: 5,
      plate: '浙F·2A91K',
      gate: '#G04 应急通道',
      type: 'out',
      typeText: '出场',
      time: '13:40:11',
      status: '正常',
      statusColor: '#1f9d6a'
    },
    {
      id: 6,
      plate: '粤S·6G33F',
      gate: '#G01 主入口',
      type: 'in',
      typeText: '入场',
      time: '13:22:48',
      status: '正常',
      statusColor: '#1f9d6a'
    }
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
// 车辆通行记录模块
// ============================================
.record-card {
  flex: 1;

  .record-list {
    flex: 1;
    overflow-y: auto;
    display: flex;
    flex-direction: column;
    gap: 8px;
    min-height: 0;

    &::-webkit-scrollbar {
      width: 4px;
    }

    &::-webkit-scrollbar-thumb {
      background: #c0d0e0;
      border-radius: 20px;
    }

    .record-item {
      display: flex;
      align-items: center;
      justify-content: space-between;
      padding: 8px 12px;
      background: #f8faff;
      border-radius: 16px;
      font-size: 14px;
      border-bottom: 1px solid #eef4fa;
      flex-shrink: 0;

      .plate {
        font-weight: 600;
        color: #0b2b44;
        letter-spacing: 0.5px;
      }

      .meta {
        color: #3e6180;
        display: flex;
        gap: 12px;
        align-items: center;
        flex-wrap: wrap;
      }

      .badge-type {
        background: #dce6f0;
        padding: 0 12px;
        border-radius: 40px;
        font-size: 11px;
        font-weight: 600;
        color: #1f4b6e;

        &.in {
          background: #d4f0e3;
          color: #0f6d4a;
        }

        &.out {
          background: #f0e2d4;
          color: #a65f2b;
        }
      }
    }
  }
}
</style>
