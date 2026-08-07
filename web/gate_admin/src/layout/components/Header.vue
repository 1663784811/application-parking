<template>
  <header class="header">
    <div class="header-left">
      <!-- Logo 区域 -->
      <div class="logo">
        <i class="fas fa-car"></i>
      </div>
      <div class="brand">
        <span class="brand-name">停车场管理系统</span>
        <span class="brand-sub">Parking Management</span>
      </div>
      <div class="header-divider"></div>
      <span class="parking-name">{{ userInfo.parkingName || '城西停车场' }}</span>
    </div>

    <div class="header-right">
      <!-- 实时时钟 -->
      <div class="time-display">
        <i class="far fa-clock"></i>
        <span class="time-text">{{ currentTime }}</span>
      </div>

      <!-- 通知 -->
      <div class="notification-btn" @click="handleNotificationClick">
        <i class="fas fa-bell"></i>
        <span class="notif-dot" v-if="unreadCount > 0">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
      </div>

      <!-- 用户信息 + 下拉菜单 -->
      <div class="user-dropdown" ref="dropdownRef">
        <div class="user-trigger" @click="toggleDropdown">
          <div class="user-avatar">
            <i class="fas fa-user"></i>
          </div>
          <div class="user-meta">
            <span class="user-name">{{ userInfo.name }}</span>
            <span class="user-role">{{ isAdmin ? '管理员' : '值守员' }}</span>
          </div>
          <i class="fas fa-chevron-down dropdown-arrow" :class="{ open: dropdownOpen }"></i>
        </div>

        <!-- 下拉菜单 -->
        <Transition name="dropdown">
          <div class="dropdown-menu" v-if="dropdownOpen" @click.stop>
            <div class="dropdown-header">
              <div class="dropdown-avatar">
                <i class="fas fa-user"></i>
              </div>
              <div class="dropdown-user-meta">
                <span class="dropdown-user-name">{{ userInfo.name }}</span>
                <span class="dropdown-user-role">{{ isAdmin ? '管理员' : '值守员' }}</span>
              </div>
            </div>
            <div class="dropdown-divider"></div>
            <div class="dropdown-item" @click="handleLogout">
              <i class="fas fa-right-from-bracket"></i>
              <span>退出登录</span>
            </div>
          </div>
        </Transition>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Message } from 'view-ui-plus'
import { useUserStore } from '@/stores/user'
import { useSystemStore } from '@/stores/system'

const router = useRouter()
const userStore = useUserStore()
const systemStore = useSystemStore()

const currentTime = ref('')
const dropdownOpen = ref(false)
const dropdownRef = ref(null)
let timeInterval = null

const unreadCount = computed(() => systemStore.unreadNotificationCount)
const userInfo = computed(() => userStore.userInfo)
const isAdmin = computed(() => userStore.isAdmin)

const updateTime = () => {
  const now = new Date()
  currentTime.value = now.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  }).replace(/\//g, '-')
}

const toggleDropdown = () => {
  dropdownOpen.value = !dropdownOpen.value
}

const closeDropdown = (e) => {
  if (dropdownRef.value && !dropdownRef.value.contains(e.target)) {
    dropdownOpen.value = false
  }
}

const handleNotificationClick = () => {
  // Trigger notification panel
}

const handleLogout = () => {
  dropdownOpen.value = false
  router.push('/login')
  userStore.logout()
  Message.success('已退出登录')
}

onMounted(() => {
  updateTime()
  timeInterval = setInterval(updateTime, 1000)
  document.addEventListener('click', closeDropdown)
})

onUnmounted(() => {
  if (timeInterval) {
    clearInterval(timeInterval)
  }
  document.removeEventListener('click', closeDropdown)
})
</script>

<style lang="less" scoped>
.header {
  height: 60px;
  background: #fff;
  border-bottom: 1px solid var(--color-border-light);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  position: relative;
  z-index: 10;
  box-shadow: 0 1px 4px rgba(0, 0, 0, 0.06);
  flex-shrink: 0;

  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;
    min-width: 0;

    .logo {
      width: 36px;
      height: 36px;
      background: linear-gradient(135deg, var(--color-primary) 0%, #4080FF 100%);
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
      font-size: 16px;
      flex-shrink: 0;
      box-shadow: 0 2px 6px rgba(22, 93, 255, 0.2);
    }

    .brand {
      display: flex;
      flex-direction: column;
      gap: 1px;
      min-width: 0;

      .brand-name {
        font-size: 15px;
        font-weight: 600;
        color: var(--color-title);
        line-height: 1.2;
      }

      .brand-sub {
        font-size: 12px;
        color: var(--color-text-secondary);
        line-height: 1.2;
        letter-spacing: 0.5px;
        text-transform: uppercase;
      }
    }

    .header-divider {
      width: 1px;
      height: 20px;
      background: var(--color-border);
      margin: 0 4px;
      flex-shrink: 0;
    }

    .parking-name {
      font-size: 12px;
      color: var(--color-text-secondary);
      padding: 2px 10px;
      background: var(--color-bg);
      border-radius: 20px;
      font-weight: var(--font-weight-medium);
      white-space: nowrap;
      overflow: hidden;
      text-overflow: ellipsis;
    }
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 16px;
    flex-shrink: 0;

    .time-display {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 14px;
      color: var(--color-text-secondary);
      padding: 0 14px;
      border-right: 1px solid var(--color-border-light);

      i {
        font-size: 14px;
      }

      .time-text {
        font-family: 'SF Mono', 'Consolas', 'Menlo', monospace;
        font-variant-numeric: tabular-nums;
      }
    }

    .notification-btn {
      position: relative;
      cursor: pointer;
      width: 34px;
      height: 34px;
      display: flex;
      align-items: center;
      justify-content: center;
      border-radius: 8px;
      transition: all var(--transition-fast);
      color: var(--color-text-secondary);
      font-size: 18px;

      &:hover {
        background: var(--color-bg);
        color: var(--color-primary);
        transform: translateY(-1px);
      }

      .notif-dot {
        position: absolute;
        top: 2px;
        right: 2px;
        min-width: 16px;
        height: 16px;
        background: var(--color-danger);
        color: #fff;
        font-size: 12px;
        font-weight: var(--font-weight-bold);
        border-radius: 20px;
        padding: 0 4px;
        display: flex;
        align-items: center;
        justify-content: center;
        line-height: 1;
        box-shadow: 0 0 0 2px #fff;
        animation: notif-pulse 2s ease-in-out infinite;
      }
    }

    // ============================================
    // 用户下拉菜单
    // ============================================
    .user-dropdown {
      position: relative;

      .user-trigger {
        display: flex;
        align-items: center;
        gap: 8px;
        padding: 4px 10px 4px 4px;
        border-radius: 8px;
        cursor: pointer;
        transition: all var(--transition-fast);
        user-select: none;

        &:hover {
          background: var(--color-bg);
        }

        .user-avatar {
          width: 32px;
          height: 32px;
          border-radius: 50%;
          background: linear-gradient(135deg, var(--color-primary) 0%, #4080FF 100%);
          display: flex;
          align-items: center;
          justify-content: center;
          color: #fff;
          font-size: 14px;
          flex-shrink: 0;
        }

        .user-meta {
          display: flex;
          flex-direction: column;
          gap: 1px;

          .user-name {
            font-size: 14px;
            font-weight: 600;
            color: var(--color-title);
            line-height: 1.2;
          }

          .user-role {
            font-size: 12px;
            color: var(--color-text-secondary);
            line-height: 1.2;
          }
        }

        .dropdown-arrow {
          font-size: 12px;
          color: var(--color-text-secondary);
          transition: transform var(--transition-fast);
          margin-left: 4px;

          &.open {
            transform: rotate(180deg);
          }
        }
      }

      // 下拉菜单面板
      .dropdown-menu {
        position: absolute;
        top: calc(100% + 8px);
        right: 0;
        width: 200px;
        background: #fff;
        border-radius: 10px;
        box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
        border: 1px solid var(--color-border-light);
        overflow: hidden;
        z-index: 1000;

        .dropdown-header {
          display: flex;
          align-items: center;
          gap: 12px;
          padding: 16px 16px 12px;

          .dropdown-avatar {
            width: 40px;
            height: 40px;
            border-radius: 50%;
            background: linear-gradient(135deg, var(--color-primary) 0%, #4080FF 100%);
            display: flex;
            align-items: center;
            justify-content: center;
            color: #fff;
            font-size: 16px;
            flex-shrink: 0;
          }

          .dropdown-user-meta {
            display: flex;
            flex-direction: column;
            gap: 2px;
            min-width: 0;

            .dropdown-user-name {
              font-size: 15px;
              font-weight: 600;
              color: var(--color-title);
              line-height: 1.2;
            }

            .dropdown-user-role {
              font-size: 12px;
              color: var(--color-text-secondary);
              line-height: 1.2;
            }
          }
        }

        .dropdown-divider {
          height: 1px;
          background: var(--color-border-light);
          margin: 0 12px;
        }

        .dropdown-item {
          display: flex;
          align-items: center;
          gap: 10px;
          padding: 12px 16px;
          font-size: 14px;
          color: var(--color-body);
          cursor: pointer;
          transition: all var(--transition-fast);

          i {
            width: 18px;
            font-size: 15px;
            color: var(--color-text-secondary);
            text-align: center;
          }

          &:hover {
            background: var(--color-bg);
            color: var(--color-danger);

            i {
              color: var(--color-danger);
            }
          }

          &:last-child {
            border-bottom-left-radius: 10px;
            border-bottom-right-radius: 10px;
          }
        }
      }
    }
  }
}

// 下拉菜单过渡动画
.dropdown-enter-active {
  transition: all 0.2s ease-out;
}
.dropdown-leave-active {
  transition: all 0.15s ease-in;
}
.dropdown-enter-from {
  opacity: 0;
  transform: translateY(-8px) scale(0.96);
}
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-4px) scale(0.98);
}

// 通知徽标脉冲动画
@keyframes notif-pulse {
  0%, 100% {
    box-shadow: 0 0 0 2px #fff, 0 0 0 4px rgba(245, 63, 63, 0);
  }
  50% {
    box-shadow: 0 0 0 2px #fff, 0 0 0 6px rgba(245, 63, 63, 0.15);
  }
}
</style>
