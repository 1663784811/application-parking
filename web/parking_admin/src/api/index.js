/**
 * API 统一导出模块
 *
 * 所有业务接口均使用 authRequest（基于 BaseResult 约定：code === 2000 为成功）。
 * 公开接口（免登录）使用 publicRequest。
 */
import authRequest from './authRequest'
import publicRequest from './publicRequest'

// ========== 停车场管理 ==========
export const parkingApi = {
  // 停车场列表
  getParkingList: (params) => authRequest.get('/admin/parking/parking/list', { params }),
  // 新增停车场
  addParking: (data) => authRequest.post('/admin/parking/parking/save', data),
  // 编辑停车场
  editParking: (data) => authRequest.post('/admin/parking/parking/save', data),
  // 删除停车场
  deleteParking: (id) => authRequest.delete(`/admin/parking/parking/delete/${id}`),
  // 停车场详情
  getParkingDetail: (id) => authRequest.get(`/admin/parking/parking/find/${id}`)
}

// ========== 车位管理 ==========
export const spaceApi = {
  // 车位列表
  getSpaceList: (params) => authRequest.get('/space/list', { params }),
  // 车位详情
  getSpaceDetail: (id) => authRequest.get(`/space/detail/${id}`),
  // 分配车位
  assignSpace: (data) => authRequest.post('/space/assign', data),
  // 解绑车位
  unbindSpace: (id) => authRequest.post(`/space/unbind/${id}`),
  // 报修车位
  reportRepair: (data) => authRequest.post('/space/repair', data),
  // 获取车位状态统计
  getSpaceStats: (parkingId) => authRequest.get(`/space/stats/${parkingId}`)
}

// ========== 通行记录 ==========
export const passageApi = {
  // 实时通行列表
  getRealTimeList: (params) => authRequest.get('/passage/realtime', { params }),
  // 通行记录列表
  getRecordList: (params) => authRequest.get('/admin/parking/carLog/list', { params }),
  // 通行记录详情
  getRecordDetail: (id) => authRequest.get(`/admin/parking/carLog/find/${id}`),
  // 异常记录列表
  getExceptionList: (params) => authRequest.get('/passage/exception/list', { params }),
  // 远程开闸
  openGate: (data) => authRequest.post('/passage/openGate', data),
  // 加入黑名单
  addBlacklist: (data) => authRequest.post('/passage/blacklist/add', data),
  // 移出黑名单
  removeBlacklist: (id) => authRequest.post(`/passage/blacklist/remove/${id}`),
  // 补缴费用
  payOverage: (data) => authRequest.post('/passage/payOverage', data)
}

// ========== 收费管理 ==========
export const chargeApi = {
  // 收费流水列表（订单列表）
  getOrderList: (params) => authRequest.get('/admin/order/list', { params }),
  // 订单详情
  getOrderDetail: (id) => authRequest.get(`/admin/order/find/${id}`),
  // 退款
  refund: (data) => authRequest.post('/admin/order/refund', data),
  // 对账记录
  getReconcileList: (params) => authRequest.get('/charge/reconcile/list', { params }),
  // 对账汇总
  getReconcileSummary: (params) => authRequest.get('/charge/reconcile/summary', { params }),
  // 优惠券列表
  getCouponList: (params) => authRequest.get('/charge/coupon/list', { params }),
  // 新增优惠券
  addCoupon: (data) => authRequest.post('/charge/coupon/add', data),
  // 编辑优惠券
  editCoupon: (data) => authRequest.put('/charge/coupon/edit', data),
  // 删除优惠券
  deleteCoupon: (id) => authRequest.delete(`/charge/coupon/delete/${id}`),
  // 发票列表
  getInvoiceList: (params) => authRequest.get('/charge/invoice/list', { params }),
  // 开票
  createInvoice: (data) => authRequest.post('/charge/invoice/create', data),
  // 冲红发票
  redInvoice: (id) => authRequest.post(`/charge/invoice/red/${id}`)
}

// ========== 计费规则 ==========
export const costRulesApi = {
  // 计费规则列表（按停车场ID查询，返回全部规则，无分页）
  getCostRulesList: (parkingId) => authRequest.get(`/admin/parking/costRules/list/${parkingId}`),
  // 计费规则详情
  getCostRulesDetail: (id) => authRequest.get(`/admin/parking/costRules/find/${id}`),
  // 保存计费规则（新增或更新）
  saveCostRules: (data) => authRequest.post('/admin/parking/costRules/save', data),
  // 删除计费规则
  deleteCostRules: (id) => authRequest.delete(`/admin/parking/costRules/delete/${id}`)
}

// ========== 会员管理 ==========
export const memberApi = {
  // 会员列表
  getMemberList: (params) => authRequest.get('/member/list', { params }),
  // 会员详情
  getMemberDetail: (id) => authRequest.get(`/member/detail/${id}`),
  // 新增会员
  addMember: (data) => authRequest.post('/member/add', data),
  // 编辑会员
  editMember: (data) => authRequest.put('/member/edit', data),
  // 冻结会员
  freezeMember: (id) => authRequest.post(`/member/freeze/${id}`),
  // 解冻会员
  unfreezeMember: (id) => authRequest.post(`/member/unfreeze/${id}`),
  // 发送到期提醒
  sendExpireNotice: (id) => authRequest.post(`/member/notice/expire/${id}`),
  // 套餐列表
  getPackageList: (params) => authRequest.get('/member/package/list', { params }),
  // 新增套餐
  addPackage: (data) => authRequest.post('/member/package/add', data),
  // 编辑套餐
  editPackage: (data) => authRequest.put('/member/package/edit', data),
  // 删除套餐
  deletePackage: (id) => authRequest.delete(`/member/package/delete/${id}`),
  // 续费记录
  getRenewalList: (params) => authRequest.get('/member/renewal/list', { params }),
  // 续费
  renewal: (data) => authRequest.post('/member/renewal', data)
}

// ========== 设备管理 ==========
export const deviceApi = {
  // 设备列表
  getDeviceList: (params) => authRequest.get('/device/list', { params }),
  // 设备详情
  getDeviceDetail: (id) => authRequest.get(`/device/detail/${id}`),
  // 新增设备
  addDevice: (data) => authRequest.post('/device/add', data),
  // 编辑设备
  editDevice: (data) => authRequest.put('/device/edit', data),
  // 删除设备
  deleteDevice: (id) => authRequest.delete(`/device/delete/${id}`),
  // 远程重启
  remoteRestart: (id) => authRequest.post(`/device/restart/${id}`),
  // 故障工单列表
  getFaultList: (params) => authRequest.get('/device/fault/list', { params }),
  // 创建故障工单
  createFault: (data) => authRequest.post('/device/fault/create', data),
  // 处理故障工单
  handleFault: (data) => authRequest.post('/device/fault/handle', data)
}

// ========== 数据报表 ==========
export const reportApi = {
  // 营收统计
  getRevenueReport: (params) => authRequest.get('/report/revenue', { params }),
  // 车流量报表
  getTrafficReport: (params) => authRequest.get('/report/traffic', { params }),
  // 车位利用率报表
  getSpaceUsageReport: (params) => authRequest.get('/report/spaceUsage', { params }),
  // 月卡营收报表
  getMemberRevenueReport: (params) => authRequest.get('/report/memberRevenue', { params }),
  // 导出报表
  exportReport: (params) => authRequest.get('/report/export', { params })
}

// ========== 系统设置 ==========
export const systemApi = {
  // 管理员列表
  getAdminList: (params) => authRequest.get('/system/admin/list', { params }),
  // 新增管理员
  addAdmin: (data) => authRequest.post('/system/admin/add', data),
  // 编辑管理员
  editAdmin: (data) => authRequest.put('/system/admin/edit', data),
  // 删除管理员
  deleteAdmin: (id) => authRequest.delete(`/system/admin/delete/${id}`),
  // 重置密码
  resetPassword: (id) => authRequest.post(`/system/admin/resetPassword/${id}`),
  // 角色列表
  getRoleList: (params) => authRequest.get('/system/role/list', { params }),
  // 新增角色
  addRole: (data) => authRequest.post('/system/role/add', data),
  // 编辑角色
  editRole: (data) => authRequest.put('/system/role/edit', data),
  // 删除角色
  deleteRole: (id) => authRequest.delete(`/system/role/delete/${id}`),
  // 短信模板列表
  getSmsTemplateList: (params) => authRequest.get('/system/sms/template/list', { params }),
  // 保存短信模板
  saveSmsTemplate: (data) => authRequest.post('/system/sms/template/save', data),
  // 登录日志
  getLoginLogList: (params) => authRequest.get('/system/log/login', { params }),
  // 操作日志
  getOperationLogList: (params) => authRequest.get('/system/log/operation', { params })
}

// ========== 工作台 ==========
export const dashboardApi = {
  // 核心指标
  getDashboardStats: () => authRequest.get('/dashboard/stats'),
  // 近7日营收趋势
  getRevenueTrend: (params) => authRequest.get('/dashboard/revenueTrend', { params }),
  // 近7日车流量
  getTrafficTrend: (params) => authRequest.get('/dashboard/trafficTrend', { params }),
  // 车位状态统计
  getSpaceStatusStats: () => authRequest.get('/dashboard/spaceStatusStats'),
  // 实时通行列表
  getRealTimePassage: (params) => authRequest.get('/dashboard/realTimePassage', { params })
}

// ========== 企业/登录/注册（公开接口，走 BaseResult 契约） ==========
export const enterpriseApi = {
  // 查询是否已存在企业（免登录）：GET /admin/login/findAny
  // 成功返回 BaseResult，data 为企业对象(AuEnterprise) 或 null
  checkEnterpriseExists: () => publicRequest.get('/admin/login/findAny'),

  // 获取图片验证码键值（免登录）：POST /common/verify/getVerifyCode
  // body: { fingerprint } → data 为 keyCode（字符串）
  getVerifyCode: (data) => publicRequest.post('/common/verify/getVerifyCode', data),

  // 拼接验证码图片地址（免登录）：GET /common/verify/getVerifyImg/{keyCode}
  // 返回 PNG，由 <img :src> 直接展示，故不走 axios；保留 /api 前缀经 vite 代理转发
  // 带 ?t=时间戳防浏览器缓存，确保刷新时拿到新验证码图片
  getVerifyImgUrl: (keyCode) => `/api/common/verify/getVerifyImg/${keyCode}?t=${Date.now()}`,

  // 发送手机验证码（免登录）：POST /common/verify/getVerifyPhoneCode
  // body: { fingerprint, phone }
  sendRegisterPhoneCode: (data) => publicRequest.post('/common/verify/getVerifyPhoneCode', data),

  // 企业管理员登录（免登录）：POST /admin/login/login
  // body: { enId, username, password, code, fingerprint }
  // 成功返回 BaseResult，data 含 { jwtToken, refreshToken }
  adminLogin: (data) => publicRequest.post('/admin/login/login', data),

  // 注册企业（免登录）：POST /root/login/registerEnterprise
  // body: { name, logo, person, phone, username, password, code, fingerprint }
  // 成功返回 BaseResult，data 含 { id, name, logo, eCode, person, phone, username }
  registerEnterprise: (data) => publicRequest.post('/root/login/registerEnterprise', data)
}

// ========== 当前登录用户（鉴权接口，走 BaseResult 契约 + Authorization） ==========
export const userApi = {
  // 获取当前登录用户信息：GET /common/token/findUserInfo
  // 成功返回 BaseResult，data = { baseInfo, role, permission, auEnterprise }
  findUserInfo: () => authRequest.get('/common/token/findUserInfo'),

  // 刷新访问令牌：POST /common/token/refreshToken
  // body: { refreshToken } → data = { jwtToken, refreshToken }
  refreshToken: (data) => authRequest.post('/common/token/refreshToken', data)
}