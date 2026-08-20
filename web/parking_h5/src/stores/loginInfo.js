import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { refreshTokenRequest } from '@/api/app'

/**
 * 登录态 store
 * 管理 token、refreshToken、用户信息，并提供短 token 刷新逻辑
 * 供 axiosRequest 拦截器调用：const userStore = loginInfo()
 */
export const useLoginInfoStore = defineStore('loginInfo', () => {
  // 双 token
  const token = ref('')
  const refreshToken = ref('')

  // 用户信息（服务器请求过来的原始数据）
  const userInfo = ref(null)

  // 是否已登录
  const isLogin = computed(() => !!token.value)

  /**
   * 设置 token
   */
  const setToken = (jwtToken, refToken) => {
    token.value = jwtToken || ''
    refreshToken.value = refToken || ''
  }

  /**
   * 设置用户信息
   */
  const setUserInfo = (info) => {
    userInfo.value = info
  }

  /**
   * 刷新短 token（拦截器在 6010/6001 时调用）
   * 返回刷新后的 token，失败返回空串
   */
  const refreshShortToken = async () => {
    if (!refreshToken.value) {
      return ''
    }
    const res = await refreshTokenRequest({ refreshToken: refreshToken.value })
    if (res && res.data) {
      token.value = res.data.jwtToken || ''
      refreshToken.value = res.data.refreshToken || ''
      return token.value
    }
    return ''
  }

  /**
   * 退出登录，清空登录态
   */
  const logout = () => {
    token.value = ''
    refreshToken.value = ''
    userInfo.value = null
  }

  return {
    token,
    refreshToken,
    userInfo,
    isLogin,
    setToken,
    setUserInfo,
    refreshShortToken,
    logout,
  }
})

// 兼容参考代码的调用方式：const userStore = loginInfo()
export const loginInfo = () => useLoginInfoStore()
