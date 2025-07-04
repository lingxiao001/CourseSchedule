import axios from 'axios';

// 定义 API 的基础路径
const BASE_URL = 'http://localhost:8080/api/schedules';

const apiClient = axios.create({
  baseURL: process.env.VUE_APP_API_BASE_URL || 'http://localhost:8080/api',
  timeout: 5000,
  headers: {
    'Content-Type': 'application/json'
  }
});

// 封装课程安排相关的 API 方法
export const scheduleApi = {
  /**
   * 添加课程安排
   * @param {number} teachingClassId - 教学班 ID
   * @param {object} scheduleData - 课程安排数据
   */
  addSchedule(teachingClassId, scheduleData) {
    return axios.post(`${BASE_URL}/teaching-class/${teachingClassId}`, scheduleData);
  },

  /**
   * 更新课程安排
   * @param {number} scheduleId - 课程安排 ID
   * @param {object} scheduleData - 更新后的课程安排数据
   */
  updateSchedule(scheduleId, scheduleData) {
    return axios.put(`${BASE_URL}/${scheduleId}`, scheduleData);
  },

  /**
   * 按教学班 ID 获取课程安排（包含教师信息）
   * @param {number} teachingClassId - 教学班 ID
   * @returns {Promise<Array>} 返回课程安排列表
   */
  async getSchedulesByTeachingClass(teachingClassId) {
    try {
      // 并行请求课程安排和教师信息
      const [scheduleResponse, teacherResponse] = await Promise.all([
        axios.get(`${BASE_URL}/teaching-class/${teachingClassId}`),
        this.getTeacherName(teachingClassId) // 调用内部方法获取教师信息
      ]);

      // 合并数据
      if (Array.isArray(scheduleResponse.data)) {
        return scheduleResponse.data.map(schedule => ({
          ...schedule,
          teacherName: teacherResponse || '未知教师' // 如果教师信息为空，默认显示 "未知教师"
        }));
      } else {
        return []; // 如果返回的数据不是数组，则返回空数组
      }
    } catch (error) {
      console.error('获取课程安排失败:', error);
      throw error; // 抛出错误以便调用方处理
    }
  },

  /**
   * 按教师 ID 获取课程安排
   * @param {number} teacherId - 教师 ID
   * @returns {Promise<Array>} 返回课程安排列表
   */
  getSchedulesByTeacher(teacherId) {
    return axios.get(`${BASE_URL}/teacher/${teacherId}`);
  },

  /**
   * 删除课程安排
   * @param {number} scheduleId - 课程安排 ID
   */
  deleteSchedule(scheduleId) {
    return axios.delete(`${BASE_URL}/${scheduleId}`);
  },

  /**
   * 获取所有课程安排（管理员专用）
   * @returns {Promise<Array>} 返回所有课程安排列表
   */
  getAllSchedules() {
    return axios.get(BASE_URL);
  },

  /**
   * 内部方法：根据教学班 ID 获取教师名称
   * @param {number} teachingClassId - 教学班 ID
   * @returns {Promise<string>} 返回教师名称
   */
  async getTeacherName(teachingClassId) {
    try {
      // 示例：假设教师信息存储在一个独立的 API 端点中
      const response = await axios.get(`${BASE_URL}/teachers/teaching-class/${teachingClassId}`);
      return response.data.name || '未知教师'; // 如果教师姓名为空，默认显示 "未知教师"
    } catch (error) {
      console.error('获取教师信息失败:', error);
      return '未知教师'; // 如果发生错误，返回默认值
    }
  },

  // 冲突检测
  detectConflicts(params) {
    return apiClient.get('/intelligent-scheduling/conflicts', { params })
  }
};

/**
 * 根据用户ID和角色获取课表
 * @param {number} userId - 用户的ID
 * @param {string} role - 用户的角色 ('student', 'teacher', 'admin')
 * @returns {Promise<Array>} - 课表数据数组
 */
export const getSchedule = async (userId, role) => {
  try {
    // 后端需要一个统一的接口来处理不同角色的课表请求
    // 我们假设这个接口是 /schedules
    const response = await apiClient.get('/schedules', {
      params: {
        userId,
        role
      }
    });
    return response.data;
  } catch (error) {
    console.error(`获取课表失败 (用户ID: ${userId}, 角色: ${role}):`, error);
    // 在真实应用中，可以抛出更具体的错误
    throw error;
  }
};
