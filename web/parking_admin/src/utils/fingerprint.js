/**
 * 客户端设备指纹
 *
 * 后端验证码校验依赖指纹归属：发送验证码与提交校验须使用同一 fingerprint。
 * 因此指纹需跨页面、跨刷新保持稳定，故缓存于 localStorage。
 * 格式要求：3-64 位字母、数字、下划线（Reg.Fingerprint_REGEX）。
 */

const STORAGE_KEY = 'client_fingerprint'

/**
 * 生成一个新的设备指纹（长度约 19 位，字符集 [0-9a-z_]）
 */
const generate = () => {
  return 'fp_' + Date.now().toString(36) + Math.random().toString(36).slice(2, 10)
}

/**
 * 获取设备指纹：优先读 localStorage，不存在则生成并持久化
 * @returns {string}
 */
export const getFingerprint = () => {
  let fp = ''
  try {
    fp = localStorage.getItem(STORAGE_KEY) || ''
  } catch (e) {
    fp = ''
  }
  if (!fp) {
    fp = generate()
    try {
      localStorage.setItem(STORAGE_KEY, fp)
    } catch (e) {
      // localStorage 不可用时退化为内存态（当次会话内仍可用）
    }
  }
  return fp
}
