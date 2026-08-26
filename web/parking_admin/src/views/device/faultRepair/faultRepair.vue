<template>
  <div class="fault-repair-page">
    <div class="filter-bar">
      <div class="filter-row">
        <div class="filter-item">
          <DatePicker v-model="state.searchForm.dateRange" type="daterange" placeholder="上报时间" class="filter-date" />
        </div>
        <div class="filter-item">
          <Select v-model="state.searchForm.status" placeholder="处理状态" class="filter-select" clearable>
            <Option value="pending">待处理</Option><Option value="processing">处理中</Option><Option value="completed">已完成</Option>
          </Select>
        </div>
        <div class="filter-item"><Button type="primary" @click="handleSearch">查询</Button></div>
        <div class="filter-item"><Button @click="handleReset">重置</Button></div>
      </div>
    </div>
    <div class="stats-row">
      <div class="stat-item" @click="state.searchForm.status = 'pending'; handleSearch()"><div class="stat-value text-error">{{ state.stats.pending }}</div><div class="stat-label">待处理</div></div>
      <div class="stat-item" @click="state.searchForm.status = 'processing'; handleSearch()"><div class="stat-value text-warning">{{ state.stats.processing }}</div><div class="stat-label">处理中</div></div>
      <div class="stat-item" @click="state.searchForm.status = 'completed'; handleSearch()"><div class="stat-value text-success">{{ state.stats.completed }}</div><div class="stat-label">已完成</div></div>
    </div>
    <div class="table-container">
      <TableColumnSetting :columns="columns" v-model:visible="visibleFields" v-model:open="colSettingVisible" @reset="resetColumns" />
      <Table :columns="displayColumns" :data="state.tableData" :loading="state.loading">
        <template #status="{ row }"><Badge :status="getStatusBadge(row.status)" :text="getStatusText(row.status)" /></template>
        <template #action="{ row }">
          <Button type="text" size="small" @click="handleAssign(row)" v-if="row.status === 'pending'">指派</Button>
          <Button type="text" size="small" @click="handleComplete(row)" v-if="row.status === 'processing'">完成</Button>
          <Button type="text" size="small" @click="handleViewLog(row)">维修记录</Button>
        </template>
      </Table>
      <div class="pagination-wrapper"><Page :total="state.pagination.total" :current="state.pagination.current" :page-size="state.pagination.pageSize" show-total show-elevator @on-change="handlePageChange" /></div>
    </div>
    <Modal v-model="state.assignModalVisible" title="指派维修人员" width="400">
      <Form :label-width="100">
        <FormItem label="设备名称">{{ state.currentFault?.deviceName }}</FormItem>
        <FormItem label="故障类型">{{ state.currentFault?.faultType }}</FormItem>
        <FormItem label="指派给"><Select v-model="state.assignForm.repairer"><Option value="张师傅">张师傅</Option><Option value="李师傅">李师傅</Option><Option value="王师傅">王师傅</Option></Select></FormItem>
        <FormItem label="预计完成"><DatePicker v-model="state.assignForm.expectDate" type="date" style="min-width: 100%" /></FormItem>
      </Form>
      <template #footer><Button @click="state.assignModalVisible = false">取消</Button><Button type="primary" @click="handleAssignSubmit">指派</Button></template>
    </Modal>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import { Button, Table, Badge, Page, DatePicker, Select, Option, Modal, Form, FormItem, Message } from 'view-ui-plus'
import { deviceApi } from '@/api'
import TableColumnSetting from '@/components/TableColumnSetting.vue'
import { useTableColumns } from '@/composables/useTableColumns'

const state = reactive({
  searchForm: { dateRange: [], status: null },
  stats: { pending: 0, processing: 0, completed: 0 },
  tableData: [],
  loading: false,
  pagination: { total: 0, current: 1, pageSize: 10 },
  assignModalVisible: false,
  currentFault: null,
  assignForm: { repairer: null, expectDate: '' }
})

const columns = [
  { field: 'orderNo', title: '工单号', key: 'orderNo', minWidth: 140 },
  { field: 'deviceName', title: '设备名称', key: 'deviceName', minWidth: 150 },
  { field: 'deviceType', title: '设备类型', key: 'deviceType', minWidth: 100 },
  { field: 'faultType', title: '故障类型', key: 'faultType', minWidth: 120 },
  { field: 'parkingName', title: '停车场', key: 'parkingName', minWidth: 150 },
  { field: 'reportTime', title: '上报时间', key: 'reportTime', minWidth: 160 },
  { field: 'repairer', title: '处理人', key: 'repairer', minWidth: 100 },
  { field: 'status', title: '处理状态', slot: 'status', minWidth: 100, align: 'center' },
  { title: '操作', slot: 'action', minWidth: 180 }
]

const { visibleFields, colSettingVisible, displayColumns, resetColumns } = useTableColumns(columns, 'faultRepair:columnVisible')

const getStatusText = (s) => ({ pending: '待处理', processing: '处理中', completed: '已完成' }[s] || s)
const getStatusBadge = (s) => ({ pending: 'error', processing: 'warning', completed: 'success' }[s] || 'default')
// 设备类型快照（camera/gate/screen/sensor）→ 中文
const getTypeText = (t) => ({ camera: '摄像头', gate: '道闸', screen: '显示屏', sensor: '地感' }[t] || t)

// Date → 'YYYY-MM-DD'（后端拼 00:00:00 / 23:59:59）
const fmtDay = (d) => {
  if (!d) return null
  const dt = d instanceof Date ? d : new Date(d)
  const p = (n) => String(n).padStart(2, '0')
  return `${dt.getFullYear()}-${p(dt.getMonth() + 1)}-${p(dt.getDate())}`
}
// Date → 'YYYY-MM-DD 00:00:00'（后端按 yyyy-MM-dd HH:mm:ss 解析）
const fmtDateTime = (d) => {
  if (!d) return null
  const dt = d instanceof Date ? d : new Date(d)
  const p = (n) => String(n).padStart(2, '0')
  return `${dt.getFullYear()}-${p(dt.getMonth() + 1)}-${p(dt.getDate())} 00:00:00`
}

// 工单数量统计
const loadStats = async () => {
  try {
    const res = await deviceApi.getFaultStats()
    state.stats = { pending: 0, processing: 0, completed: 0, ...(res.data || {}) }
  } catch (e) {
    console.error('获取故障统计失败', e)
  }
}

// 故障工单列表（分页，dateRange → startTime/endTime）
const initData = async () => {
  state.loading = true
  try {
    const dr = state.searchForm.dateRange || []
    const res = await deviceApi.getFaultList({
      page: state.pagination.current,
      size: state.pagination.pageSize,
      status: state.searchForm.status,
      startTime: dr[0] ? fmtDay(dr[0]) : null,
      endTime: dr[1] ? fmtDay(dr[1]) : null
    })
    state.tableData = (res.data || []).map(r => ({ ...r, deviceType: getTypeText(r.deviceType) }))
    state.pagination.total = (res.result && res.result.total) || 0
  } catch (e) {
    console.error('获取故障列表失败', e)
  } finally {
    state.loading = false
  }
}

const handleSearch = () => { state.pagination.current = 1; initData() }
const handleReset = () => { state.searchForm = { dateRange: [], status: null }; handleSearch() }

const handleAssign = (r) => { state.currentFault = r; state.assignForm = { repairer: null, expectDate: '' }; state.assignModalVisible = true }

const handleAssignSubmit = async () => {
  if (!state.assignForm.repairer) { Message.warning('请选择维修人员'); return }
  if (!state.assignForm.expectDate) { Message.warning('请选择预计完成日期'); return }
  try {
    await deviceApi.handleFault({
      id: state.currentFault.id,
      action: 'assign',
      repairer: state.assignForm.repairer,
      expectCompleteTime: fmtDateTime(state.assignForm.expectDate)
    })
    Message.success('已指派')
    state.assignModalVisible = false
    loadStats()
    initData()
  } catch (e) {
    console.error('指派失败', e)
    Message.error('指派失败')
  }
}

const handleComplete = (r) => {
  Modal.confirm({
    title: '确认完成',
    content: '确认此故障已修复？',
    onOk: async () => {
      try {
        await deviceApi.handleFault({ id: r.id, action: 'complete' })
        Message.success('处理完成')
        loadStats()
        initData()
      } catch (e) {
        console.error('完成失败', e)
      }
    }
  })
}

const handleViewLog = (r) => {
  Message.info('维修记录功能开发中')
}

const handlePageChange = (p) => { state.pagination.current = p; initData() }

onMounted(() => {
  loadStats()
  initData()
})
</script>

<style lang="less" scoped>
.fault-repair-page {
  .filter-bar { padding: var(--spacing-xl); background: var(--bg-color); border-radius: var(--border-radius-base); margin-bottom: var(--spacing-lg); box-shadow: var(--shadow-base); }
  .filter-row { display: flex; flex-wrap: wrap; gap: var(--spacing-md); .filter-item { flex-shrink: 0; } .filter-date { width: 260px; } .filter-select { width: 150px; } }
  .stats-row { display: grid; grid-template-columns: repeat(3, 1fr); gap: var(--spacing-lg); margin-bottom: var(--spacing-lg); .stat-item { padding: var(--spacing-xl); background: var(--bg-color); border-radius: var(--border-radius-base); box-shadow: var(--shadow-base); display: flex; flex-direction: column; align-items: center; cursor: pointer; transition: transform 0.2s; &:hover { transform: translateY(-2px); } .stat-value { font-size: 32px; font-weight: 600; } .stat-label { font-size: var(--font-size-sm); color: var(--text-color-secondary); margin-top: var(--spacing-xs); } } }
  .table-container { background: var(--bg-color); border-radius: var(--border-radius-base); padding: var(--spacing-xl); box-shadow: var(--shadow-base); .pagination-wrapper { display: flex; justify-content: flex-end; margin-top: var(--spacing-xl); } }
}
</style>
