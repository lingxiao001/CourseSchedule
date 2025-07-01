import axios from 'axios'

const API_BASE = 'http://localhost:8080/api'

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

export const getUsers = (data) => {
  return axios.get(`${API_BASE}/admin/users`, data)
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

export const getAllUsers = (data) => {
  return axios.get(`${API_BASE}/admin/users`, data)
}

