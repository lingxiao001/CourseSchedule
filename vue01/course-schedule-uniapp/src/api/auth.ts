import apiClient from '@/utils/request';

export interface LoginCredentials {
  username: string;
  password: string;
}

export interface RegisterPayload {
  username: string;
  password: string;
  realName: string;
  role: 'student' | 'teacher';
  studentId?: number;
  teacherId?: number;
  grade?: string;
  className?: string;
  title?: string;
  department?: string;
}

export interface UserInfo {
  id: number;
  username: string;
  realName: string;
  role: string;
  roleId?: number;
  roleType?: string;
}

export interface AuthResponse {
  user: UserInfo;
  token?: string;
  rawData?: any;
}

export const authApi = {
  /**
   * 用户登录
   */
  async login(credentials: LoginCredentials): Promise<AuthResponse> {
    try {
      const response = await apiClient.post('/auth/login', {
        username: credentials.username,
        password: credentials.password
      });
      
      if (!response || (!response.role && !response.roleType)) {
        throw new Error('无效的响应格式');
      }
      
      const authResponse: AuthResponse = {
        user: {
          id: response.userId,
          username: response.username,
          realName: response.real_name || response.realName,
          role: (response.roleType || response.role || '').toLowerCase(),
          roleId: response.roleId,
          roleType: response.roleType
        },
        token: response.token,
        rawData: response
      };

      // 存储用户信息到本地
      // 后端使用session认证，生成一个假token用于本地状态管理
      const sessionToken = `session_${Date.now()}_${response.userId}`;
      authResponse.token = sessionToken;
      
      uni.setStorageSync('token', sessionToken);
      uni.setStorageSync('user', authResponse.user);

      return authResponse;
    } catch (error: any) {
      const msg = error.data?.detail || error.data?.message || error.message || '登录失败';
      console.error('[登录失败]', msg, error);
      
      // 统一错误处理
      const enhancedError = new Error(msg);
      (enhancedError as any).parsedMessage = msg;
      (enhancedError as any).originalError = error;
      throw enhancedError;
    }
  },

  /**
   * 用户注册
   */
  async register(payload: RegisterPayload): Promise<AuthResponse> {
    try {
      const response = await apiClient.post('/auth/register', payload);

      const authResponse: AuthResponse = {
        user: {
          id: response.userId,
          username: response.username,
          realName: response.real_name || response.realName,
          role: (response.roleType || response.role || '').toLowerCase(),
          roleId: response.roleId,
          roleType: response.roleType
        },
        token: response.token,
        rawData: response
      };

      // 存储用户信息到本地
      // 后端使用session认证，生成一个假token用于本地状态管理
      const sessionToken = `session_${Date.now()}_${response.userId}`;
      authResponse.token = sessionToken;
      
      uni.setStorageSync('token', sessionToken);
      uni.setStorageSync('user', authResponse.user);

      return authResponse;
    } catch (error: any) {
      const msg = error.data?.detail || error.data?.message || error.message || '注册失败';
      console.error('[注册失败]', msg, error);
      
      const enhancedError = new Error(msg);
      (enhancedError as any).parsedMessage = msg;
      (enhancedError as any).originalError = error;
      throw enhancedError;
    }
  },

  /**
   * 退出登录
   */
  logout(): void {
    uni.removeStorageSync('token');
    uni.removeStorageSync('user');
  },

  /**
   * 获取本地存储的用户信息
   */
  getCurrentUser(): UserInfo | null {
    try {
      const user = uni.getStorageSync('user');
      return user || null;
    } catch {
      return null;
    }
  },

  /**
   * 检查是否已登录
   */
  isAuthenticated(): boolean {
    const user = this.getCurrentUser();
    const token = uni.getStorageSync('token');
    return !!(user && token);
  }
};