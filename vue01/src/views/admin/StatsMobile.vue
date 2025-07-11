<template>
  <view class="stats-page">
    <header class="header">
      <u-icon class="back" @click="$router.back()"><ArrowLeftBold /></u-icon>
      <text2>系统统计</text2>
    </header>

    <view v-if="loading" class="loading"><u-skeleton rows="5" animated/></view>
    <u-row v-else :gutter="12">
      <u-col :span="12" v-for="item in statsItems" :key="item.key">
        <u-card class="stat-card">
          <text4>{{ item.label }}</text4>
          <text class="num">{{ stats[item.key] }}</text>
        </u-card>
      </u-col>
    </u-row>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getStats } from '@/api/admin'
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