import axios from 'axios'

const API_URL = 'http://localhost:8080/api/selections'
const API_URL2 = 'http://localhost:8080/api'

// 获取指定学生的课程安排（包含教师信息）
export const getStudentSchedules = async (studentId) => {
  try {
    // 1. 获取该学生的选课记录（包含教师名字）
    const selections = await getSelectionsByStudentWithTeachers(studentId);
    const teachingClassIds = selections.map(s => s.teachingClassId);
    
    if (teachingClassIds.length === 0) return [];

    // 2. 获取这些教学班的课程安排
    const schedulesResults = await Promise.all(
      teachingClassIds.map(id => getSchedulesByTeachingClass(id))
    );
    
    // 3. 合并并标记为学生课程
    return schedulesResults.flat().map(schedule => ({
      ...schedule,
      isStudentCourse: true
    }));
  } catch (error) {
    console.error('获取学生课程安排失败:', error);
    throw error;
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