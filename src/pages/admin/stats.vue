<template>
	<view class="admin-stats">
		<text class="title">数据统计</text>
		
		<view class="stats-grid">
			<view v-for="stat in stats" :key="stat.key" class="stat-item">
				<text class="stat-value">{{ stat.value }}</text>
				<text class="stat-label">{{ stat.label }}</text>
			</view>
		</view>
		
		<view class="chart-section">
			<text class="section-title">课程统计图表</text>
			<text>图表组件开发中...</text>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			stats: [
				{ key: 'students', label: '学生总数', value: 1258 },
				{ key: 'teachers', label: '教师总数', value: 89 },
				{ key: 'courses', label: '课程总数', value: 156 },
				{ key: 'classes', label: '教学班总数', value: 234 }
			]
		}
	}
}
</script>

<style lang="scss">
.admin-stats {
	padding: 20px;
}

.title {
	font-size: 20px;
	font-weight: bold;
	margin-bottom: 20px;
	display: block;
}

.stats-grid {
	display: grid;
	grid-template-columns: repeat(2, 1fr);
	gap: 15px;
	margin-bottom: 20px;
}

.stat-item {
	background: white;
	padding: 20px;
	border-radius: 10px;
	box-shadow: 0 2px 10px rgba(0,0,0,0.1);
	text-align: center;
}

.stat-value {
	font-size: 24px;
	font-weight: bold;
	color: #409EFF;
	display: block;
	margin-bottom: 5px;
}

.stat-label {
	font-size: 14px;
	color: #666;
	display: block;
}

.chart-section {
	background: white;
	padding: 20px;
	border-radius: 10px;
	box-shadow: 0 2px 10px rgba(0,0,0,0.1);
	text-align: center;
}

.section-title {
	font-size: 16px;
	font-weight: bold;
	margin-bottom: 15px;
	display: block;
}
</style>