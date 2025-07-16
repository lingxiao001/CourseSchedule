package com.example.coursescheduleapp.model

import com.google.gson.annotations.SerializedName

/**
 * 教学班详细信息数据传输对象
 * 用于课程详情页中的教学班选择
 */
data class TeachingClassDetailDTO(
    @SerializedName("teachingClassId")
    val teachingClassId: Long,

    @SerializedName("courseId")
    val courseId: Long,

    @SerializedName("courseName")
    val courseName: String,

    @SerializedName("classCode")
    val classCode: String,

    @SerializedName("teacherName")
    val teacherName: String,

    @SerializedName("teacherId")
    val teacherId: Long,

    @SerializedName("currentStudents")
    val currentStudents: Int,

    @SerializedName("maxStudents")
    val maxStudents: Int,

    @SerializedName("schedules")
    val schedules: List<ClassScheduleDTO>,

    @SerializedName("isSelected")
    val isSelected: Boolean = false,

    @SerializedName("description")
    val description: String?
) {
    /**
     * 获取剩余可选名额
     */
    val remainingCapacity: Int
        get() = maxStudents - currentStudents
    
    /**
     * 是否可选
     */
    val isAvailable: Boolean
        get() = remainingCapacity > 0 && !isSelected
}