<template>
  <div class="home-page">
    <!-- 核心状态看板 -->
    <div class="status-board">
      <div class="status-card" :class="getUtilizationClass">
        <div class="status-icon blue">
          <Icon type="ios-car-outline" />
        </div>
        <div class="status-info">
          <div class="status-value">{{ systemStore.stats.currentVehicles }}</div>
          <div class="status-label">当前在场车辆</div>
          <div class="status-extra">
            车位利用率 {{ systemStore.spaceUtilization }}%
          </div>
        </div>
      </div>

      <div class="status-card">
        <div class="status-icon green">
          <Icon type="ios-enter-outline" />
        </div>
        <div class="status-info">
          <div class="status-value">{{ systemStore.stats.todayIn }}</div>
          <div class="status-label">今日进场</div>
          <div class="status-extra">出场 {{ systemStore.stats.todayOut }} 辆</div>
        </div>
      </div>

      <div class="status-card warning">
        <div class="status-icon orange">
          <Icon type="ios-alert-outline" />
        </div>
        <div class="status-info">
          <div class="status-value">{{ systemStore.stats.pendingExceptions }}</div>
          <div class="status-label">待处理异常</div>
          <div class="status-extra">点击快速处理</div>
        </div>
      </div>

      <div class="status-card" :class="{ 'has-offline': systemStore.offlineDeviceCount > 0 }">
        <div class="status-icon" :class="systemStore.offlineDeviceCount > 0 ? 'red' : 'green'">
          <Icon type="ios-construct-outline" />
        </div>
        <div class="status-info">
          <div class="status-value">
            在线 {{ systemStore.stats.onlineDevices }} / 离线 {{ systemStore.stats.offlineDevices }}
          </div>
          <div class="status-label">设备状态</div>
          <div class="status-extra">查看详情</div>
        </div>
      </div>
    </div>

    <!-- 实时通行预览 + 快捷操作 -->
    <div class="main-content">
      <!-- 实时通行预览 -->
      <div class="preview-section">
        <div class="section-header">
          <h3 class="section-title">实时通行预览</h3>
          <Button type="text" size="small" @click="router.push('/monitor')">
            全屏监控 <Icon type="ios-arrow-forward" />
          </Button>
        </div>

        <div class="channels-grid">
          <div
            v-for="channel in systemStore.channels"
            :key="channel.id"
            class="channel-card"
            :class="getChannelStatusClass(channel.status)"
            @click="handleChannelClick(channel)"
          >
            <div class="channel-video">
              <img
                v-if="channel.captureImage"
                :src="channel.captureImage"
                alt="监控画面"
              />
              <div v-else class="video-placeholder">
                <Icon type="ios-videocam-outline" size="32" />
                <span>{{ channel.name }}</span>
              </div>
              <div class="channel-overlay">
                <Tag :color="getChannelTagColor(channel.status)">
                  {{ channel.name }}
                </Tag>
              </div>
            </div>
            <div class="channel-info">
              <span class="channel-plate">{{ channel.currentPlate || '空闲' }}</span>
              <span class="channel-time" v-if="channel.currentTime">
                {{ channel.currentTime }}
              </span>
            </div>
          </div>
        </div>
      </div>

      <!-- 快捷操作区 -->
      <div class="action-section">
        <div class="section-header">
          <h3 class="section-title">快捷操作</h3>
        </div>

        <div class="action-buttons">
          <div class="action-btn-row">
            <Button
              type="primary"
              size="large"
              class="action-btn btn-in"
              @click="handleOpenGate('in')"
            >
              <Icon type="ios-log-in-outline" />
              <span>入口开闸</span>
            </Button>
            <Button
              type="primary"
              size="large"
              class="action-btn btn-out"
              @click="handleOpenGate('out')"
            >
              <Icon type="ios-log-out-outline" />
              <span>出口开闸</span>
            </Button>
          </div>

          <Button
            size="large"
            class="action-btn-secondary"
            @click="router.push('/release')"
          >
            <Icon type="ios-key-outline" />
            <span>临时权限开通</span>
          </Button>

          <Button
            size="large"
            class="action-btn-secondary"
            @click="router.push('/exception')"
          >
            <Icon type="ios-alert-outline" />
            <span>异常处理</span>
          </Button>

          <Button
            size="large"
            class="action-btn-secondary"
            @click="router.push('/equipment')"
          >
            <Icon type="ios-construct-outline" />
            <span>设备巡检</span>
          </Button>
        </div>
      </div>
    </div>

    <!-- 今日通行统计 -->
    <div class="stats-section">
      <div class="section-header">
        <h3 class="section-title">今日通行统计</h3>
        <RadioGroup v-model="statsType">
          <Radio label="traffic">车流量</Radio>
          <Radio label="revenue">营收预览</Radio>
        </RadioGroup>
      </div>

      <div class="stats-content">
        <div v-show="statsType === 'traffic'" class="traffic-chart" ref="trafficChartRef"></div>
        <div v-show="statsType === 'revenue'" class="revenue-info">
          <div class="revenue-card">
            <div class="revenue-value">¥{{ todayRevenue.total }}</div>
            <div class="revenue-label">今日营收</div>
          </div>
          <div class="revenue-breakdown">
            <div class="breakdown-item">
              <span class="breakdown-dot wechat"></span>
              <span class="breakdown-label">微信</span>
              <span class="breakdown-value">¥{{ todayRevenue.wechat }}</span>
            </div>
            <div class="breakdown-item">
              <span class="breakdown-dot alipay"></span>
              <span class="breakdown-label">支付宝</span>
              <span class="breakdown-value">¥{{ todayRevenue.alipay }}</span>
            </div>
            <div class="breakdown-item">
              <span class="breakdown-dot cash"></span>
              <span class="breakdown-label">现金</span>
              <span class="breakdown-value">¥{{ todayRevenue.cash }}</span>
            </div>
            <div class="breakdown-item">
              <span class="breakdown-dot member"></span>
              <span class="breakdown-label">月卡抵扣</span>
              <span class="breakdown-value">¥{{ todayRevenue.member }}</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Button, Icon, Tag, Radio, RadioGroup, Modal, Message } from 'view-ui-plus'
import { useSystemStore } from '@/stores/system'
import * as echarts from 'echarts'

const router = useRouter()
const systemStore = useSystemStore()

// 状态
const statsType = ref('traffic')
const trafficChartRef = ref(null)
let trafficChart = null

// 今日营收数据
const todayRevenue = reactive({
  total: '8,450',
  wechat: '4,200',
  alipay: '2,800',
  cash: '680',
  member: '770'
})

// 车位利用率样式
const getUtilizationClass = computed(() => {
  const util = systemStore.spaceUtilization
  if (util >= 95) return 'danger'
  if (util >= 80) return 'warning'
  return ''
})

// 通道状态样式
const getChannelStatusClass = (status) => {
  return {
    'channel-online': status === 'online',
    'channel-fault': status === 'fault',
    'channel-offline': status === 'offline'
  }
}

// 通道标签颜色
const getChannelTagColor = (status) => {
  const map = {
    online: 'success',
    fault: 'warning',
    offline: 'error'
  }
  return map[status] || 'default'
}

// 点击通道
const handleChannelClick = (channel) => {
  router.push({ path: '/monitor', query: { channelId: channel.id } })
}

// 开闸操作
const handleOpenGate = (direction) => {
  const title = direction === 'in' ? '入口开闸' : '出口开闸'
  Modal.confirm({
    title: `确认${title}`,
    content: `确定要远程开闸吗？操作后将自动记录日志。`,
    okText: '确认开闸',
    cancelText: '取消',
    onOk: () => {
      Message.success({
        content: `${title}成功`,
        duration: 2
      })
    }
  })
}

// 初始化图表
const initChart = () => {
  if (!trafficChartRef.value) return

  trafficChart = echarts.init(trafficChartRef.value)

  const hours = ['06:00', '08:00', '10:00', '12:00', '14:00', '16:00', '18:00', '20:00', '22:00']
  const inData = [12, 45, 78, 56, 34, 52, 68, 42, 18]
  const outData = [8, 32, 65, 48, 28, 45, 58, 38, 15]

  const option = {
    tooltip: {
      trigger: 'axis',
      axisPointer: { type: 'shadow' }
    },
    legend: {
      data: ['进场', '出场'],
      bottom: 0
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '15%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: hours
    },
    yAxis: {
      type: 'value',
      name: '车次'
    },
    series: [
      {
        name: '进场',
        type: 'bar',
        data: inData,
        itemStyle: { color: '#165DFF' },
        barWidth: 16
      },
      {
        name: '出场',
        type: 'bar',
        data: outData,
        itemStyle: { color: '#00B42A' },
        barWidth: 16
      }
    ]
  }

  trafficChart.setOption(option)
}

// 窗口调整
const handleResize = () => {
  trafficChart?.resize()
}

// 生命周期
onMounted(() => {
  initChart()
  window.addEventListener('resize', handleResize)

  // 模拟通道数据
  systemStore.channels.forEach((channel, index) => {
    if (index % 2 === 0) {
      channel.currentPlate = index === 0 ? '京A12345' : '京B67890'
      channel.currentTime = '刚刚'
      channel.status = 'online'
    }
  })
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  trafficChart?.dispose()
})
</script>

<style lang="less" scoped>
.home-page {
  max-width: 1600px;
  margin: 0 auto;
}

/* 状态看板 */
.status-board {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--spacing-lg);
  margin-bottom: var(--spacing-lg);
}

.status-card {
  background-color: var(--color-bg-card);
  border-radius: var(--border-radius-base);
  padding: var(--spacing-xl);
  display: flex;
  align-items: center;
  gap: var(--spacing-lg);
  box-shadow: var(--shadow-base);
  transition: transform var(--transition-fast), box-shadow var(--transition-fast);
  cursor: pointer;

  &:hover {
    transform: translateY(-2px);
    box-shadow: var(--shadow-md);
  }

  &.warning .status-icon {
    background: linear-gradient(135deg, var(--color-warning), var(--color-warning-light));
  }

  &.has-offline .status-icon,
  &.danger > .status-icon {
    background: linear-gradient(135deg, var(--color-danger), var(--color-danger-light));
  }
}

.status-icon {
  width: 56px;
  height: 56px;
  border-radius: var(--border-radius-base);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  color: #fff;

  &.blue {
    background: linear-gradient(135deg, var(--color-primary), var(--color-primary-light));
  }

  &.green {
    background: linear-gradient(135deg, var(--color-success), var(--color-success-light));
  }

  &.orange {
    background: linear-gradient(135deg, var(--color-warning), var(--color-warning-light));
  }

  &.red {
    background: linear-gradient(135deg, var(--color-danger), var(--color-danger-light));
  }
}

.status-info {
  flex: 1;

  .status-value {
    font-size: 24px;
    font-weight: var(--font-weight-bold);
    color: var(--color-title);
    line-height: 1.2;
  }

  .status-label {
    font-size: var(--font-size-sm);
    color: var(--color-body);
    margin: var(--spacing-xs) 0;
  }

  .status-extra {
    font-size: var(--font-size-xs);
    color: var(--color-text-secondary);
  }
}

/* 主要内容 */
.main-content {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: var(--spacing-lg);
  margin-bottom: var(--spacing-lg);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-md);

  .section-title {
    font-size: var(--font-size-md);
    font-weight: var(--font-weight-bold);
    color: var(--color-title);
  }
}

/* 通道预览 */
.preview-section {
  background-color: var(--color-bg-card);
  border-radius: var(--border-radius-base);
  padding: var(--spacing-lg);
  box-shadow: var(--shadow-base);
}

.channels-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--spacing-md);
}

.channel-card {
  border-radius: var(--border-radius-base);
  overflow: hidden;
  cursor: pointer;
  transition: transform var(--transition-fast);

  &:hover {
    transform: scale(1.02);
  }

  &.channel-fault {
    .channel-video {
      border: 2px solid var(--color-warning);
    }
  }

  &.channel-offline {
    opacity: 0.6;
  }
}

.channel-video {
  position: relative;
  aspect-ratio: 4/3;
  background-color: var(--color-title);

  img {
    width: 100%;
    height: 100%;
    object-fit: cover;
  }

  .video-placeholder {
    width: 100%;
    height: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    color: rgba(255, 255, 255, 0.6);

    span {
      font-size: var(--font-size-xs);
      margin-top: var(--spacing-sm);
    }
  }

  .channel-overlay {
    position: absolute;
    top: var(--spacing-sm);
    left: var(--spacing-sm);
  }
}

.channel-info {
  background-color: var(--color-bg-card);
  padding: var(--spacing-sm);

  .channel-plate {
    display: block;
    font-size: var(--font-size-sm);
    font-weight: var(--font-weight-medium);
    color: var(--color-title);
  }

  .channel-time {
    font-size: var(--font-size-xs);
    color: var(--color-text-secondary);
  }
}

/* 快捷操作 */
.action-section {
  background-color: var(--color-bg-card);
  border-radius: var(--border-radius-base);
  padding: var(--spacing-lg);
  box-shadow: var(--shadow-base);
}

.action-buttons {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-md);
}

.action-btn-row {
  display: flex;
  gap: var(--spacing-md);
}

.action-btn {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 80px;
  font-size: var(--font-size-md);

  .ivu-icon {
    font-size: 24px;
    margin-bottom: var(--spacing-sm);
  }
}

.action-btn-secondary {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-sm);
}

/* 统计图表 */
.stats-section {
  background-color: var(--color-bg-card);
  border-radius: var(--border-radius-base);
  padding: var(--spacing-lg);
  box-shadow: var(--shadow-base);
}

.stats-content {
  min-height: 300px;
}

.traffic-chart {
  height: 300px;
}

.revenue-info {
  display: flex;
  gap: var(--spacing-xxl);
  padding: var(--spacing-xl) 0;

  .revenue-card {
    text-align: center;
    padding: var(--spacing-xl) var(--spacing-xxl);
    background: linear-gradient(135deg, var(--color-primary), var(--color-primary-light));
    border-radius: var(--border-radius-lg);
    color: #fff;

    .revenue-value {
      font-size: 36px;
      font-weight: var(--font-weight-bold);
    }

    .revenue-label {
      font-size: var(--font-size-sm);
      opacity: 0.9;
      margin-top: var(--spacing-sm);
    }
  }

  .revenue-breakdown {
    flex: 1;
    display: flex;
    flex-direction: column;
    justify-content: center;
    gap: var(--spacing-lg);
  }

  .breakdown-item {
    display: flex;
    align-items: center;
    gap: var(--spacing-md);

    .breakdown-dot {
      width: 12px;
      height: 12px;
      border-radius: 50%;

      &.wechat { background-color: #07C160; }
      &.alipay { background-color: #1677FF; }
      &.cash { background-color: #FF9500; }
      &.member { background-color: #722ED1; }
    }

    .breakdown-label {
      flex: 1;
      color: var(--color-body);
    }

    .breakdown-value {
      font-weight: var(--font-weight-medium);
      color: var(--color-title);
    }
  }
}
</style>