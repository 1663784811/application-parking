<template>
  <div class="device-list-page">
    <div class="filter-bar">
      <div class="filter-row">
        <div class="filter-item">
          <Select v-model="state.searchForm.type" placeholder="设备类型" class="filter-select" clearable>
            <Option v-for="o in DEVICE_TYPE_OPTIONS" :key="o.value" :value="o.value">{{ o.label }}</Option>
          </Select>
        </div>
        <div class="filter-item">
          <Select v-model="state.searchForm.onlineStatus" placeholder="在线状态" class="filter-select" clearable>
            <Option :value="1">在线</Option>
            <Option :value="0">离线</Option>
          </Select>
        </div>
        <div class="filter-item">
          <Input v-model="state.searchForm.keyword" placeholder="设备名称/编号" class="filter-input" clearable
                 @on-enter="handleSearch"/>
        </div>
        <div class="filter-item">
          <Button type="primary" @click="handleSearch">查询</Button>
        </div>
        <div class="filter-item">
          <Button @click="handleReset">重置</Button>
        </div>
        <div class="filter-item">
          <Button type="primary" @click="handleAdd">
            <Icon type="ios-add"/>
            添加设备
          </Button>
        </div>
      </div>
    </div>
    <div class="stats-row">
      <div class="stat-item total"><span class="stat-label">设备总数</span><span class="stat-value">{{
          state.stats.total
        }}</span></div>
      <div class="stat-item online"><span class="stat-label">在线</span><span class="stat-value">{{
          state.stats.online
        }}</span></div>
      <div class="stat-item offline"><span class="stat-label">离线</span><span class="stat-value">{{
          state.stats.offline
        }}</span></div>
      <div class="stat-item fault"><span class="stat-label">故障</span><span class="stat-value">{{
          state.stats.fault
        }}</span></div>
    </div>
    <div class="table-container">
      <TableColumnSetting :columns="columns" v-model:visible="visibleFields" v-model:open="colSettingVisible"
                          @reset="resetColumns"/>
      <Table :columns="displayColumns" :data="state.tableData" :loading="state.loading">
        <template #pid="{ row }">{{ state.deviceMap[String(row.pid)] || '-' }}</template>
        <template #type="{ row }"><span class="device-type">{{ optLabel(DEVICE_TYPE_OPTIONS, row.type) }}</span>
        </template>
        <template #deviceType="{ row }">{{ optLabel(DEVICE_NODE_OPTIONS, row.deviceType) }}</template>
        <template #connectType="{ row }">{{ optLabel(CONNECT_TYPE_OPTIONS, row.connectType) }}</template>
        <template #onlineStatus="{ row }">
          <Badge :status="row.onlineStatus === 1 ? 'success' : 'error'"
                 :text="row.onlineStatus === 1 ? '在线' : '离线'"/>
        </template>
        <template #workStatus="{ row }">
          <Badge :status="workStatusBadge(row.workStatus)" :text="optLabel(WORK_STATUS_OPTIONS, row.workStatus)"/>
        </template>
        <template #status="{ row }">
          <Badge :status="row.status === 1 ? 'success' : 'error'" :text="row.status === 1 ? '启用' : '禁用'"/>
        </template>
        <template #action="{ row }">
          <Button type="text" size="small" @click="handleEdit(row)">编辑</Button>
          <Button type="text" size="small" @click="handleChangePassword(row)">修改密码</Button>
          <Button type="text" size="small" @click="handleDelete(row)" class="text-danger">删除</Button>
        </template>
      </Table>
      <div class="pagination-wrapper">
        <Page :total="state.pagination.total" :current="state.pagination.current" :page-size="state.pagination.pageSize"
              show-total show-elevator @on-change="handlePageChange"/>
      </div>
    </div>

    <Modal v-model="state.modalVisible" :title="state.modalType === 'add' ? '添加设备' : '编辑设备'" width="1000">
      <Form :model="state.formData" :rules="state.rules" :label-width="100" class="modal-form-2col">
        <FormItem label="设备编号" prop="code"><Input v-model="state.formData.code" placeholder="如：DVC001"/></FormItem>
        <FormItem label="设备名称" prop="name"><Input v-model="state.formData.name" placeholder="请输入设备名称"/>
        </FormItem>
        <FormItem label="设备类型" prop="type">
          <Select v-model="state.formData.type">
            <Option v-for="o in DEVICE_TYPE_OPTIONS" :key="o.value" :value="o.value">{{ o.label }}</Option>
          </Select>
        </FormItem>
        <FormItem label="节点类型" prop="deviceType">
          <Select v-model="state.formData.deviceType">
            <Option v-for="o in DEVICE_NODE_OPTIONS" :key="o.value" :value="o.value">{{ o.label }}</Option>
          </Select>
        </FormItem>
        <FormItem label="父级设备" prop="pid">
          <Select v-model="state.formData.pid" filterable clearable placeholder="子设备时选择其父级网关">
            <Option v-for="item in state.deviceList.filter(d => String(d.id) !== String(state.formData.id))"
                    :key="item.id" :value="item.id">{{ item.name }}（{{ item.code }}）
            </Option>
          </Select>
        </FormItem>
        <FormItem label="设备型号" prop="model"><Input v-model="state.formData.model" placeholder="如：CAM-X200"/>
        </FormItem>
        <FormItem label="序列号" prop="serialNo"><Input v-model="state.formData.serialNo" placeholder="设备序列号"/>
        </FormItem>
        <FormItem label="MAC地址" prop="macAddress"><Input v-model="state.formData.macAddress"
                                                           placeholder="如：00:1A:2B:3C:4D:5E"/></FormItem>
        <FormItem label="IP地址" prop="ipAddress"><Input v-model="state.formData.ipAddress"
                                                         placeholder="如：192.168.1.101"/></FormItem>
        <FormItem label="固件版本" prop="firmwareVersion"><Input v-model="state.formData.firmwareVersion"
                                                                 placeholder="如：v1.0.3"/></FormItem>
        <FormItem label="连接方式" prop="connectType">
          <Select v-model="state.formData.connectType">
            <Option v-for="o in CONNECT_TYPE_OPTIONS" :key="o.value" :value="o.value">{{ o.label }}</Option>
          </Select>
        </FormItem>
        <FormItem label="所属停车场" prop="businessId">
          <Select v-model="state.formData.businessId" filterable>
            <Option v-for="item in state.parkingList" :key="item.id" :value="item.id">{{ item.name }}</Option>
          </Select>
        </FormItem>
        <FormItem label="安装位置" prop="location"><Input v-model="state.formData.location" placeholder="如：1号入口"/>
        </FormItem>
        <FormItem label="位置类型" prop="locationType">
          <Select v-model="state.formData.locationType">
            <Option v-for="o in LOCATION_TYPE_OPTIONS" :key="o.value" :value="o.value">{{ o.label }}</Option>
          </Select>
        </FormItem>
        <FormItem label="工作状态" prop="workStatus">
          <Select v-model="state.formData.workStatus">
            <Option v-for="o in WORK_STATUS_OPTIONS" :key="o.value" :value="o.value">{{ o.label }}</Option>
          </Select>
        </FormItem>
        <FormItem label="启用状态" prop="status">
          <Select v-model="state.formData.status">
            <Option v-for="o in STATUS_OPTIONS" :key="o.value" :value="o.value">{{ o.label }}</Option>
          </Select>
        </FormItem>
        <FormItem label="设备描述" prop="description"><Input v-model="state.formData.description" type="textarea"
                                                             :rows="2" placeholder="设备描述"/></FormItem>
      </Form>
      <template #footer>
        <Button @click="state.modalVisible = false">取消</Button>
        <Button type="primary" @click="handleSubmit">确定</Button>
      </template>
    </Modal>

    <Modal v-model="state.pwdModalVisible" title="修改设备密码" width="420">
      <Form :model="state.pwdForm" :label-width="100">
        <FormItem label="设备名称">{{ state.pwdForm.deviceName }}</FormItem>
        <FormItem label="账号"><Input v-model="state.pwdForm.account" placeholder="请输入账号"/></FormItem>
        <FormItem label="新密码"><Input v-model="state.pwdForm.password" type="password" placeholder="请输入新密码"/>
        </FormItem>
        <FormItem label="确认密码"><Input v-model="state.pwdForm.confirm" type="password"
                                          placeholder="请再次输入新密码"/></FormItem>
      </Form>
      <template #footer>
        <Button @click="state.pwdModalVisible = false">取消</Button>
        <Button type="primary" @click="handlePwdSubmit">确定</Button>
      </template>
    </Modal>
  </div>
</template>

<script setup>
import {onMounted, reactive} from 'vue'
import {Badge, Button, Form, FormItem, Icon, Input, Message, Modal, Option, Page, Select, Table} from 'view-ui-plus'
import {deviceApi, parkingApi} from '@/api'
import TableColumnSetting from '@/components/TableColumnSetting.vue'
import {useTableColumns} from '@/composables/useTableColumns'

// ===== 选项常量（对齐 IotDevice 实体 / IotDeviceTypeEnum） =====
// 设备类型：light/switch/airConditioner/camera/gate/gateway（来自 IotDeviceTypeEnum）
const DEVICE_TYPE_OPTIONS = [
  {value: 'light', label: '灯'},
  {value: 'switch', label: '开关'},
  {value: 'airConditioner', label: '空调'},
  {value: 'camera', label: '摄像头'},
  {value: 'gate', label: '道闸'},
  {value: 'gateway', label: '网关'}
]
// 节点类型（设备拓扑：直连/网关/子设备）
const DEVICE_NODE_OPTIONS = [
  {value: 'connect', label: '直连'},
  {value: 'gateway', label: '网关'},
  {value: 'subDevice', label: '子设备'}
]
// 连接方式（实体 connectType 为 varchar，值 '1'..'5'）
const CONNECT_TYPE_OPTIONS = [
  {value: '1', label: 'WiFi'},
  {value: '2', label: '蓝牙'},
  {value: '3', label: '有线'},
  {value: '4', label: '4G/5G'},
  {value: '5', label: '其他'}
]
// 工作状态（int：0停用/1正常/2故障/3维护中）
const WORK_STATUS_OPTIONS = [
  {value: 0, label: '停用'},
  {value: 1, label: '正常'},
  {value: 2, label: '故障'},
  {value: 3, label: '维护中'}
]
// 位置类型（int：1入口/2出口）
const LOCATION_TYPE_OPTIONS = [
  {value: 1, label: '入口'},
  {value: 2, label: '出口'}
]
// 启用状态（int：0禁用/1启用）
const STATUS_OPTIONS = [
  {value: 0, label: '禁用'},
  {value: 1, label: '启用'}
]

// 通用：按值查选项 label（表格 slot 与回显用）；空值返回空串
const optLabel = (options, val) => {
  if (val === null || val === undefined || val === '') return ''
  const found = options.find(item => item.value === val)
  return found ? found.label : val
}
// 工作状态 → Badge status 颜色
const workStatusBadge = (s) => ({0: 'default', 1: 'success', 2: 'error', 3: 'warning'}[s] || 'default')

// 表单默认值（新增用；编辑时由行数据覆盖）
const defaultFormData = () => ({
  id: null,
  code: '',
  name: '',
  type: 'light',
  deviceType: 'connect',
  pid: null,
  model: '',
  serialNo: '',
  macAddress: '',
  ipAddress: '',
  firmwareVersion: '',
  connectType: '1',
  businessId: null,
  location: '',
  locationType: 1,
  workStatus: 1,
  status: 1,
  description: ''
})

const state = reactive({
  searchForm: {type: null, onlineStatus: null, keyword: ''},
  stats: {total: 0, online: 0, offline: 0, fault: 0},
  tableData: [],
  loading: false,
  pagination: {total: 0, current: 1, pageSize: 10},
  parkingList: [],
  parkingMap: {},
  deviceList: [],
  deviceMap: {},
  modalVisible: false,
  modalType: 'add',
  formData: defaultFormData(),
  rules: {
    code: [{required: true, message: '请输入设备编号', trigger: 'blur'}],
    name: [{required: true, message: '请输入设备名称', trigger: 'blur'}],
    type: [{required: true, message: '请选择设备类型', trigger: 'change'}],
    businessId: [{required: true, message: '请选择所属停车场', trigger: 'change'}]
  },
  pwdModalVisible: false,
  pwdForm: {id: null, deviceName: '', account: '', password: '', confirm: ''}
})

const columns = [
  {field: 'code', title: '设备编号', key: 'code', minWidth: 120},
  {field: 'name', title: '设备名称', key: 'name', minWidth: 150},
  {field: 'pid', title: '父级设备', slot: 'pid', minWidth: 150},
  {field: 'type', title: '设备类型', slot: 'type', minWidth: 100},
  {field: 'deviceType', title: '节点类型', slot: 'deviceType', minWidth: 100},
  {field: 'model', title: '设备型号', key: 'model', minWidth: 110},
  {field: 'parkingName', title: '所属停车场', key: 'parkingName', minWidth: 150},
  {field: 'location', title: '安装位置', key: 'location', minWidth: 120},
  {field: 'ipAddress', title: 'IP地址', key: 'ipAddress', minWidth: 140},
  {field: 'connectType', title: '连接方式', slot: 'connectType', minWidth: 100},
  {field: 'onlineStatus', title: '在线状态', slot: 'onlineStatus', minWidth: 100, align: 'center'},
  {field: 'workStatus', title: '工作状态', slot: 'workStatus', minWidth: 100, align: 'center'},
  {field: 'status', title: '启用状态', slot: 'status', minWidth: 100, align: 'center'},
  {field: 'lastOnlineTime', title: '最后在线', key: 'lastOnlineTime', minWidth: 160},
  {title: '操作', slot: 'action', minWidth: 200, fixed: 'right'}
]

const {
  visibleFields,
  colSettingVisible,
  displayColumns,
  resetColumns
} = useTableColumns(columns, 'deviceList:columnVisible')

// 停车场列表（下拉选择 + 名称映射用）
const loadParkingList = async () => {
  try {
    const res = await parkingApi.getParkingList({size: 1000})
    const list = res.data || []
    state.parkingList = list
    const map = {}
    list.forEach(p => {
      map[String(p.id)] = p.name
    })
    state.parkingMap = map
  } catch (e) {
    console.error('获取停车场列表失败', e)
  }
}

// 全部设备（父级选择器选项 + 名称映射用）
const loadDeviceList = async () => {
  try {
    const res = await deviceApi.getDeviceList({page: 1, size: 1000})
    const list = res.data || []
    state.deviceList = list
    const map = {}
    list.forEach(d => {
      map[String(d.id)] = d.name
    })
    state.deviceMap = map
  } catch (e) {
    console.error('获取设备列表失败', e)
  }
}

// 设备数量统计
const loadStats = async () => {
  try {
    const res = await deviceApi.getDeviceStats()
    state.stats = {total: 0, online: 0, offline: 0, fault: 0, ...(res.data || {})}
  } catch (e) {
    console.error('获取设备统计失败', e)
  }
}

// 设备列表（分页，parkingName 由前端按 businessId 映射）
const initData = async () => {
  state.loading = true
  try {
    const res = await deviceApi.getDeviceList({
      page: state.pagination.current,
      size: state.pagination.pageSize,
      type: state.searchForm.type,
      onlineStatus: state.searchForm.onlineStatus,
      keyword: state.searchForm.keyword
    })
    state.tableData = (res.data || []).map(r => ({
      ...r,
      parkingName: state.parkingMap[String(r.businessId)] || ''
    }))
    state.pagination.total = (res.result && res.result.total) || 0
  } catch (e) {
    console.error('获取设备列表失败', e)
  } finally {
    state.loading = false
  }
}

const handleSearch = () => {
  state.pagination.current = 1;
  initData()
}
const handleReset = () => {
  state.searchForm = {type: null, onlineStatus: null, keyword: ''};
  handleSearch()
}

const handleAdd = () => {
  state.modalType = 'add'
  state.formData = defaultFormData()
  state.modalVisible = true
}

const handleEdit = (r) => {
  state.modalType = 'edit'
  // 先填默认值再覆盖行数据，保证所有实体字段都有绑定（行数据缺字段时不报错）
  state.formData = {...defaultFormData(), ...r}
  state.modalVisible = true
}

const handleSubmit = async () => {
  if (!state.formData.code) {
    Message.warning('请输入设备编号');
    return
  }
  if (!state.formData.name) {
    Message.warning('请输入设备名称');
    return
  }
  if (!state.formData.type) {
    Message.warning('请选择设备类型');
    return
  }
  if (!state.formData.businessId) {
    Message.warning('请选择所属停车场');
    return
  }
  try {
    await (state.modalType === 'add' ? deviceApi.addDevice : deviceApi.editDevice)({...state.formData})
    Message.success(state.modalType === 'add' ? '添加成功' : '编辑成功')
    state.modalVisible = false
    loadStats()
    loadDeviceList()
    initData()
  } catch (e) {
    console.error('保存设备失败', e)
  }
}

const handleChangePassword = (r) => {
  state.pwdForm = {id: r.id, deviceName: r.name, account: r.username || '', password: '', confirm: ''}
  state.pwdModalVisible = true
}

const handlePwdSubmit = async () => {
  if (!state.pwdForm.account) {
    Message.warning('请输入账号');
    return
  }
  if (!state.pwdForm.password) {
    Message.warning('请输入新密码');
    return
  }
  if (state.pwdForm.password !== state.pwdForm.confirm) {
    Message.warning('两次输入的密码不一致');
    return
  }
  try {
    await deviceApi.changePassword(state.pwdForm.id, state.pwdForm.account, state.pwdForm.password)
    Message.success('密码修改成功')
    state.pwdModalVisible = false
  } catch (e) {
    console.error('修改密码失败', e)
    Message.error('修改密码失败')
  }
}

const handleDelete = (r) => {
  Modal.confirm({
    title: '确认删除',
    content: `删除设备"${r.name}"？`,
    onOk: async () => {
      try {
        await deviceApi.deleteDevice(r.id)
        Message.success('删除成功')
        loadStats()
        initData()
      } catch (e) {
        console.error('删除失败', e)
      }
    }
  })
}

const handlePageChange = (p) => {
  state.pagination.current = p;
  initData()
}

onMounted(() => {
  loadParkingList()
  loadDeviceList()
  loadStats()
  initData()
})
</script>

<style lang="less" scoped>
.device-list-page {
  .filter-bar {
    padding: var(--spacing-xl);
    background: var(--bg-color);
    border-radius: var(--border-radius-base);
    margin-bottom: var(--spacing-lg);
    box-shadow: var(--shadow-base);
  }

  .filter-row {
    display: flex;
    flex-wrap: wrap;
    gap: var(--spacing-md);
    margin-bottom: var(--spacing-md);

    .filter-item {
      flex-shrink: 0;
    }

    .filter-select {
      width: 150px;
    }

    .filter-input {
      width: 200px;
    }
  }

  .stats-row {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: var(--spacing-lg);
    margin-bottom: var(--spacing-lg);

    .stat-item {
      padding: var(--spacing-xl);
      background: var(--bg-color);
      border-radius: var(--border-radius-base);
      box-shadow: var(--shadow-base);
      display: flex;
      flex-direction: column;

      .stat-label {
        font-size: var(--font-size-sm);
        color: var(--text-color-secondary);
        margin-bottom: var(--spacing-xs);
      }

      .stat-value {
        font-size: 24px;
        font-weight: 600;
        color: var(--text-color-title);
      }

      &.online .stat-value {
        color: var(--success-color);
      }

      &.offline .stat-value {
        color: var(--text-color-secondary);
      }

      &.fault .stat-value {
        color: var(--error-color);
      }
    }
  }

  .table-container {
    background: var(--bg-color);
    border-radius: var(--border-radius-base);
    padding: var(--spacing-xl);
    box-shadow: var(--shadow-base);

    .device-type {
      padding: 2px 8px;
      background: rgba(22, 93, 255, 0.1);
      color: #165DFF;
      border-radius: var(--border-radius-sm);
      font-size: var(--font-size-xs);
    }

    .text-danger {
      color: var(--error-color);
    }

    .text-warning {
      color: var(--warning-color);
    }

    .pagination-wrapper {
      display: flex;
      justify-content: flex-end;
      margin-top: var(--spacing-xl);
    }
  }
}
</style>
