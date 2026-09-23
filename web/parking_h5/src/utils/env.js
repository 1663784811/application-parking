/**
 * 运行环境判断：微信 / 支付宝 / 普通浏览器。
 *
 * 两个必须知道的坑：
 * 1. 支付宝内置浏览器的 UA 里同样带 MicroMessenger（为兼容微信页面而伪装），
 *    所以支付宝必须放在微信前面判，顺序反了整个支付宝都会被认成微信；
 * 2. 微信小程序 webview 的 UA 也带 MicroMessenger（另有 miniProgram 标识），
 *    这里不做区分 —— 业务上小程序内打开 H5 与微信内打开走同一套。
 *
 * UA 不会变，模块加载时算一次就固定，不需要每次调用重新跑正则。
 * 需要的地方直接 `import { env, isWechat } from '@/utils/env'`，别在页面里重写正则。
 */

export const ENV = {
  WECHAT: 'wechat',
  ALIPAY: 'alipay',
  BROWSER: 'browser',
}

// 中文名，用于日志与界面展示
export const ENV_LABEL = {
  [ENV.WECHAT]: '微信',
  [ENV.ALIPAY]: '支付宝',
  [ENV.BROWSER]: '普通浏览器',
}

function detect() {
  const ua = navigator.userAgent
  if (/AlipayClient/i.test(ua)) {
    return ENV.ALIPAY
  }
  if (/MicroMessenger/i.test(ua)) {
    return ENV.WECHAT
  }
  return ENV.BROWSER
}

export const env = detect()
export const isWechat = env === ENV.WECHAT
export const isAlipay = env === ENV.ALIPAY
export const isBrowser = env === ENV.BROWSER
export const envLabel = ENV_LABEL[env]
