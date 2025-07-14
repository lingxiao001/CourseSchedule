package com.example.coursescheduleapp.repository

import com.example.coursescheduleapp.model.*
import com.example.coursescheduleapp.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CourseRepository @Inject constructor(
    private val apiService: ApiService
) {
    
    suspend fun getAllCourses(): Result<List<Course>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAllCourses()
                if (response.isSuccessful) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("获取课程列表失败: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun getCourseById(id: Long): Result<Course> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getCourseById(id)
                if (response.isSuccessful) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("获取课程详情失败: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun getAllTeachingClasses(): Result<List<TeachingClass>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAllTeachingClasses()
                if (response.isSuccessful) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("获取教学班列表失败: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun getTeachingClassesByCourse(courseId: Long): Result<List<TeachingClass>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getTeachingClassesByCourse(courseId)
                if (response.isSuccessful) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("获取课程教学班失败: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
} 