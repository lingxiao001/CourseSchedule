import axios from 'axios'

const apiClient = axios.create({
  baseURL: process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080/api',
  timeout: 5000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 添加请求拦截器记录日志
apiClient.interceptors.request.use(config => {
  console.log('[API请求]', config.method.toUpperCase(), config.url)
  return config
})

export const authApi = {
  async login(credentials) {
    try {
      const response = await apiClient.post('/auth/login', {
        username: credentials.username,
        password: credentials.password
      })
      
      if (!response.data || !response.data.role) {
        throw new Error('无效的响应格式')
      }
      
      return {
        user: {
          id: response.data.userId,
          username: response.data.username,
          realName: response.data.real_name || response.data.realName,
          role: (response.data.roleType || response.data.role || '').toLowerCase(),
          roleId: response.data.roleId
        },
        token: response.data.token,
        rawData: response.data // 保留原始数据
      }
    } catch (error) {
      console.error('[登录失败]', error.response?.data || error.message)
      throw error
    }
  }
}