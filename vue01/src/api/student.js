import axios from 'axios'

const API_BASE = process.env.VUE_APP_API_BASE_URL || '/api'
const API_URL = `${API_BASE}/selections`
const API_URL2 = API_BASE

// 获取指定学生的课程安排（包含教师信息）
export const getStudentSchedules = async (studentId) => {
  try {
    // 1. 获取该学生的选课记录
    const selections = await getSelectionsByStudentWithTeachers(studentId)
    const teachingClassIds = selections.map(s => s.teachingClassId)

    if (teachingClassIds.length === 0) return []

    // 建立教学班ID到教师姓名的映射（用于补充教师信息）
    const teacherMap = {}
    selections.forEach(sel => {
      teacherMap[sel.teachingClassId] = sel.teacherName
    })

    // 2. 获取这些教学班的课程安排（现在ScheduleDTO已包含courseName和classCode）
    const schedulesResults = await Promise.all(
      teachingClassIds.map(id => axios.get(`${API_URL2}/schedules/teaching-class/${id}`))
    )

    // 3. 处理课程安排数据，补充教师姓名
    return schedulesResults.flatMap(response => 
      Array.isArray(response.data) ? response.data.map(schedule => ({
        ...schedule,
        teacherName: teacherMap[schedule.teachingClassId] || '未知教师',
        isStudentCourse: true
      })) : []
    )
  } catch (error) {
    console.error('获取学生课程安排失败:', error)
    throw error
  }
};

export const getSelectionsByStudentWithTeachers = async (studentId) => {
  // 这个函数现在调用新的、专用的DTO接口
  const response = await axios.get(`${API_URL}/my-courses/student/${studentId}`);
  return response.data;
}


// 获取教学班的课程安排（包含教师信息）
export const getSchedulesByTeachingClass = async (teachingClassId) => {
  try {
    // 并行获取课程安排和教师信息
    const [scheduleResponse, teacherResponse] = await Promise.all([
      axios.get(`${API_URL2}/schedules/teaching-class/${teachingClassId}`),
      getTeacherName(teachingClassId)
    ]);

    // 合并数据
    return Array.isArray(scheduleResponse.data) 
      ? scheduleResponse.data.map(schedule => ({
          ...schedule,
          teacherName: teacherResponse || '未知教师'
        }))
      : [];
  } catch (error) {
    console.error('获取课程安排失败:', error);
    throw error;
  }
};

// 选课
export const selectCourse = async (studentId, teachingClassId) => {
  const response = await axios.post(`${API_URL}?studentId=${studentId}&teachingClassId=${teachingClassId}`)
  return response.data
}

// 退选
export const cancelSelection = async (studentId, teachingClassId) => {
  const response = await axios.delete(`${API_URL}?studentId=${studentId}&teachingClassId=${teachingClassId}`)
  return response.data
}

// 新增：根据教学班ID获取教师姓名
export const getTeacherName = async (teachingClassId) => {
  const response = await axios.get(`${API_URL}/teacher-name/${teachingClassId}`)
  return response.data
}