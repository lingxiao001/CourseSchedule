package com.example.coursescheduleapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coursescheduleapp.repository.SectionSettingRepository
import com.example.coursescheduleapp.repository.ScheduleRepository
import com.example.coursescheduleapp.repository.TeachingClassAdminRepository
import com.example.coursescheduleapp.repository.CourseRepository
import com.example.coursescheduleapp.repository.ClassroomAdminRepository
import com.example.coursescheduleapp.model.ClassSchedule
import com.example.coursescheduleapp.model.TeachingClass
import com.example.coursescheduleapp.model.Course
import com.example.coursescheduleapp.model.Classroom
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.serialization.builtins.serializer
import android.util.Log

data class ArrangeResult(val success: Boolean, val arranged: List<ClassSchedule>, val conflicts: List<String>)

@HiltViewModel
class QuickScheduleManagerViewModel @Inject constructor(
    private val sectionSettingRepository: SectionSettingRepository,
    private val scheduleRepository: ScheduleRepository,
    private val teachingClassAdminRepository: TeachingClassAdminRepository,
    private val courseRepository: CourseRepository,
    private val classroomAdminRepository: ClassroomAdminRepository
) : ViewModel() {
    private val _sectionTimes = MutableStateFlow<List<String>>(emptyList())
    val sectionTimes: StateFlow<List<String>> = _sectionTimes.asStateFlow()

    private val _teachingClasses = MutableStateFlow<List<TeachingClass>>(emptyList())
    val teachingClasses: StateFlow<List<TeachingClass>> = _teachingClasses.asStateFlow()

    private val _courseNameMap = MutableStateFlow<Map<Long, String>>(emptyMap())
    val courseNameMap: StateFlow<Map<Long, String>> = _courseNameMap.asStateFlow()

    private val _currentClassSchedules = MutableStateFlow<List<ClassSchedule>>(emptyList())
    val currentClassSchedules: StateFlow<List<ClassSchedule>> = _currentClassSchedules.asStateFlow()

    private val _allClassrooms = MutableStateFlow<List<Classroom>>(emptyList())
    val allClassrooms: StateFlow<List<Classroom>> = _allClassrooms.asStateFlow()

    private val _allSchedules = MutableStateFlow<List<ClassSchedule>>(emptyList())
    val allSchedules: StateFlow<List<ClassSchedule>> = _allSchedules.asStateFlow()

    // 每个班级自定义节次设置（Map<teachingClassId, List<String>>）
    private val _classCustomSectionTimes = MutableStateFlow<Map<Long, List<String>>>(emptyMap())
    val classCustomSectionTimes: StateFlow<Map<Long, List<String>>> = _classCustomSectionTimes.asStateFlow()

    init {
        loadSectionSetting()
        loadTeachingClasses()
        loadAllClassrooms()
        loadAllSchedules()
    }

    fun loadSectionSetting() {
        viewModelScope.launch {
            val entity = sectionSettingRepository.getSectionSetting()
            if (entity != null) {
                _sectionTimes.value = Json.decodeFromString(ListSerializer(String.serializer()), entity.sectionTimes)
            }
        }
    }

    fun saveSectionSetting(sectionTimes: List<String>) {
        viewModelScope.launch {
            sectionSettingRepository.saveSectionSetting(sectionTimes)
            _sectionTimes.value = sectionTimes
        }
    }

    fun loadTeachingClasses() {
        viewModelScope.launch {
            val result = teachingClassAdminRepository.getTeachingClasses()
            _teachingClasses.value = result.getOrElse { emptyList() }
            // 加载所有课程并建立id到name映射
            val courseResult = courseRepository.getAllCourses()
            val map = courseResult.getOrElse { emptyList() }.associateBy({ it.id }, { it.courseName })
            _courseNameMap.value = map
        }
    }

    fun loadSchedulesForTeachingClass(teachingClassId: Long) {
        viewModelScope.launch {
            val result = scheduleRepository.getSchedulesByTeachingClass(teachingClassId)
            _currentClassSchedules.value = result.getOrElse { emptyList() }
        }
    }

    // 批量删除某教学班所有排课
    fun deleteAllSchedulesForClass(teachingClassId: Long, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = scheduleRepository.deleteAllSchedulesForClass(teachingClassId)
            onResult(result)
        }
    }
    // 智能排课（单个教学班，遗传算法）
    fun autoArrangeByGeneticForTeachingClass(teachingClassId: Long, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = scheduleRepository.autoScheduleForTeachingClass(teachingClassId)
            if (result.isSuccess) {
                // 刷新课表
                loadSchedulesForTeachingClass(teachingClassId)
                onResult(true)
            } else {
                onResult(false)
            }
        }
    }

    fun saveArrangedSchedules(schedules: List<ClassSchedule>, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            Log.d("QuickSchedule", "开始保存排课：${schedules}")
            val ok = scheduleRepository.batchAddSchedules(schedules)
            Log.d("QuickSchedule", "保存排课结果：$ok")
            onResult(ok)
        }
    }

    fun loadAllClassrooms() {
        viewModelScope.launch {
            val result = classroomAdminRepository.getClassrooms()
            _allClassrooms.value = result.getOrElse { emptyList() }
        }
    }

    fun loadAllSchedules() {
        viewModelScope.launch {
            val result = scheduleRepository.getAllSchedules()
            _allSchedules.value = result.getOrElse { emptyList() }
        }
    }

    // 批量删除全校排课
    fun deleteAllSchedules(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = scheduleRepository.deleteAllSchedules()
            if (ok) loadAllSchedules()
            onResult(ok)
        }
    }

    // 全部教学班智能排课（遗传算法）
    fun autoArrangeByGeneticForAllTeachingClasses(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = scheduleRepository.autoScheduleForAllTeachingClasses()
            if (result.isSuccess) {
                // 刷新全部课表
                loadAllSchedules()
                onResult(true)
            } else {
                onResult(false)
            }
        }
    }

    // 设置某班级的节次设置
    fun setClassCustomSectionTimes(teachingClassId: Long, sectionTimes: List<String>) {
        _classCustomSectionTimes.value = _classCustomSectionTimes.value.toMutableMap().apply {
            put(teachingClassId, sectionTimes)
        }
    }
} 