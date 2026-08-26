<template>
  <div class="device-list-page">
    <div class="filter-bar">
      <div class="filter-row">
        <div class="filter-item">
          <Select v-model="state.searchForm.type" placeholder="设备类型" class="filter-select" clearable>
            <Option value="camera">摄像头</Option><Option value="gate">道闸</Option><Option value="screen">显示屏</Option><Option value="sensor">地感</Option>
          </Select>
        </div>
        <div class="filter-item">
          <Select v-model="state.searchForm.parkingId" placeholder="停车场" class="filter-select" clearable>
            <Option v-for="item in state.parkingList" :key="item.id" :value="item.id">{{ item.name }}</Option>
          </Select>
        </div>
        <div class="filter-item">
          <Select v-model="state.searchForm.onlineStatus" placeholder="在线状态" class="filter-select" clearable>
            <Option :value="1">在线</Option><Option :value="0">离线</Option>
          </Select>
        </div>
        <div class="filter-item">
          <Input v-model="state.searchForm.keyword" placeholder="设备名称/编号" class="filter-input" clearable @on-enter="handleSearch" />
        </div>
        <div class="filter-item"><Button type="primary" @click="handleSearch">查询</Button></div>
        <div class="filter-item"><Button @click="handleReset">重置</Button></div>
        <div class="filter-item"><Button type="primary" @click="handleAdd"><Icon type="ios-add" />添加设备</Button></div>
      </div>
    </div>
    <div class="stats-row">
      <div class="stat-item total"><span class="stat-label">设备总数</span><span class="stat-value">{{ state.stats.total }}</span></div>
      <div class="stat-item online"><span class="stat-label">在线</span><span class="stat-value">{{ state.stats.online }}</span></div>
      <div class="stat-item offline"><span class="stat-label">离线</span><span class="stat-value">{{ state.stats.offline }}</span></div>
      <div class="stat-item fault"><span class="stat-label">故障</span><span class="stat-value">{{ state.stats.fault }}</span></div>
    </div>
    <div class="table-container">
      <TableColumnSetting :columns="columns" v-model:visible="visibleFields" v-model:open="colSettingVisible" @reset="resetColumns" />
      <Table :columns="displayColumns" :data="state.tableData" :loading="state.loading">
        <template #type="{ row }"><span class="device-type">{{ getTypeText(row.type) }}</span></template>
        <template #onlineStatus="{ row }"><Badge :status="row.onlineStatus === 1 ? 'success' : 'error'" :text="row.onlineStatus === 1 ? '在线' : '离线'" /></template>
        <template #action="{ row }">
          <Button type="text" size="small" @click="handleEdit(row)">编辑</Button>
          <Button type="text" size="small" @click="handleRestart(row)">重启</Button>
          <Button type="text" size="small" @click="handleReportFault(row)" class="text-warning">报修</Button>
          <Button type="text" size="small" @click="handleDelete(row)" class="text-danger">删除</Button>
        </template>
      </Table>
      <div class="pagination-wrapper"><Page :total="state.pagination.total" :current="state.pagination.current" :page-size="state.pagination.pageSize" show-total show-elevator @on-change="handlePageChange" /></div>
    </div>

    <Modal v-model="state.modalVisible" :title="state.modalType === 'add' ? '添加设备' : '编辑设备'" width="520">
      <Form :model="state.formData" :rules="state.rules" :label-width="100">
        <FormItem label="设备编号" prop="code"><Input v-model="state.formData.code" placeholder="如：DVC001" /></FormItem>
        <FormItem label="设备名称" prop="name"><Input v-model="state.formData.name" placeholder="请输入设备名称" /></FormItem>
        <FormItem label="设备类型" prop="type">
          <Select v-model="state.formData.type">
            <Option value="camera">摄像头</Option><Option value="gate">道闸</Option><Option value="screen">显示屏</Option><Option value="sensor">地感</Option>
          </Select>
        </FormItem>
        <FormItem label="所属停车场" prop="parkingId">
          <Select v-model="state.formData.parkingId" filterable>
            <Option v-for="item in state.parkingList" :key="item.id" :value="item.id">{{ item.name }}</Option>
          </Select>
        </FormItem>
        <FormItem label="安装通道" prop="channel"><Input v-model="state.formData.channel" placeholder="如：1号入口" /></FormItem>
        <FormItem label="IP地址" prop="ip"><Input v-model="state.formData.ip" placeholder="如：192.168.1.101" /></FormItem>
      </Form>
      <template #footer><Button @click="state.modalVisible = false">取消</Button><Button type="primary" @click="handleSubmit">确定</Button></template>
    </Modal>

    <Modal v-model="state.faultModalVisible" title="设备报修" width="420">
      <Form :model="state.faultForm" :label-width="100">
        <FormItem label="设备名称">{{ state.faultForm.deviceName }}</FormItem>
        <FormItem label="故障类型"><Input v-model="state.faultForm.faultType" placeholder="如：画面丢失" /></FormItem>
      </Form>
      <template #footer><Button @click="state.faultModalVisible = false">取消</Button><Button type="primary" @click="handleFaultSubmit">提交报修</Button></template>
    </Modal>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import { Button, Icon, Table, Badge, Page, Select, Option, Input, Modal, Form, FormItem, Message } from 'view-ui-plus'
import { parkingApi, deviceApi } from '@/api'
import TableColumnSetting from '@/components/TableColumnSetting.vue'
import { useTableColumns } from '@/composables/useTableColumns'

const state = reactive({
  searchForm: { type: null, parkingId: null, onlineStatus: null, keyword: '' },
  stats: { total: 0, online: 0, offline: 0, fault: 0 },
  tableData: [],
  loading: false,
  pagination: { total: 0, current: 1, pageSize: 10 },
  parkingList: [],
  parkingMap: {},
  modalVisible: false,
  modalType: 'add',
  formData: { id: null, code: '', name: '', type: 'camera', parkingId: null, channel: '', ip: '' },
  rules: {
    code: [{ required: true, message: '请输入设备编号', trigger: 'blur' }],
    name: [{ required: true, message: '请输入设备名称', trigger: 'blur' }],
    type: [{ required: true, message: '请选择设备类型', trigger: 'change' }],
    parkingId: [{ required: true, message: '请选择所属停车场', trigger: 'change' }]
  },
  faultModalVisible: false,
  faultForm: { deviceId: null, deviceName: '', faultType: '' }
})

const columns = [
  { field: 'code', title: '设备编号', key: 'code', minWidth: 120 },
  { field: 'name', title: '设备名称', key: 'name', minWidth: 150 },
  { field: 'type', title: '设备类型', slot: 'type', minWidth: 100 },
  { field: 'parkingName', title: '所属停车场', key: 'parkingName', minWidth: 150 },
  { field: 'channel', title: '安装通道', key: 'channel', minWidth: 100 },
  { field: 'ip', title: 'IP地址', key: 'ip', minWidth: 140 },
  { field: 'onlineStatus', title: '在线状态', slot: 'onlineStatus', minWidth: 100, align: 'center' },
  { field: 'lastOnline', title: '最后在线', key: 'lastOnline', minWidth: 160 },
  { title: '操作', slot: 'action', minWidth: 200, fixed: 'right' }
]

const { visibleFields, colSettingVisible, displayColumns, resetColumns } = useTableColumns(columns, 'deviceList:columnVisible')

const getTypeText = (t) => ({ camera: '摄像头', gate: '道闸', screen: '显示屏', sensor: '地感' }[t] || t)

// 停车场列表（下拉选择 + 名称映射用）
const loadParkingList = async () => {
  try {
    const res = await parkingApi.getParkingList({ size: 1000 })
    const list = res.data || []
    state.parkingList = list
    const map = {}
    list.forEach(p => { map[String(p.id)] = p.name })
    state.parkingMap = map
  } catch (e) {
    console.error('获取停车场列表失败', e)
  }
}

// 设备数量统计
const loadStats = async () => {
  try {
    const res = await deviceApi.getDeviceStats()
    state.stats = { total: 0, online: 0, offline: 0, fault: 0, ...(res.data || {}) }
  } catch (e) {
    console.error('获取设备统计失败', e)
  }
}

// 设备列表（分页，parkingName 由前端按 parkingId 映射）
const initData = async () => {
  state.loading = true
  try {
    const res = await deviceApi.getDeviceList({
      page: state.pagination.current,
      size: state.pagination.pageSize,
      type: state.searchForm.type,
      parkingId: state.searchForm.parkingId,
      onlineStatus: state.searchForm.onlineStatus,
      keyword: state.searchForm.keyword
    })
    state.tableData = (res.data || []).map(r => ({
      ...r,
      parkingName: state.parkingMap[String(r.parkingId)] || ''
    }))
    state.pagination.total = (res.result && res.result.total) || 0
  } catch (e) {
    console.error('获取设备列表失败', e)
  } finally {
    state.loading = false
  }
}

const handleSearch = () => { state.pagination.current = 1; initData() }
const handleReset = () => { state.searchForm = { type: null, parkingId: null, onlineStatus: null, keyword: '' }; handleSearch() }

const handleAdd = () => {
  state.modalType = 'add'
  state.formData = { id: null, code: '', name: '', type: 'camera', parkingId: null, channel: '', ip: '' }
  state.modalVisible = true
}

const handleEdit = (r) => {
  state.modalType = 'edit'
  state.formData = { ...r }
  state.modalVisible = true
}

const handleSubmit = async () => {
  if (!state.formData.code) { Message.warning('请输入设备编号'); return }
  if (!state.formData.name) { Message.warning('请输入设备名称'); return }
  if (!state.formData.type) { Message.warning('请选择设备类型'); return }
  if (!state.formData.parkingId) { Message.warning('请选择所属停车场'); return }
  try {
    await (state.modalType === 'add' ? deviceApi.addDevice : deviceApi.editDevice)({ ...state.formData })
    Message.success(state.modalType === 'add' ? '添加成功' : '编辑成功')
    state.modalVisible = false
    loadStats()
    initData()
  } catch (e) {
    console.error('保存设备失败', e)
  }
}

const handleRestart = (r) => {
  Modal.confirm({
    title: '确认重启',
    content: `确定重启设备"${r.name}"吗？`,
    onOk: async () => {
      try {
        await deviceApi.remoteRestart(r.id)
        Message.success('重启命令已发送')
      } catch (e) {
        console.error('重启失败', e)
      }
    }
  })
}

const handleReportFault = (r) => {
  state.faultForm = { deviceId: r.id, deviceName: r.name, faultType: '' }
  state.faultModalVisible = true
}

const handleFaultSubmit = async () => {
  if (!state.faultForm.faultType) { Message.warning('请输入故障类型'); return }
  try {
    await deviceApi.createFault({ deviceId: state.faultForm.deviceId, faultType: state.faultForm.faultType })
    Message.success('报修成功')
    state.faultModalVisible = false
    loadStats()
  } catch (e) {
    console.error('报修失败', e)
    Message.error('报修失败')
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

const handlePageChange = (p) => { state.pagination.current = p; initData() }

onMounted(() => {
  loadParkingList()
  loadStats()
  initData()
})
</script>

<style lang="less" scoped>
.device-list-page {
  .filter-bar { padding: var(--spacing-xl); background: var(--bg-color); border-radius: var(--border-radius-base); margin-bottom: var(--spacing-lg); box-shadow: var(--shadow-base); }
  .filter-row { display: flex; flex-wrap: wrap; gap: var(--spacing-md); margin-bottom: var(--spacing-md); .filter-item { flex-shrink: 0; } .filter-select { width: 150px; } .filter-input { width: 200px; } }
  .stats-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--spacing-lg); margin-bottom: var(--spacing-lg); .stat-item { padding: var(--spacing-xl); background: var(--bg-color); border-radius: var(--border-radius-base); box-shadow: var(--shadow-base); display: flex; flex-direction: column; .stat-label { font-size: var(--font-size-sm); color: var(--text-color-secondary); margin-bottom: var(--spacing-xs); } .stat-value { font-size: 24px; font-weight: 600; color: var(--text-color-title); } &.online .stat-value { color: var(--success-color); } &.offline .stat-value { color: var(--text-color-secondary); } &.fault .stat-value { color: var(--error-color); } } }
  .table-container { background: var(--bg-color); border-radius: var(--border-radius-base); padding: var(--spacing-xl); box-shadow: var(--shadow-base); .device-type { padding: 2px 8px; background: rgba(22,93,255,0.1); color: #165DFF; border-radius: var(--border-radius-sm); font-size: var(--font-size-xs); } .text-danger { color: var(--error-color); } .text-warning { color: var(--warning-color); } .pagination-wrapper { display: flex; justify-content: flex-end; margin-top: var(--spacing-xl); } }
}
</style>
