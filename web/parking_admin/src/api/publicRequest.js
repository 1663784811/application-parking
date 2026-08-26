/**
 * 公开接口（免登录）axios 封装
 *
 * 与 axiosRequest.js 区分：后端公开模块（/admin/login、/common/verify 等）
 * 采用 BaseResult 响应约定 —— 成功码 code === 2000，提示字段为 msg，
 * 业务数据在 data，分页信息在 result。专用于免登录场景，不带 Authorization。
 */
import axios from 'axios'
import { Message } from 'view-ui-plus'
import app from './app'

// 创建 axios 实例
const service = axios.create({
  baseURL: app.baseUrl,
  timeout: 30000
})

// 响应拦截器：按 BaseResult 约定解析（code === 2000 视为成功）
service.interceptors.response.use(
  (response) => {
    const res = response.data

    if (res.code === 2000) {
      return res
    } else {
      Message.error({
        content: res.msg || '请求失败',
        duration: 3
      })
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
  },
  (error) => {
    let message = '网络错误，请稍后重试'

    if (error.response) {
      switch (error.response.status) {
        case 401:
          message = '登录已过期，请重新登录'
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

    Message.error({
      content: message,
      duration: 3
    })

    return Promise.reject(error)
  }
)

export default service
