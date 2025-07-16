<<<<<<< Updated upstream
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
=======
import { createApp } from 'vue'
import App from './App.vue'
import router from './router'
import { createPinia } from 'pinia'

// 添加以下Element Plus相关导入
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

const app = createApp(App)
const pinia = createPinia()

// 全局注册Element Plus
app.use(ElementPlus)

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

app.use(pinia)
app.use(router)
>>>>>>> Stashed changes
app.mount('#app')