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
  // 统一存裸 JWT：后端 login/refreshToken 返回的 jwtToken 可能自带 "Bearer " 前缀，
  // 而各请求拦截器（axiosRequest / authRequest）会再拼一次 "Bearer "，
  // 故在此剥掉前缀，保证 localStorage.token 始终为裸 JWT，避免出现 "Bearer Bearer xxx" 双前缀。
  const setToken = (token) => {
    const raw = (token || '').replace(/^Bearer\s+/i, '')
    state.token = raw
    localStorage.setItem('token', raw)
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