# Element Plus → uView UI 替换指南

## 1. 依赖替换

### 1.1 移除 Element Plus 依赖
```bash
npm uninstall element-plus @element-plus/icons-vue
```

### 1.2 安装 uView UI
```bash
npm install uview-ui@2.0.36
```

### 1.3 更新 main.js
```javascript
// 原 main.js
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

const app = createApp(App)
app.use(ElementPlus)

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 新的 main.js
import uView from 'uview-ui'

const app = createApp(App)
app.use(uView)
```

## 2. 组件映射表

| Element Plus 组件 | uView UI 组件 | 说明 |
|------------------|---------------|------|
| `<el-button>` | `<u-button>` | 按钮组件 |
| `<el-input>` | `<u-input>` | 输入框组件 |
| `<el-select>` | `<u-select>` | 选择器组件 |
| `<el-option>` | `<u-option>` | 选项组件 |
| `<el-form>` | `<u-form>` | 表单组件 |
| `<el-form-item>` | `<u-form-item>` | 表单项组件 |
| `<el-table>` | `<u-table>` | 表格组件 |
| `<el-table-column>` | `<u-table-column>` | 表格列组件 |
| `<el-dialog>` | `<u-popup>` | 弹窗组件 |
| `<el-card>` | `<u-card>` | 卡片组件 |
| `<el-tag>` | `<u-tag>` | 标签组件 |
| `<el-pagination>` | `<u-pagination>` | 分页组件 |
| `<el-input-number>` | `<u-number-box>` | 数字输入框 |
| `<el-divider>` | `<u-line>` | 分割线 |
| `<el-page-header>` | `<u-navbar>` | 页面头部 |
| `<el-icon>` | `<u-icon>` | 图标组件 |
| `<el-empty>` | `<u-empty>` | 空状态组件 |
| `<el-spinner>` | `<u-loading>` | 加载组件 |

## 3. 属性映射

### 3.1 按钮组件
```vue
<!-- Element Plus -->
<el-button type="primary" size="small" :icon="Plus" @click="handleClick">
  按钮
</el-button>

<!-- uView UI -->
<u-button type="primary" size="mini" :icon="'plus'" @click="handleClick">
  按钮
</u-button>
```

### 3.2 输入框组件
```vue
<!-- Element Plus -->
<el-input 
  v-model="inputValue" 
  placeholder="请输入" 
  clearable 
  @clear="handleClear"
>
  <template #append>
    <el-button :icon="Search" @click="handleSearch" />
  </template>
</el-input>

<!-- uView UI -->
<u-input 
  v-model="inputValue" 
  placeholder="请输入" 
  :clearable="true"
  @clear="handleClear"
>
  <template #suffix>
    <u-icon name="search" @click="handleSearch" />
  </template>
</u-input>
```

### 3.3 选择器组件
```vue
<!-- Element Plus -->
<el-select v-model="selectedValue" placeholder="请选择" filterable>
  <el-option 
    v-for="item in options" 
    :key="item.value" 
    :label="item.label" 
    :value="item.value" 
  />
</el-select>

<!-- uView UI -->
<u-select v-model="selectedValue" placeholder="请选择" :filterable="true">
  <u-option 
    v-for="item in options" 
    :key="item.value" 
    :label="item.label" 
    :value="item.value" 
  />
</u-select>
```

### 3.4 表格组件
```vue
<!-- Element Plus -->
<el-table :data="tableData" border stripe v-loading="loading">
  <el-table-column prop="name" label="姓名" />
  <el-table-column label="操作">
    <template #default="scope">
      <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
    </template>
  </el-table-column>
</el-table>

<!-- uView UI -->
<u-table :data="tableData" :border="true" :stripe="true" :loading="loading">
  <u-table-column prop="name" label="姓名" />
  <u-table-column label="操作">
    <template #default="{ row }">
      <u-button size="mini" @click="handleEdit(row)">编辑</u-button>
    </template>
  </u-table-column>
</u-table>
```

### 3.5 弹窗组件
```vue
<!-- Element Plus -->
<el-dialog 
  v-model="showDialog" 
  title="标题" 
  width="600px"
  @closed="handleClosed"
>
  <div>内容</div>
  <template #footer>
    <el-button @click="showDialog = false">取消</el-button>
    <el-button type="primary" @click="handleConfirm">确定</el-button>
  </template>
</el-dialog>

<!-- uView UI -->
<u-popup 
  v-model="showDialog" 
  mode="center"
  :width="600"
  @close="handleClosed"
>
  <view class="popup-content">
    <view class="popup-header">标题</view>
    <view class="popup-body">内容</view>
    <view class="popup-footer">
      <u-button @click="showDialog = false">取消</u-button>
      <u-button type="primary" @click="handleConfirm">确定</u-button>
    </view>
  </view>
</u-popup>
```

### 3.6 卡片组件
```vue
<!-- Element Plus -->
<el-card class="class-card">
  <template #header>
    <div class="card-header">
      <span>标题</span>
      <el-tag size="small" type="info">标签</el-tag>
    </div>
  </template>
  <div>内容</div>
</el-card>

<!-- uView UI -->
<u-card class="class-card">
  <template #header>
    <view class="card-header">
      <text>标题</text>
      <u-tag size="mini" type="info">标签</u-tag>
    </view>
  </template>
  <view>内容</view>
</u-card>
```

## 4. 图标替换

### 4.1 移除 Element Plus 图标
```javascript
// 移除这些导入
import { Search, Plus, ArrowLeftBold } from '@element-plus/icons-vue'
```

### 4.2 使用 uView 图标
```vue
<!-- Element Plus 图标 -->
<el-icon><Plus /></el-icon>

<!-- uView 图标 -->
<u-icon name="plus" />
```

### 4.3 常用图标映射
```javascript
// Element Plus → uView 图标映射
const iconMapping = {
  'Plus': 'plus',
  'Search': 'search',
  'ArrowLeftBold': 'arrow-left',
  'Edit': 'edit-pen',
  'Delete': 'trash',
  'Close': 'close',
  'Check': 'checkmark',
  'Warning': 'warning',
  'Info': 'info-circle',
  'Success': 'checkmark-circle',
  'Error': 'close-circle'
}
```

## 5. 消息提示替换

### 5.1 移除 Element Plus 消息
```javascript
// 移除这些导入
import { ElMessage, ElMessageBox } from 'element-plus'
```

### 5.2 使用 uView 消息
```javascript
// 成功提示
uni.showToast({
  title: '操作成功',
  icon: 'success'
})

// 错误提示
uni.showToast({
  title: '操作失败',
  icon: 'error'
})

// 确认对话框
uni.showModal({
  title: '确认删除',
  content: '确定要删除这条记录吗？',
  success: (res) => {
    if (res.confirm) {
      // 执行删除操作
    }
  }
})
```

## 6. 样式适配

### 6.1 全局样式
```css
/* 移除 Element Plus 样式 */
/* import 'element-plus/dist/index.css' */

/* 添加 uView 样式 */
@import 'uview-ui/index.scss';
```

### 6.2 组件样式调整
```css
/* Element Plus 样式 */
.el-button {
  margin-right: 10px;
}

.el-input {
  width: 300px;
}

/* uView 样式 */
.u-button {
  margin-right: 10px;
}

.u-input {
  width: 300px;
}
```

## 7. 实际项目替换示例

### 7.1 TeachingClasses.vue 替换示例

```vue
<!-- 原 Element Plus 版本 -->
<template>
  <div class="teaching-classes-management">
    <el-page-header :icon="ArrowLeftBold" title="" @back="$router.go(-1)">
      <template #content>
        <div class="flex items-center">
          <span class="text-large font-600 mr-3">教学班管理</span>
          <el-tag type="warning">教师工作台</el-tag>
        </div>
      </template>
    </el-page-header>

    <el-divider />

    <div class="action-bar">
      <el-button type="primary" @click="showAddDialog = true">
        <el-icon><Plus /></el-icon> 添加教学班
      </el-button>
      
      <el-input
        v-model="searchQuery"
        placeholder="搜索教学班代码"
        style="width: 300px"
        clearable
        @clear="handleSearch"
        @keyup.enter="handleSearch"
      >
        <template #append>
          <el-button :icon="Search" @click="handleSearch" />
        </template>
      </el-input>
    </div>

    <el-table :data="filteredClasses" border stripe v-loading="loading">
      <el-table-column prop="classCode" label="教学班代码" />
      <el-table-column prop="courseName" label="所属课程" />
      <el-table-column label="操作">
        <template #default="scope">
          <el-button size="small" @click="handleEdit(scope.row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>
```

```vue
<!-- uView UI 版本 -->
<template>
  <view class="teaching-classes-management">
    <u-navbar 
      title="教学班管理" 
      :left-icon="'arrow-left'" 
      @left-click="goBack"
    >
      <template #right>
        <u-tag type="warning" text="教师工作台" />
      </template>
    </u-navbar>

    <u-line />

    <view class="action-bar">
      <u-button type="primary" @click="showAddDialog = true">
        <u-icon name="plus" />
        添加教学班
      </u-button>
      
      <u-input
        v-model="searchQuery"
        placeholder="搜索教学班代码"
        :clearable="true"
        @clear="handleSearch"
        @confirm="handleSearch"
      >
        <template #suffix>
          <u-icon name="search" @click="handleSearch" />
        </template>
      </u-input>
    </view>

    <u-table :data="filteredClasses" :border="true" :stripe="true" :loading="loading">
      <u-table-column prop="classCode" label="教学班代码" />
      <u-table-column prop="courseName" label="所属课程" />
      <u-table-column label="操作">
        <template #default="{ row }">
          <u-button size="mini" @click="handleEdit(row)">编辑</u-button>
          <u-button size="mini" type="error" @click="handleDelete(row.id)">删除</u-button>
        </template>
      </u-table-column>
    </u-table>
  </view>
</template>

<script setup>
// 移除 Element Plus 相关导入
// import { Search, Plus, ArrowLeftBold } from '@element-plus/icons-vue'
// import { ElMessage, ElMessageBox } from 'element-plus'

// 添加 uni-app 相关方法
const goBack = () => {
  uni.navigateBack()
}

// 替换消息提示
const showSuccess = (message) => {
  uni.showToast({
    title: message,
    icon: 'success'
  })
}

const showError = (message) => {
  uni.showToast({
    title: message,
    icon: 'error'
  })
}

const showConfirm = (title, content) => {
  return new Promise((resolve) => {
    uni.showModal({
      title,
      content,
      success: (res) => {
        resolve(res.confirm)
      }
    })
  })
}
</script>
```

## 8. 注意事项

1. **模板标签替换**：`<div>` → `<view>`，`<span>` → `<text>`
2. **事件处理**：某些事件名称可能不同
3. **样式单位**：uni-app 中推荐使用 rpx 单位
4. **平台差异**：某些组件在不同平台表现可能不同
5. **性能优化**：uView 组件针对移动端进行了优化

## 9. 迁移步骤

1. **备份原项目**
2. **安装 uView UI**
3. **更新 main.js**
4. **逐个文件替换组件**
5. **调整样式和布局**
6. **测试功能完整性**
7. **优化移动端体验**

这个替换过程需要逐步进行，建议先从一个简单的页面开始，熟悉 uView 的 API 后再进行大规模替换。 