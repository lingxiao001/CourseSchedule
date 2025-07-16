<<<<<<< Updated upstream
<template>
  <view class="teacher-dashboard">
    <u-navbar :icon="null" @back="$router.go(-1)">
      <template #content>
        <view class="flex items-center">
          <text class="text-large font-600 mr-3">教师工作台</text>
          <u-tag type="warning">教师面板</u-tag>
        </view>
      </template>
    </u-navbar>

    <u-line />

    <!-- 功能卡片 -->
    <view class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <u-card shadow="hover" class="cursor-pointer" @click="$router.push('/teacher/courses')">
        <view class="flex items-center">
          <u-icon class="mr-3" size="24"><Notebook /></u-icon>
          <view>
            <text3 class="text-lg font-semibold">课程管理</text3>
            <text class="text-gray-500">管理您的课程信息</text>
          </view>
        </view>
      </u-card>

      <u-card shadow="hover" class="cursor-pointer" @click="$router.push('/teacher/classes')">
        <view class="flex items-center">
          <u-icon class="mr-3" size="24"><UserFilled /></u-icon>
          <view>
            <text3 class="text-lg font-semibold">教学班管理</text3>
            <text class="text-gray-500">管理您的教学班级</text>
          </view>
        </view>
      </u-card>
    </view>

    <!-- 最近课程/教学班概览 -->
    <view class="mt-8">
      <text3 class="text-lg font-semibold mb-4">最近课程</text3>
      <u-table :data="recentCourses" style="width: 100%" :loading="loading">
        <u-table-column prop="classCode" label="课程代码" width="120" />
        <u-table-column prop="name" label="课程名称" />
        <u-table-column label="操作" width="80">
          <template #default="">
            <u-button link type="primary" @click="$router.push(`/teacher/courses`)">管理</u-button>
          </template>
        </u-table-column>
      </u-table>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getCourses } from '@/api/teacher'

const loading = ref(false)
const recentCourses = ref([])

const fetchRecentCourses = async () => {
  try {
    loading.value = true
    const response = await getCourses({ page: 1, size: 5 })
    recentCourses.value = response.data
  } catch (error) {
    console.error('获取最近课程失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchRecentCourses()
})
</script>

<style scoped>
.teacher-dashboard {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}
=======
<template>
  <div class="teacher-dashboard">
    <el-page-header :icon="null" @back="$router.go(-1)">
      <template #content>
        <div class="flex items-center">
          <span class="text-large font-600 mr-3">教师工作台</span>
          <el-tag type="warning">教师面板</el-tag>
        </div>
      </template>
    </el-page-header>

    <el-divider />

    <!-- 功能卡片 -->
    <div class="grid grid-cols-1 md:grid-cols-2 gap-4">
      <el-card shadow="hover" class="cursor-pointer" @click="$router.push('/teacher/courses')">
        <div class="flex items-center">
          <el-icon class="mr-3" size="24"><Notebook /></el-icon>
          <div>
            <h3 class="text-lg font-semibold">课程管理</h3>
            <p class="text-gray-500">管理您的课程信息</p>
          </div>
        </div>
      </el-card>

      <el-card shadow="hover" class="cursor-pointer" @click="$router.push('/teacher/classes')">
        <div class="flex items-center">
          <el-icon class="mr-3" size="24"><UserFilled /></el-icon>
          <div>
            <h3 class="text-lg font-semibold">教学班管理</h3>
            <p class="text-gray-500">管理您的教学班级</p>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 最近课程/教学班概览 -->
    <div class="mt-8">
      <h3 class="text-lg font-semibold mb-4">最近课程</h3>
      <el-table :data="recentCourses" style="width: 100%" v-loading="loading">
        <el-table-column prop="classCode" label="课程代码" width="120" />
        <el-table-column prop="name" label="课程名称" />
        <el-table-column label="操作" width="80">
          <template #default="">
            <el-button link type="primary" @click="$router.push(`/teacher/courses`)">管理</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
  </div>
</template>

<script setup>
import { Notebook, UserFilled } from '@element-plus/icons-vue'
import { ref, onMounted } from 'vue'
import { getCourses } from '@/api/teacher'

const loading = ref(false)
const recentCourses = ref([])

const fetchRecentCourses = async () => {
  try {
    loading.value = true
    const response = await getCourses({ page: 1, size: 5 })
    recentCourses.value = response.data
  } catch (error) {
    console.error('获取最近课程失败:', error)
  } finally {
    loading.value = false
  }
}

onMounted(() => {
  fetchRecentCourses()
})
</script>

<style scoped>
.teacher-dashboard {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}
>>>>>>> Stashed changes
</style>