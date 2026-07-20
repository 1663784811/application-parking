<template>
  <div class="report-page">
    <div class="report-toolbar">
      <DatePicker v-model="state.date" type="date" @on-change="handleSearch" />
      <Select v-model="state.parkingId" placeholder="选择停车场" class="filter-select" clearable><Option value="1">全部停车场</Option><Option value="2">城西停车场</Option></Select>
      <Button type="primary" @click="handleSearch">查询</Button><Button @click="handleExport">导出</Button>
    </div>
    <div class="summary-row">
      <div class="summary-card"><Icon type="ios-square" class="summary-icon primary" /><div class="summary-info"><div class="summary-label">总车位数</div><div class="summary-value">{{ state.stats.totalSpaces }}</div></div></div>
      <div class="summary-card"><Icon type="ios-checkmark-circle" class="summary-icon success" /><div class="summary-info"><div class="summary-label">当前占用</div><div class="summary-value">{{ state.stats.occupiedSpaces }}</div></div></div>
      <div class="summary-card"><Icon type="ios-alert" class="summary-icon warning" /><div class="summary-info"><div class="summary-label">空置率</div><div class="summary-value">{{ state.stats.vacancyRate }}%</div></div></div>
      <div class="summary-card"><Icon type="ios-time" class="summary-icon danger" /><div class="summary-info"><div class="summary-label">高峰占用率</div><div class="summary-value">{{ state.stats.peakRate }}%</div></div></div>
    </div>
    <div class="charts-row">
      <div class="chart-card"><div class="chart-title">时段占用率</div><div ref="utilChartRef" class="chart-body"></div></div>
      <div class="chart-card"><div class="chart-title">区域对比</div><div ref="areaChartRef" class="chart-body"></div></div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { DatePicker, Select, Option, Button, Icon, Message } from 'view-ui-plus'

const utilChartRef = ref(null)
const areaChartRef = ref(null)

const state = reactive({
  date: '',
  parkingId: null,
  stats: { totalSpaces: 500, occupiedSpaces: 265, vacancyRate: 47, peakRate: 92 }
})

const initCharts = () => {
  nextTick(() => {
    if (utilChartRef.value) echarts.init(utilChartRef.value).setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: 50, right: 20, top: 20, bottom: 40 },
      xAxis: { type: 'category', data: ['08:00', '10:00', '12:00', '14:00', '16:00', '18:00', '20:00'], axisLine: { lineStyle: { color: '#E5E6EB' } }, axisLabel: { color: '#86909C' } },
      yAxis: { type: 'value', max: 100, axisLabel: { formatter: '{value}%' }, axisLine: { show: false }, splitLine: { lineStyle: { color: '#F2F3F5' } } },
      series: [{ type: 'line', data: [45, 62, 78, 85, 92, 88, 72], smooth: true, areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(22,93,255,0.3)' }, { offset: 1, color: 'rgba(22,93,255,0)' }]) }, itemStyle: { color: '#165DFF' }, lineStyle: { color: '#165DFF' } }]
    })
    if (areaChartRef.value) echarts.init(areaChartRef.value).setOption({
      tooltip: { trigger: 'item' },
      legend: { bottom: 0 },
      series: [{ type: 'pie', radius: ['40%', '70%'], data: [{ value: 120, name: 'A区 120' }, { value: 85, name: 'B区 85' }, { value: 60, name: 'C区 60' }], itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 }, label: { show: false } }]
    })
  })
}

const handleSearch = () => initCharts()
const handleExport = () => Message.info('导出中...')
const initChartsRef = initCharts

onMounted(() => { initChartsRef() })
</script>

<style lang="less" scoped>
.report-page { .report-toolbar { display: flex; gap: var(--spacing-md); margin-bottom: var(--spacing-lg); padding: var(--spacing-xl); background: var(--bg-color); border-radius: var(--border-radius-base); box-shadow: var(--shadow-base); .filter-select { minWidth: 180px; } } .summary-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--spacing-lg); margin-bottom: var(--spacing-lg); .summary-card { display: flex; align-items: center; padding: var(--spacing-xl); background: var(--bg-color); border-radius: var(--border-radius-base); box-shadow: var(--shadow-base); .summary-icon { font-size: 36px; margin-right: var(--spacing-lg); &.primary { color: #165DFF; } &.success { color: #00B42A; } &.warning { color: #FF7D00; } &.danger { color: #F53F3F; } } .summary-info { .summary-label { font-size: var(--font-size-sm); color: var(--text-color-secondary); } .summary-value { font-size: 24px; font-weight: 600; color: var(--text-color-title); } } } } .charts-row { display: grid; grid-template-columns: 1fr 1fr; gap: var(--spacing-lg); .chart-card { background: var(--bg-color); border-radius: var(--border-radius-base); box-shadow: var(--shadow-base); padding: var(--spacing-xl); .chart-title { font-size: var(--font-size-md); font-weight: 600; margin-bottom: var(--spacing-lg); } .chart-body { height: 280px; } } } }
</style>