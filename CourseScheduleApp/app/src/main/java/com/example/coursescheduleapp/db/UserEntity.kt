package com.example.coursescheduleapp.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey val userId: Long,
    val username: String,
    val realName: String,
    val role: String,
    val studentId: Long,
    val teacherId: Long,
    val grade: String? = null,
    val className: String? = null,
    val title: String? = null,
    val department: String? = null,
    val createdAt: String? = null
) 