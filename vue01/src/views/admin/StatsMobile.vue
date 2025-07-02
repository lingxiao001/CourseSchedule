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
import { getStats } from '@/api/admin'
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

onMounted(async () => {
  try {
    const res = await getStats()
    stats.value = res.data
  } catch (e) {
    console.error('获取统计失败', e)
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