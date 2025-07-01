<template>
  <div class="dashboard-grid">
    <!-- 统计卡片 -->
    <div class="stat-card" style="background-color: #E8F5E9;">
      <h4 class="stat-title">学生总数</h4>
      <p class="stat-number">1,234</p>
    </div>
    <div class="stat-card" style="background-color: #E3F2FD;">
      <h4 class="stat-title">教师总数</h4>
      <p class="stat-number">152</p>
    </div>

    <!-- 功能入口 -->
    <div class="action-card full-width" @click="goTo('/admin/users')">
      <el-icon><User /></el-icon>
      <div class="action-text">
        <h3>用户管理</h3>
        <p>管理教师和学生账户</p>
      </div>
      <el-icon class="arrow-icon"><ArrowRightBold /></el-icon>
    </div>
    <div class="action-card full-width" @click="goTo('/admin/courses')">
      <el-icon><Collection /></el-icon>
      <div class="action-text">
        <h3>课程管理</h3>
        <p>维护所有课程信息</p>
      </div>
      <el-icon class="arrow-icon"><ArrowRightBold /></el-icon>
    </div>
    <div class="action-card full-width" @click="goTo('/admin/schedule')">
      <el-icon><DataLine /></el-icon>
      <div class="action-text">
        <h3>智能排课</h3>
        <p>一键生成全校课表</p>
      </div>
      <el-icon class="arrow-icon"><ArrowRightBold /></el-icon>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { User, Collection, DataLine, ArrowRightBold } from '@element-plus/icons-vue'

const router = useRouter()

const goTo = (path) => {
  router.push(path)
}
</script>

<style scoped>
.dashboard-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
}
.stat-card {
  padding: 1.5rem;
  border-radius: 1.2rem;
  background-color: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 0.8rem 2rem rgba(0,0,0,0.1);
}
.stat-title {
  margin: 0 0 0.5rem;
  font-size: 1.4rem;
  font-weight: 500;
  color: #555;
}
.stat-number {
  margin: 0;
  font-size: 2.4rem;
  font-weight: 700;
  color: #f5576c;
}

.action-card {
  grid-column: 1 / -1; /* 占满整行 */
  display: flex;
  align-items: center;
  padding: 2rem 1.5rem;
  background-color: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-radius: 1.2rem;
  border: 1px solid rgba(255, 255, 255, 0.2);
  margin-bottom: 0.5rem;
  box-shadow: 0 0.8rem 2rem rgba(0,0,0,0.1);
  cursor: pointer;
  transition: all 0.2s ease-in-out;
}
.action-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 25px rgba(0,0,0,0.15);
}
.action-card .el-icon {
  font-size: 2.2rem;
  color: #f5576c;
}
.action-text {
  flex: 1;
  margin-left: 1.5rem;
}
.action-text h3 {
  margin: 0 0 0.4rem;
  font-size: 1.6rem;
  font-weight: 600;
  color: #333;
}
.action-text p {
  margin: 0;
  font-size: 1.2rem;
  color: #666;
}
.arrow-icon {
  color: #ccc !important;
}
</style> 