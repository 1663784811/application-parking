/**
 * 路由配置
 * 聚焦门口端核心功能，简化菜单层级，所有功能一键直达
 */

import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    redirect: '/home'
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/home',
    name: 'Home',
    component: () => import('@/views/home/index.vue'),
    meta: { title: '首页', icon: 'ios-home-outline' }
  },
  {
    path: '/monitor',
    name: 'Monitor',
    component: () => import('@/views/monitor/index.vue'),
    meta: { title: '实时通行监控', icon: 'ios-videocam-outline' }
  },
  {
    path: '/release',
    name: 'Release',
    component: () => import('@/views/release/index.vue'),
    meta: { title: '车辆放行管理', icon: 'ios-car-outline' }
  },
  {
    path: '/exception',
    name: 'Exception',
    component: () => import('@/views/exception/index.vue'),
    meta: { title: '异常车辆处理', icon: 'ios-alert-outline' }
  },
  {
    path: '/equipment',
    name: 'Equipment',
    component: () => import('@/views/equipment/index.vue'),
    meta: { title: '设备状态监控', icon: 'ios-construct-outline' }
  },
  {
    path: '/log',
    name: 'Log',
    component: () => import('@/views/log/index.vue'),
    meta: { title: '现场操作日志', icon: 'ios-document-text-outline' }
  },
  {
    path: '/settings',
    name: 'Settings',
    component: () => import('@/views/settings/index.vue'),
    meta: { title: '基础设置', icon: 'ios-settings-outline', requiresAdmin: true }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router