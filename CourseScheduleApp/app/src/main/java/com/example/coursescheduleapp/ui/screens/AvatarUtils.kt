package com.example.coursescheduleapp.ui.screens

import android.content.Context
import android.net.Uri
import com.example.coursescheduleapp.db.UserAvatarDao
import com.example.coursescheduleapp.db.UserAvatarEntity
import java.io.File

suspend fun saveUserAvatar(context: Context, userId: Long, uri: Uri, userAvatarDao: UserAvatarDao) {
    val inputStream = context.contentResolver.openInputStream(uri)
    val file = File(context.filesDir, "user_avatar_${userId}.jpg")
    inputStream?.use { input ->
        file.outputStream().use { output -> input.copyTo(output) }
    }
    userAvatarDao.insertOrUpdate(UserAvatarEntity(userId, file.absolutePath))
}

suspend fun loadUserAvatar(userId: Long, userAvatarDao: UserAvatarDao): String? {
    val entity = userAvatarDao.getAvatar(userId)
    return entity?.avatarPath
} 