/**
 * Axios 请求封装
 */

import axios from 'axios'
import { Message } from 'view-ui-plus'

// 创建 axios 实例
const service = axios.create({
  baseURL: '/api',
  timeout: 30000
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 添加 token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    if (res.code !== 2000) {
      Message.error({
        content: res.msg || '请求失败',
        duration: 3
      })
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
    return res
  },
  error => {
    if (error.response) {
      switch (error.response.status) {
        case 401:
          Message.error({
            content: '登录已过期，请重新登录',
            duration: 3
          })
          // 跳转登录
          setTimeout(() => {
            window.location.href = '/login'
          }, 1500)
          break
        case 403:
          Message.error({
            content: '没有权限',
            duration: 3
          })
          break
        case 500:
          Message.error({
            content: '服务器错误',
            duration: 3
          })
          break
        default:
          Message.error({
            content: error.message || '网络错误',
            duration: 3
          })
      }
    }
    return Promise.reject(error)
  }
)

export default service