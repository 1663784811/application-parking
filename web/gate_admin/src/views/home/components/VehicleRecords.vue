<template>
  <!-- 左下：车辆通行记录 — div 网格布局 -->
  <div class="card record-card">
    <!-- 卡片头部：标题 + 统计概览 -->
    <div class="card-header">
      <div class="header-left">
        <i class="fas fa-table"></i>
        <h3>车辆通行记录</h3>
        <div class="header-divider"></div>
        <div class="header-stat-items">
          <span class="stat-item">
            <i class="fas fa-arrow-right-to-bracket"></i>
            <strong>{{ stats.in }}</strong> 入场
          </span>
          <span class="stat-item">
            <i class="fas fa-arrow-right-from-bracket"></i>
            <strong>{{ stats.out }}</strong> 出场
          </span>
          <span class="stat-item abnormal">
            <i class="fas fa-exclamation-triangle"></i>
            <strong>{{ stats.abnormal }}</strong> 异常
          </span>
        </div>
      </div>
      <div class="header-right">
        <div class="search-box">
          <i class="fas fa-search"></i>
          <input
              type="text"
              v-model="state.searchQuery"
              placeholder="搜索车牌..."
          />
        </div>
        <div class="filter-tabs">
          <button
              v-for="tab in state.filterTabs"
              :key="tab.key"
              class="filter-tab"
              :class="{ active: state.activeFilter === tab.key }"
              @click="state.activeFilter = tab.key"
          >
            {{ tab.label }}
            <span class="tab-count" v-if="tab.count">{{ tab.count }}</span>
          </button>
        </div>
        <span class="live-badge">
          <span class="live-dot"></span>
          实时
        </span>
      </div>
    </div>

    <!-- 记录列表（滚动） -->
    <div class="table-scroll">
      <!-- 表头 -->
      <div class="grid-row grid-header">
        <div class="col-plate">车牌号码</div>
        <div class="col-gate">出入口</div>
        <div class="col-direction">方向</div>
        <div class="col-time">通行时间</div>
        <div class="col-status">通行状态</div>
      </div>

      <!-- 数据行 -->
      <div
          v-for="record in filteredRecords"
          :key="record.id"
          class="grid-row record-row"
          :class="record.statusClass"
      >
        <div class="col-plate">
          <span class="plate-text">{{ record.plate }}</span>
        </div>
        <div class="col-gate">
          <span class="gate-text">{{ record.gate }}</span>
        </div>
        <div class="col-direction">
          <span class="direction-badge" :class="record.type">
            <i
                class="fas"
                :class="record.type === 'in' ? 'fa-arrow-right-to-bracket' : 'fa-arrow-right-from-bracket'"
            ></i>
            {{ record.typeText }}
          </span>
        </div>
        <div class="col-time">
          <span class="time-text">{{ record.time }}</span>
        </div>
        <div class="col-status">
          <span class="status-badge" :class="record.statusClass">
            <i class="fas fa-circle"></i>
            {{ record.status }}
          </span>
        </div>
      </div>

      <!-- 空状态 -->
      <div class="empty-state" v-if="filteredRecords.length === 0">
        <i class="fas fa-car-side"></i>
        <span v-if="state.searchQuery">未找到匹配 "{{ state.searchQuery }}" 的记录</span>
        <span v-else>暂无通行记录</span>
      </div>
    </div>

    <!-- 底部：更新信息 + 翻页 -->
    <div class="table-footer">
      <div class="footer-left">
        <i class="fas fa-rotate"></i>
        最后更新 {{ state.updateTime }}
      </div>
      <div class="footer-right">
        <span class="pagination-info">1-{{ filteredRecords.length }} / {{ state.records.length }} 条</span>
        <button class="btn-page" :disabled="true"><i class="fas fa-chevron-left"></i></button>
        <button class="btn-page active">1</button>
        <button class="btn-page" :disabled="true"><i class="fas fa-chevron-right"></i></button>
      </div>
    </div>
  </div>
</template>

<script setup>
import {computed, reactive} from 'vue'

// 生成 100 条车辆通行记录
function generateRecords() {
  const provinces = ['粤', '京', '沪', '苏', '浙', '闽', '湘', '鄂', '川', '渝', '鲁', '豫', '皖', '赣', '冀', '津', '辽', '吉', '黑', '陕', '甘', '云', '贵', '桂', '琼']
  const letters = ['A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M', 'N', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z']

  const gates = [
    '#G01 主入口', '#G02 主出口', '#G03 地下车库', '#G04 应急通道',
    '#G05 南入口', '#G06 北出口', '#G07 东入口', '#G08 西出口',
    '#G09 B1入口', '#G10 B1出口', '#G11 VIP通道', '#G12 货运通道'
  ]

  const statuses = [
    {status: '正常', statusClass: 'normal', weight: 60},
    {status: '正常', statusClass: 'normal', weight: 60},
    {status: '正常', statusClass: 'normal', weight: 60},
    {status: '正常', statusClass: 'normal', weight: 60},
    {status: '正常', statusClass: 'normal', weight: 60},
    {status: '正常', statusClass: 'normal', weight: 60},
    {status: '人工放行', statusClass: 'warning', weight: 12},
    {status: '超时未缴费', statusClass: 'warning', weight: 10},
    {status: '无牌识别', statusClass: 'warning', weight: 8},
    {status: '拦截失败', statusClass: 'danger', weight: 5},
    {status: '黑名单车辆', statusClass: 'danger', weight: 3},
    {status: '识别异常', statusClass: 'danger', weight: 2}
  ]

  // 展平权重
  const statusPool = []
  statuses.forEach(s => {
    for (let i = 0; i < s.weight; i++) statusPool.push(s)
  })

  // 生成随机车牌
  function randomPlate() {
    const prov = provinces[Math.floor(Math.random() * provinces.length)]
    const letter = letters[Math.floor(Math.random() * letters.length)]
    const chars = 'ABCDEFGHJKLMNPQRSTUVWXYZ0123456789'
    let suffix = ''
    // 50% 字母+数字，50% 纯数字
    if (Math.random() < 0.5) {
      suffix = chars[Math.floor(Math.random() * chars.length)] + Math.floor(Math.random() * 10000).toString().padStart(4, '0')
    } else {
      suffix = chars[Math.floor(Math.random() * chars.length)] + Math.floor(Math.random() * 1000).toString().padStart(3, '0')
    }
    return `${prov}${letter}·${suffix}`
  }

  // 生成随机时间（今天，从 06:00:00 到 23:59:59）
  function randomTime() {
    const h = Math.floor(Math.random() * 18) + 6  // 6~23
    const m = Math.floor(Math.random() * 60)
    const s = Math.floor(Math.random() * 60)
    return `${h.toString().padStart(2, '0')}:${m.toString().padStart(2, '0')}:${s.toString().padStart(2, '0')}`
  }

  const records = []
  for (let i = 0; i < 100; i++) {
    const isIn = Math.random() < 0.55
    const status = statusPool[Math.floor(Math.random() * statusPool.length)]
    records.push({
      id: i + 1,
      plate: randomPlate(),
      gate: gates[Math.floor(Math.random() * gates.length)],
      type: isIn ? 'in' : 'out',
      typeText: isIn ? '入场' : '出场',
      time: randomTime(),
      status: status.status,
      statusClass: status.statusClass
    })
  }

  // 按时间降序排序
  records.sort((a, b) => b.time.localeCompare(a.time))
  // 重新分配 id
  records.forEach((r, i) => {
    r.id = i + 1
  })

  return records
}

const state = reactive({
  records: generateRecords(),
  updateTime: '14:35:42',
  searchQuery: '',
  activeFilter: 'all',
  filterTabs: [
    {key: 'all', label: '全部'},
    {key: 'in', label: '入场'},
    {key: 'out', label: '出场'},
    {key: 'abnormal', label: '异常'}
  ]
})

// 计算统计
const stats = computed(() => {
  const all = state.records
  return {
    total: all.length,
    in: all.filter(r => r.type === 'in').length,
    out: all.filter(r => r.type === 'out').length,
    abnormal: all.filter(r => r.statusClass !== 'normal').length
  }
})

// 筛选后的记录
const filteredRecords = computed(() => {
  let list = state.records

  // 筛选标签
  if (state.activeFilter === 'in') {
    list = list.filter(r => r.type === 'in')
  } else if (state.activeFilter === 'out') {
    list = list.filter(r => r.type === 'out')
  } else if (state.activeFilter === 'abnormal') {
    list = list.filter(r => r.statusClass !== 'normal')
  }

  // 搜索
  if (state.searchQuery.trim()) {
    const q = state.searchQuery.trim()
    list = list.filter(r => r.plate.includes(q))
  }

  return list
})
</script>

<style lang="less" scoped>
// ============================================
// 卡片通用样式
// ============================================
.record-card {
  flex: 1;
  overflow: auto;
  background: var(--color-bg-card);
  border-radius: var(--border-radius-xl);
  padding: var(--spacing-xl) var(--spacing-xxl);
  box-shadow: var(--shadow-base);
  display: flex;
  flex-direction: column;

  .card-header {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: var(--spacing-lg);
    border-bottom: 1px solid var(--color-border-light);
    padding-bottom: var(--spacing-md);

    .header-left {
      display: flex;
      align-items: center;
      gap: var(--spacing-sm);

      > i {
        color: var(--color-primary);
        font-size: var(--font-size-md);
      }

      h3 {
        font-weight: var(--font-weight-bold);
        font-size: var(--font-size-lg);
        color: var(--color-title);
        white-space: nowrap;
      }

      .header-divider {
        width: 1px;
        height: 20px;
        background: var(--color-border);
        margin: 0 var(--spacing-sm);
      }

      .header-stat-items {
        display: flex;
        align-items: center;
        gap: var(--spacing-md);

        .stat-item {
          display: inline-flex;
          align-items: center;
          gap: 4px;
          font-size: 12px;
          color: var(--color-text-secondary);

          i {
            font-size: 12px;
            color: var(--color-text-secondary);
          }

          strong {
            font-weight: var(--font-weight-bold);
            color: var(--color-title);
            font-size: 14px;
          }

          &.abnormal i,
          &.abnormal strong {
            color: var(--color-danger);
          }
        }
      }
    }

    .header-right {
      display: flex;
      align-items: center;
      gap: var(--spacing-md);
      flex-shrink: 0;

      .search-box {
        display: flex;
        align-items: center;
        gap: 6px;
        background: var(--color-bg);
        border: 1px solid var(--color-border);
        border-radius: var(--border-radius-lg);
        padding: 5px 10px;
        width: 180px;
        transition: border-color var(--transition-fast), box-shadow var(--transition-fast);

        &:focus-within {
          border-color: var(--color-primary);
          background: var(--color-bg-card);
          box-shadow: 0 0 0 3px rgba(22, 93, 255, 0.08);
        }

        i {
          font-size: 12px;
          color: var(--color-text-secondary);
        }

        input {
          border: none;
          outline: none;
          background: transparent;
          font-size: 12px;
          color: var(--color-title);
          width: 100%;

          &::placeholder {
            color: var(--color-text-secondary);
          }
        }
      }

      .filter-tabs {
        display: flex;
        align-items: center;
        gap: 1px;
        background: var(--color-bg);
        border-radius: 6px;
        padding: 2px;

        .filter-tab {
          border: none;
          background: transparent;
          padding: 4px 10px;
          border-radius: 4px;
          font-size: 12px;
          font-weight: var(--font-weight-medium);
          color: var(--color-text-secondary);
          cursor: pointer;
          transition: all var(--transition-fast);
          display: inline-flex;
          align-items: center;
          gap: 4px;

          .tab-count {
            background: var(--color-border);
            color: var(--color-text-secondary);
            font-size: 12px;
            padding: 0 5px;
            border-radius: 20px;
            line-height: 1.4;
          }

          &:hover {
            color: var(--color-body);
          }

          &.active {
            background: var(--color-bg-card);
            color: var(--color-primary);
            box-shadow: var(--shadow-sm);

            .tab-count {
              background: rgba(22, 93, 255, 0.1);
              color: var(--color-primary);
            }
          }
        }
      }

      .live-badge {
        display: inline-flex;
        align-items: center;
        gap: 6px;
        font-size: 12px;
        padding: 4px 10px;
        border-radius: 40px;
        font-weight: var(--font-weight-medium);
        color: var(--color-primary);
        background: rgba(22, 93, 255, 0.08);

        .live-dot {
          width: 6px;
          height: 6px;
          border-radius: 50%;
          background: var(--color-primary);
          animation: pulse-dot 1.5s ease-in-out infinite;
        }
      }
    }
  }
}

// ============================================
// 记录网格（div 表格）
// ============================================
.table-scroll {
  flex: 1;
  overflow-y: auto;
  min-height: 0;
  margin: 0 -4px;
  padding: 0 4px;

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-track {
    background: transparent;
  }

  &::-webkit-scrollbar-thumb {
    background: var(--color-border);
    border-radius: 20px;

    &:hover {
      background: var(--color-text-secondary);
    }
  }
}

.grid-row {
  display: grid;
  grid-template-columns: 1.2fr 1.4fr 0.8fr 1fr 1fr;
  align-items: center;
  gap: var(--spacing-sm);
  padding: 0 12px;
}

// 表头
.grid-header {
  position: sticky;
  top: 0;
  z-index: 2;
  background: var(--color-bg-card);
  font-size: 12px;
  font-weight: var(--font-weight-bold);
  color: var(--color-text-secondary);
  text-transform: uppercase;
  letter-spacing: 0.5px;
  padding: 8px 12px;
  border-bottom: 2px solid var(--color-border);
  margin-bottom: 4px;
}

// 数据行
.record-row {
  border-radius: var(--border-radius-lg);
  margin-bottom: 4px;
  transition: background var(--transition-fast);
  cursor: default;
  position: relative;

  .col-plate,
  .col-gate,
  .col-direction,
  .col-time,
  .col-status {
    padding: 10px 0;
    font-size: 13px;
    color: var(--color-body);
    min-width: 0;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }

  // 底色：浅灰 + 下边框
  &::before {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: var(--color-bg);
    border-bottom: 1px solid var(--color-border-light);
    border-radius: var(--border-radius-lg);
    z-index: 0;
  }

  &:hover::before {
    background: #f0f4ff;
  }

  // 异常行：左侧色条
  &.danger::before,
  &.warning::before {
    background: #fafbfd;
    border-left: 3px solid transparent;
    border-radius: var(--border-radius-lg);
  }

  &.danger::before {
    border-left-color: var(--color-danger);
  }

  &.warning::before {
    border-left-color: var(--color-warning);
  }

  &.danger:hover::before,
  &.warning:hover::before {
    background: #f5f7fc;
  }

  // 列内容 z-index 提升，显示在底色之上
  .col-plate,
  .col-gate,
  .col-direction,
  .col-time,
  .col-status {
    position: relative;
    z-index: 1;
  }

  .col-plate {
    .plate-text {
      font-weight: var(--font-weight-bold);
      color: var(--color-title);
      font-size: 14px;
      letter-spacing: 0.5px;
    }
  }

  .col-direction {
    .direction-badge {
      display: inline-flex;
      align-items: center;
      gap: 4px;
      padding: 2px 10px;
      border-radius: 40px;
      font-size: 12px;
      font-weight: var(--font-weight-bold);

      i {
        font-size: 12px;
      }

      &.in {
        background: rgba(0, 180, 42, 0.1);
        color: var(--color-success);
      }

      &.out {
        background: rgba(22, 93, 255, 0.1);
        color: var(--color-primary);
      }
    }
  }

  .col-time {
    .time-text {
      font-family: 'SF Mono', 'Consolas', 'Menlo', monospace;
      font-size: 13px;
      color: var(--color-body);
    }
  }

  .col-status {
    .status-badge {
      display: inline-flex;
      align-items: center;
      gap: 5px;
      font-size: 12px;
      font-weight: var(--font-weight-medium);
      padding: 2px 10px;
      border-radius: 40px;

      i {
        font-size: 6px;
      }

      &.normal {
        background: rgba(0, 180, 42, 0.08);
        color: var(--color-success);

        i {
          color: var(--color-success);
        }
      }

      &.warning {
        background: rgba(255, 125, 0, 0.08);
        color: var(--color-warning);

        i {
          color: var(--color-warning);
        }
      }

      &.danger {
        background: rgba(245, 63, 63, 0.08);
        color: var(--color-danger);

        i {
          color: var(--color-danger);
        }
      }
    }
  }
}

// 空状态
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: var(--spacing-sm);
  padding: 40px 0;
  color: var(--color-text-secondary);
  font-size: 13px;

  i {
    font-size: 36px;
    opacity: 0.25;
  }
}

// ============================================
// 表格底部
// ============================================
.table-footer {
  margin-top: var(--spacing-md);
  padding-top: var(--spacing-sm);
  border-top: 1px solid var(--color-border-light);
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-shrink: 0;

  .footer-left {
    font-size: 12px;
    color: var(--color-text-secondary);
    display: flex;
    align-items: center;
    gap: 4px;

    i {
      font-size: 12px;
    }
  }

  .footer-right {
    display: flex;
    align-items: center;
    gap: 4px;

    .pagination-info {
      font-size: 12px;
      color: var(--color-text-secondary);
      margin-right: var(--spacing-sm);
    }

    .btn-page {
      width: 26px;
      height: 26px;
      border: 1px solid var(--color-border);
      border-radius: var(--border-radius-base);
      background: var(--color-bg-card);
      color: var(--color-body);
      font-size: 12px;
      cursor: pointer;
      display: inline-flex;
      align-items: center;
      justify-content: center;
      transition: all var(--transition-fast);

      i {
        font-size: 12px;
      }

      &:hover:not(:disabled) {
        border-color: var(--color-primary);
        color: var(--color-primary);
      }

      &.active {
        background: var(--color-primary);
        border-color: var(--color-primary);
        color: #fff;
      }

      &:disabled {
        opacity: 0.4;
        cursor: not-allowed;
      }
    }
  }
}

// ============================================
// 动画
// ============================================
@keyframes pulse-dot {
  0%, 100% {
    opacity: 1;
    transform: scale(1);
  }
  50% {
    opacity: 0.5;
    transform: scale(0.75);
  }
}
</style>
