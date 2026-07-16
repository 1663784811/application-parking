<template>
  <div class="renewal-record-page">
    <div class="filter-bar">
      <div class="filter-row">
        <DatePicker v-model="state.searchForm.dateRange" type="daterange" placeholder="续费时间" class="filter-date" />
        <Input v-model="state.searchForm.plate" placeholder="车牌号" class="filter-input" clearable @on-enter="handleSearch" />
        <Select v-model="state.searchForm.cardType" placeholder="卡类型" class="filter-select" clearable>
          <Option :value="1">月卡</Option><Option :value="2">季卡</Option><Option :value="3">年卡</Option>
        </Select>
        <Button type="primary" @click="handleSearch">查询</Button><Button @click="handleReset">重置</Button>
      </div>
      <div class="filter-actions"><Button @click="handleExport"><Icon type="ios-download-outline" />导出</Button></div>
    </div>
    <div class="table-container">
      <Table :columns="columns" :data="state.tableData" :loading="state.loading">
        <template #cardType="{ row }"><span class="card-type" :class="'type-' + row.cardType">{{ getCardTypeText(row.cardType) }}</span></template>
        <template #action="{ row }"><Button type="text" size="small" @click="handleView(row)">详情</Button></template>
      </Table>
      <div class="pagination-wrapper"><Page :total="state.pagination.total" :current="state.pagination.current" :page-size="state.pagination.pageSize" show-total show-elevator @on-change="handlePageChange" /></div>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import { Button, Icon, Table, Page, DatePicker, Input, Select, Option } from 'view-ui-plus'

const state = reactive({
  searchForm: { dateRange: [], plate: '', cardType: null },
  tableData: [],
  loading: false,
  pagination: { total: 0, current: 1, pageSize: 10 }
})

const columns = [
  { title: '订单号', key: 'orderNo', width: 180 },
  { title: '车牌号', key: 'plate', width: 120 },
  { title: '车主姓名', key: 'name', width: 100 },
  { title: '卡类型', slot: 'cardType', width: 100 },
  { title: '续费时长', key: 'duration', width: 100, render: (h, p) => h('span', p.row.cardType === 1 ? '1个月' : p.row.cardType === 2 ? '1季度' : '1年') },
  { title: '续费金额', key: 'amount', width: 120, align: 'right', render: (h, p) => h('span', '¥' + p.row.amount) },
  { title: '操作员', key: 'operator', width: 100 },
  { title: '续费时间', key: 'renewalTime', width: 160 },
  { title: '操作', slot: 'action', width: 80 }
]

const getCardTypeText = (t) => ({ 1: '月卡', 2: '季卡', 3: '年卡' }[t] || t)

const initData = () => {
  state.loading = true
  setTimeout(() => {
    state.tableData = [
      { id: 1, orderNo: 'R20240115100001', plate: '京A12345', name: '张三', cardType: 1, amount: 300, operator: '管理员', renewalTime: '2024-01-15 10:00:00' },
      { id: 2, orderNo: 'R20240114150002', plate: '京B67890', name: '李四', cardType: 2, amount: 800, operator: '管理员', renewalTime: '2024-01-14 15:00:00' },
      { id: 3, orderNo: 'R20240113120003', plate: '浙C11111', name: '王五', cardType: 3, amount: 2800, operator: '管理员', renewalTime: '2024-01-13 12:00:00' },
      { id: 4, orderNo: 'R20240112180004', plate: '京D22222', name: '赵六', cardType: 1, amount: 300, operator: '管理员', renewalTime: '2024-01-12 18:00:00' }
    ]
    state.pagination.total = 4
    state.loading = false
  }, 500)
}

const handleSearch = () => { state.pagination.current = 1; initData() }
const handleReset = () => { state.searchForm = { dateRange: [], plate: '', cardType: null }; handleSearch() }
const handleExport = () => import('view-ui-plus').then(m => m.Message.info('导出中...'))
const handleView = (r) => console.log('查看', r)
const handlePageChange = (p) => { state.pagination.current = p; initData() }

initData()
</script>

<style lang="less" scoped>
.renewal-record-page {
  .filter-bar { padding: var(--spacing-xl); background: var(--bg-color); border-radius: var(--border-radius-base); margin-bottom: var(--spacing-lg); box-shadow: var(--shadow-base); }
  .filter-row { display: flex; flex-wrap: wrap; gap: var(--spacing-md); margin-bottom: var(--spacing-md); .filter-date { width: 260px; } .filter-input { width: 180px; } .filter-select { width: 150px; } }
  .filter-actions { display: flex; gap: var(--spacing-md); }
  .table-container { background: var(--bg-color); border-radius: var(--border-radius-base); padding: var(--spacing-xl); box-shadow: var(--shadow-base); .card-type { padding: 2px 8px; border-radius: var(--border-radius-sm); font-size: var(--font-size-xs); &.type-1 { background: rgba(22,93,255,0.1); color: #165DFF; } &.type-2 { background: rgba(15,198,194,0.1); color: #0FC6C2; } &.type-3 { background: rgba(114,46,209,0.1); color: #722ED1; } } .pagination-wrapper { display: flex; justify-content: flex-end; margin-top: var(--spacing-xl); } }
}
</style>