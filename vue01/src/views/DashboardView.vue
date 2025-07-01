<template>
    <div class="dashboard-container">
      <el-page-header :icon="null" @back="goBack">
        <template #content>
          <div class="flex items-center">
            <span class="text-large font-600 mr-3"> 欢迎回来, {{ authStore.user.real_name }} </span>
            <el-tag :type="roleTagType">{{ roleName }}</el-tag>
          </div>
        </template>
      </el-page-header>
  
      <el-divider />
  
      <div class="dashboard-content">
        <!-- 管理员仪表板 -->
        <template v-if="authStore.user.role === 'admin'">
          <admin-dashboard />
        </template>
  
        <!-- 教师仪表板 -->
        <template v-else-if="authStore.user.role === 'teacher'">
          <teacher-dashboard />
        </template>
  
        <!-- 学生仪表板 -->
        <template v-else>
          <student-dashboard />
        </template>
      </div>
    </div>
  </template>
  
  <script setup>
  import { computed } from 'vue'
  import { useRouter } from 'vue-router'
  import { useAuthStore } from '@/stores/auth'
  import AdminDashboard from '@/components/AdminDashboard.vue'
  import TeacherDashboard from '@/components/TeacherDashboard.vue'
  import StudentDashboard from '@/components/StudentDashboard.vue'
  
  const router = useRouter()
  const authStore = useAuthStore()
  
  const roleName = computed(() => {
    switch (authStore.user.role) {
      case 'admin': return '管理员'
      case 'teacher': return '教师'
      case 'student': return '学生'
      default: return '用户'
    }
  })
  
  const roleTagType = computed(() => {
    switch (authStore.user.role) {
      case 'admin': return 'danger'
      case 'teacher': return 'warning'
      case 'student': return 'success'
      default: return 'info'
    }
  })
  
  const goBack = () => {
    router.push('/')
  }
  </script>
  
  <style scoped>
  .dashboard-container {
    padding: 20px;
    background-color: #f5f7fa;
    min-height: calc(100vh - 60px);
  }
  
  .dashboard-content {
    margin-top: 20px;
    padding: 20px;
    background-color: #fff;
    border-radius: 4px;
    box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  }
  </style>