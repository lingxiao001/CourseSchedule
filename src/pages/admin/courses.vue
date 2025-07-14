<template>
	<view class="admin-courses">
		<view class="header">
			<text class="title">课程管理</text>
			<u-button size="small" type="primary" @click="addCourse">新增课程</u-button>
		</view>
		
		<view class="course-list">
			<view v-for="course in courses" :key="course.id" class="course-item">
				<text class="course-name">{{ course.name }}</text>
				<text class="course-info">教师：{{ course.teacher }} | 学分：{{ course.credits }}</text>
				<view class="actions">
					<u-button size="mini" @click="editCourse(course)">编辑</u-button>
					<u-button size="mini" type="error" @click="deleteCourse(course)">删除</u-button>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			courses: [
				{ id: 1, name: '高等数学', teacher: '张教授', credits: 4 },
				{ id: 2, name: '大学英语', teacher: '李老师', credits: 3 }
			]
		}
	},
	methods: {
		addCourse() {
			console.log('新增课程')
		},
		editCourse(course) {
			console.log('编辑课程:', course)
		},
		deleteCourse(course) {
			console.log('删除课程:', course)
		}
	}
}
</script>

<style lang="scss">
.admin-courses {
	padding: 20px;
}

.header {
	display: flex;
	justify-content: space-between;
	align-items: center;
	margin-bottom: 20px;
}

.title {
	font-size: 20px;
	font-weight: bold;
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
}

.course-name {
	font-size: 16px;
	font-weight: bold;
	display: block;
	margin-bottom: 5px;
}

.course-info {
	font-size: 14px;
	color: #666;
	display: block;
	margin-bottom: 10px;
}

.actions {
	display: flex;
	gap: 10px;
}
</style>