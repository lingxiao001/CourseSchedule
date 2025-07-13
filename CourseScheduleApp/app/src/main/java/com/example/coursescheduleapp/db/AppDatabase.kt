package com.example.coursescheduleapp.db

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [UserAvatarEntity::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userAvatarDao(): UserAvatarDao
} 