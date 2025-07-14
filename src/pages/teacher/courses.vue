<template>
	<view class="teacher-courses">
		<text class="title">课程管理</text>
		<view class="course-list">
			<view v-for="course in courses" :key="course.id" class="course-item">
				<text class="course-name">{{ course.name }}</text>
				<text class="course-info">学生数：{{ course.studentCount }}</text>
				<u-button size="small" @click="manageCourse(course)">管理</u-button>
			</view>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			courses: [
				{ id: 1, name: '高等数学', studentCount: 45 },
				{ id: 2, name: '线性代数', studentCount: 38 }
			]
		}
	},
	methods: {
		manageCourse(course) {
			console.log('管理课程:', course)
		}
	}
}
</script>

<style lang="scss">
.teacher-courses {
	padding: 20px;
}

.title {
	font-size: 20px;
	font-weight: bold;
	margin-bottom: 20px;
	display: block;
}

.course-list {
	display: flex;
	flex-direction: column;
	gap: 15px;
}

.course-item {
	background: white;
	padding: 20px;
	border-radius: 10px;
	box-shadow: 0 2px 10px rgba(0,0,0,0.1);
	display: flex;
	flex-direction: column;
	gap: 10px;
}

.course-name {
	font-size: 16px;
	font-weight: bold;
}

.course-info {
	font-size: 14px;
	color: #666;
}
</style>