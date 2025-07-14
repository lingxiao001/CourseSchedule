<template>
	<view class="admin-stats">
		<text class="title">数据统计</text>
		
		<!-- 加载状态 -->
		<view v-if="loading" class="loading">
			<text class="loading-text">正在统计数据...</text>
		</view>
		
		<!-- 统计数据网格 -->
		<view v-else class="stats-grid">
			<view v-for="stat in stats" :key="stat.key" class="stat-item">
				<text class="stat-value">{{ stat.value }}</text>
				<text class="stat-label">{{ stat.label }}</text>
			</view>
		</view>
		
		<!-- 刷新按钮 -->
		<view class="refresh-section">
			<button class="refresh-btn" @click="refreshStats" :disabled="loading">
				{{ loading ? '统计中...' : '刷新统计' }}
			</button>
		</view>
		
		<!-- 详细信息 -->
		<view v-if="!loading" class="detail-section">
			<text class="section-title">详细统计</text>
			<view class="detail-item">
				<text class="detail-label">学生用户：</text>
				<text class="detail-value">{{ studentCount }} 人</text>
			</view>
			<view class="detail-item">
				<text class="detail-label">教师用户：</text>
				<text class="detail-value">{{ teacherCount }} 人</text>
			</view>
			<view class="detail-item">
				<text class="detail-label">管理员用户：</text>
				<text class="detail-value">{{ adminCount }} 人</text>
			</view>
			<view class="detail-item">
				<text class="detail-label">课程总数：</text>
				<text class="detail-value">{{ courseCount }} 门</text>
			</view>
			<view class="detail-item">
				<text class="detail-label">教学班总数：</text>
				<text class="detail-value">{{ teachingClassCount }} 个</text>
			</view>
			<view class="detail-item">
				<text class="detail-label">教室总数：</text>
				<text class="detail-value">{{ classroomCount }} 间</text>
			</view>
		</view>
	</view>
</template>

<script>
import { adminApi } from '@/api/admin'

export default {
	data() {
		return {
			loading: false,
			// 用户统计
			studentCount: 0,
			teacherCount: 0,
			adminCount: 0,
			totalUserCount: 0,
			// 课程统计
			courseCount: 0,
			teachingClassCount: 0,
			classroomCount: 0
		}
	},
	
	computed: {
		stats() {
			return [
				{ key: 'users', label: '用户总数', value: this.totalUserCount },
				{ key: 'students', label: '学生总数', value: this.studentCount },
				{ key: 'teachers', label: '教师总数', value: this.teacherCount },
				{ key: 'courses', label: '课程总数', value: this.courseCount },
				{ key: 'classes', label: '教学班总数', value: this.teachingClassCount },
				{ key: 'classrooms', label: '教室总数', value: this.classroomCount }
			]
		}
	},
	
	onLoad() {
		this.fetchStats()
	},
	
	methods: {
		// 获取所有统计数据
		async fetchStats() {
			this.loading = true
			try {
				// 并行获取所有数据
				await Promise.all([
					this.fetchUserStats(),
					this.fetchCourseStats(),
					this.fetchTeachingClassStats(),
					this.fetchClassroomStats()
				])
				
				uni.showToast({
					title: '统计完成',
					icon: 'success'
				})
			} catch (error) {
				console.error('获取统计数据失败:', error)
				uni.showToast({
					title: '统计失败',
					icon: 'error'
				})
			} finally {
				this.loading = false
			}
		},
		
		// 统计用户数据
		async fetchUserStats() {
			try {
				// 获取所有用户数据（可能需要分页获取全部）
				let allUsers = []
				let page = 0
				let hasMore = true
				
				while (hasMore) {
					const response = await adminApi.getUsers({ page, size: 100 })
					allUsers = allUsers.concat(response.content)
					page++
					hasMore = response.content.length === 100 && page < response.totalPages
				}
				
				// 统计各角色用户数量
				this.studentCount = allUsers.filter(user => user.role === 'student').length
				this.teacherCount = allUsers.filter(user => user.role === 'teacher').length
				this.adminCount = allUsers.filter(user => user.role === 'admin').length
				this.totalUserCount = allUsers.length
				
			} catch (error) {
				console.error('获取用户统计失败:', error)
				// 如果分页获取失败，尝试获取第一页数据
				try {
					const response = await adminApi.getUsers({ page: 0, size: 100 })
					this.totalUserCount = response.totalElements || response.content.length
					this.studentCount = response.content.filter(user => user.role === 'student').length
					this.teacherCount = response.content.filter(user => user.role === 'teacher').length
					this.adminCount = response.content.filter(user => user.role === 'admin').length
				} catch (fallbackError) {
					console.error('获取用户统计完全失败:', fallbackError)
				}
			}
		},
		
		// 统计课程数据
		async fetchCourseStats() {
			try {
				const courses = await adminApi.getCourses()
				this.courseCount = courses.length
			} catch (error) {
				console.error('获取课程统计失败:', error)
				this.courseCount = 0
			}
		},
		
		// 统计教学班数据
		async fetchTeachingClassStats() {
			try {
				const teachingClasses = await adminApi.getTeachingClasses()
				this.teachingClassCount = teachingClasses.length
			} catch (error) {
				console.error('获取教学班统计失败:', error)
				this.teachingClassCount = 0
			}
		},
		
		// 统计教室数据
		async fetchClassroomStats() {
			try {
				const classrooms = await adminApi.getClassrooms()
				this.classroomCount = classrooms.length
			} catch (error) {
				console.error('获取教室统计失败:', error)
				this.classroomCount = 0
			}
		},
		
		// 刷新统计数据
		refreshStats() {
			this.fetchStats()
		}
	}
}
</script>

<style lang="scss">
.admin-stats {
	padding: 20px;
	background-color: #f5f5f5;
	min-height: 100vh;
}

.title {
	font-size: 20px;
	font-weight: bold;
	margin-bottom: 20px;
	display: block;
	color: #333;
	text-align: center;
}

// 加载状态
.loading {
	display: flex;
	justify-content: center;
	align-items: center;
	padding: 60px 20px;
	background: white;
	border-radius: 12px;
	box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.loading-text {
	font-size: 16px;
	color: #999;
}

// 统计网格
.stats-grid {
	display: grid;
	grid-template-columns: repeat(2, 1fr);
	gap: 15px;
	margin-bottom: 20px;
}

.stat-item {
	background: white;
	padding: 20px;
	border-radius: 12px;
	box-shadow: 0 2px 8px rgba(0,0,0,0.1);
	text-align: center;
}

.stat-value {
	font-size: 24px;
	font-weight: bold;
	color: #007aff;
	display: block;
	margin-bottom: 8px;
}

.stat-label {
	font-size: 14px;
	color: #666;
	display: block;
}

// 刷新按钮
.refresh-section {
	margin-bottom: 20px;
	text-align: center;
}

.refresh-btn {
	background: #007aff;
	color: white;
	border: none;
	padding: 12px 24px;
	border-radius: 8px;
	font-size: 14px;
	min-width: 120px;
}

.refresh-btn:disabled {
	background: #ccc;
	cursor: not-allowed;
}

// 详细信息
.detail-section {
	background: white;
	padding: 20px;
	border-radius: 12px;
	box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.section-title {
	font-size: 16px;
	font-weight: bold;
	margin-bottom: 15px;
	display: block;
	color: #333;
	border-bottom: 2px solid #007aff;
	padding-bottom: 8px;
}

.detail-item {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 10px 0;
	border-bottom: 1px solid #f0f0f0;
}

.detail-item:last-child {
	border-bottom: none;
}

.detail-label {
	font-size: 14px;
	color: #666;
}

.detail-value {
	font-size: 14px;
	font-weight: bold;
	color: #007aff;
}
</style>