import axios from 'axios'

const apiClient = axios.create({
  baseURL: process.env.VUE_APP_API_BASE_URL || '/api',
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
      const msg = error.response?.data?.detail || error.response?.data?.message || error.message
      console.error('[登录失败]', msg)
      error.parsedMessage = msg
      throw error
    }
  },
  async register(payload) {
    try {
      const response = await apiClient.post('/auth/register', payload)

      // 后端返回的字段在登录和注册接口保持一致
      return {
        user: {
          id: response.data.userId,
          username: response.data.username,
          realName: response.data.real_name || response.data.realName,
          role: (response.data.roleType || response.data.role || '').toLowerCase(),
          roleId: response.data.roleId
        },
        token: response.data.token, // 如果后端未来补充 JWT，可提前保留
        rawData: response.data
      }
    } catch (error) {
      const msg = error.response?.data?.detail || error.response?.data?.message || error.message
      console.error('[注册失败]', msg)
      error.parsedMessage = msg
      throw error
    }
  }
}