<template>
  <div class="report-page">
    <div class="report-toolbar">
      <Select v-model="state.timeType" class="filter-select-sm" @on-change="handleSearch">
        <Option value="day">按日</Option><Option value="week">按周</Option><Option value="month">按月</Option>
      </Select>
      <Select v-model="state.parkingId" placeholder="选择停车场" class="filter-select" clearable>
        <Option value="" :key="'all'">全部停车场</Option>
        <Option v-for="item in state.parkingList" :key="item.id" :value="item.id">{{ item.name }}</Option>
      </Select>
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
    <div class="table-section"><TableColumnSetting :columns="columns" v-model:visible="visibleFields" v-model:open="colSettingVisible" @reset="resetColumns" /><Table :columns="displayColumns" :data="state.tableData" :loading="state.loading" /></div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { Select, Option, Button, Table, Message } from 'view-ui-plus'
import { reportApi, parkingApi } from '@/api'
import TableColumnSetting from '@/components/TableColumnSetting.vue'
import { useTableColumns } from '@/composables/useTableColumns'

const hourChartRef = ref(null)
const compareChartRef = ref(null)

const state = reactive({
  timeType: 'day',
  parkingId: '',
  parkingList: [],
  stats: { todayIn: 0, todayOut: 0, currentIn: 0, peakHour: '--' },
  hourly: [],
  daily: [],
  tableData: [],
  loading: false
})

const columns = [
  { field: 'period', title: '时段', key: 'period', minWidth: 150 },
  { field: 'inCount', title: '入园', key: 'inCount', minWidth: 100, align: 'center' },
  { field: 'outCount', title: '出园', key: 'outCount', minWidth: 100, align: 'center' },
  { field: 'inPark', title: '在场', key: 'inPark', minWidth: 100, align: 'center' },
  { field: 'inPeak', title: '入场峰值', key: 'inPeak', minWidth: 120, align: 'center' },
  { field: 'outPeak', title: '出场峰值', key: 'outPeak', minWidth: 120, align: 'center' }
]

const { visibleFields, colSettingVisible, displayColumns, resetColumns } = useTableColumns(columns, 'trafficReport:columnVisible')

// 日期标签：2024-01-09 → 1月9日
const formatDateLabel = (d) => {
  if (!d) return ''
  const parts = String(d).split('-')
  if (parts.length !== 3) return d
  return `${parseInt(parts[1], 10)}月${parseInt(parts[2], 10)}日`
}

// 写入汇总
const applyStats = (s) => {
  if (!s) return
  state.stats.todayIn = Number(s.todayIn ?? 0)
  state.stats.todayOut = Number(s.todayOut ?? 0)
  state.stats.currentIn = Number(s.currentIn ?? 0)
  state.stats.peakHour = s.peakHour || '--'
}

// 加载停车场下拉
const loadParkingList = async () => {
  try {
    const res = await parkingApi.getParkingList({ size: 1000 })
    state.parkingList = res.data || []
  } catch (e) {
    // 忽略：下拉为空不影响主流程
  }
}

// 全量加载（汇总 + 分小时 + 近7日 + 2小时分段明细）
const loadData = async () => {
  state.loading = true
  const parkingId = state.parkingId || null
  try {
    const [stats, hourly, daily, table] = await Promise.all([
      reportApi.getTrafficStats({ parkingId }),
      reportApi.getTrafficHourly({ parkingId }),
      reportApi.getTrafficDaily({ parkingId }),
      reportApi.getTrafficTable({ parkingId })
    ])
    applyStats(stats.data)
    state.hourly = hourly.data || []
    state.daily = daily.data || []
    state.tableData = table.data || []
  } catch (e) {
    // authRequest 已统一提示
  } finally {
    state.loading = false
  }
  await nextTick()
  initCharts()
}

// 分时段车流量（24 小时） + 七日对比
const initCharts = () => {
  const hourly = state.hourly || []
  const daily = state.daily || []
  if (hourChartRef.value) {
    const chart = echarts.getInstanceByDom(hourChartRef.value) || echarts.init(hourChartRef.value)
    chart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['入园', '出园'], top: 0, textStyle: { color: '#86909C' } },
      grid: { left: 40, right: 20, top: 30, bottom: 40 },
      xAxis: { type: 'category', data: hourly.map((i) => String(i.hour)), axisLine: { lineStyle: { color: '#E5E6EB' } }, axisLabel: { color: '#86909C' } },
      yAxis: { type: 'value', axisLine: { show: false }, splitLine: { lineStyle: { color: '#F2F3F5' } }, axisLabel: { color: '#86909C' } },
      series: [
        { name: '入园', type: 'line', data: hourly.map((i) => Number(i.inCount ?? 0)), smooth: true, areaStyle: { color: 'rgba(22,93,255,0.1)' }, itemStyle: { color: '#165DFF' }, lineStyle: { color: '#165DFF' } },
        { name: '出园', type: 'line', data: hourly.map((i) => Number(i.outCount ?? 0)), smooth: true, areaStyle: { color: 'rgba(0,180,42,0.1)' }, itemStyle: { color: '#00B42A' }, lineStyle: { color: '#00B42A' } }
      ]
    }, true)
  }
  if (compareChartRef.value) {
    const chart = echarts.getInstanceByDom(compareChartRef.value) || echarts.init(compareChartRef.value)
    chart.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['入园', '出园'], top: 0, textStyle: { color: '#86909C' } },
      grid: { left: 40, right: 20, top: 30, bottom: 40 },
      xAxis: { type: 'category', data: daily.map((i) => formatDateLabel(i.date)), axisLine: { lineStyle: { color: '#E5E6EB' } }, axisLabel: { color: '#86909C' } },
      yAxis: { type: 'value', axisLine: { show: false }, splitLine: { lineStyle: { color: '#F2F3F5' } }, axisLabel: { color: '#86909C' } },
      series: [
        { name: '入园', type: 'bar', data: daily.map((i) => Number(i.inCount ?? 0)), barWidth: 12, itemStyle: { color: '#165DFF', borderRadius: [4, 4, 0, 0] } },
        { name: '出园', type: 'bar', data: daily.map((i) => Number(i.outCount ?? 0)), barWidth: 12, itemStyle: { color: '#00B42A', borderRadius: [4, 4, 0, 0] } }
      ]
    }, true)
  }
}

const handleSearch = () => {
  if (state.timeType !== 'day') {
    Message.info('按周/月维度待对接')
    return
  }
  loadData()
}
const handleExport = () => Message.info('导出功能待对接')

const handleResize = () => {
  if (hourChartRef.value) echarts.getInstanceByDom(hourChartRef.value)?.resize()
  if (compareChartRef.value) echarts.getInstanceByDom(compareChartRef.value)?.resize()
}

onMounted(async () => {
  await loadParkingList()
  await loadData()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  if (hourChartRef.value) echarts.getInstanceByDom(hourChartRef.value)?.dispose()
  if (compareChartRef.value) echarts.getInstanceByDom(compareChartRef.value)?.dispose()
})
</script>

<style lang="less" scoped>
.report-page { .report-toolbar { display: flex; gap: var(--spacing-md); margin-bottom: var(--spacing-lg); padding: var(--spacing-xl); background: var(--bg-color); border-radius: var(--border-radius-base); box-shadow: var(--shadow-base); .filter-select { minWidth: 180px; } .filter-select-sm { minWidth: 120px; } } .stats-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--spacing-lg); margin-bottom: var(--spacing-lg); .stat-card { padding: var(--spacing-xl); background: var(--bg-color); border-radius: var(--border-radius-base); box-shadow: var(--shadow-base); text-align: center; .stat-label { font-size: var(--font-size-sm); color: var(--text-color-secondary); } .stat-value { font-size: 28px; font-weight: 600; color: var(--primary-color); margin-top: var(--spacing-sm); } } } .charts-row { display: grid; grid-template-columns: 1fr 1fr; gap: var(--spacing-lg); margin-bottom: var(--spacing-lg); .chart-card { background: var(--bg-color); border-radius: var(--border-radius-base); box-shadow: var(--shadow-base); padding: var(--spacing-xl); .chart-title { font-size: var(--font-size-md); font-weight: 600; margin-bottom: var(--spacing-lg); } .chart-body { height: 250px; } } } .table-section { background: var(--bg-color); border-radius: var(--border-radius-base); box-shadow: var(--shadow-base); padding: var(--spacing-xl); } }
</style>
