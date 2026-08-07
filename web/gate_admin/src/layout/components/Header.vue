<template>
  <header class="header">
    <div class="header-left">
      <div class="logo">
        <i class="fas fa-car"></i>
      </div>
      <div class="system-info">
        <span class="system-name">停车场管理系统</span>
      </div>
    </div>

    <div class="header-right">
      <div class="time-display">
        <i class="far fa-clock"></i>
        <span>{{ currentTime }}</span>
      </div>

      <div class="notification-btn" @click="handleNotificationClick">
        <i class="fas fa-bell"></i>
        <span class="notif-dot" v-if="unreadCount > 0">{{ unreadCount > 99 ? '99+' : unreadCount }}</span>
      </div>

      <div class="user-info">
        <span class="user-name">{{ userInfo.name }}</span>
        <span class="user-role">{{ isAdmin ? '管理员' : '值守员' }}</span>
      </div>

      <div class="action-btns">
        <span class="action-icon" title="锁屏" @click="handleLock">
          <i class="fas fa-lock"></i>
        </span>
        <span class="action-icon" title="退出登录" @click="handleLogout">
          <i class="fas fa-right-from-bracket"></i>
        </span>
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

const handleNotificationClick = () => {
  // Trigger notification panel
}

const handleLock = () => {
  systemStore.lockScreen()
}

const handleLogout = () => {
  router.push('/login')
  userStore.logout()
  Message.success('已退出登录')
}

onMounted(() => {
  updateTime()
  timeInterval = setInterval(updateTime, 1000)
})

onUnmounted(() => {
  if (timeInterval) {
    clearInterval(timeInterval)
  }
})
</script>

<style lang="less" scoped>
.header {
  height: 60px;
  background: var(--color-bg-card);
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
    }

    .system-info {
      display: flex;
      flex-direction: column;

      .system-name {
        font-size: 16px;
        font-weight: 600;
        color: var(--color-title);
        line-height: 1.2;
      }
    }
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 20px;

    .time-display {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 13px;
      color: var(--color-text-secondary);
      font-family: 'SF Mono', 'Consolas', 'Menlo', monospace;

      i {
        font-size: 13px;
      }
    }

    .notification-btn {
      position: relative;
      cursor: pointer;
      padding: 6px;
      border-radius: 6px;
      transition: background-color var(--transition-fast);
      color: var(--color-text-secondary);
      font-size: 18px;

      &:hover {
        background: var(--color-bg);
        color: var(--color-primary);
      }

      .notif-dot {
        position: absolute;
        top: 0;
        right: 0;
        min-width: 16px;
        height: 16px;
        background: var(--color-danger);
        color: #fff;
        font-size: 10px;
        font-weight: var(--font-weight-bold);
        border-radius: 20px;
        padding: 0 4px;
        display: flex;
        align-items: center;
        justify-content: center;
        line-height: 1;
        box-shadow: 0 0 0 2px var(--color-bg-card);
      }
    }

    .user-info {
      display: flex;
      flex-direction: column;
      align-items: flex-end;

      .user-name {
        font-size: 14px;
        font-weight: 500;
        color: var(--color-title);
        line-height: 1.2;
      }

      .user-role {
        font-size: 11px;
        color: var(--color-text-secondary);
        line-height: 1.2;
      }
    }

    .action-btns {
      display: flex;
      align-items: center;
      gap: 6px;
      padding-left: 16px;
      border-left: 1px solid var(--color-border);

      .action-icon {
        cursor: pointer;
        width: 32px;
        height: 32px;
        display: flex;
        align-items: center;
        justify-content: center;
        border-radius: 6px;
        color: var(--color-text-secondary);
        font-size: 16px;
        transition: all var(--transition-fast);

        &:hover {
          background: var(--color-bg);
          color: var(--color-primary);
        }
      }
    }
  }
}
</style>