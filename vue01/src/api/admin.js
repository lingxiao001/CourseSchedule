import axios from 'axios'

const API_BASE = 'http://localhost:8080/api'



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

