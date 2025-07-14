<template>
	<view class="auto-schedule">
		<text class="title">自动排课</text>
		
		<view class="config-section">
			<text class="section-title">排课配置</text>
			<view class="config-item">
				<text>学期：</text>
				<u-input v-model="config.semester" placeholder="请输入学期" />
			</view>
			<view class="config-item">
				<text>开始日期：</text>
				<u-input v-model="config.startDate" placeholder="请选择开始日期" />
			</view>
		</view>
		
		<view class="action-section">
			<u-button type="primary" @click="startAutoSchedule">开始自动排课</u-button>
		</view>
		
		<view v-if="scheduleResult" class="result-section">
			<text class="section-title">排课结果</text>
			<text>排课成功！共排课 {{ scheduleResult.totalCourses }} 门课程</text>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			config: {
				semester: '',
				startDate: ''
			},
			scheduleResult: null
		}
	},
	methods: {
		startAutoSchedule() {
			// TODO: 实现自动排课逻辑
			console.log('开始自动排课', this.config)
			this.scheduleResult = { totalCourses: 10 }
		}
	}
}
</script>

<style lang="scss">
.auto-schedule {
	padding: 20px;
}

.title {
	font-size: 20px;
	font-weight: bold;
	margin-bottom: 20px;
	display: block;
}

.config-section, .action-section, .result-section {
	background: white;
	padding: 20px;
	border-radius: 10px;
	box-shadow: 0 2px 10px rgba(0,0,0,0.1);
	margin-bottom: 20px;
}

.section-title {
	font-size: 16px;
	font-weight: bold;
	margin-bottom: 15px;
	display: block;
}

.config-item {
	display: flex;
	align-items: center;
	gap: 10px;
	margin-bottom: 15px;
	
	text {
		min-width: 80px;
	}
}

.action-section {
	text-align: center;
}
</style>