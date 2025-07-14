package com.example.coursescheduleapp.repository

import com.google.gson.annotations.SerializedName

data class CourseCreateRequest(
    @SerializedName("courseName")
    val courseName: String,
    @SerializedName("classCode")
    val classCode: String,
    val description: String,
    val credit: Double,
    val hours: Int
) 