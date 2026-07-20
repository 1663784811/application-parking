/**
 * 系统常量配置
 * 遵循停车场门口端管理页面设计方案
 */

// 颜色体系
export const COLORS = {
  // 主色
  primary: '#165DFF',
  primaryLight: '#4080FF',
  primaryDark: '#1046E0',

  // 警示色
  danger: '#F53F3F',
  dangerLight: '#FF7875',
  dangerDark: '#CB2B2B',

  // 成功色
  success: '#00B42A',
  successLight: '#23C343',
  successDark: '#009918',

  // 预警色
  warning: '#FF7D00',
  warningLight: '#FF9A3C',
  warningDark: '#E56A00',

  // 中性色
  titleText: '#1D2129',
  bodyText: '#4E5969',
  secondaryText: '#86909C',
  background: '#F2F3F5',
  cardBackground: '#FFFFFF',
  border: '#E5E6EB',
  borderLight: '#F2F3F5'
}

// 设备状态
export const DEVICE_STATUS = {
  ONLINE: 'online',
  OFFLINE: 'offline',
  FAULT: 'fault'
}

// 车辆通行状态
export const PASSAGE_STATUS = {
  NORMAL: 'normal',
  ABNORMAL: 'abnormal',
  UNPAID: 'unpaid',
  BLACKLIST: 'blacklist',
  NO_PLATE: 'noPlate'
}

// 放行类型
export const RELEASE_TYPE = {
  NORMAL: 'normal',
  UNPAID: 'unpaid',
  BLACKLIST: 'blacklist',
  INTERNAL: 'internal'
}

// 设备类型
export const DEVICE_TYPE = {
  GATE: 'gate',           // 道闸
  CAMERA: 'camera',       // 摄像头
  GROUND_SENSOR: 'groundSensor',  // 地感
  DISPLAY: 'display',     // 显示屏
  CONTROLLER: 'controller' // 控制器
}

// 异常类型
export const EXCEPTION_TYPE = {
  NO_PLATE: 'noPlate',           // 无牌车辆
  BLACKLIST: 'blacklist',         // 黑名单
  OVERTIME: 'overtime',          // 超时滞留
  UNPAID: 'unpaid'               // 逃费车辆
}

// 操作类型
export const OPERATION_TYPE = {
  OPEN_GATE: 'openGate',           // 开闸放行
  TEMP_PERMISSION: 'tempPermission', // 临时权限开通
  EXCEPTION_HANDLE: 'exceptionHandle', // 异常处理
  DEVICE_OPERATION: 'deviceOperation', // 设备操作
  SYSTEM_SETTING: 'systemSetting'      // 系统设置
}

// 用户权限
export const USER_ROLE = {
  GUARD: 'guard',         // 普通值守员
  ADMIN: 'admin'           // 管理员
}

// 刷新频率（毫秒）
export const REFRESH_INTERVAL = 3000

// 布局尺寸
export const LAYOUT_SIZE = {
  TOP_BAR_HEIGHT: 60,
  SIDE_BAR_WIDTH: 200,
  CARD_BORDER_RADIUS: 4,
  BUTTON_PRIMARY_SIZE: 48,
  BUTTON_SECONDARY_SIZE: 32
}

// 字体大小
export const FONT_SIZE = {
  TITLE: 18,
  SUBTITLE: 16,
  BODY: 14,
  SMALL: 12
}