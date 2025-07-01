<template>
  <div class="profile-page">
    <div class="scroll-content">
      <div class="header-section">
        <h1>个人中心</h1>
      </div>

      <!-- User Info Card -->
      <div class="user-card">
        <el-avatar :size="70" class="user-avatar">{{ userInitial }}</el-avatar>
        <h2 class="user-name">{{ userInfo.name || '用户名' }}</h2>
        <p class="user-role">{{ userRole }}</p>
      </div>

      <!-- Action List -->
      <div class="action-list">
        <div class="action-item" @click="showComingSoon">
          <el-icon><Setting /></el-icon>
          <span>账户设置</span>
          <el-icon class="arrow"><ArrowRightBold /></el-icon>
        </div>
        <div class="action-item" @click="showComingSoon">
          <el-icon><Bell /></el-icon>
          <span>消息通知</span>
          <el-icon class="arrow"><ArrowRightBold /></el-icon>
        </div>
      </div>
    </div>
    
    <!-- Logout Button Area -->
    <div class="logout-section">
      <el-button type="danger" class="logout-button" @click="handleLogout">退出登录</el-button>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Setting, Bell, ArrowRightBold } from '@element-plus/icons-vue';

const router = useRouter();
const authStore = useAuthStore();

const userInfo = computed(() => authStore.user || {});
const userInitial = computed(() => (userInfo.value.name ? userInfo.value.name.charAt(0).toUpperCase() : 'U'));

const userRole = computed(() => {
  const role = userInfo.value.role;
  if (role === 'ADMIN') return '管理员';
  if (role === 'TEACHER') return '教师';
  if (role === 'STUDENT') return '学生';
  return '未知角色';
});


const handleLogout = () => {
  ElMessageBox.confirm('您确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    authStore.logout();
    router.push('/login');
    ElMessage.success('已成功退出登录');
  }).catch(() => {
    // User cancelled the action
  });
};

const showComingSoon = () => {
  ElMessage.info('该功能正在开发中...');
};

</script>

<style scoped>
.profile-page {
  display: flex;
  flex-direction: column;
  height: calc(100vh - 6rem); /* 减去底部导航栏高度 */
  background-color: #f7f8fa;
}

.scroll-content {
  flex-grow: 1;
  overflow-y: auto;
  padding-bottom: 2rem;
}

.header-section {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  padding: 3rem 1.5rem 6rem; /* 增加底部padding，为user-card留出空间 */
  text-align: center;
}

.header-section h1 {
  margin: 0;
  font-size: 1.8rem;
  font-weight: 600;
}

.user-card {
  background-color: #fff;
  margin: -4.5rem 1.5rem 1.5rem; /* 调整margin，使其更突出 */
  padding: 1.5rem;
  border-radius: 1rem;
  box-shadow: 0 8px 25px rgba(0,0,0,0.08);
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  z-index: 10;
}

.user-avatar {
  margin-top: -5rem; /* Make avatar pop out */
  border: 4px solid #fff;
  font-size: 2.5rem;
  background-color: #764ba2;
  box-shadow: 0 4px 10px rgba(0,0,0,0.1);
}

.user-name {
  margin: 1rem 0 0.5rem;
  font-size: 2rem;
  font-weight: 600;
  color: #333;
}

.user-role {
  margin: 0;
  font-size: 1.4rem;
  color: #666;
  background-color: #f0f2f5;
  padding: 0.4rem 1rem;
  border-radius: 1rem;
}

.action-list {
  background-color: #fff;
  margin: 0 1.5rem;
  border-radius: 1rem;
  overflow: hidden;
  box-shadow: 0 4px 15px rgba(0,0,0,0.06);
}

.action-item {
  display: flex;
  align-items: center;
  padding: 1.8rem 1.5rem; /* 增加内边距 */
  font-size: 1.5rem;
  color: #444;
  cursor: pointer;
  border-bottom: 1px solid #f5f5f5;
  transition: background-color 0.2s;
}

.action-item:last-child {
  border-bottom: none;
}

.action-item:hover {
  background-color: #fafafa;
}

.action-item .el-icon {
  font-size: 2rem;
  margin-right: 1.5rem;
  color: #555;
}

.action-item .arrow {
  margin-left: auto;
  font-size: 1.6rem;
  color: #ccc;
}

.logout-section {
  padding: 1.5rem;
  background-color: #f7f8fa;
}

.logout-button {
  width: 100%;
  padding: 2.2rem 0;
  font-size: 1.6rem;
  border-radius: 0.8rem;
  border: none;
  font-weight: 500;
}
</style> 