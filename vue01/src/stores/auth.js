import { defineStore } from 'pinia';
import { authApi } from '@/api/auth';

export const useAuthStore = defineStore('auth', {
  state: () => ({
    user: null,
    token: null,
    isAuthenticated: false
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
        localStorage.setItem('token', this.token);
        return true;
      } catch (error) {
        this.logout();
        throw error;
      }
    },
    logout() {
      this.user = null;
      this.token = null;
      this.isAuthenticated = false;
      localStorage.removeItem('token');
    }
  }
});
