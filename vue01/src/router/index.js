import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import SelectCourse from '@/views/student/SelectCourse.vue'
import CourseSchedule from '@/views/admin/CourseSchedule.vue';
import RecentSchedules from '@/views/admin/RecentSchedules.vue';
import MyCourses from '@/views/student/MyCourses.vue'

// 导入新的移动端Dashboard组件
import MobileDashboard from '@/views/DashboardView.vue'; 

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: { title: '登录', guestOnly: true }
  },
  {
    path: '/',
    name: 'Home', // 给予一个name
    meta: { requiresAuth: true }
    // 移除redirect，让路由守卫来处理
  },
  {
    path: '/student/selectCourse',
    name: 'SelectCourse',
    component: SelectCourse,
    meta: { title: '选课', requiresAuth: true }
  },
  {
    path: '/student/my-courses',
    name: 'MyCourses',
    component: MyCourses,
    meta: { requiresAuth: true, role: 'STUDENT' }
  },
  {
    path: '/dashboard',
    name: 'DashboardPC', // 重命名为PC Dashboard
    component: () => import('@/components/AdminDashboard.vue'), // 假设旧的Dashboard是这个
    meta: { title: '仪表盘 (PC)', requiresAuth: true, pcOnly: true }
  },
  {
    path: '/m/dashboard', // 新增移动端路由
    name: 'DashboardMobile',
    component: MobileDashboard,
    meta: { title: '仪表盘', requiresAuth: true, mobileOnly: true }
  },
  {
    path: '/schedule', // 新增课表页路由
    name: 'MySchedule',
    component: () => import('@/views/MySchedule.vue'),
    meta: { title: '我的课表', requiresAuth: true, mobileOnly: true }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/login'
  },
  {
    path: '/teacher/courses',
    name: 'TeacherCourses',
    component: () => import('@/views/teacher/CourseOpt.vue'),
    meta: { 
      title: '课程管理',
      requiresAuth: true,
      role: 'teacher' 
    }
  },
    {
    path: '/teacher/Classes',
    name: 'TeachingClasses',
    component: () => import('@/views/teacher/TeachingClasses.vue'),
    meta: { 
      title: '课程管理',
      requiresAuth: true,
      role: 'teacher' 
    }
  },
  {
    path: '/admin/users',
    component: () => import('@/views/admin/UserManagement.vue'),
    meta: { title: '用户管理' }
  },
  {
    path: '/admin/users/course-schedule',
    name: 'CourseSchedule',
    component: CourseSchedule,
    meta: { requiresAuth: true, role: 'admin' }
  },
  {
    path: '/admin/recent-schedules',
    name: 'RecentSchedules',
    component: RecentSchedules,
    meta: { requiresAuth: true, role: 'admin' }
  }
];
const router = createRouter({
  history: createWebHistory(),
  routes
})

// 设备检测函数
const isMobileDevice = () => {
  return /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);
}

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  const isAuthenticated = authStore.isAuthenticated

  document.title = to.meta.title ? `${to.meta.title} | 课程系统` : '课程系统'

  // 核心逻辑：登录后的智能跳转
  if (isAuthenticated) {
    if (to.name === 'Home') { // 从登录页跳转到根路径时
      return isMobileDevice() ? next({ name: 'DashboardMobile' }) : next({ name: 'DashboardPC' });
    }
    if (to.meta.guestOnly) { // 如果已登录，尝试访问登录页
       return isMobileDevice() ? next({ name: 'DashboardMobile' }) : next({ name: 'DashboardPC' });
    }
  }

  // 需要登录但未认证，强制跳转到登录页
  if (to.meta.requiresAuth && !isAuthenticated) {
    return next({ name: 'Login' });
  }

  next()
})

export default router