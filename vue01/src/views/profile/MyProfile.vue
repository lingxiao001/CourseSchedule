<template>
  <view class="profile-page">
    <view class="scroll-content">
      <view class="header-section">
        <u-icon class="back-icon" @click="router.back()"><ArrowLeft /></u-icon>
        <h1>个人中心</h1>
      </view>

      <!-- User Info Card -->
      <view class="user-card">
        <u-avatar :size="70" class="user-avatar">{{ userInitial }}</u-avatar>
        <h2 class="user-name">{{ userInfo.realName || '未命名' }}</h2>
        <p class="user-role">{{ userRole }}</text>
      </view>

      <!-- Action List -->
      <view class="action-list">
        <view class="action-item" @click="accountDrawerVisible = true">
          <u-icon><Setting /></u-icon>
          <text>账户设置</text>
          <u-icon class="arrow"><ArrowRightBold /></u-icon>
        </view>
        <view class="action-item" @click="showComingSoon">
          <u-icon><Bell /></u-icon>
          <text>消息通知</text>
          <u-icon class="arrow"><ArrowRightBold /></u-icon>
        </view>
      </view>
    </view>
    
    <!-- Logout Button Area -->
    <view class="logout-section">
      <u-button type="error" class="logout-button" @click="handleLogout">退出登录</u-button>
    </view>

    <!-- 账户设置抽屉 -->
    <u-drawer v-model="accountDrawerVisible" title="账户设置" direction="rtl" size="80%">
      <u-descriptions :column="1" :border="true">
        <u-descriptions-item label="用户名">{{ userInfo.username }}</u-descriptions-item>
        <u-descriptions-item label="真实姓名">{{ userInfo.realName }}</u-descriptions-item>
        <u-descriptions-item label="角色">{{ userRole }}</u-descriptions-item>
        <u-descriptions-item label="用户ID">{{ userInfo.id }}</u-descriptions-item>
      </u-descriptions>
      <template #footer>
        <u-button type="primary" @click="accountDrawerVisible = false">关闭</u-button>
      </template>
    </u-drawer>
  </view>
</template>

<script setup>
import { computed, ref } from 'vue';
import { useRouter } from 'vue-router';
import { useAuthStore } from '@/stores/auth';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Setting, Bell, ArrowRightBold, ArrowLeft } from '@element-plus/icons-vue';

const router = useRouter();
const authStore = useAuthStore();

const userInfo = computed(() => authStore.user || {});
const userInitial = computed(() => (userInfo.value.realName ? userInfo.value.realName.charAt(0).toUpperCase() : 'U'));

const userRole = computed(() => {
  const role = userInfo.value.role;
  if (role === 'admin') return '管理员';
  if (role === 'teacher') return '教师';
  if (role === 'student') return '学生';
  return '未知角色';
});

const accountDrawerVisible = ref(false)

const handleLogout = () => {
  uni.showModal({ title: '$1', content: '$2', success: (res) => { if (res.confirm) { $3 } } })('您确定要退出登录吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning',
  }).then(() => {
    authStore.logout();
    router.push('/login');
    uni.showToast({ title: '$1', icon: 'success' })('已成功退出登录');
  }).catch(() => {
    // User cancelled the action
  });
};

const showComingSoon = () => {
  uni.showToast({ title: '$1', icon: 'none' })('该功能正在开发中...');
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
  :border="true"-radius: 1rem;
  box-shadow: 0 8px 25px rgba(0,0,0,0.08);
  display: flex;
  flex-direction: column;
  align-items: center;
  position: relative;
  z-index: 10;
}

.user-avatar {
  margin-top: -5rem; /* Make avatar pop out */
  :border="true": 4px solid #fff;
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
  :border="true"-radius: 1rem;
}

.action-list {
  background-color: #fff;
  margin: 0 1.5rem;
  :border="true"-radius: 1rem;
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
  :border="true"-bottom: 1px solid #f5f5f5;
  transition: background-color 0.2s;
}

.action-item:last-child {
  :border="true"-bottom: none;
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
  :border="true"-radius: 0.8rem;
  :border="true": none;
  font-weight: 500;
}

.back-icon {
  position: absolute;
  left: 1.5rem;
  top: 1.5rem;
  font-size: 2.4rem;
  cursor: pointer;
}
</style> 