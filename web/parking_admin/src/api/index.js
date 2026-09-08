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
  // 车位列表（按停车场/状态/类型/编号筛选，返回全部匹配车位，平面图）
  getSpaceList: (params) => authRequest.get('/admin/parking/space/list', { params }),
  // 车位详情
  getSpaceDetail: (id) => authRequest.get(`/admin/parking/space/find/${id}`),
  // 新增/更新车位
  saveSpace: (data) => authRequest.post('/admin/parking/space/save', data),
  // 分配车位（绑定会员/车牌/有效期，状态置为占用）
  assignSpace: (data) => authRequest.post('/admin/parking/space/assign', data),
  // 解绑车位（清除绑定，状态置为空闲）
  unbindSpace: (id) => authRequest.post(`/admin/parking/space/unbind/${id}`),
  // 车位报修（状态置为故障）
  reportRepair: (id) => authRequest.post(`/admin/parking/space/repair/${id}`),
  // 车位状态统计：{ free, fixed, temp, fault }
  getSpaceStats: (parkingId) => authRequest.get(`/admin/parking/space/stats/${parkingId}`),
  // 删除车位
  deleteSpace: (id) => authRequest.delete(`/admin/parking/space/delete/${id}`)
}

// ========== 通行记录 ==========
export const passageApi = {
  // 实时通行列表
  getRealTimeList: (params) => authRequest.get('/passage/realtime', { params }),
  // 通行记录列表
  getRecordList: (params) => authRequest.get('/admin/parking/carLog/list', { params }),
  // 通行记录详情
  getRecordDetail: (id) => authRequest.get(`/admin/parking/carLog/find/${id}`),
  // 今日通行数（今日入场车辆数）
  getTodayCount: () => authRequest.get('/admin/parking/carLog/todayCount'),
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
  // 对账汇总（总/线上/线下营收与订单数、差异金额）
  getReconcileStats: (params) => authRequest.get('/admin/report/reconcile/stats', { params }),
  // 对账明细（按停车场+日分页）
  getReconcileDetail: (params) => authRequest.get('/admin/report/reconcile/detail', { params }),
  // 优惠券列表（返回全部，无分页）
  getCouponList: (params) => authRequest.get('/admin/charge/coupon/list', { params }),
  // 新增优惠券（后端 /save 为新增+更新合并接口）
  addCoupon: (data) => authRequest.post('/admin/charge/coupon/save', data),
  // 编辑优惠券（与新增同走 /save upsert）
  editCoupon: (data) => authRequest.post('/admin/charge/coupon/save', data),
  // 删除优惠券
  deleteCoupon: (id) => authRequest.delete(`/admin/charge/coupon/delete/${id}`),
  // 发票列表
  getInvoiceList: (params) => authRequest.get('/charge/invoice/list', { params }),
  // 开票
  createInvoice: (data) => authRequest.post('/charge/invoice/create', data),
  // 冲红发票
  redInvoice: (id) => authRequest.post(`/charge/invoice/red/${id}`)
}

// ========== 计费规则 ==========
export const costRulesApi = {
  // 计费规则列表（分页，轻量返回，不携带 parkingIds；编辑由 getCostRulesDetail 携带）
  getCostRulesList: (params) => authRequest.get('/admin/parking/costRules/list', { params }),
  // 计费规则详情
  getCostRulesDetail: (id) => authRequest.get(`/admin/parking/costRules/find/${id}`),
  // 保存计费规则（新增或更新）
  saveCostRules: (data) => authRequest.post('/admin/parking/costRules/save', data),
  // 删除计费规则
  deleteCostRules: (id) => authRequest.delete(`/admin/parking/costRules/delete/${id}`),
  // 按停车场查询已关联规则ID集合（供 parkingList 回显）
  getCostRulesByParking: (parkingId) => authRequest.get(`/admin/parking/costRules/findByParkingId/${parkingId}`),
  // 按停车场设置计费规则关联（多对多同步）
  saveCostRulesByParking: (parkingId, costRulesIds) => authRequest.post(`/admin/parking/costRules/saveByParking/${parkingId}`, costRulesIds)
}

// ========== 通道管理 ==========
export const channelApi = {
  // 通道列表（分页）
  getChannelList: (params) => authRequest.get('/admin/parking/channel/list', { params }),
  // 通道详情
  getChannelDetail: (id) => authRequest.get(`/admin/parking/channel/find/${id}`),
  // 新增通道（后端 /save 为新增+更新合并接口）
  addChannel: (data) => authRequest.post('/admin/parking/channel/save', data),
  // 编辑通道（与新增同走 /save upsert）
  editChannel: (data) => authRequest.post('/admin/parking/channel/save', data),
  // 删除通道
  deleteChannel: (id) => authRequest.delete(`/admin/parking/channel/delete/${id}`)
}

// ========== 通道设备绑定 ==========
export const parkingDeviceApi = {
  // 通道已绑设备（含设备明细，按绑定顺序返回）
  getBoundDevices: (channelId) => authRequest.get(`/admin/parking/parkingDevice/byChannel/${channelId}`),
  // 未绑定通道的设备（候选）；parkingId 为空时查全部未绑设备
  getUnboundDevices: (parkingId) => authRequest.get('/admin/parking/parkingDevice/unbound', { params: { parkingId } }),
  // 按通道批量统计已绑设备数：传 channelIds 逗号分隔，返回 { [channelId]: count }
  countByChannel: (channelIds) => authRequest.get('/admin/parking/parkingDevice/countByChannel', { params: { channelIds: channelIds.join(',') } }),
  // 绑定设备到通道（设备全局唯一绑定）；摄像头需传 channelType（in/out/inout），道闸由服务端取通道类型
  bind: (deviceId, channelId, channelType) => authRequest.post(`/admin/parking/parkingDevice/bind/${deviceId}/${channelId}`, {}, { params: { channelType } }),
  // 解绑设备
  unbind: (deviceId, channelId) => authRequest.post(`/admin/parking/parkingDevice/unbind/${deviceId}/${channelId}`)
}

// ========== 会员管理 ==========
export const memberApi = {
  // 会员列表（分页）
  getMemberList: (params) => authRequest.get('/admin/member/list', { params }),
  // 会员详情
  getMemberDetail: (id) => authRequest.get(`/admin/member/find/${id}`),
  // 新增会员（后端 /save 为新增+更新合并接口）
  addMember: (data) => authRequest.post('/admin/member/save', data),
  // 编辑会员（与新增同走 /save upsert）
  editMember: (data) => authRequest.post('/admin/member/save', data),
  // 冻结会员
  freezeMember: (id) => authRequest.post(`/admin/member/freeze/${id}`),
  // 解冻会员
  unfreezeMember: (id) => authRequest.post(`/admin/member/unfreeze/${id}`),
  // 发送到期提醒
  sendExpireNotice: (id) => authRequest.post(`/admin/member/notice/expire/${id}`),
  // 会员数量统计
  getMemberStats: () => authRequest.get('/admin/member/stats'),
  // 套餐列表
  getPackageList: (params) => authRequest.get('/admin/member/package/list', { params }),
  // 新增套餐（后端 /save 为新增+更新合并接口）
  addPackage: (data) => authRequest.post('/admin/member/package/save', data),
  // 编辑套餐（与新增同走 /save upsert）
  editPackage: (data) => authRequest.post('/admin/member/package/save', data),
  // 删除套餐
  deletePackage: (id) => authRequest.delete(`/admin/member/package/delete/${id}`),
  // 续费记录列表（分页）
  getRenewalList: (params) => authRequest.get('/admin/member/renewal/list', { params }),
  // 续费（为会员续费并生成续费记录）
  renewal: (data) => authRequest.post('/admin/member/renewal', data)
}

// ========== 设备管理 ==========
export const deviceApi = {
  // 设备列表（分页）
  getDeviceList: (params) => authRequest.get('/admin/device/list', { params }),
  // 设备详情
  getDeviceDetail: (id) => authRequest.get(`/admin/device/find/${id}`),
  // 新增设备（后端 /save 为新增+更新合并接口）
  addDevice: (data) => authRequest.post('/admin/device/save', data),
  // 编辑设备（与新增同走 /save upsert）
  editDevice: (data) => authRequest.post('/admin/device/save', data),
  // 删除设备
  deleteDevice: (id) => authRequest.delete(`/admin/device/delete/${id}`),
  // 远程重启
  remoteRestart: (id) => authRequest.post(`/admin/device/restart/${id}`),
  // 修改设备密码（含账号）
  changePassword: (id, account, password) => authRequest.post(`/admin/device/changePassword/${id}`, { account, password }),
  // 设备数量统计
  getDeviceStats: () => authRequest.get('/admin/device/stats'),
  // 故障工单列表（分页）
  getFaultList: (params) => authRequest.get('/admin/device/fault/list', { params }),
  // 创建故障工单（根据设备ID报修）
  createFault: (data) => authRequest.post('/admin/device/fault/create', data),
  // 处理故障工单（指派/完成）
  handleFault: (data) => authRequest.post('/admin/device/fault/handle', data),
  // 故障工单统计
  getFaultStats: () => authRequest.get('/admin/device/fault/stats')
}

// ========== 物模型 ==========
export const thingModelApi = {
  // 物模型列表（分页）
  getModelList: (params) => authRequest.get('/admin/device/thingModel/list', { params }),
  // 物模型详情
  getModelDetail: (id) => authRequest.get(`/admin/device/thingModel/find/${id}`),
  // 保存物模型（新增+更新合并 upsert）
  saveModel: (data) => authRequest.post('/admin/device/thingModel/save', data),
  // 删除物模型（级联删除属性/事件/指令）
  deleteModel: (id) => authRequest.delete(`/admin/device/thingModel/delete/${id}`),
  // 物模型统计：{ total, attribute, event, command }
  getModelStats: () => authRequest.get('/admin/device/thingModel/stats'),
  // 子实体计数（批量）：传 modelIds 逗号分隔，返回 { [modelId]: { attribute, event, command } }
  getSubCounts: (modelIds) => authRequest.get('/admin/device/thingModel/subCounts', { params: { modelIds: modelIds.join(',') } }),
  // 属性
  getAttributeList: (thingModelId) => authRequest.get('/admin/device/thingModel/attribute/list', { params: { thingModelId } }),
  saveAttribute: (data) => authRequest.post('/admin/device/thingModel/attribute/save', data),
  deleteAttribute: (id) => authRequest.delete(`/admin/device/thingModel/attribute/delete/${id}`),
  // 事件
  getEventList: (thingModelId) => authRequest.get('/admin/device/thingModel/event/list', { params: { thingModelId } }),
  saveEvent: (data) => authRequest.post('/admin/device/thingModel/event/save', data),
  deleteEvent: (id) => authRequest.delete(`/admin/device/thingModel/event/delete/${id}`),
  // 指令
  getCommandList: (thingModelId) => authRequest.get('/admin/device/thingModel/command/list', { params: { thingModelId } }),
  saveCommand: (data) => authRequest.post('/admin/device/thingModel/command/save', data),
  deleteCommand: (id) => authRequest.delete(`/admin/device/thingModel/command/delete/${id}`)
}

// ========== 数据报表 ==========
export const reportApi = {
  // 营收汇总（总/线上/线下/优惠/订单数/同比）
  getRevenueStats: (params) => authRequest.get('/admin/report/revenue/stats', { params }),
  // 日营收趋势（图表）
  getRevenueDaily: (params) => authRequest.get('/admin/report/revenue/daily', { params }),
  // 营收明细（按日，分页）
  getRevenueDetail: (params) => authRequest.get('/admin/report/revenue/detail', { params }),
  // 车流量汇总（今日入场/出场、当前在场、峰值时段）
  getTrafficStats: (params) => authRequest.get('/admin/report/traffic/stats', { params }),
  // 今日分时段车流量（{ hour, inCount, outCount }）
  getTrafficHourly: (params) => authRequest.get('/admin/report/traffic/hourly', { params }),
  // 近7日车流量（{ date, inCount, outCount }）
  getTrafficDaily: (params) => authRequest.get('/admin/report/traffic/daily', { params }),
  // 今日2小时分段明细（{ period, inCount, outCount, inPark, inPeak, outPeak }）
  getTrafficTable: (params) => authRequest.get('/admin/report/traffic/table', { params }),
  // 车位利用率汇总：{ totalSpaces, currentIn, vacancyRate, peakRate, peakHour }
  getSpaceUsageStats: (params) => authRequest.get('/admin/report/space/stats', { params }),
  // 今日分时段在场车辆（{ hour(0-23), inLot }）
  getSpaceUsageHourly: (params) => authRequest.get('/admin/report/space/hourly', { params }),
  // 区域占用对比（{ area, total, occupied }）
  getSpaceArea: (params) => authRequest.get('/admin/report/space/area', { params }),
  // 月卡营收汇总：{ totalRevenue, renewalCount, memberCount, avgRevenue, peakMonth, peakRevenue }
  getMemberRevenueStats: (params) => authRequest.get('/admin/report/memberRevenue/stats', { params }),
  // 月卡营收趋势（按月）：{ month(yyyy-MM), revenue, count }
  getMemberRevenueTrend: (params) => authRequest.get('/admin/report/memberRevenue/trend', { params }),
  // 会员营收明细（按会员分页）：{ memberId, name, plate, cardType, renewalCount, totalAmount }
  getMemberRevenueDetail: (params) => authRequest.get('/admin/report/memberRevenue/detail', { params }),
  // 导出报表（待对接）
  exportReport: (params) => authRequest.get('/report/export', { params })
}

// ========== 系统设置 ==========
export const systemApi = {
  // 管理员列表
  getAdminList: (params) => authRequest.get('/system/admin/list', { params }),
  // 管理员详情（回填关联角色ID）
  getAdminDetail: (id) => authRequest.get(`/system/admin/find/${id}`),
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
  // 新增角色（后端 /save 为新增+更新合并接口）
  addRole: (data) => authRequest.post('/system/role/save', data),
  // 编辑角色（与新增同走 /save upsert）
  editRole: (data) => authRequest.post('/system/role/save', data),
  // 删除角色
  deleteRole: (id) => authRequest.delete(`/system/role/delete/${id}`),
  // 角色详情
  getRoleDetail: (id) => authRequest.get(`/system/role/find/${id}`),
  // 短信模板列表
  getSmsTemplateList: (params) => authRequest.get('/system/sms/template/list', { params }),
  // 保存短信模板（后端 /save 为新增+更新合并接口）
  saveSmsTemplate: (data) => authRequest.post('/system/sms/template/save', data),
  // 短信模板详情
  getSmsTemplateDetail: (id) => authRequest.get(`/system/sms/template/find/${id}`),
  // 删除短信模板
  deleteSmsTemplate: (id) => authRequest.delete(`/system/sms/template/delete/${id}`),
  // 日志列表（logType 区分登录日志/操作日志）
  getLogList: (params) => authRequest.get('/system/log/list', { params }),
  // 日志详情
  getLogDetail: (id) => authRequest.get(`/system/log/find/${id}`)
}

// ========== 工作台 ==========
export const dashboardApi = {
  // 核心指标（今日营收 / 在场车辆 / 进出车次 / 异常订单等）
  getDashboardStats: () => authRequest.get('/admin/dashboard/stats'),
  // 近7日营收趋势：{ date(yyyy-MM-dd), amount }
  getRevenueTrend: () => authRequest.get('/admin/dashboard/revenueTrend'),
  // 近7日车流量：{ date(yyyy-MM-dd), inCount, outCount }
  getTrafficTrend: () => authRequest.get('/admin/dashboard/trafficTrend')
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