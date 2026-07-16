<template>
  <div class="report-page">
    <div class="report-toolbar">
      <Select v-model="state.timeType" class="filter-select-sm" @on-change="handleSearch">
        <Option value="day">按日</Option><Option value="week">按周</Option><Option value="month">按月</Option>
      </Select>
      <Select v-model="state.parkingId" placeholder="选择车场" class="filter-select" clearable><Option value="1">全部车场</Option><Option value="2">城西停车场</Option></Select>
      <Button type="primary" @click="handleSearch">查询</Button><Button @click="handleExport">导出</Button>
    </div>
    <div class="stats-row">
      <div class="stat-card"><div class="stat-label">今日入场</div><div class="stat-value">{{ state.stats.todayIn }}</div></div>
      <div class="stat-card"><div class="stat-label">今日出场</div><div class="stat-value">{{ state.stats.todayOut }}</div></div>
      <div class="stat-card"><div class="stat-label">在场车辆</div><div class="stat-value">{{ state.stats.currentIn }}</div></div>
      <div class="stat-card"><div class="stat-label">高峰期</div><div class="stat-value">{{ state.stats.peakHour }}</div></div>
    </div>
    <div class="charts-row">
      <div class="chart-card"><div class="chart-title">分时段车流量</div><div ref="hourChartRef" class="chart-body"></div></div>
      <div class="chart-card"><div class="chart-title">七日对比</div><div ref="compareChartRef" class="chart-body"></div></div>
    </div>
    <div class="table-section"><Table :columns="columns" :data="state.tableData" :loading="state.loading" /></div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { Select, Option, Button, Table, Message } from 'view-ui-plus'

const hourChartRef = ref(null)
const compareChartRef = ref(null)

const state = reactive({
  timeType: 'day',
  parkingId: null,
  stats: { todayIn: 165, todayOut: 142, currentIn: 156, peakHour: '9:00-11:00' },
  tableData: [],
  loading: false
})

const columns = [
  { title: '时段', key: 'period', width: 150 },
  { title: '入园', key: 'inCount', width: 100, align: 'center' },
  { title: '出园', key: 'outCount', width: 100, align: 'center' },
  { title: '在场', key: 'inPark', width: 100, align: 'center' },
  { title: '入场峰值', key: 'inPeak', width: 120, align: 'center' },
  { title: '出场峰值', key: 'outPeak', width: 120, align: 'center' }
]

const initCharts = () => {
  nextTick(() => {
    if (hourChartRef.value) echarts.init(hourChartRef.value).setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: 40, right: 20, top: 20, bottom: 40 },
      xAxis: { type: 'category', data: ['0', '2', '4', '6', '8', '10', '12', '14', '16', '18', '20', '22'], axisLine: { lineStyle: { color: '#E5E6EB' } }, axisLabel: { color: '#86909C' } },
      yAxis: { type: 'value', axisLine: { show: false }, splitLine: { lineStyle: { color: '#F2F3F5' } } },
      series: [{ name: '入园', type: 'line', data: [5, 3, 2, 8, 35, 45, 28, 32, 38, 25, 15, 8], smooth: true, areaStyle: { color: 'rgba(22,93,255,0.1)' }, itemStyle: { color: '#165DFF' }, lineStyle: { color: '#165DFF' } }, { name: '出园', type: 'line', data: [3, 2, 1, 5, 20, 30, 35, 28, 22, 30, 18, 10], smooth: true, areaStyle: { color: 'rgba(0,180,42,0.1)' }, itemStyle: { color: '#00B42A' }, lineStyle: { color: '#00B42A' } }]
    })
    if (compareChartRef.value) echarts.init(compareChartRef.value).setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: 40, right: 20, top: 20, bottom: 40 },
      xAxis: { type: 'category', data: ['周一', '周二', '周三', '周四', '周五', '周六', '周日'], axisLine: { lineStyle: { color: '#E5E6EB' } }, axisLabel: { color: '#86909C' } },
      yAxis: { type: 'value', axisLine: { show: false }, splitLine: { lineStyle: { color: '#F2F3F5' } } },
      series: [{ type: 'bar', data: [280, 265, 290, 310, 340, 420, 385], barWidth: 24, itemStyle: { color: '#165DFF', borderRadius: [4, 4, 0, 0] } }]
    })
  })
}

const initData = () => {
  state.loading = true
  setTimeout(() => {
    state.tableData = [
      { period: '08:00-10:00', inCount: 85, outCount: 45, inPark: 240, inPeak: 62, outPeak: 28 },
      { period: '10:00-12:00', inCount: 65, outCount: 58, inPark: 247, inPeak: 38, outPeak: 35 },
      { period: '12:00-14:00', inCount: 45, outCount: 72, inPark: 220, inPeak: 28, outPeak: 42 },
      { period: '14:00-16:00', inCount: 58, outCount: 52, inPark: 226, inPeak: 35, outPeak: 32 }
    ]
    state.loading = false
  }, 500)
}

const handleSearch = () => initData()
const handleExport = () => Message.info('导出中...')

onMounted(() => { initData(); initCharts() })
</script>

<style lang="less" scoped>
.report-page { .report-toolbar { display: flex; gap: var(--spacing-md); margin-bottom: var(--spacing-lg); padding: var(--spacing-xl); background: var(--bg-color); border-radius: var(--border-radius-base); box-shadow: var(--shadow-base); .filter-select { width: 180px; } .filter-select-sm { width: 120px; } } .stats-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--spacing-lg); margin-bottom: var(--spacing-lg); .stat-card { padding: var(--spacing-xl); background: var(--bg-color); border-radius: var(--border-radius-base); box-shadow: var(--shadow-base); text-align: center; .stat-label { font-size: var(--font-size-sm); color: var(--text-color-secondary); } .stat-value { font-size: 28px; font-weight: 600; color: var(--primary-color); margin-top: var(--spacing-sm); } } } .charts-row { display: grid; grid-template-columns: 1fr 1fr; gap: var(--spacing-lg); margin-bottom: var(--spacing-lg); .chart-card { background: var(--bg-color); border-radius: var(--border-radius-base); box-shadow: var(--shadow-base); padding: var(--spacing-xl); .chart-title { font-size: var(--font-size-md); font-weight: 600; margin-bottom: var(--spacing-lg); } .chart-body { height: 250px; } } } .table-section { background: var(--bg-color); border-radius: var(--border-radius-base); box-shadow: var(--shadow-base); padding: var(--spacing-xl); } }
</style>