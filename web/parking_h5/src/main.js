import { createApp } from 'vue'
import { createPinia } from 'pinia'
import Vant from 'vant'
import App from './App.vue'
import router from './router'

// 主题变量
import './styles/variables.css'
// vant 样式
import 'vant/lib/index.css'

const app = createApp(App)

app.use(router)
app.use(createPinia())
app.use(Vant)

app.mount('#app')
