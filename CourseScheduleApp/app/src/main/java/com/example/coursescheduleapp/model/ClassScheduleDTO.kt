package com.example.coursescheduleapp.model

import com.google.gson.annotations.SerializedName

/**
 * 课程安排数据传输对象
 * 用于修复上课时间显示问题
 */
data class ClassScheduleDTO(
    @SerializedName("dayOfWeek")
    val dayOfWeek: Int,
    
    @SerializedName("startTime")
    val startTime: String,
    
    @SerializedName("endTime")
    val endTime: String,
    
    @SerializedName("classroomName")
    val classroomName: String,
    
    @SerializedName("building")
    val building: String
) {
    /**
     * 获取星期几的中文显示
     */
    fun getDayOfWeekChinese(): String {
        val days = arrayOf("", "周一", "周二", "周三", "周四", "周五", "周六", "周日")
        return if (dayOfWeek in 1..7) days[dayOfWeek] else "未知"
    }
    
    /**
     * 获取完整的上课时间字符串
     */
    fun getFullSchedule(): String {
        return "${getDayOfWeekChinese()} $startTime-$endTime $building $classroomName"
    }
}