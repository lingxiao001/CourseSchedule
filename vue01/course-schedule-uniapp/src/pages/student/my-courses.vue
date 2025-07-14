<template>
	<view class="my-courses-container">
		<!-- 原生导航栏 -->
		<view class="nav-bar">
			<view class="nav-left" @tap="goBack">
				<text class="nav-back">←</text>
			</view>
			<text class="nav-title">已选课程</text>
			<view class="nav-right"></view>
		</view>
		
		<!-- 统计卡片 -->
		<view class="stats-container" v-if="!loading && myCourses.length > 0">
			<view class="stat-card">
				<view class="stat-number">{{ myCourses.length }}</view>
				<view class="stat-label">已选课程</view>
			</view>
			<view class="stat-card">
				<view class="stat-number">{{ totalCredits }}</view>
				<view class="stat-label">总学分</view>
			</view>
		</view>

		<!-- 搜索栏 -->
		<view class="search-container" v-if="!loading && myCourses.length > 0">
			<view class="search-box">
				<text class="search-icon">🔍</text>
				<input 
					class="search-input" 
					v-model="searchKeyword" 
					placeholder="搜索课程名称或教师"
					@input="handleSearch"
				/>
			</view>
		</view>
		
		<!-- 下拉刷新区域 -->
		<scroll-view 
			scroll-y 
			class="scroll-container"
			refresher-enabled
			:refresher-triggered="isRefreshing"
			@refresherrefresh="onRefresh"
			@refresherrestore="onRestore"
		>
			<view v-if="loading && !isRefreshing" class="loading-container">
				<view class="loading-text">加载中...</view>
			</view>
			
			<view v-else-if="filteredCourses.length === 0" class="empty-state">
				<view class="empty-content">
					<text class="empty-icon">📚</text>
					<text class="empty-text">{{ searchKeyword ? '未找到匹配的课程' : '您还没有选择任何课程' }}</text>
				</view>
			</view>
			
			<view v-else class="course-list">
				<view 
					v-for="course in filteredCourses" 
					:key="course.teachingClassId" 
					class="course-item"
					@tap="showCourseDetail(course)"
				>
					<view class="course-info">
						<view class="course-header">
							<text class="course-name">{{ course.courseName }}</text>
							<text class="course-class">{{ course.classCode }}</text>
						</view>
						<view class="course-details">
							<view class="detail-row">
								<text class="detail-icon">👨‍🏫</text>
								<text class="teacher-name">{{ course.teacherName }}</text>
							</view>
							<view class="detail-row">
								<text class="detail-icon">⭐</text>
								<text class="credits">{{ course.credits || 0 }} 学分</text>
							</view>
						</view>
					</view>
					<view class="course-actions">
						<button 
							class="drop-button"
							type="warn"
							size="mini"
							@tap.stop="confirmDropCourse(course)"
						>
							退课
						</button>
					</view>
				</view>
			</view>
		</scroll-view>
		
		<!-- 课程详情弹窗 -->
		<view v-if="showDetailPopup" class="detail-popup" @tap="closeDetailPopup">
			<view class="popup-content" @tap.stop="">
				<view class="popup-header">
					<text class="popup-title">课程详情</text>
					<text class="popup-close" @tap="closeDetailPopup">✕</text>
				</view>
				<view class="popup-body" v-if="selectedCourse">
					<view class="detail-item">
						<text class="label">课程名称：</text>
						<text class="value">{{ selectedCourse.courseName }}</text>
					</view>
					<view class="detail-item">
						<text class="label">班级代码：</text>
						<text class="value">{{ selectedCourse.classCode }}</text>
					</view>
					<view class="detail-item">
						<text class="label">授课教师：</text>
						<text class="value">{{ selectedCourse.teacherName }}</text>
					</view>
					<view class="detail-item">
						<text class="label">学分：</text>
						<text class="value">{{ selectedCourse.credits || 0 }} 学分</text>
					</view>
				</view>
				<view class="popup-footer">
					<button 
						class="drop-button-full"
						type="warn"
						@tap="confirmDropCourse(selectedCourse)"
					>
						退课
					</button>
					<button 
						class="cancel-button"
						type="default"
						@tap="closeDetailPopup"
					>
						取消
					</button>
				</view>
			</view>
		</view>
	</view>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import { studentApi } from '@/api/student';

const myCourses = ref([]);
const loading = ref(true);
const studentId = ref(null);
const searchKeyword = ref('');
const isRefreshing = ref(false);
const showDetailPopup = ref(false);
const selectedCourse = ref(null);

// 计算属性
const filteredCourses = computed(() => {
	if (!searchKeyword.value) return myCourses.value;
	const keyword = searchKeyword.value.toLowerCase();
	return myCourses.value.filter(course => 
		course.courseName.toLowerCase().includes(keyword) ||
		course.teacherName.toLowerCase().includes(keyword) ||
		course.classCode.toLowerCase().includes(keyword)
	);
});

const totalCredits = computed(() => {
	return myCourses.value.reduce((sum, course) => sum + (course.credits || 0), 0);
});

// 获取学生ID
onMounted(() => {
	// 获取认证信息 - 使用uniapp标准存储方式
	const user = uni.getStorageSync('user');
	console.log('User info from storage:', user);
	
	if (!user || !user.roleId) {
		uni.showToast({
			title: '无法获取学生信息，请重新登录',
			icon: 'none'
		});
		loading.value = false;
		setTimeout(() => {
			uni.navigateTo({
				url: '/pages/auth/login'
			});
		}, 1500);
		return;
	}
	
	// 使用实际的用户ID作为学生ID，roleId是身份类别
	studentId.value = user.id;
	fetchSelectedCourses();
});

// 返回上一页
const goBack = () => {
	uni.navigateBack();
};

// 获取已选课程 - 匹配老Vue项目的API调用方式
const fetchSelectedCourses = async () => {
	if (!studentId.value) {
		console.log('Student ID is missing');
		return;
	}
	
	console.log('Fetching courses for student ID:', studentId.value);
	loading.value = true;
	
	try {
		const response = await studentApi.getSelectionsByStudentWithTeachers(studentId.value);
		console.log('API Response:', response);
		
		// 确保正确处理响应数据
		if (Array.isArray(response)) {
			myCourses.value = response;
		} else if (response && Array.isArray(response.data)) {
			myCourses.value = response.data;
		} else if (response && response.data) {
			myCourses.value = [response.data];
		} else {
			myCourses.value = [];
		}
		
		console.log('Loaded courses:', myCourses.value.length);
	} catch (error) {
		console.error('获取已选课程失败:', error);
		myCourses.value = [];
		uni.showToast({
			title: '获取已选课程失败',
			icon: 'none'
		});
	} finally {
		loading.value = false;
		isRefreshing.value = false;
	}
};

// 搜索处理
const handleSearch = () => {
	// 搜索逻辑由computed属性处理
};

// 下拉刷新
const onRefresh = () => {
	isRefreshing.value = true;
	fetchSelectedCourses();
};

const onRestore = () => {
	isRefreshing.value = false;
};

// 显示课程详情
const showCourseDetail = (course) => {
	selectedCourse.value = course;
	showDetailPopup.value = true;
};

// 关闭详情弹窗
const closeDetailPopup = () => {
	showDetailPopup.value = false;
	selectedCourse.value = null;
};

// 确认退课
const confirmDropCourse = (course) => {
	const courseName = course?.courseName || selectedCourse.value?.courseName;
	uni.showModal({
		title: '确认退课',
		content: `您确定要退选《${courseName}》这门课程吗？此操作不可撤销！`,
		confirmText: '确认退课',
		cancelText: '取消',
		confirmColor: '#ff4d4f',
		success: (res) => {
			if (res.confirm) {
				handleDropCourse(course || selectedCourse.value);
			}
		}
	});
};

// 处理退课
const handleDropCourse = async (course) => {
	if (!studentId.value) return;
	
	try {
		const result = await studentApi.cancelSelection(studentId.value, course.teachingClassId);
		
		if (result.success) {
			uni.showToast({
				title: '退课成功',
				icon: 'success'
			});
			
			// 关闭弹窗
			closeDetailPopup();
			
			// 重新加载课程列表
			await fetchSelectedCourses();
		} else {
			uni.showToast({
				title: `退课失败: ${result.message}`,
				icon: 'none'
			});
		}
	} catch (error) {
		console.error('退课失败:', error);
		uni.showToast({
			title: `退课失败: ${error.message || '未知错误'}`,
			icon: 'none'
		});
	}
};
</script>

<style lang="scss">
.my-courses-container {
	background-color: #f5f7fa;
	min-height: 100vh;
	padding-bottom: 30rpx;
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

.nav-title {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
}

.nav-right {
	width: 80rpx;
}

.stats-container {
	display: flex;
	justify-content: space-around;
	align-items: center;
	margin: 30rpx 30rpx 20rpx;
	gap: 20rpx;
}

.stat-card {
	background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
	border-radius: 20rpx;
	padding: 40rpx 30rpx;
	flex: 1;
	text-align: center;
	box-shadow: 0 8rpx 16rpx rgba(79, 172, 254, 0.2);
	color: white;
}

.stat-number {
	font-size: 48rpx;
	font-weight: bold;
	margin-bottom: 10rpx;
}

.stat-label {
	font-size: 24rpx;
	opacity: 0.9;
}

.search-container {
	margin: 0 30rpx 20rpx;
}

.search-box {
	background-color: #f5f5f5;
	border-radius: 30rpx;
	display: flex;
	align-items: center;
	padding: 20rpx 30rpx;
}

.search-icon {
	font-size: 32rpx;
	margin-right: 20rpx;
	color: #999;
}

.search-input {
	flex: 1;
	font-size: 28rpx;
	color: #333;
	height: 40rpx;
	border: none;
	background: transparent;
}

.scroll-container {
	flex: 1;
	height: calc(100vh - 200rpx);
}

.loading-container {
	padding: 40rpx;
	text-align: center;
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
	font-size: 28rpx;
	color: #999;
}

.course-list {
	padding: 0 30rpx;
}

.course-item {
	background-color: #ffffff;
	border-radius: 20rpx;
	padding: 30rpx;
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 20rpx;
	box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.08);
	transition: all 0.3s ease;
	
	&:active {
		transform: scale(0.98);
		box-shadow: 0 2rpx 10rpx rgba(0,0,0,0.12);
	}
}

.course-info {
	flex: 1;
}

.course-header {
	display: flex;
	align-items: center;
	margin-bottom: 16rpx;
	gap: 20rpx;
	flex-wrap: wrap;
}

.course-name {
	font-size: 32rpx;
	font-weight: bold;
	color: #333;
	flex: 1;
	min-width: 0;
}

.course-class {
	font-size: 24rpx;
	color: #666;
	background-color: #f5f5f5;
	padding: 4rpx 12rpx;
	border-radius: 12rpx;
}

.course-details {
	margin-top: 16rpx;
}

.detail-row {
	display: flex;
	align-items: center;
	margin-bottom: 8rpx;
	gap: 12rpx;
}

.detail-icon {
	font-size: 24rpx;
	color: #666;
}

.teacher-name, .credits {
	font-size: 26rpx;
	color: #666;
}

.course-actions {
	margin-left: 20rpx;
}

.drop-button {
	border-radius: 20rpx;
	font-size: 24rpx;
	padding: 10rpx 20rpx;
}

/* 详情弹窗样式 */
.detail-popup {
	position: fixed;
	top: 0;
	left: 0;
	width: 100%;
	height: 100%;
	background-color: rgba(0, 0, 0, 0.5);
	z-index: 999;
	display: flex;
	align-items: flex-end;
	justify-content: center;
}

.popup-content {
	background-color: #fff;
	border-radius: 24rpx 24rpx 0 0;
	max-height: 80vh;
	width: 100%;
	animation: slideUp 0.3s ease;
}

@keyframes slideUp {
	from {
		transform: translateY(100%);
	}
	to {
		transform: translateY(0);
	}
}

.popup-header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 40rpx 30rpx 20rpx;
	border-bottom: 1rpx solid #eee;
}

.popup-title {
	font-size: 36rpx;
	font-weight: bold;
	color: #333;
}

.popup-close {
	font-size: 40rpx;
	color: #999;
	padding: 10rpx;
}

.popup-body {
	padding: 30rpx;
}

.detail-item {
	display: flex;
	justify-content: space-between;
	align-items: center;
	padding: 20rpx 0;
	border-bottom: 1rpx solid #f5f5f5;
	
	&:last-child {
		border-bottom: none;
	}
}

.label {
	font-size: 28rpx;
	color: #666;
	flex: 1;
}

.value {
	font-size: 28rpx;
	color: #333;
	font-weight: 500;
	flex: 2;
	text-align: right;
	word-break: break-all;
}

.popup-footer {
	padding: 30rpx;
	border-top: 1rpx solid #eee;
}

.drop-button-full {
	width: 100%;
	margin-bottom: 20rpx;
	border-radius: 10rpx;
}

.cancel-button {
	width: 100%;
	border-radius: 10rpx;
	background-color: #f5f5f5;
	color: #333;
}
</style>