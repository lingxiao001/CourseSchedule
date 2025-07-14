// 临时Mock认证 - 用于测试
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

export class MockAuthManager {
  private static instance: MockAuthManager
  private _user: User | null = null
  private _token: string | null = null

  constructor() {
    this.initAuth()
  }

  static getInstance(): MockAuthManager {
    if (!MockAuthManager.instance) {
      MockAuthManager.instance = new MockAuthManager()
    }
    return MockAuthManager.instance
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

  // Mock登录
  async login(loginData: LoginData): Promise<void> {
    console.log('MockAuthManager: 开始Mock登录', loginData)
    // 模拟网络延迟
    await new Promise(resolve => setTimeout(resolve, 1000))
    
    // 简单的用户名密码验证
    const mockUsers: Record<string, User> = {
      'admin': {
        id: 1,
        username: 'admin',
        realName: '系统管理员',
        role: 'admin'
      },
      'teacher1': {
        id: 2,
        username: 'teacher1',
        realName: '张老师',
        role: 'teacher',
        teacherId: 'T001'
      },
      'student1': {
        id: 3,
        username: 'student1',
        realName: '李同学',
        role: 'student',
        studentId: 'S001'
      }
    }
    
    const user = mockUsers[loginData.username]
    if (!user || loginData.password !== '123456') {
      throw new Error('用户名或密码错误')
    }
    
    const token = `mock_token_${Date.now()}`
    
    this._token = token
    this._user = user
    
    // 保存到本地存储
    uni.setStorageSync('token', token)
    uni.setStorageSync('user', JSON.stringify(user))
    
    console.log('Mock登录成功:', user)
  }

  // Mock注册
  async register(registerData: RegisterData): Promise<void> {
    // 模拟网络延迟
    await new Promise(resolve => setTimeout(resolve, 1500))
    
    // 检查用户名是否已存在
    if (['admin', 'teacher1', 'student1'].includes(registerData.username)) {
      throw new Error('用户名已存在')
    }
    
    const user: User = {
      id: Date.now(),
      username: registerData.username,
      realName: registerData.realName,
      role: registerData.role,
      ...(registerData.role === 'student' && { studentId: registerData.studentId }),
      ...(registerData.role === 'teacher' && { teacherId: registerData.teacherId })
    }
    
    const token = `mock_token_${Date.now()}`
    
    this._token = token
    this._user = user
    
    // 保存到本地存储
    uni.setStorageSync('token', token)
    uni.setStorageSync('user', JSON.stringify(user))
    
    console.log('Mock注册成功:', user)
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
export default MockAuthManager.getInstance()