<template>
  <div class="space-management-page">
    <!-- 筛选栏 -->
    <div class="filter-bar">
      <div class="filter-row">
        <div class="filter-item">
          <Select
            v-model="state.searchForm.parkingId"
            placeholder="选择停车场"
            class="filter-select"
            clearable
          >
            <Option v-for="item in state.parkingList" :key="item.id" :value="item.id">
              {{ item.name }}
            </Option>
          </Select>
        </div>

        <div class="filter-item">
          <Select
            v-model="state.searchForm.status"
            placeholder="车位状态"
            class="filter-select"
            clearable
          >
            <Option :value="0">空闲</Option>
            <Option :value="1">占用</Option>
            <Option :value="2">故障</Option>
          </Select>
        </div>

        <div class="filter-item">
          <Select
            v-model="state.searchForm.type"
            placeholder="车位类型"
            class="filter-select"
            clearable
          >
            <Option :value="1">固定</Option>
            <Option :value="2">临时</Option>
            <Option :value="3">无障碍</Option>
          </Select>
        </div>

        <div class="filter-item">
          <Input
            v-model="state.searchForm.keyword"
            placeholder="搜索车位编号"
            class="filter-input"
            clearable
            @on-enter="handleSearch"
          >
            <template #prefix>
              <Icon type="ios-search" />
            </template>
          </Input>
        </div>

        <div class="filter-item"><Button type="primary" @click="handleSearch">查询</Button></div>
        <div class="filter-item"><Button @click="handleReset">重置</Button></div>
      </div>

      <div class="filter-actions">
        <div class="view-switch">
          <Button
            :type="state.viewMode === 'table' ? 'primary' : 'default'"
            @click="state.viewMode = 'table'"
          >
            <Icon type="ios-list" />
            表格视图
          </Button>
          <Button
            :type="state.viewMode === 'grid' ? 'primary' : 'default'"
            @click="state.viewMode = 'grid'"
          >
            <Icon type="ios-grid" />
            平面图视图
          </Button>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-item stat-free">
        <span class="stat-dot"></span>
        <span class="stat-label">空闲</span>
        <span class="stat-value">{{ state.stats.free }}</span>
      </div>
      <div class="stat-item stat-fixed">
        <span class="stat-dot"></span>
        <span class="stat-label">固定车占用</span>
        <span class="stat-value">{{ state.stats.fixed }}</span>
      </div>
      <div class="stat-item stat-temp">
        <span class="stat-dot"></span>
        <span class="stat-label">临时车占用</span>
        <span class="stat-value">{{ state.stats.temp }}</span>
      </div>
      <div class="stat-item stat-fault">
        <span class="stat-dot"></span>
        <span class="stat-label">故障</span>
        <span class="stat-value">{{ state.stats.fault }}</span>
      </div>
    </div>

    <!-- 表格视图 -->
    <div class="table-container" v-show="state.viewMode === 'table'">
      <Table
        :columns="columns"
        :data="state.tableData"
        :loading="state.loading"
      >
        <template #status="{ row }">
          <span class="status-tag" :class="'status-' + getStatusClass(row.status)">
            {{ getStatusText(row.status) }}
          </span>
        </template>
        <template #type="{ row }">
          <span>{{ getTypeText(row.type) }}</span>
        </template>
        <template #action="{ row }">
          <Button type="text" size="small" @click="handleAssign(row)" v-if="row.status === 0">
            分配
          </Button>
          <Button type="text" size="small" @click="handleUnbind(row)" v-if="row.status === 1">
            解绑
          </Button>
          <Button type="text" size="small" @click="handleReportRepair(row)" v-if="row.status !== 2">
            报修
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

    <!-- 平面图视图 -->
    <div class="grid-container" v-show="state.viewMode === 'grid'">
      <div class="parking-grid">
        <div
          v-for="space in state.gridData"
          :key="space.no"
          class="space-grid-item"
          :class="'space-' + getStatusClass(space.status)"
          @click="handleSpaceClick(space)"
          :title="getSpaceTitle(space)"
        >
          <span class="space-no">{{ space.no }}</span>
          <span class="space-plate" v-if="space.plate">{{ space.plate }}</span>
        </div>
      </div>
    </div>

    <!-- 分配弹窗 -->
    <Modal v-model="state.assignModalVisible" title="分配车位" width="400">
      <Form :model="state.assignForm" :label-width="100">
        <FormItem label="车位编号">
          <Input :value="state.currentSpace?.no" disabled />
        </FormItem>
        <FormItem label="绑定车主" prop="memberId">
          <Select v-model="state.assignForm.memberId" placeholder="请选择车主">
            <Option v-for="item in state.memberList" :key="item.id" :value="item.id">
              {{ item.name }} - {{ item.plate }}
            </Option>
          </Select>
        </FormItem>
        <FormItem label="有效期至">
          <DatePicker
            v-model="state.assignForm.expireDate"
            type="date"
            placeholder="请选择有效期"
            style="min-width: 100%"
          />
        </FormItem>
      </Form>
      <template #footer>
        <Button @click="state.assignModalVisible = false">取消</Button>
        <Button type="primary" @click="handleAssignSubmit">确定</Button>
      </template>
    </Modal>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRoute } from 'vue-router'
import {
  Select,
  Option,
  Input,
  Button,
  Icon,
  Table,
  Page,
  Modal,
  Form,
  FormItem,
  DatePicker,
  Message
} from 'view-ui-plus'

const route = useRoute()

const state = reactive({
  searchForm: {
    parkingId: null,
    status: null,
    type: null,
    keyword: ''
  },

  viewMode: 'table',
  loading: false,
  tableData: [],
  gridData: [],

  stats: {
    free: 156,
    fixed: 98,
    temp: 38,
    fault: 8
  },

  parkingList: [
    { id: 1, name: '城西停车场' },
    { id: 2, name: '城东停车场' },
    { id: 3, name: '购物中心停车场' }
  ],

  memberList: [
    { id: 1, name: '张三', plate: '京A12345' },
    { id: 2, name: '李四', plate: '京B67890' },
    { id: 3, name: '王五', plate: '京C11111' }
  ],

  pagination: {
    total: 0,
    current: 1,
    pageSize: 10
  },

  assignModalVisible: false,
  currentSpace: null,
  assignForm: {
    memberId: null,
    expireDate: ''
  }
})

const columns = [
  {
    title: '车位编号',
    key: 'no',
    minWidth: 120
  },
  {
    title: '所属区域',
    key: 'area',
    minWidth: 120
  },
  {
    title: '车位类型',
    slot: 'type',
    minWidth: 100
  },
  {
    title: '绑定车主',
    key: 'memberName',
    minWidth: 120
  },
  {
    title: '当前车辆',
    key: 'plate',
    minWidth: 120
  },
  {
    title: '状态',
    slot: 'status',
    minWidth: 100
  },
  {
    title: '操作',
    slot: 'action',
    minWidth: 180
  }
]

const getStatusClass = (status) => {
  const map = { 0: 'free', 1: 'occupied', 2: 'fault' }
  return map[status] || 'free'
}

const getStatusText = (status) => {
  const map = { 0: '空闲', 1: '占用', 2: '故障' }
  return map[status] || '-'
}

const getTypeText = (type) => {
  const map = { 1: '固定', 2: '临时', 3: '无障碍' }
  return map[type] || '-'
}

const getSpaceTitle = (space) => {
  if (space.plate) {
    return `${space.no} - ${space.plate}`
  }
  return `${space.no} - ${getStatusText(space.status)}`
}

const initData = () => {
  state.loading = true
  setTimeout(() => {
    state.tableData = [
      { id: 1, no: 'A001', area: 'A区', type: 1, memberName: '张三', plate: '京A12345', status: 1 },
      { id: 2, no: 'A002', area: 'A区', type: 1, memberName: '李四', plate: '京B67890', status: 1 },
      { id: 3, no: 'A003', area: 'A区', type: 2, memberName: '-', plate: '京C11111', status: 1 },
      { id: 4, no: 'A004', area: 'A区', type: 2, memberName: '-', plate: '', status: 0 },
      { id: 5, no: 'A005', area: 'A区', type: 3, memberName: '-', plate: '', status: 2 },
      { id: 6, no: 'B001', area: 'B区', type: 1, memberName: '王五', plate: '京D22222', status: 1 },
      { id: 7, no: 'B002', area: 'B区', type: 2, memberName: '-', plate: '', status: 0 },
      { id: 8, no: 'B003', area: 'B区', type: 2, memberName: '-', plate: '', status: 0 }
    ]

    // 生成平面图数据
    state.gridData = state.tableData.map(item => ({
      ...item,
      status: item.status,
      plate: item.plate || ''
    }))

    state.pagination.total = state.tableData.length
    state.loading = false
  }, 500)
}

const handleSearch = () => {
  state.pagination.current = 1
  initData()
}

const handleReset = () => {
  state.searchForm = {
    parkingId: null,
    status: null,
    type: null,
    keyword: ''
  }
  handleSearch()
}

const handleAssign = (row) => {
  state.currentSpace = row
  state.assignForm = {
    memberId: null,
    expireDate: ''
  }
  state.assignModalVisible = true
}

const handleAssignSubmit = () => {
  if (!state.assignForm.memberId) {
    Message.warning('请选择车主')
    return
  }
  Message.success('分配成功')
  state.assignModalVisible = false
  initData()
}

const handleUnbind = (row) => {
  Modal.confirm({
    title: '确认解绑',
    content: `确定要解绑车位"${row.no}"吗？`,
    onOk: () => {
      Message.success('解绑成功')
      initData()
    }
  })
}

const handleReportRepair = (row) => {
  Modal.confirm({
    title: '确认报修',
    content: `确定要对车位"${row.no}"进行报修吗？`,
    onOk: () => {
      Message.success('报修成功')
      initData()
    }
  })
}

const handleSpaceClick = (space) => {
  state.currentSpace = space
  if (space.status === 0) {
    handleAssign(space)
  }
}

const handlePageChange = (page) => {
  state.pagination.current = page
  initData()
}

initData()
</script>

<style lang="less" scoped>
.space-management-page {
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

      .filter-select {
        width: 150px;
      }

      .filter-input {
        width: 200px;
      }
    }

    .filter-actions {
      display: flex;
      justify-content: flex-end;

      .view-switch {
        display: flex;
        gap: var(--spacing-sm);
      }
    }
  }

  .stats-row {
    display: flex;
    gap: var(--spacing-xl);
    margin-bottom: var(--spacing-lg);

    .stat-item {
      display: flex;
      align-items: center;
      padding: var(--spacing-md) var(--spacing-xl);
      background-color: var(--bg-color);
      border-radius: var(--border-radius-base);
      box-shadow: var(--shadow-base);

      .stat-dot {
        min-width: 12px;
        height: 12px;
        border-radius: 50%;
        margin-right: var(--spacing-sm);
      }

      .stat-label {
        margin-right: var(--spacing-sm);
        color: var(--text-color-secondary);
      }

      .stat-value {
        font-weight: 600;
        color: var(--text-color-title);
      }
    }

    .stat-free .stat-dot {
      background-color: #00B42A;
    }

    .stat-fixed .stat-dot {
      background-color: #165DFF;
    }

    .stat-temp .stat-dot {
      background-color: #FF7D00;
    }

    .stat-fault .stat-dot {
      background-color: #86909C;
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
    }

    .status-free {
      background-color: rgba(0, 180, 42, 0.1);
      color: #00B42A;
    }

    .status-occupied {
      background-color: rgba(22, 93, 255, 0.1);
      color: #165DFF;
    }

    .status-fault {
      background-color: rgba(134, 144, 156, 0.1);
      color: #86909C;
    }

    .pagination-wrapper {
      display: flex;
      justify-content: flex-end;
      margin-top: var(--spacing-xl);
    }
  }

  .grid-container {
    background-color: var(--bg-color);
    border-radius: var(--border-radius-base);
    padding: var(--spacing-xl);
    box-shadow: var(--shadow-base);

    .parking-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));
      gap: var(--spacing-md);

      .space-grid-item {
        aspect-ratio: 1;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        border-radius: var(--border-radius-base);
        cursor: pointer;
        transition: transform 0.2s, box-shadow 0.2s;
        color: #fff;
        font-size: var(--font-size-sm);

        &:hover {
          transform: scale(1.05);
          box-shadow: var(--shadow-medium);
        }

        .space-no {
          font-weight: 600;
          margin-bottom: 4px;
        }

        .space-plate {
          font-size: var(--font-size-xs);
        }
      }

      .space-free {
        background-color: #00B42A;
      }

      .space-occupied {
        background-color: #165DFF;
      }

      .space-fault {
        background-color: #86909C;
      }
    }
  }
}
</style>