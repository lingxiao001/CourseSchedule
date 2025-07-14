<template>
	<view class="login-container">
		<!-- 欢迎标题区域 -->
		<view class="welcome-section">
			<text class="app-title">Course Scheduler</text>
			<text class="app-subtitle">开启你的学习之旅</text>
		</view>

		<!-- 登录表单 -->
		<view v-if="!isRegister" class="login-form">
			<view class="form-item">
				<text class="input-icon">👤</text>
				<input 
					v-model="loginFormData.username" 
					placeholder="账号" 
					class="custom-input"
					:disabled="loading"
				/>
			</view>
			
			<view class="form-item">
				<text class="input-icon">🔒</text>
				<input 
					v-model="loginFormData.password" 
					type="password" 
					placeholder="密码" 
					class="custom-input"
					:disabled="loading"
				/>
			</view>
			
			<button 
				class="login-button"
				:disabled="loading || !canLogin"
				@click="handleLogin"
			>
				{{ loading ? '登录中...' : '立即进入' }}
			</button>

			<view class="switch-link">
				<text>还没有账号？</text>
				<text class="link-text" @click="isRegister = true">立即注册</text>
			</view>
		</view>

		<!-- 注册表单 -->
		<view v-else class="login-form">
			<view class="form-item">
				<text class="input-icon">👤</text>
				<input 
					v-model="registerFormData.username" 
					placeholder="账号" 
					class="custom-input"
					:disabled="loading"
				/>
			</view>
			
			<view class="form-item">
				<text class="input-icon">🔒</text>
				<input 
					v-model="registerFormData.password" 
					type="password" 
					placeholder="密码" 
					class="custom-input"
					:disabled="loading"
				/>
			</view>

			<view class="form-item">
				<text class="input-icon">📝</text>
				<input 
					v-model="registerFormData.realName" 
					placeholder="真实姓名" 
					class="custom-input"
					:disabled="loading"
				/>
			</view>

			<view class="form-item">
				<text class="input-icon">⭐</text>
				<picker 
					mode="selector" 
					:range="roleOptions" 
					range-key="label"
					@change="onRoleChange"
					class="role-picker"
				>
					<view class="custom-input picker-text">
						{{ selectedRole.label || '选择角色' }}
					</view>
				</picker>
			</view>

			<!-- 学生专属字段 -->
			<template v-if="registerFormData.role === 'student'">
				<view class="form-item">
					<text class="input-icon">🎓</text>
					<input 
						v-model="registerFormData.studentId" 
						placeholder="学号" 
						type="number"
						class="custom-input"
						:disabled="loading"
					/>
				</view>
				<view class="form-item">
					<text class="input-icon">📚</text>
					<input 
						v-model="registerFormData.grade" 
						placeholder="年级" 
						class="custom-input"
						:disabled="loading"
					/>
				</view>
				<view class="form-item">
					<text class="input-icon">🏛️</text>
					<input 
						v-model="registerFormData.className" 
						placeholder="班级" 
						class="custom-input"
						:disabled="loading"
					/>
				</view>
			</template>

			<!-- 教师专属字段 -->
			<template v-if="registerFormData.role === 'teacher'">
				<view class="form-item">
					<text class="input-icon">🏫</text>
					<input 
						v-model="registerFormData.teacherId" 
						placeholder="教师ID" 
						type="number"
						class="custom-input"
						:disabled="loading"
					/>
				</view>
				<view class="form-item">
					<text class="input-icon">👔</text>
					<input 
						v-model="registerFormData.title" 
						placeholder="职称" 
						class="custom-input"
						:disabled="loading"
					/>
				</view>
				<view class="form-item">
					<text class="input-icon">🏢</text>
					<input 
						v-model="registerFormData.department" 
						placeholder="部门" 
						class="custom-input"
						:disabled="loading"
					/>
				</view>
			</template>
			
			<button 
				class="login-button"
				:disabled="loading || !canRegister"
				@click="handleRegister"
			>
				{{ loading ? '注册中...' : '完成注册' }}
			</button>

			<view class="switch-link">
				<text>已有账号？</text>
				<text class="link-text" @click="isRegister = false">立即登录</text>
			</view>
		</view>

		<!-- 底部链接 -->
		<view class="footer-section">
			<text class="footer-link">忘记密码？</text>
			<text class="footer-link">联系管理员</text>
		</view>
	</view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { authApi } from '@/api/auth'

// 响应式数据
const isRegister = ref(false)
const loading = ref(false)

// 登录表单数据
const loginFormData = ref({
	username: '',
	password: ''
})

// 注册表单数据
const registerFormData = ref({
	username: '',
	password: '',
	realName: '',
	role: '',
	studentId: null,
	teacherId: null,
	grade: '',
	className: '',
	title: '',
	department: ''
})

// 角色选项
const roleOptions = [
	{ label: '学生', value: 'student' },
	{ label: '教师', value: 'teacher' }
]

// 计算属性
const canLogin = computed(() => {
	return loginFormData.value.username.trim() && loginFormData.value.password.trim()
})

const canRegister = computed(() => {
	const data = registerFormData.value
	return data.username.trim() && data.password.trim() && data.realName.trim() && data.role
})

const selectedRole = computed(() => {
	return roleOptions.find(item => item.value === registerFormData.value.role) || {}
})

// 实际登录方法
const handleLogin = async () => {
	if (loading.value || !canLogin.value) return
	
	loading.value = true
	
	try {
		console.log('开始登录，用户名:', loginFormData.value.username)
		
		// 调用后端登录API
		const authResponse = await authApi.login({
			username: loginFormData.value.username,
			password: loginFormData.value.password
		})
		
		console.log('登录成功:', authResponse.user)
		
		// 登录成功，跳转到首页
		uni.reLaunch({
			url: '/pages/dashboard/index'
		})
		
		uni.showToast({
			title: '登录成功',
			icon: 'success'
		})
	} catch (error) {
		console.error('登录失败:', error)
		const errorMessage = error.parsedMessage || error.message || '登录失败'
		uni.showToast({
			title: errorMessage,
			icon: 'none',
			duration: 3000
		})
	} finally {
		loading.value = false
	}
}

// 实际注册方法
const handleRegister = async () => {
	if (loading.value || !canRegister.value) return
	
	loading.value = true
	
	try {
		console.log('开始注册，用户名:', registerFormData.value.username)
		
		// 构建注册数据
		const registerPayload = {
			username: registerFormData.value.username,
			password: registerFormData.value.password,
			realName: registerFormData.value.realName,
			role: registerFormData.value.role,
			...(registerFormData.value.role === 'student' && { 
				studentId: registerFormData.value.studentId ? Number(registerFormData.value.studentId) : null,
				grade: registerFormData.value.grade,
				className: registerFormData.value.className
			}),
			...(registerFormData.value.role === 'teacher' && { 
				teacherId: registerFormData.value.teacherId ? Number(registerFormData.value.teacherId) : null,
				title: registerFormData.value.title,
				department: registerFormData.value.department
			})
		}
		
		// 调用后端注册API
		const authResponse = await authApi.register(registerPayload)
		
		console.log('注册成功:', authResponse.user)
		
		// 注册成功，跳转到首页
		uni.reLaunch({
			url: '/pages/dashboard/index'
		})
		
		uni.showToast({
			title: '注册成功',
			icon: 'success'
		})
	} catch (error) {
		console.error('注册失败:', error)
		const errorMessage = error.parsedMessage || error.message || '注册失败'
		uni.showToast({
			title: errorMessage,
			icon: 'none',
			duration: 3000
		})
	} finally {
		loading.value = false
	}
}

const onRoleChange = (e) => {
	const index = e.detail.value
	const selected = roleOptions[index]
	registerFormData.value.role = selected.value
	
	// 清空相关字段
	if (selected.value === 'student') {
		registerFormData.value.teacherId = null
		registerFormData.value.title = ''
		registerFormData.value.department = ''
	} else if (selected.value === 'teacher') {
		registerFormData.value.studentId = null
		registerFormData.value.grade = ''
		registerFormData.value.className = ''
	}
}

// 页面加载时检查是否已登录
onMounted(() => {
	try {
		// 检查登录状态
		if (authApi.isAuthenticated()) {
			console.log('检测到已登录用户，跳转到首页')
			uni.reLaunch({
				url: '/pages/dashboard/index'
			})
			return
		}
		
		console.log('登录页面加载完成 - 实际API版本')
	} catch (error) {
		console.error('初始化认证状态失败:', error)
	}
})
</script>

<style lang="scss">
/* 一体化沉浸式设计 */
.login-container {
	display: flex;
	flex-direction: column;
	justify-content: space-between;
	height: 100vh;
	padding: 60px 40px 40px 40px;
	background: linear-gradient(160deg, #8A2387, #E94057, #F27121);
	position: relative;
	overflow: hidden;
}

/* 欢迎区域 */
.welcome-section {
	flex: 1;
	display: flex;
	flex-direction: column;
	justify-content: center;
	align-items: center;
	text-align: center;
}

.app-title {
	font-size: 36px;
	font-weight: 700;
	color: #fff;
	margin-bottom: 10px;
	letter-spacing: 2px;
	text-shadow: 0 4px 8px rgba(0,0,0,0.3);
}

.app-subtitle {
	font-size: 16px;
	color: rgba(255, 255, 255, 0.9);
	font-weight: 300;
	letter-spacing: 1px;
}

/* 登录表单 */
.login-form {
	flex: 1;
	display: flex;
	flex-direction: column;
	justify-content: center;
	max-width: 100%;
	margin: 0 auto;
}

.form-item {
	position: relative;
	margin-bottom: 20px;
	display: flex;
	align-items: center;
}

.input-icon {
	position: absolute;
	left: 0;
	top: 50%;
	transform: translateY(-50%);
	z-index: 10;
	font-size: 20px;
	line-height: 1;
}

.custom-input {
	width: 100%;
	background: transparent;
	border: none;
	border-bottom: 2px solid rgba(255, 255, 255, 0.3);
	color: #fff;
	font-size: 18px;
	font-weight: 500;
	padding: 5px 0 5px 35px;
	height: 48px;
	box-sizing: border-box;
	transition: border-color 0.3s ease;
	
	&::placeholder {
		color: rgba(255, 255, 255, 0.5);
		font-weight: 300;
	}
	
	&:focus {
		outline: none;
		border-bottom-color: #fff;
	}
	
	&:disabled {
		opacity: 0.6;
	}
}

/* picker文本样式 */
.picker-text {
	cursor: pointer;
	line-height: 48px;
	padding-top: 0;
	padding-bottom: 0;
}

.role-picker {
	width: 100%;
}

/* 登录按钮 */
.login-button {
	width: 100%;
	height: 52px;
	font-size: 18px;
	font-weight: 600;
	border-radius: 26px; /* 全圆角 */
	border: none;
	background-color: #fff;
	color: #E94057; /* 呼应背景色 */
	margin-top: 15px;
	box-shadow: 0 4px 15px rgba(0,0,0,0.2);
	transition: all 0.3s ease;
	cursor: pointer;
	
	&:hover:not(:disabled) {
		transform: translateY(-3px);
		box-shadow: 0 7px 20px rgba(0,0,0,0.25);
	}
	
	&:disabled {
		opacity: 0.6;
		cursor: not-allowed;
		transform: none;
	}
}

/* 底部区域 */
.footer-section {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-top: 20px;
}

.footer-link {
	color: rgba(255, 255, 255, 0.8);
	font-size: 14px;
	font-weight: 400;
	cursor: pointer;
}

/* 表单切换链接 */
.switch-link {
	text-align: center;
	margin-top: 10px;
	color: #fff;
	font-size: 14px;
}

.link-text {
	color: #fff;
	text-decoration: underline;
	font-weight: 600;
	cursor: pointer;
	margin-left: 5px;
}
</style>