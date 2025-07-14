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

/**
 * 快速排课管理 ViewModel
 * 负责调度课表、教学班、教室等数据，调用后端自动排课接口
 */
@HiltViewModel
class QuickScheduleManagerViewModel @Inject constructor(
    private val sectionSettingRepository: SectionSettingRepository,
    private val scheduleRepository: ScheduleRepository,
    private val teachingClassAdminRepository: TeachingClassAdminRepository,
    private val courseRepository: CourseRepository,
    private val classroomAdminRepository: ClassroomAdminRepository
) : ViewModel() {
    /** 节次时间设置 */
    private val _sectionTimes = MutableStateFlow<List<String>>(emptyList())
    val sectionTimes: StateFlow<List<String>> = _sectionTimes.asStateFlow()

    /** 教学班列表 */
    private val _teachingClasses = MutableStateFlow<List<TeachingClass>>(emptyList())
    val teachingClasses: StateFlow<List<TeachingClass>> = _teachingClasses.asStateFlow()

    /** 课程ID到课程名的映射 */
    private val _courseNameMap = MutableStateFlow<Map<Long, String>>(emptyMap())
    val courseNameMap: StateFlow<Map<Long, String>> = _courseNameMap.asStateFlow()

    /** 当前教学班的课表 */
    private val _currentClassSchedules = MutableStateFlow<List<ClassSchedule>>(emptyList())
    val currentClassSchedules: StateFlow<List<ClassSchedule>> = _currentClassSchedules.asStateFlow()

    /** 所有教室列表 */
    private val _allClassrooms = MutableStateFlow<List<Classroom>>(emptyList())
    val allClassrooms: StateFlow<List<Classroom>> = _allClassrooms.asStateFlow()

    /** 全部课表（全校） */
    private val _allSchedules = MutableStateFlow<List<ClassSchedule>>(emptyList())
    val allSchedules: StateFlow<List<ClassSchedule>> = _allSchedules.asStateFlow()

    /** 每个班级自定义节次设置（Map<teachingClassId, List<String>>） */
    private val _classCustomSectionTimes = MutableStateFlow<Map<Long, List<String>>>(emptyMap())
    val classCustomSectionTimes: StateFlow<Map<Long, List<String>>> = _classCustomSectionTimes.asStateFlow()

    init {
        loadSectionSetting()
        loadTeachingClasses()
        loadAllClassrooms()
    }

    /** 加载节次设置 */
    fun loadSectionSetting() {
        viewModelScope.launch {
            val entity = sectionSettingRepository.getSectionSetting()
            if (entity != null) {
                _sectionTimes.value = Json.decodeFromString(ListSerializer(String.serializer()), entity.sectionTimes)
            } else {
                // 设置默认 5 节课时间
                _sectionTimes.value = listOf(
                    "08:00-09:30",
                    "10:00-11:30",
                    "13:30-15:00",
                    "15:30-17:00",
                    "18:00-19:30"
                )
            }
        }
    }

    /** 保存节次设置 */
    fun saveSectionSetting(sectionTimes: List<String>) {
        viewModelScope.launch {
            sectionSettingRepository.saveSectionSetting(sectionTimes)
            _sectionTimes.value = sectionTimes
        }
    }

    /** 加载所有教学班和课程名映射 */
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

    /** 加载指定教学班的课表 */
    fun loadSchedulesForTeachingClass(teachingClassId: Long) {
        viewModelScope.launch {
            val result = scheduleRepository.getSchedulesByTeachingClass(teachingClassId)
            _currentClassSchedules.value = result.getOrElse { emptyList() }
        }
    }

    /** 批量删除某教学班所有排课 */
    fun deleteAllSchedulesForClass(teachingClassId: Long, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val result = scheduleRepository.deleteAllSchedulesForClass(teachingClassId)
            onResult(result)
        }
    }

    /**
     * 调用后端接口：智能排课（单个教学班，遗传算法）
     * @param teachingClassId 教学班ID
     * @param onResult 排课结果回调（true=成功，false=失败）
     */
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

    /** 加载所有教室 */
    fun loadAllClassrooms() {
        viewModelScope.launch {
            val result = classroomAdminRepository.getClassrooms()
            _allClassrooms.value = result.getOrElse { emptyList() }
        }
    }

    /** 加载全校所有课表 */
    fun loadAllSchedules() {
        viewModelScope.launch {
            val result = scheduleRepository.getAllSchedules()
            _allSchedules.value = result.getOrElse { emptyList() }
        }
    }

    /**
     * 批量删除全校排课
     * @param onResult 删除结果回调（true=成功，false=失败）
     */
    fun deleteAllSchedules(onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = scheduleRepository.deleteAllSchedules()
            if (ok) loadAllSchedules()
            onResult(ok)
        }
    }

    /**
     * 调用后端接口：全部教学班智能排课（遗传算法）
     * @param onResult 排课结果回调（true=成功，false=失败）
     */
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

    /** 设置某班级的节次设置 */
    fun setClassCustomSectionTimes(teachingClassId: Long, sectionTimes: List<String>) {
        _classCustomSectionTimes.value = _classCustomSectionTimes.value.toMutableMap().apply {
            put(teachingClassId, sectionTimes)
        }
    }

    /** 删除单条课表 */
    fun deleteSchedule(scheduleId: Long, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val ok = scheduleRepository.deleteSchedule(scheduleId)
            onResult(ok)
        }
    }
} 