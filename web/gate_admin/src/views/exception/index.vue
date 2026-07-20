<template>
  <div class="exception-page">
    <!-- 筛选区 -->
    <div class="filter-bar">
      <div class="filter-row">
        <Select v-model="filterForm.type" placeholder="异常类型" style="width: 140px" clearable>
          <Option value="unpaid">欠费车辆</Option>
          <Option value="blacklist">黑名单</Option>
          <Option value="noPlate">无牌车辆</Option>
          <Option value="overtime">超时滞留</Option>
        </Select>
        <Select v-model="filterForm.channel" placeholder="通道" style="width: 120px" clearable>
          <Option v-for="ch in systemStore.channels" :key="ch.id" :value="ch.id">
            {{ ch.name }}
          </Option>
        </Select>
        <DatePicker
          v-model="filterForm.dateRange"
          type="daterange"
          placeholder="时间范围"
          style="width: 260px"
        />
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

    <!-- 异常车辆列表 -->
    <div class="table-section">
      <Table :columns="columns" :data="tableData" :loading="loading" :height="400">
        <template #plate="{ row }">
          <span class="plate-text" :class="row.type">
            {{ row.plate }}
          </span>
        </template>
        <template #type="{ row }">
          <Tag :color="getTypeTagColor(row.type)">
            {{ getTypeText(row.type) }}
          </Tag>
        </template>
        <template #status="{ row }">
          <Tag :color="row.status === 'pending' ? 'warning' : 'success'">
            {{ row.status === 'pending' ? '待处理' : '已处理' }}
          </Tag>
        </template>
        <template #action="{ row }">
          <Button type="primary" size="small" @click="handleProcess(row)">
            处理
          </Button>
          <Button type="text" size="small" @click="handleDetail(row)">
            详情
          </Button>
        </template>
      </Table>

      <div class="pagination-wrapper">
        <Page :total="pagination.total" :current="pagination.current" :page-size="pagination.pageSize" show-total />
      </div>
    </div>

    <!-- 处理弹窗 -->
    <Modal
      v-model="processModal.visible"
      :title="`异常处理 - ${processModal.data?.plate || ''}`"
      width="500"
      @on-cancel="processModal.visible = false"
    >
      <div class="process-content" v-if="processModal.data">
        <!-- 欠费车辆处理 -->
        <div v-if="processModal.data.type === 'unpaid'" class="process-form">
          <Form :label-width="100">
            <FormItem label="欠费金额">
              <span class="unpaid-amount">¥{{ processModal.data.unpaidAmount }}</span>
            </FormItem>
            <FormItem label="滞留时长">
              <span>{{ processModal.data.duration || '2小时' }}</span>
            </FormItem>
            <FormItem label="处理方式">
              <RadioGroup v-model="processModal.handleType">
                <Radio label="collect">现场补缴</Radio>
                <Radio label="release">欠费放行</Radio>
                <Radio label="defer">延期补缴</Radio>
              </RadioGroup>
            </FormItem>
            <FormItem label="补缴金额" v-if="processModal.handleType === 'collect'">
              <InputNumber
                v-model="processModal.collectAmount"
                :min="0"
                :precision="2"
                style="width: 100%"
              />
            </FormItem>
            <FormItem label="支付方式" v-if="processModal.handleType === 'collect'">
              <RadioGroup v-model="processModal.payMethod">
                <Radio label="wechat">微信</Radio>
                <Radio label="alipay">支付宝</Radio>
                <Radio label="cash">现金</Radio>
              </RadioGroup>
            </FormItem>
            <FormItem label="截止时间" v-if="processModal.handleType === 'defer'">
              <DatePicker
                v-model="processModal.deferDate"
                type="datetime"
                format="yyyy-MM-dd HH:mm"
                placeholder="选择截止时间"
                style="width: 100%"
              />
            </FormItem>
            <FormItem label="备注">
              <Input v-model="processModal.remark" type="textarea" :rows="2" placeholder="输入处理备注" />
            </FormItem>
          </Form>
        </div>

        <!-- 黑名单处理 -->
        <div v-else-if="processModal.data.type === 'blacklist'" class="process-form">
          <Form :label-width="100">
            <FormItem label="加黑原因">
              {{ processModal.data.blacklistReason || '多次逃费' }}
            </FormItem>
            <FormItem label="加黑时间">
              {{ processModal.data.blacklistTime || '2024-01-10' }}
            </FormItem>
            <FormItem label="处理方式">
              <RadioGroup v-model="processModal.handleType">
                <Radio label="temp">临时放行</Radio>
                <Radio label="remove" v-if="userStore.isAdmin">移出黑名单</Radio>
              </RadioGroup>
            </FormItem>
            <FormItem label="放行原因" v-if="processModal.handleType === 'temp'">
              <Input v-model="processModal.remark" placeholder="输入放行原因" />
            </FormItem>
            <FormItem label="有效期至" v-if="processModal.handleType === 'temp'">
              <DatePicker
                v-model="processModal.expireDate"
                type="datetime"
                format="yyyy-MM-dd HH:mm"
                style="width: 100%"
              />
            </FormItem>
          </Form>
        </div>

        <!-- 无牌车辆处理 -->
        <div v-else-if="processModal.data.type === 'noPlate' || !['unpaid', 'blacklist', 'overtime'].includes(processModal.data.type)" class="process-form">
          <Alert type="info" show-icon>
            请前往【车辆放行管理】页面，开通临时权限后放行车辆。
          </Alert>
          <Form :label-width="100" style="margin-top: var(--spacing-lg)">
            <FormItem label="临时编号">
              <Input v-model="processModal.tempCode" placeholder="输入临时编号" search @on-search="handleGenerateCode" />
            </FormItem>
          </Form>
        </div>

        <!-- 超时滞留处理 -->
        <div v-if="processModal.data.type === 'overtime'" class="process-form">
          <Form :label-width="100">
            <FormItem label="滞留时长">
              <span class="text-warning">{{ processModal.data.overtimeDuration || '48小时' }}</span>
            </FormItem>
            <FormItem label="停车位置">
              {{ processModal.data.parkingSpace || 'A区-025' }}
            </FormItem>
            <FormItem label="处理方式">
              <RadioGroup v-model="processModal.handleType">
                <Radio label="remind">提醒车主</Radio>
                <Radio label="force">强制放行</Radio>
              </RadioGroup>
            </FormItem>
            <FormItem label="备注">
              <Input v-model="processModal.remark" type="textarea" :rows="2" placeholder="输入处理备注" />
            </FormItem>
          </Form>
        </div>
      </div>

      <template #footer>
        <Button @click="processModal.visible = false">取消</Button>
        <Button type="primary" @click="handleSubmitProcess" :loading="processModal.loading">
          确认处理
        </Button>
      </template>
    </Modal>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Button, Icon, Select, Option, DatePicker, Table, Page, Modal, Tag, Form, FormItem, Input, InputNumber, Radio, RadioGroup, Alert, Message } from 'view-ui-plus'
import { useSystemStore } from '@/stores/system'
import { useUserStore } from '@/stores/user'

const systemStore = useSystemStore()
const userStore = useUserStore()

// 筛选表单
const filterForm = reactive({
  type: '',
  channel: null,
  dateRange: []
})

// 状态
const loading = ref(false)
const pagination = reactive({
  total: 0,
  current: 1,
  pageSize: 10
})

// 表格列
const columns = [
  { title: '车牌', slot: 'plate', minWidth: 120 },
  { title: '异常类型', slot: 'type', minWidth: 100 },
  { title: '发生时间', key: 'time', minWidth: 160 },
  { title: '通道', key: 'channel', minWidth: 100 },
  { title: '滞留时长', key: 'duration', minWidth: 100 },
  { title: '欠费金额', key: 'unpaidAmount', minWidth: 100, align: 'right' },
  { title: '状态', slot: 'status', minWidth: 100 },
  { title: '操作', slot: 'action', minWidth: 120, fixed: 'right' }
]

// 表格数据
const tableData = ref([])

// 处理弹窗
const processModal = reactive({
  visible: false,
  loading: false,
  data: null,
  handleType: 'collect',
  collectAmount: 0,
  payMethod: 'wechat',
  deferDate: '',
  expireDate: '',
  remark: '',
  tempCode: ''
})

// 方法
const getTypeTagColor = (type) => {
  const map = {
    unpaid: 'warning',
    blacklist: 'error',
    noPlate: 'default',
    overtime: 'purple'
  }
  return map[type] || 'default'
}

const getTypeText = (type) => {
  const map = {
    unpaid: '欠费',
    blacklist: '黑名单',
    noPlate: '无牌',
    overtime: '超时'
  }
  return map[type] || type
}

const handleSearch = () => {
  pagination.current = 1
  initData()
}

const handleReset = () => {
  filterForm.type = ''
  filterForm.channel = null
  filterForm.dateRange = []
  handleSearch()
}

const handleProcess = (row) => {
  processModal.data = { ...row }
  processModal.handleType = row.type === 'unpaid' ? 'collect' :
                            row.type === 'blacklist' ? 'temp' :
                            row.type === 'overtime' ? 'remind' : ''
  processModal.collectAmount = row.unpaidAmount || 0
  processModal.visible = true
}

const handleDetail = (row) => {
  Modal.info({
    title: '异常详情',
    content: `
      <p>车牌：${row.plate}</p>
      <p>类型：${getTypeText(row.type)}</p>
      <p>时间：${row.time}</p>
      <p>通道：${row.channel}</p>
      <p>欠费金额：¥${row.unpaidAmount || 0}</p>
    `
  })
}

const handleGenerateCode = () => {
  processModal.tempCode = 'TMP' + Date.now().toString().slice(-8)
}

const handleSubmitProcess = () => {
  processModal.loading = true

  setTimeout(() => {
    processModal.loading = false
    processModal.visible = false
    Message.success('处理成功')

    // 更新数据
    const item = tableData.value.find(t => t.id === processModal.data.id)
    if (item) {
      item.status = 'handled'
    }
  }, 500)
}

// 初始化数据
const initData = () => {
  loading.value = true

  setTimeout(() => {
    tableData.value = [
      { id: 1, plate: '京A12345', type: 'unpaid', time: '2024-01-15 14:30:00', channel: '入口1', duration: '2小时', unpaidAmount: 20, status: 'pending' },
      { id: 2, plate: '浙C88888', type: 'blacklist', time: '2024-01-15 14:20:00', channel: '出口1', duration: '-', unpaidAmount: 50, blacklistReason: '多次逃费', status: 'pending' },
      { id: 3, plate: '无牌-A1', type: 'noPlate', time: '2024-01-15 14:10:00', channel: '入口2', duration: '-', unpaidAmount: 0, status: 'pending' },
      { id: 4, plate: '京B67890', type: 'overtime', time: '2024-01-15 10:00:00', channel: '地下', duration: '48小时', unpaidAmount: 180, status: 'pending' },
      { id: 5, plate: '京C11111', type: 'unpaid', time: '2024-01-15 09:30:00', channel: '出口2', duration: '1小时', unpaidAmount: 15, status: 'handled' }
    ]
    pagination.total = tableData.value.length
    loading.value = false
  }, 300)
}

// 生命周期
onMounted(() => {
  initData()
})
</script>

<style lang="less" scoped>
.exception-page {
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

.table-section {
  background-color: var(--color-bg-card);
  border-radius: var(--border-radius-base);
  padding: var(--spacing-lg);
  box-shadow: var(--shadow-base);
}

.plate-text {
  font-weight: var(--font-weight-medium);
  color: var(--color-title);

  &.unpaid { color: var(--color-warning); }
  &.blacklist { color: var(--color-danger); }
  &.noPlate { color: var(--color-text-secondary); }
  &.overtime { color: #722ED1; }
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: var(--spacing-md);
}

.process-content {
  padding: var(--spacing-md) 0;
}

.process-form {
  :deep(.ivu-form-item) {
    margin-bottom: var(--spacing-md);
  }
}

.unpaid-amount {
  font-size: 20px;
  font-weight: var(--font-weight-bold);
  color: var(--color-danger);
}
</style>