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
      <Table :columns="columns" :data="state.tableData" :loading="state.loading">
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
        <FormItem label="指派给"><Select v-model="state.assignForm.repairerId"><Option value="1">张师傅</Option><Option value="2">李师傅</Option><Option value="3">王师傅</Option></Select></FormItem>
        <FormItem label="预计完成"><DatePicker v-model="state.assignForm.expectDate" type="date" style="minWidth: 100%" /></FormItem>
      </Form>
      <template #footer><Button @click="state.assignModalVisible = false">取消</Button><Button type="primary" @click="handleAssignSubmit">指派</Button></template>
    </Modal>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { Button, Icon, Table, Badge, Page, DatePicker, Select, Option, Modal, Form, FormItem, Message } from 'view-ui-plus'

const state = reactive({
  searchForm: { dateRange: [], status: null },
  stats: { pending: 5, processing: 3, completed: 42 },
  tableData: [],
  loading: false,
  pagination: { total: 0, current: 1, pageSize: 10 },
  assignModalVisible: false,
  currentFault: null,
  assignForm: { repairerId: null, expectDate: '' }
})

const columns = [
  { title: '工单号', key: 'orderNo', minWidth: 140 },
  { title: '设备名称', key: 'deviceName', minWidth: 150 },
  { title: '设备类型', key: 'deviceType', minWidth: 100 },
  { title: '故障类型', key: 'faultType', minWidth: 120 },
  { title: '停车场', key: 'parkingName', minWidth: 150 },
  { title: '上报时间', key: 'reportTime', minWidth: 160 },
  { title: '处理人', key: 'repairer', minWidth: 100 },
  { title: '处理状态', slot: 'status', minWidth: 100, align: 'center' },
  { title: '操作', slot: 'action', minWidth: 180 }
]

const getStatusText = (s) => ({ pending: '待处理', processing: '处理中', completed: '已完成' }[s] || s)
const getStatusBadge = (s) => ({ pending: 'error', processing: 'warning', completed: 'success' }[s] || 'default')

const initData = () => {
  state.loading = true
  setTimeout(() => {
    state.tableData = [
      { id: 1, orderNo: 'FK20240115001', deviceName: '1号入口摄像头', deviceType: '摄像头', faultType: '画面丢失', parkingName: '城西停车场', reportTime: '2024-01-15 14:00:00', repairer: '-', status: 'pending' },
      { id: 2, orderNo: 'FK20240115002', deviceName: '3号道闸', deviceType: '道闸', faultType: '闸杆偏移', parkingName: '城西停车场', reportTime: '2024-01-15 13:00:00', repairer: '张师傅', status: 'processing' },
      { id: 3, orderNo: 'FK20240114001', deviceName: 'LED显示屏', deviceType: '显示屏', faultType: '黑屏', parkingName: '城东停车场', reportTime: '2024-01-14 16:00:00', repairer: '李师傅', status: 'completed' }
    ]
    state.pagination.total = 3
    state.loading = false
  }, 500)
}

const handleSearch = () => { state.pagination.current = 1; initData() }
const handleReset = () => { state.searchForm = { dateRange: [], status: null }; handleSearch() }
const handleAssign = (r) => { state.currentFault = r; state.assignForm = { repairerId: null, expectDate: '' }; state.assignModalVisible = true }
const handleAssignSubmit = () => { Message.success('已指派'); state.assignModalVisible = false; initData() }
const handleComplete = (r) => Modal.confirm({ title: '确认完成', content: '确认此故障已修复？', onOk: () => { Message.success('处理完成'); initData() } })
const handleViewLog = (r) => console.log('维修记录', r)
const handlePageChange = (p) => { state.pagination.current = p; initData() }

initData()
</script>

<style lang="less" scoped>
.fault-repair-page {
  .filter-bar { padding: var(--spacing-xl); background: var(--bg-color); border-radius: var(--border-radius-base); margin-bottom: var(--spacing-lg); box-shadow: var(--shadow-base); }
  .filter-row { display: flex; flex-wrap: wrap; gap: var(--spacing-md); .filter-item { flex-shrink: 0; } .filter-date { width: 260px; } .filter-select { width: 150px; } }
  .stats-row { display: grid; grid-template-columns: repeat(3, 1fr); gap: var(--spacing-lg); margin-bottom: var(--spacing-lg); .stat-item { padding: var(--spacing-xl); background: var(--bg-color); border-radius: var(--border-radius-base); box-shadow: var(--shadow-base); display: flex; flex-direction: column; align-items: center; cursor: pointer; transition: transform 0.2s; &:hover { transform: translateY(-2px); } .stat-value { font-size: 32px; font-weight: 600; } .stat-label { font-size: var(--font-size-sm); color: var(--text-color-secondary); margin-top: var(--spacing-xs); } } }
  .table-container { background: var(--bg-color); border-radius: var(--border-radius-base); padding: var(--spacing-xl); box-shadow: var(--shadow-base); .pagination-wrapper { display: flex; justify-content: flex-end; margin-top: var(--spacing-xl); } }
}
</style>