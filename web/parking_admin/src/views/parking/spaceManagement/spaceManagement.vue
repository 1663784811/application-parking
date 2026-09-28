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
              @on-change="handleParkingChange"
          >
            <Option v-for="item in state.parkingList" :key="item.id" :value="String(item.id)">
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
          <Button :loading="state.loading" @click="handleRefresh">
            <Icon type="ios-refresh"/>
            刷新
          </Button>
        </div>

        <!-- 自动刷新状态：圆点是静态的，不用循环动画（规范禁止） -->
        <div class="filter-item auto-hint">
          <span class="auto-dot" :class="state.auto.paused ? 'is-paused' : 'is-on'"></span>
          <span class="text-secondary">
            {{ state.auto.paused ? '自动刷新已暂停' : '每 30 秒自动刷新' }}
            <template v-if="state.auto.lastTime">· 更新于 {{ state.auto.lastTime }}</template>
          </span>
        </div>
      </div>
    </div>

    <!-- 统计卡片 -->
    <div class="stats-row">
      <div class="stat-item">
        <span class="stat-label">在场车辆</span>
        <span class="stat-value">{{ state.stats.inLot }}</span>
      </div>
      <div class="stat-item">
        <span class="stat-label">总车位</span>
        <span class="stat-value">{{ capacityKnown() ? state.stats.capacity : '—' }}</span>
      </div>
      <div class="stat-item">
        <span class="stat-label">空余车位</span>
        <span class="stat-value">{{ capacityKnown() ? state.stats.free : '—' }}</span>
      </div>
      <div class="stat-item">
        <span class="stat-label">占用率</span>
        <span class="stat-value" :class="rateClass()">
          {{ capacityKnown() ? state.stats.rate + '%' : '—' }}
        </span>
      </div>
    </div>

    <!-- 在场车辆看板 -->
    <div class="board-container">
      <div class="board-header">
        <div class="board-title">
          <span class="text-title">在场车辆</span>
          <span class="board-count">{{ state.board.matchedCount }} 辆</span>
          <span v-if="state.board.keyword" class="text-secondary">
            匹配「{{ state.board.keyword }}」
          </span>
        </div>
        <div class="board-legend">
          <span class="legend-item">
            <span class="legend-bar bar-inlot"></span>
            在场
          </span>
          <span class="legend-item">
            <span class="legend-bar bar-waiting"></span>
            待缴费（出口已识别）
          </span>
        </div>
      </div>

      <p class="board-tip">
        预估费用按当前费率对「入场 → 现在」实时计算，与 H5 出场订单可能存在分钟级差异，实际以出场订单为准
      </p>
      <p v-if="state.searchForm.parkingId && !state.board.ruleConfigured" class="board-tip board-tip--warning">
        该停车场未配置收费规则，暂时无法预估费用
      </p>
      <p v-if="state.board.truncated" class="board-tip board-tip--warning">
        共命中 {{ state.board.matchedCount }} 辆，仅展示最早入场的 {{ state.board.list.length }} 辆，可用车牌搜索定位
      </p>

      <div class="board-scroll">
        <!-- 空态：未选停车场 / 加载失败 / 首次加载中 / 无在场车辆 -->
        <div v-if="!state.searchForm.parkingId" class="board-empty">
          <Icon type="ios-car-outline" class="board-empty-icon"/>
          <p>请先选择停车场</p>
        </div>
        <div v-else-if="state.error" class="board-empty">
          <Icon type="ios-alert-outline" class="board-empty-icon"/>
          <p>{{ state.error }}</p>
          <Button size="small" @click="handleRefresh">重试</Button>
        </div>
        <div v-else-if="state.loading && state.board.list.length === 0" class="board-empty">
          <p>正在加载在场车辆…</p>
        </div>
        <div v-else-if="state.board.list.length === 0" class="board-empty">
          <Icon type="ios-car-outline" class="board-empty-icon"/>
          <p>{{ state.board.keyword ? '未匹配到在场车辆' : '该停车场当前无在场车辆' }}</p>
        </div>

        <!-- 车辆格子：一车一格 -->
        <div v-else class="car-grid">
          <div
              v-for="car in state.board.list"
              :key="car.id"
              class="car-card"
              :class="{ 'car-card--waiting': car.waiting }"
              :title="carTitle(car)"
          >
            <div class="car-head">
              <span class="car-plate">{{ car.carNumber || '无牌车' }}</span>
              <span v-if="car.waiting" class="status-tag status-tag--warning">待缴费</span>
            </div>
            <span class="car-duration">
              已停 {{ car.duration || '0分钟' }} · {{ formatEntryTime(car.entryTime) }} 入场
            </span>
            <span class="car-fee">{{ feeText(car) }}</span>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive, watch, onMounted, onUnmounted } from 'vue'
import { useCommonStore } from '@/stores/common.js'
import { parkingApi, inLotApi } from '@/api'
import {
  Button,
  Icon,
  Input,
  Message,
  Option,
  Select
} from 'view-ui-plus'

const commonStore = useCommonStore()

/** 自动刷新间隔（毫秒） */
const AUTO_REFRESH_MS = 30000

const state = reactive({
  searchForm: {
    // 与 Option 的 value 保持一致，统一存字符串（后端 id 序列化为字符串，避免类型不一致选不中）
    parkingId: null,
    carNumber: ''
  },

  // 看板数据：整包来自 getInLotBoard
  board: {
    capacity: 0,
    inLotTotal: 0,
    matchedCount: 0,
    truncated: false,
    ruleConfigured: false,
    // 本次实际生效的车牌筛选（用户可能改了输入框但没点查询）
    keyword: '',
    list: []
  },

  // 统计卡片：在场总数走全场口径，不受车牌搜索影响
  stats: {
    inLot: 0,
    capacity: 0,
    free: 0,
    rate: 0
  },

  parkingList: [],
  loading: false,
  error: '',

  // 自动刷新：失败即暂停（拦截器会弹错误提示，否则每 30 秒弹一次），手动刷新成功后恢复
  auto: {
    paused: false,
    lastTime: ''
  }
})

let refreshTimer = null

/** 停车场列表：优先用侧栏缓存，没有则自己拉一次兜底 */
const loadParkingList = async () => {
  if (commonStore.state.parkingList.length > 0) {
    state.parkingList = commonStore.state.parkingList
  } else {
    try {
      const res = await parkingApi.getParkingList({size: 1000})
      state.parkingList = res.data || []
    } catch (e) {
      console.error('获取停车场列表失败', e)
    }
  }
}

/** 当前停车场：侧栏选中项优先，其次旧单选项，最后取列表第一个 */
const syncParkingFromStore = () => {
  const ids = commonStore.state.selectedParkingIds || []
  const fromStore = ids.length > 0
      ? ids[0]
      : commonStore.state.currentParking?.id
  const fallback = state.parkingList.length > 0 ? state.parkingList[0].id : null
  const next = fromStore != null ? fromStore : fallback
  if (next != null) {
    state.searchForm.parkingId = String(next)
  }
}

const capacityKnown = () => state.stats.capacity > 0

/** 占用率配色：与左侧停车场侧栏的阈值保持一致（≥90 红、≥70 橙） */
const rateClass = () => {
  if (!capacityKnown()) {
    return ''
  }
  if (state.stats.rate >= 90) {
    return 'stat-value--danger'
  }
  if (state.stats.rate >= 70) {
    return 'stat-value--warning'
  }
  return 'stat-value--success'
}

const formatAmount = (amount) => Number(amount || 0).toFixed(2)

const formatEntryTime = (t) => (t ? String(t).slice(11, 16) : '—')

const formatClock = (date) => {
  const pad = (n) => String(n).padStart(2, '0')
  return `${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

/** 格子里的费用文案：没有适用费率时不能显示 ¥0.00，那会被误读成免费 */
const feeText = (car) => {
  if (!car.ruleMatched) {
    return '无适用费率'
  }
  return `预估 ¥${formatAmount(car.amount)}`
}

/** 悬浮提示：把所有关键信息摊平，省得点进详情 */
const carTitle = (car) => {
  const parts = [
    car.carNumber || '无牌车',
    `入场 ${car.entryTime || '—'}`,
    `已停 ${car.duration || '0分钟'}`,
    feeText(car)
  ]
  if (car.waiting && car.outRecognizeTime) {
    parts.push(`出场识别 ${car.outRecognizeTime}`)
  }
  return parts.join(' · ')
}

/** 应用看板数据：统计口径在这里一次算清，避免散落在模板里 */
const applyBoard = (board) => {
  state.board = {
    capacity: Number(board.capacity || 0),
    inLotTotal: Number(board.inLotTotal || 0),
    matchedCount: Number(board.matchedCount || 0),
    truncated: !!board.truncated,
    ruleConfigured: !!board.ruleConfigured,
    list: board.list || []
  }
  const capacity = state.board.capacity
  state.stats = {
    inLot: state.board.inLotTotal,
    capacity,
    // 超停时在场数可能大于总车位，空余按 0 兜底；占用率允许超过 100%，如实反映超停
    free: Math.max(0, capacity - state.board.inLotTotal),
    rate: capacity > 0 ? Math.round(state.board.inLotTotal * 100 / capacity) : 0
  }
}

const clearBoard = () => {
  state.board = {
    capacity: 0,
    inLotTotal: 0,
    matchedCount: 0,
    truncated: false,
    ruleConfigured: false,
    keyword: '',
    list: []
  }
  state.stats = {inLot: 0, capacity: 0, free: 0, rate: 0}
}

/**
 * 加载看板。静默模式（轮询）不置 loading、失败不清空已有数据，
 * 只把错误落到 state.error：有数据时保留上一屏，没数据时才显示失败态。
 *
 * @return {Promise<boolean>} 是否加载成功，供手动刷新决定要不要提示"已刷新"
 */
const loadBoard = async ({silent = false} = {}) => {
  const {parkingId, carNumber} = state.searchForm
  if (!parkingId) {
    clearBoard()
    state.error = ''
    return false
  }

  if (!silent) {
    state.loading = true
  }
  try {
    const res = await inLotApi.getInLotBoard({
      parkingId,
      carNumber: carNumber ? carNumber.trim() : undefined
    })
    applyBoard(res.data || {})
    state.board.keyword = carNumber ? carNumber.trim() : ''
    state.auto.lastTime = formatClock(new Date())
    state.error = ''
    // 之前因失败暂停过，这次成功说明后端恢复了
    if (state.auto.paused) {
      state.auto.paused = false
      startAutoRefresh()
    }
    return true
  } catch (e) {
    // 请求拦截器已经弹过错误提示，这里不再重复提示
    state.error = state.board.list.length > 0 ? '' : '加载失败，请稍后重试'
    if (!state.auto.paused) {
      state.auto.paused = true
      stopAutoRefresh()
    }
    if (!silent) {
      console.error('获取在场车辆看板失败', e)
    }
    return false
  } finally {
    if (!silent) {
      state.loading = false
    }
  }
}

const handleSearch = () => {
  loadBoard()
}

/** 切换停车场：先清空再加载，避免加载期间还显示上一个停车场的车 */
const handleParkingChange = () => {
  clearBoard()
  loadBoard()
}

const handleReset = () => {
  state.searchForm.carNumber = ''
  syncParkingFromStore()
  loadBoard()
}

const handleRefresh = async () => {
  const ok = await loadBoard()
  if (ok) {
    Message.success('已刷新')
  }
}

const startAutoRefresh = () => {
  stopAutoRefresh()
  refreshTimer = setInterval(() => {
    // 页面切到后台时不请求；切回前台由 visibilitychange 立刻补一次
    if (document.hidden) {
      return
    }
    loadBoard({silent: true})
  }, AUTO_REFRESH_MS)
}

const stopAutoRefresh = () => {
  if (refreshTimer) {
    clearInterval(refreshTimer)
    refreshTimer = null
  }
}

const handleVisibilityChange = () => {
  if (!document.hidden && state.searchForm.parkingId) {
    loadBoard({silent: true})
  }
}

// 跟左侧停车场侧栏联动：当前停车场不在侧栏选中集合里时，切到选中集合的第一个。
// 只读 store 不写回，避免与侧栏互相覆盖。
watch(() => (commonStore.state.selectedParkingIds || []).join(','), () => {
  const ids = (commonStore.state.selectedParkingIds || []).map(String)
  if (ids.length === 0) {
    return
  }
  const current = state.searchForm.parkingId == null ? null : String(state.searchForm.parkingId)
  if (current != null && ids.includes(current)) {
    return
  }
  state.searchForm.parkingId = ids[0]
  clearBoard()
  loadBoard({silent: true})
})

onMounted(async () => {
  document.addEventListener('visibilitychange', handleVisibilityChange)
  await loadParkingList()
  syncParkingFromStore()
  loadBoard()
  startAutoRefresh()
})

onUnmounted(() => {
  stopAutoRefresh()
  document.removeEventListener('visibilitychange', handleVisibilityChange)
})
</script>

<style lang="less" scoped>
.space-management-page {
  flex: 1;
  min-height: 0;
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
      align-items: center;
      gap: var(--spacing-md);

      .filter-item {
        flex-shrink: 0;
      }

      .filter-select {
        width: 150px;
      }

      .filter-input {
        width: 180px;
      }

      .auto-hint {
        display: flex;
        align-items: center;
        gap: var(--spacing-xs);
        font-size: var(--font-size-xs);

        .auto-dot {
          width: 8px;
          height: 8px;
          border-radius: 50%;
          background-color: var(--success-color);
        }

        .auto-dot.is-paused {
          background-color: var(--text-color-disabled);
        }
      }
    }
  }

  .stats-row {
    display: grid;
    grid-template-columns: repeat(4, 1fr);
    gap: var(--spacing-lg);
    margin-bottom: var(--spacing-lg);

    .stat-item {
      display: flex;
      flex-direction: column;
      padding: var(--spacing-xl);
      background-color: var(--bg-color);
      border-radius: var(--border-radius-base);
      box-shadow: var(--shadow-base);

      .stat-label {
        font-size: var(--font-size-sm);
        color: var(--text-color-secondary);
        margin-bottom: var(--spacing-xs);
      }

      .stat-value {
        font-size: var(--font-size-xl);
        font-weight: 600;
        color: var(--text-color-title);
      }

      // 占用率语义色：阈值与左侧停车场侧栏一致（≥90 红、≥70 橙）
      .stat-value--danger {
        color: var(--error-color);
      }

      .stat-value--warning {
        color: var(--warning-color);
      }

      .stat-value--success {
        color: var(--success-color);
      }
    }
  }

  .board-container {
    flex: 1;
    min-height: 0;
    display: flex;
    flex-direction: column;
    background-color: var(--bg-color);
    border-radius: var(--border-radius-base);
    padding: var(--spacing-xl);
    box-shadow: var(--shadow-base);

    .board-header {
      display: flex;
      align-items: center;
      justify-content: space-between;
      flex-wrap: wrap;
      gap: var(--spacing-md);
      margin-bottom: var(--spacing-md);

      .board-title {
        display: flex;
        align-items: center;
        gap: var(--spacing-sm);

        .board-count {
          font-weight: 600;
          color: var(--primary-color);
        }
      }

      .board-legend {
        display: flex;
        gap: var(--spacing-xl);

        .legend-item {
          display: flex;
          align-items: center;
          gap: var(--spacing-sm);
          font-size: var(--font-size-sm);
          color: var(--text-color-secondary);

          .legend-bar {
            display: inline-block;
            width: 12px;
            height: 12px;
            border-radius: 2px;
          }

          .bar-inlot {
            background-color: var(--primary-color);
          }

          .bar-waiting {
            background-color: var(--warning-color);
          }
        }
      }
    }

    .board-tip {
      margin-bottom: var(--spacing-sm);
      font-size: var(--font-size-xs);
      color: var(--text-color-secondary);
    }

    .board-tip--warning {
      color: var(--warning-color);
    }

    .board-scroll {
      flex: 1;
      min-height: 0;
      overflow-y: auto;
    }

    .board-empty {
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: var(--spacing-md);
      padding: var(--spacing-xxl) 0;
      color: var(--text-color-secondary);

      .board-empty-icon {
        font-size: 40px;
        color: var(--text-color-disabled);
      }
    }

    .car-grid {
      display: grid;
      grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
      gap: var(--spacing-md);
      align-content: start;
    }

    .car-card {
      display: flex;
      flex-direction: column;
      gap: var(--spacing-xs);
      padding: var(--spacing-md);
      background-color: var(--bg-color);
      border: 1px solid var(--border-color);
      // 左侧语义色条：整块填色会让次要文字对比度不够
      border-left-width: var(--spacing-xs);
      border-left-color: var(--primary-color);
      border-radius: var(--border-radius-base);
      transition: box-shadow 0.25s ease, border-color 0.25s ease;

      &:hover {
        border-color: var(--primary-hover-color);
        box-shadow: var(--shadow-medium);
      }

      &.car-card--waiting {
        border-left-color: var(--warning-color);

        .car-fee {
          color: var(--warning-color);
        }
      }

      .car-head {
        display: flex;
        align-items: center;
        justify-content: space-between;
        gap: var(--spacing-xs);
      }

      .car-plate {
        font-size: var(--font-size-md);
        font-weight: 600;
        color: var(--text-color-title);
        white-space: nowrap;
        overflow: hidden;
        text-overflow: ellipsis;
      }

      .car-duration {
        font-size: var(--font-size-xs);
        color: var(--text-color-secondary);
      }

      .car-fee {
        font-size: var(--font-size-sm);
        font-weight: 600;
        color: var(--text-color-title);
      }
    }
  }
}
</style>
