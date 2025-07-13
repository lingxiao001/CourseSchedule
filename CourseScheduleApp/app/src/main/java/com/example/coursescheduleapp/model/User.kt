package com.example.coursescheduleapp.model

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName(value = "userId", alternate = ["user_id", "id"])
    val userId: Long,
    @SerializedName("username")
    val username: String,
    @SerializedName(value = "realName", alternate = ["real_name"])
    val realName: String,
    @SerializedName("role")
    val role: String,
    @SerializedName("studentId")
    val studentId: String? = null,
    @SerializedName("teacherId")
    val teacherId: String? = null,
    @SerializedName("grade")
    val grade: String? = null,
    @SerializedName("className")
    val className: String? = null,
    @SerializedName("title")
    val title: String? = null,
    @SerializedName("department")
    val department: String? = null,
    @SerializedName("createdAt")
    val createdAt: String? = null
)

data class LoginRequest(
    val username: String,
    val password: String
)

data class AuthResponse(
    @SerializedName("user") val user: User?,
    @SerializedName("message") val message: String? = null
)