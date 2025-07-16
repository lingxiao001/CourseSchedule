package com.example.coursescheduleapp.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "section_setting")
data class SectionSettingEntity(
    @PrimaryKey val id: Int = 1,
    val sectionTimes: String // 用JSON字符串保存List<String>
) 