<template>
  <div class="passage-record-page">
    <!-- 筛选栏 -->
    <div class="filter-bar">
      <div class="filter-row">
        <div class="filter-item">
          <Select v-model="state.searchForm.parkingId" placeholder="选择停车场" class="filter-select" clearable>
            <Option v-for="item in state.parkingList" :key="item.id" :value="item.id">
              {{ item.name }}
            </Option>
          </Select>
        </div>

        <div class="filter-item">
          <Input
            v-model="state.searchForm.plate"
            placeholder="输入车牌号"
            class="filter-input"
            clearable
            @on-enter="handleSearch"
          />
        </div>
      </div>

      <div class="filter-row">
        <div class="filter-item"><Button type="primary" @click="handleSearch">查询</Button></div>
        <div class="filter-item"><Button @click="handleReset">重置</Button></div>
        <div class="filter-item">
          <Button @click="handleExport">
            <Icon type="ios-download-outline" />
            导出Excel
          </Button>
        </div>
        <div class="filter-item">
          <Button @click="handlePrint">
            <Icon type="ios-print-outline" />
            批量打印小票
          </Button>
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
        <template #status="{ row }">
          <span class="status-tag" :class="getStatusClass(row.status)">
            {{ getStatusText(row.status) }}
          </span>
        </template>
        <template #action="{ row }">
          <Button type="text" size="small" @click="handleViewDetail(row)">查看记录</Button>
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

    <!-- 记录详情弹窗 -->
    <Modal v-model="state.detailVisible" title="通行记录详情" width="560" :footer-hide="true">
      <div class="detail-list" v-if="state.detailData.id">
        <div class="detail-row">
          <span class="detail-label">车牌号</span>
          <span>{{ state.detailData.carNumber }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">车辆类型</span>
          <span>{{ state.detailData.carType || '—' }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">进场时间</span>
          <span>{{ state.detailData.entryTime }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">出场时间</span>
          <span>{{ state.detailData.outTime || '—' }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">状态</span>
          <span>{{ getStatusText(state.detailData.status) }}</span>
        </div>
      </div>
    </Modal>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import {
  Select,
  Option,
  Input,
  Button,
  Icon,
  Table,
  Page,
  Modal,
  Message
} from 'view-ui-plus'
import { parkingApi, passageApi } from '@/api'

const state = reactive({
  // 搜索表单
  searchForm: {
    parkingId: null,
    plate: ''
  },

  parkingList: [],

  tableData: [],
  loading: false,
  selectedRows: [],

  // 记录详情
  detailVisible: false,
  detailData: {},

  pagination: {
    total: 0,
    current: 1,
    pageSize: 10
  }
})

// 表格列定义
const columns = [
  { type: 'selection', width: 80, align: 'center' },
  { title: '车牌号', key: 'carNumber', minWidth: 120 },
  { title: '车辆类型', key: 'carType', minWidth: 100 },
  { title: '进场时间', key: 'entryTime', minWidth: 160 },
  { title: '出场时间', key: 'outTime', minWidth: 160 },
  { title: '状态', slot: 'status', minWidth: 100, align: 'center' },
  { title: '操作', slot: 'action', minWidth: 120, fixed: 'right' }
]

const getStatusText = (status) => {
  const map = { 0: '场内', 1: '已出场' }
  return map[status] ?? status
}

const getStatusClass = (status) => {
  const map = { 0: 'success', 1: 'default' }
  return map[status] ?? 'default'
}

// 初始化数据
const initData = async () => {
  state.loading = true
  try {
    const res = await passageApi.getRecordList({
      page: state.pagination.current,
      size: state.pagination.pageSize,
      parkingId: state.searchForm.parkingId,
      carNumber: state.searchForm.plate || undefined
    })
    state.tableData = res.data || []
    if (res.result) {
      state.pagination.total = res.result.total || 0
    }
  } catch (e) {
    console.error('获取通行记录失败', e)
  } finally {
    state.loading = false
  }
}

// 加载停车场列表
const loadParkingList = async () => {
  try {
    // size 取较大值以一次性加载全部停车场供选择器使用
    const res = await parkingApi.getParkingList({ size: 1000 })
    state.parkingList = res.data || []
  } catch (e) {
    console.error('获取停车场列表失败', e)
  }
}

// 搜索
const handleSearch = () => {
  state.pagination.current = 1
  initData()
}

// 重置
const handleReset = () => {
  state.searchForm = {
    parkingId: null,
    plate: ''
  }
  handleSearch()
}

// 批量选择
const handleSelectionChange = (selection) => {
  state.selectedRows = selection
}

// 导出
const handleExport = () => {
  Message.info('正在导出...')
}

// 打印
const handlePrint = () => {
  Message.info('正在打印...')
}

// 查看记录详情
const handleViewDetail = async (row) => {
  try {
    const res = await passageApi.getRecordDetail(row.id)
    state.detailData = res.data || {}
    state.detailVisible = true
  } catch (e) {
    console.error('获取通行记录详情失败', e)
  }
}

// 分页
const handlePageChange = (page) => {
  state.pagination.current = page
  initData()
}

// 初始化
onMounted(() => {
  loadParkingList()
  initData()
})
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

      .filter-item {
        flex-shrink: 0;
      }

      .filter-select {
        width: 150px;
      }

      .filter-input {
        width: 180px;
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

      &.default {
        background-color: rgba(134, 144, 156, 0.1);
        color: var(--text-color-secondary);
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

    .detail-list {
      .detail-row {
        display: flex;
        padding: var(--spacing-sm) 0;
        border-bottom: 1px solid var(--border-color-light);

        &:last-child {
          border-bottom: none;
        }
      }

      .detail-label {
        width: 100px;
        flex-shrink: 0;
        color: var(--text-color-secondary);
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
