<template>
  <div class="equipment-page">
    <!-- 筛选区 -->
    <div class="filter-bar">
      <div class="filter-row">
        <Select v-model="filterForm.type" placeholder="设备类型" style="width: 120px" clearable>
          <Option value="gate">道闸</Option>
          <Option value="camera">摄像头</Option>
          <Option value="sensor">地感</Option>
          <Option value="display">显示屏</Option>
        </Select>
        <Select v-model="filterForm.channel" placeholder="通道" style="width: 120px" clearable>
          <Option v-for="ch in systemStore.channels" :key="ch.id" :value="ch.id">
            {{ ch.name }}
          </Option>
        </Select>
        <Select v-model="filterForm.status" placeholder="在线状态" style="width: 100px" clearable>
          <Option value="online">在线</Option>
          <Option value="offline">离线</Option>
          <Option value="fault">故障</Option>
        </Select>
        <Button type="primary" @click="handleSearch">
          <Icon type="ios-search" />
          查询
        </Button>
        <Button @click="handleReset">
          <Icon type="ios-refresh" />
          重置
        </Button>
      </div>
    </div>

    <div class="content-grid">
      <!-- 设备列表 -->
      <div class="equipment-list">
        <div class="list-header">
          <h3>设备状态</h3>
          <Tag :color="getStatusTagColor(onlineCount, offlineCount, faultCount)">
            在线 {{ onlineCount }} / 离线 {{ offlineCount }} / 故障 {{ faultCount }}
          </Tag>
        </div>

        <Table :columns="columns" :data="tableData" :loading="loading" :height="400" @on-row-click="handleRowClick">
          <template #status="{ row }">
            <span class="status-dot" :class="row.status"></span>
            <Tag :color="getDeviceStatusColor(row.status)" size="small">
              {{ getDeviceStatusText(row.status) }}
            </Tag>
          </template>
          <template #action="{ row }">
            <Button type="text" size="small" @click.stop="handleRestart(row)" :disabled="row.status === 'offline'">
              重启
            </Button>
            <Button type="text" size="small" @click.stop="handleRepair(row)">
              报修
            </Button>
            <Button type="text" size="small" @click.stop="handleDetail(row)">
              详情
            </Button>
          </template>
        </Table>
      </div>

      <!-- 报修模块 -->
      <div class="repair-panel">
        <div class="panel-header">
          <h3>故障报修</h3>
        </div>

        <div class="panel-body">
          <Alert v-if="!repairForm.deviceId" type="info" show-icon>
            请从左侧选择需要报修的设备
          </Alert>

          <Form v-else :label-width="90">
            <FormItem label="设备编号">
              <span class="repair-value">{{ repairForm.deviceId }}</span>
            </FormItem>
            <FormItem label="设备名称">
              <span class="repair-value">{{ repairForm.deviceName }}</span>
            </FormItem>
            <FormItem label="设备类型">
              <span class="repair-value">{{ getDeviceTypeText(repairForm.deviceType) }}</span>
            </FormItem>
            <FormItem label="安装通道">
              <span class="repair-value">{{ repairForm.channel }}</span>
            </FormItem>
            <FormItem label="故障状态">
              <Tag :color="getDeviceStatusColor(repairForm.status)">
                {{ getDeviceStatusText(repairForm.status) }}
              </Tag>
            </FormItem>
            <FormItem label="故障描述">
              <Input v-model="repairForm.description" type="textarea" :rows="3" placeholder="请描述故障情况" />
            </FormItem>
            <FormItem label="发生时间">
              <DatePicker
                v-model="repairForm.faultTime"
                type="datetime"
                format="yyyy-MM-dd HH:mm"
                placeholder="选择故障发生时间"
                style="width: 100%"
              />
            </FormItem>
            <FormItem label="报修方式">
              <RadioGroup v-model="repairForm.method">
                <Radio label="phone">电话报修</Radio>
                <Radio label="system">系统提交</Radio>
              </RadioGroup>
            </FormItem>
            <FormItem>
              <Button type="primary" long @click="handleSubmitRepair" :loading="repairLoading">
                提交报修
              </Button>
            </FormItem>
          </Form>
        </div>

        <!-- 报修工单 -->
        <div class="repair-orders" v-if="repairOrders.length > 0">
          <div class="orders-header">报修工单</div>
          <div class="orders-list">
            <div v-for="order in repairOrders" :key="order.id" class="order-item">
              <div class="order-info">
                <span class="order-no">{{ order.no }}</span>
                <Tag :color="getOrderStatusColor(order.status)" size="small">
                  {{ getOrderStatusText(order.status) }}
                </Tag>
              </div>
              <div class="order-device">{{ order.deviceName }}</div>
              <div class="order-time">{{ order.time }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 设备详情弹窗 -->
    <Modal v-model="detailModal.visible" title="设备详情" width="600">
      <div class="detail-content" v-if="detailModal.data">
        <Row :gutter="16">
          <Col span="12">
            <div class="detail-item">
              <label>设备编号</label>
              <span>{{ detailModal.data.id }}</span>
            </div>
          </Col>
          <Col span="12">
            <div class="detail-item">
              <label>设备名称</label>
              <span>{{ detailModal.data.name }}</span>
            </div>
          </Col>
          <Col span="12">
            <div class="detail-item">
              <label>设备类型</label>
              <span>{{ getDeviceTypeText(detailModal.data.type) }}</span>
            </div>
          </Col>
          <Col span="12">
            <div class="detail-item">
              <label>IP地址</label>
              <span>{{ detailModal.data.ip }}</span>
            </div>
          </Col>
          <Col span="12">
            <div class="detail-item">
              <label>安装通道</label>
              <span>{{ detailModal.data.channel }}</span>
            </div>
          </Col>
          <Col span="12">
            <div class="detail-item">
              <label>最后检测</label>
              <span>{{ detailModal.data.lastCheck }}</span>
            </div>
          </Col>
        </Row>

        <div class="detail-params" v-if="detailModal.data.params">
          <h4>运行参数</h4>
          <div class="params-grid">
            <div v-for="(value, key) in detailModal.data.params" :key="key" class="param-item">
              <span class="param-label">{{ key }}</span>
              <span class="param-value">{{ value }}</span>
            </div>
          </div>
        </div>
      </div>
    </Modal>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { Button, Icon, Select, Option, Table, Tag, Form, FormItem, Input, DatePicker, Radio, RadioGroup, Alert, Modal, Row, Col, Message } from 'view-ui-plus'
import { useSystemStore } from '@/stores/system'

const systemStore = useSystemStore()

// 筛选表单
const filterForm = reactive({
  type: '',
  channel: null,
  status: ''
})

// 状态
const loading = ref(false)
const repairLoading = ref(false)

// 表格列
const columns = [
  { title: '设备编号', key: 'id', minWidth: 100 },
  { title: '设备名称', key: 'name', minWidth: 120 },
  { title: '类型', key: 'typeText', minWidth: 80 },
  { title: '通道', key: 'channel', minWidth: 80 },
  { title: '状态', slot: 'status', minWidth: 100 },
  { title: '操作', slot: 'action', minWidth: 160 }
]

// 表格数据
const tableData = ref([])

// 报修表单
const repairForm = reactive({
  deviceId: '',
  deviceName: '',
  deviceType: '',
  channel: '',
  status: '',
  description: '',
  faultTime: new Date(),
  method: 'system'
})

// 报修工单
const repairOrders = ref([])

// 详情弹窗
const detailModal = reactive({
  visible: false,
  data: null
})

// 计算属性
const onlineCount = computed(() => tableData.value.filter(d => d.status === 'online').length)
const offlineCount = computed(() => tableData.value.filter(d => d.status === 'offline').length)
const faultCount = computed(() => tableData.value.filter(d => d.status === 'fault').length)

// 方法
const getStatusTagColor = (online, offline, fault) => {
  if (fault > 0) return 'error'
  if (offline > 0) return 'warning'
  return 'success'
}

const getDeviceStatusColor = (status) => {
  const map = { online: 'success', offline: 'error', fault: 'warning' }
  return map[status] || 'default'
}

const getDeviceStatusText = (status) => {
  const map = { online: '在线', offline: '离线', fault: '故障' }
  return map[status] || status
}

const getDeviceTypeText = (type) => {
  const map = { gate: '道闸', camera: '摄像头', sensor: '地感', display: '显示屏' }
  return map[type] || type
}

const getOrderStatusColor = (status) => {
  const map = { pending: 'warning', processing: 'blue', completed: 'success' }
  return map[status] || 'default'
}

const getOrderStatusText = (status) => {
  const map = { pending: '待处理', processing: '处理中', completed: '已完成' }
  return map[status] || status
}

const handleSearch = () => initData()
const handleReset = () => {
  filterForm.type = ''
  filterForm.channel = null
  filterForm.status = ''
  initData()
}

const handleRowClick = (row) => {
  repairForm.deviceId = row.id
  repairForm.deviceName = row.name
  repairForm.deviceType = row.type
  repairForm.channel = row.channel
  repairForm.status = row.status
  repairForm.description = ''
  repairForm.faultTime = new Date()
}

const handleRestart = (row) => {
  Modal.confirm({
    title: '确认重启',
    content: `确定要远程重启设备 "${row.name}" 吗？`,
    okText: '确认重启',
    onOk: () => {
      Message.success('重启指令已发送')
    }
  })
}

const handleRepair = (row) => {
  handleRowClick(row)
}

const handleDetail = (row) => {
  detailModal.data = {
    ...row,
    ip: '192.168.1.' + (100 + parseInt(row.id)),
    lastCheck: new Date().toLocaleString('zh-CN'),
    params: row.type === 'gate' ? {
      '开闸次数': '12,345',
      '运行时间': '2,580小时',
      '故障次数': '3次'
    } : row.type === 'camera' ? {
      '识别率': '98.5%',
      '今日识别': '428次',
      '识别失败': '6次'
    } : null
  }
  detailModal.visible = true
}

const handleSubmitRepair = () => {
  if (!repairForm.description) {
    Message.warning('请输入故障描述')
    return
  }

  repairLoading.value = true

  setTimeout(() => {
    repairLoading.value = false
    repairOrders.value.unshift({
      id: Date.now(),
      no: 'WO' + Date.now().toString().slice(-8),
      deviceName: repairForm.deviceName,
      status: 'pending',
      time: new Date().toLocaleString('zh-CN')
    })
    Message.success('报修工单已提交')
    // 重置
    Object.keys(repairForm).forEach(key => {
      if (key !== 'method') repairForm[key] = ''
    })
  }, 500)
}

// 初始化数据
const initData = () => {
  loading.value = true

  setTimeout(() => {
    tableData.value = [
      { id: 'D001', name: '入口1道闸', type: 'gate', typeText: '道闸', channel: '入口1', status: 'online' },
      { id: 'D002', name: '入口1摄像头', type: 'camera', typeText: '摄像头', channel: '入口1', status: 'online' },
      { id: 'D003', name: '入口1地感', type: 'sensor', typeText: '地感', channel: '入口1', status: 'online' },
      { id: 'D004', name: '入口1显示屏', type: 'display', typeText: '显示屏', channel: '入口1', status: 'online' },
      { id: 'D005', name: '出口1道闸', type: 'gate', typeText: '道闸', channel: '出口1', status: 'online' },
      { id: 'D006', name: '出口1摄像头', type: 'camera', typeText: '摄像头', channel: '出口1', status: 'online' },
      { id: 'D007', name: '出口2道闸', type: 'gate', typeText: '道闸', channel: '出口2', status: 'fault' },
      { id: 'D008', name: '出口2摄像头', type: 'camera', typeText: '摄像头', channel: '出口2', status: 'offline' }
    ]
    loading.value = false
  }, 300)
}

// 生命周期
onMounted(() => {
  initData()
})
</script>

<style lang="less" scoped>
.equipment-page {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.filter-bar {
  background-color: var(--color-bg-card);
  border-radius: var(--border-radius-base);
  padding: var(--spacing-lg);
  box-shadow: var(--shadow-base);

  .filter-row {
    display: flex;
    gap: var(--spacing-md);
    flex-wrap: wrap;
  }
}

.content-grid {
  display: grid;
  grid-template-columns: 1fr 360px;
  gap: var(--spacing-lg);
}

.equipment-list {
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
  }
}

.status-dot {
  display: inline-block;
  width: 8px;
  height: 8px;
  border-radius: 50%;
  margin-right: var(--spacing-sm);

  &.online { background-color: var(--color-success); }
  &.offline { background-color: var(--color-danger); }
  &.fault { background-color: var(--color-warning); }
}

/* 报修面板 */
.repair-panel {
  background-color: var(--color-bg-card);
  border-radius: var(--border-radius-base);
  box-shadow: var(--shadow-base);

  .panel-header {
    padding: var(--spacing-md) var(--spacing-lg);
    border-bottom: 1px solid var(--color-border-light);

    h3 {
      font-size: var(--font-size-md);
      font-weight: var(--font-weight-bold);
      color: var(--color-title);
    }
  }

  .panel-body {
    padding: var(--spacing-lg);
  }

  :deep(.ivu-form-item) {
    margin-bottom: var(--spacing-md);
  }
}

.repair-value {
  color: var(--color-title);
  font-weight: var(--font-weight-medium);
}

/* 报修工单 */
.repair-orders {
  border-top: 1px solid var(--color-border-light);
  margin-top: var(--spacing-lg);
  padding-top: var(--spacing-lg);

  .orders-header {
    font-size: var(--font-size-sm);
    font-weight: var(--font-weight-medium);
    color: var(--color-title);
    margin-bottom: var(--spacing-sm);
  }

  .orders-list {
    max-height: 150px;
    overflow-y: auto;
  }

  .order-item {
    padding: var(--spacing-sm) 0;
    border-bottom: 1px solid var(--color-border-light);

    &:last-child {
      border-bottom: none;
    }
  }

  .order-info {
    display: flex;
    align-items: center;
    gap: var(--spacing-sm);

    .order-no {
      font-weight: var(--font-weight-medium);
      font-size: var(--font-size-sm);
    }
  }

  .order-device {
    font-size: var(--font-size-xs);
    color: var(--color-body);
    margin-top: var(--spacing-xs);
  }

  .order-time {
    font-size: var(--font-size-xs);
    color: var(--color-text-secondary);
    margin-top: var(--spacing-xs);
  }
}

/* 详情弹窗 */
.detail-content {
  :deep(.ivu-col) {
    margin-bottom: var(--spacing-md);
  }

  .detail-item {
    label {
      display: block;
      font-size: var(--font-size-xs);
      color: var(--color-text-secondary);
      margin-bottom: var(--spacing-xs);
    }

    span {
      font-size: var(--font-size-sm);
      color: var(--color-title);
    }
  }

  .detail-params {
    margin-top: var(--spacing-lg);
    padding-top: var(--spacing-lg);
    border-top: 1px solid var(--color-border-light);

    h4 {
      font-size: var(--font-size-sm);
      font-weight: var(--font-weight-medium);
      color: var(--color-title);
      margin-bottom: var(--spacing-md);
    }

    .params-grid {
      display: grid;
      grid-template-columns: repeat(3, 1fr);
      gap: var(--spacing-md);
    }

    .param-item {
      background-color: var(--color-bg);
      padding: var(--spacing-md);
      border-radius: var(--border-radius-base);

      .param-label {
        display: block;
        font-size: var(--font-size-xs);
        color: var(--color-text-secondary);
      }

      .param-value {
        font-size: var(--font-size-md);
        font-weight: var(--font-weight-medium);
        color: var(--color-primary);
      }
    }
  }
}
</style>