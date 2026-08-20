import { defineStore } from 'pinia'

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
  }),

  getters: {
    // 是否已设置 appId
    hasApp: (state) => !!state.appId,
    // 应用名称
    appName: (state) => state.appInfo?.name || '',
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

    // 清空
    clearApp() {
      this.appId = null
      this.appInfo = null
      this.loading = false
    },
  },
})
