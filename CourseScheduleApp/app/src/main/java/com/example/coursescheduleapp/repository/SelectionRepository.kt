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
    
    /**
     * 获取学生可选课程列表
     * @param studentId 学生ID
     * @return 可选课程列表结果
     */
    suspend fun getAvailableCourses(studentId: Long): Result<List<AvailableCourseDTO>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAvailableCourses(studentId)
                if (response.isSuccessful) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("获取可选课程失败: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    /**
     * 获取按课程分组的可选课程列表
     * @param studentId 学生ID
     * @return 按课程分组的可选课程列表结果
     */
    suspend fun getAvailableCoursesGroupedByCourse(studentId: Long): Result<List<CourseWithTeachingClassesDTO>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAvailableCoursesGroupedByCourse(studentId)
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

    /**
     * 获取教师的教学班（课程列表）
     * @param teacherId 教师ID
     * @return 教师教学班列表结果
     */
    suspend fun getTeacherTeachingClasses(teacherId: Long): Result<List<MyCourseDTO>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getTeacherTeachingClasses(teacherId)
                if (response.isSuccessful) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("获取教师教学班失败: \\${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
} 