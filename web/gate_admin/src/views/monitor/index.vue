<template>
  <div class="monitor-page">
    <!-- 通道切换区 -->
    <div class="channel-tabs">
      <div class="tabs-left">
        <RadioGroup v-model="viewMode" type="button">
          <Radio label="single">单通道</Radio>
          <Radio label="multi">多通道</Radio>
        </RadioGroup>
      </div>
      <div class="tabs-center">
        <Button
          v-for="channel in systemStore.channels"
          :key="channel.id"
          :type="currentChannelId === channel.id ? 'primary' : 'default'"
          @click="selectChannel(channel.id)"
        >
          {{ channel.name }}
        </Button>
      </div>
      <div class="tabs-right">
        <Button icon="ios-refresh" @click="handleRefresh">刷新</Button>
      </div>
    </div>

    <!-- 主监控区 -->
    <div class="monitor-content">
      <!-- 实时监控画面 -->
      <div class="video-section">
        <div class="video-container">
          <div class="video-player">
            <img
              v-if="currentChannel?.videoUrl"
              :src="currentChannel.videoUrl"
              alt="实时监控"
            />
            <div v-else class="video-placeholder">
              <Icon type="ios-videocam-outline" size="64" />
              <p>{{ currentChannel?.name || '选择通道' }}</p>
              <p class="placeholder-hint">等待车辆通行...</p>
            </div>
          </div>

          <!-- 车辆识别信息 -->
          <div class="vehicle-info-overlay" v-if="currentVehicle">
            <div class="vehicle-plate">{{ currentVehicle.plate }}</div>
            <div class="vehicle-type">{{ currentVehicle.type }}</div>
            <div class="vehicle-status" :class="currentVehicle.status">
              {{ getStatusText(currentVehicle.status) }}
            </div>
          </div>

          <!-- 应急操作按钮 -->
          <div class="emergency-btns">
            <Button type="primary" size="large" @click="handleEmergencyOpen" :disabled="!currentChannel">
              <Icon type="ios-open-outline" />
              远程开闸
            </Button>
            <Button size="large" @click="handleForceOpen" :disabled="!currentChannel" v-if="userStore.isAdmin">
              <Icon type="ios-hand-outline" />
              强制抬杆
            </Button>
            <Button size="large" @click="handleCloseGate" :disabled="!currentChannel">
              <Icon type="ios-close-outline" />
              关闭道闸
            </Button>
          </div>
        </div>

        <!-- 通道状态 -->
        <div class="channel-status-bar">
          <div class="status-item">
            <span class="status-label">通道状态：</span>
            <Tag :color="getChannelStatusColor(currentChannel?.status)">
              {{ getChannelStatusText(currentChannel?.status) }}
            </Tag>
          </div>
          <div class="status-item">
            <span class="status-label">设备在线：</span>
            <Tag :color="currentChannel?.status === 'online' ? 'success' : 'error'">
              {{ currentChannel?.status === 'online' ? '在线' : '离线' }}
            </Tag>
          </div>
          <div class="status-item">
            <span class="status-label">今日通行：</span>
            <span>{{ todayPassCount }} 车次</span>
          </div>
        </div>
      </div>

      <!-- 车辆信息详情 -->
      <div class="detail-section">
        <Tabs v-model="detailTab">
          <TabPane label="车辆信息" name="info">
            <div class="vehicle-detail" v-if="currentVehicle">
              <div class="detail-group">
                <label>车牌号码</label>
                <div class="detail-value large">{{ currentVehicle.plate }}</div>
              </div>
              <div class="detail-group">
                <label>车辆类型</label>
                <div class="detail-value">{{ currentVehicle.type }}</div>
              </div>
              <div class="detail-group">
                <label>通行状态</label>
                <div class="detail-value">
                  <Tag :color="getVehicleTagColor(currentVehicle.status)">
                    {{ getStatusText(currentVehicle.status) }}
                  </Tag>
                </div>
              </div>
              <div class="detail-group">
                <label>车主姓名</label>
                <div class="detail-value">{{ currentVehicle.ownerName || '-' }}</div>
              </div>
              <div class="detail-group">
                <label>联系电话</label>
                <div class="detail-value">{{ currentVehicle.phone || '-' }}</div>
              </div>
              <div class="detail-group">
                <label>会员类型</label>
                <div class="detail-value">{{ currentVehicle.memberType || '-' }}</div>
              </div>
              <div class="detail-group">
                <label>欠费金额</label>
                <div class="detail-value" :class="{ 'text-danger': currentVehicle.unpaidAmount > 0 }">
                  ¥{{ currentVehicle.unpaidAmount || 0 }}
                </div>
              </div>
            </div>
            <div v-else class="empty-detail">
              <Icon type="ios-car-outline" size="48" />
              <p>暂无车辆信息</p>
            </div>
          </TabPane>

          <TabPane label="通行记录" name="record">
            <div class="passage-records">
              <div
                v-for="record in passageRecords"
                :key="record.id"
                class="record-item"
              >
                <div class="record-left">
                  <Icon :type="record.direction === 'in' ? 'ios-log-in-outline' : 'ios-log-out-outline'" />
                </div>
                <div class="record-info">
                  <div class="record-time">{{ record.time }}</div>
                  <div class="record-channel">{{ record.channel }}</div>
                </div>
                <div class="record-amount">
                  <span v-if="record.amount > 0">¥{{ record.amount }}</span>
                  <span v-else class="text-secondary">免费</span>
                </div>
              </div>
              <div v-if="passageRecords.length === 0" class="empty-records">
                暂无通行记录
              </div>
            </div>
          </TabPane>

          <TabPane label="操作记录" name="action">
            <div class="action-records">
              <div
                v-for="action in actionRecords"
                :key="action.id"
                class="action-item"
              >
                <div class="action-time">{{ action.time }}</div>
                <div class="action-content">{{ action.content }}</div>
                <div class="action-operator">{{ action.operator }}</div>
              </div>
              <div v-if="actionRecords.length === 0" class="empty-records">
                暂无操作记录
              </div>
            </div>
          </TabPane>
        </Tabs>
      </div>
    </div>

    <!-- 通行记录列表 -->
    <div class="record-list-section">
      <div class="list-header">
        <h3>今日通行记录</h3>
        <div class="list-actions">
          <Select v-model="filterStatus" placeholder="状态筛选" style="width: 120px" clearable>
            <Option value="normal">正常</Option>
            <Option value="unpaid">欠费</Option>
            <Option value="abnormal">异常</Option>
          </Select>
          <Button @click="handleExport">
            <Icon type="ios-download-outline" />
            导出
          </Button>
        </div>
      </div>

      <Table :columns="columns" :data="tableData" :loading="loading" height="200">
        <template #plate="{ row }">
          <span class="plate-cell" :class="row.status">
            {{ row.plate }}
          </span>
        </template>
        <template #status="{ row }">
          <Tag :color="getVehicleTagColor(row.status)">
            {{ getStatusText(row.status) }}
          </Tag>
        </template>
        <template #action="{ row }">
          <Button type="text" size="small" @click="handleViewDetail(row)">详情</Button>
          <Button type="text" size="small" v-if="row.status === 'unpaid'" @click="handleCollect(row)">
            补缴
          </Button>
        </template>
      </Table>

      <div class="pagination-wrapper">
        <Page :total="pagination.total" :current="pagination.current" :page-size="pagination.pageSize" show-total />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { Button, Icon, RadioGroup, Radio, Tabs, TabPane, Tag, Table, Page, Select, Option, Modal, Message } from 'view-ui-plus'
import { useSystemStore } from '@/stores/system'
import { useUserStore } from '@/stores/user'

const route = useRoute()
const systemStore = useSystemStore()
const userStore = useUserStore()

// 状态
const viewMode = ref('single')
const currentChannelId = ref(1)
const detailTab = ref('info')
const filterStatus = ref('')
const loading = ref(false)

// 分页
const pagination = reactive({
  total: 0,
  current: 1,
  pageSize: 10
})

// 当前通道
const currentChannel = computed(() => {
  return systemStore.channels.find(c => c.id === currentChannelId.value)
})

// 模拟当前车辆
const currentVehicle = ref({
  plate: '京A12345',
  type: '小型车',
  status: 'normal',
  ownerName: '张三',
  phone: '138****0001',
  memberType: '月卡会员',
  unpaidAmount: 0
})

// 今日通行数
const todayPassCount = ref(428)

// 通行记录
const passageRecords = ref([
  { id: 1, direction: 'in', time: '10:32:15', channel: '入口1', amount: 0 },
  { id: 2, direction: 'out', time: '09:45:23', channel: '出口1', amount: 15 },
  { id: 3, direction: 'in', time: '08:30:00', channel: '入口2', amount: 0 }
])

// 操作记录
const actionRecords = ref([
  { id: 1, time: '10:30:00', content: '识别成功', operator: '系统' },
  { id: 2, time: '10:30:02', content: '开闸放行', operator: '系统' }
])

// 表格列
const columns = [
  { title: '车牌', slot: 'plate', minWidth: 120 },
  { title: '通行时间', key: 'time', minWidth: 160 },
  { title: '通道', key: 'channel', minWidth: 100 },
  { title: '状态', slot: 'status', minWidth: 100 },
  { title: '操作', slot: 'action', minWidth: 120, fixed: 'right' }
]

// 表格数据
const tableData = ref([])

// 方法
const selectChannel = (id) => {
  currentChannelId.value = id
}

const getStatusText = (status) => {
  const map = {
    normal: '正常',
    unpaid: '欠费',
    abnormal: '异常',
    blacklist: '黑名单',
    noPlate: '无牌'
  }
  return map[status] || status
}

const getVehicleTagColor = (status) => {
  const map = {
    normal: 'success',
    unpaid: 'warning',
    abnormal: 'error',
    blacklist: 'error',
    noPlate: 'default'
  }
  return map[status] || 'default'
}

const getChannelStatusColor = (status) => {
  const map = {
    online: 'success',
    fault: 'warning',
    offline: 'error'
  }
  return map[status] || 'default'
}

const getChannelStatusText = (status) => {
  const map = {
    online: '正常',
    fault: '故障',
    offline: '离线'
  }
  return map[status] || '未知'
}

const handleRefresh = () => {
  Message.info('刷新监控画面')
}

const handleEmergencyOpen = () => {
  Modal.confirm({
    title: '确认远程开闸',
    content: `确定要对 ${currentChannel.value?.name} 执行远程开闸操作吗？`,
    okText: '确认开闸',
    onOk: () => {
      Message.success('开闸成功')
      actionRecords.value.unshift({
        id: Date.now(),
        time: new Date().toLocaleTimeString('zh-CN'),
        content: '远程开闸',
        operator: userStore.userInfo.name
      })
    }
  })
}

const handleForceOpen = () => {
  Modal.confirm({
    title: '确认强制抬杆',
    content: '操作需要管理员权限，确定执行吗？',
    okText: '确认',
    onOk: () => {
      Message.success('强制抬杆成功')
    }
  })
}

const handleCloseGate = () => {
  Modal.confirm({
    title: '确认关闭道闸',
    content: '确定要关闭当前通道的道闸吗？',
    okText: '确认',
    onOk: () => {
      Message.success('道闸已关闭')
    }
  })
}

const handleViewDetail = (row) => {
  console.log('查看详情', row)
}

const handleCollect = (row) => {
  Modal.confirm({
    title: '确认补缴',
    content: `确认收取车辆 ${row.plate} 停车费用？`,
    okText: '确认收款',
    onOk: () => {
      Message.success('补缴成功')
      initTableData()
    }
  })
}

const handleExport = () => {
  Message.info('导出中...')
}

// 初始化表格
const initTableData = () => {
  tableData.value = [
    { id: 1, plate: '京A12345', time: '10:45:23', channel: '入口1', status: 'normal' },
    { id: 2, plate: '京B67890', time: '10:32:15', channel: '出口1', status: 'normal' },
    { id: 3, plate: '京C11111', time: '10:20:00', channel: '入口2', status: 'unpaid' },
    { id: 4, plate: '浙D22222', time: '10:15:30', channel: '出口2', status: 'normal' },
    { id: 5, plate: '无牌车辆', time: '09:45:00', channel: '入口1', status: 'noPlate' }
  ]
  pagination.total = tableData.value.length
}

// 生命周期
onMounted(() => {
  // 检查路由参数
  if (route.query.channelId) {
    currentChannelId.value = parseInt(route.query.channelId)
  }
  initTableData()
})
</script>

<style lang="less" scoped>
.monitor-page {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

/* 通道切换区 */
.channel-tabs {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background-color: var(--color-bg-card);
  border-radius: var(--border-radius-base);
  padding: var(--spacing-md) var(--spacing-lg);
  box-shadow: var(--shadow-base);

  .tabs-center {
    display: flex;
    gap: var(--spacing-sm);
  }
}

/* 监控内容 */
.monitor-content {
  display: grid;
  grid-template-columns: 1fr 400px;
  gap: var(--spacing-lg);
}

/* 视频区域 */
.video-section {
  background-color: var(--color-bg-card);
  border-radius: var(--border-radius-base);
  overflow: hidden;
  box-shadow: var(--shadow-base);
}

.video-container {
  position: relative;
  aspect-ratio: 16/9;
  background-color: #000;
}

.video-player {
  width: 100%;
  height: 100%;

  img {
    width: 100%;
    height: 100%;
    object-fit: contain;
  }
}

.video-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: rgba(255, 255, 255, 0.5);

  p {
    margin-top: var(--spacing-md);
  }

  .placeholder-hint {
    font-size: var(--font-size-xs);
    opacity: 0.6;
  }
}

.vehicle-info-overlay {
  position: absolute;
  top: var(--spacing-lg);
  left: var(--spacing-lg);
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-sm) var(--spacing-lg);
  background-color: rgba(0, 0, 0, 0.7);
  border-radius: var(--border-radius-base);

  .vehicle-plate {
    font-size: 20px;
    font-weight: var(--font-weight-bold);
    color: #fff;
  }

  .vehicle-type {
    font-size: var(--font-size-sm);
    color: rgba(255, 255, 255, 0.8);
  }

  .vehicle-status {
    padding: 2px 8px;
    border-radius: var(--border-radius-sm);
    font-size: var(--font-size-xs);
    background-color: var(--color-success);
    color: #fff;

    &.unpaid { background-color: var(--color-warning); }
    &.abnormal { background-color: var(--color-danger); }
  }
}

.emergency-btns {
  position: absolute;
  bottom: var(--spacing-lg);
  right: var(--spacing-lg);
  display: flex;
  gap: var(--spacing-md);

  .ivu-btn {
    display: flex;
    align-items: center;
    gap: var(--spacing-sm);
    padding: var(--spacing-sm) var(--spacing-lg);
    min-height: 48px;
  }
}

.channel-status-bar {
  display: flex;
  align-items: center;
  gap: var(--spacing-xxl);
  padding: var(--spacing-md) var(--spacing-lg);
  background-color: var(--color-bg);

  .status-item {
    display: flex;
    align-items: center;
    gap: var(--spacing-sm);

    .status-label {
      color: var(--color-text-secondary);
    }
  }
}

/* 详情区域 */
.detail-section {
  background-color: var(--color-bg-card);
  border-radius: var(--border-radius-base);
  padding: var(--spacing-lg);
  box-shadow: var(--shadow-base);
}

.vehicle-detail {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: var(--spacing-md);
}

.detail-group {
  label {
    display: block;
    font-size: var(--font-size-xs);
    color: var(--color-text-secondary);
    margin-bottom: var(--spacing-xs);
  }

  .detail-value {
    font-size: var(--font-size-sm);
    color: var(--color-title);

    &.large {
      font-size: 20px;
      font-weight: var(--font-weight-bold);
    }
  }
}

.empty-detail {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 200px;
  color: var(--color-text-secondary);
  gap: var(--spacing-md);
}

/* 通行记录 */
.passage-records, .action-records {
  max-height: 300px;
  overflow-y: auto;
}

.record-item, .action-item {
  display: flex;
  align-items: center;
  padding: var(--spacing-sm) 0;
  border-bottom: 1px solid var(--color-border-light);

  &:last-child {
    border-bottom: none;
  }
}

.record-left {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background-color: var(--color-primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-right: var(--spacing-md);
}

.record-info {
  flex: 1;

  .record-time {
    font-size: var(--font-size-sm);
    color: var(--color-title);
  }

  .record-channel {
    font-size: var(--font-size-xs);
    color: var(--color-text-secondary);
  }
}

.record-amount {
  font-weight: var(--font-weight-medium);
  color: var(--color-title);
}

.empty-records {
  text-align: center;
  padding: var(--spacing-xl);
  color: var(--color-text-secondary);
}

/* 通行记录列表 */
.record-list-section {
  background-color: var(--color-bg-card);
  border-radius: var(--border-radius-base);
  padding: var(--spacing-lg);
  box-shadow: var(--shadow-base);

  .list-header {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: var(--spacing-md);

    h3 {
      font-size: var(--font-size-md);
      font-weight: var(--font-weight-bold);
      color: var(--color-title);
    }

    .list-actions {
      display: flex;
      gap: var(--spacing-md);

      .ivu-btn {
        display: inline-flex;
        align-items: center;
        gap: var(--spacing-xs);
      }
    }
  }

  .plate-cell {
    font-weight: var(--font-weight-medium);
    color: var(--color-title);

    &.unpaid { color: var(--color-warning); }
    &.abnormal, &.blacklist { color: var(--color-danger); }
  }

  .pagination-wrapper {
    display: flex;
    justify-content: flex-end;
    margin-top: var(--spacing-md);
  }
}
</style>