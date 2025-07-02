<template>
  <div class="teacher-my-courses-mobile">
    <el-page-header :icon="ArrowLeft" title="返回" @back="$router.go(-1)">
      <template #content>
        <div class="flex items-center">
          <span class="text-large font-600 mr-3">我的课程</span>
          <el-tag type="success" size="small">教师工作台</el-tag>
        </div>
      </template>
    </el-page-header>

    <div class="content-area">
      <!-- 搜索栏 -->
      <div class="search-bar">
        <el-input
          v-model="searchQuery"
          placeholder="搜索课程名称或代码"
          :prefix-icon="Search"
          clearable
          @clear="handleSearch"
          @keyup.enter="handleSearch"
        />
      </div>

      <!-- 课程统计 -->
      <div class="stats-card">
        <div class="stat-item">
          <div class="stat-number">{{ courses.length }}</div>
          <div class="stat-label">授课课程</div>
        </div>
        <div class="stat-item">
          <div class="stat-number">{{ totalClasses }}</div>
          <div class="stat-label">教学班数</div>
        </div>
        <div class="stat-item">
          <div class="stat-number">{{ totalStudents }}</div>
          <div class="stat-label">学生总数</div>
        </div>
      </div>

      <!-- 加载状态 -->
      <div v-if="loading" class="loading-spinner">
        <el-spinner size="large" />
      </div>
      
      <!-- 空状态 -->
      <div v-if="!loading && courses.length === 0" class="empty-state">
        <el-empty description="暂无授课课程" />
      </div>

      <!-- 课程列表 -->
      <div class="course-list" v-else>
        <el-card v-for="course in filteredCourses" :key="course.id" class="course-card">
          <div class="course-header">
            <div class="course-info">
              <h3 class="course-name">{{ course.name }}</h3>
              <p class="course-code">课程代码: {{ course.classCode }}</p>
            </div>
            <el-tag :type="getCourseTypeColor(course.type)" size="small">
              {{ getCourseTypeName(course.type) }}
            </el-tag>
          </div>
          
          <div class="course-details">
            <div class="detail-row">
              <span class="label">学分:</span>
              <span class="value">{{ course.credits || '未设置' }}</span>
            </div>
            <div class="detail-row">
              <span class="label">学时:</span>
              <span class="value">{{ course.hours || '未设置' }}</span>
            </div>
            <div class="detail-row">
              <span class="label">教学班数:</span>
              <span class="value">{{ course.classCount }}</span>
            </div>
            <div class="detail-row">
              <span class="label">学生总数:</span>
              <span class="value">{{ course.studentCount }}</span>
            </div>
          </div>

          <div class="course-description" v-if="course.description">
            <p class="description-text">{{ course.description }}</p>
          </div>

          <div class="course-actions">
            <el-button size="small" type="primary" @click="viewClassDetails(course)">
              查看教学班
            </el-button>
            <el-button size="small" @click="viewCourseSchedule(course)">
              课程安排
            </el-button>
          </div>
        </el-card>
      </div>
    </div>

    <!-- 教学班详情对话框 -->
    <el-dialog
      v-model="showClassDialog"
      title="教学班详情"
      width="95%"
    >
      <div class="class-list-dialog">
        <el-card v-for="cls in selectedCourseClasses" :key="cls.id" class="class-card-dialog">
          <div class="class-info">
            <h4>{{ cls.classCode }}</h4>
            <p>最大人数: {{ cls.maxStudents }}</p>
            <p>当前人数: {{ cls.currentStudents || 0 }}</p>
          </div>
        </el-card>
      </div>
      
      <template #footer>
        <el-button @click="showClassDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { Search, ArrowLeft } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { getTeachingClasses } from '@/api/teacher'
import { getCourses } from '@/api/teacher'
import { useAuthStore } from '@/stores/auth'
import { useRouter } from 'vue-router'

// 认证和路由
const authStore = useAuthStore()
const router = useRouter()
const currentTeacherId = computed(() => authStore.user?.roleId)

// 数据
const courses = ref([])
const teachingClasses = ref([])
const loading = ref(false)
const searchQuery = ref('')
const showClassDialog = ref(false)
const selectedCourseClasses = ref([])

// 统计数据
const totalClasses = computed(() => teachingClasses.value.length)
const totalStudents = computed(() => 
  teachingClasses.value.reduce((sum, cls) => sum + (cls.currentStudents || 0), 0)
)

// 过滤课程
const filteredCourses = computed(() => {
  if (!searchQuery.value) return courses.value
  
  const query = searchQuery.value.toLowerCase()
  return courses.value.filter(course => 
    course.name.toLowerCase().includes(query) ||
    course.classCode.toLowerCase().includes(query)
  )
})

// 获取课程类型颜色
const getCourseTypeColor = (type) => {
  const typeColors = {
    'required': 'danger',
    'elective': 'warning', 
    'optional': 'info'
  }
  return typeColors[type] || 'primary'
}

// 获取课程类型名称
const getCourseTypeName = (type) => {
  const typeNames = {
    'required': '必修',
    'elective': '选修',
    'optional': '任选'
  }
  return typeNames[type] || '未知'
}

// 获取教师的教学班
const fetchTeachingClasses = async () => {
  try {
    const response = await getTeachingClasses({ page: 1, size: 1000 })
    // 过滤出当前教师的教学班
    teachingClasses.value = response.data.filter(item => 
      item.teacherId === currentTeacherId.value
    )
  } catch (error) {
    ElMessage.error('获取教学班失败: ' + (error.response?.data?.message || error.message))
    throw error
  }
}

// 获取课程信息
const fetchCourses = async () => {
  try {
    const response = await getCourses({ page: 1, size: 1000 })
    
    // 获取教师授课的课程ID列表
    const teachingCourseIds = [...new Set(teachingClasses.value.map(cls => cls.courseId))]
    
    // 过滤出教师授课的课程，并添加统计信息
    courses.value = response.data
      .filter(course => teachingCourseIds.includes(course.id))
      .map(course => {
        const relatedClasses = teachingClasses.value.filter(cls => cls.courseId === course.id)
        const studentCount = relatedClasses.reduce((sum, cls) => sum + (cls.currentStudents || 0), 0)
        
        return {
          ...course,
          classCount: relatedClasses.length,
          studentCount: studentCount
        }
      })
  } catch (error) {
    ElMessage.error('获取课程信息失败: ' + (error.response?.data?.message || error.message))
    throw error
  }
}

// 搜索处理
const handleSearch = () => {
  // 搜索是通过computed实时过滤的，这里可以添加其他逻辑
}

// 查看教学班详情
const viewClassDetails = (course) => {
  selectedCourseClasses.value = teachingClasses.value.filter(cls => cls.courseId === course.id)
  showClassDialog.value = true
}

// 查看课程安排
const viewCourseSchedule = (course) => {
  ElMessage.info(`${course.name} 课程安排功能开发中...`)
  // TODO: 跳转到课程安排页面
}

// 初始化加载
onMounted(async () => {
  // 检查当前用户是否为教师
  if (!currentTeacherId.value) {
    ElMessage.error('无法获取当前教师信息')
    router.push('/dashboard')
    return
  }
  
  loading.value = true
  try {
    // 先获取教学班，再根据教学班获取课程
    await fetchTeachingClasses()
    await fetchCourses()
  } catch (error) {
    ElMessage.error('初始化数据失败: ' + error.message)
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.teacher-my-courses-mobile {
  padding: 10px;
  background-color: #f4f5f7;
  min-height: 100vh;
}

.content-area {
  padding-top: 10px;
}

.search-bar {
  margin-bottom: 16px;
}

.stats-card {
  display: flex;
  justify-content: space-around;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
  color: white;
}

.stat-item {
  text-align: center;
}

.stat-number {
  font-size: 24px;
  font-weight: bold;
  margin-bottom: 4px;
}

.stat-label {
  font-size: 12px;
  opacity: 0.9;
}

.loading-spinner, .empty-state {
  margin-top: 50px;
  display: flex;
  justify-content: center;
}

.course-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.course-card {
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.course-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.course-info h3 {
  margin: 0 0 4px 0;
  font-size: 16px;
  color: #303133;
}

.course-code {
  margin: 0;
  font-size: 12px;
  color: #909399;
}

.course-details {
  margin-bottom: 12px;
}

.detail-row {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 4px 0;
  font-size: 14px;
}

.detail-row .label {
  color: #606266;
  font-weight: 500;
}

.detail-row .value {
  color: #303133;
}

.course-description {
  margin-bottom: 12px;
  padding: 8px;
  background-color: #f8f9fa;
  border-radius: 6px;
}

.description-text {
  margin: 0;
  font-size: 13px;
  color: #606266;
  line-height: 1.4;
}

.course-actions {
  display: flex;
  justify-content: space-between;
  gap: 8px;
}

.course-actions .el-button {
  flex: 1;
}

.class-list-dialog {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 400px;
  overflow-y: auto;
}

.class-card-dialog {
  border-radius: 8px;
}

.class-info h4 {
  margin: 0 0 8px 0;
  color: #303133;
}

.class-info p {
  margin: 2px 0;
  font-size: 14px;
  color: #606266;
}
</style> 