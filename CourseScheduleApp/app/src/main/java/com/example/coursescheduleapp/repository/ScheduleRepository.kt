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

    suspend fun getSchedulesByStudent(studentId: Long): Result<List<ClassSchedule>> {
        return withContext(Dispatchers.IO) {
            try {
                // 1. 获取学生所有选课
                val selectionResp = apiService.getSelectionsByStudent(studentId)
                if (!selectionResp.isSuccessful) {
                    return@withContext Result.failure(Exception("获取学生选课失败: ${selectionResp.code()}"))
                }
                val teachingClassIds = selectionResp.body()?.map { it.teachingClassId } ?: emptyList()
                // 2. 批量获取每个教学班的课表
                val allSchedules = mutableListOf<ClassSchedule>()
                for (classId in teachingClassIds) {
                    val scheduleResp = apiService.getSchedulesByTeachingClass(classId)
                    if (scheduleResp.isSuccessful) {
                        allSchedules.addAll(scheduleResp.body() ?: emptyList())
                    }
                }
                Result.success(allSchedules)
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun deleteAllSchedulesForClass(teachingClassId: Long): Boolean {
        // 假设后端支持批量删除接口，否则循环删除
        return try {
            val schedules = getSchedulesByTeachingClass(teachingClassId).getOrNull() ?: emptyList()
            schedules.all { apiService.deleteSchedule(it.id).isSuccessful }
        } catch (e: Exception) { false }
    }
    suspend fun batchAddSchedules(schedules: List<ClassSchedule>): Boolean {
        // 假设后端支持批量新增接口，否则循环新增
        return try {
            schedules.all { s ->
                apiService.addSchedule(s.teachingClassId, mapOf(
                    "dayOfWeek" to s.dayOfWeek,
                    "startTime" to s.startTime,
                    "endTime" to s.endTime,
                    "classroomId" to s.classroomId
                )).isSuccessful
            }
        } catch (e: Exception) { false }
    }

    suspend fun autoScheduleForTeachingClass(teachingClassId: Long): Result<List<ClassSchedule>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.autoScheduleForTeachingClass(teachingClassId)
                if (response.isSuccessful) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("智能排课失败: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun autoScheduleForAllTeachingClasses(): Result<List<ClassSchedule>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.autoScheduleForAllTeachingClasses()
                if (response.isSuccessful) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(Exception("全局智能排课失败: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    suspend fun deleteAllSchedules(): Boolean {
        return try {
            apiService.deleteAllSchedules().isSuccessful
        } catch (e: Exception) { false }
    }

    suspend fun deleteSchedule(scheduleId: Long): Boolean {
        return try {
            apiService.deleteSchedule(scheduleId).isSuccessful
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 更新单条课表
     * @param scheduleId 课表ID
     * @param updatedSchedule 更新后的课表对象
     * @return 是否成功
     */
    suspend fun updateSchedule(scheduleId: Long, updatedSchedule: ClassSchedule): Boolean {
        return try {
            val data = mapOf(
                "teachingClassId" to updatedSchedule.teachingClassId,
                "dayOfWeek" to updatedSchedule.dayOfWeek,
                "startTime" to updatedSchedule.startTime,
                "endTime" to updatedSchedule.endTime,
                "classroomId" to updatedSchedule.classroomId
            )
            apiService.updateSchedule(scheduleId, data).isSuccessful
        } catch (e: Exception) {
            false
        }
    }
} 