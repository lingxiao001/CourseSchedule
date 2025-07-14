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
  name: string;
  capacity: number;
  location?: string;
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
    return apiClient.get<Classroom[]>('/admin/classrooms');
  },

  /**
   * 创建教室
   */
  async createClassroom(classroom: Omit<Classroom, 'id'>): Promise<Classroom> {
    return apiClient.post<Classroom>('/admin/classrooms', classroom);
  },

  /**
   * 更新教室信息
   */
  async updateClassroom(id: number, classroom: Partial<Classroom>): Promise<Classroom> {
    return apiClient.put<Classroom>(`/admin/classrooms/${id}`, classroom);
  },

  /**
   * 删除教室
   */
  async deleteClassroom(id: number): Promise<void> {
    return apiClient.delete<void>(`/admin/classrooms/${id}`);
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
  }
};