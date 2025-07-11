<template>
  <view class="schedule-page">
    <!-- 顶部导航栏 -->
    <header class="page-header">
      <u-icon @click="goBack"><ArrowLeftBold /></u-icon>
      <text class="page-title">我的课表</text>
      <view class="header-placeholder"></view>
    </header>

    <!-- 星期切换器 -->
    <view class="week-switcher">
      <u-button 
        v-for="(day, index) in weekDays" 
        :key="index"
        :type="currentDay === index ? 'primary' : 'default'"
        round
        @click="currentDay = index"
      >
        {{ day }}
      </u-button>
    </view>

    <!-- 课表内容区 -->
    <main class="schedule-content">
      <view v-if="loading" class="loading-state">
        <u-icon class="is-loading" size="26"><Loading /></u-icon>
        <text>正在加载课表...</text>
      </view>
      
      <view v-else-if="todayCourses.length === 0" class="empty-state">
        <u-icon size="50"><MessageBox /></u-icon>
        <text>今天没有课哦，休息一下吧！</text>
      </view>

      <view v-else class="course-list">
        <view v-for="course in todayCourses" :key="course.id" class="course-card">
          <view class="time-info">
            <text class="start-time">{{ course.startTime }}</text>
            <text class="end-time">{{ course.endTime }}</text>
          </view>
          <view class="course-details">
            <text class="course-name">{{ course.courseName }}</text>
            <p class="course-location">
              <u-icon><Location /></u-icon>
              {{ course.classroom.building }}-{{ course.classroom.classroomName }}
            </p>
            <p class="course-teacher">
              <u-icon><User /></u-icon>
              {{ course.teacherName }}
            </p>
            <p v-if="course.classCode" class="course-class">
              <u-icon><User /></u-icon>
              {{ user.role === 'teacher' ? '班级：' : '教学班：' }}{{ course.classCode }}
            </p>
          </view>
        </view>
      </view>
    </main>
  </view>
</template>

<script setup>

// 全局 uni 对象定义
const uni = {
  showToast: (options) => {
    if (options.icon === 'success') {
      alert('✅ ' + options.title);
    } else if (options.icon === 'error') {
      alert('❌ ' + options.title);
    } else {
      alert(options.title);
    }
  },
  showModal: (options) => {
    const result = confirm(options.content || options.title);
    if (options.success) {
      options.success({ confirm: result });
    }
  },
  navigateTo: (options) => {
    window.location.href = options.url;
  },
  navigateBack: () => {
    window.history.back();
  },
  redirectTo: (options) => {
    window.location.replace(options.url);
  },
  reLaunch: (options) => {
    window.location.href = options.url;
  }
};






import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { getStudentSchedules } from '@/api/student'
import { getTeacherSchedules } from '@/api/teacher'
const router = useRouter()
const authStore = useAuthStore()

const loading = ref(true)
const currentDay = ref((new Date().getDay() + 6) % 7) // 将周日(0)转换为6，其余减1
const weekDays = ['一', '二', '三', '四', '五', '六', '日']
const allCourses = ref([])

// 向模板暴露authStore
const { user } = authStore

const goBack = () => router.back()

// 计算当天课程
const todayCourses = computed(() => {
  if (!allCourses.value) return []
  return allCourses.value
    .filter(course => course.dayOfWeek === currentDay.value + 1)
    .sort((a, b) => a.startTime.localeCompare(b.startTime))
})

onMounted(async () => {
  try {
    const user = authStore.user
    const userRole = user.role
    
    let schedules = []
    
    if (userRole === 'student') {
      // 学生：通过选课记录获取课表
      schedules = await getStudentSchedules(user.id)
      // 处理DTO为前端友好格式
      allCourses.value = schedules.map(s => ({
        id: s.id,
        dayOfWeek: s.dayOfWeek,
        startTime: s.startTime,
        endTime: s.endTime,
        courseName: s.courseName || '未知课程',
        classroom: {
          building: s.building,
          classroomName: s.classroomName
        },
        teacherName: s.teacherName || '未知教师',
        classCode: s.classCode || '未知班级'
      }))
    } else if (userRole === 'teacher') {
      // 教师：通过教师ID获取其授课的所有课表
      const teacherId = user.roleId
      schedules = await getTeacherSchedules(teacherId)
      
      // 处理教师课表数据
      allCourses.value = schedules.map(s => ({
        id: s.id,
        dayOfWeek: s.dayOfWeek,
        startTime: s.startTime,
        endTime: s.endTime,
        courseName: s.courseName || '未知课程',
        classroom: {
          building: s.building,
          classroomName: s.classroomName
        },
        teacherName: '本人授课', // 教师查看自己的课表
        classCode: s.classCode || '未知班级'
      }))
    } else {
      uni.showToast({ title: '暂不支持该角色的课表查看', icon: 'none' })
    }
  } catch (error) {
    console.error("获取课表失败:", error)
    uni.showToast({ title: '获取课表失败，请稍后重试', icon: 'error' })
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.schedule-page {
  display: flex;
  flex-direction: column;
  height: 100vh;
  background-color: #f4f6f9;
}
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1.5rem 2rem;
  background-color: #fff;
  color: #1f2d3d;
}
.page-header .el-icon {
  font-size: 2rem;
  cursor: pointer;
}
.page-title {
  margin: 0;
  font-size: 1.8rem;
  font-weight: 600;
}
.header-placeholder {
  width: 2rem;
}

.week-switcher {
  display: flex;
  justify-content: space-around;
  padding: 1rem;
  background-color: #fff;
  box-shadow: 0 0.2rem 0.8rem rgba(0,0,0,0.05);
}

.schedule-content {
  flex: 1;
  padding: 2rem;
  overflow-y: auto;
}

.loading-state, .empty-state {
  text-align: center;
  margin-top: 10rem;
  color: #8492a6;
}
.empty-state p, .loading-state p {
  margin-top: 1rem;
  font-size: 1.4rem;
}

.course-list {
  display: flex;
  flex-direction: column;
  gap: 1.5rem;
}

.course-card {
  display: flex;
  align-items: center;
  background-color: #fff;
  border-radius: 1.2rem;
  padding: 1.5rem;
  box-shadow: 0 0.4rem 1.5rem rgba(0,0,0,0.05);
}
.time-info {
  width: 6rem;
  text-align: center;
  border-right: 1px solid #e5e9f2;
  margin-right: 1.5rem;
  padding-right: 1.5rem;
}
.start-time {
  font-size: 1.8rem;
  font-weight: 600;
  color: #1f2d3d;
  margin: 0;
}
.end-time {
  font-size: 1.3rem;
  color: #8492a6;
  margin: 0.4rem 0 0;
}

.course-details {
  flex: 1;
}
.course-name {
  font-size: 1.6rem;
  font-weight: 600;
  margin: 0 0 0.8rem;
}
.course-details p {
  display: flex;
  align-items: center;
  margin: 0.4rem 0;
  font-size: 1.3rem;
  color: #8492a6;
}
.course-details .el-icon {
  margin-right: 0.6rem;
  font-size: 1.4rem;
}
</style> 