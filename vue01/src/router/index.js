import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import SelectCourse from '@/views/student/SelectCourse.vue'
import CourseSchedule from '@/views/admin/CourseSchedule.vue';
import RecentSchedules from '@/views/admin/RecentSchedules.vue';
import MyCourses from '@/views/student/MyCourses.vue'

// 导入组件
import MobileDashboard from '@/views/DashboardView.vue'
import StudentDashboard from '@/components/StudentDashboard.vue'
import TeacherDashboard from '@/components/TeacherDashboard.vue'
import AdminDashboard from '@/components/AdminDashboard.vue'
import SelectCourseMobile from '@/views/student/SelectCourseMobile.vue'
import UserManagementMobile from '@/views/admin/UserManagementMobile.vue'
import StatsMobile from '@/views/admin/StatsMobile.vue'

// 设备检测函数（放顶部方便路由数组直接使用）
const isMobileDevice = () => /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent)

/* --------------------------------------------------
 * 1. 路由定义
 * --------------------------------------------------*/
const commonRoutes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: { title: '登录', guestOnly: true }
  },
  {
    path: '/:pathMatch(.*)*',
    redirect: '/login'
  }
]

// ===== PC 端路由 =====
const pcRoutes = [
  {
    path: '/',
    name: 'Home',
    meta: { requiresAuth: true }
  },
  // Dashboard - 按角色拆分
  {
    path: '/dashboard/admin',
    name: 'AdminDashboardPC',
    component: AdminDashboard,
    meta: { title: '管理员仪表盘', requiresAuth: true, role: 'admin' }
  },
  {
    path: '/dashboard/teacher',
    name: 'TeacherDashboardPC',
    component: TeacherDashboard,
    meta: { title: '教师仪表盘', requiresAuth: true, role: 'teacher' }
  },
  {
    path: '/dashboard/student',
    name: 'StudentDashboardPC',
    component: StudentDashboard,
    meta: { title: '学生仪表盘', requiresAuth: true, role: 'student' }
  },
  // 教师端额外功能
  {
    path: '/teacher/courses',
    name: 'TeacherCourses',
    component: () => import('@/views/teacher/CourseOpt.vue'),
    meta: { title: '课程管理', requiresAuth: true, role: 'teacher' }
  },
  {
    path: '/teacher/classes',
    name: 'TeacherTeachingClasses',
    component: () => import('@/views/teacher/TeachingClasses.vue'),
    meta: { requiresAuth: true, roles: ['teacher', 'admin'] }
  },
  // 学生端
  {
    path: '/student/selectCourse',
    name: 'SelectCourse',
    component: SelectCourse,
    meta: { title: '选课', requiresAuth: true, role: 'student' }
  },
  {
    path: '/student/my-courses',
    name: 'MyCourses',
    component: MyCourses,
    meta: { requiresAuth: true, role: 'student' }
  },
  // 管理员端
  {
    path: '/admin/users',
    component: UserManagementMobile,
    meta: { title: '用户管理', requiresAuth: true, role: 'admin' }
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
  },
  {
    path: '/admin/courses',
    name: 'AdminCourseManagement',
    component: () => import('@/views/admin/CourseManagement.vue'),
    meta: { requiresAuth: true, role: 'admin', title: '课程管理' }
  }
]

// ===== 移动端路由 =====
const mobileRoutes = [
  {
    path: '/',
    redirect: '/m/dashboard'
  },
  {
    path: '/m/dashboard',
    name: 'DashboardMobile',
    component: MobileDashboard,
    meta: { title: '仪表盘', requiresAuth: true }
  },
  {
    path: '/schedule',
    name: 'MySchedule',
    component: () => import('@/views/MySchedule.vue'),
    meta: { title: '我的课表', requiresAuth: true }
  },
  // 移动端使用同一组件进行选课/个人中心等
  {
    path: '/student/selectCourse',
    name: 'SelectCourseMobile',
    component: SelectCourseMobile,
    meta: { title: '选课', requiresAuth: true, role: 'student' }
  },
  {
    path: '/profile',
    name: 'MyProfile',
    component: () => import('@/views/profile/MyProfile.vue'),
    meta: { requiresAuth: true, title: '我的' }
  },
  {
    path: '/student/my-courses',
    name: 'MyCoursesMobile',
    component: MyCourses,
    meta: { requiresAuth: true, role: 'student' }
  },
  {
    path: '/admin/users',
    name: 'AdminUsersMobile',
    component: UserManagementMobile,
    meta: { requiresAuth: true, role: 'admin' }
  },
  {
    path: '/admin/stats',
    name: 'StatsMobile',
    component: StatsMobile,
    meta: { requiresAuth: true, role: 'admin' }
  },
  {
    path: '/admin/courses',
    name: 'AdminCourseMobile',
    component: () => import('@/views/admin/CourseManagementMobile.vue'),
    meta: { requiresAuth: true, role: 'admin' }
  },
  {
    path: '/admin/classrooms',
    name: 'ClassroomMobile',
    component: () => import('@/views/admin/ClassroomManagementMobile.vue'),
    meta: { requiresAuth: true, role: 'admin' }
  },
  {
    path: '/admin/manual-schedule',
    name: 'ManualScheduleMobile',
    component: () => import('@/views/admin/ManualScheduleMobile.vue'),
    meta: { requiresAuth: true, role: 'admin' }
  },
  {
    path: '/teacher/classes-mobile',
    name: 'TeacherTeachingClassesMobile',
    component: () => import('@/views/teacher/TeachingClassesMobile.vue'),
    meta: { requiresAuth: true, roles: ['teacher', 'admin'] }
  },
]

// 合并公共路由
const routes = (isMobileDevice() ? mobileRoutes : pcRoutes).concat(commonRoutes)

/* --------------------------------------------------
 * 2. 创建 Router
 * --------------------------------------------------*/
const router = createRouter({
  history: createWebHistory(),
  routes
})

/* --------------------------------------------------
 * 3. 路由守卫
 * --------------------------------------------------*/
router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  const isAuthenticated = authStore.isAuthenticated

  // 设置页面 title
  document.title = to.meta.title ? `${to.meta.title} | 课程系统` : '课程系统'

  // 登录页拦截：已登录状态下，跳转首页
  if (isAuthenticated && to.meta.guestOnly) {
    return next('/')
  }

  // PC 端根路径跳转：根据角色定向到专属 Dashboard
  if (to.name === 'Home') {
    if (isMobileDevice()) {
      return next('/') // 移动端 Home 已被 redirect 定义，无需处理
    }
    const role = authStore.user?.role
    switch (role) {
      case 'admin':
        return next({ name: 'AdminDashboardPC' })
      case 'teacher':
        return next({ name: 'TeacherDashboardPC' })
      case 'student':
        return next({ name: 'StudentDashboardPC' })
      default:
        break
    }
  }

  // 需要登录但未认证，强制跳转登录
  if (to.meta.requiresAuth && !isAuthenticated) {
    return next({ name: 'Login' })
  }

  // 角色权限校验（管理员拥有所有权限）
  const userRole = authStore.user?.role
  if (to.meta.role && userRole && userRole !== 'admin' && to.meta.role !== userRole) {
    return next('/')
  }
  if (to.meta.roles && userRole && userRole !== 'admin' && !to.meta.roles.includes(userRole)) {
    return next('/')
  }

  next()
})

export default router