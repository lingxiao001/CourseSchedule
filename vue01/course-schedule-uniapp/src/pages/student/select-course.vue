<template>
	<view class="select-course">
		<view class="search-bar">
			<input 
				v-model="searchText" 
				placeholder="搜索课程名称或教师姓名" 
				class="search-input"
				@input="onSearchInput"
			/>
		</view>
		
		<view class="course-list">
			<view v-if="loading" class="loading-container">
				<view class="loading-spinner"></view>
				<text class="loading-text">加载中...</text>
			</view>
			
			<view v-else-if="filteredCourses.length === 0" class="empty-state">
				<text class="empty-icon">📚</text>
				<text class="empty-text">暂无课程可选</text>
			</view>
			
			<view v-else>
				<view class="stats-bar">
					<text class="stats-text">共找到 {{ filteredCourses.length }} 门可选课程</text>
				</view>
				<view v-for="course in filteredCourses" :key="course.id" class="course-item">
					<view class="course-header">
						<text class="course-name">{{ course.courseName }}</text>
						<text class="course-code">{{ course.classCode }}</text>
					</view>
					<view class="course-info">
						<text class="course-teacher">教师：{{ course.teacherName }}</text>
						<text class="course-credit">学分：{{ course.credit }}</text>
						<view class="course-stats">
							<text class="course-students">已选：{{ course.currentStudents }}/{{ course.maxStudents }}</text>
							<view class="progress-bar">
								<view 
									class="progress-fill" 
									:style="{ width: (course.currentStudents / course.maxStudents * 100) + '%' }"
									:class="{ 'full': course.currentStudents >= course.maxStudents }"
								></view>
							</view>
						</view>
					</view>
					<view v-if="course.description" class="course-description">
						<text class="description-text">{{ course.description }}</text>
					</view>
					<button 
						class="select-button" 
						:disabled="course.currentStudents >= course.maxStudents"
						:class="{ 'full': course.currentStudents >= course.maxStudents }"
						@click="selectCourse(course)"
					>
						{{ course.currentStudents >= course.maxStudents ? '课程已满' : '立即选课' }}
					</button>
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
			searchText: '',
			allCourses: [],
			loading: false
		}
	},
	
	computed: {
		filteredCourses() {
			if (!this.searchText) return this.allCourses
			const keyword = this.searchText.toLowerCase()
			return this.allCourses.filter(course => 
				course.courseName.toLowerCase().includes(keyword) ||
				course.teacherName.toLowerCase().includes(keyword)
			)
		}
	},
	
	onLoad() {
		this.loadAvailableCourses()
	},
	
	methods: {
		async loadAvailableCourses() {
			this.loading = true
			try {
				// 获取所有教学班列表
				const response = await this.makeRequest('/api/courses/classes', 'GET')
				
				if (response.data) {
					// 获取每个教学班对应的课程信息
					const teachingClasses = response.data
					
					// 获取所有课程信息用于映射
					const coursesResponse = await this.makeRequest('/api/courses', 'GET')
					
					const coursesMap = {}
					if (coursesResponse.data) {
						coursesResponse.data.forEach(course => {
							coursesMap[course.id] = course
						})
					}
					
					// 获取学生已选课程
					const user = uni.getStorageSync('user')
					const selectedCourses = await this.getSelectedCourses(user.id)
					const selectedTeachingClassIds = selectedCourses.map(course => course.teachingClassId)
					
					// 组合数据并过滤已选课程
					this.allCourses = teachingClasses
						.filter(tc => !selectedTeachingClassIds.includes(tc.id))
						.map(tc => {
							const course = coursesMap[tc.courseId] || {}
							return {
								id: tc.id,
								courseName: course.name || '未知课程',
								classCode: tc.classCode,
								teacherName: tc.teacherName || '未知教师',
								credit: course.credit || 0,
								currentStudents: tc.currentStudents || 0,
								maxStudents: tc.maxStudents || 0,
								teachingClassId: tc.id,
								description: course.description || '',
								courseId: tc.courseId
							}
						})
					
					console.log('Loaded teaching classes:', this.allCourses)
				}
			} catch (error) {
				console.error('加载课程失败:', error)
				uni.showToast({
					title: '加载课程失败',
					icon: 'none'
				})
			} finally {
				this.loading = false
			}
		},

		// 通用请求方法
		makeRequest(url, method = 'GET', data = null) {
			return new Promise((resolve, reject) => {
				uni.request({
					url: `http://localhost:8080${url}`,
					method: method,
					data: data,
					header: {
						'Content-Type': 'application/json'
					},
					success: (res) => {
						resolve(res)
					},
					fail: (err) => {
						reject(err)
					}
				})
			})
		},

		// 获取学生已选课程
		async getSelectedCourses(studentId) {
			try {
				const response = await this.makeRequest(`/api/selections/my-courses/student/${studentId}`, 'GET')
				console.log('获取已选课程响应:', response)
				
				// 确保返回数组类型
				let data = response.data || []
				
				// 处理不同的响应格式
				if (typeof data === 'string') {
					try {
						data = JSON.parse(data)
					} catch (e) {
						console.error('解析JSON失败:', e)
						return []
					}
				}
				
				if (Array.isArray(data)) {
					return data
				} else if (data && typeof data === 'object') {
					// 如果响应是对象，尝试获取其中的数组
					return data.data || data.courses || data.list || []
				}
				return []
			} catch (error) {
				console.error('获取已选课程失败:', error)
				// 如果API调用失败，返回空数组而不是抛出错误
				return []
			}
		},
		
		onSearchInput() {
			// 搜索逻辑已在computed中处理
		},

		async selectCourse(course) {
			// 选课验证
			if (course.currentStudents >= course.maxStudents) {
				uni.showToast({
					title: '课程已满，无法选择',
					icon: 'none'
				})
				return
			}

			// 确认选课
			uni.showModal({
				title: '确认选课',
				content: `确定要选择《${course.courseName}》课程吗？`,
				success: async (res) => {
					if (res.confirm) {
						try {
							const user = uni.getStorageSync('user')
							if (!user || !user.id) {
								uni.showToast({
									title: '请先登录',
									icon: 'none'
								})
								return
							}

							// 显示加载中
							uni.showLoading({
								title: '选课中...'
							})

							// 使用makeRequest方法调用后端API（使用查询参数）
							const response = await this.makeRequest(`/api/selections?studentId=${user.id}&teachingClassId=${course.teachingClassId}`, 'POST')

							uni.hideLoading()

							if (response.data && response.data.success) {
								uni.showToast({
									title: '选课成功',
									icon: 'success'
								})
								
								// 重新加载可选课程列表
								await this.loadAvailableCourses()
							} else {
								uni.showToast({
									title: response.data?.message || '选课失败',
									icon: 'none'
								})
							}
						} catch (error) {
							uni.hideLoading()
							console.error('选课失败:', error)
							
							let errorMessage = '选课失败，请重试'
							if (error.data?.message) {
								errorMessage = error.data.message
							} else if (error.statusCode === 409) {
								errorMessage = '您已选择该课程'
							} else if (error.statusCode === 400) {
								errorMessage = '课程已满或选课时间已结束'
							}
							
							uni.showToast({
								title: errorMessage,
								icon: 'none'
							})
						}
					}
				}
			})
		}
	}
}
</script>

<style lang="scss">
.select-course {
	background-color: #f5f7fa;
	min-height: 100vh;
	padding: 20rpx;
}

.search-bar {
	margin-bottom: 20rpx;
	background-color: #fff;
	border-radius: 20rpx;
	padding: 20rpx;
	box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.1);
}

.search-input {
	width: 100%;
	height: 80rpx;
	padding: 0 20rpx;
	border: 1rpx solid #e4e7ed;
	border-radius: 10rpx;
	font-size: 28rpx;
	color: #303133;
	background-color: #f5f7fa;
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
	border-top: 4rpx solid #007aff;
	border-radius: 50%;
	animation: spin 1s linear infinite;
	margin-bottom: 20rpx;
}

@keyframes spin {
	0% { transform: rotate(0deg); }
	100% { transform: rotate(360deg); }
}

.loading-text {
	font-size: 28rpx;
	color: #666;
}

.empty-state {
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	padding: 200rpx 0;
}

.empty-icon {
	font-size: 120rpx;
	margin-bottom: 20rpx;
	color: #ccc;
}

.empty-text {
	font-size: 32rpx;
	color: #999;
}

.course-list {
	display: flex;
	flex-direction: column;
	gap: 20rpx;
}

.course-item {
	background-color: #fff;
	border-radius: 20rpx;
	padding: 30rpx;
	box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.1);
	position: relative;
}

.course-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 20rpx;
}

.course-name {
	font-size: 32rpx;
	font-weight: bold;
	color: #303133;
	flex: 1;
}

.course-code {
	font-size: 24rpx;
	color: #909399;
	background-color: #f5f7fa;
	padding: 4rpx 12rpx;
	border-radius: 8rpx;
	margin-left: 20rpx;
}

.stats-bar {
	background-color: #fff;
	border-radius: 20rpx;
	padding: 20rpx;
	margin-bottom: 20rpx;
	box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.1);
	text-align: center;
}

.stats-text {
	font-size: 28rpx;
	color: #606266;
	font-weight: 500;
}

.course-info {
	display: flex;
	flex-direction: column;
	gap: 15rpx;
	margin-bottom: 20rpx;
}

.course-teacher, .course-credit {
	font-size: 28rpx;
	color: #606266;
}

.course-stats {
	display: flex;
	align-items: center;
	gap: 20rpx;
}

.course-students {
	font-size: 26rpx;
	color: #909399;
	white-space: nowrap;
}

.progress-bar {
	flex: 1;
	height: 8rpx;
	background-color: #f3f4f6;
	border-radius: 4rpx;
	overflow: hidden;
}

.progress-fill {
	height: 100%;
	background-color: #007aff;
	transition: width 0.3s ease;
	border-radius: 4rpx;
}

.progress-fill.full {
	background-color: #ff3b30;
}

.course-description {
	margin-bottom: 20rpx;
	padding: 20rpx;
	background-color: #f8f9fa;
	border-radius: 10rpx;
}

.description-text {
	font-size: 26rpx;
	color: #606266;
	line-height: 1.5;
}

.course-teacher, .course-credit, .course-students {
	font-size: 28rpx;
	color: #606266;
}

.select-button {
	width: 100%;
	height: 70rpx;
	background-color: #007aff;
	color: #fff;
	border: none;
	border-radius: 10rpx;
	font-size: 28rpx;
	font-weight: 500;
	transition: all 0.3s ease;
}

.select-button:active {
	transform: scale(0.98);
}

.select-button:disabled {
	background-color: #dcdfe6;
	color: #909399;
	cursor: not-allowed;
}

.select-button.full {
	background-color: #ff3b30;
	color: white;
}
</style>