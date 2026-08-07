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
              <Icon type="ios-search"/>
            </template>
          </Input>
        </div>

        <div class="filter-item">
          <Button type="primary" @click="handleSearch">查询</Button>
        </div>
        <div class="filter-item">
          <Button @click="handleReset">重置</Button>
        </div>
      </div>

      <div class="filter-actions">
        <Button @click="handleRefreshGrid">
          <Icon type="ios-refresh"/>
          刷新
        </Button>
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

    <!-- 平面图视图 -->
    <div class="grid-container">
      <div class="grid-legend">
        <div class="legend-item">
          <span class="legend-color color-free"></span>
          <span>空闲</span>
        </div>
        <div class="legend-item">
          <span class="legend-color color-occupied"></span>
          <span>占用</span>
        </div>
        <div class="legend-item">
          <span class="legend-color color-fault"></span>
          <span>故障</span>
        </div>
      </div>
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
          <Input :value="state.currentSpace?.no" disabled/>
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
import {reactive, onMounted} from 'vue'
import {useRoute} from 'vue-router'
import {useCommonStore} from '@/stores/common.js'
import {parkingApi} from '@/api'
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
  Select
} from 'view-ui-plus'

const route = useRoute()
const commonStore = useCommonStore()

const state = reactive({
  searchForm: {
    parkingId: commonStore.state.selectedParkingIds.length > 0
      ? commonStore.state.selectedParkingIds[0]
      : commonStore.state.currentParking?.id || null,
    status: null,
    type: null,
    keyword: ''
  },

  gridData: [],

  stats: {
    free: 156,
    fixed: 98,
    temp: 38,
    fault: 8
  },

  parkingList: commonStore.state.parkingList.length > 0
    ? commonStore.state.parkingList
    : [
      {id: 1, name: '城西停车场'},
      {id: 2, name: '城东停车场'},
      {id: 3, name: '购物中心停车场'}
    ],

  memberList: [
    {id: 1, name: '张三', plate: '京A12345'},
    {id: 2, name: '李四', plate: '京B67890'},
    {id: 3, name: '王五', plate: '京C11111'}
  ],

  assignModalVisible: false,
  currentSpace: null,
  assignForm: {
    memberId: null,
    expireDate: ''
  }
})

const getStatusClass = (status) => {
  const map = {0: 'free', 1: 'occupied', 2: 'fault'}
  return map[status] || 'free'
}

const getStatusText = (status) => {
  const map = {0: '空闲', 1: '占用', 2: '故障'}
  return map[status] || '-'
}

const getSpaceTitle = (space) => {
  if (space.plate) {
    return `${space.no} - ${space.plate}`
  }
  return `${space.no} - ${getStatusText(space.status)}`
}

const initData = () => {
  setTimeout(() => {
    const list = [
      {id: 1, no: 'A001', area: 'A区', type: 1, memberName: '张三', plate: '京A12345', status: 1},
      {id: 2, no: 'A002', area: 'A区', type: 1, memberName: '李四', plate: '京B67890', status: 1},
      {id: 3, no: 'A003', area: 'A区', type: 2, memberName: '-', plate: '京C11111', status: 1},
      {id: 4, no: 'A004', area: 'A区', type: 2, memberName: '-', plate: '', status: 0},
      {id: 5, no: 'A005', area: 'A区', type: 3, memberName: '-', plate: '', status: 2},
      {id: 6, no: 'B001', area: 'B区', type: 1, memberName: '王五', plate: '京D22222', status: 1},
      {id: 7, no: 'B002', area: 'B区', type: 2, memberName: '-', plate: '', status: 0},
      {id: 8, no: 'B003', area: 'B区', type: 2, memberName: '-', plate: '', status: 0}
    ]

    state.gridData = list.map(item => ({
      ...item,
      status: item.status,
      plate: item.plate || ''
    }))
  }, 300)
}

const handleSearch = () => {
  initData()
}

const handleReset = () => {
  const ids = commonStore.state.selectedParkingIds
  state.searchForm = {
    parkingId: ids.length > 0 ? ids[0] : commonStore.state.currentParking?.id || null,
    status: null,
    type: null,
    keyword: ''
  }
  handleSearch()
}

const handleRefreshGrid = () => {
  initData()
  Message.success('已刷新')
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
  } else if (space.status === 1) {
    handleUnbind(space)
  } else if (space.status === 2) {
    handleReportRepair(space)
  }
}

// 同步停车场列表
const syncParkingList = () => {
  if (commonStore.state.parkingList.length > 0) {
    state.parkingList = commonStore.state.parkingList
    if (!state.searchForm.parkingId) {
      const ids = commonStore.state.selectedParkingIds
      state.searchForm.parkingId = ids.length > 0 ? ids[0] : commonStore.state.currentParking?.id || null
    }
  }
}

syncParkingList()
initData()

onMounted(() => {
  syncParkingList()
})
</script>

<style lang="less" scoped>
.space-management-page {
  flex: 1;
  display: flex;
  flex-direction: column;

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

  .grid-container {
    flex: 1;
    background-color: var(--bg-color);
    border-radius: var(--border-radius-base);
    padding: var(--spacing-xl);
    box-shadow: var(--shadow-base);

    .grid-legend {
      display: flex;
      gap: var(--spacing-xl);
      margin-bottom: var(--spacing-lg);

      .legend-item {
        display: flex;
        align-items: center;
        gap: var(--spacing-sm);
        font-size: var(--font-size-sm);
        color: var(--text-color-secondary);

        .legend-color {
          display: inline-block;
          width: 12px;
          height: 12px;
          border-radius: 2px;
        }

        .color-free {
          background-color: #00B42A;
        }

        .color-occupied {
          background-color: #165DFF;
        }

        .color-fault {
          background-color: #86909C;
        }
      }
    }

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