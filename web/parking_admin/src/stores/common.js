/**
 * 通用 Store - 用于全局状态管理
 */
import { defineStore } from 'pinia'
import { reactive } from 'vue'

export const useCommonStore = defineStore('common', () => {
  const state = reactive({
    // 侧边栏折叠状态
    siderCollapsed: false,
    // 当前选中的停车场（单选用，兼容旧代码）
    currentParking: null,
    // 多选选中的停车场 ID 列表
    selectedParkingIds: [],
    // 停车场列表
    parkingList: [],
    // 全局加载状态
    globalLoading: false,
    // 消息通知数
    notificationCount: 0,
    // 待处理异常数
    exceptionCount: 0
  })

  // 切换侧边栏
  const toggleSider = () => {
    state.siderCollapsed = !state.siderCollapsed
  }

  // 设置侧边栏状态
  const setSiderCollapsed = (collapsed) => {
    state.siderCollapsed = collapsed
  }

  // 设置当前停车场
  const setCurrentParking = (parking) => {
    state.currentParking = parking
  }

  // 设置多选停车场
  const setSelectedParkingIds = (ids) => {
    state.selectedParkingIds = ids
  }

  // 切换单个停车场选中状态
  const toggleParkingSelection = (id) => {
    const idx = state.selectedParkingIds.indexOf(id)
    if (idx === -1) {
      state.selectedParkingIds.push(id)
    } else {
      state.selectedParkingIds.splice(idx, 1)
    }
  }

  // 清空停车场选择
  const clearParkingSelection = () => {
    state.selectedParkingIds = []
  }

  // 设置停车场列表
  const setParkingList = (list) => {
    state.parkingList = list
  }

  // 设置全局加载状态
  const setGlobalLoading = (loading) => {
    state.globalLoading = loading
  }

  // 设置消息通知数
  const setNotificationCount = (count) => {
    state.notificationCount = count
  }

  // 设置待处理异常数
  const setExceptionCount = (count) => {
    state.exceptionCount = count
  }

  return {
    state,
    toggleSider,
    setSiderCollapsed,
    setCurrentParking,
    setSelectedParkingIds,
    toggleParkingSelection,
    clearParkingSelection,
    setParkingList,
    setGlobalLoading,
    setNotificationCount,
    setExceptionCount
  }
})