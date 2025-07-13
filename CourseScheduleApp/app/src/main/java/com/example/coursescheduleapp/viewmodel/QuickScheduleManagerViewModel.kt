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
    // 快速安排算法（简单均匀分配示例）
    fun autoArrangeSchedules(teachingClassId: Long, sectionTimes: List<String>, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            // 1. 获取当前已有排课，2. 生成新排课，3. 批量保存
            val current = scheduleRepository.getSchedulesByTeachingClass(teachingClassId).getOrNull() ?: emptyList()
            val used = current.map { it.dayOfWeek to it.startTime }.toSet()
            val newSchedules = mutableListOf<ClassSchedule>()
            var day = 1
            for ((idx, time) in sectionTimes.withIndex()) {
                // 均匀分配到一周，避开已用
                while ((day to time) in used) { day = (day % 7) + 1 }
                newSchedules.add(
                    ClassSchedule(
                        id = 0L,
                        dayOfWeek = day,
                        startTime = time.split("-")[0],
                        endTime = time.split("-")[1],
                        classroomName = "自动分配",
                        building = "自动分配",
                        teachingClassId = teachingClassId,
                        classroomId = 0L,
                        courseName = "自动课程",
                        classCode = "自动班级"
                    )
                )
                day = (day % 7) + 1
            }
            val ok = scheduleRepository.batchAddSchedules(newSchedules)
            onResult(ok)
        }
    }

    fun autoArrangeSchedulesWithPreview(
        teachingClassId: Long,
        sectionTimes: List<String>,
        allSchedules: List<ClassSchedule>,
        availableClassrooms: List<Long>,
        teacherId: Long?,
        onResult: (ArrangeResult) -> Unit
    ) {
        viewModelScope.launch {
            Log.d("QuickSchedule", "进入autoArrangeSchedulesWithPreview")
            val used = allSchedules.map { Triple(it.dayOfWeek, it.startTime, it.classroomId) }.toSet()
            val teacherUsed = allSchedules.filter { it.teachingClassId != teachingClassId && it.classroomId != 0L }
                .map { Pair(it.dayOfWeek, it.startTime) to it.teachingClassId }.toMap()
            val newSchedules = mutableListOf<ClassSchedule>()
            val conflicts = mutableListOf<String>()
            var day = 1
            for ((idx, time) in sectionTimes.withIndex()) {
                var assigned = false
                for (classroomId in availableClassrooms) {
                    val key = Triple(day, time.split("-")[0], classroomId)
                    val teacherKey = Pair(day, time.split("-")[0])
                    if (key !in used && (teacherId == null || teacherUsed[teacherKey] != teacherId)) {
                        newSchedules.add(
                            ClassSchedule(
                                id = 0L,
                                dayOfWeek = day,
                                startTime = time.split("-")[0],
                                endTime = time.split("-")[1],
                                classroomName = "自动分配",
                                building = "自动分配",
                                teachingClassId = teachingClassId,
                                classroomId = classroomId,
                                courseName = "自动课程",
                                classCode = "自动班级"
                            )
                        )
                        assigned = true
                        break
                    }
                }
                if (!assigned) {
                    conflicts.add("第${idx + 1}节(${time})无可用教室或教师冲突")
                }
                day = (day % 7) + 1
            }
            Log.d("QuickSchedule", "生成排课：${newSchedules}")
            onResult(ArrangeResult(conflicts.isEmpty(), newSchedules, conflicts))
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

    // 设置某班级的节次设置
    fun setClassCustomSectionTimes(teachingClassId: Long, sectionTimes: List<String>) {
        _classCustomSectionTimes.value = _classCustomSectionTimes.value.toMutableMap().apply {
            put(teachingClassId, sectionTimes)
        }
    }
} 