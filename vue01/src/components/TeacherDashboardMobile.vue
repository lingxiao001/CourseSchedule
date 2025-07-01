<template>
  <div>
    <!-- 今日课程概览 -->
    <div class="action-card">
      <div class="action-text">
        <h3>今日课程</h3>
        <p>您今天有 <strong>3</strong> 节课</p>
      </div>
      <el-button type="primary" round>查看详情</el-button>
    </div>

    <!-- 功能入口 -->
    <div class="quick-actions">
      <div class="action-item">
        <div class="action-icon" style="background-color: #EBF5FF;">
          <el-icon color="#409EFF"><Notebook /></el-icon>
        </div>
        <span>我的课程</span>
      </div>
      <div class="action-item">
        <div class="action-icon" style="background-color: #F0F9EB;">
          <el-icon color="#67C23A"><UserFilled /></el-icon>
        </div>
        <span>教学班级</span>
      </div>
       <div class="action-item">
        <div class="action-icon" style="background-color: #FEF0F0;">
          <el-icon color="#F56C6C"><Bell /></el-icon>
        </div>
        <span>教学通知</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { Notebook, UserFilled, Bell } from '@element-plus/icons-vue'
</script>

<style scoped>
.action-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 2rem;
  background-image: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 1.2rem;
  color: #fff;
  margin-bottom: 2.5rem;
}
.action-text h3 {
  margin: 0 0 0.4rem;
  font-size: 1.8rem;
  font-weight: 600;
}
.action-text p {
  margin: 0;
  font-size: 1.4rem;
  opacity: 0.9;
}

.quick-actions {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 2rem;
  text-align: center;
}
.action-item .action-icon {
  width: 6rem;
  height: 6rem;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  margin: 0 auto 1rem;
}
.action-item .el-icon {
  font-size: 2.8rem;
}
.action-item span {
  font-size: 1.3rem;
  font-weight: 500;
}
</style> 