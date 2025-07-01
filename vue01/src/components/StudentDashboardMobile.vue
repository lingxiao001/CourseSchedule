<template>
  <div>
    <!-- 课表速览 -->
    <div class="schedule-preview-card">
      <div class="card-header">
        <h4>下节课</h4>
        <span>10分钟后</span>
      </div>
      <div class="course-info">
        <h3>计算机网络</h3>
        <p><el-icon><Location /></el-icon> 教3-101</p>
      </div>
    </div>

    <!-- 功能入口 -->
    <div class="action-grid">
      <div class="grid-item">
        <el-icon><Calendar /></el-icon>
        <span>我的课表</span>
      </div>
      <div class="grid-item">
        <el-icon><School /></el-icon>
        <span>选课中心</span>
      </div>
      <div class="grid-item">
        <el-icon><CollectionTag /></el-icon>
        <span>已选课程</span>
      </div>
      <div class="grid-item">
        <el-icon><Trophy /></el-icon>
        <span>我的成绩</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { Calendar, School, Location, CollectionTag, Trophy } from '@element-plus/icons-vue'
</script>

<style scoped>
.schedule-preview-card {
  padding: 2rem;
  background-image: linear-gradient(135deg, #89f7fe 0%, #66a6ff 100%);
  border-radius: 1.2rem;
  color: #fff;
  margin-bottom: 2.5rem;
}
.schedule-preview-card .card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 1.4rem;
  opacity: 0.9;
  margin-bottom: 1.5rem;
}
.course-info h3 {
  margin: 0 0 0.5rem;
  font-size: 2rem;
  font-weight: 600;
}
.course-info p {
  margin: 0;
  font-size: 1.4rem;
  display: flex;
  align-items: center;
}
.course-info .el-icon {
  margin-right: 0.5rem;
}

.action-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 1.5rem;
}
.grid-item {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 12rem;
  background-color: #fff;
  border-radius: 1.2rem;
  box-shadow: 0 0.4rem 1.5rem rgba(0,0,0,0.05);
  font-size: 1.4rem;
  font-weight: 500;
}
.grid-item .el-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
  color: #409eff;
}
</style> 