import { createRouter, createWebHistory } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import SelectCourse from '@/views/student/SelectCourse.vue'
import CourseSchedule from '@/views/admin/CourseSchedule.vue';
import RecentSchedules from '@/views/admin/RecentSchedules.vue';
const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/auth/LoginView.vue'),
    meta: { title: '登录', guestOnly: true }
  },
  {
    path: '/',
    redirect: '/dashboard',
    meta: { requiresAuth: true }
  },
  {
    path: '/student/selectCourse',
    name: 'SelectCourse',
    component: SelectCourse,
    meta: { title: '选课', requiresAuth: true }
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: () => import('@/views/DashboardView.vue'),
    meta: { title: '仪表盘', requiresAuth: true }
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

router.beforeEach((to, from, next) => {
  const authStore = useAuthStore()
  const isAuthenticated = authStore.isAuthenticated

  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} | 课程系统` : '课程系统'

  // 需要登录且未认证
  if (to.meta.requiresAuth && !isAuthenticated) {
    return next('/login')
  }

  // 已登录用户访问guest页面
  if (to.meta.guestOnly && isAuthenticated) {
    return next('/dashboard')
  }

  next()
})

export default router