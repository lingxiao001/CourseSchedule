// 简单的认证工具类 - 不依赖Pinia
import apiClient from './request'

interface LoginData {
  username: string
  password: string
}

interface RegisterData {
  username: string
  password: string
  realName: string
  role: string
  studentId?: string
  teacherId?: string
}

interface User {
  id: number
  username: string
  realName: string
  role: string
  studentId?: string
  teacherId?: string
}

interface AuthResponse {
  token: string
  user: User
}

export class AuthManager {
  private static instance: AuthManager
  private _user: User | null = null
  private _token: string | null = null

  constructor() {
    this.initAuth()
  }

  static getInstance(): AuthManager {
    if (!AuthManager.instance) {
      AuthManager.instance = new AuthManager()
    }
    return AuthManager.instance
  }

  // 初始化认证状态
  initAuth() {
    try {
      const token = uni.getStorageSync('token')
      const userStr = uni.getStorageSync('user')
      
      if (token && userStr) {
        this._token = token
        this._user = JSON.parse(userStr)
      }
    } catch (error) {
      console.error('初始化认证状态失败:', error)
      this.clearAuth()
    }
  }

  // 登录
  async login(loginData: LoginData): Promise<void> {
    try {
      const response: AuthResponse = await apiClient.post('/auth/login', loginData)
      
      this._token = response.token
      this._user = response.user
      
      // 保存到本地存储
      uni.setStorageSync('token', response.token)
      uni.setStorageSync('user', JSON.stringify(response.user))
      
    } catch (error) {
      console.error('登录失败:', error)
      throw error
    }
  }

  // 注册
  async register(registerData: RegisterData): Promise<void> {
    try {
      const response: AuthResponse = await apiClient.post('/auth/register', registerData)
      
      this._token = response.token
      this._user = response.user
      
      // 保存到本地存储
      uni.setStorageSync('token', response.token)
      uni.setStorageSync('user', JSON.stringify(response.user))
      
    } catch (error) {
      console.error('注册失败:', error)
      throw error
    }
  }

  // 登出
  logout() {
    this.clearAuth()
  }

  // 清除认证信息
  private clearAuth() {
    this._token = null
    this._user = null
    uni.removeStorageSync('token')
    uni.removeStorageSync('user')
  }

  // 获取用户信息
  get user(): User | null {
    return this._user
  }

  // 获取token
  get token(): string | null {
    return this._token
  }

  // 是否已认证
  get isAuthenticated(): boolean {
    return !!(this._token && this._user)
  }

  // 获取用户角色
  get userRole(): string | null {
    return this._user?.role || null
  }
}

// 导出单例实例
export default AuthManager.getInstance()