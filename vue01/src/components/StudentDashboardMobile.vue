<template>
  <view>
    <!-- 课表速览 -->
    <view class="schedule-preview-card" @click="goTo('/student/schedule')">
      <view class="card-header">
        <text4>{{ todayWeekStr }}</text4>
        <text v-if="nextCourse">{{ formatCountdown(nextCourse) }}</text>
        <text v-else>今日已无课</text>
      </view>
      <view class="course-info" v-if="nextCourse">
        <text3>{{ nextCourse.courseName }}</text3>
        <text><u-icon><Location /></u-icon> {{ nextCourse.building }}-{{ nextCourse.classroomName }}</text>
      </view>
      <view class="course-info" v-else>
        <text3 style="font-size:1.6rem;">享受你的空闲时间吧~</text3>
      </view>
    </view>

    <!-- 功能入口 -->
    <view class="action-grid">
      <view class="grid-item" @click="goTo('/schedule')">
        <u-icon><Calendar /></u-icon>
        <text>我的课表</text>
      </view>
      <view class="grid-item" @click="goTo('/student/selectCourse')">
        <u-icon><School /></u-icon>
        <text>选课中心</text>
      </view>
      <view class="grid-item" @click="goTo('/student/my-courses')">
        <u-icon><CollectionTag /></u-icon>
        <text>已选课程</text>
      </view>
    </view>

    <!-- 已选课程列表 -->
    <view v-if="selectedCourses.length" class="selected-course-list">
      <text4 class="section-title">已选课程</text4>
      <u-card v-for="course in selectedCourses" :key="course.selectionId" class="course-item">
        <view class="course-name">{{ course.courseName }}</view>
        <view class="course-info">{{ course.teacherName }} | {{ course.teachingClassId }}</view>
      </u-card>
    </view>
  </view>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { onMounted, onUnmounted, ref, computed } from 'vue'
import { useAuthStore } from '@/stores/auth'
import { getSelectionsByStudentWithTeachers, getStudentSchedules } from '@/api/student'

const WEEK_NAMES = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']

const router = useRouter()

const authStore = useAuthStore()
const selectedCourses = ref([])

// 最近即将上课的课程
const nextCourse = ref(null)

// 当前星期字符串
const todayWeekStr = computed(() => {
  const todayIdx = (new Date().getDay() + 6) % 7 // convert Sunday 0 to 6
  return WEEK_NAMES[todayIdx]
})

// 返回开始时间距离现在的分钟差（>0 表示未来）
function diffMinutes(startTimeStr, dayOfWeek) {
  const now = new Date()
  const todayIdx = (now.getDay() + 6) % 7 + 1 // make 1-7
  const [hh, mm] = startTimeStr.split(':').map(Number)
  const courseDate = new Date(now)
  const daysAhead = (dayOfWeek - todayIdx + 7) % 7
  courseDate.setDate(now.getDate() + daysAhead)
  courseDate.setHours(hh, mm, 0, 0)
  return (courseDate - now) / 60000 // in minutes
}

function formatCountdown(course) {
  if (!course) return ''
  if (course.status === 'ongoing') return '进行中'
  const minDiff = course.minutesDiff
  if (minDiff < 1) return '马上开始'
  if (minDiff < 60) return `${Math.round(minDiff)}分钟后`
  const hours = Math.floor(minDiff / 60)
  if (hours < 24) return `${hours}小时后`
  const days = Math.floor(hours / 24)
  return `${days}天后`
}

const goTo = (path) => {
  router.push(path)
}

async function loadData() {
  const studentId = authStore.user?.roleId
  if (!studentId) return

  try {
    // 已选课程
    selectedCourses.value = await getSelectionsByStudentWithTeachers(studentId)

    // 全部课表
    const schedules = await getStudentSchedules(studentId)

    const enhanced = schedules.map(s => {
      const startMin = diffMinutes(s.startTime, s.dayOfWeek)
      const endMin   = diffMinutes(s.endTime, s.dayOfWeek) // negative if ended
      let status = 'upcoming'
      let minutes = startMin
      if (startMin <= 0 && endMin > 0) {
        // 正在进行
        status = 'ongoing'
        minutes = 0
      }
      return { ...s, minutesDiff: minutes, status, startMin }
    })

    const candidate = enhanced
      .filter(c => c.status === 'ongoing' || c.startMin >= 0) // 进行中或未开始
      .sort((a,b)=> {
        // ongoing 优先，其次最近开始
        if (a.status === 'ongoing' && b.status !== 'ongoing') return -1
        if (b.status === 'ongoing' && a.status !== 'ongoing') return 1
        return a.startMin - b.startMin
      })[0]

    nextCourse.value = candidate || null
  } catch (e) {
    console.error('加载学生Dashboard数据失败', e)
  }
}

let refreshTimer = null

onMounted(()=> {
  loadData()
  refreshTimer = setInterval(loadData, 300000) // 5分钟刷新
})

onUnmounted(() => {
  if (refreshTimer) clearInterval(refreshTimer)
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
  :border="true": 1px solid rgba(255, 255, 255, 0.2);
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