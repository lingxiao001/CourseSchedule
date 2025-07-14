<template>
	<view style="padding: 20px; background: #f0f8ff; min-height: 100vh;">
		<text style="font-size: 24px; color: #333;">🔧 后端连接测试</text>
		
		<view style="margin: 20px 0;">
			<button 
				style="background: #007aff; color: white; padding: 15px 30px; border: none; border-radius: 8px; font-size: 16px;"
				@click="testAPI"
			>
				点击测试后端连接
			</button>
		</view>
		
		<view style="background: white; padding: 15px; border-radius: 8px; margin: 20px 0;">
			<text style="font-weight: bold; color: #333;">测试状态: </text>
			<text :style="{ color: statusColor }">{{ status }}</text>
		</view>
		
		<view style="background: white; padding: 15px; border-radius: 8px; margin: 20px 0;">
			<text style="font-weight: bold; color: #333; display: block; margin-bottom: 10px;">详细结果:</text>
			<text style="font-size: 14px; color: #666; white-space: pre-wrap;">{{ result }}</text>
		</view>
		
		<view style="background: #fff3cd; padding: 15px; border-radius: 8px; margin: 20px 0;">
			<text style="font-size: 14px; color: #856404;">
📝 说明：
• 后端服务器: localhost:8080 (通过代理访问)
• 前端服务器: localhost:5173
• 如果显示CORS错误，说明连接正常，只是跨域配置问题
• 如果显示404错误，说明连接正常，只是接口不存在
			</text>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			status: '未测试',
			result: '点击按钮开始测试...',
			statusColor: '#666'
		}
	},
	methods: {
		async testAPI() {
			this.status = '测试中...'
			this.result = '正在尝试连接后端服务器...'
			this.statusColor = '#ff9500'
			
			try {
				// 方法1: 使用uni.request直接测试
				const response = await this.directTest()
				this.status = '连接成功 ✅'
				this.result = `成功连接到后端服务器！\n\n响应数据:\n${JSON.stringify(response, null, 2)}`
				this.statusColor = '#28a745'
				
			} catch (error1) {
				try {
					// 方法2: 测试简单的健康检查
					const healthCheck = await this.healthCheck()
					this.status = '部分成功 ⚠️'
					this.result = `基础连接正常，但API可能有问题:\n\n${healthCheck}`
					this.statusColor = '#ffc107'
					
				} catch (error2) {
					// 方法3: 显示详细错误信息
					this.status = '连接失败 ❌'
					this.result = `连接失败，错误详情:\n\n错误1: ${error1.message || error1}\n\n错误2: ${error2.message || error2}\n\n可能原因:\n1. 后端服务器未启动\n2. 端口8080被占用\n3. 网络连接问题\n4. CORS跨域限制`
					this.statusColor = '#dc3545'
				}
			}
		},
		
		// 直接使用uni.request测试
		directTest() {
			return new Promise((resolve, reject) => {
				uni.request({
					url: '/api/test',
					method: 'GET',
					timeout: 5000,
					success: (res) => {
						resolve(res.data)
					},
					fail: (err) => {
						reject(err)
					}
				})
			})
		},
		
		// 健康检查
		healthCheck() {
			return new Promise((resolve, reject) => {
				uni.request({
					url: '/api/health',
					method: 'GET',
					timeout: 3000,
					success: (res) => {
						resolve(`服务器响应状态: ${res.statusCode}\n响应头: ${JSON.stringify(res.header, null, 2)}`)
					},
					fail: (err) => {
						reject(err)
					}
				})
			})
		}
	},
	
	// 页面加载时显示基本信息
	onLoad() {
		console.log('测试页面加载成功')
		this.result = `页面加载成功！\n\n当前时间: ${new Date().toLocaleString()}\n前端地址: ${window.location.href}\n后端地址: http://localhost:8080\n\n点击按钮开始测试连接...`
	}
}
</script>