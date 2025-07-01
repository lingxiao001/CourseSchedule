import { defineStore } from 'pinia';
import { scheduleApi } from '@/api/schedule';

export const useScheduleStore = defineStore('schedule', {
  state: () => ({
    schedules: [],
    recentSchedules: [],
    isLoading: false,
    error: null
  }),
  
  actions: {
    async addSchedule(teachingClassId, scheduleData) {
      try {
        this.isLoading = true;
        const response = await scheduleApi.addSchedule(teachingClassId, scheduleData);
        this.schedules.push(response.data);
        return response.data;
      } catch (error) {
        this.error = error.response?.data?.message || error.message;
        throw error;
      } finally {
        this.isLoading = false;
      }
    },
    
    async updateSchedule(scheduleId, scheduleData) {
      try {
        this.isLoading = true;
        const response = await scheduleApi.updateSchedule(scheduleId, scheduleData);
        const index = this.schedules.findIndex(s => s.id === scheduleId);
        if (index !== -1) {
          this.schedules[index] = response.data;
        }
        return response.data;
      } catch (error) {
        this.error = error.response?.data?.message || error.message;
        throw error;
      } finally {
        this.isLoading = false;
      }
    },
    
    async fetchSchedulesByTeachingClass(teachingClassId) {
      try {
        this.isLoading = true;
        const response = await scheduleApi.getSchedulesByTeachingClass(teachingClassId);
        this.schedules = response.data;
        return response.data;
      } catch (error) {
        this.error = error.response?.data?.message || error.message;
        throw error;
      } finally {
        this.isLoading = false;
      }
    },
    
    async fetchSchedulesByTeacher(teacherId) {
      try {
        this.isLoading = true;
        const response = await scheduleApi.getSchedulesByTeacher(teacherId);
        this.schedules = response.data;
        return response.data;
      } catch (error) {
        this.error = error.response?.data?.message || error.message;
        throw error;
      } finally {
        this.isLoading = false;
      }
    },
    
    async deleteSchedule(scheduleId) {
      try {
        this.isLoading = true;
        await scheduleApi.deleteSchedule(scheduleId);
        this.schedules = this.schedules.filter(s => s.id !== scheduleId);
      } catch (error) {
        this.error = error.response?.data?.message || error.message;
        throw error;
      } finally {
        this.isLoading = false;
      }
    },
    
    async fetchAllSchedules() {
      try {
        this.isLoading = true;
        const response = await scheduleApi.getAllSchedules();
        this.schedules = response.data;
        return response.data;
      } catch (error) {
        this.error = error.response?.data?.message || error.message;
        throw error;
      } finally {
        this.isLoading = false;
      }
    },

    async fetchRecentSchedules() {
      try {
        this.isLoading = true;
        const response = await scheduleApi.getAllSchedules();
    
        // 假设我们获取最近一周的课程安排
        const oneWeekAgo = new Date();
        oneWeekAgo.setDate(oneWeekAgo.getDate() - 7);
    
        this.recentSchedules = response.data.filter(schedule => {
          return schedule.date >= oneWeekAgo;
        });
    
      } catch (error) {
        this.error = error.response?.data?.message || error.message;
        throw error;
      } finally {
        this.isLoading = false;
      }
    }    
  }
});