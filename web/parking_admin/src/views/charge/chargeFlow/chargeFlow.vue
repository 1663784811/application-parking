<template>
  <div class="charge-flow-page">
    <!-- 筛选栏 -->
    <div class="filter-bar">
      <div class="filter-row">
        <div class="filter-item">
          <RangePicker
            v-model="state.searchForm.dateRange"
            :show-time="true"
            format="YYYY-MM-DD HH:mm:ss"
            placeholder="通行时间"
            class="filter-date"
          />
        </div>

        <div class="filter-item">
          <Select v-model="state.searchForm.parkingId" placeholder="选择车场" class="filter-select" clearable>
            <Option value="1">城西停车场</Option>
            <Option value="2">城东停车场</Option>
          </Select>
        </div>

        <div class="filter-item">
          <Input v-model="state.searchForm.plate" placeholder="输入车牌号" class="filter-input" clearable />
        </div>

        <div class="filter-item">
          <Select v-model="state.searchForm.payType" placeholder="支付方式" class="filter-select" clearable>
            <Option value="wechat">微信支付</Option>
            <Option value="alipay">支付宝</Option>
            <Option value="cash">现金</Option>
            <Option value="card">月卡抵扣</Option>
          </Select>
        </div>
      </div>

      <div class="filter-row">
        <div class="filter-item">
          <Select v-model="state.searchForm.orderStatus" placeholder="订单状态" class="filter-select" clearable>
            <Option value="pending">待支付</Option>
            <Option value="paid">已结清</Option>
            <Option value="refund">已退款</Option>
          </Select>
        </div>

        <div class="filter-item">
          <Button type="primary" @click="handleSearch">查询</Button>
        </div>
        <div class="filter-item">
          <Button @click="handleReset">重置</Button>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card">
        <div class="stat-label">今日总营收</div>
        <div class="stat-value">¥{{ state.stats.todayRevenue }}</div>
        <div class="stat-trend up">{{ state.stats.revenueTrend > 0 ? '↑' : '↓' }}{{ Math.abs(state.stats.revenueTrend) }}%</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">待结清订单</div>
        <div class="stat-value">{{ state.stats.pendingOrders }}</div>
        <div class="stat-sub">待收 ¥{{ state.stats.pendingAmount }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">微信支付</div>
        <div class="stat-value">¥{{ state.stats.wechatRevenue }}</div>
        <div class="stat-sub">{{ state.stats.wechatCount }} 笔</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">支付宝</div>
        <div class="stat-value">¥{{ state.stats.alipayRevenue }}</div>
        <div class="stat-sub">{{ state.stats.alipayCount }} 笔</div>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="table-container">
      <Table
        :columns="columns"
        :data="state.tableData"
        :loading="state.loading"
      >
        <template #orderStatus="{ row }">
          <Badge :status="getOrderStatusBadge(row.orderStatus)" :text="getOrderStatusText(row.orderStatus)" />
        </template>
        <template #payType="{ row }">
          {{ getPayTypeText(row.payType) }}
        </template>
        <template #action="{ row }">
          <Button type="text" size="small" @click="handleViewOrder(row)">查看</Button>
          <Button type="text" size="small" @click="handleInvoice(row)" v-if="row.orderStatus === 'paid'">开发票</Button>
          <Button type="text" size="small" @click="handleRefund(row)" v-if="row.orderStatus === 'paid'" class="text-danger">
            退款
          </Button>
        </template>
      </Table>

      <div class="pagination-wrapper">
        <Page
          :total="state.pagination.total"
          :current="state.pagination.current"
          :page-size="state.pagination.pageSize"
          show-total
          show-elevator
          @on-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import {
  RangePicker,
  Select,
  Option,
  Input,
  Button,
  Icon,
  Table,
  Badge,
  Page,
  Modal,
  Message
} from 'view-ui-plus'

const state = reactive({
  searchForm: {
    dateRange: [],
    parkingId: null,
    plate: '',
    payType: null,
    orderStatus: null
  },

  stats: {
    todayRevenue: '12,580',
    revenueTrend: 15.6,
    pendingOrders: 8,
    pendingAmount: '356',
    wechatRevenue: '8,450',
    wechatCount: 156,
    alipayRevenue: '3,890',
    alipayCount: 72
  },

  tableData: [],
  loading: false,

  pagination: {
    total: 0,
    current: 1,
    pageSize: 10
  }
})

const columns = [
  { title: '订单号', key: 'orderNo', minWidth: 180 },
  { title: '车牌号', key: 'plate', minWidth: 120 },
  { title: '进场时间', key: 'inTime', minWidth: 160 },
  { title: '出场时间', key: 'outTime', minWidth: 160 },
  { title: '停车时长', key: 'duration', minWidth: 100 },
  { title: '应付金额', key: 'payableAmount', minWidth: 100, align: 'right' },
  { title: '实付金额', key: 'paidAmount', minWidth: 100, align: 'right' },
  { title: '优惠金额', key: 'discountAmount', minWidth: 100, align: 'right' },
  { title: '支付方式', slot: 'payType', minWidth: 100 },
  { title: '操作员', key: 'operator', minWidth: 100 },
  { title: '订单状态', slot: 'orderStatus', minWidth: 100, align: 'center' },
  { title: '操作', slot: 'action', minWidth: 150, fixed: 'right' }
]

const getOrderStatusText = (status) => {
  const map = { pending: '待支付', paid: '已结清', refund: '已退款' }
  return map[status] || status
}

const getOrderStatusBadge = (status) => {
  const map = { pending: 'error', paid: 'success', refund: 'warning' }
  return map[status] || 'default'
}

const getPayTypeText = (type) => {
  const map = { wechat: '微信支付', alipay: '支付宝', cash: '现金', card: '月卡抵扣' }
  return map[type] || type
}

const initData = () => {
  state.loading = true
  setTimeout(() => {
    state.tableData = [
      {
        id: 1,
        orderNo: 'P20240115143215001',
        plate: '京A12345',
        inTime: '2024-01-15 09:00:00',
        outTime: '2024-01-15 11:30:00',
        duration: '2小时30分',
        payableAmount: 15,
        paidAmount: 15,
        discountAmount: 0,
        payType: 'wechat',
        operator: '张三',
        orderStatus: 'paid'
      },
      {
        id: 2,
        orderNo: 'P20240115132020002',
        plate: '京B67890',
        inTime: '2024-01-15 08:00:00',
        outTime: '-',
        duration: '-',
        payableAmount: 20,
        paidAmount: 0,
        discountAmount: 0,
        payType: '-',
        operator: '-',
        orderStatus: 'pending'
      },
      {
        id: 3,
        orderNo: 'P20240115124530003',
        plate: '浙C11111',
        inTime: '2024-01-15 07:00:00',
        outTime: '2024-01-15 14:00:00',
        duration: '7小时',
        payableAmount: 35,
        paidAmount: 30,
        discountAmount: 5,
        payType: 'alipay',
        operator: '李四',
        orderStatus: 'paid'
      },
      {
        id: 4,
        orderNo: 'P20240115112010004',
        plate: '京D22222',
        inTime: '2024-01-14 22:00:00',
        outTime: '2024-01-15 10:00:00',
        duration: '12小时',
        payableAmount: 60,
        paidAmount: 60,
        discountAmount: 0,
        payType: 'cash',
        operator: '王五',
        orderStatus: 'refund'
      }
    ]
    state.pagination.total = 4
    state.loading = false
  }, 500)
}

const handleSearch = () => {
  state.pagination.current = 1
  initData()
}

const handleReset = () => {
  state.searchForm = {
    dateRange: [],
    parkingId: null,
    plate: '',
    payType: null,
    orderStatus: null
  }
  handleSearch()
}

const handleViewOrder = (row) => {
  console.log('查看订单', row)
}

const handleInvoice = (row) => {
  Message.success('正在开具发票...')
}

const handleRefund = (row) => {
  Modal.confirm({
    title: '确认退款',
    content: `确定要对订单 "${row.orderNo}" 进行退款？退款金额：¥${row.paidAmount}`,
    onOk: () => {
      Message.success('退款成功')
      initData()
    }
  })
}

const handlePageChange = (page) => {
  state.pagination.current = page
  initData()
}

initData()
</script>

<style lang="less" scoped>
.charge-flow-page {
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
      margin-bottom: var(--spacing-md);

      &:last-of-type {
        margin-bottom: 0;
      }

      .filter-item {
        flex-shrink: 0;
      }

      .filter-date {
        width: 320px;
      }

      .filter-select {
        width: 150px;
      }

      .filter-input {
        width: 180px;
      }
    }
  }

  .stats-row {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: var(--spacing-lg);
    margin-bottom: var(--spacing-lg);

    .stat-card {
      padding: var(--spacing-xl);
      background-color: var(--bg-color);
      border-radius: var(--border-radius-base);
      box-shadow: var(--shadow-base);

      .stat-label {
        font-size: var(--font-size-sm);
        color: var(--text-color-secondary);
        margin-bottom: var(--spacing-sm);
      }

      .stat-value {
        font-size: 24px;
        font-weight: 600;
        color: var(--text-color-title);
        margin-bottom: var(--spacing-xs);
      }

      .stat-trend {
        font-size: var(--font-size-sm);

        &.up {
          color: var(--success-color);
        }

        &.down {
          color: var(--error-color);
        }
      }

      .stat-sub {
        font-size: var(--font-size-xs);
        color: var(--text-color-secondary);
      }
    }
  }

  .table-container {
    background-color: var(--bg-color);
    border-radius: var(--border-radius-base);
    padding: var(--spacing-xl);
    box-shadow: var(--shadow-base);

    .text-danger {
      color: var(--error-color);
    }

    .pagination-wrapper {
      display: flex;
      justify-content: flex-end;
      margin-top: var(--spacing-xl);
    }
  }
}
</style>