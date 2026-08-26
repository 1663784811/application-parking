<template>
  <div class="report-page">
    <div class="report-toolbar">
      <DatePicker v-model="state.date" type="date" placeholder="选择日期" @on-change="handleSearch" />
      <Select v-model="state.parkingId" placeholder="选择停车场" class="filter-select" clearable>
        <Option value="">全部停车场</Option>
        <Option v-for="item in state.parkingList" :key="item.id" :value="item.id">{{ item.name }}</Option>
      </Select>
      <Button type="primary" @click="handleSearch">查询</Button><Button @click="handleExport">导出</Button>
    </div>
    <div class="summary-row">
      <div class="summary-card"><Icon type="ios-square" class="summary-icon primary" /><div class="summary-info"><div class="summary-label">总车位数</div><div class="summary-value">{{ state.stats.totalSpaces }}</div></div></div>
      <div class="summary-card"><Icon type="ios-checkmark-circle" class="summary-icon success" /><div class="summary-info"><div class="summary-label">当前占用</div><div class="summary-value">{{ state.stats.occupiedSpaces }}</div></div></div>
      <div class="summary-card"><Icon type="ios-alert" class="summary-icon warning" /><div class="summary-info"><div class="summary-label">空置率</div><div class="summary-value">{{ state.stats.vacancyRate }}%</div></div></div>
      <div class="summary-card"><Icon type="ios-time" class="summary-icon danger" /><div class="summary-info"><div class="summary-label">高峰占用率</div><div class="summary-value">{{ state.stats.peakRate }}%</div><div style="font-size:12px;color:var(--text-color-secondary)">高峰时段 {{ state.stats.peakHour }}</div></div></div>
    </div>
    <div class="charts-row">
      <div class="chart-card"><div class="chart-title">时段占用率</div><div ref="utilChartRef" class="chart-body"></div></div>
      <div class="chart-card"><div class="chart-title">区域对比</div><div ref="areaChartRef" class="chart-body"></div></div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { DatePicker, Select, Option, Button, Icon, Message } from 'view-ui-plus'
import { useCommonStore } from '@/stores/common.js'
import { parkingApi, reportApi } from '@/api'

const commonStore = useCommonStore()

const utilChartRef = ref(null)
const areaChartRef = ref(null)

const pad = (n) => String(n).padStart(2, '0')
const todayStr = () => {
  const d = new Date()
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

const state = reactive({
  date: '',
  parkingId: '',
  parkingList: commonStore.state.parkingList.length > 0 ? commonStore.state.parkingList : [],
  stats: { totalSpaces: 0, occupiedSpaces: 0, vacancyRate: 0, peakRate: 0, peakHour: '-' },
  hourlyList: [],
  areaList: []
})

const loadParkingList = async () => {
  if (commonStore.state.parkingList.length > 0) {
    state.parkingList = commonStore.state.parkingList
  } else {
    try {
      const res = await parkingApi.getParkingList({ size: 1000 })
      state.parkingList = res.data || []
    } catch (e) { /* ignore */ }
  }
  if (!state.parkingId && state.parkingList.length > 0) {
    const ids = commonStore.state.selectedParkingIds
    state.parkingId = ids.length > 0 ? ids[0] : (commonStore.state.currentParking?.id || null)
  }
}

const loadData = async () => {
  const params = { parkingId: state.parkingId || undefined }
  try {
    const [statsRes, hourlyRes, areaRes] = await Promise.all([
      reportApi.getSpaceUsageStats(params),
      reportApi.getSpaceUsageHourly(params),
      reportApi.getSpaceArea(params)
    ])
    const s = statsRes?.data || {}
    state.stats = {
      totalSpaces: s.totalSpaces || 0,
      occupiedSpaces: s.currentIn || 0,
      vacancyRate: s.vacancyRate || 0,
      peakRate: s.peakRate || 0,
      peakHour: s.peakHour || '-'
    }
    state.hourlyList = hourlyRes?.data || []
    state.areaList = areaRes?.data || []
    renderCharts()
  } catch (e) { /* authRequest 已提示 */ }
}

const renderCharts = () => {
  nextTick(() => {
    renderUtilChart()
    renderAreaChart()
  })
}

const renderUtilChart = () => {
  if (!utilChartRef.value) return
  const chart = echarts.getInstanceByDom(utilChartRef.value) || echarts.init(utilChartRef.value)
  const total = state.stats.totalSpaces || 0
  const hours = Array.from({ length: 24 }, (_, h) => `${pad(h)}:00`)
  const rates = Array.from({ length: 24 }, (__, h) => {
    const row = state.hourlyList.find(r => r.hour === h)
    const inLot = row ? row.inLot : 0
    const rate = total > 0 ? Math.round((inLot * 100) / total) : 0
    return Math.min(100, rate)
  })
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 50, right: 20, top: 20, bottom: 40 },
    xAxis: { type: 'category', data: hours, axisLine: { lineStyle: { color: '#E5E6EB' } }, axisLabel: { color: '#86909C' } },
    yAxis: { type: 'value', max: 100, axisLabel: { formatter: '{value}%' }, axisLine: { show: false }, splitLine: { lineStyle: { color: '#F2F3F5' } } },
    series: [{ type: 'line', data: rates, smooth: true, areaStyle: { color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [{ offset: 0, color: 'rgba(22,93,255,0.3)' }, { offset: 1, color: 'rgba(22,93,255,0)' }]) }, itemStyle: { color: '#165DFF' }, lineStyle: { color: '#165DFF' } }]
  }, true)
}

const renderAreaChart = () => {
  if (!areaChartRef.value) return
  const chart = echarts.getInstanceByDom(areaChartRef.value) || echarts.init(areaChartRef.value)
  const totalOccupied = state.areaList.reduce((s, a) => s + (a.occupied || 0), 0)
  const useTotal = totalOccupied === 0
  const data = state.areaList.map(a => ({ value: useTotal ? a.total : a.occupied, name: a.area || '未知' }))
  chart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [{ type: 'pie', radius: ['40%', '70%'], data: data, itemStyle: { borderRadius: 6, borderColor: '#fff', borderWidth: 2 }, label: { show: false } }]
  }, true)
}

const handleSearch = () => {
  const d = state.date
  if (d) {
    const ds = d instanceof Date ? `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}` : d
    if (ds !== todayStr()) {
      Message.info('历史日期查询待对接')
      return
    }
  }
  loadData()
}

const handleExport = () => Message.info('导出功能待对接')

const handleResize = () => {
  if (utilChartRef.value) (echarts.getInstanceByDom(utilChartRef.value) || echarts.init(utilChartRef.value)).resize()
  if (areaChartRef.value) (echarts.getInstanceByDom(areaChartRef.value) || echarts.init(areaChartRef.value)).resize()
}

onMounted(() => {
  loadParkingList().then(() => loadData())
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (utilChartRef.value) echarts.getInstanceByDom(utilChartRef.value)?.dispose()
  if (areaChartRef.value) echarts.getInstanceByDom(areaChartRef.value)?.dispose()
})
</script>

<style lang="less" scoped>
.report-page { .report-toolbar { display: flex; gap: var(--spacing-md); margin-bottom: var(--spacing-lg); padding: var(--spacing-xl); background: var(--bg-color); border-radius: var(--border-radius-base); box-shadow: var(--shadow-base); .filter-select { minWidth: 180px; } } .summary-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--spacing-lg); margin-bottom: var(--spacing-lg); .summary-card { display: flex; align-items: center; padding: var(--spacing-xl); background: var(--bg-color); border-radius: var(--border-radius-base); box-shadow: var(--shadow-base); .summary-icon { font-size: 36px; margin-right: var(--spacing-lg); &.primary { color: #165DFF; } &.success { color: #00B42A; } &.warning { color: #FF7D00; } &.danger { color: #F53F3F; } } .summary-info { .summary-label { font-size: var(--font-size-sm); color: var(--text-color-secondary); } .summary-value { font-size: 24px; font-weight: 600; color: var(--text-color-title); } } } } .charts-row { display: grid; grid-template-columns: 1fr 1fr; gap: var(--spacing-lg); .chart-card { background: var(--bg-color); border-radius: var(--border-radius-base); box-shadow: var(--shadow-base); padding: var(--spacing-xl); .chart-title { font-size: var(--font-size-md); font-weight: 600; margin-bottom: var(--spacing-lg); } .chart-body { height: 280px; } } } }
</style>
