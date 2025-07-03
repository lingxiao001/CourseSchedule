import { defineStore } from 'pinia';
import { authApi } from '@/api/auth';

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: JSON.parse(localStorage.getItem('user') || 'null'),
    token: (localStorage.getItem('token') && localStorage.getItem('token') !== 'null') ? localStorage.getItem('token') : null,
    isAuthenticated: !!localStorage.getItem('user')
  }),
  actions: {
    async login(credentials) {
      try {
        const response = await authApi.login(credentials);

        // 字段映射（根据后端实际返回字段调整）
        this.user = {
          id: response.user.id,
          username: response.user.username,
          realName: response.user.realName, 
          role: response.user.role.toLowerCase(),
          roleId: response.user.roleId
        };

        this.token = response.token;
        this.isAuthenticated = true;
        if (this.token) {
          localStorage.setItem('token', this.token);
        } else {
          localStorage.removeItem('token');
        }
        localStorage.setItem('user', JSON.stringify(this.user));
        return true;
      } catch (error) {
        this.logout();
        throw error;
      }
    },
    async register(payload) {
      try {
        const response = await authApi.register(payload)

        // 成功后，同步登录逻辑
        this.user = {
          id: response.user.id,
          username: response.user.username,
          realName: response.user.realName,
          role: response.user.role.toLowerCase(),
          roleId: response.user.roleId
        }
        this.token = response.token
        this.isAuthenticated = true
        if (this.token) {
          localStorage.setItem('token', this.token)
        } else {
          localStorage.removeItem('token')
        }
        localStorage.setItem('user', JSON.stringify(this.user))
        return true
      } catch (error) {
        this.logout()
        throw error
      }
    },
    logout() {
      this.user = null;
      this.token = null;
      this.isAuthenticated = false;
      localStorage.removeItem('token');
      localStorage.removeItem('user');
    }
  }
});
