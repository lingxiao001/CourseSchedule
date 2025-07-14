import apiClient from '@/utils/request';

export interface User {
  id: number;
  username: string;
  real_name: string; // 匹配后端字段名
  role: 'student' | 'teacher' | 'admin';
  roleId?: number;
  createdAt?: string;
  // 学生相关字段
  grade?: string;
  className?: string;
  // 教师相关字段
  title?: string;
  department?: string;
}

export interface UserCreateRequest {
  username: string;
  password: string;
  realName: string;
  role: 'student' | 'teacher';
  // 学生相关字段
  studentId?: number;
  grade?: string;
  className?: string;
  // 教师相关字段
  teacherId?: number;
  title?: string;
  department?: string;
}

export interface UserUpdateRequest {
  real_name?: string;
  role?: 'student' | 'teacher' | 'admin';
  // 学生相关字段
  studentId?: number;
  grade?: string;
  className?: string;
  // 教师相关字段
  teacherId?: number;
  title?: string;
  department?: string;
  newPassword?: string;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}

export interface Course {
  id: number;
  courseName: string;
  classCode: string;
  credit: number;
  hours: number;
  description?: string;
  created_at?: string;
}

export interface TeachingClass {
  id: number;
  classCode: string;
  maxStudents: number;
  currentStudents: number;
  courseId: number;
  teacherId: number;
  teacherName?: string;
}

export interface Classroom {
  id: number;
  building: string;
  classroomName: string;
  capacity: number;
  available?: boolean;
}

export interface ScheduleConfig {
  semester: string;
  startDate: string;
  endDate: string;
  timeSlots: TimeSlot[];
}

export interface TimeSlot {
  id: number;
  startTime: string;
  endTime: string;
  period: string;
}

export interface StatsData {
  totalStudents: number;
  totalTeachers: number;
  totalCourses: number;
  totalClasses: number;
  recentRegistrations: number;
  activeSelections: number;
}

export interface Schedule {
  id: number;
  dayOfWeek: number; // 1-7, 1为周一
  startTime: string;
  endTime: string;
  classroomId: number;
  building: string;
  classroomName: string;
  teachingClassId: number;
  week?: number;
  semester?: string;
}

export interface ScheduleCreateRequest {
  dayOfWeek: number;
  startTime: string;
  endTime: string;
  classroomId: number;
  building?: string;
  classroomName?: string;
  week?: number;
  semester?: string;
}

export interface ScheduleUpdateRequest {
  dayOfWeek?: number;
  startTime?: string;
  endTime?: string;
  classroomId?: number;
  building?: string;
  classroomName?: string;
  week?: number;
  semester?: string;
}

export const adminApi = {
  // ========== 用户管理 ==========
  /**
   * 获取所有用户（分页）
   */
  async getUsers(params: { page?: number; size?: number; search?: string } = {}): Promise<PaginatedResponse<User>> {
    return apiClient.get<PaginatedResponse<User>>('/admin/users', params);
  },

  /**
   * 获取单个用户
   */
  async getUser(userId: number): Promise<User> {
    return apiClient.get<User>(`/admin/users/${userId}`);
  },

  /**
   * 创建用户
   */
  async createUser(user: UserCreateRequest): Promise<User> {
    return apiClient.post<User>('/admin/users', user);
  },

  /**
   * 更新用户信息
   */
  async updateUser(id: number, user: UserUpdateRequest): Promise<User> {
    return apiClient.put<User>(`/admin/users/${id}`, user);
  },

  /**
   * 删除用户
   */
  async deleteUser(id: number): Promise<void> {
    return apiClient.delete<void>(`/admin/users/${id}`);
  },

  // ========== 课程管理 ==========
  /**
   * 获取所有课程
   */
  async getCourses(): Promise<Course[]> {
    return apiClient.get<Course[]>('/courses');
  },

  /**
   * 获取单个课程
   */
  async getCourse(courseId: number): Promise<Course> {
    return apiClient.get<Course>(`/courses/${courseId}`);
  },

  /**
   * 创建课程
   */
  async createCourse(course: Omit<Course, 'id' | 'created_at'>): Promise<Course> {
    return apiClient.post<Course>('/courses', course);
  },

  /**
   * 更新课程信息
   */
  async updateCourse(id: number, course: Partial<Omit<Course, 'id' | 'created_at'>>): Promise<Course> {
    return apiClient.put<Course>(`/courses/${id}`, course);
  },

  /**
   * 删除课程
   */
  async deleteCourse(id: number): Promise<void> {
    return apiClient.delete<void>(`/courses/${id}`);
  },

  // ========== 教学班管理 ==========
  /**
   * 获取所有教学班
   */
  async getTeachingClasses(): Promise<TeachingClass[]> {
    return apiClient.get<TeachingClass[]>('/courses/classes');
  },

  /**
   * 获取指定课程的教学班
   */
  async getTeachingClassesByCourse(courseId: number): Promise<TeachingClass[]> {
    return apiClient.get<TeachingClass[]>(`/courses/${courseId}/classes`);
  },

  /**
   * 创建教学班
   */
  async createTeachingClass(courseId: number, teachingClass: Omit<TeachingClass, 'id' | 'courseId' | 'teacherName'>): Promise<TeachingClass> {
    return apiClient.post<TeachingClass>(`/courses/${courseId}/classes`, teachingClass);
  },

  /**
   * 更新教学班信息
   */
  async updateTeachingClass(classId: number, teachingClass: Partial<Omit<TeachingClass, 'id' | 'courseId' | 'teacherName'>>): Promise<TeachingClass> {
    return apiClient.put<TeachingClass>(`/courses/classes/${classId}`, teachingClass);
  },

  /**
   * 删除教学班
   */
  async deleteTeachingClass(classId: number): Promise<void> {
    return apiClient.delete<void>(`/courses/classes/${classId}`);
  },

  // ========== 教室管理 ==========
  /**
   * 获取所有教室
   */
  async getClassrooms(): Promise<Classroom[]> {
    return apiClient.get<Classroom[]>('/classrooms');
  },

  /**
   * 创建教室
   */
  async createClassroom(classroom: Omit<Classroom, 'id'>): Promise<Classroom> {
    return apiClient.post<Classroom>('/classrooms', classroom);
  },

  /**
   * 更新教室信息
   */
  async updateClassroom(id: number, classroom: Partial<Classroom>): Promise<Classroom> {
    return apiClient.put<Classroom>(`/classrooms/${id}`, classroom);
  },

  /**
   * 删除教室
   */
  async deleteClassroom(id: number): Promise<void> {
    return apiClient.delete<void>(`/classrooms/${id}`);
  },

  /**
   * 根据时间段获取可用教室
   */
  async getAvailableClassrooms(params: { dayOfWeek: number; startTime: string; endTime: string }): Promise<Classroom[]> {
    return apiClient.get<Classroom[]>('/classrooms/available', params);
  },

  /**
   * 根据教学楼获取教室
   */
  async getClassroomsByBuilding(building: string): Promise<Classroom[]> {
    return apiClient.get<Classroom[]>(`/classrooms/building/${building}`);
  },

  // ========== 自动排课 ==========
  /**
   * 启动自动排课
   */
  async startAutoSchedule(config: { semester: string; startDate: string }): Promise<any> {
    return apiClient.post('/admin/auto-schedule', config);
  },

  /**
   * 获取排课结果
   */
  async getScheduleResult(scheduleId: string): Promise<any> {
    return apiClient.get(`/admin/schedule-result/${scheduleId}`);
  },

  /**
   * 获取最近的排课记录
   */
  async getRecentSchedules(): Promise<any[]> {
    return apiClient.get('/admin/recent-schedules');
  },

  // ========== 数据统计 ==========
  /**
   * 获取系统统计数据
   */
  async getStats(): Promise<StatsData> {
    return apiClient.get<StatsData>('/admin/stats');
  },

  /**
   * 获取课程统计图表数据
   */
  async getCourseStats(): Promise<any> {
    return apiClient.get('/admin/stats/courses');
  },

  /**
   * 获取用户注册趋势
   */
  async getRegistrationTrends(): Promise<any> {
    return apiClient.get('/admin/stats/registrations');
  },

  // ========== 系统配置 ==========
  /**
   * 获取排课配置
   */
  async getScheduleConfig(): Promise<ScheduleConfig> {
    return apiClient.get<ScheduleConfig>('/admin/schedule-config');
  },

  /**
   * 更新排课配置
   */
  async updateScheduleConfig(config: ScheduleConfig): Promise<ScheduleConfig> {
    return apiClient.put<ScheduleConfig>('/admin/schedule-config', config);
  },

  // ========== 课程安排管理 ==========
  /**
   * 获取指定教学班的课程安排
   */
  async getSchedulesByTeachingClass(teachingClassId: number, params?: { week?: number; semester?: string }): Promise<Schedule[]> {
    return apiClient.get<Schedule[]>(`/schedules/teaching-class/${teachingClassId}`, params);
  },

  /**
   * 获取所有课程安排
   */
  async getAllSchedules(params?: { page?: number; size?: number; week?: number; semester?: string }): Promise<PaginatedResponse<Schedule>> {
    return apiClient.get<PaginatedResponse<Schedule>>('/schedules', params);
  },

  /**
   * 获取单个课程安排
   */
  async getSchedule(scheduleId: number): Promise<Schedule> {
    return apiClient.get<Schedule>(`/schedules/${scheduleId}`);
  },

  /**
   * 为教学班添加课程安排
   */
  async addSchedule(teachingClassId: number, schedule: ScheduleCreateRequest): Promise<Schedule> {
    return apiClient.post<Schedule>(`/schedules/teaching-class/${teachingClassId}`, schedule);
  },

  /**
   * 更新课程安排
   */
  async updateSchedule(scheduleId: number, schedule: ScheduleUpdateRequest): Promise<Schedule> {
    return apiClient.put<Schedule>(`/schedules/${scheduleId}`, schedule);
  },

  /**
   * 删除课程安排
   */
  async deleteSchedule(scheduleId: number): Promise<void> {
    return apiClient.delete<void>(`/schedules/${scheduleId}`);
  },

  /**
   * 批量添加课程安排
   */
  async batchAddSchedules(teachingClassId: number, schedules: ScheduleCreateRequest[]): Promise<Schedule[]> {
    return apiClient.post<Schedule[]>(`/schedules/teaching-class/${teachingClassId}/batch`, { schedules });
  },

  /**
   * 检查课程安排冲突
   */
  async checkScheduleConflict(schedule: ScheduleCreateRequest): Promise<{ hasConflict: boolean; conflicts: Schedule[] }> {
    return apiClient.post<{ hasConflict: boolean; conflicts: Schedule[] }>('/schedules/check-conflict', schedule);
  },

  /**
   * 获取教室在指定时间的可用性
   */
  async getClassroomAvailability(classroomId: number, params: { dayOfWeek: number; startTime: string; endTime: string; week?: number }): Promise<{ available: boolean; conflictSchedules: Schedule[] }> {
    return apiClient.get<{ available: boolean; conflictSchedules: Schedule[] }>(`/classrooms/${classroomId}/availability`, params);
  }
};