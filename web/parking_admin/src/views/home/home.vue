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
            ¥{{ state.stats.todayRevenue }}
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
          <RadioGroup v-model="state.revenueChartType" type="button" size="small">
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

    <!-- 下方分区 -->
    <div class="bottom-row">
      <!-- 车位状态可视化 -->
      <div class="card space-status-card">
        <div class="card-header">
          <h3 class="card-title">车位状态</h3>
          <span class="card-more" @click="goToSpaceManagement">查看详情 →</span>
        </div>
        <div class="card-content">
          <div class="space-legend">
            <span class="legend-item">
              <span class="legend-block legend-free"></span>
              空闲 {{ state.spaceStats.free }}
            </span>
            <span class="legend-item">
              <span class="legend-block legend-fixed"></span>
              固定车 {{ state.spaceStats.fixed }}
            </span>
            <span class="legend-item">
              <span class="legend-block legend-temp"></span>
              临时车 {{ state.spaceStats.temp }}
            </span>
            <span class="legend-item">
              <span class="legend-block legend-fault"></span>
              故障 {{ state.spaceStats.fault }}
            </span>
          </div>
          <div class="space-grid">
            <div
              v-for="(space, index) in state.spaceList"
              :key="index"
              class="space-item"
              :class="'space-' + space.status"
              :title="'车位号: ' + space.no"
            >
              {{ space.no }}
            </div>
          </div>
        </div>
      </div>

      <!-- 实时通行列表 -->
      <div class="card realtime-passage-card">
        <div class="card-header">
          <h3 class="card-title">实时通行</h3>
          <div class="realtime-indicator">
            <span class="pulse"></span>
            实时更新中
          </div>
        </div>
        <div class="card-content">
          <Table
            :columns="passageColumns"
            :data="state.passageList"
            :loading="state.passageLoading"
            size="small"
            :height="280"
          >
            <template #status="{ row }">
              <span class="status-tag" :class="'status-tag--' + getStatusTag(row.status)">
                {{ getStatusText(row.status) }}
              </span>
            </template>
            <template #action="{ row }">
              <Button type="text" size="small" @click="handleViewDetail(row)">详情</Button>
              <Button type="text" size="small" @click="handleOpenGate(row)" v-if="row.canOpen">开闸</Button>
            </template>
          </Table>
        </div>
      </div>
    </div>

    <!-- 右下角快捷操作按钮 -->
    <div class="quick-actions">
      <div class="quick-btn" @click="goToAddParking">
        <Icon type="ios-add-circle-outline" />
        <span>新增停车场</span>
      </div>
      <div class="quick-btn" @click="handleExportBill">
        <Icon type="ios-download-outline" />
        <span>导出账单</span>
      </div>
      <div class="quick-btn" @click="goToDeviceInspection">
        <Icon type="ios-checkbox-outline" />
        <span>设备巡检</span>
      </div>
      <div class="quick-btn" @click="goToBatchRenewal">
        <Icon type="ios-refresh-circle-outline" />
        <span>批量续费</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, onUnmounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { Icon, RadioGroup, Radio, Progress, Table, Button, Message } from 'view-ui-plus'
import * as echarts from 'echarts'

const router = useRouter()
const revenueChartRef = ref(null)
const trafficChartRef = ref(null)

const state = reactive({
  // 统计数据
  stats: {
    todayRevenue: '12,580',
    revenueTrend: 15.6,
    currentVehicles: 156,
    totalSpaces: 300,
    spaceUsageRate: 52,
    todayIn: 89,
    todayOut: 76,
    exceptionCount: 8,
    unpaidCount: 4,
    noPlateCount: 2,
    faultCount: 2
  },

  // 营收图表类型
  revenueChartType: '日',

  // 车位统计
  spaceStats: {
    free: 144,
    fixed: 98,
    temp: 50,
    fault: 8
  },

  // 车位列表（模拟）
  spaceList: [],

  // 实时通行列表
  passageList: [],
  passageLoading: false
})

// 通行记录表格列
const passageColumns = [
  {
    title: '车牌号',
    key: 'plate',
    minWidth: 100,
    fixed: 'left'
  },
  {
    title: '通道',
    key: 'channel',
    minWidth: 100
  },
  {
    title: '类型',
    key: 'type',
    minWidth: 80,
    render: (h, params) => {
      const types = { in: '进场', out: '出场' }
      return h('span', types[params.row.type] || '-')
    }
  },
  {
    title: '进场时间',
    key: 'inTime',
    minWidth: 150
  },
  {
    title: '状态',
    slot: 'status',
    minWidth: 100
  },
  {
    title: '操作',
    slot: 'action',
    minWidth: 120,
    fixed: 'right'
  }
]

// 初始化车位列表
const initSpaceList = () => {
  const list = []
  const statuses = ['free', 'fixed', 'temp', 'fault']
  const statusCounts = {
    free: 15,
    fixed: 10,
    temp: 8,
    fault: 3
  }

  statuses.forEach(status => {
    for (let i = 0; i < statusCounts[status]; i++) {
      list.push({
        no: list.length + 1,
        status: status
      })
    }
  })

  state.spaceList = list
}

// 初始化实时通行列表
const initPassageList = () => {
  state.passageLoading = true
  setTimeout(() => {
    state.passageList = [
      { id: 1, plate: '京A12345', channel: '1号入口', type: 'in', inTime: '2024-01-15 09:23:15', status: 'normal', canOpen: true },
      { id: 2, plate: '京B67890', channel: '2号出口', type: 'out', inTime: '2024-01-15 08:15:00', status: 'normal', canOpen: true },
      { id: 3, plate: '无牌车', channel: '3号入口', type: 'in', inTime: '2024-01-15 09:18:00', status: 'noPlate', canOpen: false },
      { id: 4, plate: '京C11111', channel: '1号入口', type: 'in', inTime: '2024-01-15 09:12:00', status: 'normal', canOpen: false },
      { id: 5, plate: '京D22222', channel: '地下入口', type: 'in', inTime: '2024-01-15 09:05:00', status: 'normal', canOpen: false },
      { id: 6, plate: '黑名单', channel: '2号出口', type: 'out', inTime: '2024-01-14 22:30:00', status: 'blacklist', canOpen: false }
    ]
    state.passageLoading = false
  }, 500)
}

// 获取状态标签样式
const getStatusTag = (status) => {
  const map = {
    normal: 'success',
    noPlate: 'warning',
    blacklist: 'error'
  }
  return map[status] || 'default'
}

// 获取状态文本
const getStatusText = (status) => {
  const map = {
    normal: '正常',
    noPlate: '无牌',
    blacklist: '黑名单'
  }
  return map[status] || status
}

// 初始化营收图表
const initRevenueChart = () => {
  if (!revenueChartRef.value) return

  const chart = echarts.init(revenueChartRef.value)
  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        const item = params[0]
        return `${item.axisValue}<br/>营收: ¥${item.value}`
      }
    },
    grid: {
      left: 40,
      right: 20,
      top: 20,
      bottom: 30
    },
    xAxis: {
      type: 'category',
      data: ['1月9日', '1月10日', '1月11日', '1月12日', '1月13日', '1月14日', '1月15日'],
      axisLine: { lineStyle: { color: '#E5E6EB' } },
      axisLabel: { color: '#86909C', fontSize: 12 }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisLabel: {
        color: '#86909C',
        fontSize: 12,
        formatter: (val) => `¥${val / 1000}k`
      },
      splitLine: { lineStyle: { color: '#F2F3F5' } }
    },
    series: [{
      type: 'line',
      data: [8200, 9320, 9010, 12340, 12900, 13300, 12580],
      smooth: true,
      symbol: 'circle',
      symbolSize: 8,
      lineStyle: { color: '#165DFF', minWidth: 2 },
      itemStyle: { color: '#165DFF' },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: 'rgba(22, 93, 255, 0.2)' },
          { offset: 1, color: 'rgba(22, 93, 255, 0)' }
        ])
      }
    }]
  }
  chart.setOption(option)
}

// 初始化车流量图表
const initTrafficChart = () => {
  if (!trafficChartRef.value) return

  const chart = echarts.init(trafficChartRef.value)
  const option = {
    tooltip: {
      trigger: 'axis',
      formatter: (params) => {
        let result = params[0].axisValue + '<br/>'
        params.forEach(item => {
          result += `${item.marker} ${item.seriesName}: ${item.value}辆<br/>`
        })
        return result
      }
    },
    grid: {
      left: 40,
      right: 20,
      top: 20,
      bottom: 30
    },
    legend: {
      show: false
    },
    xAxis: {
      type: 'category',
      data: ['1月9日', '1月10日', '1月11日', '1月12日', '1月13日', '1月14日', '1月15日'],
      axisLine: { lineStyle: { color: '#E5E6EB' } },
      axisLabel: { color: '#86909C', fontSize: 12 }
    },
    yAxis: {
      type: 'value',
      axisLine: { show: false },
      axisLabel: { color: '#86909C', fontSize: 12 },
      splitLine: { lineStyle: { color: '#F2F3F5' } }
    },
    series: [
      {
        name: '进场',
        type: 'bar',
        data: [120, 145, 132, 168, 175, 189, 165],
        barWidth: 16,
        itemStyle: { color: '#165DFF', borderRadius: [4, 4, 0, 0] }
      },
      {
        name: '出场',
        type: 'bar',
        data: [105, 130, 118, 152, 160, 172, 156],
        barWidth: 16,
        itemStyle: { color: '#00B42A', borderRadius: [4, 4, 0, 0] }
      }
    ]
  }
  chart.setOption(option)
}

// 路由跳转方法
const goToChargeFlow = () => router.push({ name: 'chargeFlow' })
const goToSpaceManagement = () => router.push({ name: 'spaceManagement' })
const goToPassageRecord = () => router.push({ name: 'passageRecord' })
const goToExceptionRecord = () => router.push({ name: 'exceptionRecord' })
const goToAddParking = () => router.push({ name: 'parkingList' })
const goToDeviceInspection = () => router.push({ name: 'deviceList' })
const goToBatchRenewal = () => router.push({ name: 'memberList' })
const handleExportBill = () => Message.info('导出功能开发中')
const handleViewDetail = (row) => console.log('查看详情', row)
const handleOpenGate = (row) => {
  Message.success(`正在为 ${row.plate} 开闸...`)
}

// 定时刷新实时通行
let refreshTimer = null

onMounted(() => {
  initSpaceList()
  initPassageList()

  nextTick(() => {
    initRevenueChart()
    initTrafficChart()
  })

  // 每30秒刷新一次实时通行
  refreshTimer = setInterval(() => {
    initPassageList()
  }, 30000)

  // 监听窗口变化，重绘图表
  window.addEventListener('resize', () => {
    if (revenueChartRef.value) {
      echarts.getInstanceByDom(revenueChartRef.value)?.resize()
    }
    if (trafficChartRef.value) {
      echarts.getInstanceByDom(trafficChartRef.value)?.resize()
    }
  })
})

onUnmounted(() => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
  }
})
</script>

<style lang="less" scoped>
.home-page {
  position: relative;
  padding-bottom: 80px;
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
    minWidth: 56px;
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
          minWidth: 8px;
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
      minWidth: 100%;
      height: 100%;
    }
  }
}

// 下方分区
.bottom-row {
  display: grid;
  grid-template-columns: 1fr 1.5fr;
  gap: var(--spacing-xl);
}

.card {
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
}

// 车位状态卡片
.space-status-card {
  .space-legend {
    display: flex;
    gap: var(--spacing-lg);
    margin-bottom: var(--spacing-lg);

    .legend-item {
      display: flex;
      align-items: center;
      font-size: var(--font-size-sm);
      color: var(--text-color-secondary);

      .legend-block {
        minWidth: 12px;
        height: 12px;
        border-radius: 2px;
        margin-right: var(--spacing-xs);
      }

      .legend-free {
        background-color: #00B42A;
      }

      .legend-fixed {
        background-color: #165DFF;
      }

      .legend-temp {
        background-color: #FF7D00;
      }

      .legend-fault {
        background-color: #86909C;
      }
    }
  }

  .space-grid {
    display: grid;
    grid-template-columns: repeat(6, 1fr);
    gap: 8px;

    .space-item {
      aspect-ratio: 1;
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: var(--font-size-xs);
      color: #fff;
      border-radius: var(--border-radius-sm);
      cursor: pointer;
      transition: transform 0.2s;

      &:hover {
        transform: scale(1.1);
      }

      &.space-free {
        background-color: #00B42A;
      }

      &.space-fixed {
        background-color: #165DFF;
      }

      &.space-temp {
        background-color: #FF7D00;
      }

      &.space-fault {
        background-color: #86909C;
      }
    }
  }
}

// 实时通行卡片
.realtime-passage-card {
  .realtime-indicator {
    display: flex;
    align-items: center;
    font-size: var(--font-size-sm);
    color: var(--success-color);

    .pulse {
      minWidth: 8px;
      height: 8px;
      background-color: var(--success-color);
      border-radius: 50%;
      margin-right: var(--spacing-xs);
      animation: pulse 1.5s ease-in-out infinite;
    }
  }
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

// 右下角快捷操作
.quick-actions {
  position: fixed;
  right: var(--spacing-xl);
  bottom: var(--spacing-xl);
  display: flex;
  gap: var(--spacing-md);

  .quick-btn {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    minWidth: 80px;
    height: 80px;
    background-color: var(--bg-color);
    border-radius: var(--border-radius-base);
    box-shadow: var(--shadow-medium);
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      transform: translateY(-4px);
      box-shadow: var(--shadow-heavy);
      color: var(--primary-color);

      .ivu-icon {
        color: var(--primary-color);
      }
    }

    .ivu-icon {
      font-size: 28px;
      color: var(--text-color);
      margin-bottom: var(--spacing-xs);
    }

    span {
      font-size: var(--font-size-xs);
      color: var(--text-color-secondary);
    }
  }
}
</style>