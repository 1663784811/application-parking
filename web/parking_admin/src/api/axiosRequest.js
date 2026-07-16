/**
 * axios 请求封装
 */
import axios from 'axios'
import { Message } from 'view-ui-plus'
import app from './app'

// 创建 axios 实例
const service = axios.create({
  baseURL: app.baseUrl,
  timeout: 30000
})

// 请求拦截器
service.interceptors.request.use(
  (config) => {
    // 添加 token
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response) => {
    const res = response.data

    // 根据业务状态码判断
    if (res.code === 200) {
      return res
    } else {
      Message.error({
        content: res.message || '请求失败',
        duration: 3
      })
      return Promise.reject(new Error(res.message || '请求失败'))
    }
  },
  (error) => {
    let message = '网络错误，请稍后重试'

    if (error.response) {
      switch (error.response.status) {
        case 401:
          message = '登录已过期，请重新登录'
          localStorage.removeItem('token')
          // 跳转到登录页
          break
        case 403:
          message = '没有权限访问该资源'
          break
        case 404:
          message = '请求的资源不存在'
          break
        case 500:
          message = '服务器错误'
          break
        default:
          message = error.response.data?.message || message
      }
    } else if (error.request) {
      message = '网络连接失败，请检查网络'
    }

    Message.error({
      content: message,
      duration: 3
    })

    return Promise.reject(error)
  }
)

export default service