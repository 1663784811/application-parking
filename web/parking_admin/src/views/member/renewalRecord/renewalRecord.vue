<template>
  <div class="renewal-record-page">
    <div class="filter-bar">
      <div class="filter-row">
        <div class="filter-item">
          <DatePicker v-model="state.searchForm.dateRange" type="daterange" placeholder="续费时间" class="filter-date" />
        </div>
        <div class="filter-item">
          <Input v-model="state.searchForm.plate" placeholder="车牌号" class="filter-input" clearable @on-enter="handleSearch" />
        </div>
        <div class="filter-item">
          <Select v-model="state.searchForm.cardType" placeholder="卡类型" class="filter-select" clearable>
            <Option :value="1">月卡</Option><Option :value="2">季卡</Option><Option :value="3">年卡</Option>
          </Select>
        </div>
        <div class="filter-item"><Button type="primary" @click="handleSearch">查询</Button></div>
        <div class="filter-item"><Button @click="handleReset">重置</Button></div>
        <div class="filter-item"><Button @click="handleExport"><Icon type="ios-download-outline" />导出</Button></div>
      </div>
    </div>
    <div class="table-container">
      <TableColumnSetting :columns="columns" v-model:visible="visibleFields" v-model:open="colSettingVisible" @reset="resetColumns" />
      <Table :columns="displayColumns" :data="state.tableData" :loading="state.loading">
        <template #cardType="{ row }"><span class="card-type" :class="'type-' + row.cardType">{{ getCardTypeText(row.cardType) }}</span></template>
        <template #action="{ row }"><Button type="text" size="small" @click="handleView(row)">详情</Button></template>
      </Table>
      <div class="pagination-wrapper"><Page :total="state.pagination.total" :current="state.pagination.current" :page-size="state.pagination.pageSize" show-total show-elevator @on-change="handlePageChange" /></div>
    </div>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import { Button, Icon, Table, Page, DatePicker, Input, Select, Option, Message } from 'view-ui-plus'
import { memberApi } from '@/api'
import TableColumnSetting from '@/components/TableColumnSetting.vue'
import { useTableColumns } from '@/composables/useTableColumns'

const state = reactive({
  searchForm: { dateRange: [], plate: '', cardType: null },
  tableData: [],
  loading: false,
  pagination: { total: 0, current: 1, pageSize: 10 }
})

const columns = [
  { field: 'orderNo', title: '订单号', key: 'orderNo', minWidth: 180 },
  { field: 'plate', title: '车牌号', key: 'plate', minWidth: 120 },
  { field: 'name', title: '车主姓名', key: 'name', minWidth: 100 },
  { field: 'cardType', title: '卡类型', slot: 'cardType', minWidth: 100 },
  { field: 'duration', title: '续费时长', key: 'duration', minWidth: 100, render: (h, p) => h('span', p.row.cardType === 1 ? '1个月' : p.row.cardType === 2 ? '1季度' : '1年') },
  { field: 'amount', title: '续费金额', key: 'amount', minWidth: 120, align: 'right', render: (h, p) => h('span', '¥' + p.row.amount) },
  { field: 'operator', title: '操作员', key: 'operator', minWidth: 100 },
  { field: 'renewalTime', title: '续费时间', key: 'renewalTime', minWidth: 160 },
  { title: '操作', slot: 'action', minWidth: 80 }
]

const { visibleFields, colSettingVisible, displayColumns, resetColumns } = useTableColumns(columns, 'renewalRecord:columnVisible')

const getCardTypeText = (t) => ({ 1: '月卡', 2: '季卡', 3: '年卡' }[t] || t)

// 将 DatePicker 的日期值格式化为日期字符串（后端再拼接 00:00:00 / 23:59:59）
const formatDateOnly = (val) => {
  if (!val) return null
  if (typeof val === 'string') return val
  const d = new Date(val)
  const pad = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}`
}

// 续费记录列表（只读，分页）
const initData = async () => {
  state.loading = true
  try {
    const dateRange = state.searchForm.dateRange || []
    const res = await memberApi.getRenewalList({
      page: state.pagination.current,
      size: state.pagination.pageSize,
      plate: state.searchForm.plate,
      cardType: state.searchForm.cardType,
      startTime: dateRange[0] ? formatDateOnly(dateRange[0]) : null,
      endTime: dateRange[1] ? formatDateOnly(dateRange[1]) : null
    })
    state.tableData = res.data || []
    state.pagination.total = (res.result && res.result.total) || 0
  } catch (e) {
    console.error('获取续费记录失败', e)
  } finally {
    state.loading = false
  }
}

const handleSearch = () => { state.pagination.current = 1; initData() }
const handleReset = () => { state.searchForm = { dateRange: [], plate: '', cardType: null }; handleSearch() }
const handleExport = () => Message.info('导出功能待对接')
const handleView = (r) => Message.info('详情功能待对接')
const handlePageChange = (p) => { state.pagination.current = p; initData() }

onMounted(() => {
  initData()
})
</script>

<style lang="less" scoped>
.renewal-record-page {
  .filter-bar { padding: var(--spacing-xl); background: var(--bg-color); border-radius: var(--border-radius-base); margin-bottom: var(--spacing-lg); box-shadow: var(--shadow-base); }
  .filter-row { display: flex; flex-wrap: wrap; gap: var(--spacing-md); margin-bottom: var(--spacing-md); .filter-item { flex-shrink: 0; } .filter-date { width: 260px; } .filter-input { width: 180px; } .filter-select { width: 150px; } }
  .table-container { background: var(--bg-color); border-radius: var(--border-radius-base); padding: var(--spacing-xl); box-shadow: var(--shadow-base); .card-type { padding: 2px 8px; border-radius: var(--border-radius-sm); font-size: var(--font-size-xs); &.type-1 { background: rgba(22,93,255,0.1); color: #165DFF; } &.type-2 { background: rgba(15,198,194,0.1); color: #0FC6C2; } &.type-3 { background: rgba(114,46,209,0.1); color: #722ED1; } } .pagination-wrapper { display: flex; justify-content: flex-end; margin-top: var(--spacing-xl); } }
}
</style>