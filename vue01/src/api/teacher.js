import axios from 'axios'

const API_BASE = 'http://localhost:8080/api'

// 课程相关API
export const getCourses = async (params = {}) => {
  try {
    const response = await axios.get(`${API_BASE}/courses`, { params })
    return {
      data: response.data,
      total: response.headers['x-total-count'] || response.data.length
    }
  } catch (error) {
    console.error('获取课程列表失败:', error)
    throw error
  }
}

export const getCourseById = async (id) => {
  try {
    const response = await axios.get(`${API_BASE}/courses/${id}`)
    return response.data
  } catch (error) {
    console.error('获取课程详情失败:', error)
    throw error
  }
}

export const getCourseNameById = async (courseId) => {
  try {
    // 直接调用专门获取课程名称的API端点
    const response = await axios.get(`${API_BASE}/courses/${courseId}/name`)
    return response.data || '未知课程'
  } catch (error) {
    console.error('获取课程名称失败:', error)
    // 更友好的错误处理
    if (error.response?.status === 404) {
      return '课程不存在'
    }
    return '未知课程'
  }
}

export const createCourse = async (courseData) => {
  try {
    const response = await axios.post(`${API_BASE}/courses`, courseData)
    return response.data
  } catch (error) {
    console.error('创建课程失败:', error)
    throw error
  }
}

export const updateCourse = async (id, courseData) => {
  try {
    const response = await axios.put(`${API_BASE}/courses/${id}`, courseData)
    return response.data
  } catch (error) {
    console.error('更新课程失败:', error)
    throw error
  }
}

export const deleteCourse = async (id) => {
  try {
    await axios.delete(`${API_BASE}/courses/${id}`)
  } catch (error) {
    console.error('删除课程失败:', error)
    throw error
  }
}

// 教学班相关API
export const getTeachingClasses = async (params = {}) => {
  try {
    const response = await axios.get(`${API_BASE}/courses/classes`, { params })
    return {
      data: response.data,
      total: response.headers['x-total-count'] || response.data.length
    }
  } catch (error) {
    console.error('获取教学班列表失败:', error)
    throw error
  }
}

export const getTeachingClassesByCourse = async (courseId) => {
  try {
    const response = await axios.get(`${API_BASE}/courses/${courseId}/classes`)
    return response.data
  } catch (error) {
    console.error('获取课程教学班失败:', error)
    throw error
  }
}

export const getCourseIdByTeachingClassId = async (teachingClassId) => {
  try {
    const response = await axios.get(`${API_BASE}/courses/classes/${teachingClassId}`);
    return response.data;
  } catch (error) {
    console.error('获取课程ID失败:', error);
    throw error;
  }
};

export const createTeachingClass = async (courseId, classData) => {
  try {
    const response = await axios.post(
      `${API_BASE}/courses/${courseId}/classes`,
      classData
    )
    return response.data
  } catch (error) {
    console.error('创建教学班失败:', error)
    throw error
  }
}

export const updateTeachingClass = async (classId, classData) => {
  try {
    const response = await axios.put(
      `${API_BASE}/courses/classes/${classId}`,
      classData
    )
    return response.data
  } catch (error) {
    console.error('更新教学班失败:', error)
    throw error
  }
}

export const deleteTeachingClass = async (classId) => {
  try {
    await axios.delete(`${API_BASE}/courses/classes/${classId}`)
  } catch (error) {
    console.error('删除教学班失败:', error)
    throw error
  }
}

// 教师课表相关API
export const getTeacherSchedules = async (teacherId) => {
  try {
    const response = await axios.get(`${API_BASE}/schedules/teacher/${teacherId}`)
    return response.data
  } catch (error) {
    console.error('获取教师课表失败:', error)
    throw error
  }
}

// 其他教师相关API
export const getTeacherDashboardData = async () => {
  try {
    const response = await axios.get(`${API_BASE}/teacher/dashboard`)
    return response.data
  } catch (error) {
    console.error('获取教师仪表盘数据失败:', error)
    throw error
  }
}