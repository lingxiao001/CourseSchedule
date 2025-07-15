package com.example.coursescheduleapp.model

import com.google.gson.annotations.SerializedName

/**
 * 课程及其教学班分组数据传输对象
 * 用于选课中心按课程显示
 */
data class CourseWithTeachingClassesDTO(
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
    
    @SerializedName("teachingClasses")
    val teachingClasses: List<TeachingClassDetailDTO>,
    
    @SerializedName("totalTeachingClasses")
    val totalTeachingClasses: Int,
    
    @SerializedName("selectedTeachingClasses")
    val selectedTeachingClasses: Int
) {
    /**
     * 是否已全部选满
     */
    val isFullySelected: Boolean
        get() = selectedTeachingClasses > 0 && selectedTeachingClasses == totalTeachingClasses
    
    /**
     * 是否部分选择
     */
    val isPartiallySelected: Boolean
        get() = selectedTeachingClasses > 0 && selectedTeachingClasses < totalTeachingClasses
    
    /**
     * 是否完全未选择
     */
    val isNotSelected: Boolean
        get() = selectedTeachingClasses == 0
}