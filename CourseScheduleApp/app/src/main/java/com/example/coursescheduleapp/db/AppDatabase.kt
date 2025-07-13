package com.example.coursescheduleapp.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.coursescheduleapp.db.SectionSettingDao
import com.example.coursescheduleapp.model.SectionSettingEntity

@Database(
    entities = [SectionSettingEntity::class, UserAvatarEntity::class],
    version = 2
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sectionSettingDao(): SectionSettingDao
    abstract fun userAvatarDao(): UserAvatarDao
} 