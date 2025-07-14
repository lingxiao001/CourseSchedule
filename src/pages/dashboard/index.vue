<template>
	<view class="mobile-dashboard">
		<!-- 顶部用户信息栏 -->
		<view class="dashboard-header">
			<view class="user-info">
				<view class="user-avatar" :style="{ backgroundColor: avatarColor }">
					{{ userInfo.realName?.charAt(0) }}
				</view>
				<view class="user-details">
					<text class="welcome-text">欢迎回来,</text>
					<text class="user-name">{{ userInfo.realName }}</text>
					<text class="user-role">{{ roleText }}</text>
				</view>
			</view>
		</view>
		
		<!-- 主内容区：根据角色动态渲染 -->
		<view class="dashboard-content">
			<student-dashboard v-if="userInfo.role === 'student'" />
			<teacher-dashboard v-else-if="userInfo.role === 'teacher'" />
			<admin-dashboard v-else-if="userInfo.role === 'admin'" />
		</view>

		<!-- 底部导航栏 -->
		<view class="bottom-nav">
			<view class="nav-item" :class="{ active: currentTab === 'dashboard' }" @click="switchTab('dashboard')">
				<text class="nav-icon">🏠</text>
				<text class="nav-text">首页</text>
			</view>
			<view v-if="userInfo.role !== 'admin'" class="nav-item" :class="{ active: currentTab === 'schedule' }" @click="switchTab('schedule')">
				<text class="nav-icon">📅</text>
				<text class="nav-text">课表</text>
			</view>
			<view class="nav-item" :class="{ active: currentTab === 'profile' }" @click="switchTab('profile')">
				<text class="nav-icon">👤</text>
				<text class="nav-text">我的</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { authApi } from '@/api/auth'
import StudentDashboard from './components/StudentDashboard.vue'
import TeacherDashboard from './components/TeacherDashboard.vue'
import AdminDashboard from './components/AdminDashboard.vue'

const currentTab = ref('dashboard')

// 用户信息
const userInfo = computed(() => {
	const user = authApi.getCurrentUser()
	return user || { realName: '用户', role: 'student' }
})

const roleText = computed(() => {
	const roleMap = {
		student: '学生',
		teacher: '教师', 
		admin: '管理员'
	}
	return roleMap[userInfo.value.role] || '用户'
})

// 根据用户名生成头像颜色
const avatarColor = computed(() => {
	const colors = ['#FF7875', '#FFC069', '#95DE64', '#597EF7', '#AD6800']
	const charCodeSum = userInfo.value.realName?.charCodeAt(0) || 0
	return colors[charCodeSum % colors.length]
})

// 底部导航切换
const switchTab = (tab) => {
	currentTab.value = tab
	
	switch (tab) {
		case 'dashboard':
			// 当前页面，不需要跳转
			break
		case 'schedule':
			uni.navigateTo({ url: '/pages/schedule/index' })
			break
		case 'profile':
			uni.navigateTo({ url: '/pages/profile/index' })
			break
	}
}

// 页面加载时检查认证状态
onMounted(() => {
	// 检查是否已登录
	if (!authApi.isAuthenticated()) {
		uni.reLaunch({
			url: '/pages/auth/login'
		})
		return
	}
	
	console.log('用户信息:', userInfo.value)
})
</script>

<style lang="scss">
.mobile-dashboard {
	display: flex;
	flex-direction: column;
	height: 100vh;
	background: linear-gradient(to top, #fdfbfb 0%, #ebedee 100%);
	overflow: hidden;
}

/* 顶部栏样式 */
.dashboard-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 40px 32px 24px;
	background: linear-gradient(135deg, #f5576c, #f093fb);
	color: #fff;
	flex-shrink: 0;
}

.user-info {
	display: flex;
	align-items: center;
}

.user-avatar {
	width: 88px;
	height: 88px;
	border-radius: 44px;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 32px;
	font-weight: 500;
	color: #fff;
}

.user-details {
	margin-left: 24px;
}

.welcome-text {
	font-size: 26px;
	color: #f0f0f0;
	display: block;
	margin: 0;
}

.user-name {
	font-size: 36px;
	font-weight: 600;
	color: #fff;
	display: block;
	margin: 4px 0 0;
}

.user-role {
	font-size: 22px;
	color: #e0e0e0;
	display: block;
	margin-top: 4px;
}

/* 主内容区 */
.dashboard-content {
	flex: 1;
	padding: 32px;
	overflow-y: auto;
}

/* 底部导航栏 */
.bottom-nav {
	display: flex;
	justify-content: space-around;
	align-items: center;
	height: 120px;
	background-color: #fff;
	box-shadow: 0 -4px 16px rgba(0,0,0,0.05);
}

.nav-item {
	display: flex;
	flex-direction: column;
	align-items: center;
	color: #8492a6;
	font-size: 24px;
	transition: color 0.3s;
	cursor: pointer;
}

.nav-icon {
	font-size: 44px;
	margin-bottom: 6px;
}

.nav-text {
	font-size: 24px;
}

.nav-item.active {
	color: #409eff;
}
</style>