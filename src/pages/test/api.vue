<template>
	<view class="test-container">
		<view class="header">
			<text class="title">API连接测试</text>
		</view>
		
		<view class="test-section">
			<button class="test-button" @click="testConnection" :disabled="loading">
				{{ loading ? '测试中...' : '测试后端连接' }}
			</button>
		</view>
		
		<view class="result-section">
			<view class="result-title">测试结果：</view>
			<view class="result-content" :class="{ success: isSuccess, error: isError }">
				{{ resultText }}
			</view>
		</view>
		
		<view class="info-section">
			<text class="info-text">点击按钮测试是否能连接到后端服务器 (localhost:8080)</text>
		</view>
	</view>
</template>

<script setup>
import { ref } from 'vue'
import apiClient from '@/utils/request'

// 响应式数据
const loading = ref(false)
const resultText = ref('等待测试...')
const isSuccess = ref(false)
const isError = ref(false)

// 测试连接方法
const testConnection = async () => {
	loading.value = true
	isSuccess.value = false
	isError.value = false
	resultText.value = '正在连接后端...'
	
	try {
		// 测试一个简单的GET请求
		const response = await apiClient.get('/test')
		
		// 成功
		isSuccess.value = true
		resultText.value = `✅ 连接成功！\n服务器响应: ${JSON.stringify(response, null, 2)}`
		
		uni.showToast({
			title: '连接成功',
			icon: 'success'
		})
		
	} catch (error) {
		// 失败
		isError.value = true
		resultText.value = `❌ 连接失败\n错误信息: ${error.message}\n错误详情: ${JSON.stringify(error, null, 2)}`
		
		uni.showToast({
			title: '连接失败',
			icon: 'none',
			duration: 3000
		})
		
		console.error('API连接测试失败:', error)
	} finally {
		loading.value = false
	}
}
</script>

<style lang="scss">
.test-container {
	padding: 40px 20px;
	min-height: 100vh;
	background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.header {
	text-align: center;
	margin-bottom: 40px;
}

.title {
	font-size: 28px;
	font-weight: bold;
	color: #fff;
	text-shadow: 0 2px 4px rgba(0,0,0,0.3);
}

.test-section {
	text-align: center;
	margin-bottom: 30px;
}

.test-button {
	width: 200px;
	height: 50px;
	background: #fff;
	color: #667eea;
	border: none;
	border-radius: 25px;
	font-size: 16px;
	font-weight: 600;
	box-shadow: 0 4px 15px rgba(0,0,0,0.2);
	transition: all 0.3s ease;
	
	&:hover:not(:disabled) {
		transform: translateY(-2px);
		box-shadow: 0 6px 20px rgba(0,0,0,0.25);
	}
	
	&:disabled {
		opacity: 0.6;
		cursor: not-allowed;
	}
}

.result-section {
	background: rgba(255, 255, 255, 0.9);
	border-radius: 15px;
	padding: 20px;
	margin-bottom: 20px;
	backdrop-filter: blur(10px);
}

.result-title {
	font-size: 18px;
	font-weight: 600;
	color: #333;
	margin-bottom: 10px;
}

.result-content {
	font-size: 14px;
	line-height: 1.6;
	padding: 15px;
	border-radius: 10px;
	background: #f5f5f5;
	color: #666;
	white-space: pre-wrap;
	word-break: break-all;
	
	&.success {
		background: #e8f5e8;
		color: #2d8659;
		border: 1px solid #4caf50;
	}
	
	&.error {
		background: #ffeaea;
		color: #d32f2f;
		border: 1px solid #f44336;
	}
}

.info-section {
	text-align: center;
	margin-top: 30px;
}

.info-text {
	font-size: 14px;
	color: rgba(255, 255, 255, 0.8);
	line-height: 1.5;
}
</style>