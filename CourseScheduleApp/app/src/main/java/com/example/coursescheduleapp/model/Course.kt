package com.example.coursescheduleapp.model

import com.google.gson.annotations.SerializedName

data class Course(
    val id: Long,
    @SerializedName("courseName")
    val courseName: String,
    @SerializedName("classCode")
    val classCode: String,
    val description: String?,
    val credit: Double,
    val hours: Int,
    @SerializedName("created_at")
    val createdAt: String?
)

data class TeachingClass(
    @SerializedName("id")
    val id: Long = 0L,
    @SerializedName("classCode")
    val classCode: String = "",
    @SerializedName("courseId")
    val courseId: Long = 0L,
    @SerializedName("teacherId")
    val teacherId: Long = 0L,
    @SerializedName("currentStudents")
    val currentStudents: Int = 0,
    @SerializedName("maxStudents")
    val maxStudents: Int = 30,
    @SerializedName("teacherName")
    val teacherName: String? = null,
    val teacher: User? = null
)

data class Teacher(
    val id: Long,
    val title: String,
    val department: String,
    val user: User? = null
)

data class Student(
    val id: Long,
    val grade: String,
    val className: String,
    val user: User? = null
)

data class Classroom(
    val id: Long = 0L,
    val building: String = "",
    val classroomName: String = "",
    val capacity: Int = 0,
    val createdAt: String? = null
)

data class ClassSchedule(
    val id: Long,
    val dayOfWeek: Int,
    val startTime: String,
    val endTime: String,
    val classroomName: String,
    val building: String,
    val teachingClassId: Long,
    val classroomId: Long,
    val courseName: String,
    val classCode: String
)

data class CourseSelection(
    val id: Long,
    val student: Student,
    val teachingClass: TeachingClass,
    val selectionTime: String? = null
)

data class SelectionDTO(
    val id: Long,
    val studentId: Long,
    val studentName: String,
    val teachingClassId: Long,
    val courseName: String,
    val teacherName: String,
    val selectionTime: String? = null
)

data class MyCourseDTO(
    val courseId: Long,
    val courseName: String,
    val credits: Double,
    val teacherName: String,
    val classCode: String,
    val selectionTime: String? = null
)