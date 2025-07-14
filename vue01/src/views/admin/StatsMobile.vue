<template>
  <div class="stats-page">
    <header class="header">
      <el-icon class="back" @click="$router.back()"><ArrowLeftBold /></el-icon>
      <h2>系统统计</h2>
    </header>

    <div v-if="loading" class="loading"><el-skeleton rows="5" animated/></div>
    <el-row v-else :gutter="12">
      <el-col :span="12" v-for="item in statsItems" :key="item.key">
        <el-card class="stat-card">
          <h4>{{ item.label }}</h4>
          <p class="num">{{ stats[item.key] }}</p>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getAllUsers, getAllCourses, getClassrooms } from '@/api/admin'
import { ArrowLeftBold } from '@element-plus/icons-vue'

const loading = ref(true)
const stats = ref({})
const statsItems = [
  { key:'userCount', label:'用户总数' },
  { key:'studentCount', label:'学生总数' },
  { key:'teacherCount', label:'教师总数' },
  { key:'courseCount', label:'课程总数' },
  { key:'classroomCount', label:'教室总数' },
  { key:'teachingClassCount', label:'教学班总数' },
]

// 获取统计数据
const fetchStats = async () => {
  try {
    // 并行获取所有数据
    const [userResponse, courses, teachingClasses, classrooms] = await Promise.all([
      getAllUsers({ page: 0, size: 1000 }), // 获取尽可能多的用户
      getAllCourses(),
      getTeachingClasses ? getTeachingClasses() : Promise.resolve([]),
      getClassrooms ? getClassrooms() : Promise.resolve([])
    ])
    
    // 统计用户数据
    const allUsers = userResponse.data.content || []
    const studentCount = allUsers.filter(user => user.role === 'student').length
    const teacherCount = allUsers.filter(user => user.role === 'teacher').length
    const adminCount = allUsers.filter(user => user.role === 'admin').length
    const userCount = allUsers.length
    
    // 设置统计数据
    stats.value = {
      userCount,
      studentCount,
      teacherCount,
      courseCount: courses.data.length,
      classroomCount: Array.isArray(classrooms.data) ? classrooms.data.length : 0,
      teachingClassCount: Array.isArray(teachingClasses.data) ? teachingClasses.data.length : 0
    }
    
  } catch (error) {
    console.error('获取统计数据失败:', error)
    ElMessage.error('获取统计数据失败')
  }
}

onMounted(async () => {
  loading.value = true
  try {
    await fetchStats()
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.stats-page { padding: 1rem; }
.header { display:flex; align-items:center; gap:.5rem; margin-bottom:1rem; }
.back { font-size:1.8rem; cursor:pointer; }
.stat-card { text-align:center; margin-bottom:1rem; }
.stat-card .num { font-size:2rem; font-weight:700; color:#409eff; margin:0; }
.loading { padding:1rem; }
</style> 