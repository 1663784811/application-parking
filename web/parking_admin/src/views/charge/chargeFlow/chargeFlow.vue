<template>
  <div class="charge-flow-page">
    <!-- 数据表格 -->
    <div class="table-container">
      <Table
        :columns="columns"
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
  Table,
  Badge,
  Page,
  Modal,
  Message
} from 'view-ui-plus'
import { chargeApi } from '@/api'

const state = reactive({
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
  { title: '订单号', key: 'orderNo', minWidth: 180 },
  { title: '应付金额', key: 'totalAmount', minWidth: 100, align: 'right' },
  { title: '实付金额', key: 'payAmounted', minWidth: 100, align: 'right' },
  { title: '优惠金额', key: 'discountAmount', minWidth: 100, align: 'right' },
  { title: '支付时间', key: 'payTime', minWidth: 160 },
  { title: '订单状态', slot: 'orderStatus', minWidth: 100, align: 'center' },
  { title: '操作', slot: 'action', minWidth: 120, fixed: 'right' }
]

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

const initData = async () => {
  state.loading = true
  try {
    const res = await chargeApi.getOrderList({
      page: state.pagination.current,
      size: state.pagination.pageSize
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
