import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import { fileURLToPath, URL } from 'node:url'

// https://vite.dev/config/
export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      // @ 指向 src 目录
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  build: {
    rollupOptions: {
      // 多页入口：不加这段 oauth.html 不会进构建产物（dev 下倒是能直接访问）
      input: {
        index: fileURLToPath(new URL('./index.html', import.meta.url)),
        // 微信授权中转页：微信回跳的 redirect_uri 指向它，所以它必须是独立 HTML（不带 hash）
        oauth: fileURLToPath(new URL('./oauth.html', import.meta.url)),
      },
    },
  },
  server: {
    port: 3108,
    host: '0.0.0.0',
    proxy: {
      '/api/admin/workflow': {
        target: 'http://localhost:10000',
        changeOrigin: true,
        // rewrite: (path) => path.replace(/^\/api/, '')
      },
      '/api': {
        target: 'http://localhost:10000',
        changeOrigin: true,
        // rewrite: (path) => path.replace(/^\/api/, '')
      },
      '/file': {
        target: 'http://localhost:80',
        changeOrigin: true,
        // rewrite: (path) => path.replace(/^\/api/, '')
      },
    },
  },
})
