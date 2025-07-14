<template>
	<view class="manual-schedule">
		<!-- 顶部筛选栏 -->
		<view class="top-bar">
			<view class="filter-section">
				<picker 
					mode="selector" 
					:range="teachingClassOptions" 
					range-key="label"
					@change="onTeachingClassChange"
				>
					<view class="picker-input">
						<text>{{ selectedTeachingClassLabel || '选择教学班' }}</text>
						<text class="arrow">▼</text>
					</view>
				</picker>
				
				<picker 
					mode="selector" 
					:range="weekOptions" 
					range-key="label"
					@change="onWeekChange"
				>
					<view class="picker-input">
						<text>{{ selectedWeekLabel || '选择周次' }}</text>
						<text class="arrow">▼</text>
					</view>
				</picker>
			</view>
			
			<view class="action-section">
				<button class="refresh-btn" @click="loadSchedules">刷新</button>
				<button class="view-toggle-btn" @click="toggleView">
					{{ isGridView ? '列表' : '网格' }}
				</button>
			</view>
		</view>

		<!-- 网格视图 - 课表 -->
		<view v-if="isGridView" class="schedule-grid">
			<view class="grid-header">
				<view class="time-header">时间</view>
				<view v-for="day in 7" :key="day" class="day-header">
					{{ getDayLabel(day) }}
				</view>
			</view>
			
			<view v-for="(timeSlot, index) in timeSlots" :key="index" class="grid-row">
				<view class="time-cell">
					<text class="slot-number">第{{ index + 1 }}节</text>
					<text class="slot-time">{{ timeSlot.start }}-{{ timeSlot.end }}</text>
				</view>
				
				<view v-for="day in 7" :key="day" class="schedule-cell" @click="onCellClick(day, index)">
					<view v-if="getScheduleForCell(day, index)" class="schedule-content">
						<text class="classroom">{{ getScheduleForCell(day, index).building }}-{{ getScheduleForCell(day, index).classroomName }}</text>
						<text class="time">{{ getScheduleForCell(day, index).startTime }}</text>
					</view>
					<view v-else class="empty-cell">
						<text class="plus-icon">+</text>
					</view>
				</view>
			</view>
		</view>

		<!-- 列表视图 -->
		<view v-else class="schedule-list">
			<view v-if="schedules.length === 0" class="empty-state">
				<text class="empty-text">暂无课程安排</text>
				<button class="add-btn" @click="openAddDialog">添加安排</button>
			</view>
			
			<view v-else>
				<view v-for="schedule in schedules" :key="schedule.id" class="schedule-item">
					<view class="schedule-info">
						<view class="schedule-header">
							<text class="day">星期{{ getDayLabel(schedule.dayOfWeek) }}</text>
							<text class="time">{{ schedule.startTime }}-{{ schedule.endTime }}</text>
						</view>
						<view class="schedule-detail">
							<text class="classroom">教室：{{ schedule.building }}-{{ schedule.classroomName }}</text>
						</view>
					</view>
					<view class="schedule-actions">
						<button class="edit-btn" @click="editSchedule(schedule)">编辑</button>
						<button class="delete-btn" @click="deleteSchedule(schedule)">删除</button>
					</view>
				</view>
			</view>
		</view>

		<!-- 添加/编辑弹窗 -->
		<view v-if="showDialog" class="dialog-overlay" @click="closeDialog">
			<view class="dialog" @click.stop>
				<view class="dialog-header">
					<text class="dialog-title">{{ isEdit ? '编辑课程安排' : '添加课程安排' }}</text>
					<button class="close-btn" @click="closeDialog">×</button>
				</view>
				
				<view class="dialog-body">
					<view class="form-item">
						<text class="label">星期 *</text>
						<uni-data-select
					v-model="form.dayOfWeek"
					:localdata="dayOptions"
					@change="onDayChange"
					label="label"
					value="value"
				></uni-data-select>
					</view>
					
					<view class="form-item">
						<text class="label">时间段 *</text>
						<uni-data-select
					v-model="form.timeSlotIndex"
					:localdata="timeSlotOptions"
					@change="onTimeSlotChange"
					label="label"
					value="value"
				></uni-data-select>
					</view>
					
					<view class="form-item">
						<text class="label">教室 *</text>
						<uni-data-select
					v-model="form.classroomId"
					:localdata="classroomOptions"
					@change="onClassroomChange"
					label="label"
					value="value"
					:disabled="classroomOptions.length === 0"
				></uni-data-select>
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

		<!-- 加载状态 -->
		<view v-if="loading" class="loading-overlay">
			<text class="loading-text">加载中...</text>
		</view>
	</view>
</template>

<script>
import { adminApi } from '@/api/admin'

export default {
	data() {
		return {
			loading: false,
			isGridView: true,
			
			// 数据
			teachingClasses: [],
			schedules: [],
			classrooms: [],
			
			// 选择状态
			selectedTeachingClass: null,
			selectedWeek: 1,
			
			// 时间段配置
			timeSlots: [
				{ start: '08:00', end: '08:45' },
				{ start: '08:55', end: '09:40' },
				{ start: '10:00', end: '10:45' },
				{ start: '14:00', end: '14:45' },
				{ start: '15:00', end: '15:45' }
			],
			
			// 网格数据 - grid[timeSlotIndex][dayOfWeek]
			grid: {},
			
			// 弹窗状态
			showDialog: false,
			isEdit: false,
			submitting: false,
			currentSchedule: null,
			
			// 表单数据
			form: {
				dayOfWeek: 1,
				timeSlotIndex: 0,
				classroomId: null,
				classroomIndex: 0,
				startTime: '',
				endTime: ''
			}
		}
	},
	
	computed: {
		teachingClassOptions() {
			return this.teachingClasses.map(tc => ({
				value: tc.id,
				label: tc.classCode || `教学班${tc.id}`
			}))
		},
		
		selectedTeachingClassLabel() {
			const selected = this.teachingClassOptions.find(option => option.value === this.selectedTeachingClass)
			return selected?.label
		},
		
		weekOptions() {
			return Array.from({ length: 20 }, (_, i) => ({
				value: i + 1,
				label: `第${i + 1}周`
			}))
		},
		
		selectedWeekLabel() {
			return `第${this.selectedWeek}周`
		},
		
		dayOptions() {
			return [
				{ value: 1, text: '星期一' },
				{ value: 2, text: '星期二' },
				{ value: 3, text: '星期三' },
				{ value: 4, text: '星期四' },
				{ value: 5, text: '星期五' },
				{ value: 6, text: '星期六' },
				{ value: 7, text: '星期日' }
			]
		},
		
		timeSlotOptions() {
			return this.timeSlots.map((slot, index) => ({
				value: index,
				text: `第${index + 1}节 (${slot.start}-${slot.end})`
			}))
		},
		
		classroomOptions() {
			return this.classrooms.map(classroom => ({
				value: classroom.id,
				text: `${classroom.building || '教学楼'}-${classroom.classroomName || '未命名教室'}`
			}))
		}
	},
	
	onLoad() {
		this.initData()
	},
	
	methods: {
		// 初始化数据
		async initData() {
			this.loading = true
			try {
				await Promise.all([
					this.loadTeachingClasses(),
					this.loadClassrooms()
				])
			} catch (error) {
				console.error('初始化数据失败:', error)
				uni.showToast({
					title: '初始化失败',
					icon: 'error'
				})
			} finally {
				this.loading = false
			}
		},
		
		// 加载教学班列表
		async loadTeachingClasses() {
			try {
				const teachingClasses = await adminApi.getTeachingClasses()
				this.teachingClasses = teachingClasses || []
				
				// 默认选择第一个教学班
				if (this.teachingClasses.length > 0) {
					this.selectedTeachingClass = this.teachingClasses[0].id
					this.loadSchedules()
				}
			} catch (error) {
				console.error('加载教学班失败:', error)
			}
		},
		
		// 加载教室列表
		async loadClassrooms() {
			try {
				const classrooms = await adminApi.getClassrooms()
				this.classrooms = classrooms || []
				console.log('加载的教室数据:', this.classrooms)
				console.log('教室选项:', this.classroomOptions)
				
				// 添加nextTick确保组件更新
				this.$nextTick(() => {
					// 可以在这里添加一些确保组件刷新的代码
					// 例如临时修改一个不相关的状态变量
					this.forceUpdate = !this.forceUpdate
				})
			} catch (error) {
				console.error('加载教室失败:', error)
				uni.showToast({
					title: '加载教室失败',
					icon: 'error'
				})
			}
		},
		
		// 加载课程安排
		async loadSchedules() {
			if (!this.selectedTeachingClass) return
			
			this.loading = true
			try {
				// 这里需要根据实际API调整
				const schedules = await this.getSchedulesByTeachingClass(this.selectedTeachingClass)
				this.schedules = schedules || []
				this.buildGrid()
				
				uni.showToast({
					title: '加载完成',
					icon: 'success'
				})
			} catch (error) {
				console.error('加载课程安排失败:', error)
				uni.showToast({
					title: '加载失败',
					icon: 'error'
				})
			} finally {
				this.loading = false
			}
		},
		
		// 构建网格数据
		buildGrid() {
			this.grid = {}
			
			// 初始化网格
			for (let timeIndex = 0; timeIndex < this.timeSlots.length; timeIndex++) {
				this.grid[timeIndex] = {}
				for (let day = 1; day <= 7; day++) {
					this.grid[timeIndex][day] = null
				}
			}
			
			// 填充数据
			this.schedules.forEach(schedule => {
				const timeIndex = this.getTimeSlotIndex(schedule.startTime)
				if (timeIndex >= 0) {
					this.grid[timeIndex][schedule.dayOfWeek] = schedule
				}
			})
		},
		
		// 获取时间段索引
		getTimeSlotIndex(startTime) {
			return this.timeSlots.findIndex(slot => slot.start === startTime)
		},
		
		// 获取指定单元格的课程安排
		getScheduleForCell(day, timeIndex) {
			return this.grid[timeIndex] && this.grid[timeIndex][day]
		},
		
		// 获取星期标签
		getDayLabel(day) {
			const labels = ['', '一', '二', '三', '四', '五', '六', '日']
			return labels[day] || day
		},
		
		// 切换视图
		toggleView() {
			this.isGridView = !this.isGridView
		},
		
		// 事件处理
		onTeachingClassChange(e) {
			const index = e.detail.value
			this.selectedTeachingClass = this.teachingClassOptions[index].value
			this.loadSchedules()
		},
		
		onWeekChange(e) {
			const index = e.detail.value
			this.selectedWeek = this.weekOptions[index].value
		},
		
		onDayChange(value) {
			// uni-data-select直接返回选中的值
			this.form.dayOfWeek = value;
		},
		
		onTimeSlotChange(value) {
			this.form.timeSlotIndex = value;
			const timeSlot = this.timeSlots[value];
			this.form.startTime = timeSlot.start;
			this.form.endTime = timeSlot.end;
		},
		
		onClassroomChange(value) {
			this.form.classroomId = value;
			const classroomIndex = this.classroomOptions.findIndex(option => option.value === value);
			this.form.classroomIndex = classroomIndex >= 0 ? classroomIndex : 0;
			
			// 用户反馈
			if (classroomIndex >= 0) {
				uni.showToast({
					title: `已选择: ${this.classroomOptions[classroomIndex].label}`,
					icon: 'none'
				});
			}
		},
		
		// 单元格点击
		onCellClick(day, timeIndex) {
			const existingSchedule = this.getScheduleForCell(day, timeIndex)
			
			if (existingSchedule) {
				// 编辑现有安排
				this.editSchedule(existingSchedule)
			} else {
				// 添加新安排
				this.openAddDialog(day, timeIndex)
			}
		},
		
		// 打开添加对话框
		openAddDialog(day = 1, timeIndex = 0) {
			this.isEdit = false;
			this.currentSchedule = null;
			
			// 确保教室选项已加载
			if (this.classrooms.length === 0) {
				this.loadClassrooms();
			}
			
			// 设置默认选中第一个教室（如果有）
			const defaultClassroomId = this.classroomOptions.length > 0 ? this.classroomOptions[0].value : null;
			
			this.form = {
				dayOfWeek: day,
				timeSlotIndex: timeIndex,
				classroomId: defaultClassroomId,
				classroomIndex: 0,
				startTime: this.timeSlots[timeIndex].start,
				endTime: this.timeSlots[timeIndex].end
			};
			this.showDialog = true;
		},
		
		// 编辑课程安排
		editSchedule(schedule) {
			this.isEdit = true
			this.currentSchedule = schedule
			
			const timeIndex = this.getTimeSlotIndex(schedule.startTime)
			const classroom = this.classrooms.find(c => 
				c.building === schedule.building && c.classroomName === schedule.classroomName
			)
			const classroomIndex = this.classroomOptions.findIndex(option => option.value === classroom?.id)
			
			this.form = {
				dayOfWeek: schedule.dayOfWeek,
				timeSlotIndex: timeIndex >= 0 ? timeIndex : 0,
				classroomId: classroom?.id || null,
				classroomIndex: classroomIndex >= 0 ? classroomIndex : 0,
				startTime: schedule.startTime,
				endTime: schedule.endTime
			}
			this.showDialog = true
		},
		
		// 删除课程安排
		deleteSchedule(schedule) {
			uni.showModal({
				title: '确认删除',
				content: `确定要删除星期${this.getDayLabel(schedule.dayOfWeek)} ${schedule.startTime}的课程安排吗？`,
				confirmText: '删除',
				confirmColor: '#ff4757',
				success: async (res) => {
					if (res.confirm) {
						try {
							await this.deleteScheduleApi(schedule.id)
							uni.showToast({
								title: '删除成功',
								icon: 'success'
							})
							this.loadSchedules()
						} catch (error) {
							console.error('删除失败:', error)
							uni.showToast({
								title: '删除失败',
								icon: 'error'
							})
						}
					}
				}
			})
		},
		
		// 关闭对话框
		closeDialog() {
			this.showDialog = false
			this.isEdit = false
			this.currentSchedule = null
		},
		
		// 提交表单
		async submitForm() {
			// 验证表单
			if (!this.form.classroomId) {
				uni.showToast({
					title: '请选择教室',
					icon: 'error'
				})
				return
			}
			
			if (!this.selectedTeachingClass) {
				uni.showToast({
					title: '请选择教学班',
					icon: 'error'
				})
				return
			}
			
			this.submitting = true
			try {
				const classroom = this.classrooms.find(c => c.id === this.form.classroomId)
				const payload = {
					dayOfWeek: this.form.dayOfWeek,
					startTime: this.form.startTime,
					endTime: this.form.endTime,
					classroomId: this.form.classroomId,
					building: classroom?.building || '',
					classroomName: classroom?.classroomName || '',
					week: this.selectedWeek
				}
				
				// 如果是新增，检查冲突
				if (!this.isEdit) {
					const conflictResult = await this.checkConflict(payload)
					if (conflictResult.hasConflict && conflictResult.conflicts.length > 0) {
						const conflictInfo = conflictResult.conflicts.map(c => 
							`星期${this.getDayLabel(c.dayOfWeek)} ${c.startTime}-${c.endTime}`
						).join(', ')
						
						const confirmed = await this.showConfirmDialog(
							'课程冲突',
							`发现时间冲突：${conflictInfo}。是否继续添加？`
						)
						
						if (!confirmed) {
							this.submitting = false
							return
						}
					}
				}
				
				if (this.isEdit && this.currentSchedule) {
					await this.updateScheduleApi(this.currentSchedule.id, payload)
					uni.showToast({
						title: '更新成功',
						icon: 'success'
					})
				} else {
					await this.addScheduleApi(this.selectedTeachingClass, payload)
					uni.showToast({
						title: '添加成功',
						icon: 'success'
					})
				}
				
				this.closeDialog()
				this.loadSchedules()
				
			} catch (error) {
				console.error('提交失败:', error)
				uni.showToast({
					title: '操作失败',
					icon: 'error'
				})
			} finally {
				this.submitting = false
			}
		},
		
		// 显示确认对话框
		showConfirmDialog(title, content) {
			return new Promise((resolve) => {
				uni.showModal({
					title,
					content,
					confirmText: '继续',
					cancelText: '取消',
					success: (res) => {
						resolve(res.confirm)
					}
				})
			})
		},
		
		// 获取教室索引
		getClassroomIndex() {
			const index = this.classroomOptions.findIndex(option => option.value === this.form.classroomId)
			return index >= 0 ? index : 0
		},
		
		// 获取选中教室标签
		getSelectedClassroomLabel() {
			if (this.form.classroomIndex >= 0 && this.classroomOptions[this.form.classroomIndex]) {
				return this.classroomOptions[this.form.classroomIndex].label
			}
			return '请选择教室'
		},
		
		// API 方法
		async getSchedulesByTeachingClass(teachingClassId) {
			try {
				const schedules = await adminApi.getSchedulesByTeachingClass(teachingClassId, {
					week: this.selectedWeek
				})
				return schedules || []
			} catch (error) {
				console.error('获取课程安排失败:', error)
				return []
			}
		},
		
		async addScheduleApi(teachingClassId, payload) {
			try {
				await adminApi.addSchedule(teachingClassId, payload)
			} catch (error) {
				console.error('添加课程安排失败:', error)
				throw error
			}
		},
		
		async updateScheduleApi(scheduleId, payload) {
			try {
				await adminApi.updateSchedule(scheduleId, payload)
			} catch (error) {
				console.error('更新课程安排失败:', error)
				throw error
			}
		},
		
		async deleteScheduleApi(scheduleId) {
			try {
				await adminApi.deleteSchedule(scheduleId)
			} catch (error) {
				console.error('删除课程安排失败:', error)
				throw error
			}
		},
		
		// 检查课程安排冲突
		async checkConflict(payload) {
			try {
				const result = await adminApi.checkScheduleConflict(payload)
				return result
			} catch (error) {
				console.error('检查冲突失败:', error)
				return { hasConflict: false, conflicts: [] }
			}
		}
	}
}
</script>

<style lang="scss">
.manual-schedule {
	padding: 20px;
	background-color: #f5f5f5;
	min-height: 100vh;
}

// 顶部筛选栏
.top-bar {
	background: white;
	padding: 15px;
	border-radius: 12px;
	margin-bottom: 20px;
	box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.filter-section {
	display: flex;
	gap: 10px;
	margin-bottom: 15px;
}

.action-section {
	display: flex;
	gap: 10px;
	justify-content: flex-end;
}

.picker-input {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 10px 15px;
	background: #f8f9fa;
	border-radius: 6px;
	border: 1px solid #e9ecef;
	flex: 1;
	cursor: pointer;
	user-select: none;
}

.arrow {
	color: #666;
	font-size: 12px;
}

.picker-input.disabled {
	background: #f5f5f5;
	color: #999;
	cursor: not-allowed;
}

.refresh-btn, .view-toggle-btn {
	background: #007aff;
	color: white;
	border: none;
	padding: 8px 16px;
	border-radius: 6px;
	font-size: 14px;
}

// 网格视图
.schedule-grid {
	background: white;
	border-radius: 12px;
	overflow: hidden;
	box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.grid-header {
	display: flex;
	background: #f8f9fa;
	border-bottom: 2px solid #007aff;
}

.time-header, .day-header {
	flex: 1;
	padding: 15px 10px;
	text-align: center;
	font-weight: bold;
	color: #333;
	border-right: 1px solid #eee;
}

.time-header {
	flex: 0.8;
}

.grid-row {
	display: flex;
	border-bottom: 1px solid #eee;
}

.time-cell {
	flex: 0.8;
	padding: 15px 10px;
	border-right: 1px solid #eee;
	background: #f8f9fa;
	display: flex;
	flex-direction: column;
	align-items: center;
}

.slot-number {
	font-weight: bold;
	color: #333;
	font-size: 14px;
}

.slot-time {
	font-size: 12px;
	color: #666;
	margin-top: 4px;
}

.schedule-cell {
	flex: 1;
	min-height: 80px;
	border-right: 1px solid #eee;
	display: flex;
	align-items: center;
	justify-content: center;
	cursor: pointer;
	transition: background-color 0.2s;
}

.schedule-cell:hover {
	background-color: #f8f9fa;
}

.schedule-content {
	text-align: center;
	padding: 8px;
	background: #e3f2fd;
	border-radius: 6px;
	width: 90%;
}

.classroom {
	display: block;
	font-size: 12px;
	font-weight: bold;
	color: #1976d2;
	margin-bottom: 4px;
}

.time {
	display: block;
	font-size: 11px;
	color: #666;
}

.empty-cell {
	display: flex;
	align-items: center;
	justify-content: center;
	width: 100%;
	height: 100%;
}

.plus-icon {
	font-size: 24px;
	color: #ccc;
}

// 列表视图
.schedule-list {
	background: white;
	border-radius: 12px;
	padding: 20px;
	box-shadow: 0 2px 8px rgba(0,0,0,0.1);
}

.empty-state {
	text-align: center;
	padding: 60px 20px;
}

.empty-text {
	display: block;
	font-size: 16px;
	color: #999;
	margin-bottom: 20px;
}

.add-btn {
	background: #007aff;
	color: white;
	border: none;
	padding: 12px 24px;
	border-radius: 8px;
	font-size: 14px;
}

.schedule-item {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 15px;
	border-bottom: 1px solid #f0f0f0;
}

.schedule-item:last-child {
	border-bottom: none;
}

.schedule-info {
	flex: 1;
}

.schedule-header {
	display: flex;
	gap: 15px;
	margin-bottom: 5px;
}

.day {
	font-weight: bold;
	color: #007aff;
}

.schedule-detail {
	font-size: 14px;
	color: #666;
}

.schedule-actions {
	display: flex;
	gap: 10px;
}

.edit-btn {
	background: #007aff;
	color: white;
	border: none;
	padding: 6px 12px;
	border-radius: 4px;
	font-size: 12px;
}

.delete-btn {
	background: #ff3b30;
	color: white;
	border: none;
	padding: 6px 12px;
	border-radius: 4px;
	font-size: 12px;
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

.label {
	display: block;
	font-size: 14px;
	color: #333;
	margin-bottom: 8px;
	font-weight: 500;
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

// 加载状态
.loading-overlay {
	position: fixed;
	top: 0;
	left: 0;
	right: 0;
	bottom: 0;
	background: rgba(0,0,0,0.3);
	display: flex;
	justify-content: center;
	align-items: center;
	z-index: 999;
}

.loading-text {
	background: white;
	padding: 20px 40px;
	border-radius: 8px;
	color: #333;
}
</style>