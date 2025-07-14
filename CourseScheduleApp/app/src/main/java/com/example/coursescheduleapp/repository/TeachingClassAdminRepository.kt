package com.example.coursescheduleapp.repository

import com.example.coursescheduleapp.model.TeachingClass
import com.example.coursescheduleapp.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TeachingClassAdminRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getTeachingClasses(): Result<List<TeachingClass>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAllTeachingClasses()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("获取教学班列表失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun getTeachingClassesByCourse(courseId: Long): Result<List<TeachingClass>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getTeachingClassesByCourse(courseId)
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("获取课程下教学班失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun createTeachingClass(courseId: Long, teachingClass: TeachingClass): Result<TeachingClass> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createTeachingClass(courseId, teachingClass)
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) } ?: Result.failure(Exception("创建教学班失败"))
            } else {
                Result.failure(Exception("创建教学班失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun updateTeachingClass(classId: Long, teachingClass: TeachingClass): Result<TeachingClass> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updateTeachingClass(classId, teachingClass)
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) } ?: Result.failure(Exception("更新教学班失败"))
            } else {
                Result.failure(Exception("更新教学班失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    suspend fun deleteTeachingClass(classId: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteTeachingClass(classId)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("删除教学班失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 