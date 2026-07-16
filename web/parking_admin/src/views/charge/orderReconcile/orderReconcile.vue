<template>
  <div class="order-reconcile-page">
    <!-- 筛选栏 -->
    <div class="filter-bar">
      <div class="filter-row">
        <DatePicker
          v-model="state.searchForm.date"
          format="yyyy-MM-dd"
          type="date"
          placeholder="对账日期"
          class="filter-date"
        />

        <Select v-model="state.searchForm.parkingId" placeholder="选择车场" class="filter-select" clearable>
          <Option value="1">城西停车场</Option>
          <Option value="2">城东停车场</Option>
        </Select>

        <Button type="primary" @click="handleSearch">查询</Button>
        <Button @click="handleReset">重置</Button>
      </div>
    </div>

    <!-- 汇总卡片 -->
    <div class="summary-row">
      <div class="summary-card total">
        <div class="summary-icon">
          <Icon type="logo-yen" />
        </div>
        <div class="summary-info">
          <div class="summary-label">今日总收入</div>
          <div class="summary-value">¥{{ state.summary.totalRevenue }}</div>
          <div class="summary-count">共 {{ state.summary.totalOrders }} 笔订单</div>
        </div>
      </div>
      <div class="summary-card online">
        <div class="summary-icon">
          <Icon type="logo-chrome" />
        </div>
        <div class="summary-info">
          <div class="summary-label">线上收入</div>
          <div class="summary-value">¥{{ state.summary.onlineRevenue }}</div>
          <div class="summary-count">{{ state.summary.onlineCount }} 笔</div>
        </div>
      </div>
      <div class="summary-card offline">
        <div class="summary-icon">
          <Icon type="ios-cash" />
        </div>
        <div class="summary-info">
          <div class="summary-label">线下收入</div>
          <div class="summary-value">¥{{ state.summary.offlineRevenue }}</div>
          <div class="summary-count">{{ state.summary.offlineCount }} 笔</div>
        </div>
      </div>
      <div class="summary-card diff">
        <div class="summary-icon">
          <Icon type="ios-alert" />
        </div>
        <div class="summary-info">
          <div class="summary-label">差额</div>
          <div class="summary-value" :class="{ 'has-diff': state.summary.difference !== 0 }">
            ¥{{ state.summary.difference }}
          </div>
          <div class="summary-count">{{ state.summary.difference === 0 ? '无差异' : '存在差异' }}</div>
        </div>
      </div>
    </div>

    <!-- 对账表格 -->
    <div class="reconcile-section">
      <div class="section-header">
        <h3 class="section-title">对账明细</h3>
        <Button type="primary" @click="handleExport">
          <Icon type="ios-download-outline" />
          导出报表
        </Button>
      </div>

      <Table
        :columns="columns"
        :data="state.tableData"
        :loading="state.loading"
      >
        <template #status="{ row }">
          <Badge :status="row.status === 'success' ? 'success' : 'error'" :text="getStatusText(row.status)" />
        </template>
        <template #action="{ row }">
          <Button type="text" size="small" @click="handleViewDetail(row)">查看明细</Button>
          <Button type="text" size="small" @click="handleMarkException(row)" v-if="row.status !== 'success'">
            标记异常
          </Button>
        </template>
      </Table>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import {
  DatePicker,
  Select,
  Option,
  Button,
  Icon,
  Table,
  Badge,
  Modal,
  Message
} from 'view-ui-plus'

const state = reactive({
  searchForm: {
    date: '',
    parkingId: null
  },

  summary: {
    totalRevenue: '12,580',
    totalOrders: 228,
    onlineRevenue: '10,450',
    onlineCount: 198,
    offlineRevenue: '2,130',
    offlineCount: 30,
    difference: 0
  },

  tableData: [],
  loading: false
})

const columns = [
  { title: '车场名称', key: 'parkingName', width: 180 },
  { title: '对账日期', key: 'reconcileDate', width: 120 },
  { title: '线上订单数', key: 'onlineOrders', width: 120, align: 'center' },
  { title: '线上金额', key: 'onlineAmount', width: 120, align: 'right' },
  { title: '线下订单数', key: 'offlineOrders', width: 120, align: 'center' },
  { title: '线下金额', key: 'offlineAmount', width: 120, align: 'right' },
  { title: '差异金额', key: 'diffAmount', width: 100, align: 'right' },
  { title: '对账状态', slot: 'status', width: 100, align: 'center' },
  { title: '操作', slot: 'action', width: 150, fixed: 'right' }
]

const getStatusText = (status) => {
  return status === 'success' ? '已结清' : '有差异'
}

const initData = () => {
  state.loading = true
  setTimeout(() => {
    state.tableData = [
      {
        id: 1,
        parkingName: '城西停车场',
        reconcileDate: '2024-01-15',
        onlineOrders: 145,
        onlineAmount: '8,450',
        offlineOrders: 22,
        offlineAmount: '1,580',
        diffAmount: '0',
        status: 'success'
      },
      {
        id: 2,
        parkingName: '城东停车场',
        reconcileDate: '2024-01-15',
        onlineOrders: 53,
        onlineAmount: '2,000',
        offlineOrders: 8,
        offlineAmount: '550',
        diffAmount: '85',
        status: 'error'
      },
      {
        id: 3,
        parkingName: '购物中心停车场',
        reconcileDate: '2024-01-15',
        onlineOrders: 28,
        onlineAmount: '1,890',
        offlineOrders: 12,
        offlineAmount: '980',
        diffAmount: '0',
        status: 'success'
      }
    ]
    state.loading = false
  }, 500)
}

const handleSearch = () => {
  initData()
}

const handleReset = () => {
  state.searchForm = {
    date: '',
    parkingId: null
  }
  handleSearch()
}

const handleExport = () => {
  Message.info('正在导出...')
}

const handleViewDetail = (row) => {
  console.log('查看明细', row)
}

const handleMarkException = (row) => {
  Modal.confirm({
    title: '确认标记异常',
    content: `确定要将 "${row.parkingName}" 的对账记录标记为异常吗？`,
    onOk: () => {
      Message.success('标记成功')
      initData()
    }
  })
}

initData()
</script>

<style lang="less" scoped>
.order-reconcile-page {
  .filter-bar {
    padding: var(--spacing-xl);
    background-color: var(--bg-color);
    border-radius: var(--border-radius-base);
    margin-bottom: var(--spacing-lg);
    box-shadow: var(--shadow-base);

    .filter-row {
      display: flex;
      flex-wrap: wrap;
      gap: var(--spacing-md);

      .filter-date {
        width: 200px;
      }

      .filter-select {
        width: 180px;
      }
    }
  }

  .summary-row {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: var(--spacing-lg);
    margin-bottom: var(--spacing-lg);

    .summary-card {
      display: flex;
      padding: var(--spacing-xl);
      background-color: var(--bg-color);
      border-radius: var(--border-radius-base);
      box-shadow: var(--shadow-base);

      .summary-icon {
        width: 56px;
        height: 56px;
        border-radius: var(--border-radius-base);
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: var(--spacing-xl);

        .ivu-icon {
          font-size: 28px;
          color: #fff;
        }
      }

      .summary-info {
        .summary-label {
          font-size: var(--font-size-sm);
          color: var(--text-color-secondary);
          margin-bottom: var(--spacing-xs);
        }

        .summary-value {
          font-size: 24px;
          font-weight: 600;
          color: var(--text-color-title);

          &.has-diff {
            color: var(--error-color);
          }
        }

        .summary-count {
          font-size: var(--font-size-xs);
          color: var(--text-color-secondary);
          margin-top: var(--spacing-xs);
        }
      }

      &.total .summary-icon {
        background: linear-gradient(135deg, #165DFF, #4080FF);
      }

      &.online .summary-icon {
        background: linear-gradient(135deg, #00B42A, #23D130);
      }

      &.offline .summary-icon {
        background: linear-gradient(135deg, #FF7D00, #FF9A3C);
      }

      &.diff .summary-icon {
        background: linear-gradient(135deg, #722ED1, #9254DE);
      }
    }
  }

  .reconcile-section {
    background-color: var(--bg-color);
    border-radius: var(--border-radius-base);
    padding: var(--spacing-xl);
    box-shadow: var(--shadow-base);

    .section-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: var(--spacing-lg);

      .section-title {
        font-size: var(--font-size-md);
        font-weight: 600;
        color: var(--text-color-title);
      }

      .ivu-btn {
        display: inline-flex;
        align-items: center;

        .ivu-icon {
          margin-right: 4px;
        }
      }
    }
  }
}
</style>