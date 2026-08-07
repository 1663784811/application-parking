<template>
  <div class="parking-sidebar">
    <div class="parking-sidebar-header">
      <div class="parking-sidebar-title">停车场列表</div>
      <span class="parking-sidebar-count">{{ state.filteredList.length }}个</span>
    </div>
    <!-- 搜索框 -->
    <div class="parking-search">
      <Icon type="ios-search" class="search-icon" />
      <input
        v-model="state.searchKeyword"
        class="search-input"
        placeholder="搜索停车场..."
        @input="handleSearch"
      />
      <Icon
        v-if="state.searchKeyword"
        type="ios-close-circle"
        class="search-clear"
        @click="clearSearch"
      />
    </div>
    <!-- 全选/多选操作栏 -->
    <div class="parking-actions">
      <label class="parking-select-all" @click="handleSelectAll">
        <span class="checkbox" :class="{ checked: isAllSelected }">
          <Icon v-if="isAllSelected" type="ios-checkmark" />
        </span>
        <span>全选</span>
      </label>
      <span class="parking-selected-count">
        已选 {{ state.selectedParkingIds.length }} 项
      </span>
    </div>
    <div class="parking-list" v-if="state.filteredList.length > 0">
      <div
        v-for="item in state.filteredList"
        :key="item.id"
        class="parking-item"
        :class="{ active: state.selectedParkingIds.includes(item.id) }"
        @click="handleToggleParking(item)"
      >
        <div class="parking-item-checkbox">
          <span class="checkbox" :class="{ checked: state.selectedParkingIds.includes(item.id) }">
            <Icon v-if="state.selectedParkingIds.includes(item.id)" type="ios-checkmark" />
          </span>
        </div>
        <div class="parking-item-icon">
          <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect x="4" y="3" width="16" height="18" rx="2" stroke="currentColor" stroke-width="1.5"/>
            <path d="M4 9H20" stroke="currentColor" stroke-width="1.5"/>
            <path d="M10 14.5L12 16.5L16 12.5" stroke="currentColor" stroke-width="1.5" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
        </div>
        <div class="parking-item-content">
          <div class="parking-item-top">
            <span class="parking-item-name">{{ item.name }}</span>
          </div>
          <div class="parking-item-info">
            <span class="parking-item-spaces" :class="spaceStatusClass(item)">
              空 {{ item.availableSpaces }}/{{ item.totalSpaces }}
            </span>
            <span class="parking-item-occupancy">{{ occupancyRate(item) }}%</span>
          </div>
          <div class="parking-item-bar">
            <div class="parking-item-bar-fill" :style="{ width: occupancyRate(item) + '%' }"></div>
          </div>
        </div>
      </div>
    </div>
    <div class="parking-empty" v-else>
      <Icon v-if="state.searchKeyword" type="ios-search" class="parking-empty-icon" />
      <Icon v-else type="ios-car" class="parking-empty-icon" />
      <p>{{ state.searchKeyword ? '未匹配到停车场' : '暂无停车场' }}</p>
    </div>
  </div>
</template>

<script setup>
import { reactive, computed, onMounted } from 'vue'
import { Icon } from 'view-ui-plus'
import { parkingApi } from '@/api'
import { useCommonStore } from '@/stores/common.js'

const commonStore = useCommonStore()

const state = reactive({
  parkingList: [],
  currentParking: null,
  searchKeyword: '',
  filteredList: [],
  selectedParkingIds: commonStore.state.selectedParkingIds.length > 0
    ? [...commonStore.state.selectedParkingIds]
    : commonStore.state.currentParking
      ? [commonStore.state.currentParking.id]
      : []
})

// 模拟数据
const mockParkingList = [
  { id: 1, name: '万象城停车场', address: '深圳市南山区深南大道9668号', totalSpaces: 1200, availableSpaces: 342, status: 1 },
  { id: 2, name: 'CBD 商务中心停车场', address: '深圳市福田区福华三路88号', totalSpaces: 800, availableSpaces: 156, status: 1 },
  { id: 3, name: '科技园停车场', address: '深圳市南山区科技南路18号', totalSpaces: 600, availableSpaces: 89, status: 1 },
  { id: 4, name: '火车站西广场停车场', address: '深圳市罗湖区建设路1003号', totalSpaces: 450, availableSpaces: 210, status: 1 },
  { id: 5, name: '海岸城购物中心停车场', address: '深圳市南山区文心五路33号', totalSpaces: 900, availableSpaces: 423, status: 1 },
  { id: 6, name: '人民医院停车场', address: '深圳市福田区笋岗西路3002号', totalSpaces: 350, availableSpaces: 45, status: 1 },
  { id: 7, name: '大学城体育中心停车场', address: '深圳市南山区留仙大道2032号', totalSpaces: 500, availableSpaces: 278, status: 1 },
  { id: 8, name: '宝安国际机场停车场', address: '深圳市宝安区宝安大道', totalSpaces: 2000, availableSpaces: 876, status: 1 },
  { id: 9, name: '欢乐谷主题公园停车场', address: '深圳市南山区侨城西街18号', totalSpaces: 750, availableSpaces: 312, status: 1 },
  { id: 10, name: '深业上城停车场', address: '深圳市福田区皇岗路5001号', totalSpaces: 680, availableSpaces: 198, status: 1 }
]

// 加载停车场列表
const loadParkingList = async () => {
  try {
    const res = await parkingApi.getParkingList({ page: 1, pageSize: 100 })
    if (res.code === 0 || res.data) {
      const list = res.data?.list || res.data || []
      if (list.length > 0) {
        applyParkingData(list)
        return
      }
    }
    // API 返回空数据时使用模拟数据
    useMockData()
  } catch (e) {
    console.error('加载停车场列表失败，使用模拟数据', e)
    useMockData()
  }
}

// 使用模拟数据
const useMockData = () => {
  applyParkingData(mockParkingList)
}

// 应用停车场数据
const applyParkingData = (list) => {
  state.parkingList = list
  state.filteredList = list
  commonStore.setParkingList(list)
  // 如果还没有选中项，默认选中第一个
  if (state.selectedParkingIds.length === 0 && list.length > 0) {
    state.selectedParkingIds = [list[0].id]
    syncSelectionToStore()
  }
}

// 是否全选
const isAllSelected = computed(() => {
  return state.filteredList.length > 0
    && state.selectedParkingIds.length === state.filteredList.length
})

// 同步选中状态到 store
const syncSelectionToStore = () => {
  commonStore.setSelectedParkingIds([...state.selectedParkingIds])
  // 兼容旧代码：如果有且仅有一个选中，同步到 currentParking
  const first = state.parkingList.find(p => p.id === state.selectedParkingIds[0])
  commonStore.setCurrentParking(first || null)
}

// 搜索过滤
const handleSearch = () => {
  const keyword = state.searchKeyword.trim().toLowerCase()
  if (!keyword) {
    state.filteredList = state.parkingList
    return
  }
  state.filteredList = state.parkingList.filter(item =>
    item.name.toLowerCase().includes(keyword) ||
    (item.address && item.address.toLowerCase().includes(keyword))
  )
}

// 清除搜索
const clearSearch = () => {
  state.searchKeyword = ''
  state.filteredList = state.parkingList
}

// 切换单个停车场选中
const handleToggleParking = (parking) => {
  const idx = state.selectedParkingIds.indexOf(parking.id)
  if (idx === -1) {
    state.selectedParkingIds.push(parking.id)
  } else {
    state.selectedParkingIds.splice(idx, 1)
  }
  syncSelectionToStore()
}

// 全选/取消全选
const handleSelectAll = () => {
  if (isAllSelected.value) {
    state.selectedParkingIds = []
  } else {
    state.selectedParkingIds = state.filteredList.map(item => item.id)
  }
  syncSelectionToStore()
}

// 计算占用率
const occupancyRate = (item) => {
  if (!item.totalSpaces) return 0
  return Math.round(((item.totalSpaces - item.availableSpaces) / item.totalSpaces) * 100)
}

// 获取车位状态样式
const spaceStatusClass = (item) => {
  const rate = occupancyRate(item)
  if (rate >= 90) return 'space-danger'
  if (rate >= 70) return 'space-warning'
  return 'space-normal'
}

onMounted(() => {
  // 如果 store 中已有数据，优先使用
  if (commonStore.state.parkingList.length > 0) {
    state.parkingList = commonStore.state.parkingList
    state.currentParking = commonStore.state.currentParking
    state.selectedParkingIds = commonStore.state.selectedParkingIds.length > 0
      ? [...commonStore.state.selectedParkingIds]
      : commonStore.state.currentParking
        ? [commonStore.state.currentParking.id]
        : []
    state.filteredList = state.parkingList
  } else {
    loadParkingList()
  }
})
</script>

<style lang="less" scoped>
.parking-sidebar {
  width: 260px;
  flex-shrink: 0;
  display: flex;
  flex-direction: column;
  background-color: var(--bg-color);
  border-radius: var(--border-radius-base);
  box-shadow: var(--shadow-base);
  overflow: hidden;

  .parking-sidebar-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: var(--spacing-lg);
    border-bottom: 1px solid var(--border-color);

    .parking-sidebar-title {
      font-size: var(--font-size-md);
      font-weight: 600;
      color: var(--text-color-title);
    }

    .parking-sidebar-count {
      font-size: var(--font-size-xs);
      color: var(--text-color-secondary);
      background-color: var(--bg-color-hover);
      padding: 1px 8px;
      border-radius: 10px;
    }
  }

  // 搜索框
  .parking-search {
    display: flex;
    align-items: center;
    margin: var(--spacing-sm) var(--spacing-md) 0;
    padding: 0 var(--spacing-sm);
    border: 1px solid var(--border-color);
    border-radius: var(--border-radius-base);
    background-color: var(--bg-color-hover);
    transition: border-color 0.2s, background-color 0.2s;

    &:focus-within {
      border-color: var(--primary-color);
      background-color: var(--bg-color);
    }

    .search-icon {
      flex-shrink: 0;
      font-size: 14px;
      color: var(--text-color-secondary);
      margin-right: var(--spacing-xs);
    }

    .search-input {
      flex: 1;
      border: none;
      outline: none;
      background: transparent;
      height: 30px;
      font-size: var(--font-size-sm);
      color: var(--text-color);

      &::placeholder {
        color: var(--text-color-disabled);
      }
    }

    .search-clear {
      flex-shrink: 0;
      font-size: 14px;
      color: var(--text-color-secondary);
      cursor: pointer;
      transition: color 0.2s;

      &:hover {
        color: var(--text-color);
      }
    }
  }

  // 多选操作栏
  .parking-actions {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: var(--spacing-sm) var(--spacing-md);
    border-bottom: 1px solid var(--border-color-light);
    user-select: none;

    .parking-select-all {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: var(--font-size-xs);
      color: var(--text-color-secondary);
      cursor: pointer;
      transition: color 0.2s;

      &:hover {
        color: var(--text-color);
      }
    }

    .parking-selected-count {
      font-size: var(--font-size-xs);
      color: var(--text-color-secondary);
    }
  }

  // 复选框
  .checkbox {
    display: inline-flex;
    align-items: center;
    justify-content: center;
    width: 16px;
    height: 16px;
    border: 1.5px solid var(--text-color-disabled);
    border-radius: 3px;
    background-color: var(--bg-color);
    transition: all 0.2s ease;
    flex-shrink: 0;

    .ivu-icon {
      font-size: 12px;
      font-weight: 700;
      color: #fff;
      line-height: 1;
    }

    &.checked {
      border-color: var(--primary-color);
      background-color: var(--primary-color);
    }
  }

  .parking-list {
    flex: 1;
    overflow-y: auto;
    padding: 0 var(--spacing-sm) var(--spacing-sm);
  }

  .parking-item {
    display: flex;
    align-items: flex-start;
    padding: var(--spacing-sm) var(--spacing-md);
    margin-bottom: 2px;
    border-radius: var(--border-radius-sm);
    cursor: pointer;
    transition: all 0.2s ease;
    color: var(--text-color);

    &:hover {
      background-color: var(--bg-color-hover);
    }

    &.active {
      background-color: rgba(22, 93, 255, 0.08);
      color: var(--primary-color);

      .parking-item-icon {
        color: var(--primary-color);
      }

      .parking-item-name {
        color: var(--primary-color);
        font-weight: 500;
      }
    }

    .parking-item-checkbox {
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
      width: 20px;
      height: 24px;
      margin-right: 6px;
      margin-top: 2px;
    }

    .parking-item-icon {
      display: flex;
      align-items: center;
      justify-content: center;
      flex-shrink: 0;
      width: 24px;
      height: 24px;
      margin-right: var(--spacing-sm);
      margin-top: 2px;
      color: var(--text-color-secondary);

      svg {
        width: 20px;
        height: 20px;
      }
    }

    .parking-item-content {
      flex: 1;
      min-width: 0;
    }

    .parking-item-top {
      display: flex;
      align-items: center;
      justify-content: space-between;
    }

    .parking-item-name {
      font-size: var(--font-size-sm);
      color: var(--text-color-title);
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
      transition: color 0.2s;
    }

    .parking-item-info {
      display: flex;
      align-items: center;
      justify-content: space-between;
      margin-top: 3px;
    }

    .parking-item-spaces {
      font-size: var(--font-size-xs);
      transition: color 0.2s;

      &.space-normal {
        color: var(--success-color);
      }

      &.space-warning {
        color: var(--warning-color);
      }

      &.space-danger {
        color: var(--error-color);
      }
    }

    .parking-item-occupancy {
      font-size: var(--font-size-xs);
      color: var(--text-color-secondary);
    }

    .parking-item-bar {
      width: 100%;
      height: 3px;
      margin-top: 4px;
      background-color: var(--border-color-light);
      border-radius: 2px;
      overflow: hidden;

      .parking-item-bar-fill {
        height: 100%;
        border-radius: 2px;
        background-color: var(--success-color);
        transition: width 0.3s ease;
      }
    }

    &.active .parking-item-bar-fill {
      background-color: var(--primary-color);
    }

    // 占用率颜色
    .parking-item-bar-fill {
      &.bar-danger {
        background-color: var(--error-color);
      }
      &.bar-warning {
        background-color: var(--warning-color);
      }
    }
  }

  .parking-empty {
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    padding: var(--spacing-xxl) 0;
    color: var(--text-color-secondary);

    .parking-empty-icon {
      font-size: 36px;
      margin-bottom: var(--spacing-sm);
    }

    p {
      font-size: var(--font-size-sm);
    }
  }
}
</style>