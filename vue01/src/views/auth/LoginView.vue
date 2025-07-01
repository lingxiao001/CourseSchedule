<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2>课程管理系统</h2>
      
      <el-form
        ref="loginForm"
        :model="form"
        :rules="rules"
        @submit.prevent="handleLogin"
      >
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="用户名"
            prefix-icon="User"
            size="large"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="密码"
            prefix-icon="Lock"
            size="large"
            show-password
          />
        </el-form-item>

        <el-button
          type="primary"
          size="large"
          native-type="submit"
          :loading="loading"
          class="login-button"
        >
          登录
        </el-button>
      </el-form>
      
      <!-- 调试信息展示 -->
      <el-collapse v-if="debugInfo" class="debug-panel">
        <el-collapse-item title="调试信息">
          <pre>{{ debugInfo }}</pre>
        </el-collapse-item>
      </el-collapse>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import { authApi } from '@/api/auth'

const router = useRouter()
const authStore = useAuthStore()

const form = ref({
  username: 's001', // 默认填充测试账号
  password: '123456'
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const loading = ref(false)
const loginForm = ref()
const debugInfo = ref(null)

const handleLogin = async () => {
  try {
    await loginForm.value.validate()
    loading.value = true
    debugInfo.value = null
    
    console.group('登录过程调试')
    console.log('提交数据:', JSON.stringify(form.value))
    
    // 调用更新后的API方法
    const startTime = Date.now()
    const response = await authApi.login(form.value)
    const duration = Date.now() - startTime
    
    console.log('登录响应:', response)
    console.log(`请求耗时: ${duration}ms`)
    
    // 存储完整的响应数据用于调试
    debugInfo.value = {
      duration: `${duration}ms`,
      response: response.rawData
    }
    
    // 更新Pinia状态
    authStore.user = response.user
    authStore.token = response.token
    authStore.isAuthenticated = true
    
    // 进入根据角色跳转逻辑
    const redirectMap = {
      admin: 'dashboard',
      teacher: 'dashboard',
      student: 'dashboard'
    }
    
    const targetPath = redirectMap[response.user.role] || '/'
    console.log('跳转路径:', targetPath)
    
    await router.push(targetPath)
    ElMessage.success('登录成功')
    
    console.groupEnd()
  } catch (error) {
    console.error('登录错误详情:', error)
    let errorMessage = '登录失败'
    
    if (error.response) {
      errorMessage = error.response.data?.message || errorMessage
      debugInfo.value = {
        error: error.response.data
      }
    } else if (error.request) {
      errorMessage = '后端服务无响应'
      debugInfo.value = {
        error: '请求未到达服务器'
      }
    }
    
    ElMessage.error(errorMessage)
    console.groupEnd()
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* 全新的移动端优先设计 */

.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  /* 优雅的渐变背景 */
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 2rem; /* 在小屏幕上留出边距 */
}

.login-card {
  width: 100%;
  max-width: 42rem;
  
  /* 玻璃拟态效果 */
  background: rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  
  padding: 4rem 3rem;
  border-radius: 1.6rem;
  box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.37);
  
  color: white; /* 调整文字颜色以适应深色背景 */
  transition: all 0.3s ease;
}

.login-card:hover {
  box-shadow: 0 8px 40px 0 rgba(31, 38, 135, 0.5);
}

.login-card h2 {
  text-align: center;
  margin-bottom: 3rem;
  font-size: 2.8rem;
  font-weight: 600;
  letter-spacing: 0.1rem;
  color: #fff;
}

/* 对Element Plus组件样式进行深度定制 */
:deep(.el-form-item__content) {
  width: 100%;
}

:deep(.el-input__wrapper) {
  background-color: rgba(255, 255, 255, 0.2) !important;
  box-shadow: none !important;
  border-radius: 0.8rem !important;
  padding: 0.5rem 1.5rem !important;
  height: 4.8rem;
}

:deep(.el-input__inner) {
  color: #fff !important;
  font-size: 1.6rem;
}

:deep(.el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.7) !important;
}

:deep(.el-input__icon) {
  color: #fff !important;
  font-size: 1.8rem;
}

.login-button {
  width: 100%;
  margin-top: 2rem;
  font-size: 1.8rem;
  padding: 1.5rem;
  height: auto;
  border: none;
  border-radius: 0.8rem;
  background: linear-gradient(135deg, #89f7fe 0%, #66a6ff 100%);
  color: #fff;
  font-weight: 600;
  transition: all 0.3s ease;
  box-shadow: 0 4px 15px 0 rgba(102, 166, 255, 0.75);
}

.login-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px 0 rgba(102, 166, 255, 0.85);
}

.login-button:active {
  transform: translateY(0);
}

/* 移除调试面板的默认样式，使其更融入新设计 */
.debug-panel {
  margin-top: 3rem;
  background: rgba(0, 0, 0, 0.2);
  border-radius: 0.8rem;
  border: none;
}
:deep(.el-collapse-item__header),
:deep(.el-collapse-item__wrap) {
  background: transparent !important;
  color: #fff !important;
  border: none !important;
}
:deep(.el-collapse-item__content) {
  font-size: 1.2rem;
  color: #eee;
}
</style>