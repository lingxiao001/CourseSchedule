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

    /**
     * 创建新课程
     */
    suspend fun createCourse(courseRequest: CourseCreateRequest): Result<Course> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.createCourse(
                    Course(
                        id = 0L,
                        courseName = courseRequest.courseName,
                        classCode = courseRequest.classCode,
                        description = courseRequest.description,
                        credit = courseRequest.credit,
                        hours = courseRequest.hours,
                        createdAt = null
                    )
                )
                if (response.isSuccessful) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("创建课程失败: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 更新课程信息
     */
    suspend fun updateCourse(id: Long, courseRequest: CourseUpdateRequest): Result<Course> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.updateCourse(
                    id,
                    Course(
                        id = id,
                        courseName = courseRequest.courseName,
                        classCode = courseRequest.classCode,
                        description = courseRequest.description,
                        credit = courseRequest.credit,
                        hours = courseRequest.hours,
                        createdAt = null
                    )
                )
                if (response.isSuccessful) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("更新课程失败: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 删除课程
     */
    suspend fun deleteCourse(id: Long): Result<Unit> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.deleteCourse(id)
                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(Exception("删除课程失败: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
} 