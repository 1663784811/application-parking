/**
 * 应用入口
 */

import { createApp } from 'vue'
import { createPinia } from 'pinia'
import ViewUIPlus from 'view-ui-plus'
import App from './App.vue'
import router from './router'

// 导入样式
import 'view-ui-plus/dist/styles/viewuiplus.css'
import '@/styles/variables.css'

// 创建应用实例
const app = createApp(App)

// 安装插件
app.use(createPinia())
app.use(router)
app.use(ViewUIPlus)

// 挂载应用
app.mount('#app')