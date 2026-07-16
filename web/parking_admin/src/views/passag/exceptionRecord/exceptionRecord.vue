<template>
  <div class="exception-record-page">
    <!-- 筛选栏 -->
    <div class="filter-bar">
      <div class="filter-row">
        <div class="filter-item">
          <Select v-model="state.searchForm.exceptionType" placeholder="异常类型" class="filter-select" clearable>
            <Option value="noPlate">无牌车辆</Option>
            <Option value="blacklist">黑名单</Option>
            <Option value="overtime">超时滞留</Option>
            <Option value="unpaid">逃费车辆</Option>
          </Select>
        </div>

        <div class="filter-item">
          <DatePicker
            v-model="state.searchForm.dateRange"
            type="daterange"
            placeholder="发生时间"
            class="filter-date"
          />
        </div>

        <div class="filter-item">
          <Select v-model="state.searchForm.parkingId" placeholder="车场" class="filter-select" clearable>
            <Option value="1">城西停车场</Option>
            <Option value="2">城东停车场</Option>
          </Select>
        </div>

        <div class="filter-item"><Button type="primary" @click="handleSearch">查询</Button></div>
        <div class="filter-item"><Button @click="handleReset">重置</Button></div>
      </div>

      <div class="filter-actions">
        <Button type="primary" @click="handleBatchBlacklist" :disabled="state.selectedRows.length === 0">
          <Icon type="ios-close-circle-outline" />
          批量加入黑名单
        </Button>
        <Button @click="handleBatchSms" :disabled="state.selectedRows.length === 0">
          <Icon type="ios-mail-outline" />
          批量短信通知
        </Button>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-card" @click="state.searchForm.exceptionType = 'noPlate'; handleSearch()">
        <div class="stat-icon no-plate">
          <Icon type="ios-help-circle-outline" />
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ state.stats.noPlate }}</span>
          <span class="stat-label">无牌车辆</span>
        </div>
      </div>
      <div class="stat-card" @click="state.searchForm.exceptionType = 'blacklist'; handleSearch()">
        <div class="stat-icon blacklist">
          <Icon type="ios-close-circle-outline" />
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ state.stats.blacklist }}</span>
          <span class="stat-label">黑名单车辆</span>
        </div>
      </div>
      <div class="stat-card" @click="state.searchForm.exceptionType = 'overtime'; handleSearch()">
        <div class="stat-icon overtime">
          <Icon type="ios-time-outline" />
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ state.stats.overtime }}</span>
          <span class="stat-label">超时滞留</span>
        </div>
      </div>
      <div class="stat-card" @click="state.searchForm.exceptionType = 'unpaid'; handleSearch()">
        <div class="stat-icon unpaid">
          <Icon type="ios-card-outline" />
        </div>
        <div class="stat-info">
          <span class="stat-value">{{ state.stats.unpaid }}</span>
          <span class="stat-label">逃费车辆</span>
        </div>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="table-container">
      <Table
        :columns="columns"
        :data="state.tableData"
        :loading="state.loading"
        :selection="true"
        @on-selection-change="handleSelectionChange"
      >
        <template #exceptionType="{ row }">
          <span class="status-tag" :class="'exception-' + row.exceptionType">
            {{ getExceptionTypeText(row.exceptionType) }}
          </span>
        </template>
        <template #status="{ row }">
          <span :class="row.status === 'pending' ? 'text-error' : 'text-success'">
            {{ row.status === 'pending' ? '待处理' : '已处理' }}
          </span>
        </template>
        <template #action="{ row }">
          <Button type="text" size="small" @click="handleViewDetail(row)">详情</Button>
          <Button type="text" size="small" @click="handleAddBlacklist(row)" v-if="row.exceptionType !== 'blacklist'">
            加黑名单
          </Button>
          <Button type="text" size="small" @click="handleSendSms(row)">发送短信</Button>
          <Button type="text" size="small" @click="handleProcessed(row)" v-if="row.status === 'pending'">
            标记已处理
          </Button>
        </template>
      </Table>

      <div class="pagination-wrapper">
        <Page
          :total="state.pagination.total"
          :current="state.pagination.current"
          :page-size="state.pagination.pageSize"
          show-total
          show-elevator
          @on-change="handlePageChange"
        />
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import {
  Select,
  Option,
  DatePicker,
  Button,
  Icon,
  Table,
  Tag,
  Page,
  Modal,
  Message
} from 'view-ui-plus'

const state = reactive({
  searchForm: {
    exceptionType: null,
    dateRange: [],
    parkingId: null
  },

  stats: {
    noPlate: 12,
    blacklist: 8,
    overtime: 15,
    unpaid: 6
  },

  tableData: [],
  loading: false,
  selectedRows: [],

  pagination: {
    total: 0,
    current: 1,
    pageSize: 10
  }
})

const columns = [
  { type: 'selection', minWidth: 60, align: 'center' },
  { title: '车牌号', key: 'plate', minWidth: 120 },
  { title: '异常类型', slot: 'exceptionType', minWidth: 120 },
  { title: '发生时间', key: 'exceptionTime', minWidth: 160 },
  { title: '车场', key: 'parkingName', minWidth: 150 },
  { title: '通道', key: 'channel', minWidth: 100 },
  { title: '异常描述', key: 'description', minWidth: 200, tooltip: true },
  { title: '待缴费金额', key: 'unpaidAmount', minWidth: 120, align: 'right' },
  { title: '处理状态', slot: 'status', minWidth: 100, align: 'center' },
  { title: '操作', slot: 'action', minWidth: 250, fixed: 'right' }
]

const getExceptionTypeText = (type) => {
  const map = {
    noPlate: '无牌车辆',
    blacklist: '黑名单',
    overtime: '超时滞留',
    unpaid: '逃费车辆'
  }
  return map[type] || type
}

const initData = () => {
  state.loading = true
  setTimeout(() => {
    state.tableData = [
      {
        id: 1,
        plate: '无牌车-A1',
        exceptionType: 'noPlate',
        exceptionTime: '2024-01-15 14:30:00',
        parkingName: '城西停车场',
        channel: '1号入口',
        description: '车辆无牌照，疑似恶意闯入',
        unpaidAmount: 0,
        status: 'pending'
      },
      {
        id: 2,
        plate: '浙C88888',
        exceptionType: 'blacklist',
        exceptionTime: '2024-01-15 14:20:00',
        parkingName: '城东停车场',
        channel: '2号出口',
        description: '黑名单车辆强行闯出',
        unpaidAmount: 50,
        status: 'pending'
      },
      {
        id: 3,
        plate: '京A12345',
        exceptionType: 'overtime',
        exceptionTime: '2024-01-15 10:00:00',
        parkingName: '城西停车场',
        channel: '地下入口',
        description: '车辆停留已超过48小时',
        unpaidAmount: 180,
        status: 'handled'
      },
      {
        id: 4,
        plate: '京B11111',
        exceptionType: 'unpaid',
        exceptionTime: '2024-01-15 09:30:00',
        parkingName: '购物中心停车场',
        channel: '1号入口',
        description: '多次逃费记录',
        unpaidAmount: 320,
        status: 'pending'
      }
    ]
    state.pagination.total = 4
    state.loading = false
  }, 500)
}

const handleSearch = () => {
  state.pagination.current = 1
  initData()
}

const handleReset = () => {
  state.searchForm = {
    exceptionType: null,
    dateRange: [],
    parkingId: null
  }
  handleSearch()
}

const handleSelectionChange = (selection) => {
  state.selectedRows = selection
}

const handleBatchBlacklist = () => {
  Modal.confirm({
    title: '确认批量加入黑名单',
    content: `确定要将 ${state.selectedRows.length} 辆车加入黑名单吗？`,
    onOk: () => {
      Message.success('批量加入黑名单成功')
      initData()
    }
  })
}

const handleBatchSms = () => {
  Modal.confirm({
    title: '确认批量发送短信',
    content: `确定要向 ${state.selectedRows.length} 位车主发送短信通知吗？`,
    onOk: () => {
      Message.success('短信发送成功')
    }
  })
}

const handleViewDetail = (row) => {
  console.log('查看详情', row)
}

const handleAddBlacklist = (row) => {
  Modal.confirm({
    title: '确认加入黑名单',
    content: `确定要将 "${row.plate}" 加入黑名单吗？`,
    onOk: () => {
      Message.success('加入黑名单成功')
      initData()
    }
  })
}

const handleSendSms = (row) => {
  Message.success(`已向 ${row.plate} 发送短信通知`)
}

const handleProcessed = (row) => {
  Modal.confirm({
    title: '确认标记已处理',
    content: `确定将 "${row.plate}" 的异常记录标记为已处理吗？`,
    onOk: () => {
      Message.success('处理成功')
      initData()
    }
  })
}

const handlePageChange = (page) => {
  state.pagination.current = page
  initData()
}

initData()
</script>

<style lang="less" scoped>
.exception-record-page {
  .filter-bar {
    padding: var(--spacing-xl);
    background-color: var(--bg-color);
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
    }

    .filter-actions {
      display: flex;
      gap: var(--spacing-md);

      .ivu-btn {
        display: inline-flex;
        align-items: center;

        .ivu-icon {
          margin-right: 4px;
        }
      }
    }
  }

  .stats-row {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: var(--spacing-lg);
    margin-bottom: var(--spacing-lg);

    .stat-card {
      display: flex;
      align-items: center;
      padding: var(--spacing-xl);
      background-color: var(--bg-color);
      border-radius: var(--border-radius-base);
      box-shadow: var(--shadow-base);
      cursor: pointer;
      transition: transform 0.2s;

      &:hover {
        transform: translateY(-2px);
      }

      .stat-icon {
        minWidth: 48px;
        height: 48px;
        border-radius: var(--border-radius-base);
        display: flex;
        align-items: center;
        justify-content: center;
        margin-right: var(--spacing-lg);

        .ivu-icon {
          font-size: 24px;
          color: #fff;
        }

        &.no-plate {
          background: linear-gradient(135deg, #FF7D00, #FF9A3C);
        }

        &.blacklist {
          background: linear-gradient(135deg, #F53F3F, #FF7875);
        }

        &.overtime {
          background: linear-gradient(135deg, #722ED1, #9254DE);
        }

        &.unpaid {
          background: linear-gradient(135deg, #165DFF, #4080FF);
        }
      }

      .stat-info {
        display: flex;
        flex-direction: column;

        .stat-value {
          font-size: 24px;
          font-weight: 600;
          color: var(--text-color-title);
        }

        .stat-label {
          font-size: var(--font-size-sm);
          color: var(--text-color-secondary);
        }
      }
    }
  }

  .table-container {
    background-color: var(--bg-color);
    border-radius: var(--border-radius-base);
    padding: var(--spacing-xl);
    box-shadow: var(--shadow-base);

    .status-tag {
      padding: 2px 8px;
      border-radius: var(--border-radius-sm);
      font-size: var(--font-size-xs);

      &.exception-noPlate {
        background-color: rgba(255, 125, 0, 0.1);
        color: #FF7D00;
      }

      &.exception-blacklist {
        background-color: rgba(245, 63, 63, 0.1);
        color: #F53F3F;
      }

      &.exception-overtime {
        background-color: rgba(114, 46, 209, 0.1);
        color: #722ED1;
      }

      &.exception-unpaid {
        background-color: rgba(22, 93, 255, 0.1);
        color: #165DFF;
      }
    }

    .pagination-wrapper {
      display: flex;
      justify-content: flex-end;
      margin-top: var(--spacing-xl);
    }
  }
}
</style>