import apiClient from '@/utils/request';

export interface TeachingClass {
  id: number;
  courseName: string;
  classCode: string;
  teacherId: number;
  teacherName: string;
  semester: string;
  studentCount: number;
  maxStudents: number;
  students?: Student[];
}

export interface Student {
  id: number;
  studentId: string;
  realName: string;
  username: string;
}

export interface TeacherCourse {
  id: number;
  name: string;
  credits: number;
  description?: string;
  teachingClasses: TeachingClass[];
}

export interface CourseSchedule {
  id: number;
  teachingClassId: number;
  courseName: string;
  classCode: string;
  dayOfWeek: number;
  startTime: string;
  endTime: string;
  classroom: string;
}

export const teacherApi = {
  // ========== 课程管理 ==========
  /**
   * 获取教师的所有课程
   */
  async getTeacherCourses(teacherId: number): Promise<TeacherCourse[]> {
    return apiClient.get<TeacherCourse[]>(`/teacher/courses/${teacherId}`);
  },

  /**
   * 获取课程详情
   */
  async getCourseDetail(courseId: number): Promise<TeacherCourse> {
    return apiClient.get<TeacherCourse>(`/teacher/courses/detail/${courseId}`);
  },

  // ========== 教学班管理 ==========
  /**
   * 获取教师的所有教学班
   */
  async getTeachingClasses(teacherId: number): Promise<TeachingClass[]> {
    return apiClient.get<TeachingClass[]>(`/teacher/teaching-classes/${teacherId}`);
  },

  /**
   * 获取教学班详情（包含学生名单）
   */
  async getTeachingClassDetail(classId: number): Promise<TeachingClass> {
    return apiClient.get<TeachingClass>(`/teacher/teaching-classes/detail/${classId}`);
  },

  /**
   * 获取教学班的学生名单
   */
  async getClassStudents(classId: number): Promise<Student[]> {
    return apiClient.get<Student[]>(`/teacher/teaching-classes/${classId}/students`);
  },

  /**
   * 更新教学班信息
   */
  async updateTeachingClass(classId: number, data: Partial<TeachingClass>): Promise<TeachingClass> {
    return apiClient.put<TeachingClass>(`/teacher/teaching-classes/${classId}`, data);
  },

  // ========== 课程安排查看 ==========
  /**
   * 获取教师的课程安排
   */
  async getTeacherSchedule(teacherId: number): Promise<CourseSchedule[]> {
    return apiClient.get<CourseSchedule[]>(`/teacher/schedule/${teacherId}`);
  },

  /**
   * 获取特定教学班的课程安排
   */
  async getClassSchedule(classId: number): Promise<CourseSchedule[]> {
    return apiClient.get<CourseSchedule[]>(`/teacher/schedule/class/${classId}`);
  },

  /**
   * 按周获取教师课程安排
   */
  async getWeeklySchedule(teacherId: number, weekStart: string): Promise<CourseSchedule[]> {
    return apiClient.get<CourseSchedule[]>(`/teacher/schedule/${teacherId}/week`, {
      weekStart
    });
  },

  // ========== 学生管理 ==========
  /**
   * 从教学班中移除学生
   */
  async removeStudentFromClass(classId: number, studentId: number): Promise<void> {
    return apiClient.delete<void>(`/teacher/teaching-classes/${classId}/students/${studentId}`);
  },

  /**
   * 向教学班添加学生
   */
  async addStudentToClass(classId: number, studentId: number): Promise<void> {
    return apiClient.post<void>(`/teacher/teaching-classes/${classId}/students/${studentId}`);
  },

  // ========== 统计信息 ==========
  /**
   * 获取教师的教学统计
   */
  async getTeachingStats(teacherId: number): Promise<{
    totalCourses: number;
    totalClasses: number;
    totalStudents: number;
    averageClassSize: number;
  }> {
    return apiClient.get(`/teacher/stats/${teacherId}`);
  },

  /**
   * 获取课程选课情况统计
   */
  async getCourseSelectionStats(courseId: number): Promise<{
    totalSelections: number;
    byClass: Array<{
      classId: number;
      className: string;
      selectionCount: number;
      capacity: number;
    }>;
  }> {
    return apiClient.get(`/teacher/stats/course/${courseId}/selections`);
  }
};