<template>
  <div class="release-page">
    <div class="release-content">
      <!-- 手动放行模块 -->
      <div class="release-card manual-release">
        <div class="card-header">
          <h3>手动放行</h3>
        </div>
        <div class="card-body">
          <Form :label-width="100">
            <FormItem label="车牌号码">
              <Input
                v-model="releaseForm.plate"
                placeholder="输入或扫描车牌"
                size="large"
                search
                @on-search="handleQueryPlate"
              />
            </FormItem>
            <FormItem label="查询结果">
              <div class="query-result" v-if="releaseForm.queryResult">
                <Tag :color="getVehicleTagColor(releaseForm.queryResult.status)">
                  {{ getStatusText(releaseForm.queryResult.status) }}
                </Tag>
                <span class="result-info">
                  {{ releaseForm.queryResult.type }} · {{ releaseForm.queryResult.owner || '临时车' }}
                </span>
                <span class="result-unpaid" v-if="releaseForm.queryResult.unpaidAmount > 0">
                  欠费 ¥{{ releaseForm.queryResult.unpaidAmount }}
                </span>
              </div>
              <span v-else class="text-secondary">输入车牌后点击查询</span>
            </FormItem>
            <FormItem label="放行类型">
              <Select v-model="releaseForm.releaseType" size="large">
                <Option value="normal">正常放行</Option>
                <Option value="unpaid">欠费放行</Option>
                <Option value="blacklist">黑名单临时放行</Option>
                <Option value="internal">内部车辆放行</Option>
              </Select>
            </FormItem>
            <FormItem label="放行通道" v-if="releaseForm.releaseType === 'unpaid' || releaseForm.releaseType === 'blacklist'">
              <Select v-model="releaseForm.channel" size="large" placeholder="选择放行通道">
                <Option v-for="ch in systemStore.channels" :key="ch.id" :value="ch.id">
                  {{ ch.name }}
                </Option>
              </Select>
            </FormItem>
            <FormItem label="欠费金额" v-if="releaseForm.releaseType === 'unpaid'">
              <InputNumber
                v-model="releaseForm.unpaidAmount"
                :min="0"
                :precision="2"
                size="large"
                style="width: 100%"
              />
            </FormItem>
            <FormItem label="放行原因">
              <Input
                v-model="releaseForm.remark"
                type="textarea"
                :rows="2"
                placeholder="输入放行备注"
              />
            </FormItem>
            <FormItem>
              <Button type="primary" size="large" long @click="handleRelease">
                <Icon type="ios-car-outline" />
                确认放行
              </Button>
            </FormItem>
          </Form>
        </div>
      </div>

      <!-- 临时权限开通模块 -->
      <div class="release-card temp-permission">
        <div class="card-header">
          <h3>临时权限开通</h3>
        </div>
        <div class="card-body">
          <Form :label-width="100">
            <FormItem label="临时编号">
              <Input
                v-model="tempForm.code"
                placeholder="输入或自动生成"
                size="large"
              >
                <template #append>
                  <Button @click="generateTempCode">自动生成</Button>
                </template>
              </Input>
            </FormItem>
            <FormItem label="通行类型">
              <RadioGroup v-model="tempForm.type">
                <Radio label="single">单次通行</Radio>
                <Radio label="limited">限时通行</Radio>
              </RadioGroup>
            </FormItem>
            <FormItem label="通行时长" v-if="tempForm.type === 'limited'">
              <TimePicker
                v-model="tempForm.duration"
                format="HH:mm"
                placeholder="选择通行时长"
                style="width: 100%"
              />
            </FormItem>
            <FormItem label="有效期至" v-if="tempForm.type === 'limited'">
              <DatePicker
                v-model="tempForm.expireDate"
                type="datetime"
                format="yyyy-MM-dd HH:mm"
                placeholder="选择有效期"
                style="width: 100%"
              />
            </FormItem>
            <FormItem label="适用通道">
              <Select v-model="tempForm.channels" multiple placeholder="选择通道">
                <Option v-for="ch in systemStore.channels" :key="ch.id" :value="ch.id">
                  {{ ch.name }}
                </Option>
              </Select>
            </FormItem>
            <FormItem label="备注">
              <Input
                v-model="tempForm.remark"
                type="textarea"
                :rows="2"
                placeholder="如：XX公司来访车辆"
              />
            </FormItem>
            <FormItem>
              <Button type="primary" size="large" long @click="handleCreatePermission">
                <Icon type="ios-key-outline" />
                开通权限
              </Button>
            </FormItem>
          </Form>
        </div>

        <!-- 权限列表 -->
        <div class="permission-list">
          <div class="list-header">
            <span>已开通的临时权限</span>
            <span class="list-count">共 {{ permissionList.length }} 条</span>
          </div>
          <div class="list-body">
            <div
              v-for="item in permissionList"
              :key="item.id"
              class="permission-item"
            >
              <div class="permission-info">
                <div class="permission-code">{{ item.code }}</div>
                <div class="permission-detail">
                  <Tag :color="item.type === 'single' ? 'blue' : 'purple'" size="small">
                    {{ item.type === 'single' ? '单次' : '限时' }}
                  </Tag>
                  <span class="permission-expire">至 {{ item.expireTime }}</span>
                </div>
              </div>
              <div class="permission-channels">
                {{ item.channels.join('、') }}
              </div>
              <div class="permission-status">
                <Tag :color="item.status === 'active' ? 'success' : 'default'">
                  {{ item.status === 'active' ? '有效' : '已过期' }}
                </Tag>
              </div>
              <div class="permission-action">
                <Button type="text" size="small" @click="handleRevokePermission(item)">
                  撤销
                </Button>
              </div>
            </div>
            <div v-if="permissionList.length === 0" class="empty-list">
              暂无临时权限
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 放行记录列表 -->
    <div class="record-section">
      <div class="section-header">
        <h3>放行记录</h3>
        <div class="filter-actions">
          <Select v-model="recordFilter.type" placeholder="操作类型" style="width: 140px" clearable>
            <Option value="manual">手动放行</Option>
            <Option value="temp">临时权限</Option>
          </Select>
          <Button @click="handleExportRecord">
            <Icon type="ios-download-outline" />
            导出
          </Button>
        </div>
      </div>

      <Table :columns="recordColumns" :data="recordList" :loading="loading">
        <template #type="{ row }">
          <Tag :color="row.type === 'manual' ? 'blue' : 'purple'">
            {{ row.type === 'manual' ? '手动放行' : '临时权限' }}
          </Tag>
        </template>
        <template #status="{ row }">
          <Tag :color="row.success ? 'success' : 'error'">
            {{ row.success ? '成功' : '失败' }}
          </Tag>
        </template>
      </Table>

      <div class="pagination-wrapper">
        <Page :total="pagination.total" :current="pagination.current" :page-size="pagination.pageSize" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { Form, FormItem, Input, InputNumber, Select, Option, Button, Icon, Tag, DatePicker, TimePicker, Radio, RadioGroup, Table, Page, Modal, Message } from 'view-ui-plus'
import { useSystemStore } from '@/stores/system'
import { useUserStore } from '@/stores/user'

const systemStore = useSystemStore()
const userStore = useUserStore()

// 状态
const loading = ref(false)
const releaseForm = reactive({
  plate: '',
  queryResult: null,
  releaseType: 'normal',
  channel: null,
  unpaidAmount: 0,
  remark: ''
})

const tempForm = reactive({
  code: '',
  type: 'single',
  duration: '',
  expireDate: '',
  channels: [],
  remark: ''
})

const recordFilter = reactive({
  type: ''
})

const pagination = reactive({
  total: 0,
  current: 1,
  pageSize: 10
})

// 临时权限列表
const permissionList = ref([])

// 放行记录
const recordList = ref([])

// 表格列
const recordColumns = [
  { title: '操作时间', key: 'time', minWidth: 160 },
  { title: '类型', slot: 'type', minWidth: 100 },
  { title: '车牌/编号', key: 'plate', minWidth: 120 },
  { title: '放行原因', key: 'reason', minWidth: 150 },
  { title: '操作人', key: 'operator', minWidth: 100 },
  { title: '状态', slot: 'status', minWidth: 80 }
]

// 方法
const getVehicleTagColor = (status) => {
  const map = {
    normal: 'success',
    unpaid: 'warning',
    blacklist: 'error'
  }
  return map[status] || 'default'
}

const getStatusText = (status) => {
  const map = {
    normal: '正常',
    unpaid: '欠费车辆',
    blacklist: '黑名单'
  }
  return map[status] || status
}

const handleQueryPlate = () => {
  if (!releaseForm.plate) {
    Message.warning('请输入车牌')
    return
  }
  // 模拟查询
  releaseForm.queryResult = {
    status: 'normal',
    type: '临时车',
    unpaidAmount: 0
  }
  Message.info('查询完成')
}

const handleRelease = () => {
  if (!releaseForm.plate) {
    Message.warning('请输入车牌')
    return
  }

  Modal.confirm({
    title: '确认放行',
    content: `确认对车辆 ${releaseForm.plate} 执行放行操作？`,
    okText: '确认放行',
    onOk: () => {
      Message.success('放行成功')
      // 记录
      recordList.value.unshift({
        id: Date.now(),
        time: new Date().toLocaleString('zh-CN'),
        type: 'manual',
        plate: releaseForm.plate,
        reason: releaseForm.remark || '正常放行',
        operator: userStore.userInfo.name,
        success: true
      })
      // 重置表单
      releaseForm.plate = ''
      releaseForm.queryResult = null
      releaseForm.remark = ''
    }
  })
}

const generateTempCode = () => {
  tempForm.code = 'TMP' + Date.now().toString().slice(-8)
}

const handleCreatePermission = () => {
  if (!tempForm.code) {
    Message.warning('请输入或生成临时编号')
    return
  }

  const expireTime = tempForm.type === 'limited' && tempForm.expireDate
    ? new Date(tempForm.expireDate).toLocaleString('zh-CN')
    : '永久'

  permissionList.value.unshift({
    id: Date.now(),
    code: tempForm.code,
    type: tempForm.type,
    expireTime: expireTime,
    channels: tempForm.channels.length
      ? tempForm.channels.map(id => systemStore.channels.find(c => c.id === id)?.name || id)
      : ['全部门'],
    status: 'active'
  })

  Message.success('权限开通成功')

  // 记录
  recordList.value.unshift({
    id: Date.now(),
    time: new Date().toLocaleString('zh-CN'),
    type: 'temp',
    plate: tempForm.code,
    reason: '临时权限开通',
    operator: userStore.userInfo.name,
    success: true
  })

  // 重置
  tempForm.code = ''
  tempForm.channels = []
  tempForm.remark = ''
}

const handleRevokePermission = (item) => {
  Modal.confirm({
    title: '确认撤销',
    content: `确定要撤销权限 "${item.code}" 吗？`,
    onOk: () => {
      item.status = 'expired'
      Message.success('权限已撤销')
    }
  })
}

const handleExportRecord = () => {
  Message.info('导出中...')
}

// 生命周期
onMounted(() => {
  // 模拟数据
  permissionList.value = [
    { id: 1, code: 'TMP12345678', type: 'limited', expireTime: '2024-01-15 18:00', channels: ['入口1', '出口1'], status: 'active' },
    { id: 2, code: 'TMP87654321', type: 'single', expireTime: '永久', channels: ['全部门'], status: 'active' }
  ]

  recordList.value = [
    { id: 1, time: '2024-01-15 14:30:25', type: 'manual', plate: '京A12345', reason: '正常放行', operator: '张三', success: true },
    { id: 2, time: '2024-01-15 14:15:00', type: 'temp', plate: 'TMP12345678', reason: '临时权限开通', operator: '张三', success: true },
    { id: 3, time: '2024-01-15 13:45:30', type: 'manual', plate: '京B67890', reason: '内部车辆', operator: '张三', success: true }
  ]

  pagination.total = recordList.value.length
})
</script>

<style lang="less" scoped>
.release-page {
  display: flex;
  flex-direction: column;
  gap: var(--spacing-lg);
}

.release-content {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--spacing-lg);
}

.release-card {
  background-color: var(--color-bg-card);
  border-radius: var(--border-radius-base);
  box-shadow: var(--shadow-base);

  .card-header {
    padding: var(--spacing-md) var(--spacing-lg);
    border-bottom: 1px solid var(--color-border-light);

    h3 {
      font-size: var(--font-size-md);
      font-weight: var(--font-weight-bold);
      color: var(--color-title);
    }
  }

  .card-body {
    padding: var(--spacing-lg);
  }
}

.query-result {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);

  .result-info {
    color: var(--color-body);
  }

  .result-unpaid {
    color: var(--color-danger);
    font-weight: var(--font-weight-medium);
  }

  .ivu-btn {
    display: inline-flex;
    align-items: center;
  }
}

/* 权限列表 */
.permission-list {
  border-top: 1px solid var(--color-border-light);
  margin-top: var(--spacing-lg);
}

.list-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--spacing-md) 0;
  font-size: var(--font-size-sm);
  font-weight: var(--font-weight-medium);
  color: var(--color-title);

  .list-count {
    color: var(--color-text-secondary);
    font-weight: normal;
  }
}

.list-body {
  max-height: 200px;
  overflow-y: auto;
}

.permission-item {
  display: flex;
  align-items: center;
  padding: var(--spacing-sm) 0;
  border-bottom: 1px solid var(--color-border-light);

  &:last-child {
    border-bottom: none;
  }
}

.permission-info {
  flex: 1;

  .permission-code {
    font-weight: var(--font-weight-medium);
    color: var(--color-title);
  }

  .permission-expire {
    font-size: var(--font-size-xs);
    color: var(--color-text-secondary);
    margin-left: var(--spacing-sm);
  }
}

.permission-channels {
  width: 100px;
  font-size: var(--font-size-xs);
  color: var(--color-text-secondary);
  text-align: center;
}

.permission-status {
  width: 70px;
  text-align: center;
}

.permission-action {
  width: 50px;
  text-align: right;
}

.empty-list {
  text-align: center;
  padding: var(--spacing-lg);
  color: var(--color-text-secondary);
}

/* 记录列表 */
.record-section {
  background-color: var(--color-bg-card);
  border-radius: var(--border-radius-base);
  padding: var(--spacing-lg);
  box-shadow: var(--shadow-base);
}

.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--spacing-md);

  h3 {
    font-size: var(--font-size-md);
    font-weight: var(--font-weight-bold);
    color: var(--color-title);
  }

  .filter-actions {
    display: flex;
    gap: var(--spacing-md);

    .ivu-btn {
      display: inline-flex;
      align-items: center;
      gap: var(--spacing-xs);
    }
  }
}

.pagination-wrapper {
  display: flex;
  justify-content: flex-end;
  margin-top: var(--spacing-md);
}
</style>