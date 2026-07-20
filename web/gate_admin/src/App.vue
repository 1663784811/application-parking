<template>
  <div class="gate-admin-app">
    <!-- 顶部操作栏 -->
    <header class="top-bar">
      <div class="top-bar-left">
        <div class="system-logo">
          <Icon type="ios-car" size="24" />
        </div>
        <div class="system-info">
          <span class="system-name">停车场门口管理端</span>
          <span class="parking-name">{{ systemStore.stats.parkingName || '城西停车场' }}</span>
        </div>
      </div>

      <div class="top-bar-center">
        <div class="search-box">
          <Input
            v-model="searchKeyword"
            placeholder="搜索车牌、订单号"
            search
            @on-search="handleSearch"
            class="search-input"
          >
            <template #prefix>
              <Icon type="ios-search" />
            </template>
          </Input>
        </div>
        <div class="time-display">
          <Icon type="ios-time-outline" />
          <span>{{ currentTime }}</span>
        </div>
      </div>

      <div class="top-bar-right">
        <!-- 消息通知 -->
        <div class="notification-btn" @click="handleNotificationClick">
          <Badge :count="systemStore.unreadNotificationCount" :overflow-count="99">
            <Icon type="ios-notifications-outline" size="22" />
          </Badge>
        </div>

        <!-- 紧急求助 -->
        <div class="help-btn" @click="handleHelp">
          <Icon type="ios-call-outline" size="22" />
          <span>紧急求助</span>
        </div>

        <!-- 用户信息 -->
        <div class="user-info">
          <span class="user-name">{{ userStore.userInfo.name }}</span>
          <span class="user-role">{{ userStore.isAdmin ? '管理员' : '值守员' }}</span>
        </div>

        <!-- 退出登录 -->
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

    <div class="main-container">
      <!-- 左侧快捷导航 -->
      <nav class="side-bar" :class="{ collapsed: sideBarCollapsed }">
        <div class="nav-menu">
          <div
            v-for="item in menuItems"
            :key="item.path"
            class="nav-item"
            :class="{ active: currentRoute === item.path }"
            @click="handleNavClick(item.path)"
            v-show="!item.adminOnly || userStore.isAdmin"
          >
            <Icon :type="item.icon" size="20" />
            <span class="nav-text">{{ item.name }}</span>
          </div>
        </div>

        <div class="nav-footer" @click="toggleSideBar">
          <Icon :type="sideBarCollapsed ? 'ios-arrow-forward' : 'ios-arrow-back'" size="16" />
        </div>
      </nav>

      <!-- 主内容区 -->
      <main class="content-area" :class="{ 'side-collapsed': sideBarCollapsed }">
        <router-view />
      </main>
    </div>

    <!-- 锁屏界面 -->
    <LockScreen v-if="systemStore.isLocked" />

    <!-- 通知面板 -->
    <NotificationPanel v-model:visible="notificationVisible" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { Message, Modal, Tooltip, Badge } from 'view-ui-plus'
import { useUserStore } from '@/stores/user'
import { useSystemStore } from '@/stores/system'
import LockScreen from '@/components/LockScreen.vue'
import NotificationPanel from '@/components/NotificationPanel.vue'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const systemStore = useSystemStore()

// 状态
const searchKeyword = ref('')
const sideBarCollapsed = ref(false)
const notificationVisible = ref(false)

// 当前时间
const currentTime = ref('')
let timeInterval = null

// 菜单项
const menuItems = [
  { path: '/home', name: '首页', icon: 'ios-home-outline', adminOnly: false },
  { path: '/monitor', name: '实时通行监控', icon: 'ios-videocam-outline', adminOnly: false },
  { path: '/release', name: '车辆放行管理', icon: 'ios-car-outline', adminOnly: false },
  { path: '/exception', name: '异常车辆处理', icon: 'ios-alert-outline', adminOnly: false },
  { path: '/equipment', name: '设备状态监控', icon: 'ios-construct-outline', adminOnly: false },
  { path: '/log', name: '现场操作日志', icon: 'ios-document-text-outline', adminOnly: false },
  { path: '/settings', name: '基础设置', icon: 'ios-settings-outline', adminOnly: true }
]

// 当前路由
const currentRoute = computed(() => route.path)

// 更新时间
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

// 搜索
const handleSearch = (value) => {
  if (value) {
    Message.info(`搜索：${value}`)
    // 实际应用跳转到搜索结果页
  }
}

// 导航点击
const handleNavClick = (path) => {
  router.push(path)
}

// 通知点击
const handleNotificationClick = () => {
  notificationVisible.value = true
}

// 紧急求助
const handleHelp = () => {
  Modal.info({
    title: '紧急求助',
    content: '正在联系后台管理员...<br>电话：400-888-8888',
    okText: '呼叫'
  })
}

// 锁屏
const handleLock = () => {
  systemStore.lockScreen()
}

// 退出登录
const handleLogout = () => {
  Modal.confirm({
    title: '确认退出',
    content: '确定要退出登录吗？',
    onOk: () => {
      userStore.logout()
      router.push('/login')
      Message.success('已退出登录')
    }
  })
}

// 切换侧边栏
const toggleSideBar = () => {
  sideBarCollapsed.value = !sideBarCollapsed.value
}

// 模拟数据
const initData = () => {
  systemStore.updateStats({
    currentVehicles: 156,
    totalSpaces: 300,
    todayIn: 428,
    todayOut: 396,
    pendingExceptions: 3,
    onlineDevices: 7,
    offlineDevices: 1,
    parkingName: '城西停车场'
  })

  // 模拟通知
  systemStore.addNotification({
    type: 'device',
    title: '设备故障告警',
    content: '出口2道闸设备离线，请及时处理',
    level: 'danger'
  })
  systemStore.addNotification({
    type: 'exception',
    title: '异常通行提醒',
    content: '车牌京A12345欠费20元，请注意',
    level: 'warning'
  })
  systemStore.addNotification({
    type: 'system',
    title: '系统消息',
    content: '今日营收已达到预期目标',
    level: 'success'
  })
}

// 生命周期
onMounted(() => {
  // 检查登录状态
  if (!userStore.isLoggedIn) {
    userStore.login({ name: '值守员张三', role: 'admin' })
  }

  updateTime()
  timeInterval = setInterval(updateTime, 1000)
  initData()
})

onUnmounted(() => {
  if (timeInterval) {
    clearInterval(timeInterval)
  }
})
</script>

<style lang="less" scoped>
.gate-admin-app {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background-color: var(--color-bg);
}

/* 顶部操作栏 */
.top-bar {
  height: var(--top-bar-height);
  background-color: var(--color-bg-card);
  border-bottom: 1px solid var(--color-border);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--spacing-lg);
  z-index: var(--z-index-header);
  box-shadow: var(--shadow-sm);
}

.top-bar-left {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);

  .system-logo {
    width: 36px;
    height: 36px;
    background: linear-gradient(135deg, var(--color-primary), var(--color-primary-light));
    border-radius: var(--border-radius-base);
    display: flex;
    align-items: center;
    justify-content: center;
    color: #fff;
  }

  .system-info {
    display: flex;
    flex-direction: column;

    .system-name {
      font-size: var(--font-size-md);
      font-weight: var(--font-weight-bold);
      color: var(--color-title);
      line-height: 1.2;
    }

    .parking-name {
      font-size: var(--font-size-xs);
      color: var(--color-text-secondary);
      line-height: 1.2;
    }
  }
}

.top-bar-center {
  display: flex;
  align-items: center;
  gap: var(--spacing-xxl);

  .search-input {
    width: 300px;
  }

  .time-display {
    display: flex;
    align-items: center;
    gap: var(--spacing-sm);
    font-size: var(--font-size-sm);
    color: var(--color-body);
  }
}

.top-bar-right {
  display: flex;
  align-items: center;
  gap: var(--spacing-xl);

  .notification-btn,
  .help-btn {
    cursor: pointer;
    padding: var(--spacing-sm);
    border-radius: var(--border-radius-base);
    transition: background-color var(--transition-fast);

    &:hover {
      background-color: var(--color-bg);
    }
  }

  .help-btn {
    display: flex;
    align-items: center;
    gap: var(--spacing-sm);
    background-color: var(--color-primary);
    color: #fff;
    font-size: var(--font-size-sm);
    padding: var(--spacing-sm) var(--spacing-md);

    &:hover {
      background-color: var(--color-primary-dark);
    }
  }

  .user-info {
    display: flex;
    flex-direction: column;
    align-items: flex-end;

    .user-name {
      font-size: var(--font-size-sm);
      font-weight: var(--font-weight-medium);
      color: var(--color-title);
      line-height: 1.2;
    }

    .user-role {
      font-size: var(--font-size-xs);
      color: var(--color-text-secondary);
      line-height: 1.2;
    }
  }

  .action-btns {
    display: flex;
    align-items: center;
    gap: var(--spacing-md);

    .action-icon {
      cursor: pointer;
      color: var(--color-text-secondary);
      transition: color var(--transition-fast);

      &:hover {
        color: var(--color-primary);
      }
    }
  }
}

/* 主容器 */
.main-container {
  flex: 1;
  display: flex;
  overflow: hidden;
}

/* 侧边栏 */
.side-bar {
  width: var(--side-bar-width);
  background-color: var(--color-bg-card);
  border-right: 1px solid var(--color-border);
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  transition: width var(--transition-normal);
  overflow: hidden;

  &.collapsed {
    width: 56px;

    .nav-text {
      display: none;
    }
  }
}

.nav-menu {
  flex: 1;
  padding: var(--spacing-md) 0;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: var(--spacing-md);
  padding: var(--spacing-md) var(--spacing-lg);
  margin: var(--spacing-xs) var(--spacing-sm);
  border-radius: var(--border-radius-base);
  cursor: pointer;
  color: var(--color-body);
  transition: all var(--transition-fast);

  &:hover {
    background-color: var(--color-bg);
    color: var(--color-primary);
  }

  &.active {
    background-color: rgba(22, 93, 255, 0.1);
    color: var(--color-primary);
    font-weight: var(--font-weight-medium);
  }

  .nav-text {
    font-size: var(--font-size-sm);
    white-space: nowrap;
  }
}

.nav-footer {
  padding: var(--spacing-md);
  text-align: center;
  cursor: pointer;
  color: var(--color-text-secondary);
  border-top: 1px solid var(--color-border);
  transition: color var(--transition-fast);

  &:hover {
    color: var(--color-primary);
  }
}

/* 内容区 */
.content-area {
  flex: 1;
  overflow: auto;
  background-color: var(--color-bg);
  padding: var(--spacing-lg);

  &.side-collapsed {
    margin-left: 0;
  }
}
</style>