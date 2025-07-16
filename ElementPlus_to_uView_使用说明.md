# Element Plus → uView UI 替换使用说明

## 📋 概述

这个项目提供了完整的 Element Plus 到 uView UI 的替换方案，包括：

1. **详细替换指南** (`ElementPlus_to_uView_替换指南.md`)
2. **实际替换示例** (`vue01/src/views/teacher/TeachingClasses_uView.vue`)
3. **自动化替换脚本** (`element_plus_to_uview_replace.js`)

## 🚀 快速开始

### 方法一：使用自动化脚本（推荐）

```bash
# 1. 给脚本添加执行权限
chmod +x element_plus_to_uview_replace.js

# 2. 运行替换脚本
node element_plus_to_uview_replace.js ./vue01/src

# 3. 安装 uView UI
cd vue01
npm install uview-ui@2.0.36

# 4. 检查并修复可能的错误
npm run serve
```

### 方法二：手动替换

1. **安装 uView UI**
```bash
cd vue01
npm install uview-ui@2.0.36
```

2. **更新 main.js**
```javascript
// 移除 Element Plus
// import ElementPlus from 'element-plus'
// import 'element-plus/dist/index.css'
// import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// 添加 uView
import uView from 'uview-ui'

const app = createApp(App)
app.use(uView) // 替换 app.use(ElementPlus)
```

3. **逐个文件替换组件**

## 📚 详细替换指南

### 1. 组件映射表

| Element Plus | uView UI | 说明 |
|-------------|----------|------|
| `<el-button>` | `<u-button>` | 按钮组件 |
| `<el-input>` | `<u-input>` | 输入框组件 |
| `<el-select>` | `<u-select>` | 选择器组件 |
| `<el-table>` | `<u-table>` | 表格组件 |
| `<el-dialog>` | `<u-popup>` | 弹窗组件 |
| `<el-card>` | `<u-card>` | 卡片组件 |
| `<el-tag>` | `<u-tag>` | 标签组件 |
| `<el-pagination>` | `<u-pagination>` | 分页组件 |
| `<el-page-header>` | `<u-navbar>` | 页面头部 |
| `<el-icon>` | `<u-icon>` | 图标组件 |

### 2. 属性映射

```vue
<!-- Element Plus -->
<el-button type="danger" size="small" :icon="Plus">
  按钮
</el-button>

<!-- uView UI -->
<u-button type="error" size="mini" :icon="'plus'">
  按钮
</u-button>
```

### 3. 图标替换

```vue
<!-- Element Plus -->
<el-icon><Plus /></el-icon>

<!-- uView UI -->
<u-icon name="plus" />
```

### 4. 消息提示替换

```javascript
// Element Plus
ElMessage.success('操作成功')
ElMessage.error('操作失败')

// uView UI
uni.showToast({ title: '操作成功', icon: 'success' })
uni.showToast({ title: '操作失败', icon: 'error' })
```

### 5. 路由替换

```javascript
// Element Plus
this.$router.push('/admin/dashboard')
this.$router.go(-1)

// uView UI
uni.navigateTo({ url: '/pages/admin/dashboard' })
uni.navigateBack()
```

## 🔧 自动化脚本功能

### 脚本功能

1. **组件标签替换**：自动替换所有 Element Plus 组件标签
2. **属性映射**：自动转换组件属性
3. **图标替换**：自动替换图标组件和图标名称
4. **消息提示替换**：自动替换 ElMessage 和 ElMessageBox
5. **路由替换**：自动替换 Vue Router 为 uni-app 路由
6. **文件更新**：自动更新 main.js 和 package.json

### 使用方法

```bash
# 替换指定目录
node element_plus_to_uview_replace.js ./vue01/src

# 替换当前目录
node element_plus_to_uview_replace.js .

# 查看帮助
node element_plus_to_uview_replace.js --help
```

### 脚本输出示例

```
🚀 开始替换 Element Plus 到 uView UI...
📁 目标路径: ./vue01/src
✅ 已处理: ./vue01/src/views/teacher/TeachingClasses.vue
✅ 已处理: ./vue01/src/views/admin/UserManagement.vue
⏭️  无需处理: ./vue01/src/views/auth/LoginView.vue
✅ 已更新: ./vue01/src/main.js
✅ 已更新: ./vue01/package.json

🎉 替换完成！共处理了 15 个文件

📝 后续步骤:
1. 运行 npm install 安装 uView UI
2. 检查并修复可能的语法错误
3. 测试功能完整性
4. 调整样式和布局
```

## 📝 注意事项

### 1. 模板标签替换

```vue
<!-- Element Plus -->
<div class="container">
  <span>文本</span>
  <p>段落</p>
</div>

<!-- uView UI -->
<view class="container">
  <text>文本</text>
  <text>段落</text>
</view>
```

### 2. 样式单位

```css
/* Element Plus (px) */
.container {
  padding: 20px;
  font-size: 14px;
}

/* uView UI (rpx) */
.container {
  padding: 20rpx;
  font-size: 28rpx;
}
```

### 3. 平台差异

```javascript
// 检测平台
// #ifdef H5
console.log('H5 平台')
// #endif

// #ifdef MP-WEIXIN
console.log('微信小程序')
// #endif

// #ifdef APP-PLUS
console.log('App 平台')
// #endif
```

### 4. 事件处理

```vue
<!-- Element Plus -->
<el-input @keyup.enter="handleSearch" />

<!-- uView UI -->
<u-input @confirm="handleSearch" />
```

## 🐛 常见问题

### 1. 组件不显示

**问题**：uView 组件不显示或样式异常

**解决**：
```javascript
// 确保正确导入 uView
import uView from 'uview-ui'
app.use(uView)

// 确保样式文件正确引入
import 'uview-ui/index.scss'
```

### 2. 图标不显示

**问题**：uView 图标不显示

**解决**：
```vue
<!-- 正确的图标使用方式 -->
<u-icon name="plus" />
<u-icon name="search" />
<u-icon name="arrow-left" />
```

### 3. 弹窗不工作

**问题**：u-popup 不显示

**解决**：
```vue
<!-- 正确的弹窗使用方式 -->
<u-popup v-model="showPopup" mode="center">
  <view class="popup-content">
    内容
  </view>
</u-popup>
```

### 4. 表格数据不显示

**问题**：u-table 数据不显示

**解决**：
```vue
<!-- 正确的表格使用方式 -->
<u-table :data="tableData" :border="true" :stripe="true">
  <u-table-column prop="name" label="姓名" />
  <u-table-column prop="age" label="年龄" />
</u-table>
```

## 📖 学习资源

### 官方文档
- [uView UI 官方文档](https://www.uviewui.com/)
- [uni-app 官方文档](https://uniapp.dcloud.io/)

### 组件示例
- [uView UI 组件示例](https://www.uviewui.com/components/intro.html)
- [uni-app 组件示例](https://uniapp.dcloud.io/component/)

### 社区资源
- [uView UI 社区](https://www.uviewui.com/community.html)
- [uni-app 社区](https://ask.dcloud.net.cn/)

## 🤝 贡献

如果您发现任何问题或有改进建议，请：

1. 检查现有文档
2. 搜索相关问题
3. 提交 Issue 或 Pull Request

## 📄 许可证

本项目采用 MIT 许可证。 