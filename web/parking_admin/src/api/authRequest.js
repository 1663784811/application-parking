/**
 * 鉴权接口（需登录）axios 封装
 *
 * 与 axiosRequest.js 区别：后端鉴权模块同样采用 BaseResult 响应约定
 * —— 成功码 code === 2000，提示字段为 msg，业务数据在 data，分页信息在 result。
 * 而 axiosRequest.js 拦截器判定 code === 200 / message，与真实后端不兼容且该文件禁止修改，
 * 故鉴权接口统一走本实例：请求拦截器注入 Authorization（Bearer），响应拦截器按 BaseResult 处理。
 */
import axios from 'axios'
import { Message } from 'view-ui-plus'
import app from './app'

const service = axios.create({
  baseURL: app.baseUrl,
  timeout: 30000
})

// 请求拦截器：注入登录令牌
service.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers['Authorization'] = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

// 响应拦截器：按 BaseResult 约定处理
service.interceptors.response.use(
  (response) => {
    const res = response.data
    if (res.code === 2000) {
      return res
    } else {
      Message.error({ content: res.msg || '请求失败', duration: 3 })
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
  },
  (error) => {
    let message = '网络错误，请稍后重试'
    if (error.response) {
      switch (error.response.status) {
        case 401:
          message = '登录已过期，请重新登录'
          localStorage.removeItem('token')
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
          message = error.response.data?.msg || message
      }
    } else if (error.request) {
      message = '网络连接失败，请检查网络'
    }
    Message.error({ content: message, duration: 3 })
    return Promise.reject(error)
  }
)

export default service
