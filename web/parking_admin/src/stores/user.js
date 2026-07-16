/**
 * 用户 Store
 */
import { defineStore } from 'pinia'
import { reactive, ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  // 状态
  const state = reactive({
    token: localStorage.getItem('token') || '',
    userInfo: null
  })

  // 加载状态
  const loading = ref(false)

  // 设置 Token
  const setToken = (token) => {
    state.token = token
    localStorage.setItem('token', token)
  }

  // 设置用户信息
  const setUserInfo = (userInfo) => {
    state.userInfo = userInfo
  }

  // 清除登录信息
  const logout = () => {
    state.token = ''
    state.userInfo = null
    localStorage.removeItem('token')
  }

  // 判断是否已登录
  const isLoggedIn = () => {
    return !!state.token
  }

  return {
    state,
    loading,
    setToken,
    setUserInfo,
    logout,
    isLoggedIn
  }
})