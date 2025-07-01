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
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background: #f0f2f5;
}

.login-card {
  width: 450px;
  padding: 30px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.login-card h2 {
  text-align: center;
  margin-bottom: 25px;
  color: #333;
}

.test-accounts {
  margin-bottom: 20px;
  font-size: 14px;
}

.test-accounts ul {
  padding-left: 20px;
  margin: 10px 0 0;
}

.test-accounts li {
  margin-bottom: 8px;
}

.login-button {
  width: 100%;
  margin-top: 10px;
}

.debug-panel {
  margin-top: 20px;
  font-family: monospace;
  font-size: 12px;
}

.debug-panel pre {
  margin: 0;
  white-space: pre-wrap;
}
</style>