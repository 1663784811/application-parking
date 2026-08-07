<template>
  <!-- 右上：车辆通行图片 -->
  <div class="card image-card">
    <!-- 卡片头部 -->
    <div class="card-header">
      <div class="header-left">
        <i class="fas fa-camera"></i>
        <h3>车辆通行图片</h3>
        <div class="header-divider"></div>
        <span class="header-stat">
          <i class="fas fa-images"></i>
          {{ state.images.length }} 张抓拍
        </span>
      </div>
      <div class="header-right">
        <span class="live-badge">
          <span class="live-dot"></span>
          实时
        </span>
        <span class="refresh-btn" @click="handleRefresh">
          <i class="fas fa-rotate" :class="{ spinning: state.refreshing }"></i>
        </span>
      </div>
    </div>

    <!-- 图片网格 -->
    <div class="image-grid">
      <div
        class="image-box"
        v-for="img in state.images"
        :key="img.id"
        :class="img.direction"
      >
        <!-- 占位图标 -->
        <i class="fas fa-camera placeholder-icon"></i>

        <!-- 左上：车牌 + 方向 -->
        <span class="plate-overlay">
          <i class="fas fa-car"></i>
          {{ img.plate }}
        </span>

        <!-- 右上：方向标签 -->
        <span class="direction-tag" :class="img.direction">
          <i class="fas" :class="img.direction === 'in' ? 'fa-arrow-right-to-bracket' : 'fa-arrow-right-from-bracket'"></i>
          {{ img.direction === 'in' ? '入场' : '出场' }}
        </span>

        <!-- 左下：抓拍编号 -->
        <span class="img-id">{{ img.label }}</span>

        <!-- 右下：时间 -->
        <span class="time-label">
          <i class="far fa-clock"></i>
          {{ img.time }}
        </span>
      </div>
    </div>
  </div>
</template>

<script setup>
import {reactive} from 'vue'

const state = reactive({
  images: [
    {id: 1, label: '抓拍 #01', plate: '粤A·8K92F', time: '14:28:15', direction: 'in'},
    {id: 2, label: '抓拍 #02', plate: '粤A·8K92F', time: '14:28:16', direction: 'in'},
    {id: 3, label: '抓拍 #03', plate: '粤A·8K92F', time: '14:35:42', direction: 'out'},
    {id: 4, label: '抓拍 #04', plate: '粤A·8K92F', time: '14:35:43', direction: 'out'}
  ],
  refreshing: false
})

const handleRefresh = () => {
  if (state.refreshing) return
  state.refreshing = true
  setTimeout(() => { state.refreshing = false }, 800)
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

      .header-stat {
        font-size: var(--font-size-xs);
        color: var(--color-text-secondary);
        display: inline-flex;
        align-items: center;
        gap: 4px;

        i {
          font-size: 12px;
        }
      }
    }

    .header-right {
      display: flex;
      align-items: center;
      gap: var(--spacing-sm);
      flex-shrink: 0;

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

      .refresh-btn {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        width: 28px;
        height: 28px;
        border-radius: 50%;
        color: var(--color-text-secondary);
        cursor: pointer;
        transition: all var(--transition-fast);

        &:hover {
          background: var(--color-bg);
          color: var(--color-primary);
        }

        i {
          font-size: 13px;

          &.spinning {
            animation: icon-spin 0.8s linear infinite;
          }
        }
      }
    }
  }
}

// ============================================
// 图片网格
// ============================================
.image-card {
  .image-grid {
    display: grid;
    grid-template-columns: 1fr 1fr;
    gap: var(--spacing-md);

    .image-box {
      position: relative;
      border-radius: var(--border-radius-lg);
      aspect-ratio: 16/9;
      background: #1a2a3a;
      border: 1px solid var(--color-border);
      border-top: 3px solid var(--color-success);
      display: flex;
      align-items: center;
      justify-content: center;
      overflow: hidden;
      transition: all var(--transition-normal);
      cursor: default;

      &:hover {
        box-shadow: var(--shadow-md);
        transform: translateY(-2px);
      }

      // 出场方向：蓝色顶边
      &.out {
        border-top-color: var(--color-primary);
      }

      // 占位图标（居中）
      .placeholder-icon {
        font-size: 32px;
        opacity: 0.3;
        color: rgba(255, 255, 255, 0.5);
        z-index: 1;
      }

      // 左上：车牌
      .plate-overlay {
        position: absolute;
        top: 0;
        left: 0;
        z-index: 2;
        display: inline-flex;
        align-items: center;
        gap: 4px;
        padding: 5px 8px;
        background: linear-gradient(135deg, rgba(0, 0, 0, 0.6), transparent);
        border-bottom-right-radius: var(--border-radius-lg);
        font-size: 12px;
        font-weight: var(--font-weight-bold);
        color: #ffffff;
        max-width: 70%;

        i {
          font-size: 12px;
        }
      }

      // 右上：方向标签
      .direction-tag {
        position: absolute;
        top: 5px;
        right: 5px;
        z-index: 2;
        display: inline-flex;
        align-items: center;
        gap: 3px;
        padding: 2px 8px;
        border-radius: 20px;
        font-size: 12px;
        font-weight: var(--font-weight-bold);
        backdrop-filter: blur(4px);

        i {
          font-size: 9px;
        }

        &.in {
          background: rgba(0, 180, 42, 0.85);
          color: #ffffff;
        }

        &.out {
          background: rgba(22, 93, 255, 0.85);
          color: #ffffff;
        }
      }

      // 左下：抓拍编号
      .img-id {
        position: absolute;
        bottom: 6px;
        left: 8px;
        z-index: 2;
        font-size: 12px;
        font-weight: var(--font-weight-bold);
        color: rgba(255, 255, 255, 0.7);
      }

      // 右下：时间
      .time-label {
        position: absolute;
        bottom: 6px;
        right: 8px;
        z-index: 2;
        display: inline-flex;
        align-items: center;
        gap: 3px;
        font-size: 12px;
        font-weight: var(--font-weight-medium);
        color: rgba(255, 255, 255, 0.7);

        i {
          font-size: 8px;
        }
      }
    }
  }
}

// ============================================
// 动画
// ============================================
@keyframes pulse-dot {
  0%, 100% { opacity: 1; transform: scale(1); }
  50% { opacity: 0.5; transform: scale(0.75); }
}

@keyframes icon-spin {
  from { transform: rotate(0deg); }
  to { transform: rotate(360deg); }
}
</style>
