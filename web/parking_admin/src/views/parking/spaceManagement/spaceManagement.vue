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
          <Input
              v-model="state.searchForm.carNumber"
              placeholder="搜索车牌"
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
      <div class="stat-item stat-inlot">
        <span class="stat-dot"></span>
        <span class="stat-label">在场车辆</span>
        <span class="stat-value">{{ state.stats.inLot }}</span>
      </div>
      <div class="stat-item stat-total">
        <span class="stat-dot"></span>
        <span class="stat-label">总车位</span>
        <span class="stat-value">{{ state.stats.total }}</span>
      </div>
      <div class="stat-item stat-free">
        <span class="stat-dot"></span>
        <span class="stat-label">空余车位</span>
        <span class="stat-value">{{ state.stats.free }}</span>
      </div>
      <div class="stat-item stat-rate">
        <span class="stat-dot"></span>
        <span class="stat-label">占用率</span>
        <span class="stat-value">{{ state.stats.rate }}%</span>
      </div>
    </div>

    <!-- 在场车辆看板 -->
    <div class="grid-container">
      <div class="grid-legend">
        <div class="legend-item">
          <span class="legend-color color-inlot"></span>
          <span>在场</span>
        </div>
        <div class="legend-item">
          <span class="legend-color color-waiting"></span>
          <span>待缴费</span>
        </div>
      </div>
      <div class="parking-grid">
        <div
            v-for="car in state.gridData"
            :key="car.id"
            class="car-grid-item"
            :class="'car-' + getCarClass(car)"
            :title="getCarTitle(car)"
        >
          <span class="car-plate">{{ car.carNumber || '无牌车' }}</span>
          <span class="car-entry">{{ formatEntryTime(car.entryTime) }}</span>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import {reactive, onMounted} from 'vue'
import {useCommonStore} from '@/stores/common.js'
import {parkingApi, inLotApi} from '@/api'
import {
  Button,
  Icon,
  Input,
  Message,
  Option,
  Select
} from 'view-ui-plus'

const commonStore = useCommonStore()

// 看板一次性拉取的在场车辆条数上限；result.total 仍是该停车场的真实在场总数
const IN_LOT_PAGE_SIZE = 1000

const state = reactive({
  searchForm: {
    parkingId: commonStore.state.selectedParkingIds.length > 0
      ? commonStore.state.selectedParkingIds[0]
      : commonStore.state.currentParking?.id || null,
    carNumber: ''
  },

  gridData: [],

  stats: {
    inLot: 0,
    total: 0,
    free: 0,
    rate: 0
  },

  parkingList: commonStore.state.parkingList.length > 0
    ? commonStore.state.parkingList
    : []
})

// 待缴费：出场通道摄像头已识别到该车（此时 status 仍为 0，缴费放行后才写 outTime）
const getCarClass = (car) => (car.outChannelId ? 'waiting' : 'inlot')

const formatEntryTime = (t) => (t ? String(t).slice(11, 16) : '-')

const getCarTitle = (car) => {
  const plate = car.carNumber || '无牌车'
  return `${plate} · 入场 ${car.entryTime || '-'}${car.outChannelId ? ' · 待缴费' : ''}`
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
  const {parkingId, carNumber} = state.searchForm
  if (!parkingId) {
    state.gridData = []
    state.stats = {inLot: 0, total: 0, free: 0, rate: 0}
    return
  }
  try {
    const [listRes, detailRes] = await Promise.all([
      inLotApi.getInLotList({
        parkingId,
        status: 0,
        size: IN_LOT_PAGE_SIZE,
        carNumber: carNumber || undefined
      }),
      parkingApi.getParkingDetail(parkingId)
    ])
    state.gridData = listRes.data || []
    // 按车牌搜索时以实际命中条数为准，否则用分页 total（不受拉取上限影响）
    const inLot = carNumber ? state.gridData.length : Number(listRes.result?.total ?? state.gridData.length)
    const total = Number(detailRes.data?.capacity ?? 0)
    state.stats = {
      inLot,
      total,
      free: Math.max(0, total - inLot),
      rate: total > 0 ? Math.round(inLot * 100 / total) : 0
    }
  } catch (e) { /* authRequest 已提示 */ }
}

const handleSearch = () => {
  loadData()
}

const handleReset = () => {
  const ids = commonStore.state.selectedParkingIds
  state.searchForm = {
    parkingId: ids.length > 0 ? ids[0] : commonStore.state.currentParking?.id || null,
    carNumber: ''
  }
  loadData()
}

const handleRefreshGrid = async () => {
  await loadData()
  Message.success('已刷新')
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

    .stat-inlot .stat-dot {
      background-color: #165DFF;
    }

    .stat-total .stat-dot {
      background-color: #86909C;
    }

    .stat-free .stat-dot {
      background-color: #00B42A;
    }

    .stat-rate .stat-dot {
      background-color: #FF7D00;
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

        .color-inlot {
          background-color: #165DFF;
        }

        .color-waiting {
          background-color: #FF7D00;
        }
      }
    }

    .parking-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(80px, 1fr));
      gap: var(--spacing-md);

      .car-grid-item {
        aspect-ratio: 1;
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;
        border-radius: var(--border-radius-base);
        transition: transform 0.2s, box-shadow 0.2s;
        color: #fff;
        font-size: var(--font-size-sm);

        &:hover {
          transform: scale(1.05);
          box-shadow: var(--shadow-medium);
        }

        .car-plate {
          font-weight: 600;
          margin-bottom: 4px;
        }

        .car-entry {
          font-size: var(--font-size-xs);
        }
      }

      .car-inlot {
        background-color: #165DFF;
      }

      .car-waiting {
        background-color: #FF7D00;
      }
    }
  }
}
</style>
