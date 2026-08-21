<template>
  <div class="member-list-page">
    <div class="filter-bar">
      <div class="filter-row">
        <div class="filter-item">
          <Input v-model="state.searchForm.plate" placeholder="搜索车牌" class="filter-input" clearable @on-enter="handleSearch">
            <template #prefix><Icon type="ios-search" /></template>
          </Input>
        </div>
        <div class="filter-item">
          <Input v-model="state.searchForm.phone" placeholder="手机号" class="filter-input" clearable @on-enter="handleSearch" />
        </div>
        <div class="filter-item">
          <Select v-model="state.searchForm.status" placeholder="卡状态" class="filter-select" clearable>
            <Option :value="1">正常</Option>
            <Option :value="2">即将到期</Option>
            <Option :value="3">已过期</Option>
            <Option :value="4">已冻结</Option>
          </Select>
        </div>
        <div class="filter-item"><Button type="primary" @click="handleSearch">查询</Button></div>
        <div class="filter-item"><Button @click="handleReset">重置</Button></div>
        <div class="filter-item">
          <Button type="primary" @click="handleAdd"><Icon type="ios-add" />新增会员</Button>
        </div>
        <div class="filter-item">
          <Button @click="handleBatchExport"><Icon type="ios-download-outline" />导出</Button>
        </div>
      </div>
    </div>

    <div class="stats-row">
      <div class="stat-item"><span class="stat-label">总会员数</span><span class="stat-value">{{ state.stats.total }}</span></div>
      <div class="stat-item"><span class="stat-label">月卡</span><span class="stat-value">{{ state.stats.monthly }}</span></div>
      <div class="stat-item"><span class="stat-label">季卡</span><span class="stat-value">{{ state.stats.quarterly }}</span></div>
      <div class="stat-item"><span class="stat-label">年卡</span><span class="stat-value">{{ state.stats.yearly }}</span></div>
      <div class="stat-item warning"><span class="stat-label">即将到期</span><span class="stat-value">{{ state.stats.expiring }}</span></div>
    </div>

    <div class="table-container">
      <Table :columns="columns" :data="state.tableData" :loading="state.loading">
        <template #cardType="{ row }">
          <span class="card-type" :class="'type-' + row.cardType">{{ getCardTypeText(row.cardType) }}</span>
        </template>
        <template #status="{ row }">
          <Badge :status="getStatusBadge(row.status)" :text="getStatusText(row.status)" />
        </template>
        <template #action="{ row }">
          <Button type="text" size="small" @click="handleRenewal(row)">续费</Button>
          <Button type="text" size="small" @click="handleFreeze(row)">{{ row.status === 4 ? '解冻' : '冻结' }}</Button>
          <Button type="text" size="small" @click="handleUnbind(row)">解绑</Button>
          <Button type="text" size="small" @click="handleSendNotice(row)" class="text-warning">提醒</Button>
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
import { Input, Button, Icon, Table, Badge, Page, Select, Option, Modal, Message } from 'view-ui-plus'

const state = reactive({
  searchForm: { plate: '', phone: '', status: null },
  stats: { total: 365, monthly: 180, quarterly: 95, yearly: 90, expiring: 28 },
  tableData: [],
  loading: false,
  pagination: { total: 0, current: 1, pageSize: 10 }
})

const columns = [
  { title: '车牌号', key: 'plate', minWidth: 120 },
  { title: '车主姓名', key: 'name', minWidth: 100 },
  { title: '手机号', key: 'phone', minWidth: 130 },
  { title: '卡类型', slot: 'cardType', minWidth: 100 },
  { title: '到期时间', key: 'expireTime', minWidth: 160 },
  { title: '剩余天数', key: 'remainDays', minWidth: 100, align: 'center', render: (h, p) => h('span', { class: getRemainDaysClass(p.row.remainDays) }, p.row.remainDays + '天') },
  { title: '累计充值', key: 'totalAmount', minWidth: 120, align: 'right', render: (h, p) => h('span', '¥' + p.row.totalAmount) },
  { title: '卡状态', slot: 'status', minWidth: 100, align: 'center' },
  { title: '操作', slot: 'action', minWidth: 220, fixed: 'right' }
]

const getCardTypeText = (t) => ({ 1: '月卡', 2: '季卡', 3: '年卡' }[t] || t)
const getStatusText = (s) => ({ 1: '正常', 2: '即将到期', 3: '已过期', 4: '已冻结' }[s] || s)
const getStatusBadge = (s) => ({ 1: 'success', 2: 'warning', 3: 'error', 4: 'default' }[s] || 'default')
const getRemainDaysClass = (d) => d <= 0 ? 'text-error' : d <= 7 ? 'text-warning' : ''

const initData = () => {
  state.loading = true
  setTimeout(() => {
    state.tableData = [
      { id: 1, plate: '京A12345', name: '张三', phone: '13800138001', cardType: 1, expireTime: '2024-02-15', remainDays: 31, totalAmount: 600, status: 1 },
      { id: 2, plate: '京B67890', name: '李四', phone: '13800138002', cardType: 2, expireTime: '2024-01-20', remainDays: 5, totalAmount: 600, status: 2 },
      { id: 3, plate: '浙C11111', name: '王五', phone: '13800138003', cardType: 3, expireTime: '2025-01-01', remainDays: 382, totalAmount: 3600, status: 1 },
      { id: 4, plate: '京D22222', name: '赵六', phone: '13800138004', cardType: 1, expireTime: '2024-01-05', remainDays: -10, totalAmount: 300, status: 3 },
      { id: 5, plate: '沪E33333', name: '钱七', phone: '13800138005', cardType: 1, expireTime: '2024-01-18', remainDays: 3, totalAmount: 500, status: 2 }
    ]
    state.pagination.total = 5
    state.loading = false
  }, 500)
}

const handleSearch = () => { state.pagination.current = 1; initData() }
const handleReset = () => { state.searchForm = { plate: '', phone: '', status: null }; handleSearch() }
const handleAdd = () => Message.info('新增会员')
const handleBatchExport = () => Message.info('导出中...')
const handleRenewal = (r) => Modal.confirm({ title: '续费', content: `确定要为 ${r.plate} 续费吗？`, onOk: () => Message.success('续费成功') })
const handleFreeze = (r) => Message.success(r.status === 4 ? '已解冻' : '已冻结')
const handleUnbind = (r) => Modal.confirm({ title: '解绑', content: `确定要解绑 ${r.plate} 吗？`, onOk: () => Message.success('解绑成功') })
const handleSendNotice = (r) => Message.success(`已向 ${r.phone} 发送到期提醒`)
const handlePageChange = (p) => { state.pagination.current = p; initData() }

initData()
</script>

<style lang="less" scoped>
.member-list-page {
  .filter-bar { padding: var(--spacing-xl); background: var(--bg-color); border-radius: var(--border-radius-base); margin-bottom: var(--spacing-lg); box-shadow: var(--shadow-base); }
  .filter-row { display: flex; flex-wrap: wrap; gap: var(--spacing-md); margin-bottom: var(--spacing-md); .filter-item { flex-shrink: 0; } .filter-input { width: 180px; } .filter-select { width: 150px; } }
  .stats-row { display: grid; grid-template-columns: repeat(5, 1fr); gap: var(--spacing-lg); margin-bottom: var(--spacing-lg); .stat-item { padding: var(--spacing-xl); background: var(--bg-color); border-radius: var(--border-radius-base); box-shadow: var(--shadow-base); display: flex; flex-direction: column; &.warning .stat-value { color: var(--warning-color); } .stat-label { font-size: var(--font-size-sm); color: var(--text-color-secondary); margin-bottom: var(--spacing-xs); } .stat-value { font-size: 24px; font-weight: 600; color: var(--text-color-title); } } }
  .table-container { background: var(--bg-color); border-radius: var(--border-radius-base); padding: var(--spacing-xl); box-shadow: var(--shadow-base); .card-type { padding: 2px 8px; border-radius: var(--border-radius-sm); font-size: var(--font-size-xs); &.type-1 { background: rgba(22,93,255,0.1); color: #165DFF; } &.type-2 { background: rgba(15,198,194,0.1); color: #0FC6C2; } &.type-3 { background: rgba(114,46,209,0.1); color: #722ED1; } } .text-warning { color: var(--warning-color); } .text-error { color: var(--error-color); } .pagination-wrapper { display: flex; justify-content: flex-end; margin-top: var(--spacing-xl); } }
}
</style>