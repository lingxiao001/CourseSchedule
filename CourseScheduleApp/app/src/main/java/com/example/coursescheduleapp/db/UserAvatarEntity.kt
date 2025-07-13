package com.example.coursescheduleapp.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_avatar")
data class UserAvatarEntity(
    @PrimaryKey val userId: Long,
    val avatarPath: String // 本地文件路径
) 