/**
 * 简易字符串哈希（原生JS，无依赖，替代md5）
 * @param {string} str 原始特征串
 * @returns {string} 指纹hash串
 */
function simpleHash(str) {
    let h = 0;
    for (let i = 0; i < str.length; i++) {
        const char = str.charCodeAt(i);
        h = ((h << 5) - h) + char;
        h = h & h; // 转32位有符号整数
    }
    // 转16进制，补零，统一长度
    return Math.abs(h).toString(16).padStart(8, '0');
}

/**
 * 获取Canvas指纹
 */
function getCanvasFp() {
    const canvas = document.createElement('canvas');
    const ctx = canvas.getContext('2d');
    canvas.width = 220;
    canvas.height = 60;
    ctx.textBaseline = 'top';
    ctx.font = '16px Times New Roman';
    ctx.fillStyle = '#069';
    ctx.fillText('BrowserFP_2026!@#', 4, 4);
    ctx.fillStyle = 'rgba(102,204,0,0.7)';
    ctx.fillRect(20, 20, 80, 40);
    return canvas.toDataURL();
}

/**
 * 获取WebGL指纹
 */
function getWebGLFp() {
    try {
        const gl = document.createElement('canvas').getContext('webgl');
        const debugInfo = gl.getExtension('WEBGL_debug_renderer_info');
        const vendor = gl.getParameter(debugInfo.UNMASKED_VENDOR_WEBGL);
        const renderer = gl.getParameter(debugInfo.UNMASKED_RENDERER_WEBGL);
        return `${vendor}||${renderer}`;
    } catch (e) {
        return 'no-webgl';
    }
}

/**
 * 收集所有特征，返回原始特征字符串
 */
function collectFingerprintFeatures() {
    const features = {
        ua: navigator.userAgent,
        lang: navigator.language,
        langs: navigator.languages.join(','),
        tz: Intl.DateTimeFormat().resolvedOptions().timeZone,
        screen: `${screen.width}*${screen.height}|${screen.colorDepth}|${screen.pixelDepth}`,
        hardwareConcurrency: navigator.hardwareConcurrency || 0,
        canvas: getCanvasFp(),
        webgl: getWebGLFp(),
        platform: navigator.platform
    };
    // sort keys，保证每次序列化顺序一致（重点！不然同样特征hash不一样）
    const sortedKeys = Object.keys(features).sort();
    let rawStr = '';
    for (const k of sortedKeys) {
        rawStr += `${k}=${features[k]};`;
    }
    return rawStr;
}

export const getBrowserFingerprint = async (s) => {
    const raw = collectFingerprintFeatures();
    return simpleHash(raw);
};
