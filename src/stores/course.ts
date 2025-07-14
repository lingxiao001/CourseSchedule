import { defineStore } from 'pinia';
import { studentApi, teacherApi, adminApi } from '@/api';
import type { CourseSchedule, MyCourse, TeachingClass, Course } from '@/api';

interface CourseState {
  // 学生相关
  mySchedules: CourseSchedule[];
  myCourses: MyCourse[];
  availableCourses: TeachingClass[];
  
  // 教师相关
  teachingClasses: TeachingClass[];
  teacherSchedules: CourseSchedule[];
  
  // 管理员相关
  allCourses: Course[];
  
  // 加载状态
  loading: {
    schedules: boolean;
    courses: boolean;
    selection: boolean;
  };
}

export const useCourseStore = defineStore('course', {
  state: (): CourseState => ({
    mySchedules: [],
    myCourses: [],
    availableCourses: [],
    teachingClasses: [],
    teacherSchedules: [],
    allCourses: [],
    loading: {
      schedules: false,
      courses: false,
      selection: false
    }
  }),

  getters: {
    // 按星期分组的课表
    schedulesByDay: (state) => {
      const grouped: Record<number, CourseSchedule[]> = {};
      state.mySchedules.forEach(schedule => {
        if (!grouped[schedule.dayOfWeek]) {
          grouped[schedule.dayOfWeek] = [];
        }
        grouped[schedule.dayOfWeek].push(schedule);
      });
      return grouped;
    },

    // 今日课程
    todaySchedules: (state) => {
      const today = new Date().getDay();
      return state.mySchedules.filter(schedule => schedule.dayOfWeek === today);
    },

    // 课程统计
    courseStats: (state) => ({
      totalCourses: state.myCourses.length,
      totalClasses: state.teachingClasses.length,
      todayClasses: state.mySchedules.filter(s => s.dayOfWeek === new Date().getDay()).length
    })
  },

  actions: {
    // ========== 学生相关操作 ==========
    /**
     * 获取学生课表
     */
    async fetchStudentSchedules(studentId: number) {
      this.loading.schedules = true;
      try {
        this.mySchedules = await studentApi.getStudentSchedules(studentId);
      } catch (error) {
        console.error('获取学生课表失败:', error);
        throw error;
      } finally {
        this.loading.schedules = false;
      }
    },

    /**
     * 获取学生选课列表
     */
    async fetchMyCourses(studentId: number) {
      this.loading.courses = true;
      try {
        this.myCourses = await studentApi.getSelectionsByStudentWithTeachers(studentId);
      } catch (error) {
        console.error('获取我的课程失败:', error);
        throw error;
      } finally {
        this.loading.courses = false;
      }
    },

    /**
     * 选课
     */
    async selectCourse(studentId: number, teachingClassId: number) {
      this.loading.selection = true;
      try {
        const result = await studentApi.selectCourse(studentId, teachingClassId);
        if (result.success) {
          // 刷新课程列表
          await this.fetchMyCourses(studentId);
          await this.fetchStudentSchedules(studentId);
        }
        return result;
      } catch (error) {
        console.error('选课失败:', error);
        throw error;
      } finally {
        this.loading.selection = false;
      }
    },

    /**
     * 退课
     */
    async cancelSelection(studentId: number, teachingClassId: number) {
      this.loading.selection = true;
      try {
        const result = await studentApi.cancelSelection(studentId, teachingClassId);
        if (result.success) {
          // 刷新课程列表
          await this.fetchMyCourses(studentId);
          await this.fetchStudentSchedules(studentId);
        }
        return result;
      } catch (error) {
        console.error('退课失败:', error);
        throw error;
      } finally {
        this.loading.selection = false;
      }
    },

    // ========== 教师相关操作 ==========
    /**
     * 获取教师教学班
     */
    async fetchTeachingClasses(teacherId: number) {
      this.loading.courses = true;
      try {
        this.teachingClasses = await teacherApi.getTeachingClasses(teacherId);
      } catch (error) {
        console.error('获取教学班失败:', error);
        throw error;
      } finally {
        this.loading.courses = false;
      }
    },

    /**
     * 获取教师课表
     */
    async fetchTeacherSchedule(teacherId: number) {
      this.loading.schedules = true;
      try {
        this.teacherSchedules = await teacherApi.getTeacherSchedule(teacherId);
      } catch (error) {
        console.error('获取教师课表失败:', error);
        throw error;
      } finally {
        this.loading.schedules = false;
      }
    },

    /**
     * 获取教学班详情
     */
    async fetchTeachingClassDetail(classId: number) {
      try {
        return await teacherApi.getTeachingClassDetail(classId);
      } catch (error) {
        console.error('获取教学班详情失败:', error);
        throw error;
      }
    },

    // ========== 管理员相关操作 ==========
    /**
     * 获取所有课程
     */
    async fetchAllCourses() {
      this.loading.courses = true;
      try {
        this.allCourses = await adminApi.getCourses();
      } catch (error) {
        console.error('获取所有课程失败:', error);
        throw error;
      } finally {
        this.loading.courses = false;
      }
    },

    /**
     * 创建课程
     */
    async createCourse(course: Omit<Course, 'id'>) {
      try {
        const newCourse = await adminApi.createCourse(course);
        this.allCourses.push(newCourse);
        return newCourse;
      } catch (error) {
        console.error('创建课程失败:', error);
        throw error;
      }
    },

    /**
     * 更新课程
     */
    async updateCourse(id: number, course: Partial<Course>) {
      try {
        const updatedCourse = await adminApi.updateCourse(id, course);
        const index = this.allCourses.findIndex(c => c.id === id);
        if (index !== -1) {
          this.allCourses[index] = updatedCourse;
        }
        return updatedCourse;
      } catch (error) {
        console.error('更新课程失败:', error);
        throw error;
      }
    },

    /**
     * 删除课程
     */
    async deleteCourse(id: number) {
      try {
        await adminApi.deleteCourse(id);
        this.allCourses = this.allCourses.filter(c => c.id !== id);
      } catch (error) {
        console.error('删除课程失败:', error);
        throw error;
      }
    },

    // ========== 通用操作 ==========
    /**
     * 清空状态
     */
    clearState() {
      this.mySchedules = [];
      this.myCourses = [];
      this.availableCourses = [];
      this.teachingClasses = [];
      this.teacherSchedules = [];
      this.allCourses = [];
    },

    /**
     * 重置加载状态
     */
    resetLoading() {
      this.loading = {
        schedules: false,
        courses: false,
        selection: false
      };
    }
  }
});