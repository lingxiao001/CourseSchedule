<template>
  <view>
    <!-- 今日课程概览 -->
    <view class="action-card" @click="goTo('/schedule')">
      <view class="action-text">
        <h3>{{ todayWeekStr }}</h3>
        <p v-if="nextCourse">{{ formatCountdown(nextCourse) }} · {{ nextCourse.courseName }}</text>
        <p v-else-if="todayCourseCount > 0">今日共 <strong>{{ todayCourseCount }}</strong> 节课</text>
        <p v-else>今日无课，休息一下~</text>
      </view>
      <u-button type="primary" round>查看详情</u-button>
    </view>

    <!-- 功能入口 -->
    <view class="quick-actions">
      <view class="action-item" @click="goTo('/teacher/courses-mobile')">
        <view class="action-icon" style="background-color: #EBF5FF;">
          <u-icon color="#409EFF"><Notebook /></u-icon>
        </view>
        <text>我的课程</text>
      </view>
      <view class="action-item" @click="goTo('/teacher/classes-mobile')">
        <view class="action-icon" style="background-color: #F0F9EB;">
          <u-icon color="#67C23A"><UserFilled /></u-icon>
        </view>
        <text>教学班级</text>
      </view>
       <view class="action-item" @click="goTo('/teacher/notifications')">
        <view class="action-icon" style="background-color: #FEF0F0;">
          <u-icon color="#F56C6C"><Bell /></u-icon>
        </view>
        <text>教学通知</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { onMounted, onUnmounted, ref, computed } from 'vue'
import { Notebook, UserFilled, Bell } from '@element-plus/icons-vue'
import { useAuthStore } from '@/stores/auth'
import { getTeacherSchedules } from '@/api/teacher'

const WEEK_NAMES = ['周一', '周二', '周三', '周四', '周五', '周六', '周日']

const router = useRouter()
const authStore = useAuthStore()

// 今日课程相关数据
const nextCourse = ref(null)
const todayCourseCount = ref(0)

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
  const teacherId = authStore.user?.roleId
  if (!teacherId) return

  try {
    // 获取教师课表
    const schedules = await getTeacherSchedules(teacherId)
    
    // 今日课程统计
    const today = (new Date().getDay() + 6) % 7 + 1
    todayCourseCount.value = schedules.filter(s => s.dayOfWeek === today).length

    // 找到最近课程
    const enhanced = schedules.map(s => {
      const startMin = diffMinutes(s.startTime, s.dayOfWeek)
      const endMin   = diffMinutes(s.endTime, s.dayOfWeek)
      let status = 'upcoming'
      let minutes = startMin
      if (startMin <= 0 && endMin > 0) {
        status = 'ongoing'
        minutes = 0
      }
      return { ...s, minutesDiff: minutes, status, startMin }
    })

    const candidate = enhanced
      .filter(c => c.status === 'ongoing' || c.startMin >= 0)
      .sort((a,b)=> {
        if (a.status === 'ongoing' && b.status !== 'ongoing') return -1
        if (b.status === 'ongoing' && a.status !== 'ongoing') return 1
        return a.startMin - b.startMin
      })[0]

    nextCourse.value = candidate || null
  } catch (e) {
    console.error('加载教师Dashboard数据失败', e)
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
.action-card {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 2rem;
  background-image: linear-gradient(135deg, #f5576c, #f093fb);
  :border="true"-radius: 1.2rem;
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
  :border="true"-radius: 1.2rem;
  :border="true": 1px solid rgba(255, 255, 255, 0.2);
  box-shadow: 0 0.8rem 2rem rgba(0,0,0,0.1);
  color: #333;
  width: 100%;
}

.action-item .action-icon {
  width: 5rem;
  height: 5rem;
  :border="true"-radius: 50%;
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