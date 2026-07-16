/**
 * API 统一导出模块
 */
import request from './axiosRequest'

// ========== 车场管理 ==========
export const parkingApi = {
  // 车场列表
  getParkingList: (params) => request.get('/parking/list', { params }),
  // 新增车场
  addParking: (data) => request.post('/parking/add', data),
  // 编辑车场
  editParking: (data) => request.put('/parking/edit', data),
  // 删除车场
  deleteParking: (id) => request.delete(`/parking/delete/${id}`),
  // 车场详情
  getParkingDetail: (id) => request.get(`/parking/detail/${id}`)
}

// ========== 车位管理 ==========
export const spaceApi = {
  // 车位列表
  getSpaceList: (params) => request.get('/space/list', { params }),
  // 车位详情
  getSpaceDetail: (id) => request.get(`/space/detail/${id}`),
  // 分配车位
  assignSpace: (data) => request.post('/space/assign', data),
  // 解绑车位
  unbindSpace: (id) => request.post(`/space/unbind/${id}`),
  // 报修车位
  reportRepair: (data) => request.post('/space/repair', data),
  // 获取车位状态统计
  getSpaceStats: (parkingId) => request.get(`/space/stats/${parkingId}`)
}

// ========== 通行记录 ==========
export const passageApi = {
  // 实时通行列表
  getRealTimeList: (params) => request.get('/passage/realtime', { params }),
  // 通行记录列表
  getRecordList: (params) => request.get('/passage/record/list', { params }),
  // 通行记录详情
  getRecordDetail: (id) => request.get(`/passage/record/detail/${id}`),
  // 异常记录列表
  getExceptionList: (params) => request.get('/passage/exception/list', { params }),
  // 远程开闸
  openGate: (data) => request.post('/passage/openGate', data),
  // 加入黑名单
  addBlacklist: (data) => request.post('/passage/blacklist/add', data),
  // 移出黑名单
  removeBlacklist: (id) => request.post(`/passage/blacklist/remove/${id}`),
  // 补缴费用
  payOverage: (data) => request.post('/passage/payOverage', data)
}

// ========== 收费管理 ==========
export const chargeApi = {
  // 收费流水列表
  getOrderList: (params) => request.get('/charge/order/list', { params }),
  // 订单详情
  getOrderDetail: (id) => request.get(`/charge/order/detail/${id}`),
  // 退款
  refund: (data) => request.post('/charge/refund', data),
  // 对账记录
  getReconcileList: (params) => request.get('/charge/reconcile/list', { params }),
  // 对账汇总
  getReconcileSummary: (params) => request.get('/charge/reconcile/summary', { params }),
  // 优惠券列表
  getCouponList: (params) => request.get('/charge/coupon/list', { params }),
  // 新增优惠券
  addCoupon: (data) => request.post('/charge/coupon/add', data),
  // 编辑优惠券
  editCoupon: (data) => request.put('/charge/coupon/edit', data),
  // 删除优惠券
  deleteCoupon: (id) => request.delete(`/charge/coupon/delete/${id}`),
  // 发票列表
  getInvoiceList: (params) => request.get('/charge/invoice/list', { params }),
  // 开票
  createInvoice: (data) => request.post('/charge/invoice/create', data),
  // 冲红发票
  redInvoice: (id) => request.post(`/charge/invoice/red/${id}`)
}

// ========== 会员管理 ==========
export const memberApi = {
  // 会员列表
  getMemberList: (params) => request.get('/member/list', { params }),
  // 会员详情
  getMemberDetail: (id) => request.get(`/member/detail/${id}`),
  // 新增会员
  addMember: (data) => request.post('/member/add', data),
  // 编辑会员
  editMember: (data) => request.put('/member/edit', data),
  // 冻结会员
  freezeMember: (id) => request.post(`/member/freeze/${id}`),
  // 解冻会员
  unfreezeMember: (id) => request.post(`/member/unfreeze/${id}`),
  // 发送到期提醒
  sendExpireNotice: (id) => request.post(`/member/notice/expire/${id}`),
  // 套餐列表
  getPackageList: (params) => request.get('/member/package/list', { params }),
  // 新增套餐
  addPackage: (data) => request.post('/member/package/add', data),
  // 编辑套餐
  editPackage: (data) => request.put('/member/package/edit', data),
  // 删除套餐
  deletePackage: (id) => request.delete(`/member/package/delete/${id}`),
  // 续费记录
  getRenewalList: (params) => request.get('/member/renewal/list', { params }),
  // 续费
  renewal: (data) => request.post('/member/renewal', data)
}

// ========== 设备管理 ==========
export const deviceApi = {
  // 设备列表
  getDeviceList: (params) => request.get('/device/list', { params }),
  // 设备详情
  getDeviceDetail: (id) => request.get(`/device/detail/${id}`),
  // 新增设备
  addDevice: (data) => request.post('/device/add', data),
  // 编辑设备
  editDevice: (data) => request.put('/device/edit', data),
  // 删除设备
  deleteDevice: (id) => request.delete(`/device/delete/${id}`),
  // 远程重启
  remoteRestart: (id) => request.post(`/device/restart/${id}`),
  // 故障工单列表
  getFaultList: (params) => request.get('/device/fault/list', { params }),
  // 创建故障工单
  createFault: (data) => request.post('/device/fault/create', data),
  // 处理故障工单
  handleFault: (data) => request.post('/device/fault/handle', data)
}

// ========== 数据报表 ==========
export const reportApi = {
  // 营收统计
  getRevenueReport: (params) => request.get('/report/revenue', { params }),
  // 车流量报表
  getTrafficReport: (params) => request.get('/report/traffic', { params }),
  // 车位利用率报表
  getSpaceUsageReport: (params) => request.get('/report/spaceUsage', { params }),
  // 月卡营收报表
  getMemberRevenueReport: (params) => request.get('/report/memberRevenue', { params }),
  // 导出报表
  exportReport: (params) => request.get('/report/export', { params })
}

// ========== 系统设置 ==========
export const systemApi = {
  // 管理员列表
  getAdminList: (params) => request.get('/system/admin/list', { params }),
  // 新增管理员
  addAdmin: (data) => request.post('/system/admin/add', data),
  // 编辑管理员
  editAdmin: (data) => request.put('/system/admin/edit', data),
  // 删除管理员
  deleteAdmin: (id) => request.delete(`/system/admin/delete/${id}`),
  // 重置密码
  resetPassword: (id) => request.post(`/system/admin/resetPassword/${id}`),
  // 角色列表
  getRoleList: (params) => request.get('/system/role/list', { params }),
  // 新增角色
  addRole: (data) => request.post('/system/role/add', data),
  // 编辑角色
  editRole: (data) => request.put('/system/role/edit', data),
  // 删除角色
  deleteRole: (id) => request.delete(`/system/role/delete/${id}`),
  // 收费规则配置
  getChargeRule: () => request.get('/system/chargeRule'),
  // 保存收费规则
  saveChargeRule: (data) => request.post('/system/chargeRule/save', data),
  // 短信模板列表
  getSmsTemplateList: (params) => request.get('/system/sms/template/list', { params }),
  // 保存短信模板
  saveSmsTemplate: (data) => request.post('/system/sms/template/save', data),
  // 登录日志
  getLoginLogList: (params) => request.get('/system/log/login', { params }),
  // 操作日志
  getOperationLogList: (params) => request.get('/system/log/operation', { params })
}

// ========== 工作台 ==========
export const dashboardApi = {
  // 核心指标
  getDashboardStats: () => request.get('/dashboard/stats'),
  // 近7日营收趋势
  getRevenueTrend: (params) => request.get('/dashboard/revenueTrend', { params }),
  // 近7日车流量
  getTrafficTrend: (params) => request.get('/dashboard/trafficTrend', { params }),
  // 车位状态统计
  getSpaceStatusStats: () => request.get('/dashboard/spaceStatusStats'),
  // 实时通行列表
  getRealTimePassage: (params) => request.get('/dashboard/realTimePassage', { params })
}