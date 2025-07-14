// API统一导出文件
export { authApi } from './auth';
export { studentApi } from './student';
export { teacherApi } from './teacher';
export { adminApi } from './admin';

// 导出类型定义
export type { LoginCredentials, RegisterPayload, UserInfo, AuthResponse } from './auth';
export type { CourseSchedule, MyCourse, SelectionResult } from './student';
export type { TeachingClass, Student, TeacherCourse } from './teacher';
export type { User, Course, Classroom, ScheduleConfig, StatsData } from './admin';

// 导出请求工具
export { default as apiClient } from '../utils/request';