<template>
	<view class="my-courses">
		<text class="title">我的课程</text>
		<view class="course-list">
			<view v-for="course in myCourses" :key="course.id" class="course-item">
				<text class="course-name">{{ course.name }}</text>
				<text class="course-info">{{ course.teacher }} | {{ course.time }}</text>
			</view>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			myCourses: []
		}
	}
}
</script>

<style lang="scss">
.my-courses {
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
}
</style>