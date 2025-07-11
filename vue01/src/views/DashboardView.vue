<template>
  <view class="mobile-dashboard">
    
    <!-- 顶部用户信息栏 -->
    <header class="dashboard-header">
      <view class="user-info">
        <u-avatar class="user-avatar" :size="44" :style="{ backgroundColor: avatarColor }">
          {{ authStore.user.realName?.charAt(0) }}
        </u-avatar>
        <view class="user-details">
          <text class="welcome-text">欢迎回来,</text>
          <text3 class="user-name">{{ authStore.user.realName }}</text3>
        </view>
      </view>
    </header>
    
    <!-- 主内容区：根据角色动态渲染 -->
    <main class="dashboard-content">
      <StudentDashboardMobile v-if="authStore.user.role === 'student'" />
      <TeacherDashboardMobile v-else-if="authStore.user.role === 'teacher'" />
      <AdminDashboardMobile v-else-if="authStore.user.role === 'admin'" />
    </main>

    <!-- 底部导航栏 -->
    <footer class="bottom-nav">
      <view class="nav-item" :class="{ active: $route.path === '/m/dashboard' }" @click="goTo('/m/dashboard')">
        <u-icon><House /></u-icon>
        <text>首页</text>
      </view>
      <view v-if="authStore.user.role !== 'admin'" class="nav-item" :class="{ active: $route.path === '/schedule' }" @click="goTo('/schedule')">
        <u-icon><Calendar /></u-icon>
        <text>课表</text>
      </view>
      <view class="nav-item" :class="{ active: $route.path === '/profile' }" @click="goTo('/profile')">
        <u-icon><User /></u-icon>
        <text>我的</text>
      </view>
    </footer>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import AdminDashboardMobile from '@/components/AdminDashboardMobile.vue'
import TeacherDashboardMobile from '@/components/TeacherDashboardMobile.vue'
import StudentDashboardMobile from '@/components/StudentDashboardMobile.vue'

const router = useRouter()
const authStore = useAuthStore()

const goTo = (path) => {
  router.push(path)
}

// 根据用户名生成一个简单的颜色，让头像不单调
const avatarColor = computed(() => {
  const colors = ['#FF7875', '#FFC069', '#95DE64', '#597EF7', '#AD6800']
  const charCodeSum = authStore.user.realName?.charCodeAt(0) || 0
  return colors[charCodeSum % colors.length]
})
</script>

<style scoped>
.mobile-dashboard {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background: linear-gradient(to top, #fdfbfb 0%, #ebedee 100%);
  overflow: hidden;
}

/* 顶部栏样式 */
.dashboard-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem 2rem;
  background: linear-gradient(135deg, #f5576c, #f093fb);
  color: #fff;
  flex-shrink: 0; /* 防止header被压缩 */
}
.user-info {
  display: flex;
  align-items: center;
}
.user-avatar {
  font-size: 2rem;
  font-weight: 500;
}
.user-details {
  margin-left: 1.2rem;
}
.welcome-text {
  font-size: 1.3rem;
  color: #f0f0f0;
  margin: 0;
}
.user-name {
  font-size: 1.8rem;
  font-weight: 600;
  color: #fff;
  margin: 0.2rem 0 0;
}

/* 主内容区 */
.dashboard-content {
  flex: 1;
  padding: 2rem;
  overflow-y: auto; /* 内容超出时可滚动 */
}

/* 底部导航栏 */
.bottom-nav {
  display: flex;
  justify-content: space-around;
  align-items: center;
  height: 6rem;
  background-color: #fff;
  box-shadow: 0 -0.2rem 0.8rem rgba(0,0,0,0.05);
}
.nav-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  color: #8492a6;
  font-size: 1.2rem;
  transition: color 0.3s;
  cursor: pointer;
}
.nav-item .el-icon {
  font-size: 2.2rem;
  margin-bottom: 0.3rem;
}
.nav-item.active {
  color: #409eff;
}
</style>