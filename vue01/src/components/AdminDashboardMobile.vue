<template>
  <view class="dashboard-grid">
    <!-- 功能入口 -->
    <view class="action-card full-width" @click="goTo('/admin/stats')">
      <u-icon><DataAnalysis /></u-icon>
      <view class="action-text">
        <text3>统计信息</text3>
        <text>系统数据总览</text>
      </view>
      <u-icon class="arrow-icon"><ArrowRightBold /></u-icon>
    </view>
    <view class="action-card full-width" @click="goTo('/admin/users')">
      <u-icon><User /></u-icon>
      <view class="action-text">
        <text3>用户管理</text3>
        <text>管理教师和学生账户</text>
      </view>
      <u-icon class="arrow-icon"><ArrowRightBold /></u-icon>
    </view>
    <view class="action-card full-width" @click="goTo('/admin/courses')">
      <u-icon><Collection /></u-icon>
      <view class="action-text">
        <text3>课程管理</text3>
        <text>维护所有课程信息</text>
      </view>
      <u-icon class="arrow-icon"><ArrowRightBold /></u-icon>
    </view>
    <view class="action-card full-width" @click="goTo('/admin/classrooms')">
      <u-icon><OfficeBuilding /></u-icon>
      <view class="action-text">
        <text3>教室管理</text3>
        <text>维护教室信息</text>
      </view>
      <u-icon class="arrow-icon"><ArrowRightBold /></u-icon>
    </view>
    <view class="action-card full-width" @click="goTo('/admin/manual-schedule')">
      <u-icon><Calendar /></u-icon>
      <view class="action-text">
        <text3>手动排课</text3>
        <text>手动编辑课程表</text>
      </view>
      <u-icon class="arrow-icon"><ArrowRightBold /></u-icon>
    </view>
    <view class="action-card full-width" @click="goTo('/admin/auto-schedule')">
      <u-icon><Clock /></u-icon>
      <view class="action-text">
        <text3>自动排课</text3>
        <text>一键生成课程安排</text>
      </view>
      <u-icon class="arrow-icon"><ArrowRightBold /></u-icon>
    </view>
    <view class="action-card full-width" @click="goTo('/admin/teaching-classes')">
      <u-icon><CollectionTag /></u-icon>
      <view class="action-text">
        <text3>教学班管理</text3>
        <text>维护教学班信息</text>
      </view>
      <u-icon class="arrow-icon"><ArrowRightBold /></u-icon>
    </view>
    <!-- 智能排课入口暂时隐藏
    <view class="action-card full-width" @click="goTo('/admin/schedule')">
      <u-icon><DataLine /></u-icon>
      <view class="action-text">
        <text3>智能排课</text3>
        <text>一键生成全校课表</text>
      </view>
      <u-icon class="arrow-icon"><ArrowRightBold /></u-icon>
    </view>
    -->
  </view>
</template>

<script setup>
import { useRouter } from 'vue-router'
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
  :border="true": 1px solid rgba(255, 255, 255, 0.2);
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
  :border="true": 1px solid rgba(255, 255, 255, 0.2);
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