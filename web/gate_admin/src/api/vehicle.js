/**
 * 车辆通行记录相关接口
 */

import service from './axiosRequest'

// 获取车辆通行记录列表：GET /api/vehicle/records
export function getVehicleRecords() {
  return service.get('/vehicle/records')
}
