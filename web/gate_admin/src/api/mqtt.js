/**
 * MQTT 相关接口
 */

import service from './axiosRequest'

// 获取 MQTT 连接状态：GET /api/mqtt/status
export function getMqttStatus() {
  return service.get('/mqtt/status')
}
