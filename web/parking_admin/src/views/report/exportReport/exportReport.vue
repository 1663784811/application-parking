<template>
  <div class="export-report-page">
    <div class="filter-bar">
      <div class="filter-row">
        <div class="filter-item">
          <Select v-model="state.reportType" placeholder="报表类型" class="filter-select">
            <Option value="revenue">营收报表</Option>
            <Option value="traffic">车流量报表</Option>
            <Option value="space">车位利用率</Option>
            <Option value="member">月卡营收报表</Option>
          </Select>
        </div>
        <div class="filter-item">
          <DatePicker v-model="state.dateRange" type="daterange" placeholder="时间范围" class="filter-date"/>
        </div>
        <div class="filter-item">
          <Select v-model="state.parkingId" placeholder="停车场" class="filter-select" clearable>
            <Option value="1">全部停车场</Option>
            <Option value="2">城西停车场</Option>
          </Select>
        </div>
        <div class="filter-item"><Button type="primary" @click="handleSearch">查询</Button></div>
      </div>
    </div>
    <div class="export-preview">
      <h3 class="preview-title">报表预览</h3>
      <Table :columns="columns" :data="state.tableData" :loading="state.loading" size="small"></Table>
    </div>
    <div class="export-actions">
      <Button type="primary" size="large" @click="handleExportExcel">
        <Icon type="ios-download-outline"/>
        导出Excel
      </Button>
      <Button size="large" @click="handleExportPdf">
        <Icon type="ios-document-outline"/>
        导出PDF
      </Button>
    </div>
  </div>
</template>

<script setup>
import {reactive} from 'vue'
import {Button, DatePicker, Icon, Message, Option, Select, Table} from 'view-ui-plus'

const state = reactive({
  reportType: 'revenue',
  dateRange: [],
  parkingId: null,
  tableData: [],
  loading: false
})

const columns = [
  {title: '日期', key: 'date', minWidth: 120},
  {title: '停车场', key: 'parking', minWidth: 150},
  {title: '临时收入', key: 'tempIncome', minWidth: 120, align: 'right'},
  {title: '月卡收入', key: 'memberIncome', minWidth: 120, align: 'right'},
  {title: '优惠减免', key: 'discount', minWidth: 100, align: 'right'},
  {title: '实收金额', key: 'actualAmount', minWidth: 120, align: 'right'},
  {title: '订单数', key: 'orderCount', minWidth: 80, align: 'center'}
]

const initData = () => {
  state.loading = true
  setTimeout(() => {
    state.tableData = [
      {
        date: '2024-01-15',
        parking: '城西停车场',
        tempIncome: '8,450',
        memberIncome: '2,100',
        discount: '420',
        actualAmount: '10,130',
        orderCount: 228
      },
      {
        date: '2024-01-14',
        parking: '城西停车场',
        tempIncome: '7,890',
        memberIncome: '2,100',
        discount: '380',
        actualAmount: '9,610',
        orderCount: 215
      },
      {
        date: '2024-01-13',
        parking: '城东停车场',
        tempIncome: '5,680',
        memberIncome: '1,500',
        discount: '260',
        actualAmount: '6,920',
        orderCount: 165
      }
    ]
    state.loading = false
  }, 500)
}

const handleSearch = () => initData()
const handleExportExcel = () => Message.success('Excel导出成功')
const handleExportPdf = () => Message.success('PDF导出成功')

initData()
</script>

<style lang="less" scoped>
.export-report-page {
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

      .filter-item {
        flex-shrink: 0;
      }

      .filter-select {
        width: 180px;
      }

      .filter-date {
        width: 260px;
      }
    }
  }

  .export-preview {
    background: var(--bg-color);
    border-radius: var(--border-radius-base);
    box-shadow: var(--shadow-base);
    padding: var(--spacing-xl);
    margin-bottom: var(--spacing-lg);

    .preview-title {
      font-size: var(--font-size-md);
      font-weight: 600;
      margin-bottom: var(--spacing-lg);
    }
  }

  .export-actions {
    display: flex;
    justify-content: center;
    gap: var(--spacing-xl);

    .ivu-btn {
      display: inline-flex;
      align-items: center;

      .ivu-icon {
        margin-right: var(--spacing-sm);
      }
    }
  }
}
</style>