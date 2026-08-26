<template>
  <div class="home-page">
    <!-- 顶部核心指标卡片 -->
    <div class="stats-row">
      <div class="stat-card stat-revenue" @click="goToChargeFlow">
        <div class="stat-icon">
          <Icon type="logo-yen" />
        </div>
        <div class="stat-content">
          <div class="stat-label">今日总营收</div>
          <div class="stat-value">
            ¥{{ formatRevenue(state.stats.todayRevenue) }}
            <span class="stat-trend" :class="state.stats.revenueTrend < 0 ? 'down' : 'up'">
              {{ state.stats.revenueTrend >= 0 ? '↑' : '↓' }}{{ Math.abs(state.stats.revenueTrend) }}%
            </span>
          </div>
          <div class="stat-compare">较昨日{{ state.stats.revenueTrend >= 0 ? '增长' : '下降' }}</div>
        </div>
      </div>

      <div class="stat-card stat-spaces" @click="goToSpaceManagement">
        <div class="stat-icon">
          <Icon type="ios-square" />
        </div>
        <div class="stat-content">
          <div class="stat-label">当前在场车辆</div>
          <div class="stat-value">
            {{ state.stats.currentVehicles }}
            <span class="stat-unit">辆 / {{ state.stats.totalSpaces }}位</span>
          </div>
          <div class="stat-progress">
            <Progress
              :percent="state.stats.spaceUsageRate"
              :stroke-width="6"
              :show-text="false"
              stroke-color="#165DFF"
            />
          </div>
          <div class="stat-compare">车位利用率 {{ state.stats.spaceUsageRate }}%</div>
        </div>
      </div>

      <div class="stat-card stat-traffic" @click="goToPassageRecord">
        <div class="stat-icon">
          <Icon type="ios-swap" />
        </div>
        <div class="stat-content">
          <div class="stat-label">今日进出车次</div>
          <div class="stat-value">
            <span class="traffic-item">
              <span class="traffic-label">进</span>
              <span class="traffic-value">{{ state.stats.todayIn }}</span>
            </span>
            <span class="traffic-separator">/</span>
            <span class="traffic-item">
              <span class="traffic-label">出</span>
              <span class="traffic-value">{{ state.stats.todayOut }}</span>
            </span>
          </div>
          <div class="stat-compare">活跃车辆统计</div>
        </div>
      </div>

      <div class="stat-card stat-exception" @click="goToExceptionRecord">
        <div class="stat-icon">
          <Icon type="ios-warning" />
        </div>
        <div class="stat-content">
          <div class="stat-label">待处理异常订单</div>
          <div class="stat-value stat-value-exception">{{ state.stats.exceptionCount }} 笔</div>
          <div class="stat-tags">
            <span class="exception-tag" v-if="state.stats.unpaidCount > 0">
              欠费 {{ state.stats.unpaidCount }}
            </span>
            <span class="exception-tag" v-if="state.stats.noPlateCount > 0">
              无牌 {{ state.stats.noPlateCount }}
            </span>
            <span class="exception-tag" v-if="state.stats.faultCount > 0">
              故障 {{ state.stats.faultCount }}
            </span>
          </div>
        </div>
      </div>
    </div>

    <!-- 中部图表区 -->
    <div class="charts-row">
      <div class="chart-card">
        <div class="chart-header">
          <h3 class="chart-title">近7日营收趋势</h3>
          <RadioGroup v-model="state.revenueChartType" type="button" size="small" @on-change="handleRevenueChartTypeChange">
            <Radio label="日">日</Radio>
            <Radio label="周">周</Radio>
            <Radio label="月">月</Radio>
          </RadioGroup>
        </div>
        <div class="chart-body">
          <div ref="revenueChartRef" class="chart-container"></div>
        </div>
      </div>

      <div class="chart-card">
        <div class="chart-header">
          <h3 class="chart-title">近7日车流量</h3>
          <div class="chart-legend">
            <span class="legend-item">
              <span class="legend-dot legend-in"></span>
              进场
            </span>
            <span class="legend-item">
              <span class="legend-dot legend-out"></span>
              出场
            </span>
          </div>
        </div>
        <div class="chart-body">
          <div ref="trafficChartRef" class="chart-container"></div>
        </div>
      </div>
    </div>

    <!-- 实时通行抓拍列表 -->
    <div class="realtime-passage-card">
      <div class="card-header">
        <h3 class="card-title">实时通行抓拍列表</h3>
        <div class="realtime-indicator">
          <span class="pulse"></span>
          实时更新中
        </div>
      </div>
      <div class="card-content">
        <div class="section-tip">抓拍图片需对接摄像头设备，下方为通行记录占位缩略图</div>
        <TableColumnSetting :columns="passageColumns" v-model:visible="visibleFields" v-model:open="colSettingVisible" @reset="resetColumns" />
        <Table
          :columns="displayColumns"
          :data="state.passageList"
          :loading="state.passageLoading"
          size="small"
        >
          <template #captureImage="{ row }">
            <div class="capture-thumbnail">
              <Icon type="ios-image" />
            </div>
          </template>
          <template #passageType="{ row }">
            <Tag :color="row.passageType === 'in' ? 'blue' : 'green'">
              {{ row.passageType === 'in' ? '进场' : '出场' }}
            </Tag>
          </template>
          <template #status="{ row }">
            <span class="status-tag" :class="'status-tag--' + getStatusTag(row.status)">
              {{ getStatusText(row.status) }}
            </span>
          </template>
          <template #action="{ row }">
            <Button type="text" size="small" @click="handleViewDetail(row)">查看详情</Button>
            <Button type="text" size="small" @click="handleOpenGate(row)" v-if="row.canOpen">开闸</Button>
          </template>
        </Table>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { Icon, RadioGroup, Radio, Progress, Table, Button, Tag, Message } from 'view-ui-plus'
import * as echarts from 'echarts'
import { dashboardApi, passageApi } from '@/api'
import TableColumnSetting from '@/components/TableColumnSetting.vue'
import { useTableColumns } from '@/composables/useTableColumns'

const router = useRouter()
const revenueChartRef = ref(null)
const trafficChartRef = ref(null)

const state = reactive({
  // 核心指标
  stats: {
    todayRevenue: 0,
    yesterdayRevenue: 0,
    revenueTrend: 0,
    currentVehicles: 0,
    totalSpaces: 0,
    spaceUsageRate: 0,
    todayIn: 0,
    todayOut: 0,
    exceptionCount: 0,
    unpaidCount: 0,
    noPlateCount: 0,
    faultCount: 0
  },
  // 近7日趋势
  revenueSeries: [],
  trafficSeries: [],
  // 营收图表维度（仅"日"已对接）
  revenueChartType: '日',
  // 实时通行列表
  passageList: [],
  passageLoading: false
})

// 通行记录表格列
const passageColumns = [
  { field: 'captureImage', title: '抓拍图片', slot: 'captureImage', width: 90, align: 'center' },
  { field: 'plate', title: '车牌', key: 'plate', minWidth: 120 },
  { field: 'inTime', title: '通行时间', key: 'inTime', minWidth: 160 },
  { field: 'channel', title: '通道', key: 'channel', minWidth: 100 },
  { field: 'carType', title: '车辆类型', key: 'carType', minWidth: 90, render: (h, params) => h('span', params.row.carType || '-') },
  { field: 'status', title: '状态', slot: 'status', minWidth: 100 },
  { title: '操作', slot: 'action', minWidth: 150, fixed: 'right' }
]

const { visibleFields, colSettingVisible, displayColumns, resetColumns } = useTableColumns(passageColumns, 'home:passage:columnVisible')

// 金额格式化（千分位 + 两位小数）
const formatRevenue = (n) => Number(n || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })

// 日期标签：2024-01-09 → 1月9日
const formatDateLabel = (d) => {
  if (!d) return ''
  const parts = String(d).split('-')
  if (parts.length !== 3) return d
  return `${parseInt(parts[1], 10)}月${parseInt(parts[2], 10)}日`
}

// 停车记录 → 实时通行行
const mapCarLog = (log) => ({
  id: log.id,
  plate: log.carNumber || '无牌车',
  passageType: log.outTime ? 'out' : 'in',
  inTime: log.outTime || log.entryTime || '',
  channel: '—',
  carType: log.carType || '',
  status: log.carNumber ? 'normal' : 'noPlate',
  canOpen: true
})

// 写入核心指标
const applyStats = (s) => {
  if (!s) return
  state.stats.todayRevenue = Number(s.todayRevenue ?? 0)
  state.stats.yesterdayRevenue = Number(s.yesterdayRevenue ?? 0)
  state.stats.revenueTrend = Number(s.revenueTrend ?? 0)
  state.stats.currentVehicles = Number(s.currentVehicles ?? 0)
  state.stats.totalSpaces = Number(s.totalSpaces ?? 0)
  state.stats.spaceUsageRate = Number(s.spaceUsageRate ?? 0)
  state.stats.todayIn = Number(s.todayIn ?? 0)
  state.stats.todayOut = Number(s.todayOut ?? 0)
  state.stats.exceptionCount = Number(s.exceptionCount ?? 0)
  state.stats.unpaidCount = Number(s.unpaidCount ?? 0)
  state.stats.noPlateCount = Number(s.noPlateCount ?? 0)
  state.stats.faultCount = Number(s.faultCount ?? 0)
}

// 写入实时通行
const applyPassage = (list) => {
  state.passageList = (list || []).map(mapCarLog)
}

// 全量加载（首屏：指标 + 趋势 + 通行）
const loadData = async () => {
  state.passageLoading = true
  try {
    const [stats, revenue, traffic, passage] = await Promise.all([
      dashboardApi.getDashboardStats(),
      dashboardApi.getRevenueTrend(),
      dashboardApi.getTrafficTrend(),
      passageApi.getRecordList({ size: 8 })
    ])
    applyStats(stats.data)
    state.revenueSeries = revenue.data || []
    state.trafficSeries = traffic.data || []
    applyPassage(passage.data)
  } catch (e) {
    // authRequest 已统一提示
  } finally {
    state.passageLoading = false
  }
  await nextTick()
  initRevenueChart()
  initTrafficChart()
}

// 实时刷新（每30秒：仅指标 + 通行，趋势按日变化无需高频刷新）
const refreshRealtime = async () => {
  try {
    const [stats, passage] = await Promise.all([
      dashboardApi.getDashboardStats(),
      passageApi.getRecordList({ size: 8 })
    ])
    applyStats(stats.data)
    applyPassage(passage.data)
  } catch (e) {
    // 忽略静默失败
  }
}

// 状态标签样式
const getStatusTag = (status) => ({ normal: 'success', noPlate: 'warning', blacklist: 'error' }[status] || 'default')
const getStatusText = (status) => ({ normal: '正常', noPlate: '无牌', blacklist: '黑名单' }[status] || status)

// 营收图表
const initRevenueChart = () => {
  if (!revenueChartRef.value) return
  const chart = echarts.getInstanceByDom(revenueChartRef.value) || echarts.init(revenueChartRef.value)
  const series = state.revenueSeries || []
  const option = {
    tooltip: { trigger: 'axis', formatter: (params) => `${params[0].axisValue}<br/>营收: ¥${formatRevenue(params[0].value)}` },
    grid: { left: 50, right: 20, top: 20, bottom: 30 },
    xAxis: {
      type: 'category',
      data: series.map((i) => formatDateLabel(i.date)),
      axisLine: { lineStyle: { color: '#E5E6EB' } },
      axisLabel: { color: '#86909C', fontSize: 12 }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisLabel: { color: '#86909C', fontSize: 12, formatter: (val) => `¥${val / 1000}k` },
      splitLine: { lineStyle: { color: '#F2F3F5' } }
    },
    series: [{
      type: 'line',
      data: series.map((i) => Number(i.amount ?? 0)),
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      lineStyle: { color: '#165DFF', width: 2 },
      itemStyle: { color: '#165DFF' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(22, 93, 255, 0.2)' },
          { offset: 1, color: 'rgba(22, 93, 255, 0)' }
        ])
      }
    }]
  }
  chart.setOption(option, true)
}

// 车流量图表
const initTrafficChart = () => {
  if (!trafficChartRef.value) return
  const chart = echarts.getInstanceByDom(trafficChartRef.value) || echarts.init(trafficChartRef.value)
  const series = state.trafficSeries || []
  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        let result = params[0].axisValue + '<br/>'
        params.forEach((item) => { result += `${item.marker} ${item.seriesName}: ${item.value}辆<br/>` })
        return result
      }
    },
    grid: { left: 40, right: 20, top: 20, bottom: 30 },
    legend: { show: false },
    xAxis: {
      type: 'category',
      data: series.map((i) => formatDateLabel(i.date)),
      axisLine: { lineStyle: { color: '#E5E6EB' } },
      axisLabel: { color: '#86909C', fontSize: 12 }
    },
    yAxis: { type: 'value', axisLine: { show: false }, axisLabel: { color: '#86909C', fontSize: 12 }, splitLine: { lineStyle: { color: '#F2F3F5' } } },
    series: [
      { name: '进场', type: 'bar', data: series.map((i) => Number(i.inCount ?? 0)), barWidth: 16, itemStyle: { color: '#165DFF', borderRadius: [4, 4, 0, 0] } },
      { name: '出场', type: 'bar', data: series.map((i) => Number(i.outCount ?? 0)), barWidth: 16, itemStyle: { color: '#00B42A', borderRadius: [4, 4, 0, 0] } }
    ]
  }
  chart.setOption(option, true)
}

// 路由跳转
const goToChargeFlow = () => router.push({ name: 'chargeFlow' })
const goToSpaceManagement = () => router.push({ name: 'spaceManagement' })
const goToPassageRecord = () => router.push({ name: 'passageRecord' })
const goToExceptionRecord = () => router.push({ name: 'exceptionRecord' })
const handleViewDetail = () => router.push({ name: 'passageRecord' })
const handleOpenGate = () => Message.info('远程开闸需对接道闸设备控制接口')
const handleRevenueChartTypeChange = (val) => {
  if (val !== '日') {
    Message.info('周/月维度统计待对接')
    state.revenueChartType = '日'
  }
}

// 定时刷新与窗口尺寸
let refreshTimer = null
const handleResize = () => {
  if (revenueChartRef.value) echarts.getInstanceByDom(revenueChartRef.value)?.resize()
  if (trafficChartRef.value) echarts.getInstanceByDom(trafficChartRef.value)?.resize()
}

onMounted(async () => {
  await loadData()
  refreshTimer = setInterval(refreshRealtime, 30000)
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  if (refreshTimer) clearInterval(refreshTimer)
  window.removeEventListener('resize', handleResize)
  ;[revenueChartRef, trafficChartRef].forEach((r) => {
    if (r.value) echarts.getInstanceByDom(r.value)?.dispose()
  })
})
</script>

<style lang="less" scoped>
.home-page {
  height: 100%;
}

// 顶部统计卡片行
.stats-row {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--spacing-xl);
  margin-bottom: var(--spacing-xl);
}

.stat-card {
  display: flex;
  padding: var(--spacing-xl);
  background-color: var(--bg-color);
  border-radius: var(--border-radius-base);
  box-shadow: var(--shadow-base);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;

  &:hover {
    transform: translateY(-2px);
    box-shadow: var(--shadow-medium);
  }

  .stat-icon {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 56px;
    height: 56px;
    border-radius: var(--border-radius-lg);
    margin-right: var(--spacing-lg);

    .ivu-icon {
      font-size: 28px;
      color: #fff;
    }
  }

  .stat-content {
    flex: 1;

    .stat-label {
      font-size: var(--font-size-sm);
      color: var(--text-color-secondary);
      margin-bottom: var(--spacing-sm);
    }

    .stat-value {
      font-size: 24px;
      font-weight: 600;
      color: var(--text-color-title);
      line-height: 1.2;

      .stat-trend {
        font-size: var(--font-size-sm);
        font-weight: normal;
        margin-left: var(--spacing-sm);

        &.up {
          color: var(--success-color);
        }

        &.down {
          color: var(--error-color);
        }
      }

      .stat-unit {
        font-size: var(--font-size-sm);
        font-weight: normal;
        color: var(--text-color-secondary);
      }
    }

    .stat-compare {
      font-size: var(--font-size-xs);
      color: var(--text-color-secondary);
      margin-top: var(--spacing-xs);
    }

    .stat-tags {
      display: flex;
      gap: var(--spacing-sm);
      margin-top: var(--spacing-sm);

      .exception-tag {
        padding: 2px 6px;
        font-size: var(--font-size-xs);
        background-color: rgba(245, 63, 63, 0.1);
        color: var(--error-color);
        border-radius: var(--border-radius-sm);
      }
    }
  }
}

// 统计卡片颜色
.stat-revenue .stat-icon {
  background: linear-gradient(135deg, #165DFF, #4080FF);
}

.stat-spaces .stat-icon {
  background: linear-gradient(135deg, #722ED1, #9254DE);
}

.stat-traffic .stat-icon {
  background: linear-gradient(135deg, #00B42A, #23D130);
}

.stat-exception .stat-icon {
  background: linear-gradient(135deg, #F53F3F, #FF7875);
}

// 车流量数值
.stat-value-exception {
  color: var(--error-color) !important;
}

.traffic-item {
  display: inline-flex;
  align-items: baseline;

  .traffic-label {
    font-size: var(--font-size-sm);
    margin-right: var(--spacing-xs);
    color: var(--text-color-secondary);
  }

  .traffic-value {
    font-size: 20px;
  }
}

.traffic-separator {
  margin: 0 var(--spacing-sm);
  color: var(--text-color-secondary);
}

// 图表区
.charts-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--spacing-xl);
  margin-bottom: var(--spacing-xl);
}

.chart-card {
  background-color: var(--bg-color);
  border-radius: var(--border-radius-base);
  box-shadow: var(--shadow-base);
  padding: var(--spacing-xl);

  .chart-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: var(--spacing-lg);

    .chart-title {
      font-size: var(--font-size-md);
      font-weight: 600;
      color: var(--text-color-title);
    }

    .chart-legend {
      display: flex;
      gap: var(--spacing-lg);

      .legend-item {
        display: flex;
        align-items: center;
        font-size: var(--font-size-sm);
        color: var(--text-color-secondary);

        .legend-dot {
          width: 8px;
          height: 8px;
          border-radius: 50%;
          margin-right: var(--spacing-xs);
        }

        .legend-in {
          background-color: #165DFF;
        }

        .legend-out {
          background-color: #00B42A;
        }
      }
    }
  }

  .chart-body {
    height: 260px;

    .chart-container {
      width: 100%;
      height: 100%;
    }
  }
}

// 实时通行卡片
.realtime-passage-card {
  background-color: var(--bg-color);
  border-radius: var(--border-radius-base);
  box-shadow: var(--shadow-base);
  padding: var(--spacing-xl);

  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: var(--spacing-lg);

    .card-title {
      font-size: var(--font-size-md);
      font-weight: 600;
      color: var(--text-color-title);
    }

    .card-more {
      font-size: var(--font-size-sm);
      color: var(--primary-color);
      cursor: pointer;

      &:hover {
        text-decoration: underline;
      }
    }
  }

  .realtime-indicator {
    display: flex;
    align-items: center;
    font-size: var(--font-size-sm);
    color: var(--success-color);

    .pulse {
      width: 8px;
      height: 8px;
      background-color: var(--success-color);
      border-radius: 50%;
      margin-right: var(--spacing-xs);
      animation: pulse 1.5s ease-in-out infinite;
    }
  }

  .capture-thumbnail {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 60px;
    height: 40px;
    background-color: var(--bg-color-page);
    border-radius: var(--border-radius-sm);
    color: var(--text-color-secondary);
  }
}

.section-tip {
  margin-bottom: var(--spacing-sm);
  font-size: var(--font-size-xs);
  color: var(--text-color-secondary);
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.5;
    transform: scale(1.2);
  }
}
</style>
