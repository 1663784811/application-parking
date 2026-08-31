/**
 * 路由配置
 * 聚焦门口端核心功能，简化菜单层级，所有功能一键直达
 */

import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

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
        meta: { title: '首页', icon: 'fa-home' }
      }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：未登录跳登录页；已登录但用户信息缺失（页面刷新）则回拉一次
router.beforeEach(async (to, from, next) => {
  const token = localStorage.getItem('token')

  // 已登录访问登录页 → 直接去首页
  if (to.path === '/login') {
    if (token) {
      next('/home')
    } else {
      next()
    }
    return
  }

  // 未登录 → 登录页
  if (!token) {
    next('/login')
    return
  }

  // 有 token 但未加载用户信息（页面刷新）→ 拉取当前用户
  const userStore = useUserStore()
  if (!userStore.isLoggedIn) {
    try {
      await userStore.fetchUserInfo()
    } catch (e) {
      // token 失效，清理后回登录页
      await userStore.logout()
      next('/login')
      return
    }
  }

  next()
})

export default router
