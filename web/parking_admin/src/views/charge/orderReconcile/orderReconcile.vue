<template>
  <div class="order-reconcile-page">
    <!-- 筛选栏 -->
    <div class="filter-bar">
      <div class="filter-row">
        <div class="filter-item">
          <DatePicker
            v-model="state.dateRange"
            type="daterange"
            format="yyyy-MM-dd"
            placeholder="对账日期范围"
            class="filter-date"
          />
        </div>

        <div class="filter-item">
          <Select v-model="state.parkingId" placeholder="选择停车场" class="filter-select" clearable>
            <Option v-for="item in state.parkingList" :key="item.id" :value="item.id">{{ item.name }}</Option>
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

    <!-- 汇总卡片 -->
    <div class="summary-row">
      <div class="summary-card total">
        <div class="summary-icon">
          <Icon type="logo-yen" />
        </div>
        <div class="summary-info">
          <div class="summary-label">对账总收入</div>
          <div class="summary-value">¥{{ formatMoney(state.summary.totalRevenue) }}</div>
          <div class="summary-count">共 {{ state.summary.totalOrders }} 笔订单</div>
        </div>
      </div>
      <div class="summary-card online">
        <div class="summary-icon">
          <Icon type="logo-chrome" />
        </div>
        <div class="summary-info">
          <div class="summary-label">线上收入</div>
          <div class="summary-value">¥{{ formatMoney(state.summary.onlineRevenue) }}</div>
          <div class="summary-count">{{ state.summary.onlineCount }} 笔</div>
        </div>
      </div>
      <div class="summary-card offline">
        <div class="summary-icon">
          <Icon type="ios-cash" />
        </div>
        <div class="summary-info">
          <div class="summary-label">线下收入</div>
          <div class="summary-value">¥{{ formatMoney(state.summary.offlineRevenue) }}</div>
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
            ¥{{ formatMoney(state.summary.difference) }}
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

      <TableColumnSetting :columns="columns" v-model:visible="visibleFields" v-model:open="colSettingVisible" @reset="resetColumns" />
      <Table
        :columns="displayColumns"
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

      <div class="pagination-wrapper">
        <Page :total="state.pagination.total" :current="state.pagination.current" :page-size="state.pagination.pageSize" show-total show-elevator @on-change="handlePageChange" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import {
  DatePicker,
  Select,
  Option,
  Button,
  Icon,
  Table,
  Badge,
  Page,
  Message
} from 'view-ui-plus'
import { chargeApi, parkingApi } from '@/api'
import TableColumnSetting from '@/components/TableColumnSetting.vue'
import { useTableColumns } from '@/composables/useTableColumns'

// 默认对账日期：今日 → 今日
const _today = new Date()

const state = reactive({
  dateRange: [_today, _today],
  parkingId: null,
  parkingList: [],
  summary: {
    totalRevenue: 0,
    totalOrders: 0,
    onlineRevenue: 0,
    onlineCount: 0,
    offlineRevenue: 0,
    offlineCount: 0,
    difference: 0
  },
  tableData: [],
  loading: false,
  pagination: { total: 0, current: 1, pageSize: 10 }
})

// 金额格式化（千分位 + 两位小数）
const formatMoney = (n) => Number(n || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })

const pad = (n) => String(n).padStart(2, '0')
// Date 对象 / 字符串 → 'yyyy-MM-dd'
const toDateStr = (d) => {
  if (!d) return ''
  if (typeof d === 'string') return d.slice(0, 10)
  const dt = d instanceof Date ? d : new Date(d)
  return `${dt.getFullYear()}-${pad(dt.getMonth() + 1)}-${pad(dt.getDate())}`
}
// 当前日期范围 → { start, end }
const rangeToStrings = () => {
  const [s, e] = state.dateRange || []
  return { start: toDateStr(s), end: toDateStr(e) }
}

const getStatusText = (status) => (status === 'success' ? '已结清' : '有差异')

// 后端行 → 表格行（差异金额 → 对账状态）
const mapDetailRow = (r) => {
  const diff = Number(r.diffAmount ?? 0)
  return {
    parkingId: r.parkingId,
    parkingName: r.parkingName || '-',
    reconcileDate: r.reconcileDate || '-',
    onlineOrders: Number(r.onlineOrders ?? 0),
    onlineAmount: Number(r.onlineAmount ?? 0),
    offlineOrders: Number(r.offlineOrders ?? 0),
    offlineAmount: Number(r.offlineAmount ?? 0),
    diffAmount: diff,
    status: diff !== 0 ? 'error' : 'success'
  }
}

const columns = [
  { field: 'parkingName', title: '停车场名称', key: 'parkingName', minWidth: 180 },
  { field: 'reconcileDate', title: '对账日期', key: 'reconcileDate', minWidth: 120 },
  { field: 'onlineOrders', title: '线上订单数', key: 'onlineOrders', minWidth: 110, align: 'center' },
  { field: 'onlineAmount', title: '线上金额', key: 'onlineAmount', minWidth: 120, align: 'right', render: (h, p) => h('span', formatMoney(p.row.onlineAmount)) },
  { field: 'offlineOrders', title: '线下订单数', key: 'offlineOrders', minWidth: 110, align: 'center' },
  { field: 'offlineAmount', title: '线下金额', key: 'offlineAmount', minWidth: 120, align: 'right', render: (h, p) => h('span', formatMoney(p.row.offlineAmount)) },
  { field: 'diffAmount', title: '差异金额', key: 'diffAmount', minWidth: 110, align: 'right', render: (h, p) => h('span', { style: { color: p.row.diffAmount !== 0 ? 'var(--error-color)' : 'inherit' } }, formatMoney(p.row.diffAmount)) },
  { field: 'status', title: '对账状态', slot: 'status', minWidth: 100, align: 'center' },
  { title: '操作', slot: 'action', minWidth: 150, fixed: 'right' }
]

const { visibleFields, colSettingVisible, displayColumns, resetColumns } = useTableColumns(columns, 'orderReconcile:columnVisible')

const applySummary = (s) => {
  if (!s) return
  state.summary.totalRevenue = Number(s.totalRevenue ?? 0)
  state.summary.totalOrders = Number(s.totalOrders ?? 0)
  state.summary.onlineRevenue = Number(s.onlineRevenue ?? 0)
  state.summary.onlineCount = Number(s.onlineCount ?? 0)
  state.summary.offlineRevenue = Number(s.offlineRevenue ?? 0)
  state.summary.offlineCount = Number(s.offlineCount ?? 0)
  state.summary.difference = Number(s.difference ?? 0)
}

const loadParkingList = async () => {
  try {
    const res = await parkingApi.getParkingList({ size: 1000 })
    state.parkingList = res.data || []
  } catch (e) {
    // authRequest 已统一提示
  }
}

const loadData = async () => {
  const { start, end } = rangeToStrings()
  if (!start || !end) {
    Message.info('请选择对账日期范围')
    return
  }
  state.loading = true
  try {
    const [stats, detail] = await Promise.all([
      chargeApi.getReconcileStats({ start, end, parkingId: state.parkingId || null }),
      chargeApi.getReconcileDetail({ start, end, parkingId: state.parkingId || null, page: state.pagination.current, size: state.pagination.pageSize })
    ])
    applySummary(stats.data)
    state.tableData = (detail.data || []).map(mapDetailRow)
    state.pagination.total = Number(detail.result?.total ?? 0)
  } catch (e) {
    // authRequest 已统一提示
  } finally {
    state.loading = false
  }
}

const handleSearch = () => { state.pagination.current = 1; loadData() }
const handleReset = () => {
  const today = new Date()
  state.dateRange = [today, today]
  state.parkingId = null
  state.pagination.current = 1
  loadData()
}
const handlePageChange = (p) => { state.pagination.current = p; loadData() }

const handleExport = () => Message.info('导出功能待对接')
const handleViewDetail = (row) => Message.info(`"${row.parkingName}"（${row.reconcileDate}）的订单明细待对接`)
const handleMarkException = () => Message.info('标记异常功能待对接')

onMounted(() => {
  loadParkingList()
  loadData()
})
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

      .filter-item {
        flex-shrink: 0;
      }

      .filter-date {
        width: 260px;
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
        min-width: 56px;
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

    .pagination-wrapper {
      display: flex;
      justify-content: flex-end;
      margin-top: var(--spacing-xl);
    }
  }
}
</style>
