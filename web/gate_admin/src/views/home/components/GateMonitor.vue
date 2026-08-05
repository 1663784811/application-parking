<template>
  <!-- 1.2.1 道闸实时监控 -->
  <div class="card gate-monitor-card">
    <!-- 卡片头部 -->
    <div class="card-header">
      <div class="header-title">
        <i class="fas fa-gate"></i>
        <h3>道闸实时监控</h3>
      </div>
      <div class="header-stats">
        <span class="stat-pill online">
          <i class="fas fa-circle"></i>
          {{ state.gateStats.online }} 在线
        </span>
        <span class="stat-pill abnormal">
          <i class="fas fa-circle"></i>
          {{ state.gateStats.offline + state.gateStats.fault }} 异常
        </span>
        <span class="refresh-btn" @click="handleRefresh">
          <i class="fas fa-rotate" :class="{ spinning: state.refreshing }"></i>
          刷新
        </span>
      </div>
    </div>

    <!-- 道闸网格 - 上下滚动 -->
    <div class="gate-scroll-wrapper">
      <div
          class="gate-item"
          v-for="gate in state.gateList"
          :key="gate.id"
          :class="[gate.status, gate.cameraStatus]"
      >
        <!-- 占位图标 + 文案（居中） -->
        <i class="fas camera-placeholder-icon" :class="gate.cameraIcon"></i>
        <span class="camera-placeholder-text">{{ gate.cameraLabel }}</span>

        <!-- 左上：道闸编号 + 位置 -->
        <div class="camera-info">
          <span class="gate-name">{{ gate.name }}</span>
          <span class="gate-location">{{ gate.location }}</span>
        </div>

        <!-- 右上：状态徽标 -->
        <span class="gate-status-dot" :class="gate.status">
          <i class="fas fa-circle"></i>
          {{ gate.statusText }}
        </span>

        <!-- 左下：摄像头编号 -->
        <span class="camera-id">{{ gate.cameraId }}</span>

        <!-- 右下：摄像头状态 -->
        <span class="camera-status" :class="gate.cameraStatus">
          <i class="fas fa-circle"></i>
          {{ gate.cameraStatusText }}
        </span>

        <!-- 信号强度指示 -->
        <div class="signal-bar" v-if="gate.signal > 0">
          <span
              v-for="n in 4"
              :key="n"
              class="signal-cell"
              :class="{ active: gate.signal >= n * 25 }"
          ></span>
        </div>

        <!-- 异常告警条 -->
        <div class="gate-alert" v-if="gate.alert">
          <i class="fas" :class="gate.alertIcon"></i>
          {{ gate.alertText }}
        </div>
      </div>
    </div>

    <!-- 最后更新时间 -->
    <div class="gate-update-time">
      <i class="fas fa-rotate"></i>
      姿态实时刷新 · 最后更新 {{ state.updateTime }}
    </div>
  </div>
</template>

<script setup>
import {reactive} from 'vue'

// 生成 20 个道闸数据
function generateGateData() {
  const locations = [
    '北门 · 入口', '北门 · 出口', '南门 · 入口', '南门 · 出口',
    '东门 · 入口', '东门 · 出口', '西门 · 入口', '西门 · 出口',
    'B1 · 入口', 'B1 · 出口', 'B2 · 入口', 'B2 · 出口',
    '应急通道 · 东', '应急通道 · 西', 'VIP · 入口', 'VIP · 出口',
    '员工通道 · 南', '员工通道 · 北', '货运通道', '临时通道'
  ]

  const statuses = ['normal', 'normal', 'normal', 'normal', 'normal', 'offline', 'fault']
  const gateActions = ['开启', '关闭']

  return locations.map((loc, index) => {
    const status = statuses[index % statuses.length]
    const isNormal = status === 'normal'
    const isOffline = status === 'offline'
    const isFault = status === 'fault'

    return {
      id: index + 1,
      name: `#G${String(index + 1).padStart(2, '0')}`,
      location: loc,
      status: status,
      statusText: isNormal ? '在线' : isOffline ? '离线' : '故障',
      gateAction: gateActions[index % 2],
      alert: isOffline || isFault,
      alertText: isOffline ? '信号丢失' : '闸杆异常悬停',
      alertIcon: isOffline ? 'fa-wifi-slash' : 'fa-tools',
      cameraStatus: isNormal ? 'online' : isOffline ? 'offline' : 'fault',
      cameraIcon: isNormal ? 'fa-video' : isOffline ? 'fa-video-slash' : 'fa-exclamation-triangle',
      cameraLabel: isNormal ? '实时画面' : isOffline ? '摄像头离线' : '画面异常',
      cameraId: `CAM-${String(index + 1).padStart(2, '0')}`,
      cameraStatusText: isNormal ? '直播中' : isOffline ? '已断开' : '信号中断',
      // 信号强度：在线 70~98，离线 0，故障 20~35
      signal: isNormal ? 70 + (index % 5) * 7 : isOffline ? 0 : 20 + (index % 4) * 5
    }
  })
}

const state = reactive({
  // 道闸列表
  gateList: generateGateData(),
  // 最后更新时间
  updateTime: '14:28:32',
  // 刷新动画状态
  refreshing: false,
  // 在线/离线/故障统计（随 gateList 变化自动更新）
  get gateStats() {
    const stats = {online: 0, offline: 0, fault: 0}
    this.gateList.forEach(gate => {
      if (gate.status === 'normal') stats.online++
      else if (gate.status === 'offline') stats.offline++
      else if (gate.status === 'fault') stats.fault++
    })
    return stats
  }
})

// 手动刷新
const handleRefresh = () => {
  if (state.refreshing) return
  state.refreshing = true
  // 模拟刷新：稍后还原动画状态
  setTimeout(() => {
    state.refreshing = false
  }, 800)
}
</script>

<style lang="less" scoped>
// ============================================
// 卡片通用样式
// ============================================
.card {
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

    .header-title {
      display: flex;
      align-items: center;
      gap: var(--spacing-sm);

      i {
        color: var(--color-primary);
        font-size: var(--font-size-md);
      }

      h3 {
        font-weight: var(--font-weight-bold);
        font-size: var(--font-size-lg);
        color: var(--color-title);
      }
    }

    .header-stats {
      display: flex;
      align-items: center;
      gap: var(--spacing-sm);

      .stat-pill {
        display: inline-flex;
        align-items: center;
        gap: 4px;
        font-size: var(--font-size-xs);
        padding: 4px 12px;
        border-radius: 40px;
        font-weight: var(--font-weight-medium);

        i {
          font-size: 6px;
        }

        &.online {
          background: rgba(0, 180, 42, 0.1);
          color: var(--color-success);

          i {
            color: var(--color-success);
          }
        }

        &.abnormal {
          background: rgba(245, 63, 63, 0.1);
          color: var(--color-danger);

          i {
            color: var(--color-danger);
          }
        }
      }

      .refresh-btn {
        display: inline-flex;
        align-items: center;
        gap: 4px;
        font-size: var(--font-size-xs);
        padding: 4px 12px;
        border-radius: 40px;
        color: var(--color-primary);
        background: rgba(22, 93, 255, 0.08);
        cursor: pointer;
        user-select: none;
        transition: background var(--transition-fast);

        &:hover {
          background: rgba(22, 93, 255, 0.16);
        }

        i {
          font-size: 11px;

          &.spinning {
            animation: icon-spin 0.8s linear infinite;
          }
        }
      }
    }
  }
}

// ============================================
// 道闸监控模块
// ============================================
.gate-monitor-card {
  height: 520px;

  .gate-scroll-wrapper {
    flex: 1;
    overflow: auto;
    min-height: 0;
    padding-right: 4px;
    // 网格布局（兼作滚动容器 + 网格）
    display: grid;
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
    gap: var(--spacing-md);
    align-content: start;

    &::-webkit-scrollbar {
      width: 6px;
    }

    &::-webkit-scrollbar-track {
      background: var(--color-border-light);
      border-radius: 10px;
    }

    &::-webkit-scrollbar-thumb {
      background: var(--color-border);
      border-radius: 10px;

      &:hover {
        background: var(--color-text-secondary);
      }
    }

    .gate-item {
      // 卡片框架 + 预览区合一
      position: relative;
      min-width: 0;
      border-radius: var(--border-radius-lg);
      border: 1px solid var(--color-border);
      border-top: 3px solid var(--color-success);
      aspect-ratio: 16/9;
      background: #1a2a3a;
      display: flex;
      flex-direction: column;
      align-items: center;
      justify-content: center;
      gap: 4px;
      transition: all var(--transition-normal);

      &:hover {
        box-shadow: var(--shadow-md);
        transform: translateY(-2px);
      }

      // ===== 状态：离线（框架灰 + 暗屏偏灰） =====
      &.offline {
        background: #2d3d4d;
        border-color: var(--color-border);
        border-top-color: var(--color-text-secondary);
        opacity: 0.9;
      }

      // ===== 状态：故障（框架红 + 暗屏偏红 + 闪烁） =====
      &.fault {
        background: #3d2d2d;
        border-color: rgba(245, 63, 63, 0.3);
        border-top-color: var(--color-danger);
        animation: camera-blink 1.5s ease-in-out infinite;
      }

      // 占位图标（居中）
      .camera-placeholder-icon {
        font-size: 26px;
        opacity: 0.45;
        color: rgba(255, 255, 255, 0.5);
        z-index: 1;
      }

      // 占位文案（居中）
      .camera-placeholder-text {
        position: absolute;
        bottom: 26%;
        left: 50%;
        transform: translateX(-50%);
        font-size: 10px;
        font-weight: var(--font-weight-normal);
        color: rgba(255, 255, 255, 0.5);
        z-index: 1;
        white-space: nowrap;
      }

      // 左上：道闸编号 + 位置
      .camera-info {
        position: absolute;
        top: 0;
        left: 0;
        z-index: 2;
        display: flex;
        flex-direction: column;
        gap: 1px;
        padding: 6px 8px;
        background: linear-gradient(135deg, rgba(0, 0, 0, 0.55), transparent);
        border-bottom-right-radius: var(--border-radius-lg);
        max-width: 70%;

        .gate-name {
          font-size: var(--font-size-sm);
          font-weight: var(--font-weight-bold);
          color: #ffffff;
          letter-spacing: 0.3px;
          line-height: 1.2;
        }

        .gate-location {
          font-size: 10px;
          color: rgba(255, 255, 255, 0.8);
          overflow: hidden;
          text-overflow: ellipsis;
          white-space: nowrap;
        }
      }

      // 右上：状态徽标
      .gate-status-dot {
        position: absolute;
        top: 6px;
        right: 6px;
        z-index: 2;
        font-size: 10px;
        font-weight: var(--font-weight-medium);
        padding: 2px 8px;
        border-radius: 20px;
        display: inline-flex;
        align-items: center;
        gap: 4px;
        backdrop-filter: blur(4px);

        i {
          font-size: 6px;
        }

        &.normal {
          background: rgba(0, 180, 42, 0.85);
          color: #ffffff;

          i {
            color: #ffffff;
          }
        }

        &.offline {
          background: rgba(134, 144, 156, 0.85);
          color: #ffffff;

          i {
            color: #ffffff;
          }
        }

        &.fault {
          background: rgba(245, 63, 63, 0.9);
          color: #ffffff;

          i {
            color: #ffffff;
          }
        }
      }

      // 左下：摄像头编号
      .camera-id {
        position: absolute;
        bottom: 6px;
        left: 8px;
        z-index: 2;
        font-size: 9px;
        font-weight: var(--font-weight-bold);
        color: rgba(255, 255, 255, 0.85);
      }

      // 右下：摄像头状态
      .camera-status {
        position: absolute;
        bottom: 6px;
        right: 8px;
        z-index: 2;
        display: inline-flex;
        align-items: center;
        gap: 3px;
        font-size: 9px;
        font-weight: var(--font-weight-medium);

        i {
          font-size: 5px;
        }

        &.online {
          color: var(--color-success-light);

          i {
            color: var(--color-success-light);
          }
        }

        &.offline {
          color: #95a9bf;

          i {
            color: #95a9bf;
          }
        }

        &.fault {
          color: var(--color-danger-light);

          i {
            color: var(--color-danger-light);
          }
        }
      }

      // 信号强度指示
      .signal-bar {
        position: absolute;
        top: 6px;
        left: 50%;
        transform: translateX(-50%);
        z-index: 2;
        display: flex;
        align-items: flex-end;
        gap: 2px;

        .signal-cell {
          width: 3px;
          border-radius: 1px;
          background: rgba(255, 255, 255, 0.25);

          &:nth-child(1) {
            height: 4px;
          }

          &:nth-child(2) {
            height: 6px;
          }

          &:nth-child(3) {
            height: 8px;
          }

          &:nth-child(4) {
            height: 10px;
          }

          &.active {
            background: var(--color-success-light);
          }
        }
      }

      // 异常告警条（贴在预览底部）
      .gate-alert {
        position: absolute;
        bottom: 0;
        left: 0;
        right: 0;
        z-index: 3;
        background: rgba(245, 63, 63, 0.92);
        color: #ffffff;
        font-size: 10px;
        font-weight: var(--font-weight-medium);
        padding: 3px 10px;
        display: flex;
        align-items: center;
        gap: 4px;

        i {
          font-size: 10px;
        }
      }

      @keyframes camera-blink {
        0%, 100% {
          opacity: 1;
        }
        50% {
          opacity: 0.5;
        }
      }
    }
  }

  .gate-update-time {
    margin-top: var(--spacing-sm);
    font-size: var(--font-size-xs);
    color: var(--color-text-secondary);
    flex-shrink: 0;
    display: flex;
    align-items: center;
    gap: 4px;

    i {
      font-size: 11px;
    }
  }
}

// 刷新图标旋转动画
@keyframes icon-spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}
</style>
