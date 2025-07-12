package com.example.coursescheduleapp.repository

import com.example.coursescheduleapp.model.ClassSchedule
import com.example.coursescheduleapp.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ScheduleRepository @Inject constructor(
    private val apiService: ApiService
) {
    
    suspend fun getAllSchedules(): Result<List<ClassSchedule>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAllSchedules()
                if (response.isSuccessful) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("获取课表列表失败: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun getSchedulesByTeachingClass(teachingClassId: Long): Result<List<ClassSchedule>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getSchedulesByTeachingClass(teachingClassId)
                if (response.isSuccessful) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("获取教学班课表失败: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun getSchedulesByTeacher(teacherId: Long): Result<List<ClassSchedule>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getSchedulesByTeacher(teacherId)
                if (response.isSuccessful) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("获取教师课表失败: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
} 