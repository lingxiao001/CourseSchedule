<template>
  <view class="login-container">
    <view class="welcome-section">
      <text1 class="app-title">Course Scheduler</text1>
      <text class="app-subtitle">开启你的学习之旅</text>
    </view>

    <!-- 登录表单 -->
    <u-form
      v-if="!isRegister"
      ref="loginForm"
      :model="loginFormData"
      :rules="loginRules"
      class="login-form"
      @submit.prevent="handleLogin"
    >
      <u-form-item prop="username">
        <u-input v-model="loginFormData.username" placeholder="账号" :prefix-icon="User" />
      </u-form-item>

      <u-form-item prop="password">
        <u-input v-model="loginFormData.password" type="password" placeholder="密码" :prefix-icon="Lock" show-password />
      </u-form-item>

      <u-form-item>
        <u-button type="primary" native-type="submit" :loading="loading" class="login-button">立即进入</u-button>
      </u-form-item>

      <view class="switch-link">
        还没有账号？ <a href="#" @click.prevent="isRegister = true">立即注册</a>
      </view>
    </u-form>

    <!-- 注册表单 -->
    <u-form
      v-else
      ref="registerForm"
      :model="registerFormData"
      :rules="registerRules"
      class="login-form"
      @submit.prevent="handleRegister"
    >
      <u-form-item prop="username">
        <u-input v-model="registerFormData.username" placeholder="账号" :prefix-icon="User" />
      </u-form-item>

      <u-form-item prop="password">
        <u-input v-model="registerFormData.password" type="password" placeholder="密码" :prefix-icon="Lock" show-password />
      </u-form-item>

      <u-form-item prop="realName">
        <u-input v-model="registerFormData.realName" placeholder="真实姓名" />
      </u-form-item>

      <u-form-item prop="role">
        <u-select v-model="registerFormData.role" placeholder="选择角色">
          <u-option label="学生" value="student" />
          <u-option label="教师" value="teacher" />
        </u-select>
      </u-form-item>

      <!-- 学生专属 -->
      <template v-if="registerFormData.role === 'student'">
        <u-form-item prop="studentId">
          <u-input v-model="registerFormData.studentId" placeholder="学号" />
        </u-form-item>
        <u-form-item prop="grade">
          <u-input v-model="registerFormData.grade" placeholder="年级" />
        </u-form-item>
        <u-form-item prop="className">
          <u-input v-model="registerFormData.className" placeholder="班级" />
        </u-form-item>
      </template>

      <!-- 教师专属 -->
      <template v-if="registerFormData.role === 'teacher'">
        <u-form-item prop="teacherId">
          <u-input v-model="registerFormData.teacherId" placeholder="教师ID" />
        </u-form-item>
        <u-form-item prop="title">
          <u-input v-model="registerFormData.title" placeholder="职称" />
        </u-form-item>
        <u-form-item prop="department">
          <u-input v-model="registerFormData.department" placeholder="部门" />
        </u-form-item>
      </template>

      <u-form-item>
        <u-button type="primary" native-type="submit" :loading="loading" class="login-button">完成注册</u-button>
      </u-form-item>

      <view class="switch-link">
        已有账号？ <a href="#" @click.prevent="isRegister = false">立即登录</a>
      </view>
    </u-form>

    <view class="footer-section">
      <a href="#">忘记密码?</a>
    </view>
  </view>
</template>

<script setup>

// 全局 uni 对象定义
const uni = {
  showToast: (options) => {
    if (options.icon === 'success') {
      alert('✅ ' + options.title);
    } else if (options.icon === 'error') {
      alert('❌ ' + options.title);
    } else {
      alert(options.title);
    }
  },
  showModal: (options) => {
    const result = confirm(options.content || options.title);
    if (options.success) {
      options.success({ confirm: result });
    }
  },
  navigateTo: (options) => {
    window.location.href = options.url;
  },
  navigateBack: () => {
    window.history.back();
  },
  redirectTo: (options) => {
    window.location.replace(options.url);
  },
  reLaunch: (options) => {
    window.location.href = options.url;
  }
};






import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
const router = useRouter()
const authStore = useAuthStore()

// 表单切换
const isRegister = ref(false)

// 登录相关
const loginFormData = ref({ username: 'admin', password: '123456' })
const loginRules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}
const loginForm = ref(null)

// 注册相关
const registerFormData = ref({
  username: '',
  password: '',
  realName: '',
  role: '',
  // student
  studentId: null,
  grade: '',
  className: '',
  // teacher
  teacherId: null,
  title: '',
  department: ''
})

const registerRules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  // 角色专属字段在提交阶段动态校验
}
const registerForm = ref(null)

const loading = ref(false)

const handleLogin = async () => {
  try {
    await loginForm.value.validate()
    loading.value = true
    await authStore.login(loginFormData.value)
    // 登录成功，跳转主页
    await router.push('/')
    uni.showToast({ title: '登录成功', icon: 'success' })
  } catch (error) {
    const msg = error.parsedMessage || error.response?.data?.detail || error.response?.data?.message || '登录失败'
    uni.showToast({ title: msg, icon: 'error' })
  } finally {
    loading.value = false
  }
}

const handleRegister = async () => {
  try {
    await registerForm.value.validate()
    // 根据角色去除无关字段，防止后端校验失败
    const payload = { ...registerFormData.value }
    if (payload.role === 'student') {
      delete payload.teacherId
      delete payload.title
      delete payload.department
    } else if (payload.role === 'teacher') {
      delete payload.studentId
      delete payload.grade
      delete payload.className
    }

    loading.value = true
    await authStore.register(payload)
    await router.push('/')
    uni.showToast({ title: '注册并登录成功', icon: 'success' })
  } catch (error) {
    const msg = error.parsedMessage || error.response?.data?.detail || error.response?.data?.message || '注册失败'
    uni.showToast({ title: msg, icon: 'error' })
  } finally {
    loading.value = false
  }
}

// 切换角色时重置特定字段
watch(
  () => registerFormData.value.role,
  newVal => {
    if (newVal === 'student') {
      registerFormData.value.teacherId = null
      registerFormData.value.title = ''
      registerFormData.value.department = ''
    } else if (newVal === 'teacher') {
      registerFormData.value.studentId = null
      registerFormData.value.grade = ''
      registerFormData.value.className = ''
    }
  }
)
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
  overflow-y: auto; /* 允许纵向滚动，解决移动端内容被截断 */
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

/* 表单切换链接 */
.switch-link {
  text-align: center;
  margin-top: 1rem;
  color: #fff;
  font-size: 1.4rem;
}

.switch-link a {
  color: #fff;
  text-decoration: underline;
  font-weight: 600;
}
</style>