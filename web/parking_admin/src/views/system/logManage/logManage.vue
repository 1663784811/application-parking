<template>
  <div class="log-manage-page">
    <div class="filter-bar">
      <div class="filter-row">
        <div class="filter-item">
          <Select v-model="state.logType" placeholder="日志类型" class="filter-select" @on-change="handleTypeChange">
            <Option value="login">登录日志</Option>
            <Option value="operation">操作日志</Option>
          </Select>
        </div>
        <div class="filter-item">
          <DatePicker v-model="state.dateRange" type="daterange" placeholder="时间范围" class="filter-date"/>
        </div>
        <div class="filter-item">
          <Input v-model="state.keyword" placeholder="关键词" class="filter-input" clearable @on-enter="handleSearch"/>
        </div>
        <div class="filter-item"><Button type="primary" @click="handleSearch">查询</Button></div>
        <div class="filter-item"><Button @click="handleReset">重置</Button></div>
        <div class="filter-item">
          <Button @click="handleExport">
            <Icon type="ios-download-outline"/>
            导出
          </Button>
        </div>
      </div>
    </div>
    <div class="table-container">
      <TableColumnSetting :columns="settingColumns" v-model:visible="visibleFields" v-model:open="colSettingVisible" @reset="resetColumns" />
      <Table :columns="displayColumns" :data="state.tableData" :loading="state.loading">
        <template #operator="{ row }"><span class="operator-cell">{{ row.operator }}</span></template>
        <template #action="{ row }">
          <Button type="text" size="small" @click="handleViewDetail(row)">详情</Button>
        </template>
      </Table>
      <div class="pagination-wrapper">
        <Page :total="state.pagination.total" :current="state.pagination.current" :page-size="state.pagination.pageSize"
              show-total show-elevator @on-change="handlePageChange"/>
      </div>
    </div>
    <Modal v-model="state.detailVisible" title="日志详情" width="600">
      <div class="detail-content" v-if="state.currentLog">
        <Form :label-width="100" label-colon>
          <FormItem label="操作时间">{{ state.currentLog.time }}</FormItem>
          <FormItem label="操作用户">{{ state.currentLog.operator }}</FormItem>
          <FormItem label="操作类型">{{ state.currentLog.actionType }}</FormItem>
          <FormItem label="操作描述">{{ state.currentLog.description }}</FormItem>
          <FormItem label="IP地址">{{ state.currentLog.ip }}</FormItem>
          <FormItem label="浏览器">{{ state.currentLog.browser }}</FormItem>
          <FormItem label="操作系统">{{ state.currentLog.os }}</FormItem>
        </Form>
      </div>
      <template #footer>
        <Button @click="state.detailVisible = false">关闭</Button>
      </template>
    </Modal>
  </div>
</template>

<script setup>
import {reactive, computed, onMounted} from 'vue'
import {
  Button,
  DatePicker,
  Form,
  FormItem,
  Icon,
  Input,
  Message,
  Modal,
  Option,
  Page,
  Select,
  Table
} from 'view-ui-plus'
import {systemApi} from '@/api'
import TableColumnSetting from '@/components/TableColumnSetting.vue'
import { useTableColumns } from '@/composables/useTableColumns'

const state = reactive({
  logType: 'login',
  dateRange: [],
  keyword: '',
  tableData: [],
  loading: false,
  pagination: {total: 0, current: 1, pageSize: 10},
  detailVisible: false,
  currentLog: null
})

const loginColumns = [
  {field: 'time', title: '时间', key: 'time', minWidth: 160},
  {field: 'operator', title: '用户', key: 'operator', minWidth: 120},
  {field: 'ip', title: 'IP地址', key: 'ip', minWidth: 140},
  {field: 'browser', title: '浏览器', key: 'browser', minWidth: 150},
  {field: 'os', title: '操作系统', key: 'os', minWidth: 120},
  {
    field: 'statusName',
    title: '状态',
    key: 'statusName',
    minWidth: 80,
    render: (h, p) => h('span', {class: p.row.status === 1 ? 'text-success' : 'text-error'}, p.row.statusName)
  },
  {title: '操作', slot: 'action', minWidth: 80}
]

const operationColumns = [
  {field: 'time', title: '时间', key: 'time', minWidth: 160},
  {field: 'operator', title: '操作人', key: 'operator', minWidth: 120},
  {field: 'actionType', title: '操作类型', key: 'actionType', minWidth: 120},
  {field: 'description', title: '操作描述', key: 'description', minWidth: 200, tooltip: true},
  {field: 'ip', title: 'IP地址', key: 'ip', minWidth: 140},
  {title: '操作', slot: 'action', minWidth: 80}
]

// 列设置：登录/操作两套列各自持久化到 localStorage（按 tab 独立）
const loginColSetting = useTableColumns(loginColumns, 'logManage:login:columnVisible')
const opColSetting = useTableColumns(operationColumns, 'logManage:operation:columnVisible')
// 当前 tab 对应的列设置实例
const activeSetting = () => state.logType === 'login' ? loginColSetting : opColSetting

// 列设置组件展示的可选列（随 tab 切换）
const settingColumns = computed(() => state.logType === 'login' ? loginColumns : operationColumns)
// 当前应显示的列（已按列设置过滤）
const displayColumns = computed(() => activeSetting().displayColumns.value)
// 列设置显隐状态随 tab 路由到对应实例
const visibleFields = computed({
  get: () => activeSetting().visibleFields.value,
  set: (v) => { activeSetting().visibleFields.value = v }
})
const colSettingVisible = computed({
  get: () => activeSetting().colSettingVisible.value,
  set: (v) => { activeSetting().colSettingVisible.value = v }
})
const resetColumns = () => activeSetting().resetColumns()

// Date → 'YYYY-MM-DD'（后端拼 00:00:00 / 23:59:59）
const fmtDay = (d) => {
  if (!d) return null
  const dt = d instanceof Date ? d : new Date(d)
  const p = (n) => String(n).padStart(2, '0')
  return `${dt.getFullYear()}-${p(dt.getMonth() + 1)}-${p(dt.getDate())}`
}

// 后端 createTime 可能是字符串或 Jackson LocalDateTime 数组，统一格式化为 'YYYY-MM-DD HH:mm:ss'
const fmtDateTime = (v) => {
  if (!v) return ''
  let d
  if (Array.isArray(v)) {
    d = new Date(v[0] || 1970, (v[1] || 1) - 1, v[2] || 1, v[3] || 0, v[4] || 0, v[5] || 0)
  } else if (v instanceof Date) {
    d = v
  } else {
    d = new Date(String(v).replace('T', ' ').replace(/-/g, '/'))
  }
  if (isNaN(d.getTime())) return String(v)
  const p = (n) => String(n).padStart(2, '0')
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())} ${p(d.getHours())}:${p(d.getMinutes())}:${p(d.getSeconds())}`
}

// 从 userAgent 粗略解析浏览器/操作系统
const parseBrowser = (ua) => {
  if (!ua) return ''
  if (/Edg/.test(ua)) return 'Edge'
  if (/Chrome/.test(ua)) return 'Chrome'
  if (/Firefox/.test(ua)) return 'Firefox'
  if (/Safari/.test(ua)) return 'Safari'
  return ''
}
const parseOs = (ua) => {
  if (!ua) return ''
  if (/Windows NT 10/.test(ua)) return 'Windows 10/11'
  if (/Windows/.test(ua)) return 'Windows'
  if (/iPhone|iPad|iOS/.test(ua)) return 'iOS'
  if (/Android/.test(ua)) return 'Android'
  if (/Mac OS X/.test(ua)) return 'macOS'
  if (/Linux/.test(ua)) return 'Linux'
  return ''
}

// AuLog 字段 → 页面字段映射
const mapRow = (r) => ({
  ...r,
  time: fmtDateTime(r.createTime),
  operator: r.adminName,
  browser: parseBrowser(r.userAgent),
  os: parseOs(r.userAgent),
  statusName: r.status === 1 ? '成功' : '失败'
})

// tab 切换：列由 displayColumns 按 logType 自动派生，无需手动替换数组
const handleTypeChange = () => {
  handleSearch()
}

// 日志列表（分页，logType + dateRange + keyword）
const initData = async () => {
  state.loading = true
  try {
    const dr = state.dateRange || []
    const res = await systemApi.getLogList({
      page: state.pagination.current,
      size: state.pagination.pageSize,
      logType: state.logType,
      startTime: dr[0] ? fmtDay(dr[0]) : null,
      endTime: dr[1] ? fmtDay(dr[1]) : null,
      keyword: state.keyword
    })
    state.tableData = (res.data || []).map(mapRow)
    state.pagination.total = (res.result && res.result.total) || 0
  } catch (e) {
    console.error('获取日志列表失败', e)
  } finally {
    state.loading = false
  }
}

const handleSearch = () => {
  state.pagination.current = 1;
  initData()
}
const handleReset = () => {
  state.dateRange = [];
  state.keyword = '';
  handleSearch()
}
const handleExport = () => Message.info('导出日志功能开发中')
const handlePageChange = p => {
  state.pagination.current = p;
  initData()
}
const handleViewDetail = r => {
  state.currentLog = r;
  state.detailVisible = true
}

onMounted(() => {
  handleTypeChange()
})
</script>

<style lang="less" scoped>
.log-manage-page {
  .filter-bar {
    padding: var(--spacing-xl);
    background: var(--bg-color);
    border-radius: var(--border-radius-base);
    margin-bottom: var(--spacing-lg);
    box-shadow: var(--shadow-base);

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

      .filter-date {
        width: 260px;
      }

      .filter-input {
        width: 200px;
      }
    }
  }

  .table-container {
    background: var(--bg-color);
    border-radius: var(--border-radius-base);
    padding: var(--spacing-xl);
    box-shadow: var(--shadow-base);

    .text-success {
      color: var(--success-color);
    }

    .text-error {
      color: var(--error-color);
    }

    .pagination-wrapper {
      display: flex;
      justify-content: flex-end;
      margin-top: var(--spacing-xl);
    }
  }

  .detail-content {
    padding: var(--spacing-md) 0;
  }
}
</style>
