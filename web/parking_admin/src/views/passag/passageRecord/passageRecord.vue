<template>
  <div class="passage-record-page">
    <!-- 筛选栏 -->
    <div class="filter-bar">
      <div class="filter-row">
        <RangePicker
          v-model="state.searchForm.dateRange"
          :show-time="true"
          format="YYYY-MM-DD HH:mm:ss"
          placeholder="通行时间"
          class="filter-date"
        />

        <Select v-model="state.searchForm.parkingId" placeholder="选择车场" class="filter-select" clearable>
          <Option v-for="item in state.parkingList" :key="item.id" :value="item.id">
            {{ item.name }}
          </Option>
        </Select>

        <Select v-model="state.searchForm.passageType" placeholder="通行类型" class="filter-select" clearable>
          <Option value="in">进场</Option>
          <Option value="out">出场</Option>
        </Select>

        <Select v-model="state.searchForm.carType" placeholder="车辆类型" class="filter-select" clearable>
          <Option value="temp">临时车</Option>
          <Option value="fixed">固定车</Option>
        </Select>

        <Input
          v-model="state.searchForm.plate"
          placeholder="输入车牌号"
          class="filter-input"
          clearable
        />
      </div>

      <div class="filter-row">
        <Select v-model="state.searchForm.hasPaid" placeholder="是否缴费" class="filter-select" clearable>
          <Option :value="1">已缴费</Option>
          <Option :value="0">未缴费</Option>
        </Select>

        <Select v-model="state.searchForm.hasException" placeholder="有无异常" class="filter-select" clearable>
          <Option :value="1">有异常</Option>
          <Option :value="0">正常</Option>
        </Select>

        <Select v-model="state.searchForm.channelId" placeholder="通道" class="filter-select" clearable>
          <Option value="1">1号入口</Option>
          <Option value="2">2号出口</Option>
          <Option value="3">地下入口</Option>
        </Select>

        <Button type="primary" @click="handleSearch">查询</Button>
        <Button @click="handleReset">重置</Button>
      </div>

      <div class="filter-actions">
        <Button @click="handleExport">
          <Icon type="ios-download-outline" />
          导出Excel
        </Button>
        <Button @click="handlePrint">
          <Icon type="ios-print-outline" />
          批量打印小票
        </Button>
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
        <template #passageType="{ row }">
          <Tag :color="row.passageType === 'in' ? 'blue' : 'green'">
            {{ row.passageType === 'in' ? '进场' : '出场' }}
          </Tag>
        </template>
        <template #carType="{ row }">
          <span>{{ getCarTypeText(row.carType) }}</span>
        </template>
        <template #hasPaid="{ row }">
          <Badge status="success" text="已缴" v-if="row.hasPaid" />
          <Badge status="error" text="未缴" v-else />
        </template>
        <template #status="{ row }">
          <span class="status-tag" :class="getStatusClass(row.status)">
            {{ getStatusText(row.status) }}
          </span>
        </template>
        <template #action="{ row }">
          <Button type="text" size="small" @click="handleViewDetail(row)">查看记录</Button>
          <Button type="text" size="small" @click="handlePay(row)" v-if="!row.hasPaid">补费</Button>
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
  RangePicker,
  Select,
  Option,
  Input,
  Button,
  Icon,
  Table,
  Tag,
  Badge,
  Page,
  Modal,
  Message
} from 'view-ui-plus'

const state = reactive({
  searchForm: {
    dateRange: [],
    parkingId: null,
    passageType: null,
    carType: null,
    plate: '',
    hasPaid: null,
    hasException: null,
    channelId: null
  },

  parkingList: [
    { id: 1, name: '城西停车场' },
    { id: 2, name: '城东停车场' }
  ],

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
  { type: 'selection', width: 60, align: 'center' },
  { title: '抓拍图', key: 'captureImage', width: 100, align: 'center' },
  { title: '车牌号', key: 'plate', width: 120 },
  { title: '通道', key: 'channel', width: 100 },
  { title: '通行类型', slot: 'passageType', width: 100 },
  { title: '进场时间', key: 'inTime', width: 160 },
  { title: '出场时间', key: 'outTime', width: 160 },
  { title: '停留时长', key: 'duration', width: 100 },
  { title: '车辆类型', slot: 'carType', width: 100 },
  { title: '收费金额', key: 'amount', width: 100, align: 'right' },
  { title: '是否缴费', slot: 'hasPaid', width: 100, align: 'center' },
  { title: '状态', slot: 'status', width: 100, align: 'center' },
  { title: '操作', slot: 'action', width: 150, fixed: 'right' }
]

const getCarTypeText = (type) => {
  const map = { temp: '临时车', fixed: '固定车' }
  return map[type] || type
}

const getStatusText = (status) => {
  const map = {
    normal: '正常',
    unpaid: '欠费',
    noPlate: '无牌',
    blacklist: '黑名单'
  }
  return map[status] || status
}

const getStatusClass = (status) => {
  const map = {
    normal: 'success',
    unpaid: 'error',
    noPlate: 'warning',
    blacklist: 'error'
  }
  return map[status] || 'default'
}

const initData = () => {
  state.loading = true
  setTimeout(() => {
    state.tableData = [
      {
        id: 1,
        plate: '京A12345',
        channel: '1号入口',
        passageType: 'in',
        inTime: '2024-01-15 09:00:00',
        outTime: '-',
        duration: '-',
        carType: 'temp',
        amount: 0,
        hasPaid: false,
        status: 'normal'
      },
      {
        id: 2,
        plate: '京B67890',
        channel: '2号出口',
        passageType: 'out',
        inTime: '2024-01-15 08:00:00',
        outTime: '2024-01-15 10:30:00',
        duration: '2小时30分',
        carType: 'temp',
        amount: 15,
        hasPaid: true,
        status: 'normal'
      },
      {
        id: 3,
        plate: '京C11111',
        channel: '1号入口',
        passageType: 'in',
        inTime: '2024-01-15 07:00:00',
        outTime: '-',
        duration: '-',
        carType: 'fixed',
        amount: 0,
        hasPaid: true,
        status: 'normal'
      },
      {
        id: 4,
        plate: '无牌车',
        channel: '3号入口',
        passageType: 'in',
        inTime: '2024-01-15 09:30:00',
        outTime: '-',
        duration: '-',
        carType: 'temp',
        amount: 0,
        hasPaid: false,
        status: 'noPlate'
      },
      {
        id: 5,
        plate: '浙D22222',
        channel: '2号出口',
        passageType: 'out',
        inTime: '2024-01-15 10:00:00',
        outTime: '2024-01-15 14:00:00',
        duration: '4小时',
        carType: 'temp',
        amount: 30,
        hasPaid: false,
        status: 'unpaid'
      }
    ]
    state.pagination.total = 5
    state.loading = false
  }, 500)
}

const handleSearch = () => {
  state.pagination.current = 1
  initData()
}

const handleReset = () => {
  state.searchForm = {
    dateRange: [],
    parkingId: null,
    passageType: null,
    carType: null,
    plate: '',
    hasPaid: null,
    hasException: null,
    channelId: null
  }
  handleSearch()
}

const handleSelectionChange = (selection) => {
  state.selectedRows = selection
}

const handleExport = () => {
  Message.info('正在导出...')
}

const handlePrint = () => {
  Message.info('正在打印...')
}

const handleViewDetail = (row) => {
  console.log('查看详情', row)
}

const handlePay = (row) => {
  Modal.confirm({
    title: '确认补缴',
    content: `确认收取车辆 "${row.plate}" 停车费用 ¥${row.amount}？`,
    onOk: () => {
      Message.success('补缴成功')
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
.passage-record-page {
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

      &:last-of-type {
        margin-bottom: var(--spacing-lg);
      }

      .filter-date {
        width: 320px;
      }

      .filter-select {
        width: 150px;
      }

      .filter-input {
        width: 180px;
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

  .table-container {
    background-color: var(--bg-color);
    border-radius: var(--border-radius-base);
    padding: var(--spacing-xl);
    box-shadow: var(--shadow-base);

    .status-tag {
      padding: 2px 8px;
      border-radius: var(--border-radius-sm);
      font-size: var(--font-size-xs);

      &.success {
        background-color: rgba(0, 180, 42, 0.1);
        color: var(--success-color);
      }

      &.warning {
        background-color: rgba(255, 125, 0, 0.1);
        color: var(--warning-color);
      }

      &.error {
        background-color: rgba(245, 63, 63, 0.1);
        color: var(--error-color);
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