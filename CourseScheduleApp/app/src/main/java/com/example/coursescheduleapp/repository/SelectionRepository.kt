package com.example.coursescheduleapp.repository

import com.example.coursescheduleapp.model.*
import com.example.coursescheduleapp.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SelectionRepository @Inject constructor(
    private val apiService: ApiService
) {
    
    suspend fun selectCourse(studentId: Long, teachingClassId: Long): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.selectCourse(studentId, teachingClassId)
                if (response.isSuccessful) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("选课失败: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun cancelSelection(studentId: Long, teachingClassId: Long): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.cancelSelection(studentId, teachingClassId)
                if (response.isSuccessful) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("退选失败: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun getSelectionsByStudent(studentId: Long): Result<List<SelectionDTO>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getSelectionsByStudent(studentId)
                if (response.isSuccessful) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("获取学生选课记录失败: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun getMyCourses(studentId: Long): Result<List<MyCourseDTO>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getMyCourses(studentId)
                if (response.isSuccessful) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("获取我的课程失败: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
} 