<template>
  <div class="invoice-manage-page">
    <div class="filter-bar">
      <div class="filter-row">
        <Input v-model="state.searchForm.keyword" placeholder="搜索发票抬头/税号" class="filter-input" clearable @on-enter="handleSearch" />
        <DatePicker v-model="state.searchForm.dateRange" type="daterange" placeholder="开票日期" class="filter-date" />
        <Select v-model="state.searchForm.status" placeholder="开票状态" class="filter-select" clearable>
          <Option value="pending">待开票</Option>
          <Option value="processing">开票中</Option>
          <Option value="completed">已完成</Option>
          <Option value="cancelled">已取消</Option>
        </Select>
        <Button type="primary" @click="handleSearch">查询</Button>
        <Button @click="handleReset">重置</Button>
      </div>
      <div class="filter-actions">
        <Button @click="handleBatchRed" :disabled="state.selectedRows.length === 0">
          <Icon type="ios-undo" />
          批量冲红
        </Button>
      </div>
    </div>

    <div class="stats-row">
      <div class="stat-item">
        <span class="stat-label">待处理</span>
        <span class="stat-value">{{ state.stats.pending }}</span>
      </div>
      <div class="stat-item">
        <span class="stat-label">开票中</span>
        <span class="stat-value">{{ state.stats.processing }}</span>
      </div>
      <div class="stat-item">
        <span class="stat-label">已完成</span>
        <span class="stat-value">{{ state.stats.completed }}</span>
      </div>
      <div class="stat-item">
        <span class="stat-label">已冲红</span>
        <span class="stat-value">{{ state.stats.cancelled }}</span>
      </div>
    </div>

    <div class="table-container">
      <Table :columns="columns" :data="state.tableData" :loading="state.loading" :selection="true" @on-selection-change="handleSelectionChange">
        <template #status="{ row }">
          <Badge :status="getStatusBadge(row.status)" :text="getStatusText(row.status)" />
        </template>
        <template #action="{ row }">
          <Button type="text" size="small" @click="handleView(row)">详情</Button>
          <Button type="text" size="small" @click="handleRed(row)" v-if="row.status === 'completed'" class="text-danger">冲红</Button>
        </template>
      </Table>
      <div class="pagination-wrapper">
        <Page :total="state.pagination.total" :current="state.pagination.current" :page-size="state.pagination.pageSize" show-total show-elevator @on-change="handlePageChange" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { Input, Button, Icon, Table, Badge, Page, DatePicker, Select, Option, Modal, Message } from 'view-ui-plus'

const state = reactive({
  searchForm: { keyword: '', dateRange: [], status: null },
  stats: { pending: 5, processing: 2, completed: 128, cancelled: 8 },
  tableData: [],
  loading: false,
  selectedRows: [],
  pagination: { total: 0, current: 1, pageSize: 10 }
})

const columns = [
  { type: 'selection', width: 60, align: 'center' },
  { title: '发票抬头', key: 'title', width: 180 },
  { title: '税号', key: 'taxNo', width: 180 },
  { title: '发票金额', key: 'amount', width: 120, align: 'right' },
  { title: '订单号', key: 'orderNo', width: 180 },
  { title: '开票时间', key: 'createTime', width: 160 },
  { title: '状态', slot: 'status', width: 100, align: 'center' },
  { title: '操作', slot: 'action', width: 120 }
]

const getStatusText = (s) => ({ pending: '待开票', processing: '开票中', completed: '已完成', cancelled: '已冲红' }[s] || s)
const getStatusBadge = (s) => ({ pending: 'warning', processing: 'processing', completed: 'success', cancelled: 'error' }[s] || 'default')

const initData = () => {
  state.loading = true
  setTimeout(() => {
    state.tableData = [
      { id: 1, title: '北京XX科技有限公司', taxNo: '91110000XXXXXXXX', amount: 150, orderNo: 'P20240115120001', createTime: '2024-01-15 12:00:00', status: 'completed' },
      { id: 2, title: '张三', taxNo: '-', amount: 30, orderNo: 'P20240115100002', createTime: '2024-01-15 10:00:00', status: 'completed' },
      { id: 3, title: '上海XX集团', taxNo: '91310000XXXXXXXX', amount: 500, orderNo: 'P20240114180003', createTime: '2024-01-14 18:00:00', status: 'cancelled' },
      { id: 4, title: '李四', taxNo: '-', amount: 20, orderNo: 'P20240115143004', createTime: '2024-01-15 14:30:00', status: 'pending' }
    ]
    state.pagination.total = 4
    state.loading = false
  }, 500)
}

const handleSearch = () => { state.pagination.current = 1; initData() }
const handleReset = () => { state.searchForm = { keyword: '', dateRange: [], status: null }; handleSearch() }
const handleSelectionChange = (s) => { state.selectedRows = s }
const handlePageChange = (p) => { state.pagination.current = p; initData() }
const handleView = (r) => console.log('查看', r)
const handleRed = (r) => Modal.confirm({ title: '确认冲红', content: `确定要冲红发票"${r.title}"（¥${r.amount}）吗？`, onOk: () => { Message.success('冲红成功'); initData() } })
const handleBatchRed = () => Modal.confirm({ title: '确认批量冲红', content: `确定要冲红 ${state.selectedRows.length} 张发票吗？`, onOk: () => { Message.success('批量冲红成功'); initData() } })

initData()
</script>

<style lang="less" scoped>
.invoice-manage-page {
  .filter-bar { padding: var(--spacing-xl); background: var(--bg-color); border-radius: var(--border-radius-base); margin-bottom: var(--spacing-lg); box-shadow: var(--shadow-base); }
  .filter-row { display: flex; flex-wrap: wrap; gap: var(--spacing-md); margin-bottom: var(--spacing-md); .filter-input { width: 220px; } .filter-date { width: 260px; } .filter-select { width: 150px; } }
  .filter-actions { display: flex; gap: var(--spacing-md); }
  .stats-row { display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--spacing-lg); margin-bottom: var(--spacing-lg); .stat-item { padding: var(--spacing-xl); background: var(--bg-color); border-radius: var(--border-radius-base); box-shadow: var(--shadow-base); display: flex; flex-direction: column; .stat-label { font-size: var(--font-size-sm); color: var(--text-color-secondary); margin-bottom: var(--spacing-xs); } .stat-value { font-size: 24px; font-weight: 600; color: var(--text-color-title); } } }
  .table-container { background: var(--bg-color); border-radius: var(--border-radius-base); padding: var(--spacing-xl); box-shadow: var(--shadow-base); .text-danger { color: var(--error-color); } .pagination-wrapper { display: flex; justify-content: flex-end; margin-top: var(--spacing-xl); } }
}
</style>