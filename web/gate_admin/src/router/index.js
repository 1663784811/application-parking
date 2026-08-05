/**
 * 路由配置
 * 聚焦门口端核心功能，简化菜单层级，所有功能一键直达
 */

import { createRouter, createWebHistory } from 'vue-router'

// Layout
const Layout = () => import('@/layout/MainLayout.vue')

// 页面
const Login = () => import('@/views/login/Login.vue')
const Home = () => import('@/views/home/Home.vue')

const routes = [
  {
    path: '/',
    redirect: '/home'
  },
  {
    path: '/login',
    name: 'Login',
    component: Login,
    meta: { title: '登录', requiresAuth: false }
  },
  {
    path: '/',
    component: Layout,
    children: [
      {
        path: '/home',
        name: 'Home',
        component: Home,
        meta: { title: '首页', icon: 'ios-home-outline' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
