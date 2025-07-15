package com.example.coursescheduleapp.db

import androidx.room.*
import com.example.coursescheduleapp.model.SectionSettingEntity

@Dao
interface SectionSettingDao {
    @Query("SELECT * FROM section_setting WHERE id = 1 LIMIT 1")
    suspend fun getSectionSetting(): SectionSettingEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(setting: SectionSettingEntity)
} 