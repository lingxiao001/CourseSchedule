<template>
	<view class="admin-courses">
		<!-- 头部操作栏 -->
		<view class="header">
			<text class="title">课程管理</text>
			<button class="add-btn" @click="openAddDialog">新增课程</button>
		</view>
		
		<!-- 课程列表 -->
		<view class="course-list" v-if="!loading">
			<view v-for="course in courses" :key="course.id" class="course-item">
				<view class="course-info">
					<text class="course-name">{{ course.courseName }}</text>
					<text class="course-code">课程代码：{{ course.classCode }}</text>
					<text class="course-desc" v-if="course.description">{{ course.description }}</text>
					<view class="course-meta">
						<text class="meta-item">学分：{{ course.credit }}</text>
						<text class="meta-item">学时：{{ course.hours }}</text>
					</view>
				</view>
				<view class="actions">
					<button class="edit-btn" @click="openEditDialog(course)">编辑</button>
					<button class="delete-btn" @click="confirmDelete(course)">删除</button>
				</view>
			</view>
			
			<!-- 空状态 -->
			<view v-if="courses.length === 0" class="empty-state">
				<text class="empty-text">暂无课程数据</text>
			</view>
		</view>
		
		<!-- 加载状态 -->
		<view v-if="loading" class="loading">
			<text class="loading-text">加载中...</text>
		</view>

		<!-- 添加/编辑课程弹窗 -->
		<view v-if="showDialog" class="dialog-overlay" @click="closeDialog">
			<view class="dialog" @click.stop>
				<view class="dialog-header">
					<text class="dialog-title">{{ isEdit ? '编辑课程' : '新增课程' }}</text>
					<button class="close-btn" @click="closeDialog">×</button>
				</view>
				
				<view class="dialog-body">
					<view class="form-item">
						<text class="label">课程名称 *</text>
						<input 
							class="input" 
							v-model="form.courseName" 
							placeholder="请输入课程名称"
							maxlength="50"
						/>
					</view>
					
					<view class="form-item">
						<text class="label">课程代码 *</text>
						<input 
							class="input" 
							v-model="form.classCode" 
							placeholder="例如：CS101"
							maxlength="20"
						/>
					</view>
					
					<view class="form-item">
						<text class="label">课程描述</text>
						<textarea 
							class="textarea" 
							v-model="form.description" 
							placeholder="请输入课程描述"
							maxlength="200"
						></textarea>
					</view>
					
					<view class="form-row">
						<view class="form-item half">
							<text class="label">学分 *</text>
							<input 
								class="input" 
								v-model="form.credit" 
								type="digit"
								placeholder="1.0"
							/>
						</view>
						
						<view class="form-item half">
							<text class="label">学时 *</text>
							<input 
								class="input" 
								v-model="form.hours" 
								type="number"
								placeholder="16"
							/>
						</view>
					</view>
				</view>
				
				<view class="dialog-footer">
					<button class="cancel-btn" @click="closeDialog">取消</button>
					<button class="confirm-btn" @click="submitForm" :disabled="submitting">
						{{ submitting ? '提交中...' : '确定' }}
					</button>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
import { adminApi } from '@/api/admin'

export default {
	data() {
		return {
			courses: [],
			loading: false,
			showDialog: false,
			isEdit: false,
			submitting: false,
			form: {
				id: null,
				courseName: '',
				classCode: '',
				description: '',
				credit: 1.0,
				hours: 16
			}
		}
	},
	
	onLoad() {
		this.fetchCourses()
	},
	
	methods: {
		// 获取课程列表
		async fetchCourses() {
			try {
				this.loading = true
				const courses = await adminApi.getCourses()
				this.courses = courses
			} catch (error) {
				console.error('获取课程列表失败:', error)
				uni.showToast({
					title: '获取课程列表失败',
					icon: 'error'
				})
			} finally {
				this.loading = false
			}
		},
		
		// 打开新增对话框
		openAddDialog() {
			this.isEdit = false
			this.resetForm()
			this.showDialog = true
		},
		
		// 打开编辑对话框
		openEditDialog(course) {
			this.isEdit = true
			this.form = { ...course }
			this.showDialog = true
		},
		
		// 关闭对话框
		closeDialog() {
			this.showDialog = false
			this.resetForm()
		},
		
		// 重置表单
		resetForm() {
			this.form = {
				id: null,
				courseName: '',
				classCode: '',
				description: '',
				credit: 1.0,
				hours: 16
			}
		},
		
		// 提交表单
		async submitForm() {
			// 验证必填字段
			if (!this.form.courseName.trim()) {
				uni.showToast({
					title: '请输入课程名称',
					icon: 'error'
				})
				return
			}
			
			if (!this.form.classCode.trim()) {
				uni.showToast({
					title: '请输入课程代码',
					icon: 'error'
				})
				return
			}
			
			if (!this.form.credit || this.form.credit <= 0) {
				uni.showToast({
					title: '请输入有效学分',
					icon: 'error'
				})
				return
			}
			
			if (!this.form.hours || this.form.hours <= 0) {
				uni.showToast({
					title: '请输入有效学时',
					icon: 'error'
				})
				return
			}
			
			try {
				this.submitting = true
				
				if (this.isEdit) {
					await adminApi.updateCourse(this.form.id, this.form)
					uni.showToast({
						title: '更新成功',
						icon: 'success'
					})
				} else {
					await adminApi.createCourse(this.form)
					uni.showToast({
						title: '添加成功',
						icon: 'success'
					})
				}
				
				this.closeDialog()
				this.fetchCourses()
				
			} catch (error) {
				console.error('操作失败:', error)
				uni.showToast({
					title: '操作失败',
					icon: 'error'
				})
			} finally {
				this.submitting = false
			}
		},
		
		// 确认删除
		confirmDelete(course) {
			uni.showModal({
				title: '确认删除',
				content: `确定要删除课程"${course.courseName}"吗？此操作不可恢复。`,
				confirmText: '删除',
				confirmColor: '#ff4757',
				success: (res) => {
					if (res.confirm) {
						this.deleteCourse(course.id)
					}
				}
			})
		},
		
		// 删除课程
		async deleteCourse(courseId) {
			try {
				await adminApi.deleteCourse(courseId)
				uni.showToast({
					title: '删除成功',
					icon: 'success'
				})
				this.fetchCourses()
			} catch (error) {
				console.error('删除失败:', error)
				uni.showToast({
					title: '删除失败',
					icon: 'error'
				})
			}
		}
	}
}
</script>

<style lang="scss">
.admin-courses {
	padding: 20px;
	background-color: #f5f5f5;
	min-height: 100vh;
}

// 头部样式
.header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 20px;
	background: white;
	padding: 15px 20px;
	border-radius: 12px;
	box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.title {
	font-size: 20px;
	font-weight: bold;
	color: #333;
}

.add-btn {
	background: #007aff;
	color: white;
	border: none;
	padding: 8px 16px;
	border-radius: 6px;
	font-size: 14px;
}

// 课程列表样式
.course-list {
	display: flex;
	flex-direction: column;
	gap: 15px;
}

.course-item {
	background: white;
	padding: 20px;
	border-radius: 12px;
	box-shadow: 0 2px 8px rgba(0,0,0,0.1);
	display: flex;
	justify-content: space-between;
	align-items: flex-start;
}

.course-info {
	flex: 1;
}

.course-name {
	font-size: 18px;
	font-weight: bold;
	color: #333;
	margin-bottom: 8px;
	display: block;
}

.course-code {
	font-size: 14px;
	color: #666;
	margin-bottom: 6px;
	display: block;
}

.course-desc {
	font-size: 14px;
	color: #888;
	margin-bottom: 8px;
	display: block;
	line-height: 1.4;
}

.course-meta {
	display: flex;
	gap: 15px;
}

.meta-item {
	font-size: 13px;
	color: #666;
	background: #f0f0f0;
	padding: 4px 8px;
	border-radius: 4px;
}

// 操作按钮样式
.actions {
	display: flex;
	flex-direction: column;
	gap: 8px;
	margin-left: 15px;
}

.edit-btn {
	background: #007aff;
	color: white;
	border: none;
	padding: 6px 12px;
	border-radius: 4px;
	font-size: 12px;
	min-width: 60px;
}

.delete-btn {
	background: #ff3b30;
	color: white;
	border: none;
	padding: 6px 12px;
	border-radius: 4px;
	font-size: 12px;
	min-width: 60px;
}

// 加载和空状态
.loading, .empty-state {
	display: flex;
	justify-content: center;
	align-items: center;
	padding: 60px 20px;
	background: white;
	border-radius: 12px;
	box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.loading-text, .empty-text {
	font-size: 16px;
	color: #999;
}

// 弹窗样式
.dialog-overlay {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	background: rgba(0,0,0,0.5);
	display: flex;
	justify-content: center;
	align-items: center;
	z-index: 1000;
}

.dialog {
	background: white;
	border-radius: 12px;
	width: 90%;
	max-width: 500px;
	max-height: 80vh;
	overflow: hidden;
}

.dialog-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 20px;
	border-bottom: 1px solid #eee;
}

.dialog-title {
	font-size: 18px;
	font-weight: bold;
	color: #333;
}

.close-btn {
	background: none;
	border: none;
	font-size: 24px;
	color: #999;
	padding: 0;
	width: 30px;
	height: 30px;
	display: flex;
	justify-content: center;
	align-items: center;
}

.dialog-body {
	padding: 20px;
	max-height: 60vh;
	overflow-y: auto;
}

.form-item {
	margin-bottom: 20px;
}

.form-item.half {
	flex: 1;
}

.form-row {
	display: flex;
	gap: 15px;
}

.label {
	display: block;
	font-size: 14px;
	color: #333;
	margin-bottom: 8px;
	font-weight: 500;
}

.input, .textarea {
	width: 100%;
	padding: 12px;
	border: 1px solid #ddd;
	border-radius: 6px;
	font-size: 14px;
	background: #fff;
	box-sizing: border-box;
}

.input:focus, .textarea:focus {
	border-color: #007aff;
	outline: none;
}

.textarea {
	height: 80px;
	resize: none;
}

.dialog-footer {
	display: flex;
	justify-content: flex-end;
	gap: 12px;
	padding: 20px;
	border-top: 1px solid #eee;
}

.cancel-btn {
	background: #f0f0f0;
	color: #333;
	border: none;
	padding: 10px 20px;
	border-radius: 6px;
	font-size: 14px;
}

.confirm-btn {
	background: #007aff;
	color: white;
	border: none;
	padding: 10px 20px;
	border-radius: 6px;
	font-size: 14px;
}

.confirm-btn:disabled {
	background: #ccc;
	cursor: not-allowed;
}
</style>