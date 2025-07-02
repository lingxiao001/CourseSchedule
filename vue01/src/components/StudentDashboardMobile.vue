<template>
  <div>
    <!-- 课表速览 -->
    <div class="schedule-preview-card" @click="goTo('/student/schedule')">
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
      <div class="grid-item" @click="goTo('/schedule')">
        <el-icon><Calendar /></el-icon>
        <span>我的课表</span>
      </div>
      <div class="grid-item" @click="goTo('/student/selectCourse')">
        <el-icon><School /></el-icon>
        <span>选课中心</span>
      </div>
      <div class="grid-item" @click="goTo('/student/my-courses')">
        <el-icon><CollectionTag /></el-icon>
        <span>已选课程</span>
      </div>
    </div>

    <!-- 已选课程列表 -->
    <div v-if="selectedCourses.length" class="selected-course-list">
      <h4 class="section-title">已选课程</h4>
      <el-card v-for="course in selectedCourses" :key="course.selectionId" class="course-item">
        <div class="course-name">{{ course.courseName }}</div>
        <div class="course-info">{{ course.teacherName }} | {{ course.teachingClassId }}</div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { onMounted, ref } from 'vue'
import { Calendar, School, Location, CollectionTag } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { getSelectionsByStudentWithTeachers } from '@/api/student'

const router = useRouter()

const authStore = useAuthStore()
const selectedCourses = ref([])

const goTo = (path) => {
  router.push(path)
}

onMounted(async () => {
  const studentId = authStore.user?.roleId
  if (!studentId) return
  try {
    selectedCourses.value = await getSelectionsByStudentWithTeachers(studentId)
  } catch (e) {
    console.error('获取已选课程失败', e)
  }
})
</script>

<style scoped>
.schedule-preview-card {
  padding: 2rem;
  background-image: linear-gradient(135deg, #f5576c, #f093fb);
  border-radius: 1.2rem;
  color: #fff;
  margin-bottom: 2.5rem;
  box-shadow: 0 8px 20px rgba(245, 87, 108, 0.3);
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
  background-color: rgba(255, 255, 255, 0.7);
  backdrop-filter: blur(10px);
  -webkit-backdrop-filter: blur(10px);
  border-radius: 1.2rem;
  border: 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 0.8rem 2rem rgba(0,0,0,0.1);
  font-size: 1.4rem;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s ease-in-out;
  color: #333;
}
.grid-item .el-icon {
  font-size: 3rem;
  margin-bottom: 1rem;
  color: #f5576c;
}
.grid-item:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 20px rgba(0,0,0,0.1);
}

.selected-course-list {
  margin-top: 2rem;
}
.section-title {
  font-size: 1.6rem;
  font-weight: 600;
  margin-bottom: 1rem;
}
.course-item {
  margin-bottom: 1rem;
}
.course-name {
  font-weight: 600;
  font-size: 1.4rem;
}
.course-info {
  font-size: 1.2rem;
  color: #666;
}
</style> 