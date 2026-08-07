/**
 * 用户状态管理
 */

import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  state: () => ({
    // 用户信息
    userInfo: {
      id: null,
      name: '',
      role: '',
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

    // 退出登录
    logout() {
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
    },

    // 模拟登录
    login(userData) {
      this.setUserInfo({
        id: 1,
        name: userData.name || '值守员张三',
        role: userData.role || 'guard',
        phone: '13800138000',
        parkingName: '城西停车场',
        parkingId: 1
      })
    }
  }
})