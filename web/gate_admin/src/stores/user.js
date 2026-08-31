/**
 * 用户状态管理
 */

import { defineStore } from 'pinia'
import { login as loginApi, getUserInfo, logout as logoutApi } from '@/api/user'

export const useUserStore = defineStore('user', {
  state: () => ({
    // 用户信息
    userInfo: {
      id: null,
      name: '',
      role: 'guard',
      phone: '',
      parkingName: '',
      parkingId: null
    },
    // 登录状态
    isLoggedIn: false,
    // 权限信息
    permissions: []
  }),

  getters: {
    // 是否为管理员
    isAdmin: (state) => state.userInfo.role === 'admin',

    // 是否有权限
    hasPermission: (state) => (permission) => {
      if (state.userInfo.role === 'admin') return true
      return state.permissions.includes(permission)
    }
  },

  actions: {
    // 设置用户信息
    setUserInfo(userInfo) {
      this.userInfo = { ...this.userInfo, ...userInfo }
      this.isLoggedIn = true
    },

    // 登录：调用后端接口，保存 token 与用户信息
    async login(payload) {
      const res = await loginApi(payload)
      const { token, userInfo } = res.data
      localStorage.setItem('token', token)
      this.setUserInfo(userInfo)
      return res
    },

    // 拉取当前登录用户信息（页面刷新后恢复会话用）
    async fetchUserInfo() {
      const res = await getUserInfo()
      this.setUserInfo(res.data)
      return res.data
    },

    // 退出登录：调用登出接口并清理本地状态
    async logout() {
      try {
        await logoutApi()
      } catch (e) {
        // 忽略登出接口错误，确保本地一定清理
      }
      localStorage.removeItem('token')
      this.userInfo = {
        id: null,
        name: '',
        role: 'guard',
        phone: '',
        parkingName: '',
        parkingId: null
      }
      this.isLoggedIn = false
      this.permissions = []
    }
  }
})
