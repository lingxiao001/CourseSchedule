<template>
  <view class="content">
    <view class="header">
      <text class="title">🔧 后端连接测试</text>
    </view>
    
    <button class="test-btn" @click="testConnection">{{ buttonText }}</button>
    
    <view class="result">
      <text class="status" :style="{ color: statusColor }">{{ status }}</text>
      <text class="detail">{{ detail }}</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'

const buttonText = ref('点击测试后端连接')
const status = ref('等待测试...')
const detail = ref('准备测试后端服务器连接')
const statusColor = ref('#666')

const testConnection = async () => {
  buttonText.value = '测试中...'
  status.value = '正在连接...'
  detail.value = '尝试连接 localhost:8080'
  statusColor.value = '#ff9500'
  
  try {
    // 使用uni.request直接测试
    const result = await new Promise((resolve, reject) => {
      uni.request({
        url: 'http://localhost:8080/api/auth/login',
        method: 'POST',
        data: { username: 'test', password: 'test' },
        timeout: 5000,
        success: (res) => resolve(res),
        fail: (err) => reject(err)
      })
    })
    
    status.value = '✅ 连接成功'
    detail.value = `后端服务器响应正常！\n状态码: ${(result as any).statusCode}\n这证明前后端连接正常`
    statusColor.value = '#28a745'
    
  } catch (error: any) {
    if (error.statusCode) {
      status.value = '✅ 连接成功 (有响应)'
      detail.value = `后端服务器已连接！\n状态码: ${error.statusCode}\n错误: ${error.errMsg || '正常，这只是业务逻辑错误'}`
      statusColor.value = '#28a745'
    } else {
      status.value = '❌ 连接失败'
      detail.value = `无法连接到后端服务器\n错误: ${error.errMsg || error.message || error}\n请检查后端是否在运行`
      statusColor.value = '#dc3545'
    }
  }
  
  buttonText.value = '再次测试'
}</script>

<style>
.content {
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

.test-btn {
  display: block;
  width: 200px;
  height: 50px;
  margin: 0 auto 30px;
  background: #fff;
  color: #667eea;
  border: none;
  border-radius: 25px;
  font-size: 16px;
  font-weight: 600;
  box-shadow: 0 4px 15px rgba(0,0,0,0.2);
}

.result {
  background: rgba(255, 255, 255, 0.9);
  border-radius: 15px;
  padding: 20px;
  backdrop-filter: blur(10px);
}

.status {
  display: block;
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 10px;
}

.detail {
  font-size: 14px;
  line-height: 1.6;
  color: #666;
  white-space: pre-wrap;
}
</style>
