/**
 * 用户相关接口
 */

import service from './axiosRequest'

// 登录：POST /api/login  { username, password } -> { token, userInfo }
export function login(data) {
  return service.post('/login', data)
}

// 获取当前登录用户信息：GET /api/user/info
export function getUserInfo() {
  return service.get('/user/info')
}

// 退出登录：POST /api/logout
export function logout() {
  return service.post('/logout')
}
