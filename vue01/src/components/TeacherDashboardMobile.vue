<template>
  <div>
    <!-- 今日课程概览 -->
    <div class="action-card" @click="goTo('/teacher/today-schedule')">
      <div class="action-text">
        <h3>今日课程</h3>
        <p>您今天有 <strong>3</strong> 节课</p>
      </div>
      <el-button type="primary" round>查看详情</el-button>
    </div>

    <!-- 功能入口 -->
    <div class="quick-actions">
      <div class="action-item" @click="goTo('/teacher/courses-mobile')">
        <div class="action-icon" style="background-color: #EBF5FF;">
          <el-icon color="#409EFF"><Notebook /></el-icon>
        </div>
        <span>我的课程</span>
      </div>
      <div class="action-item" @click="goTo('/teacher/classes-mobile')">
        <div class="action-icon" style="background-color: #F0F9EB;">
          <el-icon color="#67C23A"><UserFilled /></el-icon>
        </div>
        <span>教学班级</span>
      </div>
       <div class="action-item" @click="goTo('/teacher/notifications')">
        <div class="action-icon" style="background-color: #FEF0F0;">
          <el-icon color="#F56C6C"><Bell /></el-icon>
        </div>
        <span>教学通知</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { Notebook, UserFilled, Bell } from '@element-plus/icons-vue'

const router = useRouter()

const goTo = (path) => {
  router.push(path)
}
</script>

<style scoped>
.action-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 2rem;
  background-image: linear-gradient(135deg, #f5576c, #f093fb);
  border-radius: 1.2rem;
  color: #fff;
  margin-bottom: 2.5rem;
  cursor: pointer;
  transition: all 0.2s ease-in-out;
  box-shadow: 0 8px 20px rgba(245, 87, 108, 0.3);
}
.action-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 25px rgba(245, 87, 108, 0.4);
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
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
  text-align: center;
}

.action-item {
  display: flex;
  flex-direction: row;
  align-items: center;
  justify-content: flex-start;
  padding: 1.5rem 2rem;
  background-color: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-radius: 1.2rem;
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 0.8rem 2rem rgba(0,0,0,0.1);
  color: #333;
  width: 100%;
}

.action-item .action-icon {
  width: 5rem;
  height: 5rem;
  border-radius: 50%;
  display: flex;
  justify-content: center;
  align-items: center;
  margin-right: 1.5rem;
  background: none !important; /* Override inline styles */
}
.action-item .el-icon {
  font-size: 2.5rem;
  color: #f5576c !important; /* Override inline styles */
}
.action-item span {
  font-size: 1.6rem;
  font-weight: 500;
  text-align: left;
}

.action-card, .action-item {
  cursor: pointer;
  transition: all 0.2s ease-in-out;
}
.action-item:hover {
  transform: translateY(-5px);
}
</style> 