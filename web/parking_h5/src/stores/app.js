import { defineStore } from 'pinia'
import { findApp } from '@/api/app'

/**
 * 应用 store
 * 存放应用级原始数据，例如服务器请求过来的数据、当前 appId、app 信息等
 */
export const useAppStore = defineStore('app', {
  state: () => ({
    // 当前应用 id（路由参数 appId）
    appId: null,
    // 应用信息（服务器请求过来的原始数据）
    appInfo: null,
    // 加载状态
    loading: false,
    // 查询是否失败（用于欢迎页失败重试）
    error: false,
  }),

  getters: {
    // 是否已设置 appId
    hasApp: (state) => !!state.appId,
    // 应用名称
    appName: (state) => state.appInfo?.name || '',
    // 应用 logo
    appLogo: (state) => state.appInfo?.logo || '',
  },

  actions: {
    // 设置 appId
    setAppId(appId) {
      this.appId = appId
    },

    // 设置应用信息
    setAppInfo(appInfo) {
      this.appInfo = appInfo
    },

    // 设置加载状态
    setLoading(loading) {
      this.loading = loading
    },

    // 按 appType 查询 app 信息（/app/login/findApp）
    // 成功后写入 appInfo 与 appId；appId 用字符串保存（后端 id 以字符串序列化，避免雪花 ID 精度丢失）
    async fetchApp(appType) {
      // 已加载过且有效则复用，避免重复请求
      if (this.appInfo && this.appId) {
        return this.appInfo
      }
      this.loading = true
      this.error = false
      try {
        const res = await findApp({ appType })
        const info = res?.data || null
        if (info && info.id) {
          this.appInfo = info
          this.appId = String(info.id)
        } else {
          this.appInfo = null
          this.appId = null
          this.error = true
        }
      } catch (e) {
        this.appInfo = null
        this.appId = null
        this.error = true
      } finally {
        this.loading = false
      }
      return this.appInfo
    },

    // 清空
    clearApp() {
      this.appId = null
      this.appInfo = null
      this.loading = false
      this.error = false
    },
  },
})
