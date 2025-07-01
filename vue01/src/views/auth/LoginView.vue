<template>
  <div class="login-container">
    
    <div class="welcome-section">
      <h1 class="app-title">Course Scheduler</h1>
      <p class="app-subtitle">开启你的学习之旅</p>
    </div>
    
    <el-form
      ref="loginForm"
      :model="form"
      :rules="rules"
      class="login-form"
      @submit.prevent="handleLogin"
    >
      <el-form-item prop="username">
        <el-input
          v-model="form.username"
          placeholder="账号"
          :prefix-icon="User"
        />
      </el-form-item>

      <el-form-item prop="password">
        <el-input
          v-model="form.password"
          type="password"
          placeholder="密码"
          :prefix-icon="Lock"
          show-password
        />
      </el-form-item>

      <el-form-item>
        <el-button
          type="primary"
          native-type="submit"
          :loading="loading"
          class="login-button"
        >
          立即进入
        </el-button>
      </el-form-item>
    </el-form>

    <div class="footer-section">
      <a href="#">忘记密码?</a>
    </div>

  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { ElMessage } from 'element-plus'
import { authApi } from '@/api/auth'
import { User, Lock } from '@element-plus/icons-vue'

const router = useRouter()
const authStore = useAuthStore()
const form = ref({ username: 's001', password: '123456' })
const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}
const loading = ref(false)
const loginForm = ref(null)

const handleLogin = async () => {
  try {
    await loginForm.value.validate()
    loading.value = true
    const response = await authApi.login(form.value)
    
    authStore.user = response.user
    authStore.token = response.token
    authStore.isAuthenticated = true
    
    const redirectMap = {
      admin: 'dashboard',
      teacher: 'dashboard',
      student: 'dashboard'
    }
    const targetPath = redirectMap[response.user.role] || '/'
    await router.push(targetPath)
    ElMessage.success('登录成功')
    
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
/* 一体化沉浸式设计 */
.login-container {
  display: flex;
  flex-direction: column;
  justify-content: space-between;
  height: 100vh;
  padding: 6rem 3.5rem 4rem;
  background: linear-gradient(160deg, #8A2387, #E94057, #F27121);
  overflow: hidden;
}

.welcome-section {
  text-align: center;
  color: #fff;
}

.app-title {
  font-size: 3.6rem;
  font-weight: 700;
  letter-spacing: 0.1rem;
  margin: 0;
}

.app-subtitle {
  font-size: 1.6rem;
  font-weight: 300;
  opacity: 0.8;
  margin-top: 0.5rem;
}

.login-form {
  width: 100%;
}

/* 深度定制无框输入框 */
:deep(.el-form-item) {
  margin-bottom: 2.5rem;
}

:deep(.el-input__wrapper) {
  background: transparent !important;
  box-shadow: none !important;
  border-bottom: 2px solid rgba(255, 255, 255, 0.3) !important;
  border-radius: 0 !important;
  padding: 0.5rem 0 !important;
  height: 4.8rem;
  transition: border-color 0.3s ease;
}

:deep(.el-input__wrapper.is-focus) {
  border-bottom-color: #fff !important;
}

:deep(.el-input__inner) {
  color: #fff !important;
  font-size: 1.8rem;
  font-weight: 500;
}

:deep(.el-input__inner::placeholder) {
  color: rgba(255, 255, 255, 0.5) !important;
  font-weight: 300;
}

:deep(.el-input__icon) {
  color: rgba(255, 255, 255, 0.8) !important;
  font-size: 2rem;
}

/* 登录按钮 */
.login-button {
  width: 100%;
  height: 5.2rem;
  font-size: 1.8rem;
  font-weight: 600;
  border-radius: 2.6rem; /* 全圆角 */
  border: none;
  background-color: #fff;
  color: #E94057; /* 呼应背景色 */
  margin-top: 1.5rem;
  box-shadow: 0 4px 15px rgba(0,0,0,0.2);
  transition: all 0.3s ease;
}

.login-button:hover {
  transform: translateY(-3px);
  box-shadow: 0 7px 20px rgba(0,0,0,0.25);
}

.footer-section {
  text-align: center;
}

.footer-section a {
  font-size: 1.4rem;
  color: rgba(255, 255, 255, 0.8);
  text-decoration: none;
  font-weight: 400;
}
</style>