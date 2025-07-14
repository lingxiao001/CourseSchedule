import { defineStore } from 'pinia';
import { authApi, type UserInfo, type LoginCredentials, type RegisterPayload } from '@/api/auth';

interface AuthState {
  user: UserInfo | null;
  token: string | null;
  isAuthenticated: boolean;
}

export const useAuthStore = defineStore('auth', {
  state: (): AuthState => ({
    user: null,
    token: null,
    isAuthenticated: false
  }),

  getters: {
    // 获取用户角色
    userRole: (state): string => {
      return state.user?.role || '';
    },

    // 获取用户ID
    userId: (state): number | null => {
      return state.user?.id || null;
    },

    // 获取用户真实姓名
    realName: (state): string => {
      return state.user?.realName || '';
    },

    // 检查是否为管理员
    isAdmin: (state): boolean => {
      return state.user?.role === 'admin';
    },

    // 检查是否为教师
    isTeacher: (state): boolean => {
      return state.user?.role === 'teacher';
    },

    // 检查是否为学生
    isStudent: (state): boolean => {
      return state.user?.role === 'student';
    }
  },

  actions: {
    /**
     * 初始化认证状态（从本地存储恢复）
     */
    initAuth() {
      try {
        const user = uni.getStorageSync('user');
        const token = uni.getStorageSync('token');
        
        if (user && token) {
          this.user = user;
          this.token = token;
          this.isAuthenticated = true;
        }
      } catch (error) {
        console.error('初始化认证状态失败:', error);
        this.logout();
      }
    },

    /**
     * 用户登录
     */
    async login(credentials: LoginCredentials): Promise<boolean> {
      try {
        const response = await authApi.login(credentials);

        // 更新状态
        this.user = response.user;
        this.token = response.token || null;
        this.isAuthenticated = true;

        console.log('[登录成功]', this.user);
        return true;
      } catch (error) {
        console.error('[登录失败]', error);
        this.logout();
        throw error;
      }
    },

    /**
     * 用户注册
     */
    async register(payload: RegisterPayload): Promise<boolean> {
      try {
        const response = await authApi.register(payload);

        // 注册成功后自动登录
        this.user = response.user;
        this.token = response.token || null;
        this.isAuthenticated = true;

        console.log('[注册成功]', this.user);
        return true;
      } catch (error) {
        console.error('[注册失败]', error);
        this.logout();
        throw error;
      }
    },

    /**
     * 退出登录
     */
    logout() {
      // 清空状态
      this.user = null;
      this.token = null;
      this.isAuthenticated = false;

      // 清空本地存储
      authApi.logout();

      console.log('[退出登录成功]');
    },

    /**
     * 更新用户信息
     */
    updateUser(user: Partial<UserInfo>) {
      if (this.user) {
        this.user = { ...this.user, ...user };
        // 同步到本地存储
        uni.setStorageSync('user', this.user);
      }
    },

    /**
     * 检查认证状态
     */
    checkAuth(): boolean {
      const user = uni.getStorageSync('user');
      const token = uni.getStorageSync('token');
      
      if (!user || !token) {
        this.logout();
        return false;
      }

      return this.isAuthenticated;
    },

    /**
     * 强制刷新认证状态
     */
    refreshAuth() {
      this.initAuth();
    }
  }
});