<template>
	<view class="test-container">
		<view class="basic-test">
			<text class="title">基础页面测试</text>
			<text class="description">如果你能看到这段文字，说明页面可以正常渲染</text>
			<button class="test-button" @click="handleClick">点击测试</button>
			<text v-if="clicked">按钮点击成功！</text>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			testValue: '',
			clicked: false
		}
	},
	methods: {
		handleClick() {
			this.clicked = true;
			console.log('按钮被点击了');
		}
	}
}
</script>

<style lang="scss">
.test-container {
	padding: 20px;
	background-color: #fff;
	min-height: 100vh;
}

.basic-test {
	display: flex;
	flex-direction: column;
	align-items: center;
	gap: 20px;
}

.title {
	font-size: 20px;
	font-weight: bold;
	text-align: center;
	color: #333;
	display: block;
}

.description {
	font-size: 14px;
	color: #666;
	text-align: center;
	display: block;
}

.test-button {
	background-color: #409EFF;
	color: white;
	border: none;
	padding: 10px 20px;
	border-radius: 4px;
	font-size: 14px;
}

.test-section {
	margin-bottom: 30px;
}

.section-title {
	font-size: 16px;
	font-weight: bold;
	margin-bottom: 15px;
	display: block;
}
</style>