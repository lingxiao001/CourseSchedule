<template>
	<view class="teaching-classes">
		<text class="title">教学班管理</text>
		<view class="class-list">
			<view v-for="classItem in classes" :key="classItem.id" class="class-item">
				<text class="class-name">{{ classItem.name }}</text>
				<text class="class-info">人数：{{ classItem.studentCount }}</text>
				<u-button size="small" @click="manageClass(classItem)">查看详情</u-button>
			</view>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			classes: [
				{ id: 1, name: '高等数学A班', studentCount: 30 },
				{ id: 2, name: '高等数学B班', studentCount: 28 }
			]
		}
	},
	methods: {
		manageClass(classItem) {
			console.log('管理教学班:', classItem)
		}
	}
}
</script>

<style lang="scss">
.teaching-classes {
	padding: 20px;
}

.title {
	font-size: 20px;
	font-weight: bold;
	margin-bottom: 20px;
	display: block;
}

.class-list {
	display: flex;
	flex-direction: column;
	gap: 15px;
}

.class-item {
	background: white;
	padding: 20px;
	border-radius: 10px;
	box-shadow: 0 2px 10px rgba(0,0,0,0.1);
	display: flex;
	flex-direction: column;
	gap: 10px;
}

.class-name {
	font-size: 16px;
	font-weight: bold;
}

.class-info {
	font-size: 14px;
	color: #666;
}
</style>