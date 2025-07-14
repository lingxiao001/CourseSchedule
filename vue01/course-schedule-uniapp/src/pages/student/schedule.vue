<template>
	<view class="schedule">
		<text class="title">我的课表</text>
		<view class="schedule-table">
			<!-- TODO: 实现课表组件 -->
			<text>课表功能开发中...</text>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			scheduleData: []
		}
	}
}
</script>

<style lang="scss">
.schedule {
	padding: 20px;
}

.title {
	font-size: 20px;
	font-weight: bold;
	margin-bottom: 20px;
	display: block;
}

.schedule-table {
	background: white;
	padding: 20px;
	border-radius: 10px;
	box-shadow: 0 2px 10px rgba(0,0,0,0.1);
	text-align: center;
}
</style>