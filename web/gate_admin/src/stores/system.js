/**
 * 系统状态管理
 */

import { defineStore } from 'pinia'

export const useSystemStore = defineStore('system', {
  state: () => ({
    // 实时车辆统计
    stats: {
      currentVehicles: 0,
      totalSpaces: 300,
      todayIn: 0,
      todayOut: 0,
      pendingExceptions: 0,
      onlineDevices: 0,
      offlineDevices: 0
    },

    // 设备列表
    devices: [],

    // 通道列表
    channels: [
      { id: 1, name: '入口1', direction: 'in', status: 'online' },
      { id: 2, name: '入口2', direction: 'in', status: 'online' },
      { id: 3, name: '出口1', direction: 'out', status: 'online' },
      { id: 4, name: '出口2', direction: 'out', status: 'fault' }
    ],

    // 通知消息
    notifications: [],

    // 是否锁定屏幕
    isLocked: false,

    // 网络状态
    networkStatus: 'online',

    // 刷新状态
    isRefreshing: false
  }),

  getters: {
    // 在线设备数
    onlineDeviceCount: (state) => state.devices.filter(d => d.status === 'online').length,

    // 离线设备数
    offlineDeviceCount: (state) => state.devices.filter(d => d.status === 'offline').length,

    // 故障设备数
    faultDeviceCount: (state) => state.devices.filter(d => d.status === 'fault').length,

    // 未读通知数
    unreadNotificationCount: (state) => state.notifications.filter(n => !n.read).length,

    // 车位利用率
    spaceUtilization: (state) => {
      if (state.stats.totalSpaces === 0) return 0
      return Math.round((state.stats.currentVehicles / state.stats.totalSpaces) * 100)
    },

    // 入口通道
    inChannels: (state) => state.channels.filter(c => c.direction === 'in'),

    // 出口通道
    outChannels: (state) => state.channels.filter(c => c.direction === 'out')
  },

  actions: {
    // 更新统计数据
    updateStats(stats) {
      this.stats = { ...this.stats, ...stats }
    },

    // 添加通知
    addNotification(notification) {
      this.notifications.unshift({
        id: Date.now(),
        ...notification,
        read: false,
        time: new Date().toLocaleString('zh-CN')
      })
    },

    // 标记通知已读
    markAsRead(id) {
      const notification = this.notifications.find(n => n.id === id)
      if (notification) {
        notification.read = true
      }
    },

    // 标记全部已读
    markAllAsRead() {
      this.notifications.forEach(n => n.read = true)
    },

    // 锁定屏幕
    lockScreen() {
      this.isLocked = true
    },

    // 解锁屏幕
    unlockScreen(password) {
      // 简化验证，实际应该调用后端
      if (password === '123456' || password === '') {
        this.isLocked = false
        return true
      }
      return false
    },

    // 刷新数据
    async refreshData() {
      this.isRefreshing = true
      // 模拟加载
      await new Promise(resolve => setTimeout(resolve, 500))
      this.isRefreshing = false
    }
  }
})