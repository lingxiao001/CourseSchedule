<template>
	<view class="select-course">
		<view class="search-bar">
			<u-input v-model="searchText" placeholder="搜索课程" />
		</view>
		
		<view class="course-list">
			<view v-for="course in filteredCourses" :key="course.id" class="course-item">
				<text class="course-name">{{ course.name }}</text>
				<text class="course-teacher">教师：{{ course.teacher }}</text>
				<text class="course-time">时间：{{ course.time }}</text>
				<u-button size="small" type="primary" @click="selectCourse(course)">选择</u-button>
			</view>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			searchText: '',
			courses: [
				{ id: 1, name: '高等数学', teacher: '张教授', time: '周一 8:00-10:00' },
				{ id: 2, name: '大学英语', teacher: '李老师', time: '周二 10:00-12:00' }
			]
		}
	},
	computed: {
		filteredCourses() {
			return this.courses.filter(course => 
				course.name.includes(this.searchText)
			)
		}
	},
	methods: {
		selectCourse(course) {
			// TODO: 实现选课逻辑
			console.log('选课:', course)
		}
	}
}
</script>

<style lang="scss">
.select-course {
	padding: 20px;
}

.search-bar {
	margin-bottom: 20px;
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

.course-teacher, .course-time {
	font-size: 14px;
	color: #666;
	display: block;
	margin-bottom: 5px;
}
</style>