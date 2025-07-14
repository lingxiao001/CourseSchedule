<template>
	<view class="profile-page">
		<view class="profile-header">
			<view class="user-avatar" :style="{ backgroundColor: avatarColor }">
				{{ userInfo.realName?.charAt(0) }}
			</view>
			<view class="user-info">
				<text class="user-name">{{ userInfo.realName }}</text>
				<text class="user-role">{{ roleText }}</text>
				<text class="user-id">ID: {{ userInfo.id }}</text>
			</view>
		</view>

		<view class="profile-content">
			<!-- 个人信息卡片 -->
			<view class="info-card">
				<view class="card-header">
					<text class="card-title">个人信息</text>
					<view class="edit-btn" @click="editProfile">
						<text>编辑</text>
					</view>
				</view>
				<view class="info-list">
					<view class="info-item">
						<text class="info-label">用户名</text>
						<text class="info-value">{{ userInfo.username }}</text>
					</view>
					<view class="info-item">
						<text class="info-label">真实姓名</text>
						<text class="info-value">{{ userInfo.realName }}</text>
					</view>
					<view class="info-item">
						<text class="info-label">角色</text>
						<text class="info-value">{{ roleText }}</text>
					</view>
					<view v-if="userInfo.roleId" class="info-item">
						<text class="info-label">{{ roleIdLabel }}</text>
						<text class="info-value">{{ userInfo.roleId }}</text>
					</view>
				</view>
			</view>

			<!-- 功能菜单 -->
			<view class="menu-card">
				<view class="menu-header">
					<text class="card-title">功能菜单</text>
				</view>
				<view class="menu-list">
					<view class="menu-item" @click="goToSchedule">
						<view class="menu-icon">📅</view>
						<text class="menu-text">课表查看</text>
						<text class="menu-arrow">→</text>
					</view>
					<view v-if="userInfo.role === 'student'" class="menu-item" @click="goToMyCourses">
						<view class="menu-icon">📚</view>
						<text class="menu-text">我的课程</text>
						<text class="menu-arrow">→</text>
					</view>
					<view v-if="userInfo.role === 'teacher'" class="menu-item" @click="goToTeacherCourses">
						<view class="menu-icon">📖</view>
						<text class="menu-text">课程管理</text>
						<text class="menu-arrow">→</text>
					</view>
					<view v-if="userInfo.role === 'admin'" class="menu-item" @click="goToUserManagement">
						<view class="menu-icon">👥</view>
						<text class="menu-text">用户管理</text>
						<text class="menu-arrow">→</text>
					</view>
					<view class="menu-item" @click="showAbout">
						<view class="menu-icon">ℹ️</view>
						<text class="menu-text">关于系统</text>
						<text class="menu-arrow">→</text>
					</view>
				</view>
			</view>

			<!-- 系统设置 -->
			<view class="settings-card">
				<view class="card-header">
					<text class="card-title">系统设置</text>
				</view>
				<view class="settings-list">
					<view class="setting-item" @click="clearCache">
						<view class="setting-icon">🗑️</view>
						<text class="setting-text">清除缓存</text>
					</view>
					<view class="setting-item danger" @click="logout">
						<view class="setting-icon">🚪</view>
						<text class="setting-text">退出登录</text>
					</view>
				</view>
			</view>
		</view>

		<!-- 底部导航 -->
		<view class="bottom-nav">
			<view class="nav-item" @click="goHome">
				<text class="nav-icon">🏠</text>
				<text class="nav-text">首页</text>
			</view>
			<view v-if="userInfo.role !== 'admin'" class="nav-item" @click="goToSchedule">
				<text class="nav-icon">📅</text>
				<text class="nav-text">课表</text>
			</view>
			<view class="nav-item active">
				<text class="nav-icon">👤</text>
				<text class="nav-text">我的</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { authApi } from '@/api/auth'

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

const roleIdLabel = computed(() => {
	const labelMap = {
		student: '学号',
		teacher: '教师编号'
	}
	return labelMap[userInfo.value.role] || 'ID'
})

// 根据用户名生成头像颜色
const avatarColor = computed(() => {
	const colors = ['#FF7875', '#FFC069', '#95DE64', '#597EF7', '#AD6800']
	const charCodeSum = userInfo.value.realName?.charCodeAt(0) || 0
	return colors[charCodeSum % colors.length]
})

// 编辑个人信息
const editProfile = () => {
	uni.showToast({
		title: '功能开发中',
		icon: 'none'
	})
}

// 导航函数
const goHome = () => {
	uni.reLaunch({ url: '/pages/dashboard/index' })
}

const goToSchedule = () => {
	uni.navigateTo({ url: '/pages/schedule/index' })
}

const goToMyCourses = () => {
	uni.navigateTo({ url: '/pages/student/my-courses' })
}

const goToTeacherCourses = () => {
	uni.navigateTo({ url: '/pages/teacher/courses' })
}

const goToUserManagement = () => {
	uni.navigateTo({ url: '/pages/admin/users' })
}

// 显示关于信息
const showAbout = () => {
	uni.showModal({
		title: '关于系统',
		content: 'Course Scheduler v1.0\n智能课程调度管理系统\n\n© 2024 Course Schedule Team',
		showCancel: false,
		confirmText: '确定'
	})
}

// 清除缓存
const clearCache = () => {
	uni.showModal({
		title: '清除缓存',
		content: '确定要清除所有缓存数据吗？',
		success: (res) => {
			if (res.confirm) {
				try {
					uni.clearStorageSync()
					uni.showToast({
						title: '缓存已清除',
						icon: 'success'
					})
					// 清除后重新登录
					setTimeout(() => {
						uni.reLaunch({ url: '/pages/auth/login' })
					}, 1500)
				} catch (error) {
					uni.showToast({
						title: '清除失败',
						icon: 'error'
					})
				}
			}
		}
	})
}

// 退出登录
const logout = () => {
	uni.showModal({
		title: '退出登录',
		content: '确定要退出登录吗？',
		success: (res) => {
			if (res.confirm) {
				authApi.logout()
				uni.showToast({
					title: '已退出登录',
					icon: 'success'
				})
				setTimeout(() => {
					uni.reLaunch({ url: '/pages/auth/login' })
				}, 1000)
			}
		}
	})
}

onMounted(() => {
	// 检查登录状态
	if (!authApi.isAuthenticated()) {
		uni.reLaunch({ url: '/pages/auth/login' })
		return
	}
})
</script>

<style lang="scss" scoped>
.profile-page {
	display: flex;
	flex-direction: column;
	height: 100vh;
	background: #f5f7fa;
}

.profile-header {
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	color: white;
	padding: 60px 32px 40px;
	display: flex;
	align-items: center;
}

.user-avatar {
	width: 120px;
	height: 120px;
	border-radius: 60px;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 48px;
	font-weight: 600;
	color: white;
	margin-right: 32px;
	border: 4px solid rgba(255,255,255,0.3);
}

.user-info {
	flex: 1;
}

.user-name {
	font-size: 48px;
	font-weight: 600;
	display: block;
	margin-bottom: 8px;
}

.user-role {
	font-size: 28px;
	opacity: 0.9;
	display: block;
	margin-bottom: 8px;
}

.user-id {
	font-size: 24px;
	opacity: 0.7;
	display: block;
}

.profile-content {
	flex: 1;
	padding: 32px;
	overflow-y: auto;
}

.info-card, .menu-card, .settings-card {
	background: white;
	border-radius: 16px;
	margin-bottom: 24px;
	box-shadow: 0 4px 20px rgba(0,0,0,0.08);
	overflow: hidden;
}

.card-header, .menu-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 32px;
	background: #f5f7fa;
	border-bottom: 1px solid #ebeef5;
}

.card-title {
	font-size: 32px;
	font-weight: 600;
	color: #303133;
}

.edit-btn {
	background: #409eff;
	color: white;
	padding: 12px 20px;
	border-radius: 8px;
	font-size: 24px;
}

.info-list, .menu-list, .settings-list {
	padding: 32px;
}

.info-item {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 20px 0;
	border-bottom: 1px solid #f5f7fa;
}

.info-item:last-child {
	border-bottom: none;
}

.info-label {
	font-size: 28px;
	color: #909399;
	width: 140px;
}

.info-value {
	flex: 1;
	font-size: 28px;
	color: #303133;
	text-align: right;
}

.menu-item, .setting-item {
	display: flex;
	align-items: center;
	padding: 24px 0;
	border-bottom: 1px solid #f5f7fa;
	transition: background-color 0.3s;
}

.menu-item:last-child, .setting-item:last-child {
	border-bottom: none;
}

.menu-item:active {
	background-color: #f5f7fa;
}

.menu-icon, .setting-icon {
	font-size: 36px;
	margin-right: 24px;
	width: 48px;
	text-align: center;
}

.menu-text, .setting-text {
	flex: 1;
	font-size: 28px;
	color: #303133;
}

.menu-arrow {
	font-size: 24px;
	color: #c0c4cc;
}

.setting-item.danger .setting-text {
	color: #f56c6c;
}

.setting-item.danger .setting-icon {
	color: #f56c6c;
}

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