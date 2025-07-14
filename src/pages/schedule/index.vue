<template>
	<view class="schedule-page">
		<view class="page-header">
			<text class="page-title">课表查看</text>
			<view class="week-selector">
				<text class="week-text">第{{ currentWeek }}周</text>
			</view>
		</view>

		<view class="timetable-container">
			<view class="timetable">
				<view class="timetable-header">
					<view class="time-col">时间</view>
					<view v-for="day in weekDays" :key="day" class="day-col">
						{{ day }}
					</view>
				</view>
				<view class="timetable-body">
					<view v-for="(timeSlot, index) in timeSlots" :key="index" class="time-row">
						<view class="time-col">{{ timeSlot }}</view>
						<view v-for="day in 7" :key="day" class="day-col" :class="{ 'has-course': getCourseAt(day, index + 1) }" @click="showCourseDetail(day, index + 1)">
							<view v-if="getCourseAt(day, index + 1)" class="course-cell">
								<view class="course-name">{{ getCourseAt(day, index + 1).courseName }}</view>
								<view class="course-info">
									<text class="course-location">{{ getCourseAt(day, index + 1).building }}{{ getCourseAt(day, index + 1).classroomName }}</text>
								</view>
							</view>
						</view>
					</view>
				</view>
			</view>
		</view>

		<!-- 课程详情弹窗 -->
		<view v-if="detailVisible" class="detail-modal" @click="hideDetail">
			<view class="modal-content" @click.stop>
				<view class="modal-header">
					<text class="modal-title">课程详情</text>
					<view class="close-btn" @click="hideDetail">✕</view>
				</view>
				<view v-if="currentCourse" class="course-detail">
					<view class="detail-item">
						<text class="detail-label">课程名称</text>
						<text class="detail-value">{{ currentCourse.courseName }}</text>
					</view>
					<view class="detail-item">
						<text class="detail-label">教学班ID</text>
						<text class="detail-value">{{ currentCourse.teachingClassId }}</text>
					</view>
					<view class="detail-item">
						<text class="detail-label">授课教师</text>
						<text class="detail-value">{{ currentCourse.teacherName || '未知教师' }}</text>
					</view>
					<view class="detail-item">
						<text class="detail-label">上课时间</text>
						<text class="detail-value">{{ formatClassTime(currentCourse) }}</text>
					</view>
					<view class="detail-item">
						<text class="detail-label">上课地点</text>
						<text class="detail-value">{{ currentCourse.building }}{{ currentCourse.classroomName }}</text>
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
			<view class="nav-item active">
				<text class="nav-icon">📅</text>
				<text class="nav-text">课表</text>
			</view>
			<view class="nav-item" @click="goProfile">
				<text class="nav-icon">👤</text>
				<text class="nav-text">我的</text>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { authApi } from '@/api/auth'

const currentWeek = ref(1)
const detailVisible = ref(false)
const currentCourse = ref(null)

const weekDays = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
const timeSlots = ref([
	"08:00-09:30", 
	"09:50-11:20",
	"13:30-15:00", 
	"15:20-16:50",
	"18:30-20:00"
])

// 课表数据，按天、节次组织
const timetable = ref(Array(7).fill().map(() => Array(5).fill(null)))

// 获取指定位置课程
const getCourseAt = (day, timeSlot) => {
	return timetable.value[day - 1]?.[timeSlot - 1] || null
}

// 显示课程详情
const showCourseDetail = (day, timeSlot) => {
	const course = getCourseAt(day, timeSlot)
	if (course) {
		currentCourse.value = course
		detailVisible.value = true
	}
}

// 隐藏详情
const hideDetail = () => {
	detailVisible.value = false
	currentCourse.value = null
}

// 格式化上课时间显示
const formatClassTime = (course) => {
	if (!course) return ''
	const dayMap = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
	return `${dayMap[course.dayOfWeek - 1]} ${course.startTime}-${course.endTime}`
}

// 导航函数
const goHome = () => {
	uni.reLaunch({ url: '/pages/dashboard/index' })
}

const goProfile = () => {
	uni.navigateTo({ url: '/pages/profile/index' })
}

// 加载课表数据
const loadScheduleData = () => {
	// 模拟数据
	timetable.value[0][0] = { 
		courseName: '计算机科学导论', 
		teacherName: '张教授',
		building: 'A楼',
		classroomName: '101',
		teachingClassId: 'CS101-001',
		dayOfWeek: 1,
		startTime: '08:00',
		endTime: '09:30'
	}
	timetable.value[1][1] = { 
		courseName: '高等数学', 
		teacherName: '李教授',
		building: 'B楼',
		classroomName: '205',
		teachingClassId: 'MATH201-002',
		dayOfWeek: 2,
		startTime: '09:50',
		endTime: '11:20'
	}
	timetable.value[2][2] = { 
		courseName: '数据结构', 
		teacherName: '王教授',
		building: 'C楼',
		classroomName: '303',
		teachingClassId: 'CS201-003',
		dayOfWeek: 3,
		startTime: '13:30',
		endTime: '15:00'
	}
}

onMounted(() => {
	// 检查登录状态
	if (!authApi.isAuthenticated()) {
		uni.reLaunch({ url: '/pages/auth/login' })
		return
	}
	
	loadScheduleData()
	
	// 计算当前周数
	const now = new Date()
	const startOfYear = new Date(now.getFullYear(), 0, 1)
	const weekNumber = Math.ceil(((now - startOfYear) / 86400000 + startOfYear.getDay() + 1) / 7)
	currentWeek.value = weekNumber
})
</script>

<style lang="scss" scoped>
.schedule-page {
	display: flex;
	flex-direction: column;
	height: 100vh;
	background: #f5f7fa;
}

.page-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 40px 32px 24px;
	background: white;
	box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.page-title {
	font-size: 36px;
	font-weight: 600;
	color: #303133;
}

.week-selector {
	background: #e1f3d8;
	color: #67c23a;
	padding: 12px 20px;
	border-radius: 8px;
}

.week-text {
	font-size: 28px;
	color: #67c23a;
}

.timetable-container {
	flex: 1;
	padding: 32px;
	overflow: auto;
}

.timetable {
	background: white;
	border-radius: 16px;
	overflow: hidden;
	box-shadow: 0 4px 20px rgba(0,0,0,0.08);
	min-width: 800px;
}

.timetable-header {
	display: flex;
	background-color: #f5f7fa;
	border-bottom: 2px solid #ebeef5;
	font-weight: 600;
}

.timetable-body {
	display: flex;
	flex-direction: column;
}

.time-row {
	display: flex;
	min-height: 120px;
	border-bottom: 1px solid #ebeef5;
}

.time-row:last-child {
	border-bottom: none;
}

.time-col {
	width: 140px;
	padding: 20px;
	display: flex;
	align-items: center;
	justify-content: center;
	background-color: #f5f7fa;
	border-right: 1px solid #ebeef5;
	font-size: 26px;
	color: #606266;
	font-weight: 500;
}

.day-col {
	flex: 1;
	padding: 8px;
	border-right: 1px solid #ebeef5;
	min-height: 120px;
	position: relative;
	transition: all 0.3s;
}

.day-col:last-child {
	border-right: none;
}

.day-col.has-course {
	background-color: #f0f9ff;
	cursor: pointer;
}

.course-cell {
	padding: 16px;
	border-radius: 8px;
	height: 100%;
	display: flex;
	flex-direction: column;
	justify-content: center;
	background: linear-gradient(135deg, #e6f7ff 0%, #f0f9ff 100%);
	border: 1px solid #b3d8ff;
}

.course-name {
	font-weight: 600;
	font-size: 26px;
	color: #409eff;
	text-align: center;
	margin-bottom: 8px;
}

.course-info {
	text-align: center;
}

.course-location {
	font-size: 22px;
	color: #606266;
}

.detail-modal {
	position: fixed;
	top: 0;
	left: 0;
	width: 100%;
	height: 100%;
	background: rgba(0,0,0,0.5);
	display: flex;
	align-items: center;
	justify-content: center;
	z-index: 1000;
}

.modal-content {
	background: white;
	border-radius: 16px;
	width: 80%;
	max-width: 500px;
	overflow: hidden;
}

.modal-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 32px;
	background: #f5f7fa;
	border-bottom: 1px solid #ebeef5;
}

.modal-title {
	font-size: 32px;
	font-weight: 600;
	color: #303133;
}

.close-btn {
	width: 48px;
	height: 48px;
	border-radius: 50%;
	background: #f56c6c;
	color: white;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 24px;
}

.course-detail {
	padding: 32px;
}

.detail-item {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 20px 0;
	border-bottom: 1px solid #f5f7fa;
}

.detail-item:last-child {
	border-bottom: none;
}

.detail-label {
	font-size: 28px;
	color: #909399;
	width: 120px;
}

.detail-value {
	flex: 1;
	font-size: 28px;
	color: #303133;
	text-align: right;
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