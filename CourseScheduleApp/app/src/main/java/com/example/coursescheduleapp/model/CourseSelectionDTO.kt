package com.example.coursescheduleapp.model

import com.google.gson.annotations.SerializedName

/**
 * 可选课程数据传输对象
 * 用于学生选课功能，包含课程详细信息、教学班信息、已选人数等
 */
data class AvailableCourseDTO(
    @SerializedName("teachingClassId")
    val teachingClassId: Long,

    @SerializedName("courseId")
    val courseId: Long,

    @SerializedName("courseName")
    val courseName: String,

    @SerializedName("courseCode")
    val courseCode: String,

    @SerializedName("credit")
    val credit: Double,

    @SerializedName("description")
    val description: String?,

    @SerializedName("teacherName")
    val teacherName: String,

    @SerializedName("teacherId")
    val teacherId: Long,

    @SerializedName("classCode")
    val classCode: String,

    @SerializedName("currentStudents")
    val currentStudents: Int,

    @SerializedName("maxStudents")
    val maxStudents: Int,

    @SerializedName("schedules")
    val schedules: List<ClassScheduleDTO>,

    @SerializedName("isSelected")
    val isSelected: Boolean = false
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

/**
 * 选课请求数据传输对象
 */
data class SelectCourseRequest(
    @SerializedName("studentId")
    val studentId: Long,
    
    @SerializedName("teachingClassId")
    val teachingClassId: Long
)

/**
 * 退课请求数据传输对象
 */
data class CancelCourseRequest(
    @SerializedName("studentId")
    val studentId: Long,
    
    @SerializedName("teachingClassId")
    val teachingClassId: Long
)

/**
 * 选课结果响应
 */
data class SelectionResult(
    @SerializedName("success")
    val success: Boolean,
    
    @SerializedName("message")
    val message: String,
    
    @SerializedName("selectionId")
    val selectionId: Long? = null
)