package com.example.coursescheduleapp.repository

import com.example.coursescheduleapp.model.Classroom
import com.example.coursescheduleapp.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Response
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ClassroomAdminRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getClassrooms(): Result<List<Classroom>> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getAllClassrooms()
            if (response.isSuccessful) {
                Result.success(response.body() ?: emptyList())
            } else {
                Result.failure(Exception("获取教室列表失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getClassroomById(id: Long): Result<Classroom> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getClassroomById(id)
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) } ?: Result.failure(Exception("未找到教室"))
            } else {
                Result.failure(Exception("获取教室失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createClassroom(classroom: Classroom): Result<Classroom> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.createClassroom(classroom)
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) } ?: Result.failure(Exception("创建教室失败"))
            } else {
                Result.failure(Exception("创建教室失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateClassroom(id: Long, classroom: Classroom): Result<Classroom> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.updateClassroom(id, classroom)
            if (response.isSuccessful) {
                response.body()?.let { Result.success(it) } ?: Result.failure(Exception("更新教室失败"))
            } else {
                Result.failure(Exception("更新教室失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteClassroom(id: Long): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteClassroom(id)
            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("删除教室失败: ${response.code()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 