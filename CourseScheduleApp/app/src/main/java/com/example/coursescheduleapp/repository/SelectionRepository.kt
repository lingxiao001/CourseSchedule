package com.example.coursescheduleapp.repository

import android.util.Log
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
    
<<<<<<< HEAD
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
=======
    suspend fun getTeacherTeachingClasses(teacherId: Long): Result<List<MyCourseDTO>> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d("SelectionRepository", "Loading teacher teaching classes for teacherId: $teacherId")
                
                // 临时实现：使用mock数据
                val mockCourses = listOf(
                    MyCourseDTO(1L, "高等数学", 4.0, "张教授", "MATH101-01", "2024-01-01"),
                    MyCourseDTO(2L, "线性代数", 3.0, "李教授", "MATH201-01", "2024-01-02"),
                    MyCourseDTO(3L, "概率论与数理统计", 3.0, "王教授", "MATH301-01", "2024-01-03")
                )
                Log.d("SelectionRepository", "Returning mock courses: ${mockCourses.size} items")
                Result.success(mockCourses)
            } catch (e: Exception) {
                Log.e("SelectionRepository", "Error loading teacher teaching classes", e)
                // 使用模拟数据作为后备
                val mockCourses = listOf(
                    MyCourseDTO(1L, "高等数学", 4.0, "张教授", "MATH101-01", "2024-01-01"),
                    MyCourseDTO(2L, "线性代数", 3.0, "李教授", "MATH201-01", "2024-01-02")
                )
                Result.success(mockCourses)
>>>>>>> origin/branch1
            }
        }
    }
} 