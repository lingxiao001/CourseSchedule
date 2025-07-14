<template>
	<view class="admin-dashboard">
		<view class="dashboard-title">管理员后台</view>

		<!-- 系统概览 -->
		<view class="dashboard-row">
			<view class="overview-stats">
				<view class="overview-card users">
					<view class="stat-icon">👥</view>
					<view class="stat-content">
						<text class="stat-number">{{ systemStats.totalUsers }}</text>
						<text class="stat-label">系统用户</text>
					</view>
				</view>
				<view class="overview-card courses">
					<view class="stat-icon">📚</view>
					<view class="stat-content">
						<text class="stat-number">{{ systemStats.totalCourses }}</text>
						<text class="stat-label">总课程数</text>
					</view>
				</view>
			</view>
		</view>

		<!-- 用户统计 -->
		<view class="dashboard-row">
			<view class="card user-stats">
				<view class="card-header">
					<text class="card-title">用户统计</text>
				</view>
				<view class="user-breakdown">
					<view class="user-type">
						<view class="type-indicator student"></view>
						<text class="type-label">学生</text>
						<text class="type-count">{{ systemStats.studentCount }}</text>
					</view>
					<view class="user-type">
						<view class="type-indicator teacher"></view>
						<text class="type-label">教师</text>
						<text class="type-count">{{ systemStats.teacherCount }}</text>
					</view>
					<view class="user-type">
						<view class="type-indicator admin"></view>
						<text class="type-label">管理员</text>
						<text class="type-count">{{ systemStats.adminCount }}</text>
					</view>
				</view>
			</view>
		</view>

		<!-- 管理功能 -->
		<view class="dashboard-row">
			<view class="management-grid">
				<view class="mgmt-card" @click="navigateTo('/pages/admin/users')">
					<view class="mgmt-icon">👤</view>
					<text class="mgmt-title">用户管理</text>
					<text class="mgmt-desc">管理系统用户</text>
				</view>
				<view class="mgmt-card" @click="navigateTo('/pages/admin/courses')">
					<view class="mgmt-icon">📖</view>
					<text class="mgmt-title">课程管理</text>
					<text class="mgmt-desc">管理课程信息</text>
				</view>
				<view class="mgmt-card" @click="navigateTo('/pages/admin/auto-schedule')">
					<view class="mgmt-icon">🤖</view>
					<text class="mgmt-title">自动排课</text>
					<text class="mgmt-desc">智能课程安排</text>
				</view>
				<view class="mgmt-card" @click="navigateTo('/pages/admin/stats')">
					<view class="mgmt-icon">📊</view>
					<text class="mgmt-title">数据统计</text>
					<text class="mgmt-desc">系统数据分析</text>
				</view>
			</view>
		</view>

		<!-- 最近活动 -->
		<view class="dashboard-row">
			<view class="card recent-activity">
				<view class="card-header">
					<text class="card-title">最近活动</text>
					<view class="card-action" @click="viewAllActivities">
						<text class="action-text">查看全部</text>
					</view>
				</view>
				<view v-if="loadingActivities" class="loading-state">
					<text>加载中...</text>
				</view>
				<view v-else-if="recentActivities.length === 0" class="empty-state">
					<text class="empty-text">暂无活动记录</text>
				</view>
				<view v-else class="activity-list">
					<view v-for="activity in recentActivities.slice(0, 5)" :key="activity.id" class="activity-item">
						<view class="activity-icon" :class="activity.type">
							{{ getActivityIcon(activity.type) }}
						</view>
						<view class="activity-content">
							<text class="activity-title">{{ activity.title }}</text>
							<text class="activity-time">{{ activity.time }}</text>
						</view>
					</view>
				</view>
			</view>
		</view>

		<!-- 系统状态 -->
		<view class="dashboard-row">
			<view class="system-status">
				<view class="status-item">
					<view class="status-dot active"></view>
					<text class="status-text">系统运行正常</text>
				</view>
				<view class="status-item">
					<view class="status-dot active"></view>
					<text class="status-text">数据库连接正常</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const loadingActivities = ref(true)
const systemStats = ref({
	totalUsers: 0,
	totalCourses: 0,
	studentCount: 0,
	teacherCount: 0,
	adminCount: 0
})
const recentActivities = ref([])

// 导航到指定页面
const navigateTo = (url) => {
	uni.navigateTo({ url })
}

// 查看所有活动
const viewAllActivities = () => {
	uni.showToast({
		title: '功能开发中',
		icon: 'none'
	})
}

// 获取活动图标
const getActivityIcon = (type) => {
	const icons = {
		user: '👤',
		course: '📚',
		schedule: '📅',
		system: '⚙️'
	}
	return icons[type] || '📝'
}

// 加载管理员数据
const loadAdminData = async () => {
	loadingActivities.value = true
	try {
		// 模拟数据，实际应该调用API
		setTimeout(() => {
			systemStats.value = {
				totalUsers: 1247,
				totalCourses: 156,
				studentCount: 1089,
				teacherCount: 145,
				adminCount: 13
			}
			
			recentActivities.value = [
				{
					id: 1,
					type: 'user',
					title: '新用户注册：张三',
					time: '2小时前'
				},
				{
					id: 2,
					type: 'course',
					title: '课程更新：计算机科学导论',
					time: '4小时前'
				},
				{
					id: 3,
					type: 'schedule',
					title: '自动排课完成',
					time: '6小时前'
				},
				{
					id: 4,
					type: 'user',
					title: '教师信息更新：李教授',
					time: '8小时前'
				},
				{
					id: 5,
					type: 'system',
					title: '系统备份完成',
					time: '12小时前'
				}
			]
			
			loadingActivities.value = false
		}, 600)
	} catch (error) {
		console.error('加载管理员数据失败:', error)
		loadingActivities.value = false
	}
}

onMounted(() => {
	loadAdminData()
})
</script>

<style lang="scss" scoped>
.admin-dashboard {
	padding: 0;
}

.dashboard-title {
	color: #303133;
	margin-bottom: 32px;
	font-size: 48px;
	font-weight: 500;
	display: flex;
	align-items: center;
}

.dashboard-title::before {
	content: "";
	display: inline-block;
	width: 8px;
	height: 40px;
	background-color: #f56c6c;
	margin-right: 20px;
	border-radius: 4px;
}

.dashboard-row {
	margin-bottom: 32px;
}

.overview-stats {
	display: grid;
	grid-template-columns: repeat(2, 1fr);
	gap: 16px;
}

.overview-card {
	background: white;
	border-radius: 16px;
	padding: 32px;
	display: flex;
	align-items: center;
	box-shadow: 0 4px 20px rgba(0,0,0,0.08);
}

.overview-card.users {
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
	color: white;
}

.overview-card.courses {
	background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
	color: white;
}

.stat-icon {
	font-size: 48px;
	margin-right: 24px;
}

.stat-content {
	flex: 1;
}

.stat-number {
	font-size: 48px;
	font-weight: 600;
	display: block;
	margin-bottom: 8px;
}

.stat-label {
	font-size: 28px;
	opacity: 0.9;
}

.card {
	background: white;
	border-radius: 16px;
	box-shadow: 0 4px 20px rgba(0,0,0,0.08);
	overflow: hidden;
}

.card-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 32px;
	background-color: #f5f7fa;
	border-bottom: 1px solid #ebeef5;
}

.card-title {
	font-size: 32px;
	font-weight: 500;
	color: #303133;
}

.card-action {
	background: #f56c6c;
	color: white;
	padding: 16px 24px;
	border-radius: 8px;
	font-size: 28px;
}

.action-text {
	color: white;
	font-size: 28px;
}

.user-breakdown {
	padding: 32px;
}

.user-type {
	display: flex;
	align-items: center;
	padding: 20px 0;
	border-bottom: 1px solid #f5f7fa;
}

.user-type:last-child {
	border-bottom: none;
}

.type-indicator {
	width: 16px;
	height: 16px;
	border-radius: 50%;
	margin-right: 16px;
}

.type-indicator.student {
	background-color: #409eff;
}

.type-indicator.teacher {
	background-color: #e6a23c;
}

.type-indicator.admin {
	background-color: #f56c6c;
}

.type-label {
	flex: 1;
	font-size: 28px;
	color: #303133;
}

.type-count {
	font-size: 32px;
	font-weight: 600;
	color: #606266;
}

.management-grid {
	display: grid;
	grid-template-columns: repeat(2, 1fr);
	gap: 16px;
}

.mgmt-card {
	background: white;
	border-radius: 16px;
	padding: 32px;
	text-align: center;
	box-shadow: 0 4px 20px rgba(0,0,0,0.08);
	transition: all 0.3s;
}

.mgmt-card:active {
	transform: scale(0.98);
}

.mgmt-icon {
	font-size: 48px;
	display: block;
	margin-bottom: 16px;
}

.mgmt-title {
	font-size: 28px;
	font-weight: 500;
	color: #303133;
	display: block;
	margin-bottom: 8px;
}

.mgmt-desc {
	font-size: 24px;
	color: #909399;
}

.loading-state, .empty-state {
	padding: 64px 32px;
	text-align: center;
}

.empty-text {
	font-size: 32px;
	color: #909399;
}

.activity-list {
	padding: 32px;
}

.activity-item {
	display: flex;
	align-items: center;
	padding: 20px 0;
	border-bottom: 1px solid #f5f7fa;
}

.activity-item:last-child {
	border-bottom: none;
}

.activity-icon {
	width: 64px;
	height: 64px;
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 28px;
	margin-right: 20px;
	background-color: #f5f7fa;
}

.activity-icon.user {
	background-color: #e1f3d8;
}

.activity-icon.course {
	background-color: #fdf6ec;
}

.activity-icon.schedule {
	background-color: #f4f4f5;
}

.activity-icon.system {
	background-color: #fef0f0;
}

.activity-content {
	flex: 1;
}

.activity-title {
	font-size: 28px;
	color: #303133;
	display: block;
	margin-bottom: 8px;
}

.activity-time {
	font-size: 24px;
	color: #909399;
}

.system-status {
	background: white;
	border-radius: 16px;
	padding: 32px;
	box-shadow: 0 4px 20px rgba(0,0,0,0.08);
}

.status-item {
	display: flex;
	align-items: center;
	margin-bottom: 16px;
}

.status-item:last-child {
	margin-bottom: 0;
}

.status-dot {
	width: 12px;
	height: 12px;
	border-radius: 50%;
	margin-right: 16px;
}

.status-dot.active {
	background-color: #67c23a;
}

.status-text {
	font-size: 28px;
	color: #303133;
}
</style>