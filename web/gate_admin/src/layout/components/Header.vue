<template>
  <header class="header">
    <div class="header-left">
      <div class="logo">
        <Icon type="ios-car" size="24" />
      </div>
      <div class="system-info">
        <span class="system-name">停车场管理系统</span>
      </div>
    </div>

    <div class="header-right">
      <div class="time-display">
        <Icon type="ios-time-outline" />
        <span>{{ currentTime }}</span>
      </div>

      <div class="notification-btn" @click="handleNotificationClick">
        <Badge :count="unreadCount" :overflow-count="99">
          <Icon type="ios-notifications-outline" size="22" />
        </Badge>
      </div>

      <div class="user-info">
        <span class="user-name">{{ userInfo.name }}</span>
        <span class="user-role">{{ isAdmin ? '管理员' : '值守员' }}</span>
      </div>

      <div class="action-btns">
        <Tooltip content="锁屏">
          <Icon type="ios-lock-outline" size="20" class="action-icon" @click="handleLock" />
        </Tooltip>
        <Tooltip content="退出登录">
          <Icon type="ios-log-out-outline" size="20" class="action-icon" @click="handleLogout" />
        </Tooltip>
      </div>
    </div>
  </header>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { Icon, Badge, Tooltip, message } from 'view-ui-plus'
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
  message.success('已退出登录')
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
  background-color: #fff;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  position: relative;
  z-index: 10;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);

  .header-left {
    display: flex;
    align-items: center;
    gap: 12px;

    .logo {
      width: 36px;
      height: 36px;
      background: linear-gradient(135deg, #165DFF, #4080FF);
      border-radius: 8px;
      display: flex;
      align-items: center;
      justify-content: center;
      color: #fff;
    }

    .system-info {
      display: flex;
      flex-direction: column;

      .system-name {
        font-size: 16px;
        font-weight: 600;
        color: #1a1a1a;
        line-height: 1.2;
      }
    }
  }

  .header-right {
    display: flex;
    align-items: center;
    gap: 16px;

    .time-display {
      display: flex;
      align-items: center;
      gap: 6px;
      font-size: 14px;
      color: #666;
    }

    .notification-btn {
      cursor: pointer;
      padding: 6px;
      border-radius: 6px;
      transition: background-color 0.2s;

      &:hover {
        background-color: #f5f7fa;
      }
    }

    .user-info {
      display: flex;
      flex-direction: column;
      align-items: flex-end;

      .user-name {
        font-size: 14px;
        font-weight: 500;
        color: #1a1a1a;
        line-height: 1.2;
      }

      .user-role {
        font-size: 12px;
        color: #999;
        line-height: 1.2;
      }
    }

    .action-btns {
      display: flex;
      align-items: center;
      gap: 8px;

      .action-icon {
        cursor: pointer;
        color: #666;
        transition: color 0.2s;

        &:hover {
          color: #165DFF;
        }
      }
    }
  }
}
</style>