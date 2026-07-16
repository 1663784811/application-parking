<template>
  <aside class="layout-sider" :style="{ width: siderWidth }">
    <!-- Logo 区域 -->
    <div class="sider-logo">
      <div class="logo-icon">
        <svg class="logo-svg" viewBox="0 0 40 40" fill="none" xmlns="http://www.w3.org/2000/svg">
          <rect width="40" height="40" rx="8" fill="#165DFF"/>
          <path d="M10 28V16C10 13.7909 11.7909 12 14 12H26C28.2091 12 30 13.7909 30 16V28" stroke="white" stroke-width="2" stroke-linecap="round"/>
          <path d="M10 20H30" stroke="white" stroke-width="2"/>
          <circle cx="15" cy="23" r="2" fill="white"/>
          <circle cx="25" cy="23" r="2" fill="white"/>
        </svg>
      </div>
      <span v-if="!isCollapsed" class="logo-text">智慧停车场</span>
    </div>

    <!-- 导航菜单 -->
    <div class="sider-menu-wrapper">
      <Menu
        :active-name="activeMenu"
        :theme="'light'"
        :width="'100%'"
        :open-names="openMenus"
        accordion
        @on-select="handleMenuSelect"
      >
        <Submenu v-for="item in menuList" :key="item.name" :name="item.name">
          <template #title>
            <Icon :type="item.icon" class="menu-icon" />
            <span class="menu-title">{{ item.title }}</span>
          </template>
          <MenuItem
            v-for="child in item.children"
            :key="child.name"
            :name="child.name"
          >
            {{ child.title }}
          </MenuItem>
        </Submenu>
      </Menu>
    </div>

    <!-- 折叠按钮 -->
    <div class="sider-collapse" @click="handleToggleCollapsed">
      <Icon :type="isCollapsed ? 'ios-menu' : 'ios-arrow-back'" />
      <span v-if="!isCollapsed" class="collapse-text">收起菜单</span>
    </div>
  </aside>
</template>

<script setup>
import { computed, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { Menu, Submenu, MenuItem, Icon } from 'view-ui-plus'
import { useCommonStore } from '@/stores/common'

const route = useRoute()
const router = useRouter()
const commonStore = useCommonStore()

const state = reactive({
  isCollapsed: commonStore.state.siderCollapsed
})

// 菜单列表
const menuList = [
  {
    name: 'dashboard',
    icon: 'ios-speedometer',
    title: '工作台',
    children: [
      {
        name: 'home',
        title: '首页仪表盘',
        path: '/home'
      }
    ]
  },
  {
    name: 'parking',
    icon: 'ios-car',
    title: '车场管理',
    children: [
      {
        name: 'parkingList',
        title: '车场列表',
        path: '/parking/parkingList'
      },
      {
        name: 'spaceManagement',
        title: '车位管理',
        path: '/parking/spaceManagement'
      }
    ]
  },
  {
    name: 'passag',
    icon: 'ios-camera',
    title: '车辆通行',
    children: [
      {
        name: 'realTimeMonitor',
        title: '实时监控',
        path: '/passag/realTimeMonitor'
      },
      {
        name: 'passageRecord',
        title: '通行记录查询',
        path: '/passag/passageRecord'
      },
      {
        name: 'exceptionRecord',
        title: '异常通行记录',
        path: '/passag/exceptionRecord'
      }
    ]
  },
  {
    name: 'charge',
    icon: 'ios-card',
    title: '收费管理',
    children: [
      {
        name: 'chargeFlow',
        title: '收费流水',
        path: '/charge/chargeFlow'
      },
      {
        name: 'orderReconcile',
        title: '订单对账',
        path: '/charge/orderReconcile'
      },
      {
        name: 'couponConfig',
        title: '优惠配置',
        path: '/charge/couponConfig'
      },
      {
        name: 'invoiceManage',
        title: '发票管理',
        path: '/charge/invoiceManage'
      }
    ]
  },
  {
    name: 'member',
    icon: 'ios-person',
    title: '会员管理',
    children: [
      {
        name: 'memberList',
        title: '固定车主列表',
        path: '/member/memberList'
      },
      {
        name: 'packageConfig',
        title: '套餐配置',
        path: '/member/packageConfig'
      },
      {
        name: 'renewalRecord',
        title: '续费记录',
        path: '/member/renewalRecord'
      }
    ]
  },
  {
    name: 'device',
    icon: 'ios-construct',
    title: '设备管理',
    children: [
      {
        name: 'deviceList',
        title: '设备列表',
        path: '/device/deviceList'
      },
      {
        name: 'faultRepair',
        title: '故障报修',
        path: '/device/faultRepair'
      }
    ]
  },
  {
    name: 'report',
    icon: 'ios-stats-chart',
    title: '数据报表',
    children: [
      {
        name: 'revenueReport',
        title: '营收统计',
        path: '/report/revenueReport'
      },
      {
        name: 'trafficReport',
        title: '车流量报表',
        path: '/report/trafficReport'
      },
      {
        name: 'spaceUsageReport',
        title: '车位利用率',
        path: '/report/spaceUsageReport'
      },
      {
        name: 'exportReport',
        title: '导出报表',
        path: '/report/exportReport'
      }
    ]
  },
  {
        name: 'system',
    icon: 'ios-settings',
    title: '系统设置',
    children: [
      {
        name: 'adminAccount',
        title: '管理员账号',
        path: '/system/adminAccount'
      },
      {
        name: 'rolePermission',
        title: '角色权限',
        path: '/system/rolePermission'
      },
      {
        name: 'chargeRuleConfig',
        title: '收费规则配置',
        path: '/system/chargeRuleConfig'
      },
      {
        name: 'smsConfig',
        title: '短信配置',
        path: '/system/smsConfig'
      },
      {
        name: 'logManage',
        title: '日志管理',
        path: '/system/logManage'
      },
      {
        name: 'personal',
        title: '个人中心',
        path: '/personal/personal'
      }
    ]
  }
]

// 是否折叠
const isCollapsed = computed(() => state.isCollapsed)

// 侧边栏宽度
const siderWidth = computed(() => {
  return state.isCollapsed ? 'var(--sider-collapsed-width)' : 'var(--sider-width)'
})

// 当前激活菜单
const activeMenu = computed(() => {
  return route.name
})

// 展开菜单
const openMenus = computed(() => {
  const matched = route.matched
  if (matched.length > 1) {
    return [matched[matched.length - 2].name]
  }
  // 根据当前路由找到父级菜单
  const currentMenu = menuList.find(item =>
    item.children.some(child => child.name === route.name)
  )
  return currentMenu ? [currentMenu.name] : []
})

// 切换折叠
const handleToggleCollapsed = () => {
  state.isCollapsed = !state.isCollapsed
  commonStore.setSiderCollapsed(state.isCollapsed)
}

// 菜单选择
const handleMenuSelect = (name) => {
  router.push({ name })
}
</script>

<style lang="less" scoped>
.layout-sider {
  position: relative;
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: var(--bg-color);
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.05);
  transition: width 0.3s ease;
  overflow: hidden;

  .sider-logo {
    display: flex;
    align-items: center;
    height: var(--header-height);
    padding: 0 var(--spacing-lg);
    border-bottom: 1px solid var(--border-color);

    .logo-icon {
      flex-shrink: 0;

      .logo-svg {
        width: 36px;
        height: 36px;
      }
    }

    .logo-text {
      margin-left: var(--spacing-md);
      font-size: var(--font-size-lg);
      font-weight: 600;
      color: var(--text-color-title);
      white-space: nowrap;
    }
  }

  .sider-menu-wrapper {
    flex: 1;
    overflow-y: auto;
    overflow-x: hidden;

    .menu-icon {
      margin-right: var(--spacing-sm);
    }

    .menu-title {
      display: inline-block;
    }
  }

  .sider-collapse {
    display: flex;
    align-items: center;
    justify-content: center;
    height: 48px;
    cursor: pointer;
    border-top: 1px solid var(--border-color);
    transition: background-color 0.3s;
    color: var(--text-color-secondary);

    &:hover {
      background-color: var(--bg-color-hover);
      color: var(--primary-color);
    }

    .ivu-icon {
      font-size: 18px;
    }

    .collapse-text {
      margin-left: var(--spacing-sm);
      font-size: var(--font-size-sm);
    }
  }
}
</style>

<style lang="less">
// 全局菜单样式覆盖
.ivu-menu {
  &.ivu-menu-light {
    background-color: transparent;
  }

  .ivu-menu-submenu {
    .ivu-menu-item {
      padding-left: 52px !important;
      font-size: var(--font-size-sm);
    }
  }
}
</style>