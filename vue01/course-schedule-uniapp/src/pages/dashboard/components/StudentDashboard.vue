<template>
	<view class="student-dashboard">
		<view class="dashboard-title">学生个人中心</view>

		<!-- 课程概览卡片 -->
		<view class="dashboard-row">
			<view class="card selected-courses">
				<view class="card-header">
					<text class="card-title">已选课程</text>
					<view class="card-action" @click="navigateTo('/pages/student/select-course')">
						<text class="action-text">前往选课</text>
					</view>
				</view>
				<view v-if="loading" class="loading-state">
					<text>加载中...</text>
				</view>
				<view v-else-if="selectedCourses.length === 0" class="empty-state">
					<text class="empty-text">暂无选课记录</text>
					<text class="empty-hint">点击右上角按钮开始选课</text>
				</view>
				<view v-else class="course-list">
					<view v-for="course in selectedCourses.slice(0, 3)" :key="course.teachingClassId" class="course-item">
						<view class="course-name">{{ course.courseName }}</view>
						<view class="course-info">
							<text class="course-teacher">{{ course.teacherName }}</text>
							<text class="course-class">班级: {{ course.teachingClassId }}</text>
						</view>
					</view>
					<view v-if="selectedCourses.length > 3" class="more-courses" @click="navigateTo('/pages/student/my-courses')">
						<text>查看全部 {{ selectedCourses.length }} 门课程</text>
					</view>
				</view>
			</view>
		</view>

		<!-- 本周课表 -->
		<view class="dashboard-row">
			<view class="card schedule-card">
				<view class="card-header">
					<text class="card-title">本周课表</text>
					<text class="current-week">第{{ currentWeek }}周</text>
				</view>
				<view class="timetable-container">
					<view class="timetable-mini">
						<view class="timetable-header">
							<view class="time-col">时间</view>
							<view v-for="day in weekDays" :key="day" class="day-col">
								{{ day }}
							</view>
						</view>
						<view class="timetable-body">
							<view v-for="(timeSlot, index) in timeSlots.slice(0, 3)" :key="index" class="time-row">
								<view class="time-col">{{ timeSlot }}</view>
								<view v-for="day in 5" :key="day" class="day-col" :class="{ 'has-course': getCourseAt(day, index + 1) }">
									<view v-if="getCourseAt(day, index + 1)" class="course-cell">
										<text class="course-name">{{ getCourseAt(day, index + 1).courseName }}</text>
									</view>
								</view>
							</view>
						</view>
					</view>
					<view class="view-full" @click="navigateTo('/pages/schedule/index')">
						<text>查看完整课表</text>
					</view>
				</view>
			</view>
		</view>

		<!-- 快捷操作 -->
		<view class="dashboard-row">
			<view class="quick-actions">
				<view class="quick-action" @click="navigateTo('/pages/student/select-course')">
					<text class="action-icon">📚</text>
					<text class="action-title">选课</text>
				</view>
				<view class="quick-action" @click="navigateTo('/pages/student/my-courses')">
					<text class="action-icon">📖</text>
					<text class="action-title">我的课程</text>
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
import { authApi } from '@/api/auth'

const loading = ref(true)
const selectedCourses = ref([])
const courseSchedules = ref([])
const currentWeek = ref(1)

const weekDays = ['周一', '周二', '周三', '周四', '周五']
const timeSlots = ref([
	"08:00-09:30", 
	"09:50-11:20",
	"13:30-15:00", 
	"15:20-16:50",
	"18:30-20:00"
])

// 课表数据，按天、节次组织
const timetable = ref(Array(5).fill().map(() => Array(5).fill(null)))

// 获取指定位置课程
const getCourseAt = (day, timeSlot) => {
	return timetable.value[day - 1]?.[timeSlot - 1] || null
}

// 导航到指定页面
const navigateTo = (url) => {
	uni.navigateTo({ url })
}

// 加载学生数据
const loadStudentData = async () => {
	loading.value = true
	try {
		// 模拟数据，实际应该调用API
		setTimeout(() => {
			selectedCourses.value = [
				{
					teachingClassId: 'CS101-001',
					courseName: '计算机科学导论',
					teacherName: '张教授',
					selectionTime: '2024-07-01'
				},
				{
					teachingClassId: 'MATH201-002',
					courseName: '高等数学',
					teacherName: '李教授',
					selectionTime: '2024-07-01'
				}
			]
			
			// 模拟课表数据
			timetable.value[0][0] = { courseName: '计算机科学导论' }
			timetable.value[1][1] = { courseName: '高等数学' }
			
			loading.value = false
		}, 1000)
	} catch (error) {
		console.error('加载学生数据失败:', error)
		loading.value = false
	}
}

onMounted(() => {
	loadStudentData()
	
	// 计算当前周数
	const now = new Date()
	const startOfYear = new Date(now.getFullYear(), 0, 1)
	const weekNumber = Math.ceil(((now - startOfYear) / 86400000 + startOfYear.getDay() + 1) / 7)
	currentWeek.value = weekNumber
})
</script>

<style lang="scss" scoped>
.student-dashboard {
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
	background-color: #409eff;
	margin-right: 20px;
	border-radius: 4px;
}

.dashboard-row {
	margin-bottom: 32px;
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
	background: #409eff;
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

.course-list {
	padding: 32px;
}

.course-item {
	padding: 24px 0;
	border-bottom: 1px solid #f5f7fa;
}

.course-item:last-child {
	border-bottom: none;
}

.course-name {
	font-size: 32px;
	font-weight: 500;
	color: #303133;
	margin-bottom: 12px;
}

.course-info {
	display: flex;
	justify-content: space-between;
}

.course-teacher, .course-class {
	font-size: 28px;
	color: #606266;
}

.more-courses {
	text-align: center;
	padding: 24px;
	color: #409eff;
	font-size: 28px;
	border-top: 1px solid #f5f7fa;
}

.current-week {
	background: #e1f3d8;
	color: #67c23a;
	padding: 8px 16px;
	border-radius: 8px;
	font-size: 24px;
}

.timetable-container {
	padding: 32px;
}

.timetable-mini {
	border: 1px solid #ebeef5;
	border-radius: 8px;
	overflow: hidden;
	margin-bottom: 24px;
}

.timetable-header {
	display: flex;
	background-color: #f5f7fa;
	border-bottom: 1px solid #ebeef5;
	font-weight: 500;
}

.timetable-body {
	display: flex;
	flex-direction: column;
}

.time-row {
	display: flex;
	min-height: 80px;
	border-bottom: 1px solid #ebeef5;
}

.time-row:last-child {
	border-bottom: none;
}

.time-col {
	width: 120px;
	padding: 16px;
	display: flex;
	align-items: center;
	justify-content: center;
	background-color: #f5f7fa;
	border-right: 1px solid #ebeef5;
	font-size: 24px;
	color: #606266;
	font-weight: 500;
}

.day-col {
	flex: 1;
	padding: 8px;
	border-right: 1px solid #ebeef5;
	min-height: 80px;
	position: relative;
	transition: all 0.3s;
}

.day-col:last-child {
	border-right: none;
}

.day-col.has-course {
	background-color: #f0f9ff;
}

.course-cell {
	padding: 8px;
	border-radius: 4px;
	height: 100%;
	display: flex;
	align-items: center;
	justify-content: center;
	background-color: #e6f7ff;
}

.course-cell .course-name {
	font-size: 24px;
	color: #409eff;
	text-align: center;
	margin: 0;
}

.view-full {
	text-align: center;
	color: #409eff;
	font-size: 28px;
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