import axios from 'axios'

const API_BASE = process.env.VUE_APP_API_BASE_URL || '/api'

// 创建一个配置了 baseURL 的 axios 实例
const apiClient = axios.create({
  baseURL: API_BASE,
})

export const createUser = (data) => {
  return axios.post(`${API_BASE}/admin/users`, data)
}

export const updateUser = (id, data) => {
  return axios.put(`${API_BASE}/admin/users/${id}`, data)
}

export const deleteUser = (id) => {
  return axios.delete(`${API_BASE}/admin/users/${id}`)
}

// 课程安排API
export const getSchedules = () => {
  return axios.get(`${API_BASE}/schedules`)
}

export const getUsers = (params = {}) => {
  return axios.get(`${API_BASE}/admin/users`, { params })
}

export const getStats = () => {
  return axios.get(`${API_BASE}/admin/stats`)
}

export const addSchedule = (teachingClassId, data) => {
  return axios.post(`${API_BASE}/schedules/teaching-class/${teachingClassId}`, data)
}

export const updateSchedule = (id, data) => {
  return axios.put(`${API_BASE}/schedules/${id}`, data)
}

export const deleteSchedule = (id) => {
  return axios.delete(`${API_BASE}/schedules/${id}`)
}

/**
 * Courses API
 */
export const getAllCourses = () => {
  return apiClient.get('/courses')
}

export const createCourse = (courseData) => {
  return apiClient.post('/courses', courseData)
}

export const updateCourse = (id, courseData) => {
  return apiClient.put(`/courses/${id}`, courseData)
}

export const deleteCourse = (id) => {
  return apiClient.delete(`/courses/${id}`)
}

export const getAllUsers = (params = {}) => {
  return axios.get(`${API_BASE}/admin/users`, { params })
}

// Classroom APIs
export const getClassrooms = () => axios.get(`${API_BASE}/classrooms`)
export const createClassroom = (data) => axios.post(`${API_BASE}/classrooms`, data)
export const updateClassroom = (id,data) => axios.put(`${API_BASE}/classrooms/${id}`, data)
export const deleteClassroom = (id) => axios.delete(`${API_BASE}/classrooms/${id}`)

