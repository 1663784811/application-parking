class BrowserFingerprint {
    constructor(options = {}) {
        this.options = {
            cache: true,
            ...options
        };
        this.cacheKey = 'browser_fingerprint';
        this.fingerprint = null;
    }

    /**
     * 生成或获取缓存的浏览器指纹
     * @returns {Promise<string>} 32位浏览器指纹
     */
    async get() {
        // 优先使用缓存的指纹
        if (this.options.cache && this.fingerprint) {
            return this.fingerprint;
        }

        // 尝试从本地存储获取
        if (this.options.cache && typeof localStorage !== 'undefined') {
            const cachedFingerprint = localStorage.getItem(this.cacheKey);
            if (cachedFingerprint) {
                this.fingerprint = cachedFingerprint;
                return cachedFingerprint;
            }
        }

        // 生成新指纹
        const fingerprint = await this.generate();

        // 缓存指纹
        if (this.options.cache && typeof localStorage !== 'undefined') {
            localStorage.setItem(this.cacheKey, fingerprint);
        }

        this.fingerprint = fingerprint;
        return fingerprint;
    }

    /**
     * 生成新的浏览器指纹
     * @returns {Promise<string>} 32位浏览器指纹
     */
    async generate() {
        const components = await this.collectComponents();
        const fingerprintString = components.join('|');
        const hashBuffer = await this.hash(fingerprintString);
        const hashHex = this.bufferToHex(hashBuffer);
        return hashHex.slice(0, 32);
    }

    /**
     * 收集浏览器特征组件
     * @returns {Promise<Array<string>>} 特征组件数组
     */
    async collectComponents() {
        const navigatorInfo = window.navigator;
        const screenInfo = window.screen;
        const date = new Date();

        return [
            navigatorInfo.userAgent,
            navigatorInfo.language,
            navigatorInfo.platform,
            navigatorInfo.hardwareConcurrency,
            `${screenInfo.width}x${screenInfo.height}`,
            screenInfo.colorDepth,
            date.getTimezoneOffset(),
            performance.memory ? performance.memory.jsHeapSizeLimit : '',
            window.devicePixelRatio,
            await this.getFonts(),
            this.getCanvasFingerprint(),
            this.getWebGLFingerprint()
        ];
    }

    /**
     * 使用SHA-256哈希字符串
     * @param {string} text 要哈希的文本
     * @returns {Promise<ArrayBuffer>} 哈希结果
     */
    async hash(text) {
        return await crypto.subtle.digest('SHA-256', new TextEncoder().encode(text));
    }

    /**
     * 将ArrayBuffer转换为16进制字符串
     * @param {ArrayBuffer} buffer 要转换的缓冲区
     * @returns {string} 16进制字符串
     */
    bufferToHex(buffer) {
        return Array.from(new Uint8Array(buffer))
            .map(b => b.toString(16).padStart(2, '0'))
            .join('');
    }

    /**
     * 获取Canvas指纹
     * @returns {string} Canvas指纹
     */
    getCanvasFingerprint() {
        const canvas = document.createElement('canvas');
        const ctx = canvas.getContext('2d');

        // 绘制一些图案
        ctx.textBaseline = 'top';
        ctx.font = '14px "Arial"';
        ctx.textBaseline = 'alphabetic';
        ctx.fillStyle = '#f60';
        ctx.fillRect(125, 1, 62, 20);
        ctx.fillStyle = '#069';
        ctx.fillText('BrowserFingerprint', 2, 15);
        ctx.fillStyle = 'rgba(102, 204, 0, 0.7)';
        ctx.fillText('BrowserFingerprint', 4, 17);

        // 获取canvas数据
        const dataURL = canvas.toDataURL();
        // 提取部分数据作为指纹
        return dataURL.length > 100 ? dataURL.substring(10, 30) : 'no-canvas';
    }

    /**
     * 获取WebGL指纹
     * @returns {string} WebGL指纹
     */
    getWebGLFingerprint() {
        try {
            const canvas = document.createElement('canvas');
            const gl = canvas.getContext('webgl') || canvas.getContext('experimental-webgl');

            if (!gl) return 'no-webgl';

            const debugInfo = gl.getExtension('WEBGL_debug_renderer_info');
            const vendor = gl.getParameter(debugInfo.UNMASKED_VENDOR_WEBGL);
            const renderer = gl.getParameter(debugInfo.UNMASKED_RENDERER_WEBGL);

            return `${vendor}|${renderer}`;
        } catch (e) {
            return 'webgl-error';
        }
    }

    /**
     * 检测可用字体
     * @returns {Promise<string>} 可用字体列表的JSON字符串
     */
    async getFonts() {
        const baseFonts = ['monospace', 'sans-serif', 'serif'];
        const testFonts = [
            'Arial', 'Times New Roman', 'Courier New', 'Verdana', 'Georgia',
            'Palatino', 'Garamond', 'Bookman', 'Tahoma', 'Trebuchet MS',
            'Arial Black', 'Impact'
        ];

        const canvas = document.createElement('canvas');
        const ctx = canvas.getContext('2d');
        const detectedFonts = [];

        for (const testFont of testFonts) {
            for (const baseFont of baseFonts) {
                if (await this.checkFont(testFont, baseFont, ctx)) {
                    detectedFonts.push(testFont);
                    break;
                }
            }
        }

        return JSON.stringify(detectedFonts);
    }

    /**
     * 检查特定字体是否可用
     * @param {string} font 测试字体
     * @param {string} baseFont 基础字体
     * @param {CanvasRenderingContext2D} ctx Canvas上下文
     * @returns {Promise<boolean>} 字体是否可用
     */
    async checkFont(font, baseFont, ctx) {
        const text = 'abcdefghijklmnopqrstuvwxyz0123456789';
        const size = 100;

        // 使用基础字体测量文本宽度
        ctx.font = `${size}px ${baseFont}`;
        const baseWidth = ctx.measureText(text).width;

        // 使用测试字体+基础字体测量文本宽度
        ctx.font = `${size}px ${font},${baseFont}`;
        const testWidth = ctx.measureText(text).width;

        // 如果宽度不同，则字体可用
        return Math.abs(baseWidth - testWidth) > 0.5;
    }

    /**
     * 验证指纹是否与当前浏览器匹配
     * @param {string} fingerprint 要验证的指纹
     * @returns {Promise<boolean>} 指纹是否匹配
     */
    async verify(fingerprint) {
        const currentFingerprint = await this.get();
        return currentFingerprint === fingerprint;
    }
}
// 创建指纹实例
const fp = new BrowserFingerprint({ cache: true });

export const getBrowserFingerprint = async (s) => {
    // 获取指纹
    const fingerprint = await fp.get();
    return fingerprint + "";
};
