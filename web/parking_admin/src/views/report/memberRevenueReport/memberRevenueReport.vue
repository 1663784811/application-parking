<template>
  <div class="report-page">
    <div class="report-toolbar">
      <DatePicker v-model="state.dateRange" type="daterange" format="yyyy-MM-dd" @on-change="handleDateChange" />
      <Select v-model="state.cardType" placeholder="卡类型" class="filter-select" clearable>
        <Option value="" :key="'all'">全部卡类型</Option>
        <Option v-for="opt in cardTypeOptions" :key="opt.value" :value="opt.value">{{ opt.label }}</Option>
      </Select>
      <Button type="primary" @click="handleSearch">查询</Button>
      <Button @click="handleExport">导出</Button>
    </div>
    <div class="summary-cards">
      <div class="summary-card">
        <div class="sc-label">续费总金额</div>
        <div class="sc-value">¥{{ formatMoney(state.stats.totalRevenue) }}</div>
        <div class="sc-detail">峰值月份 {{ state.stats.peakMonth }}</div>
      </div>
      <div class="summary-card">
        <div class="sc-label">续费笔数</div>
        <div class="sc-value">{{ state.stats.renewalCount }}</div>
        <div class="sc-detail">笔</div>
      </div>
      <div class="summary-card">
        <div class="sc-label">活跃会员</div>
        <div class="sc-value">{{ state.stats.memberCount }}</div>
        <div class="sc-detail">人</div>
      </div>
      <div class="summary-card">
        <div class="sc-label">人均续费</div>
        <div class="sc-value">¥{{ formatMoney(state.stats.avgRevenue) }}</div>
        <div class="sc-detail">人均金额</div>
      </div>
    </div>
    <div class="chart-section">
      <div class="chart-card"><div class="chart-header"><h3>月度营收趋势</h3></div><div ref="trendChartRef" class="chart-body"></div></div>
    </div>
    <div class="table-section">
      <h3 class="section-title">会员营收明细</h3>
      <TableColumnSetting :columns="columns" v-model:visible="visibleFields" v-model:open="colSettingVisible" @reset="resetColumns" />
      <Table :columns="displayColumns" :data="state.tableData" :loading="state.loading" />
      <div class="pagination-wrapper"><Page :total="state.pagination.total" :current="state.pagination.current" :page-size="state.pagination.pageSize" show-total show-elevator @on-change="handlePageChange" /></div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { DatePicker, Select, Option, Button, Table, Page, Message } from 'view-ui-plus'
import { reportApi } from '@/api'
import TableColumnSetting from '@/components/TableColumnSetting.vue'
import { useTableColumns } from '@/composables/useTableColumns'

const trendChartRef = ref(null)

// 卡类型 {1:月卡,2:季卡,3:年卡}
const cardTypeOptions = [
  { value: 1, label: '月卡' },
  { value: 2, label: '季卡' },
  { value: 3, label: '年卡' }
]
const cardTypeText = (t) => ({ 1: '月卡', 2: '季卡', 3: '年卡' }[t] || '-')

// 默认本年度（1月1日 → 今天）
const _now = new Date()
const _yearStart = new Date(_now.getFullYear(), 0, 1)

const state = reactive({
  dateRange: [_yearStart, _now],
  cardType: '',
  stats: { totalRevenue: 0, renewalCount: 0, memberCount: 0, avgRevenue: 0, peakMonth: '-' },
  trendSeries: [],
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

// 月度趋势：补齐区间内缺失月份（金额 0）
const buildMonthlySeries = (rows, start, end) => {
  const map = new Map()
  rows.forEach((r) => map.set(String(r.month), Number(r.revenue ?? 0)))
  const out = []
  const s = new Date(start)
  const e = new Date(end)
  let y = s.getFullYear()
  let m = s.getMonth()
  while (y < e.getFullYear() || (y === e.getFullYear() && m <= e.getMonth())) {
    const key = `${y}-${pad(m + 1)}`
    out.push({ month: key, revenue: map.get(key) || 0 })
    m++
    if (m > 11) { m = 0; y++ }
  }
  return out
}

// 后端行 → 表格行
const mapDetailRow = (r) => ({
  memberId: r.memberId,
  name: r.name || '-',
  plate: r.plate || '-',
  cardType: r.cardType,
  cardTypeText: cardTypeText(r.cardType),
  renewalCount: Number(r.renewalCount ?? 0),
  totalAmount: Number(r.totalAmount ?? 0)
})

const columns = [
  { field: 'name', title: '会员姓名', key: 'name', minWidth: 120 },
  { field: 'plate', title: '车牌号', key: 'plate', minWidth: 120 },
  { field: 'cardTypeText', title: '卡类型', key: 'cardTypeText', minWidth: 100, align: 'center' },
  { field: 'renewalCount', title: '续费次数', key: 'renewalCount', minWidth: 100, align: 'center' },
  { field: 'totalAmount', title: '累计金额', key: 'totalAmount', minWidth: 140, align: 'right', render: (h, params) => h('span', formatMoney(params.row.totalAmount)) }
]

const { visibleFields, colSettingVisible, displayColumns, resetColumns } = useTableColumns(columns, 'memberRevenueReport:columnVisible')

const applyStats = (s) => {
  if (!s) return
  state.stats.totalRevenue = Number(s.totalRevenue ?? 0)
  state.stats.renewalCount = Number(s.renewalCount ?? 0)
  state.stats.memberCount = Number(s.memberCount ?? 0)
  state.stats.avgRevenue = Number(s.avgRevenue ?? 0)
  state.stats.peakMonth = s.peakMonth || '-'
}

// 全量加载（汇总 + 趋势 + 明细）
const loadData = async () => {
  const { start, end } = rangeToStrings()
  if (!start || !end) {
    Message.info('请选择日期范围')
    return
  }
  state.loading = true
  const cardType = state.cardType || null
  try {
    const [stats, trend, detail] = await Promise.all([
      reportApi.getMemberRevenueStats({ start, end, cardType }),
      reportApi.getMemberRevenueTrend({ start, end, cardType }),
      reportApi.getMemberRevenueDetail({ start, end, cardType, page: state.pagination.current, size: state.pagination.pageSize })
    ])
    applyStats(stats.data)
    state.trendSeries = buildMonthlySeries(trend.data || [], start, end)
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

// 月度营收趋势折线图
const initChart = () => {
  if (!trendChartRef.value) return
  const chart = echarts.getInstanceByDom(trendChartRef.value) || echarts.init(trendChartRef.value)
  const series = state.trendSeries || []
  chart.setOption({
    tooltip: { trigger: 'axis', formatter: (params) => `${params[0].axisValue}<br/>营收: ¥${formatMoney(params[0].value)}` },
    grid: { left: 50, right: 20, top: 20, bottom: 40 },
    xAxis: { type: 'category', data: series.map((i) => i.month), axisLine: { lineStyle: { color: '#E5E6EB' } }, axisLabel: { color: '#86909C' } },
    yAxis: { type: 'value', axisLine: { show: false }, axisLabel: { color: '#86909C', formatter: (v) => `¥${v / 1000}k` }, splitLine: { lineStyle: { color: '#F2F3F5' } } },
    series: [{
      type: 'line',
      data: series.map((i) => Number(i.revenue ?? 0)),
      smooth: true,
      areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(22,93,255,0.3)' }, { offset: 1, color: 'rgba(22,93,255,0)' }]) },
      itemStyle: { color: '#165DFF' },
      lineStyle: { color: '#165DFF' }
    }]
  }, true)
}

const handleSearch = () => { state.pagination.current = 1; loadData() }
const handleDateChange = () => { state.pagination.current = 1; loadData() }
const handlePageChange = (p) => { state.pagination.current = p; loadData() }
const handleExport = () => Message.info('导出功能待对接')

const handleResize = () => {
  if (trendChartRef.value) (echarts.getInstanceByDom(trendChartRef.value) || echarts.init(trendChartRef.value)).resize()
}

onMounted(() => {
  loadData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (trendChartRef.value) echarts.getInstanceByDom(trendChartRef.value)?.dispose()
})
</script>

<style lang="less" scoped>
.report-page { .report-toolbar { display: flex; gap: var(--spacing-md); margin-bottom: var(--spacing-lg); padding: var(--spacing-xl); background: var(--bg-color); border-radius: var(--border-radius-base); box-shadow: var(--shadow-base); .filter-select { minWidth: 160px; } } .summary-cards { display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--spacing-lg); margin-bottom: var(--spacing-lg); .summary-card { padding: var(--spacing-xl); background: var(--bg-color); border-radius: var(--border-radius-base); box-shadow: var(--shadow-base); .sc-label { font-size: var(--font-size-sm); color: var(--text-color-secondary); } .sc-value { font-size: 28px; font-weight: 600; color: var(--text-color-title); margin: var(--spacing-sm) 0; } .sc-detail { font-size: var(--font-size-xs); color: var(--text-color-secondary); } } } .chart-section { margin-bottom: var(--spacing-lg); .chart-card { background: var(--bg-color); border-radius: var(--border-radius-base); box-shadow: var(--shadow-base); padding: var(--spacing-xl); .chart-header { margin-bottom: var(--spacing-lg); h3 { font-size: var(--font-size-md); font-weight: 600; } } .chart-body { height: 300px; } } } .table-section { background: var(--bg-color); border-radius: var(--border-radius-base); box-shadow: var(--shadow-base); padding: var(--spacing-xl); .section-title { font-size: var(--font-size-md); font-weight: 600; margin-bottom: var(--spacing-lg); } .pagination-wrapper { display: flex; justify-content: flex-end; margin-top: var(--spacing-xl); } } }
</style>
