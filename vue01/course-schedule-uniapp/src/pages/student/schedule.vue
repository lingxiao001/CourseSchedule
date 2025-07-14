<template>
	<view class="schedule">
		<!-- 原生导航栏 -->
		<view class="nav-bar">
			<view class="nav-left" @tap="goBack">
				<text class="nav-back">←</text>
			</view>
			<view class="nav-title-container">
				<text class="nav-title">我的课表</text>
				<text class="nav-subtitle">{{ currentWeek }}</text>
			</view>
			<view class="nav-right">
				<text class="nav-refresh" @tap="loadScheduleData">🔄</text>
			</view>
		</view>

		<!-- 加载状态 -->
		<view v-if="loading" class="loading-container">
			<view class="loading-spinner"></view>
			<text class="loading-text">正在加载课表...</text>
		</view>

		<!-- 空状态 -->
		<view v-else-if="!loading && scheduleData.length === 0" class="empty-state">
			<view class="empty-content">
				<text class="empty-icon">📅</text>
				<text class="empty-text">暂无课程安排</text>
				<text class="empty-tip">请先前往选课页面选择课程</text>
				<button class="goto-select" @click="gotoSelectCourse">前往选课</button>
			</view>
		</view>

		<!-- 课表内容 -->
		<view v-else class="schedule-container">
			<!-- 课表表格 -->
			<scroll-view class="schedule-scroll" scroll-x="true">
				<view class="schedule-table">
					<!-- 表头 -->
					<view class="table-header">
						<view class="time-header">时间</view>
						<view 
							v-for="day in weekDays" 
							:key="day.value" 
							class="day-header"
							:class="{ 'today': day.isToday }"
						>
							<text class="day-name">{{ day.name }}</text>
							<text class="day-date">{{ day.date }}</text>
						</view>
					</view>

					<!-- 表格内容 -->
					<view class="table-body">
						<view 
							v-for="(timeSlot, timeIndex) in timeSlots" 
							:key="timeIndex" 
							class="time-row"
						>
							<!-- 时间列 -->
							<view class="time-cell">
								<text class="time-text">{{ timeSlot }}</text>
							</view>

							<!-- 课程列 -->
							<view 
								v-for="day in 7" 
								:key="day" 
								class="course-cell"
								:class="{ 
									'has-course': getCourseAt(day, timeIndex + 1),
									'current-time': isCurrentTime(day, timeIndex + 1)
								}"
								@click="showCourseDetail(day, timeIndex + 1)"
							>
								<view v-if="getCourseAt(day, timeIndex + 1)" class="course-info">
									<text class="course-name">{{ getCourseAt(day, timeIndex + 1).courseName }}</text>
									<text class="course-teacher">{{ getCourseAt(day, timeIndex + 1).teacherName }}</text>
									<text class="course-location">{{ getCourseAt(day, timeIndex + 1).classroom }}</text>
								</view>
							</view>
						</view>
					</view>
				</view>
			</scroll-view>
		</view>

		<!-- 课程详情弹窗 -->
		<view v-if="showDetailModal" class="course-detail-modal" @click="closeCourseDetail">
			<view class="modal-content" @click.stop>
				<view class="modal-header">
					<text class="modal-title">课程详情</text>
					<view class="close-btn" @click="closeCourseDetail">
						<text class="close-icon">×</text>
					</view>
				</view>
				
				<view v-if="currentCourse" class="modal-body">
					<view class="detail-item">
						<text class="detail-label">课程名称</text>
						<text class="detail-value">{{ currentCourse.courseName }}</text>
					</view>
					<view class="detail-item">
						<text class="detail-label">授课教师</text>
						<text class="detail-value">{{ currentCourse.teacherName }}</text>
					</view>
					<view class="detail-item">
						<text class="detail-label">上课时间</text>
						<text class="detail-value">{{ formatClassTime(currentCourse) }}</text>
					</view>
					<view class="detail-item">
						<text class="detail-label">上课地点</text>
						<text class="detail-value">{{ currentCourse.classroom || '待安排' }}</text>
					</view>
					<view class="detail-item">
						<text class="detail-label">课程代码</text>
						<text class="detail-value">{{ currentCourse.classCode }}</text>
					</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
import { studentApi } from '@/api/student'

export default {
	data() {
		return {
			loading: false,
			scheduleData: [],
			currentCourse: null,
			showDetailModal: false,
			// 时间段定义
			timeSlots: [
				'08:00-09:30',
				'09:50-11:20', 
				'13:30-15:00',
				'15:20-16:50',
				'18:30-20:00'
			],
			// 课表数据，按天、节次组织
			timetable: []
		}
	},

	computed: {
		// 当前周信息
		currentWeek() {
			const now = new Date()
			const year = now.getFullYear()
			const month = now.getMonth() + 1
			const date = now.getDate()
			return `${year}年${month}月${date}日 第${this.getWeekNumber()}周`
		},

		// 星期数据
		weekDays() {
			const days = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
			const today = new Date().getDay() || 7 // 将周日从0转为7
			const monday = this.getMonday()
			
			return days.map((name, index) => {
				const date = new Date(monday)
				date.setDate(monday.getDate() + index)
				return {
					name,
					value: index + 1,
					date: `${date.getMonth() + 1}/${date.getDate()}`,
					isToday: (index + 1) === today
				}
			})
		}
	},

	onLoad() {
		this.loadScheduleData()
	},

	onPullDownRefresh() {
		this.loadScheduleData().finally(() => {
			uni.stopPullDownRefresh()
		})
	},

	methods: {
		// 加载课表数据
		async loadScheduleData() {
			this.loading = true
			try {
				// 获取当前用户信息 - 使用uniapp标准存储方式
				const user = uni.getStorageSync('user')
				console.log('User info from storage:', user)
				
				if (!user || !user.id) {
					uni.showToast({
						title: '无法获取学生信息，请重新登录',
						icon: 'none'
					})
					this.loading = false
					setTimeout(() => {
						uni.navigateTo({
							url: '/pages/auth/login'
						})
					}, 1500)
					return
				}

				// 获取学生已选课程
				console.log('Fetching courses for student ID:', user.id)
				const myCourses = await studentApi.getSelectionsByStudentWithTeachers(user.id)
				console.log('Loaded my courses:', myCourses?.length || 0)
				
				// 获取每个课程的排课信息
				const allSchedules = []
				for (const course of myCourses) {
					try {
						const schedules = await studentApi.getSchedulesByTeachingClass(course.teachingClassId)
						console.log(`Schedules for ${course.courseName}:`, schedules?.length || 0)
						
						// 添加课程名称和教师信息到排课记录
						const enrichedSchedules = schedules.map(schedule => ({
							...schedule,
							courseName: course.courseName,
							teacherName: course.teacherName,
							classCode: course.classCode,
							isStudentCourse: true
						}))
						
						allSchedules.push(...enrichedSchedules)
					} catch (error) {
						console.error(`Error loading schedule for course ${course.courseName}:`, error)
					}
				}
				
				this.scheduleData = allSchedules
				console.log('Total schedules loaded:', this.scheduleData.length)
				
				// 将课程安排映射到时间表
				this.mapSchedulesToTimetable()
				
			} catch (error) {
				console.error('加载课表失败:', error)
				uni.showToast({
					title: `加载课表失败: ${error.message || '未知错误'}`,
					icon: 'none'
				})
			} finally {
				this.loading = false
			}
		},

		// 将课程安排映射到时间表
		mapSchedulesToTimetable() {
			// 初始化7天x5节课的二维数组
			this.timetable = Array(7).fill().map(() => Array(5).fill(null))
			
			console.log('Mapping schedules:', this.scheduleData)
			
			this.scheduleData.forEach(schedule => {
				// 记录每个schedule的详细信息
				console.log('Processing schedule:', {
					dayOfWeek: schedule.dayOfWeek,
					startTime: schedule.startTime,
					endTime: schedule.endTime,
					courseName: schedule.courseName,
					teachingClassId: schedule.teachingClassId
				})
				
				// 确保有有效数据
				if (!schedule || !schedule.dayOfWeek) {
					console.warn('Invalid schedule data:', schedule)
					return
				}
				
				// 解析时间字符串获取节次
				let timeSlotIndex = -1
				if (typeof schedule.startTime === 'string') {
					// 解析时间字符串如 "8:00-9:40" 中的起始小时
					const startHour = parseInt(schedule.startTime.split(':')[0])
					if (startHour === 8) timeSlotIndex = 0      // 1-2节 8:00-9:40
					else if (startHour === 10) timeSlotIndex = 1  // 3-4节 10:00-11:40
					else if (startHour === 14) timeSlotIndex = 2  // 5-6节 14:00-15:40
					else if (startHour === 16) timeSlotIndex = 3  // 7-8节 16:00-17:40
					else if (startHour === 19) timeSlotIndex = 4  // 9-10节 19:00-20:40
				} else {
					// 如果是数字，直接使用
					timeSlotIndex = schedule.startTime - 1
				}
				
				// 验证数据有效性
				if (timeSlotIndex >= 0 && timeSlotIndex < 5 && schedule.dayOfWeek >= 1 && schedule.dayOfWeek <= 7) {
					this.timetable[schedule.dayOfWeek - 1][timeSlotIndex] = {
						...schedule,
						teacherName: schedule.teacherName || '未知教师',
						classroom: schedule.classroomName || schedule.building || '待安排'
					}
					console.log(`Added course to day ${schedule.dayOfWeek}, slot ${timeSlotIndex + 1}: ${schedule.courseName}`)
				} else {
					console.warn('Invalid time slot mapping:', {
						dayOfWeek: schedule.dayOfWeek,
						startTime: schedule.startTime,
						timeSlotIndex
					})
				}
			})
			
			console.log('Final timetable:', this.timetable)
		},

		// 获取指定位置的课程
		getCourseAt(day, timeSlot) {
			return this.timetable[day - 1]?.[timeSlot - 1] || null
		},

		// 显示课程详情
		showCourseDetail(day, timeSlot) {
			const course = this.getCourseAt(day, timeSlot)
			if (course) {
				this.currentCourse = course
				this.showDetailModal = true
			}
		},

		// 关闭课程详情
		closeCourseDetail() {
			this.showDetailModal = false
			this.currentCourse = null
		},

		// 格式化上课时间
		formatClassTime(course) {
			if (!course) return ''
			const dayMap = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']
			const timeSlotMap = ['8:00-9:40', '10:00-11:40', '14:00-15:40', '16:00-17:40', '19:00-20:40']
			const timeText = timeSlotMap[course.startTime - 1] || `${course.startTime}-${course.endTime}`
			return `${dayMap[course.dayOfWeek - 1]} ${timeText}`
		},

		// 判断是否为当前时间
		isCurrentTime(day, timeSlot) {
			const now = new Date()
			const currentDay = now.getDay() || 7
			const currentHour = now.getHours()
			const currentMinute = now.getMinutes()
			const currentTime = currentHour * 60 + currentMinute
			
			if (day !== currentDay) return false
			
			// 解析时间段
			const timeSlotStr = this.timeSlots[timeSlot - 1]
			if (!timeSlotStr) return false
			
			const [startTime, endTime] = timeSlotStr.split('-')
			const [startHour, startMinute] = startTime.split(':').map(Number)
			const [endHour, endMinute] = endTime.split(':').map(Number)
			
			const slotStart = startHour * 60 + startMinute
			const slotEnd = endHour * 60 + endMinute
			
			return currentTime >= slotStart && currentTime <= slotEnd
		},

		// 前往选课页面
		gotoSelectCourse() {
			uni.navigateTo({
				url: '/pages/student/select-course'
			})
		},

		// 返回上一页
		goBack() {
			uni.navigateBack()
		},

		// 获取周数
		getWeekNumber() {
			const now = new Date()
			const start = new Date(now.getFullYear(), 0, 1)
			const diff = now - start
			return Math.ceil(diff / (7 * 24 * 60 * 60 * 1000))
		},

		// 获取本周一的日期
		getMonday() {
			const today = new Date()
			const day = today.getDay() || 7 // 将周日从0转为7
			const monday = new Date(today)
			monday.setDate(today.getDate() - day + 1)
			return monday
		}
	}
}
</script>

<style lang="scss">
.schedule {
	background-color: #f5f7fa;
	min-height: 100vh;
}

/* 原生导航栏样式 */
.nav-bar {
	display: flex;
	align-items: center;
	justify-content: space-between;
	height: 88rpx;
	background-color: #fff;
	border-bottom: 1rpx solid #eee;
	padding: 0 30rpx;
	position: sticky;
	top: 0;
	z-index: 100;
}

.nav-left {
	width: 80rpx;
}

.nav-back {
	font-size: 32rpx;
	color: #333;
}

.nav-title-container {
	display: flex;
	flex-direction: column;
	align-items: center;
}

.nav-title {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
	line-height: 1.2;
}

.nav-subtitle {
	font-size: 20rpx;
	color: #909399;
	line-height: 1;
}

.nav-right {
	width: 80rpx;
	text-align: right;
}

.nav-refresh {
	font-size: 28rpx;
	color: #409eff;
	padding: 10rpx;
}

.loading-container {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 200rpx 0;
}

.loading-spinner {
	width: 60rpx;
	height: 60rpx;
	border: 4rpx solid #f3f3f3;
	border-top: 4rpx solid #409eff;
	border-radius: 50%;
	animation: spin 1s linear infinite;
	margin-bottom: 30rpx;
}

@keyframes spin {
	0% { transform: rotate(0deg); }
	100% { transform: rotate(360deg); }
}

.loading-text {
	font-size: 28rpx;
	color: #999;
}

.empty-state {
	margin-top: 200rpx;
	text-align: center;
}

.empty-content {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
}

.empty-icon {
	font-size: 120rpx;
	color: #ccc;
	margin-bottom: 20rpx;
}

.empty-text {
	font-size: 32rpx;
	color: #909399;
	display: block;
	margin-bottom: 20rpx;
}

.empty-tip {
	font-size: 28rpx;
	color: #c0c4cc;
	display: block;
	margin-bottom: 40rpx;
}

.goto-select {
	background-color: #409eff;
	color: white;
	border: none;
	padding: 20rpx 40rpx;
	border-radius: 40rpx;
	font-size: 28rpx;
}

.schedule-container {
	margin: 20rpx;
	background: white;
	border-radius: 20rpx;
	overflow: hidden;
	box-shadow: 0 4rpx 20rpx rgba(0, 0, 0, 0.1);
}

.schedule-scroll {
	width: 100%;
}

.schedule-table {
	min-width: 750rpx;
	display: flex;
	flex-direction: column;
}

.table-header {
	display: flex;
	background-color: #f5f7fa;
	border-bottom: 2rpx solid #ebeef5;
}

.time-header {
	width: 120rpx;
	padding: 20rpx 10rpx;
	text-align: center;
	font-weight: bold;
	font-size: 26rpx;
	color: #606266;
	border-right: 2rpx solid #ebeef5;
}

.day-header {
	flex: 1;
	padding: 15rpx 5rpx;
	text-align: center;
	border-right: 2rpx solid #ebeef5;
	display: flex;
	flex-direction: column;
	align-items: center;

	&.today {
		background-color: #e1f3d8;
	}

	&:last-child {
		border-right: none;
	}
}

.day-name {
	font-size: 26rpx;
	font-weight: bold;
	color: #303133;
	margin-bottom: 5rpx;
}

.day-date {
	font-size: 22rpx;
	color: #909399;
}

.table-body {
	display: flex;
	flex-direction: column;
}

.time-row {
	display: flex;
	min-height: 120rpx;
	border-bottom: 2rpx solid #ebeef5;

	&:last-child {
		border-bottom: none;
	}
}

.time-cell {
	width: 120rpx;
	padding: 15rpx 10rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	background-color: #f5f7fa;
	border-right: 2rpx solid #ebeef5;
}

.time-text {
	font-size: 22rpx;
	color: #606266;
	text-align: center;
	line-height: 1.2;
}

.course-cell {
	flex: 1;
	padding: 8rpx;
	border-right: 2rpx solid #ebeef5;
	min-height: 120rpx;
	position: relative;
	transition: all 0.3s;

	&:last-child {
		border-right: none;
	}

	&.has-course {
		background-color: #f0f9ff;
		cursor: pointer;

		&:active {
			background-color: #e6f7ff;
		}
	}

	&.current-time {
		background-color: #e1f3d8;

		&:active {
			background-color: #d1e9c6;
		}
	}
}

.course-info {
	padding: 8rpx;
	border-radius: 12rpx;
	height: 100%;
	display: flex;
	flex-direction: column;
	justify-content: center;
	align-items: center;
	background: linear-gradient(135deg, #e6f7ff 0%, #f0f9ff 100%);
	border: 2rpx solid rgba(64, 158, 255, 0.2);
	box-shadow: 0 2rpx 8rpx rgba(64, 158, 255, 0.1);
}

.course-name {
	font-weight: 600;
	font-size: 22rpx;
	color: #409eff;
	margin-bottom: 6rpx;
	text-align: center;
	line-height: 1.2;
	word-break: break-all;
	overflow: hidden;
	text-overflow: ellipsis;
	display: -webkit-box;
	-webkit-line-clamp: 2;
	-webkit-box-orient: vertical;
}

.course-teacher {
	font-size: 20rpx;
	color: #606266;
	margin-bottom: 4rpx;
	text-align: center;
	font-weight: 500;
}

.course-location {
	font-size: 18rpx;
	color: #909399;
	text-align: center;
	font-weight: 400;
}

// 弹窗样式
.course-detail-modal {
	position: fixed;
	top: 0;
	left: 0;
	width: 100%;
	height: 100%;
	background-color: rgba(0, 0, 0, 0.5);
	display: flex;
	align-items: center;
	justify-content: center;
	z-index: 1000;
}

.modal-content {
	background: white;
	border-radius: 24rpx;
	padding: 40rpx;
	margin: 40rpx;
	max-width: 600rpx;
	width: 100%;
	max-height: 80vh;
	overflow-y: auto;
	box-shadow: 0 8rpx 32rpx rgba(0, 0, 0, 0.15);
}

.modal-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 40rpx;
	padding-bottom: 20rpx;
	border-bottom: 2rpx solid #ebeef5;
}

.modal-title {
	font-size: 32rpx;
	font-weight: bold;
	color: #303133;
}

.close-btn {
	width: 60rpx;
	height: 60rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	background-color: #f5f7fa;
	border-radius: 50%;
}

.close-icon {
	font-size: 40rpx;
	color: #909399;
}

.modal-body {
	padding: 20rpx 0;
}

.detail-item {
	display: flex;
	justify-content: space-between;
	align-items: flex-start;
	padding: 30rpx 0;
	border-bottom: 2rpx solid #f5f7fa;

	&:last-child {
		border-bottom: none;
	}
}

.detail-label {
	font-size: 28rpx;
	color: #606266;
	font-weight: 500;
	min-width: 120rpx;
}

.detail-value {
	font-size: 28rpx;
	color: #303133;
	flex: 1;
	text-align: right;
	margin-left: 20rpx;
	font-weight: 500;
	line-height: 1.4;
}
</style>