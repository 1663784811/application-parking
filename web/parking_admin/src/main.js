import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import pinia from './stores'
import ViewUIPlus from 'view-ui-plus'
import './styles/viewuiplus.css'
import './styles/variables.css'

const app = createApp(App)

// 使用路由
app.use(router)

// 使用 Pinia
app.use(pinia)

// 使用 View UI Plus
app.use(ViewUIPlus)

app.mount('#app')
