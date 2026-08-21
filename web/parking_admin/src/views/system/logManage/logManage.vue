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
      <Table :columns="columns" :data="state.tableData" :loading="state.loading">
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
import {reactive} from 'vue'
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
  {title: '时间', key: 'time', minWidth: 160},
  {title: '用户', key: 'operator', minWidth: 120},
  {title: 'IP地址', key: 'ip', minWidth: 140},
  {title: '浏览器', key: 'browser', minWidth: 150},
  {title: '操作系统', key: 'os', minWidth: 120},
  {
    title: '状态',
    key: 'statusName',
    minWidth: 80,
    render: (h, p) => h('span', {class: p.row.status === 1 ? 'text-success' : 'text-error'}, p.row.statusName)
  },
  {title: '操作', slot: 'action', minWidth: 80}
]

const operationColumns = [
  {title: '时间', key: 'time', minWidth: 160},
  {title: '操作人', key: 'operator', minWidth: 120},
  {title: '操作类型', key: 'actionType', minWidth: 120},
  {title: '操作描述', key: 'description', minWidth: 200, tooltip: true},
  {title: 'IP地址', key: 'ip', minWidth: 140},
  {title: '操作', slot: 'action', minWidth: 80}
]

const columns = reactive(loginColumns)

const handleTypeChange = () => {
  if (state.logType === 'login') {
    columns.length = 0
    loginColumns.forEach(c => columns.push(c))
  } else {
    columns.length = 0
    operationColumns.forEach(c => columns.push(c))
  }
  handleSearch()
}

const initData = () => {
  state.loading = true
  setTimeout(() => {
    if (state.logType === 'login') {
      state.tableData = [
        {
          id: 1,
          time: '2024-01-15 14:32:15',
          operator: 'admin',
          ip: '192.168.1.100',
          browser: 'Chrome 120',
          os: 'Windows 11',
          status: 1,
          statusName: '成功'
        },
        {
          id: 2,
          time: '2024-01-15 10:20:00',
          operator: 'finance',
          ip: '192.168.1.101',
          browser: 'Firefox 121',
          os: 'Windows 10',
          status: 1,
          statusName: '成功'
        },
        {
          id: 3,
          time: '2024-01-15 09:15:00',
          operator: 'guard1',
          ip: '192.168.1.102',
          browser: 'Safari 17',
          os: 'macOS',
          status: 1,
          statusName: '成功'
        },
        {
          id: 4,
          time: '2024-01-14 22:00:00',
          operator: 'unknown',
          ip: '192.168.1.103',
          browser: 'Chrome 120',
          os: 'Windows 11',
          status: 0,
          statusName: '失败'
        }
      ]
    } else {
      state.tableData = [
        {
          id: 1,
          time: '2024-01-15 14:30:00',
          operator: 'admin',
          actionType: '退款',
          description: '对订单P20240115001进行退款操作，退款金额¥30',
          ip: '192.168.1.100'
        },
        {
          id: 2,
          time: '2024-01-15 14:00:00',
          operator: 'finance',
          actionType: '开闸',
          description: '远程开闸，车辆：京A12345，通道：1号入口',
          ip: '192.168.1.101'
        },
        {
          id: 3,
          time: '2024-01-15 13:30:00',
          operator: 'admin',
          actionType: '编辑规则',
          description: '修改临时车收费规则，首小时费用调整为¥5',
          ip: '192.168.1.100'
        },
        {
          id: 4,
          time: '2024-01-15 11:00:00',
          operator: 'guard1',
          actionType: '黑名单',
          description: '将车辆京D88888加入黑名单',
          ip: '192.168.1.102'
        }
      ]
    }
    state.pagination.total = 4
    state.loading = false
  }, 500)
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
const handleExport = () => Message.info('导出日志')
const handlePageChange = p => {
  state.pagination.current = p;
  initData()
}
const handleViewDetail = r => {
  state.currentLog = r;
  state.detailVisible = true
}

initData()
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