<template>
  <div class="report-page">
    <div class="report-toolbar">
      <DatePicker v-model="state.dateRange" type="daterange" format="yyyy-MM-dd" @on-change="handleDateChange" />
      <Select v-model="state.parkingId" placeholder="选择停车场" class="filter-select" clearable>
        <Option value="" :key="'all'">全部停车场</Option>
        <Option v-for="item in state.parkingList" :key="item.id" :value="item.id">{{ item.name }}</Option>
      </Select>
      <Button type="primary" @click="handleSearch">查询</Button>
      <Button @click="handleExport">导出Excel</Button><Button @click="handleExportPdf">导出PDF</Button>
    </div>
    <div class="summary-cards">
      <div class="summary-card">
        <div class="sc-label">总营收</div>
        <div class="sc-value">¥{{ formatRevenue(state.stats.totalRevenue) }}</div>
        <div class="sc-trend" :class="state.stats.revenueTrend >= 0 ? 'up' : 'down'">
          {{ state.stats.revenueTrend >= 0 ? '↑' : '↓' }} {{ Math.abs(state.stats.revenueTrend) }}%
        </div>
      </div>
      <div class="summary-card">
        <div class="sc-label">线上收入</div>
        <div class="sc-value">{{ state.stats.onlinePercent }}%</div>
        <div class="sc-detail">¥{{ formatRevenue(state.stats.onlineRevenue) }}</div>
      </div>
      <div class="summary-card">
        <div class="sc-label">线下收入</div>
        <div class="sc-value">{{ state.stats.offlinePercent }}%</div>
        <div class="sc-detail">¥{{ formatRevenue(state.stats.offlineRevenue) }}</div>
      </div>
      <div class="summary-card">
        <div class="sc-label">优惠抵扣</div>
        <div class="sc-value">¥{{ formatRevenue(state.stats.discount) }}</div>
        <div class="sc-detail">{{ state.stats.orderCount }} 笔</div>
      </div>
    </div>
    <div class="chart-section">
      <div class="chart-card"><div class="chart-header"><h3>日营收趋势</h3></div><div ref="revenueChartRef" class="chart-body"></div></div>
    </div>
    <div class="table-section">
      <h3 class="section-title">营收明细</h3>
      <TableColumnSetting :columns="columns" v-model:visible="visibleFields" v-model:open="colSettingVisible" @reset="resetColumns" />
      <Table :columns="displayColumns" :data="state.tableData" :loading="state.loading" show-summary>
        <template #action="{ row }"><Button type="text" size="small" @click="handleViewDetail(row)">查看详情</Button></template>
      </Table>
      <div class="pagination-wrapper"><Page :total="state.pagination.total" :current="state.pagination.current" :page-size="state.pagination.pageSize" show-total show-elevator @on-change="handlePageChange" /></div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import * as echarts from 'echarts'
import { DatePicker, Select, Option, Button, Table, Page, Message } from 'view-ui-plus'
import { reportApi, parkingApi } from '@/api'
import TableColumnSetting from '@/components/TableColumnSetting.vue'
import { useTableColumns } from '@/composables/useTableColumns'

const router = useRouter()
const revenueChartRef = ref(null)

// 默认近 7 天
const _today = new Date()
const _ago = new Date()
_ago.setDate(_ago.getDate() - 6)

const state = reactive({
  dateRange: [_ago, _today],
  parkingId: '',
  parkingList: [],
  stats: {
    totalRevenue: 0,
    revenueTrend: 0,
    onlinePercent: 0,
    onlineRevenue: 0,
    offlinePercent: 0,
    offlineRevenue: 0,
    discount: 0,
    orderCount: 0
  },
  dailySeries: [],
  tableData: [],
  loading: false,
  pagination: { total: 0, current: 1, pageSize: 10 }
})

// 金额格式化（千分位 + 两位小数）
const formatRevenue = (n) => Number(n || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })

// 日期标签：2024-01-09 → 1月9日
const formatDateLabel = (d) => {
  if (!d) return ''
  const parts = String(d).split('-')
  if (parts.length !== 3) return d
  return `${parseInt(parts[1], 10)}月${parseInt(parts[2], 10)}日`
}

const pad = (n) => String(n).padStart(2, '0')
// Date 对象 / 字符串 → 'yyyy-MM-dd'
const toDateStr = (d) => {
  if (!d) return ''
  if (typeof d === 'string') return d.slice(0, 10)
  const dt = d instanceof Date ? d : new Date(d)
  return `${dt.getFullYear()}-${pad(dt.getMonth() + 1)}-${pad(dt.getDate())}`
}
// 当前选择的日期范围 → { start, end }
const rangeToStrings = () => {
  const [s, e] = state.dateRange || []
  return { start: toDateStr(s), end: toDateStr(e) }
}

// 当前停车场名称（用于明细行的"停车场"列）
const currentParkingName = () => {
  if (!state.parkingId) return '全部停车场'
  const p = state.parkingList.find((i) => String(i.id) === String(state.parkingId))
  return p ? p.name : '全部停车场'
}

const columns = [
  { field: 'date', title: '日期', key: 'date', minWidth: 120 },
  { field: 'parkingName', title: '停车场', key: 'parkingName', minWidth: 150 },
  { field: 'onlineAmount', title: '线上金额', key: 'onlineAmount', minWidth: 120, align: 'right', render: (h, params) => h('span', formatRevenue(params.row.onlineAmount)) },
  { field: 'offlineAmount', title: '线下金额', key: 'offlineAmount', minWidth: 120, align: 'right', render: (h, params) => h('span', formatRevenue(params.row.offlineAmount)) },
  { field: 'discountAmount', title: '优惠抵扣', key: 'discountAmount', minWidth: 120, align: 'right', render: (h, params) => h('span', formatRevenue(params.row.discountAmount)) },
  { field: 'totalAmount', title: '合计', key: 'totalAmount', minWidth: 120, align: 'right', render: (h, params) => h('span', formatRevenue(params.row.totalAmount)) },
  { field: 'orderCount', title: '订单数', key: 'orderCount', minWidth: 80, align: 'center' },
  { title: '操作', slot: 'action', minWidth: 100 }
]

const { visibleFields, colSettingVisible, displayColumns, resetColumns } = useTableColumns(columns, 'revenueReport:columnVisible')

// 写入汇总
const applyStats = (s) => {
  if (!s) return
  state.stats.totalRevenue = Number(s.totalRevenue ?? 0)
  state.stats.revenueTrend = Number(s.revenueTrend ?? 0)
  state.stats.onlinePercent = Number(s.onlinePercent ?? 0)
  state.stats.onlineRevenue = Number(s.onlineRevenue ?? 0)
  state.stats.offlinePercent = Number(s.offlinePercent ?? 0)
  state.stats.offlineRevenue = Number(s.offlineRevenue ?? 0)
  state.stats.discount = Number(s.discount ?? 0)
  state.stats.orderCount = Number(s.orderCount ?? 0)
}

// 日营收趋势：补齐区间内缺失日期（金额 0）
const buildDailySeries = (rows, start, end) => {
  const map = new Map()
  rows.forEach((r) => map.set(String(r.date), Number(r.amount ?? 0)))
  const out = []
  const s = new Date(start)
  const e = new Date(end)
  for (let d = new Date(s); d <= e; d.setDate(d.getDate() + 1)) {
    const key = `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
    out.push({ date: key, amount: map.get(key) || 0 })
  }
  return out
}

// 后端行 → 表格行（金额保持数值，由 render 格式化，使 show-summary 可合计）
const mapDetailRow = (r) => ({
  date: r.date,
  parkingName: currentParkingName(),
  onlineAmount: Number(r.onlineAmount ?? 0),
  offlineAmount: Number(r.offlineAmount ?? 0),
  discountAmount: Number(r.discountAmount ?? 0),
  totalAmount: Number(r.totalAmount ?? 0),
  orderCount: Number(r.orderCount ?? 0)
})

// 加载停车场下拉
const loadParkingList = async () => {
  try {
    const res = await parkingApi.getParkingList({ size: 1000 })
    state.parkingList = res.data || []
  } catch (e) {
    // 忽略：下拉为空不影响主流程
  }
}

// 全量加载（汇总 + 趋势 + 明细）
const loadData = async () => {
  const { start, end } = rangeToStrings()
  if (!start || !end) {
    Message.info('请选择日期范围')
    return
  }
  state.loading = true
  const parkingId = state.parkingId || null
  try {
    const [stats, daily, detail] = await Promise.all([
      reportApi.getRevenueStats({ start, end, parkingId }),
      reportApi.getRevenueDaily({ start, end, parkingId }),
      reportApi.getRevenueDetail({ start, end, parkingId, page: state.pagination.current, size: state.pagination.pageSize })
    ])
    applyStats(stats.data)
    state.dailySeries = buildDailySeries(daily.data || [], start, end)
    state.tableData = (detail.data || []).map(mapDetailRow)
    state.pagination.total = Number(detail.result?.total ?? 0)
  } catch (e) {
    // authRequest 已统一提示
  } finally {
    state.loading = false
  }
  await nextTick()
  initChart()
}

// 日营收图表
const initChart = () => {
  if (!revenueChartRef.value) return
  const chart = echarts.getInstanceByDom(revenueChartRef.value) || echarts.init(revenueChartRef.value)
  const series = state.dailySeries || []
  chart.setOption({
    tooltip: { trigger: 'axis', formatter: (params) => `${params[0].axisValue}<br/>营收: ¥${formatRevenue(params[0].value)}` },
    grid: { left: 50, right: 20, top: 20, bottom: 40 },
    xAxis: { type: 'category', data: series.map((i) => formatDateLabel(i.date)), axisLine: { lineStyle: { color: '#E5E6EB' } }, axisLabel: { color: '#86909C' } },
    yAxis: { type: 'value', axisLine: { show: false }, axisLabel: { color: '#86909C', formatter: (v) => `¥${v / 1000}k` }, splitLine: { lineStyle: { color: '#F2F3F5' } } },
    series: [{ type: 'bar', data: series.map((i) => Number(i.amount ?? 0)), barWidth: 24, itemStyle: { color: '#165DFF', borderRadius: [4, 4, 0, 0] } }]
  }, true)
}

const handleSearch = () => { state.pagination.current = 1; loadData() }
const handleDateChange = () => { state.pagination.current = 1; loadData() }
const handlePageChange = (p) => { state.pagination.current = p; loadData() }
const handleExport = () => Message.info('导出Excel功能待对接')
const handleExportPdf = () => Message.info('导出PDF功能待对接')
const handleViewDetail = () => Message.info('按日订单下钻待对接')

const handleResize = () => {
  if (revenueChartRef.value) echarts.getInstanceByDom(revenueChartRef.value)?.resize()
}

onMounted(async () => {
  await loadParkingList()
  await loadData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (revenueChartRef.value) echarts.getInstanceByDom(revenueChartRef.value)?.dispose()
})
</script>

<style lang="less" scoped>
.report-page { .report-toolbar { display: flex; gap: var(--spacing-md); margin-bottom: var(--spacing-lg); padding: var(--spacing-xl); background: var(--bg-color); border-radius: var(--border-radius-base); box-shadow: var(--shadow-base); .filter-select { minWidth: 180px; } } .summary-cards { display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--spacing-lg); margin-bottom: var(--spacing-lg); .summary-card { padding: var(--spacing-xl); background: var(--bg-color); border-radius: var(--border-radius-base); box-shadow: var(--shadow-base); .sc-label { font-size: var(--font-size-sm); color: var(--text-color-secondary); } .sc-value { font-size: 28px; font-weight: 600; color: var(--text-color-title); margin: var(--spacing-sm) 0; } .sc-trend { font-size: var(--font-size-sm); &.up { color: var(--success-color); } &.down { color: var(--error-color); } } .sc-detail { font-size: var(--font-size-xs); color: var(--text-color-secondary); } } } .chart-section { margin-bottom: var(--spacing-lg); .chart-card { background: var(--bg-color); border-radius: var(--border-radius-base); box-shadow: var(--shadow-base); padding: var(--spacing-xl); .chart-header { margin-bottom: var(--spacing-lg); h3 { font-size: var(--font-size-md); font-weight: 600; } } .chart-body { height: 300px; } } } .table-section { background: var(--bg-color); border-radius: var(--border-radius-base); box-shadow: var(--shadow-base); padding: var(--spacing-xl); .section-title { font-size: var(--font-size-md); font-weight: 600; margin-bottom: var(--spacing-lg); } .pagination-wrapper { display: flex; justify-content: flex-end; margin-top: var(--spacing-xl); } } }
</style>
