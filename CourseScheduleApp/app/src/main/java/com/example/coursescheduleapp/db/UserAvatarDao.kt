package com.example.coursescheduleapp.db

import androidx.room.*

@Dao
interface UserAvatarDao {
    @Query("SELECT * FROM user_avatar WHERE userId = :userId LIMIT 1")
    suspend fun getAvatar(userId: Long): UserAvatarEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(avatar: UserAvatarEntity)
} 