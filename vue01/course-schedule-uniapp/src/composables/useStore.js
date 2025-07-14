// 延迟导入，避免全局调用
// 封装store使用，处理uniapp环境
export function useAuthStore() {
  try {
    // 局部导入，避免全局调用
    const { useAuthStore: _useAuthStore } = require('@/stores')
    return _useAuthStore()
  } catch (error) {
    console.error('AuthStore初始化失败:', error)
    // 返回一个mock对象，避免应用崩溃
    return {
      login: async () => { throw new Error('认证服务不可用') },
      register: async () => { throw new Error('认证服务不可用') },
      logout: () => {},
      isAuthenticated: false,
      user: null
    }
  }
}

export function useCourseStore() {
  try {
    // 局部导入，避免全局调用
    const { useCourseStore: _useCourseStore } = require('@/stores')
    return _useCourseStore()
  } catch (error) {
    console.error('CourseStore初始化失败:', error)
    return {
      // 提供基本的mock方法
      fetchStudentSchedules: async () => [],
      fetchMyCourses: async () => [],
      selectCourse: async () => ({ success: false, message: '服务不可用' }),
      loading: { schedules: false, courses: false, selection: false }
    }
  }
}