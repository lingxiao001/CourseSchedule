<template>
	<view class="teacher-dashboard">
		<view class="dashboard-title">教师工作台</view>

		<!-- 功能卡片区域 -->
		<view class="dashboard-row">
			<view class="function-cards">
				<view class="function-card" @click="navigateTo('/pages/teacher/courses')">
					<view class="card-icon">📚</view>
					<view class="card-content">
						<text class="card-title">课程管理</text>
						<text class="card-desc">管理您的课程信息</text>
					</view>
					<view class="card-arrow">→</view>
				</view>

				<view class="function-card" @click="navigateTo('/pages/teacher/teaching-classes')">
					<view class="card-icon">👥</view>
					<view class="card-content">
						<text class="card-title">教学班管理</text>
						<text class="card-desc">管理您的教学班级</text>
					</view>
					<view class="card-arrow">→</view>
				</view>
			</view>
		</view>

		<!-- 最近课程概览 -->
		<view class="dashboard-row">
			<view class="card recent-courses">
				<view class="card-header">
					<text class="card-title">最近课程</text>
					<view class="card-action" @click="navigateTo('/pages/teacher/courses')">
						<text class="action-text">查看全部</text>
					</view>
				</view>
				<view v-if="loading" class="loading-state">
					<text>加载中...</text>
				</view>
				<view v-else-if="recentCourses.length === 0" class="empty-state">
					<text class="empty-text">暂无课程数据</text>
					<text class="empty-hint">请先添加课程信息</text>
				</view>
				<view v-else class="course-table">
					<view class="table-header">
						<text class="col-code">课程代码</text>
						<text class="col-name">课程名称</text>
						<text class="col-action">操作</text>
					</view>
					<view v-for="course in recentCourses.slice(0, 4)" :key="course.classCode" class="table-row">
						<text class="col-code">{{ course.classCode }}</text>
						<text class="col-name">{{ course.name }}</text>
						<view class="col-action">
							<text class="action-btn" @click="manageCourse(course)">管理</text>
						</view>
					</view>
				</view>
			</view>
		</view>

		<!-- 教学统计 -->
		<view class="dashboard-row">
			<view class="stats-grid">
				<view class="stat-card">
					<view class="stat-number">{{ stats.totalCourses }}</view>
					<view class="stat-label">总课程数</view>
				</view>
				<view class="stat-card">
					<view class="stat-number">{{ stats.totalClasses }}</view>
					<view class="stat-label">教学班数</view>
				</view>
				<view class="stat-card">
					<view class="stat-number">{{ stats.totalStudents }}</view>
					<view class="stat-label">学生总数</view>
				</view>
				<view class="stat-card">
					<view class="stat-number">{{ stats.weeklyHours }}</view>
					<view class="stat-label">周课时数</view>
				</view>
			</view>
		</view>

		<!-- 快捷操作 -->
		<view class="dashboard-row">
			<view class="quick-actions">
				<view class="quick-action" @click="navigateTo('/pages/teacher/courses')">
					<text class="action-icon">📖</text>
					<text class="action-title">课程管理</text>
				</view>
				<view class="quick-action" @click="navigateTo('/pages/teacher/teaching-classes')">
					<text class="action-icon">👨‍🏫</text>
					<text class="action-title">教学班</text>
				</view>
				<view class="quick-action" @click="navigateTo('/pages/schedule/index')">
					<text class="action-icon">📅</text>
					<text class="action-title">课表查看</text>
				</view>
				<view class="quick-action" @click="navigateTo('/pages/profile/index')">
					<text class="action-icon">👤</text>
					<text class="action-title">个人信息</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, onMounted } from 'vue'

const loading = ref(true)
const recentCourses = ref([])
const stats = ref({
	totalCourses: 0,
	totalClasses: 0,
	totalStudents: 0,
	weeklyHours: 0
})

// 导航到指定页面
const navigateTo = (url) => {
	uni.navigateTo({ url })
}

// 管理课程
const manageCourse = (course) => {
	console.log('管理课程:', course)
	navigateTo('/pages/teacher/courses')
}

// 加载教师数据
const loadTeacherData = async () => {
	loading.value = true
	try {
		// 模拟数据，实际应该调用API
		setTimeout(() => {
			recentCourses.value = [
				{
					classCode: 'CS101',
					name: '计算机科学导论'
				},
				{
					classCode: 'CS201',
					name: '数据结构与算法'
				},
				{
					classCode: 'CS301',
					name: '操作系统原理'
				}
			]
			
			stats.value = {
				totalCourses: 8,
				totalClasses: 12,
				totalStudents: 180,
				weeklyHours: 16
			}
			
			loading.value = false
		}, 800)
	} catch (error) {
		console.error('加载教师数据失败:', error)
		loading.value = false
	}
}

onMounted(() => {
	loadTeacherData()
})
</script>

<style lang="scss" scoped>
.teacher-dashboard {
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
	background-color: #e6a23c;
	margin-right: 20px;
	border-radius: 4px;
}

.dashboard-row {
	margin-bottom: 32px;
}

.function-cards {
	display: flex;
	flex-direction: column;
	gap: 16px;
}

.function-card {
	background: white;
	border-radius: 16px;
	padding: 32px;
	display: flex;
	align-items: center;
	box-shadow: 0 4px 20px rgba(0,0,0,0.08);
	transition: all 0.3s;
}

.function-card:active {
	transform: scale(0.98);
}

.card-icon {
	font-size: 48px;
	margin-right: 24px;
}

.card-content {
	flex: 1;
}

.card-title {
	font-size: 32px;
	font-weight: 500;
	color: #303133;
	display: block;
	margin-bottom: 8px;
}

.card-desc {
	font-size: 28px;
	color: #909399;
	display: block;
}

.card-arrow {
	font-size: 32px;
	color: #c0c4cc;
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

.card-header .card-title {
	font-size: 32px;
	font-weight: 500;
	color: #303133;
}

.card-action {
	background: #e6a23c;
	color: white;
	padding: 16px 24px;
	border-radius: 8px;
	font-size: 28px;
}

.action-text {
	color: white;
	font-size: 28px;
}

.loading-state, .empty-state {
	padding: 64px 32px;
	text-align: center;
}

.empty-text {
	font-size: 32px;
	color: #909399;
	display: block;
	margin-bottom: 16px;
}

.empty-hint {
	font-size: 28px;
	color: #c0c4cc;
	display: block;
}

.course-table {
	padding: 32px;
}

.table-header {
	display: flex;
	padding: 16px 0;
	border-bottom: 2px solid #ebeef5;
	font-weight: 500;
	color: #909399;
}

.table-row {
	display: flex;
	padding: 24px 0;
	border-bottom: 1px solid #f5f7fa;
	align-items: center;
}

.table-row:last-child {
	border-bottom: none;
}

.col-code {
	width: 120px;
	font-size: 28px;
}

.col-name {
	flex: 1;
	font-size: 28px;
	color: #303133;
}

.col-action {
	width: 80px;
	text-align: center;
}

.action-btn {
	color: #e6a23c;
	font-size: 28px;
}

.stats-grid {
	display: grid;
	grid-template-columns: repeat(2, 1fr);
	gap: 16px;
}

.stat-card {
	background: white;
	border-radius: 16px;
	padding: 32px;
	text-align: center;
	box-shadow: 0 4px 20px rgba(0,0,0,0.08);
}

.stat-number {
	font-size: 48px;
	font-weight: 600;
	color: #e6a23c;
	display: block;
	margin-bottom: 12px;
}

.stat-label {
	font-size: 28px;
	color: #606266;
}

.quick-actions {
	display: grid;
	grid-template-columns: repeat(2, 1fr);
	gap: 24px;
}

.quick-action {
	background: white;
	padding: 40px 32px;
	border-radius: 16px;
	text-align: center;
	box-shadow: 0 2px 16px rgba(0,0,0,0.08);
	transition: all 0.3s;
}

.quick-action:active {
	transform: scale(0.98);
}

.action-icon {
	font-size: 48px;
	display: block;
	margin-bottom: 16px;
}

.action-title {
	font-size: 28px;
	color: #303133;
}
</style>