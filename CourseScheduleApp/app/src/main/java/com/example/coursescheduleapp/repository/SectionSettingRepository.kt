package com.example.coursescheduleapp.repository

import com.example.coursescheduleapp.db.SectionSettingDao
import com.example.coursescheduleapp.model.SectionSettingEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import kotlinx.serialization.builtins.serializer

class SectionSettingRepository @Inject constructor(
    private val dao: SectionSettingDao
) {
    suspend fun getSectionSetting(): SectionSettingEntity? = withContext(Dispatchers.IO) {
        dao.getSectionSetting()
    }
    suspend fun saveSectionSetting(sectionTimes: List<String>) = withContext(Dispatchers.IO) {
        val json = kotlinx.serialization.json.Json.encodeToString(ListSerializer(String.serializer()), sectionTimes)
        dao.insertOrUpdate(SectionSettingEntity(sectionTimes = json))
    }
} 