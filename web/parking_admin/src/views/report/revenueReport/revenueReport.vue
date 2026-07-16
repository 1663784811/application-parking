<template>
  <div class="report-page">
    <div class="report-toolbar">
      <DatePicker v-model="state.dateRange" type="daterange" format="yyyy-MM-dd" @on-change="handleDateChange" />
      <Select v-model="state.parkingId" placeholder="选择车场" class="filter-select" clearable>
        <Option value="1">全部车场</Option><Option value="2">城西停车场</Option><Option value="3">城东停车场</Option>
      </Select>
      <Button type="primary" @click="handleSearch">查询</Button>
      <Button @click="handleExport">导出Excel</Button><Button @click="handleExportPdf">导出PDF</Button>
    </div>
    <div class="summary-cards">
      <div class="summary-card"><div class="sc-label">总营收</div><div class="sc-value">¥{{ state.stats.totalRevenue }}</div><div class="sc-trend up">↑ {{ state.stats.revenueTrend }}%</div></div>
      <div class="summary-card"><div class="sc-label">线上收入</div><div class="sc-value">{{ state.stats.onlinePercent }}%</div><div class="sc-detail">¥{{ state.stats.onlineRevenue }}</div></div>
      <div class="summary-card"><div class="sc-label">线下收入</div><div class="sc-value">{{ state.stats.offlinePercent }}%</div><div class="sc-detail">¥{{ state.stats.offlineRevenue }}</div></div>
      <div class="summary-card"><div class="sc-label">优惠抵扣</div><div class="sc-value">¥{{ state.stats.discount }}</div><div class="sc-detail">{{ state.stats.orderCount }} 笔</div></div>
    </div>
    <div class="chart-section">
      <div class="chart-card"><div class="chart-header"><h3>日营收趋势</h3></div><div ref="revenueChartRef" class="chart-body"></div></div>
    </div>
    <div class="table-section">
      <h3 class="section-title">营收明细</h3>
      <Table :columns="columns" :data="state.tableData" :loading="state.loading" show-summary>
        <template #action="{ row }"><Button type="text" size="small" @click="handleViewDetail(row)">查看详情</Button></template>
      </Table>
      <div class="pagination-wrapper"><Page :total="state.pagination.total" :current="state.pagination.current" :page-size="state.pagination.pageSize" show-total show-elevator @on-change="handlePageChange" /></div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import * as echarts from 'echarts'
import { DatePicker, Select, Option, Button, Table, Page, Message } from 'view-ui-plus'

const revenueChartRef = ref(null)

const state = reactive({
  dateRange: [],
  parkingId: null,
  stats: { totalRevenue: '88,560', revenueTrend: 15.6, onlinePercent: 85, onlineRevenue: '75,276', offlinePercent: 15, offlineRevenue: '13,284', discount: '2,850', orderCount: 1245 },
  tableData: [],
  loading: false,
  pagination: { total: 0, current: 1, pageSize: 10 }
})

const columns = [
  { title: '日期', key: 'date', width: 120 },
  { title: '车场', key: 'parkingName', width: 150 },
  { title: '线上金额', key: 'onlineAmount', width: 120, align: 'right' },
  { title: '线下金额', key: 'offlineAmount', width: 120, align: 'right' },
  { title: '优惠抵扣', key: 'discountAmount', width: 120, align: 'right' },
  { title: '合计', key: 'totalAmount', width: 120, align: 'right' },
  { title: '订单数', key: 'orderCount', width: 80, align: 'center' },
  { title: '操作', slot: 'action', width: 100 }
]

const initChart = () => {
  if (!revenueChartRef.value) return
  const chart = echarts.init(revenueChartRef.value)
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 50, right: 20, top: 20, bottom: 40 },
    xAxis: { type: 'category', data: ['1月9日', '1月10日', '1月11日', '1月12日', '1月13日', '1月14日', '1月15日'], axisLine: { lineStyle: { color: '#E5E6EB' } }, axisLabel: { color: '#86909C' } },
    yAxis: { type: 'value', axisLine: { show: false }, axisLabel: { color: '#86909C', formatter: v => `¥${v/1000}k` }, splitLine: { lineStyle: { color: '#F2F3F5' } } },
    series: [{ type: 'bar', data: [8200, 9320, 9010, 12340, 12900, 13300, 12580], barWidth: 24, itemStyle: { color: '#165DFF', borderRadius: [4, 4, 0, 0] } }]
  })
}

const initData = () => {
  state.loading = true
  setTimeout(() => {
    state.tableData = [
      { id: 1, date: '1月9日', parkingName: '城西停车场', onlineAmount: '6,980', offlineAmount: '1,220', discountAmount: '420', totalAmount: '7,780', orderCount: 168 },
      { id: 2, date: '1月10日', parkingName: '城西停车场', onlineAmount: '7,922', offlineAmount: '1,398', discountAmount: '580', totalAmount: '8,740', orderCount: 189 },
      { id: 3, date: '1月11日', parkingName: '城东停车场', onlineAmount: '7,659', offlineAmount: '1,351', discountAmount: '350', totalAmount: '8,660', orderCount: 172 },
      { id: 4, date: '1月12日', parkingName: '全部', onlineAmount: '10,489', offlineAmount: '1,851', discountAmount: '620', totalAmount: '11,720', orderCount: 245 }
    ]
    state.pagination.total = 4
    state.loading = false
  }, 500)
}

const handleDateChange = () => initData()
const handleSearch = () => initData()
const handleExport = () => Message.info('导出Excel')
const handleExportPdf = () => Message.info('导出PDF')
const handleViewDetail = r => console.log('详情', r)
const handlePageChange = p => { state.pagination.current = p; initData() }

onMounted(() => { initData(); initChart() })
</script>

<style lang="less" scoped>
.report-page { .report-toolbar { display: flex; gap: var(--spacing-md); margin-bottom: var(--spacing-lg); padding: var(--spacing-xl); background: var(--bg-color); border-radius: var(--border-radius-base); box-shadow: var(--shadow-base); .filter-select { width: 180px; } } .summary-cards { display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--spacing-lg); margin-bottom: var(--spacing-lg); .summary-card { padding: var(--spacing-xl); background: var(--bg-color); border-radius: var(--border-radius-base); box-shadow: var(--shadow-base); .sc-label { font-size: var(--font-size-sm); color: var(--text-color-secondary); } .sc-value { font-size: 28px; font-weight: 600; color: var(--text-color-title); margin: var(--spacing-sm) 0; } .sc-trend { font-size: var(--font-size-sm); &.up { color: var(--success-color); } } .sc-detail { font-size: var(--font-size-xs); color: var(--text-color-secondary); } } } .chart-section { margin-bottom: var(--spacing-lg); .chart-card { background: var(--bg-color); border-radius: var(--border-radius-base); box-shadow: var(--shadow-base); padding: var(--spacing-xl); .chart-header { margin-bottom: var(--spacing-lg); h3 { font-size: var(--font-size-md); font-weight: 600; } } .chart-body { height: 300px; } } } .table-section { background: var(--bg-color); border-radius: var(--border-radius-base); box-shadow: var(--shadow-base); padding: var(--spacing-xl); .section-title { font-size: var(--font-size-md); font-weight: 600; margin-bottom: var(--spacing-lg); } .pagination-wrapper { display: flex; justify-content: flex-end; margin-top: var(--spacing-xl); } } }
</style>