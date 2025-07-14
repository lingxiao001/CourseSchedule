/// <reference types='@dcloudio/types' />

import 'vue'

declare module '@vue/runtime-core' {
  type Hooks = App.AppInstance & Page.PageInstance;

  interface ComponentCustomOptions extends Hooks {
    // 可以在这里添加自定义选项
  }
}

declare module 'vue' {
  interface ComponentCustomProperties {
    // 添加全局属性
  }
}
