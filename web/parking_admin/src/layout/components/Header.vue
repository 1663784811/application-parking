<template>
  <header class="layout-header">
    <div class="header-left">
      <Icon type="ios-menu" class="menu-toggle" @click="handleToggleMenu" />
      <Breadcrumb />
    </div>

    <div class="header-center">
      <div class="global-search">
        <Icon type="ios-search" class="search-icon" />
        <input
          type="text"
          class="search-input"
          placeholder="搜索车牌、订单号、车场名..."
          v-model="state.searchKeyword"
          @keyup.enter="handleSearch"
        />
      </div>
    </div>

    <div class="header-right">
      <!-- 消息通知 -->
      <div class="header-action" @click="handleNotification">
        <Badge :count="notificationCount" :offset="[-2, 2]">
          <Icon type="ios-notifications-outline" class="action-icon" />
        </Badge>
      </div>

      <!-- 切换车场 -->
      <div class="header-action parking-selector">
        <Dropdown @on-click="handleParkingChange">
          <div class="parking-selector-inner">
            <Icon type="ios-car" />
            <span class="parking-name">{{ currentParkingName }}</span>
            <Icon type="ios-arrow-down" />
          </div>
          <template #list>
            <DropdownMenu>
              <DropdownItem
                v-for="item in parkingOptions"
                :key="item.id"
                :name="item.id"
              >
                {{ item.name }}
              </DropdownItem>
            </DropdownMenu>
          </template>
        </Dropdown>
      </div>

      <!-- 用户信息 -->
      <Dropdown @on-click="handleUserMenuClick">
        <div class="user-info">
          <Avatar :size="32" style="background-color: var(--primary-color)">
            {{ userAvatarText }}
          </Avatar>
          <span class="user-name">{{ userName }}</span>
          <Icon type="ios-arrow-down" class="user-arrow" />
        </div>
        <template #list>
          <DropdownMenu>
            <DropdownItem name="profile">个人中心</DropdownItem>
            <DropdownItem name="password">修改密码</DropdownItem>
            <DropdownItem divided name="logout">退出登录</DropdownItem>
          </DropdownMenu>
        </template>
      </Dropdown>
    </div>
  </header>
</template>

<script setup>
import { computed, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { Avatar, Badge, Dropdown, DropdownMenu, DropdownItem, Icon } from 'view-ui-plus'
import Breadcrumb from './Breadcrumb.vue'
import { useUserStore } from '@/stores/user'
import { useCommonStore } from '@/stores/common'

const router = useRouter()
const userStore = useUserStore()
const commonStore = useCommonStore()

const state = reactive({
  searchKeyword: ''
})

// 车场选项（模拟数据，实际应该从接口获取）
const parkingOptions = [
  { id: 1, name: '全部车场' },
  { id: 2, name: '城西停车场' },
  { id: 3, name: '城东停车场' },
  { id: 4, name: '购物中心停车场' }
]

// 当前车场名称
const currentParkingName = computed(() => {
  return commonStore.state.currentParking?.name || '全部车场'
})

// 消息通知数
const notificationCount = computed(() => {
  return commonStore.state.notificationCount
})

// 用户名
const userName = computed(() => {
  return userStore.state.userInfo?.name || '管理员'
})

// 用户头像文字
const userAvatarText = computed(() => {
  const name = userName.value
  return name.length > 0 ? name.substring(0, 1) : '管'
})

// 切换菜单
const handleToggleMenu = () => {
  commonStore.toggleSider()
}

// 全局搜索
const handleSearch = () => {
  if (state.searchKeyword.trim()) {
    // TODO: 调用搜索接口或跳转搜索结果页
    console.log('搜索:', state.searchKeyword)
  }
}

// 消息通知
const handleNotification = () => {
  // TODO: 打开消息通知面板
}

// 切换车场
const handleParkingChange = (id) => {
  const parking = parkingOptions.find(item => item.id === id)
  if (parking) {
    commonStore.setCurrentParking(parking)
  }
}

// 用户菜单点击
const handleUserMenuClick = (name) => {
  switch (name) {
    case 'profile':
      router.push({ name: 'personal' })
      break
    case 'password':
      router.push({ name: 'personal' })
      break
    case 'logout':
      handleLogout()
      break
  }
}

// 退出登录
const handleLogout = () => {
  userStore.logout()
  router.push({ name: 'login' })
}
</script>

<style lang="less" scoped>
.layout-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: var(--header-height);
  padding: 0 var(--spacing-xl);
  background-color: var(--bg-color);
  box-shadow: var(--shadow-light);

  .header-left {
    display: flex;
    align-items: center;

    .menu-toggle {
      font-size: 20px;
      margin-right: var(--spacing-lg);
      cursor: pointer;
      color: var(--text-color-secondary);
      transition: color 0.3s;

      &:hover {
        color: var(--primary-color);
      }
    }
  }

  .header-center {
    flex: 1;
    display: flex;
    justify-content: center;
    padding: 0 var(--spacing-xl);

    .global-search {
      display: flex;
      align-items: center;
      width: 400px;
      height: 36px;
      padding: 0 var(--spacing-md);
      background-color: var(--bg-color-page);
      border: 1px solid var(--border-color);
      border-radius: var(--border-radius-base);
      transition: border-color 0.3s, box-shadow 0.3s;

      &:focus-within {
        border-color: var(--primary-color);
        box-shadow: 0 0 0 2px rgba(22, 93, 255, 0.1);
      }

      .search-icon {
        color: var(--text-color-secondary);
        font-size: 16px;
      }

      .search-input {
        flex: 1;
        margin-left: var(--spacing-sm);
        border: none;
        outline: none;
        background: transparent;
        font-size: var(--font-size-sm);
        color: var(--text-color);

        &::placeholder {
          color: var(--text-color-secondary);
        }
      }
    }
  }

  .header-right {
    display: flex;
    align-items: center;

    .header-action {
      display: flex;
      align-items: center;
      justify-content: center;
      width: 40px;
      height: 40px;
      margin-right: var(--spacing-lg);
      cursor: pointer;
      border-radius: var(--border-radius-base);
      transition: background-color 0.3s;

      &:hover {
        background-color: var(--bg-color-hover);
      }

      .action-icon {
        font-size: 20px;
        color: var(--text-color);
      }
    }

    .parking-selector {
      width: auto;
      margin-right: var(--spacing-xl);

      .parking-selector-inner {
        display: flex;
        align-items: center;
        padding: 6px 12px;
        background-color: var(--bg-color-page);
        border-radius: var(--border-radius-base);
        transition: background-color 0.3s;

        &:hover {
          background-color: var(--border-color);
        }

        .parking-name {
          margin: 0 var(--spacing-sm);
          font-size: var(--font-size-sm);
          color: var(--text-color);
        }
      }
    }

    .user-info {
      display: flex;
      align-items: center;
      cursor: pointer;
      padding: 0 var(--spacing-sm);
      border-radius: var(--border-radius-base);
      transition: background-color 0.3s;

      &:hover {
        background-color: var(--bg-color-hover);
      }

      .user-name {
        margin-left: var(--spacing-sm);
        margin-right: var(--spacing-xs);
        font-size: var(--font-size-sm);
        color: var(--text-color);
      }

      .user-arrow {
        color: var(--text-color-secondary);
        font-size: 12px;
      }
    }
  }
}
</style>