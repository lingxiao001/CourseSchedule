import apiClient from '@/utils/request';

export interface CourseSchedule {
  id: number;
  courseName: string;
  classCode: string;
  teachingClassId: number;
  teacherName: string;
  dayOfWeek: number;
  startTime: string;
  endTime: string;
  classroom?: string;
  isStudentCourse?: boolean;
}

export interface MyCourse {
  teachingClassId: number;
  courseName: string;
  teacherName: string;
  classCode: string;
  credits?: number;
}

export interface SelectionResult {
  success: boolean;
  message: string;
}

export const studentApi = {
  /**
   * 获取指定学生的课程安排（包含教师信息）
   */
  async getStudentSchedules(studentId: number): Promise<CourseSchedule[]> {
    try {
      // 1. 获取该学生的选课记录
      const selections = await this.getSelectionsByStudentWithTeachers(studentId);
      const teachingClassIds = selections.map(s => s.teachingClassId);

      if (teachingClassIds.length === 0) return [];

      // 建立教学班ID到教师姓名的映射
      const teacherMap: Record<number, string> = {};
      selections.forEach(sel => {
        teacherMap[sel.teachingClassId] = sel.teacherName;
      });

      // 2. 获取这些教学班的课程安排
      const schedulesResults = await Promise.all(
        teachingClassIds.map(id => 
          apiClient.get<CourseSchedule[]>(`/schedules/teaching-class/${id}`)
        )
      );

      // 3. 处理课程安排数据，补充教师姓名
      return schedulesResults.flatMap(schedules => 
        Array.isArray(schedules) ? schedules.map(schedule => ({
          ...schedule,
          teacherName: teacherMap[schedule.teachingClassId] || '未知教师',
          isStudentCourse: true
        })) : []
      );
    } catch (error) {
      console.error('获取学生课程安排失败:', error);
      throw error;
    }
  },

  /**
   * 获取学生的选课记录（包含教师信息）
   */
  async getSelectionsByStudentWithTeachers(studentId: number): Promise<MyCourse[]> {
    return apiClient.get<MyCourse[]>(`/selections/my-courses/student/${studentId}`);
  },

  /**
   * 获取教学班的课程安排（包含教师信息）
   */
  async getSchedulesByTeachingClass(teachingClassId: number): Promise<CourseSchedule[]> {
    try {
      // 并行获取课程安排和教师信息
      const [schedules, teacherName] = await Promise.all([
        apiClient.get<CourseSchedule[]>(`/schedules/teaching-class/${teachingClassId}`),
        this.getTeacherName(teachingClassId)
      ]);

      // 合并数据
      return Array.isArray(schedules) 
        ? schedules.map(schedule => ({
            ...schedule,
            teacherName: teacherName || '未知教师'
          }))
        : [];
    } catch (error) {
      console.error('获取课程安排失败:', error);
      throw error;
    }
  },

  /**
   * 选课
   */
  async selectCourse(studentId: number, teachingClassId: number): Promise<SelectionResult> {
    try {
      const result = await apiClient.post<any>(`/selections?studentId=${studentId}&teachingClassId=${teachingClassId}`);
      return {
        success: true,
        message: '选课成功'
      };
    } catch (error: any) {
      return {
        success: false,
        message: error.message || '选课失败'
      };
    }
  },

  /**
   * 退选
   */
  async cancelSelection(studentId: number, teachingClassId: number): Promise<SelectionResult> {
    try {
      const result = await apiClient.delete<any>(`/selections?studentId=${studentId}&teachingClassId=${teachingClassId}`);
      return {
        success: true,
        message: '退课成功'
      };
    } catch (error: any) {
      return {
        success: false,
        message: error.message || '退课失败'
      };
    }
  },

  /**
   * 根据教学班ID获取教师姓名
   */
  async getTeacherName(teachingClassId: number): Promise<string> {
    try {
      return await apiClient.get<string>(`/selections/teacher-name/${teachingClassId}`);
    } catch (error) {
      console.error('获取教师姓名失败:', error);
      return '未知教师';
    }
  }
};