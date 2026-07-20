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
            <Option value="1">城西停车场</Option><Option value="2">城东停车场</Option>
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
      </div>
      <div class="filter-actions"><Button type="primary" @click="handleAdd"><Icon type="ios-add" />添加设备</Button></div>
    </div>
    <div class="stats-row">
      <div class="stat-item total"><span class="stat-label">设备总数</span><span class="stat-value">{{ state.stats.total }}</span></div>
      <div class="stat-item online"><span class="stat-label">在线</span><span class="stat-value">{{ state.stats.online }}</span></div>
      <div class="stat-item offline"><span class="stat-label">离线</span><span class="stat-value">{{ state.stats.offline }}</span></div>
      <div class="stat-item fault"><span class="stat-label">故障</span><span class="stat-value">{{ state.stats.fault }}</span></div>
    </div>
    <div class="table-container">
      <Table :columns="columns" :data="state.tableData" :loading="state.loading">
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
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { Button, Icon, Table, Badge, Page, Select, Option, Input, Modal, Message } from 'view-ui-plus'

const state = reactive({
  searchForm: { type: null, parkingId: null, onlineStatus: null, keyword: '' },
  stats: { total: 48, online: 42, offline: 4, fault: 2 },
  tableData: [],
  loading: false,
  pagination: { total: 0, current: 1, pageSize: 10 }
})

const columns = [
  { title: '设备编号', key: 'code', minWidth: 120 },
  { title: '设备名称', key: 'name', minWidth: 150 },
  { title: '设备类型', slot: 'type', minWidth: 100 },
  { title: '所属停车场', key: 'parkingName', minWidth: 150 },
  { title: '安装通道', key: 'channel', minWidth: 100 },
  { title: 'IP地址', key: 'ip', minWidth: 140 },
  { title: '在线状态', slot: 'onlineStatus', minWidth: 100, align: 'center' },
  { title: '最后在线', key: 'lastOnline', minWidth: 160 },
  { title: '操作', slot: 'action', minWidth: 200, fixed: 'right' }
]

const getTypeText = (t) => ({ camera: '摄像头', gate: '道闸', screen: '显示屏', sensor: '地感' }[t] || t)

const initData = () => {
  state.loading = true
  setTimeout(() => {
    state.tableData = [
      { id: 1, code: 'DVC001', name: '入口摄像头1', type: 'camera', parkingName: '城西停车场', channel: '1号入口', ip: '192.168.1.101', onlineStatus: 1, lastOnline: '2024-01-15 14:30:00' },
      { id: 2, code: 'DVC002', name: '出口摄像头1', type: 'camera', parkingName: '城西停车场', channel: '2号出口', ip: '192.168.1.102', onlineStatus: 1, lastOnline: '2024-01-15 14:29:00' },
      { id: 3, code: 'GTW001', name: '1号道闸', type: 'gate', parkingName: '城西停车场', channel: '1号入口', ip: '192.168.1.103', onlineStatus: 1, lastOnline: '2024-01-15 14:30:00' },
      { id: 4, code: 'DVC003', name: '地下摄像头', type: 'camera', parkingName: '城东停车场', channel: '地下入口', ip: '192.168.1.104', onlineStatus: 0, lastOnline: '2024-01-15 10:00:00' },
      { id: 5, code: 'SCR001', name: 'LED显示屏', type: 'screen', parkingName: '城西停车场', channel: '主通道', ip: '192.168.1.105', onlineStatus: 1, lastOnline: '2024-01-15 14:30:00' },
      { id: 6, code: 'SNS001', name: '地感A1', type: 'sensor', parkingName: '城西停车场', channel: 'A1区域', ip: '192.168.1.106', onlineStatus: 1, lastOnline: '2024-01-15 14:30:00' }
    ]
    state.pagination.total = 6
    state.loading = false
  }, 500)
}

const handleSearch = () => { state.pagination.current = 1; initData() }
const handleReset = () => { state.searchForm = { type: null, parkingId: null, onlineStatus: null, keyword: '' }; handleSearch() }
const handleAdd = () => Message.info('添加设备')
const handleEdit = (r) => console.log('编辑', r)
const handleRestart = (r) => Modal.confirm({ title: '确认重启', content: `确定重启设备"${r.name}"吗？`, onOk: () => Message.success('重启命令已发送') })
const handleReportFault = (r) => Message.success('报修成功')
const handleDelete = (r) => Modal.confirm({ title: '确认删除', content: `删除设备"${r.name}"？`, onOk: () => Message.success('删除成功') })
const handlePageChange = (p) => { state.pagination.current = p; initData() }

initData()
</script>

<style lang="less" scoped>
.device-list-page {
  .filter-bar { padding: var(--spacing-xl); background: var(--bg-color); border-radius: var(--border-radius-base); margin-bottom: var(--spacing-lg); box-shadow: var(--shadow-base); }
  .filter-row { display: flex; flex-wrap: wrap; gap: var(--spacing-md); margin-bottom: var(--spacing-md); .filter-item { flex-shrink: 0; } .filter-select { width: 150px; } .filter-input { width: 200px; } }
  .filter-actions { display: flex; gap: var(--spacing-md); }
  .stats-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--spacing-lg); margin-bottom: var(--spacing-lg); .stat-item { padding: var(--spacing-xl); background: var(--bg-color); border-radius: var(--border-radius-base); box-shadow: var(--shadow-base); display: flex; flex-direction: column; .stat-label { font-size: var(--font-size-sm); color: var(--text-color-secondary); margin-bottom: var(--spacing-xs); } .stat-value { font-size: 24px; font-weight: 600; color: var(--text-color-title); } &.online .stat-value { color: var(--success-color); } &.offline .stat-value { color: var(--text-color-secondary); } &.fault .stat-value { color: var(--error-color); } } }
  .table-container { background: var(--bg-color); border-radius: var(--border-radius-base); padding: var(--spacing-xl); box-shadow: var(--shadow-base); .device-type { padding: 2px 8px; background: rgba(22,93,255,0.1); color: #165DFF; border-radius: var(--border-radius-sm); font-size: var(--font-size-xs); } .text-danger { color: var(--error-color); } .text-warning { color: var(--warning-color); } .pagination-wrapper { display: flex; justify-content: flex-end; margin-top: var(--spacing-xl); } }
}
</style>