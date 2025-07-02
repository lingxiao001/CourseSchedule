<template>
  <div class="teacher-my-courses-container">
    <div class="header">
      <el-icon @click="goBack"><ArrowLeftBold /></el-icon>
      <h1>我的课程</h1>
      <span></span>
    </div>
    
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="5" animated />
    </div>
    
    <div v-else-if="courses.length === 0" class="empty-state">
      <el-empty description="您还没有教授任何课程"></el-empty>
    </div>
    
    <div v-else class="course-list">
      <el-card v-for="course in courses" :key="course.courseId" class="course-card">
        <div class="card-content">
          <div class="course-details">
            <h3 class="course-name">{{ course.name }}</h3>
            <p class="course-code">课程代码: {{ course.classCode }}</p>
            <p class="credits">学分: {{ course.credits }}</p>
            <p class="class-info">教学班: {{ course.teachingClassCount || 0 }} 个班级</p>
          </div>
          <div class="course-actions">
            <el-button type="primary" plain size="small" @click="viewCourseDetails(course)">
              查看详情
            </el-button>
            <el-button type="success" plain size="small" @click="manageClasses(course)">
              管理班级
            </el-button>
          </div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeftBold } from '@element-plus/icons-vue'
import { getTeachingClasses } from '@/api/teacher'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()
const allTeachingClasses = ref([])
const loading = ref(true)

// 使用 computed 属性来派生出教师的课程列表
const courses = computed(() => {
  if (!authStore.user?.roleId) return []

  const teacherId = authStore.user.roleId
  // 1. 筛选出当前教师的教学班
  const teacherClasses = allTeachingClasses.value.filter(
    (tc) => tc.teacherId === teacherId
  )

  // 2. 从教学班中提取课程信息，并用 Map 进行去重
  const coursesMap = new Map()
  teacherClasses.forEach((tc) => {
    if (tc.course && !coursesMap.has(tc.course.courseId)) {
      coursesMap.set(tc.course.courseId, {
        ...tc.course,
        teachingClassCount: 0 // 初始化教学班数量
      })
    }
  })

  // 3. 计算每个课程的教学班数量
  teacherClasses.forEach((tc) => {
    if (tc.course && coursesMap.has(tc.course.courseId)) {
      const course = coursesMap.get(tc.course.courseId)
      course.teachingClassCount++
    }
  })

  return Array.from(coursesMap.values())
})

const goBack = () => {
  router.back()
}

const fetchTeacherCourses = async () => {
  loading.value = true
  try {
    // 获取所有教学班
    const response = await getTeachingClasses()
    allTeachingClasses.value = response.data || []
  } catch (error) {
    ElMessage.error('获取课程列表失败')
    console.error('获取教师课程失败:', error)
  } finally {
    loading.value = false
  }
}

const viewCourseDetails = () => {
  // 跳转到课程管理页面
  router.push('/teacher/courses')
}

const manageClasses = () => {
  // 跳转到教学班管理页面
  router.push('/teacher/Classes')
}

onMounted(() => {
  fetchTeacherCourses()
})
</script>

<style scoped>
.teacher-my-courses-container {
  padding: 1.5rem;
  background-color: #f4f7fa;
  min-height: 100vh;
}

.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 2rem;
  color: #333;
}

.header .el-icon {
  font-size: 2rem;
  cursor: pointer;
  color: #409eff;
}

.header h1 {
  font-size: 1.8rem;
  font-weight: 600;
  margin: 0;
}

.header span {
  width: 2rem; 
}

.loading-container {
  padding: 2rem;
}

.empty-state {
  margin-top: 5rem;
}

.course-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.course-card {
  border-radius: 1rem;
  border: 1px solid #e0e6ed;
  transition: all 0.3s ease;
}

.course-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.card-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.course-details .course-name {
  font-size: 1.6rem;
  font-weight: 600;
  margin: 0 0 0.8rem 0;
  color: #333;
}

.course-details p {
  margin: 0.4rem 0;
  font-size: 1.3rem;
  color: #666;
}

.course-actions {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}

.course-actions .el-button {
  font-size: 1.2rem;
  min-width: 80px;
}

@media (max-width: 768px) {
  .card-content {
    flex-direction: column;
    align-items: flex-start;
    gap: 1rem;
  }
  
  .course-actions {
    flex-direction: row;
    width: 100%;
    justify-content: flex-end;
  }
}
</style> 