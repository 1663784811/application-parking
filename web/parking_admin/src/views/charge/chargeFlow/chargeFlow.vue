<template>
  <div class="charge-flow-page">
    <!-- 搜索筛选栏 -->
    <div class="filter-bar">
      <div class="filter-row">
        <div class="filter-item">
          <Input
            v-model="state.searchForm.orderNo"
            placeholder="搜索订单号"
            class="filter-input"
            clearable
            @on-enter="handleSearch"
          >
            <template #prefix>
              <Icon type="ios-search" />
            </template>
          </Input>
        </div>

        <div class="filter-item">
          <Select
            v-model="state.searchForm.orderStatus"
            placeholder="订单状态"
            class="filter-select"
            clearable
          >
            <Option :value="0">待付款</Option>
            <Option :value="2">待发货</Option>
            <Option :value="3">待收货</Option>
            <Option :value="4">已完成</Option>
            <Option :value="5">申请售后</Option>
            <Option :value="6">取消中</Option>
            <Option :value="7">已取消</Option>
          </Select>
        </div>

        <div class="filter-item">
          <Select
            v-model="state.searchForm.payStatus"
            placeholder="支付状态"
            class="filter-select"
            clearable
          >
            <Option :value="0">未支付</Option>
            <Option :value="1">部分支付</Option>
            <Option :value="2">已支付</Option>
            <Option :value="3">部分退款</Option>
            <Option :value="4">全部退款</Option>
          </Select>
        </div>

        <div class="filter-item">
          <DatePicker
            v-model="state.searchForm.dateRange"
            type="daterange"
            placeholder="创建时间"
            class="filter-date"
            format="yyyy-MM-dd"
          />
        </div>

        <div class="filter-item"><Button type="primary" @click="handleSearch">查询</Button></div>
        <div class="filter-item"><Button @click="handleReset">重置</Button></div>
      </div>
    </div>

    <!-- 数据表格 -->
    <div class="table-container">
      <!-- 列设置：表格上方 -->
      <TableColumnSetting
        :columns="columns"
        v-model:visible="visibleFields"
        v-model:open="colSettingVisible"
        @reset="resetColumns"
      />

      <Table
        :columns="displayColumns"
        :data="state.tableData"
        :loading="state.loading"
      >
        <template #orderStatus="{ row }">
          <Badge :status="getOrderStatusBadge(row.orderStatus)" :text="getOrderStatusText(row.orderStatus)" />
        </template>
        <template #action="{ row }">
          <Button type="text" size="small" @click="handleViewOrder(row)">查看</Button>
          <Button type="text" size="small" @click="handleInvoice(row)" v-if="row.payStatus === 2">开发票</Button>
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

    <!-- 订单详情弹窗 -->
    <Modal v-model="state.detailVisible" title="订单详情" width="600" :footer-hide="true">
      <div class="detail-list" v-if="state.detailData.id">
        <div class="detail-row">
          <span class="detail-label">订单号</span>
          <span>{{ state.detailData.orderNo }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">订单状态</span>
          <span>{{ getOrderStatusText(state.detailData.orderStatus) }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">支付状态</span>
          <span>{{ getPayStatusText(state.detailData.payStatus) }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">应付金额</span>
          <span>¥{{ state.detailData.totalAmount }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">优惠金额</span>
          <span>¥{{ state.detailData.discountAmount }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">实付金额</span>
          <span>¥{{ state.detailData.payAmounted }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">支付时间</span>
          <span>{{ state.detailData.payTime || '—' }}</span>
        </div>
        <div class="detail-row">
          <span class="detail-label">备注</span>
          <span>{{ state.detailData.remark || '—' }}</span>
        </div>
      </div>
    </Modal>
  </div>
</template>

<script setup>
import { reactive, onMounted } from 'vue'
import {
  Button,
  Input,
  Select,
  Option,
  DatePicker,
  Icon,
  Table,
  Badge,
  Page,
  Modal,
  Message
} from 'view-ui-plus'
import TableColumnSetting from '@/components/TableColumnSetting.vue'
import { useTableColumns } from '@/composables/useTableColumns'
import { chargeApi } from '@/api'

const state = reactive({
  // 搜索表单
  searchForm: {
    orderNo: '',
    orderStatus: null,
    payStatus: null,
    dateRange: []
  },

  tableData: [],
  loading: false,

  // 订单详情
  detailVisible: false,
  detailData: {},

  pagination: {
    total: 0,
    current: 1,
    pageSize: 10
  }
})

const columns = [
  { field: 'orderNo', title: '订单号', key: 'orderNo', minWidth: 180 },
  { field: 'totalAmount', title: '应付金额', key: 'totalAmount', minWidth: 100, align: 'right' },
  { field: 'payAmounted', title: '实付金额', key: 'payAmounted', minWidth: 100, align: 'right' },
  { field: 'discountAmount', title: '优惠金额', key: 'discountAmount', minWidth: 100, align: 'right' },
  { field: 'payTime', title: '支付时间', key: 'payTime', minWidth: 160 },
  { field: 'orderStatus', title: '订单状态', slot: 'orderStatus', minWidth: 100, align: 'center' },
  { title: '操作', slot: 'action', minWidth: 120, fixed: 'right' }
]

// 表格列设置（显隐 + 持久化到 localStorage）
const {
  visibleFields,
  colSettingVisible,
  displayColumns,
  resetColumns
} = useTableColumns(columns, 'chargeFlow:columnVisible')

const getOrderStatusText = (status) => {
  const map = { 0: '待付款', 2: '待发货', 3: '待收货', 4: '已完成', 5: '申请售后', 6: '取消中', 7: '已取消' }
  return map[status] ?? status
}

const getOrderStatusBadge = (status) => {
  const map = { 0: 'error', 2: 'processing', 3: 'processing', 4: 'success', 5: 'warning', 6: 'warning', 7: 'default' }
  return map[status] ?? 'default'
}

const getPayStatusText = (status) => {
  const map = { 0: '未支付', 1: '部分支付', 2: '已支付', 3: '部分退款', 4: '全部退款' }
  return map[status] ?? status
}

// 归一化日期区间为 [startDate, endDate] 字符串数组（兼容 DatePicker 的 Date 对象/字符串）
const normalizeDateRange = (range) => {
  if (!range || !Array.isArray(range) || range.length < 2) return [null, null]
  const fmt = (v) => {
    if (!v) return null
    if (typeof v === 'string') return v
    if (v instanceof Date) {
      const y = v.getFullYear()
      const m = String(v.getMonth() + 1).padStart(2, '0')
      const d = String(v.getDate()).padStart(2, '0')
      return `${y}-${m}-${d}`
    }
    return null
  }
  return [fmt(range[0]), fmt(range[1])]
}

const initData = async () => {
  state.loading = true
  try {
    const [startDate, endDate] = normalizeDateRange(state.searchForm.dateRange)
    const res = await chargeApi.getOrderList({
      page: state.pagination.current,
      size: state.pagination.pageSize,
      orderNo: state.searchForm.orderNo || undefined,
      orderStatus: state.searchForm.orderStatus,
      payStatus: state.searchForm.payStatus,
      startDate: startDate || undefined,
      endDate: endDate || undefined
    })
    state.tableData = res.data || []
    if (res.result) {
      state.pagination.total = res.result.total || 0
    }
  } catch (e) {
    console.error('获取订单列表失败', e)
  } finally {
    state.loading = false
  }
}

// 搜索（回到第一页）
const handleSearch = () => {
  state.pagination.current = 1
  initData()
}

// 重置
const handleReset = () => {
  state.searchForm = {
    orderNo: '',
    orderStatus: null,
    payStatus: null,
    dateRange: []
  }
  state.pagination.current = 1
  initData()
}

const handleViewOrder = async (row) => {
  try {
    const res = await chargeApi.getOrderDetail(row.id)
    state.detailData = res.data || {}
    state.detailVisible = true
  } catch (e) {
    console.error('获取订单详情失败', e)
  }
}

const handleInvoice = (row) => {
  Message.success('正在开具发票...')
}

const handlePageChange = (page) => {
  state.pagination.current = page
  initData()
}

onMounted(() => {
  initData()
})
</script>

<style lang="less" scoped>
.charge-flow-page {
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
      margin-bottom: var(--spacing-lg);

      .filter-item {
        flex-shrink: 0;
      }

      .filter-input {
        width: 200px;
      }

      .filter-select {
        width: 150px;
      }

      .filter-date {
        width: 260px;
      }
    }
  }

  .table-container {
    background-color: var(--bg-color);
    border-radius: var(--border-radius-base);
    padding: var(--spacing-xl);
    box-shadow: var(--shadow-base);

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
