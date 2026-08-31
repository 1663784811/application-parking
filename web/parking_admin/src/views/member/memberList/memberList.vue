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
      <TableColumnSetting :columns="columns" v-model:visible="visibleFields" v-model:open="colSettingVisible" @reset="resetColumns" />
      <Table :columns="displayColumns" :data="state.tableData" :loading="state.loading">
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

    <Modal v-model="state.modalVisible" :title="state.modalType === 'add' ? '新增会员' : '编辑会员'" width="1000">
      <Form :model="state.formData" :rules="state.rules" :label-width="100" class="modal-form-2col">
        <FormItem label="车牌号" prop="plate"><Input v-model="state.formData.plate" placeholder="如：京A12345" /></FormItem>
        <FormItem label="车主姓名" prop="name"><Input v-model="state.formData.name" placeholder="请输入车主姓名" /></FormItem>
        <FormItem label="手机号" prop="phone"><Input v-model="state.formData.phone" placeholder="请输入手机号" /></FormItem>
        <FormItem label="卡类型" prop="cardType">
          <Select v-model="state.formData.cardType">
            <Option :value="1">月卡</Option><Option :value="2">季卡</Option><Option :value="3">年卡</Option>
          </Select>
        </FormItem>
        <FormItem label="到期时间" prop="expireTime">
          <DatePicker v-model="state.formData.expireTime" type="datetime" format="yyyy-MM-dd HH:mm:ss" placeholder="选择到期时间" style="width: 100%" />
        </FormItem>
      </Form>
      <template #footer><Button @click="state.modalVisible = false">取消</Button><Button type="primary" @click="handleSubmit">确定</Button></template>
    </Modal>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import { Input, Button, Icon, Table, Badge, Page, Select, Option, Modal, DatePicker, Form, FormItem, Message } from 'view-ui-plus'
import { memberApi } from '@/api'
import TableColumnSetting from '@/components/TableColumnSetting.vue'
import { useTableColumns } from '@/composables/useTableColumns'

const state = reactive({
  searchForm: { plate: '', phone: '', status: null },
  stats: { total: 0, monthly: 0, quarterly: 0, yearly: 0, expiring: 0 },
  tableData: [],
  loading: false,
  pagination: { total: 0, current: 1, pageSize: 10 },
  modalVisible: false,
  modalType: 'add',
  formData: { id: null, plate: '', name: '', phone: '', cardType: 1, expireTime: '', frozen: 0, totalAmount: 0 },
  rules: {
    plate: [{ required: true, message: '请输入车牌号', trigger: 'blur' }],
    name: [{ required: true, message: '请输入车主姓名', trigger: 'blur' }],
    phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }]
  }
})

const columns = [
  { field: 'plate', title: '车牌号', key: 'plate', minWidth: 120 },
  { field: 'name', title: '车主姓名', key: 'name', minWidth: 100 },
  { field: 'phone', title: '手机号', key: 'phone', minWidth: 130 },
  { field: 'cardType', title: '卡类型', slot: 'cardType', minWidth: 100 },
  { field: 'expireTime', title: '到期时间', key: 'expireTime', minWidth: 160 },
  { field: 'remainDays', title: '剩余天数', key: 'remainDays', minWidth: 100, align: 'center', render: (h, p) => h('span', { class: getRemainDaysClass(p.row.remainDays) }, p.row.remainDays + '天') },
  { field: 'totalAmount', title: '累计充值', key: 'totalAmount', minWidth: 120, align: 'right', render: (h, p) => h('span', '¥' + p.row.totalAmount) },
  { field: 'status', title: '卡状态', slot: 'status', minWidth: 100, align: 'center' },
  { title: '操作', slot: 'action', minWidth: 220, fixed: 'right' }
]

const { visibleFields, colSettingVisible, displayColumns, resetColumns } = useTableColumns(columns, 'memberList:columnVisible')

const getCardTypeText = (t) => ({ 1: '月卡', 2: '季卡', 3: '年卡' }[t] || t)
const getStatusText = (s) => ({ 1: '正常', 2: '即将到期', 3: '已过期', 4: '已冻结' }[s] || s)
const getStatusBadge = (s) => ({ 1: 'success', 2: 'warning', 3: 'error', 4: 'default' }[s] || 'default')
const getRemainDaysClass = (d) => d <= 0 ? 'text-error' : d <= 7 ? 'text-warning' : ''

// 由到期时间计算剩余天数（剩余天数与卡状态均为前端派生值，不落库）
const computeRemainDays = (expireTime) => {
  if (!expireTime) return 0
  const diff = new Date(expireTime).getTime() - Date.now()
  return Math.floor(diff / 86400000)
}
const computeStatus = (row) => {
  if (row.frozen === 1) return 4
  const d = computeRemainDays(row.expireTime)
  if (d < 0) return 3
  if (d <= 7) return 2
  return 1
}

// 将 DatePicker 的值格式化为后端 LocalDateTime 接受的字符串
const formatDateTime = (val) => {
  if (!val) return null
  if (typeof val === 'string') return val
  const d = new Date(val)
  const pad = n => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())} ${pad(d.getHours())}:${pad(d.getMinutes())}:${pad(d.getSeconds())}`
}

// 会员数量统计
const loadStats = async () => {
  try {
    const res = await memberApi.getMemberStats()
    state.stats = { total: 0, monthly: 0, quarterly: 0, yearly: 0, expiring: 0, ...(res.data || {}) }
  } catch (e) {
    console.error('获取会员统计失败', e)
  }
}

// 会员列表（分页，status 为派生状态：后端按 frozen/expire_time 过滤）
const initData = async () => {
  state.loading = true
  try {
    const res = await memberApi.getMemberList({
      page: state.pagination.current,
      size: state.pagination.pageSize,
      plate: state.searchForm.plate,
      phone: state.searchForm.phone,
      status: state.searchForm.status
    })
    state.tableData = (res.data || []).map(r => ({
      ...r,
      remainDays: computeRemainDays(r.expireTime),
      status: computeStatus(r)
    }))
    state.pagination.total = (res.result && res.result.total) || 0
  } catch (e) {
    console.error('获取会员列表失败', e)
  } finally {
    state.loading = false
  }
}

const handleSearch = () => { state.pagination.current = 1; initData() }
const handleReset = () => { state.searchForm = { plate: '', phone: '', status: null }; handleSearch() }

const handleAdd = () => {
  state.modalType = 'add'
  state.formData = { id: null, plate: '', name: '', phone: '', cardType: 1, expireTime: '', frozen: 0, totalAmount: 0 }
  state.modalVisible = true
}

const handleSubmit = async () => {
  if (!state.formData.plate) { Message.warning('请输入车牌号'); return }
  if (!state.formData.name) { Message.warning('请输入车主姓名'); return }
  if (!state.formData.phone) { Message.warning('请输入手机号'); return }
  try {
    const payload = {
      ...state.formData,
      expireTime: formatDateTime(state.formData.expireTime),
      frozen: state.formData.frozen ?? 0,
      totalAmount: state.formData.totalAmount ?? 0
    }
    await (state.modalType === 'add' ? memberApi.addMember : memberApi.editMember)(payload)
    Message.success(state.modalType === 'add' ? '新增成功' : '编辑成功')
    state.modalVisible = false
    loadStats()
    initData()
  } catch (e) {
    console.error('保存会员失败', e)
  }
}

const handleBatchExport = () => Message.info('导出功能待对接')

const handleRenewal = (r) => {
  Modal.confirm({
    title: '续费',
    content: `确定要为 ${r.plate} 续费吗？`,
    onOk: async () => {
      try {
        await memberApi.renewal({ memberId: r.id })
        Message.success('续费成功')
        loadStats()
        initData()
      } catch (e) {
        console.error('续费失败', e)
        Message.error('续费失败，请检查是否已配置该卡类型的套餐')
      }
    }
  })
}

const handleFreeze = (r) => {
  const frozen = r.status === 4
  Modal.confirm({
    title: frozen ? '解冻' : '冻结',
    content: `确定要${frozen ? '解冻' : '冻结'} ${r.plate} 吗？`,
    onOk: async () => {
      try {
        await (frozen ? memberApi.unfreezeMember : memberApi.freezeMember)(r.id)
        Message.success(frozen ? '已解冻' : '已冻结')
        loadStats()
        initData()
      } catch (e) {
        console.error('操作失败', e)
      }
    }
  })
}

const handleUnbind = (r) => Modal.confirm({ title: '解绑', content: `确定要解绑 ${r.plate} 吗？`, onOk: () => Message.info('解绑功能待对接') })

const handleSendNotice = async (r) => {
  try {
    await memberApi.sendExpireNotice(r.id)
    Message.success(`已向 ${r.phone} 发送到期提醒`)
  } catch (e) {
    console.error('发送提醒失败', e)
  }
}

const handlePageChange = (p) => { state.pagination.current = p; initData() }

onMounted(() => {
  loadStats()
  initData()
})
</script>

<style lang="less" scoped>
.member-list-page {
  .filter-bar { padding: var(--spacing-xl); background: var(--bg-color); border-radius: var(--border-radius-base); margin-bottom: var(--spacing-lg); box-shadow: var(--shadow-base); }
  .filter-row { display: flex; flex-wrap: wrap; gap: var(--spacing-md); margin-bottom: var(--spacing-md); .filter-item { flex-shrink: 0; } .filter-input { width: 180px; } .filter-select { width: 150px; } }
  .stats-row { display: grid; grid-template-columns: repeat(5, 1fr); gap: var(--spacing-lg); margin-bottom: var(--spacing-lg); .stat-item { padding: var(--spacing-xl); background: var(--bg-color); border-radius: var(--border-radius-base); box-shadow: var(--shadow-base); display: flex; flex-direction: column; &.warning .stat-value { color: var(--warning-color); } .stat-label { font-size: var(--font-size-sm); color: var(--text-color-secondary); margin-bottom: var(--spacing-xs); } .stat-value { font-size: 24px; font-weight: 600; color: var(--text-color-title); } } }
  .table-container { background: var(--bg-color); border-radius: var(--border-radius-base); padding: var(--spacing-xl); box-shadow: var(--shadow-base); .card-type { padding: 2px 8px; border-radius: var(--border-radius-sm); font-size: var(--font-size-xs); &.type-1 { background: rgba(22,93,255,0.1); color: #165DFF; } &.type-2 { background: rgba(15,198,194,0.1); color: #0FC6C2; } &.type-3 { background: rgba(114,46,209,0.1); color: #722ED1; } } .text-warning { color: var(--warning-color); } .text-error { color: var(--error-color); } .pagination-wrapper { display: flex; justify-content: flex-end; margin-top: var(--spacing-xl); } }
}
</style>