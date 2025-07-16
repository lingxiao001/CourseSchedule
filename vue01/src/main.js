import { createApp } from 'vue'
import App from './App.vue'
// import router from './router'
// import { createPinia } from 'pinia'

// 导入 Vant 4
import Vant from 'vant'
import 'vant/lib/index.css'

const app = createApp(App)
// const pinia = createPinia()

// 全局注册 Vant
app.use(Vant)

// app.use(pinia)
// app.use(router)
app.mount('#app')