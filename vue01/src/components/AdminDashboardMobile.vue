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
    <div class="action-card full-width">
      <el-icon><User /></el-icon>
      <div class="action-text">
        <h3>用户管理</h3>
        <p>管理教师和学生账户</p>
      </div>
      <el-icon class="arrow-icon"><ArrowRightBold /></el-icon>
    </div>
    <div class="action-card full-width">
      <el-icon><Collection /></el-icon>
      <div class="action-text">
        <h3>课程管理</h3>
        <p>维护所有课程信息</p>
      </div>
      <el-icon class="arrow-icon"><ArrowRightBold /></el-icon>
    </div>
    <div class="action-card full-width">
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
import { User, Collection, DataLine, ArrowRightBold } from '@element-plus/icons-vue'
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
}
.stat-title {
  margin: 0 0 0.5rem;
  font-size: 1.4rem;
  font-weight: 500;
  color: #333;
}
.stat-number {
  margin: 0;
  font-size: 2.4rem;
  font-weight: 700;
  color: #1d3557;
}

.action-card {
  grid-column: 1 / -1; /* 占满整行 */
  display: flex;
  align-items: center;
  padding: 2rem 1.5rem;
  background-color: #fff;
  border-radius: 1.2rem;
  margin-bottom: 0.5rem; /* 微调间距 */
  box-shadow: 0 0.4rem 1.5rem rgba(0,0,0,0.05);
}
.action-card .el-icon {
  font-size: 2.2rem;
  color: #409eff;
}
.action-text {
  flex: 1;
  margin-left: 1.5rem;
}
.action-text h3 {
  margin: 0 0 0.4rem;
  font-size: 1.6rem;
  font-weight: 600;
}
.action-text p {
  margin: 0;
  font-size: 1.2rem;
  color: #8492a6;
}
.arrow-icon {
  color: #c0c4cc !important;
}
</style> 