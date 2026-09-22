import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
// api/app.js 是禁改文件，里面的导出名就叫 userInfo，与下面 store 里的字段重名，import 时起别名
import { refreshTokenRequest, userInfo as findUserInfoRequest } from '@/api/app'

// ===== 持久化键 =====
// 登录态原本只在内存里，刷新即掉线（webview 重载、切后台被系统回收都算整页加载），
// 所以落到 localStorage。前缀跟 Login.vue 的 parking_remember_phone 保持一致。
const TOKEN_KEY = 'parking_token'
const REFRESH_TOKEN_KEY = 'parking_refresh_token'
const USER_INFO_KEY = 'parking_user_info'

// 隐私模式 / 禁用存储时 localStorage 会直接抛异常，读写都得兜住：
// 存不下最坏是刷新掉线，不能让登录本身失败
function readItem(key) {
  try {
    return localStorage.getItem(key) || ''
  } catch (e) {
    return ''
  }
}

function writeItem(key, value) {
  try {
    if (value) {
      localStorage.setItem(key, value)
    } else {
      localStorage.removeItem(key)
    }
  } catch (e) {
    // 存不下就算了，本次会话内登录态仍然有效
  }
}

// userInfo 是对象，单独走 JSON；内容坏了（手改过、后端结构变了）就当没有
function readUserInfo() {
  const raw = readItem(USER_INFO_KEY)
  if (!raw) {
    return null
  }
  try {
    return JSON.parse(raw)
  } catch (e) {
    return null
  }
}

/**
 * 登录态 store
 * 管理 token、refreshToken、用户信息，并提供短 token 刷新逻辑
 * 供 axiosRequest 拦截器调用：const userStore = loginInfo()
 *
 * 三份数据都持久化在 localStorage，初始化时读回 —— 刷新页面不该等于掉线
 */
export const useLoginInfoStore = defineStore('loginInfo', () => {
  // 双 token（初值从本地存储恢复）
  const token = ref(readItem(TOKEN_KEY))
  const refreshToken = ref(readItem(REFRESH_TOKEN_KEY))

  // 用户信息（服务器请求过来的原始数据）
  const userInfo = ref(readUserInfo())

  // 是否已登录
  const isLogin = computed(() => !!token.value)

  /**
   * 设置 token（同时落盘）
   */
  const setToken = (jwtToken, refToken) => {
    token.value = jwtToken || ''
    refreshToken.value = refToken || ''
    writeItem(TOKEN_KEY, token.value)
    writeItem(REFRESH_TOKEN_KEY, refreshToken.value)
  }

  /**
   * 设置用户信息（同时落盘）
   */
  const setUserInfo = (info) => {
    userInfo.value = info || null
    writeItem(USER_INFO_KEY, userInfo.value ? JSON.stringify(userInfo.value) : '')
  }

  /**
   * 拉取用户信息写进 store。
   * 失败不抛、也不清登录态 —— 调用方（登录成功后、授权回跳后）不该因为拿不到昵称就中断，
   * 最坏是"我的"页面暂时显示兜底昵称，下次进页面再拉
   */
  const fetchUserInfo = async () => {
    if (!token.value) {
      return null
    }
    try {
      const res = await findUserInfoRequest()
      if (res && res.data) {
        setUserInfo(res.data)
      }
    } catch (e) {
      console.warn('获取用户信息失败', e)
    }
    return userInfo.value
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
      // 这里绕过了 setToken，落盘得自己补 —— 否则刷来的新 refreshToken 一刷新又没了
      writeItem(TOKEN_KEY, token.value)
      writeItem(REFRESH_TOKEN_KEY, refreshToken.value)
      return token.value
    }
    return ''
  }

  /**
   * 退出登录，清空登录态（含本地存储）
   */
  const logout = () => {
    token.value = ''
    refreshToken.value = ''
    userInfo.value = null
    writeItem(TOKEN_KEY, '')
    writeItem(REFRESH_TOKEN_KEY, '')
    writeItem(USER_INFO_KEY, '')
  }

  return {
    token,
    refreshToken,
    userInfo,
    isLogin,
    setToken,
    setUserInfo,
    fetchUserInfo,
    refreshShortToken,
    logout,
  }
})

// 兼容参考代码的调用方式：const userStore = loginInfo()
export const loginInfo = () => useLoginInfoStore()
