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
        <div class="filter-item">
          <Button @click="handleRefreshGrid">
            <Icon type="ios-refresh"/>
            刷新
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
            :key="space.id"
            class="space-grid-item"
            :class="'space-' + getStatusClass(space.status)"
            @click="handleSpaceClick(space)"
            :title="getSpaceTitle(space)"
        >
          <span class="space-no">{{ space.spaceNo }}</span>
          <span class="space-plate" v-if="space.plate">{{ space.plate }}</span>
        </div>
      </div>
    </div>

    <!-- 分配弹窗 -->
    <Modal v-model="state.assignModalVisible" title="分配车位" width="1000">
      <Form :model="state.assignForm" :label-width="100" class="modal-form-2col">
        <FormItem label="车位编号">
          <Input :value="state.currentSpace?.spaceNo" disabled/>
        </FormItem>
        <FormItem label="绑定车主" prop="memberId">
          <Select v-model="state.assignForm.memberId" placeholder="请选择车主" filterable>
            <Option v-for="item in state.memberList" :key="item.id" :value="item.id">
              {{ item.name }} - {{ item.plate }}
            </Option>
          </Select>
        </FormItem>
        <FormItem label="有效期至" class="modal-form-full">
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
import {useCommonStore} from '@/stores/common.js'
import {parkingApi, memberApi, spaceApi} from '@/api'
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
    free: 0,
    fixed: 0,
    temp: 0,
    fault: 0
  },

  parkingList: commonStore.state.parkingList.length > 0
    ? commonStore.state.parkingList
    : [],

  memberList: [],

  assignModalVisible: false,
  currentSpace: null,
  assignForm: {
    memberId: null,
    expireDate: ''
  }
})

const pad = (n) => String(n).padStart(2, '0')
const toDateStr = (d) => {
  if (!d) return null
  const dt = d instanceof Date ? d : new Date(d)
  if (isNaN(dt.getTime())) return null
  return `${dt.getFullYear()}-${pad(dt.getMonth() + 1)}-${pad(dt.getDate())}`
}

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
    return `${space.spaceNo} - ${space.plate}`
  }
  return `${space.spaceNo} - ${getStatusText(space.status)}`
}

const loadParkingList = async () => {
  if (commonStore.state.parkingList.length > 0) {
    state.parkingList = commonStore.state.parkingList
  } else {
    try {
      const res = await parkingApi.getParkingList({size: 1000})
      state.parkingList = res.data || []
    } catch (e) { /* ignore */ }
  }
  if (!state.searchForm.parkingId && state.parkingList.length > 0) {
    state.searchForm.parkingId = state.parkingList[0].id
  }
}

const loadData = async () => {
  const {parkingId, status, type, keyword} = state.searchForm
  const params = {parkingId, status, type, keyword}
  try {
    const [listRes, statRes] = await Promise.all([
      spaceApi.getSpaceList(params),
      parkingId ? spaceApi.getSpaceStats(parkingId) : Promise.resolve(null)
    ])
    state.gridData = listRes.data || []
    state.stats = statRes?.data || {free: 0, fixed: 0, temp: 0, fault: 0}
  } catch (e) { /* authRequest 已提示 */ }
}

const handleSearch = () => {
  loadData()
}

const handleReset = () => {
  const ids = commonStore.state.selectedParkingIds
  state.searchForm = {
    parkingId: ids.length > 0 ? ids[0] : commonStore.state.currentParking?.id || null,
    status: null,
    type: null,
    keyword: ''
  }
  loadData()
}

const handleRefreshGrid = async () => {
  await loadData()
  Message.success('已刷新')
}

const handleAssign = async (space) => {
  state.currentSpace = space
  state.assignForm = {memberId: null, expireDate: ''}
  if (state.memberList.length === 0) {
    try {
      const res = await memberApi.getMemberList({size: 1000})
      state.memberList = res.data || []
    } catch (e) { /* ignore */ }
  }
  state.assignModalVisible = true
}

const handleAssignSubmit = async () => {
  if (!state.assignForm.memberId) {
    Message.warning('请选择车主')
    return
  }
  const member = state.memberList.find(m => m.id === state.assignForm.memberId)
  const payload = {
    id: state.currentSpace.id,
    memberId: member.id,
    memberName: member.name,
    plate: member.plate,
    expireDate: state.assignForm.expireDate ? toDateStr(state.assignForm.expireDate) : null
  }
  try {
    await spaceApi.assignSpace(payload)
    Message.success('分配成功')
    state.assignModalVisible = false
    await loadData()
  } catch (e) { /* authRequest 已提示 */ }
}

const handleUnbind = (space) => {
  Modal.confirm({
    title: '确认解绑',
    content: `确定要解绑车位"${space.spaceNo}"吗？`,
    onOk: async () => {
      try {
        await spaceApi.unbindSpace(space.id)
        Message.success('解绑成功')
        await loadData()
      } catch (e) { /* ignore */ }
    }
  })
}

const handleReportRepair = (space) => {
  Modal.confirm({
    title: '确认报修',
    content: `确定要对车位"${space.spaceNo}"进行报修吗？`,
    onOk: async () => {
      try {
        await spaceApi.reportRepair(space.id)
        Message.success('报修成功')
        await loadData()
      } catch (e) { /* ignore */ }
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

onMounted(() => {
  loadParkingList().then(() => loadData())
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
