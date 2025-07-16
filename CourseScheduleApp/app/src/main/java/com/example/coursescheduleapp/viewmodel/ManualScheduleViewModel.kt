package com.example.coursescheduleapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coursescheduleapp.model.ClassSchedule
import com.example.coursescheduleapp.model.TeachingClass
import com.example.coursescheduleapp.model.Classroom
import com.example.coursescheduleapp.ui.screens.ManualScheduleData
import com.example.coursescheduleapp.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import com.example.coursescheduleapp.model.Course

@HiltViewModel
class ManualScheduleViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {
    private val _uiState = MutableStateFlow(ManualScheduleUiState())
    val uiState: StateFlow<ManualScheduleUiState> = _uiState.asStateFlow()

    private val _feedback = MutableStateFlow<FeedbackMessage?>(null)
    val feedback: StateFlow<FeedbackMessage?> = _feedback.asStateFlow()

    init {
        loadAll()
    }

    fun loadAll() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            try {
                val teachingClassesResp = apiService.getAllTeachingClasses()
                val classroomsResp = apiService.getAllClassrooms()
                val schedulesResp = apiService.getAllSchedules()
                val coursesResp = apiService.getAllCourses()
                if (teachingClassesResp.isSuccessful && classroomsResp.isSuccessful && schedulesResp.isSuccessful && coursesResp.isSuccessful) {
                    _uiState.value = _uiState.value.copy(
                        teachingClasses = teachingClassesResp.body() ?: emptyList(),
                        classrooms = classroomsResp.body() ?: emptyList(),
                        schedules = schedulesResp.body() ?: emptyList(),
                        courses = coursesResp.body() ?: emptyList(),
                        isLoading = false
                    )
                } else {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    fun addSchedule(data: ManualScheduleData) {
        val check = validateSchedule(data, null)
        if (check != null) {
            _feedback.value = FeedbackMessage.Error(check)
            return
        }
        viewModelScope.launch {
            try {
                apiService.addSchedule(
                    teachingClassId = data.teachingClassId,
                    scheduleData = mapOf(
                        "dayOfWeek" to data.dayOfWeek,
                        "startTime" to data.startTime,
                        "endTime" to data.endTime,
                        "classroomId" to data.classroomId
                    )
                )
                _feedback.value = FeedbackMessage.Success("排课成功")
                loadAll()
            } catch (e: Exception) {
                _feedback.value = FeedbackMessage.Error(e.message ?: "排课失败")
            }
        }
    }

    fun updateSchedule(scheduleId: Long, data: ManualScheduleData) {
        val check = validateSchedule(data, scheduleId)
        if (check != null) {
            _feedback.value = FeedbackMessage.Error(check)
            return
        }
        viewModelScope.launch {
            try {
                apiService.updateSchedule(
                    scheduleId = scheduleId,
                    scheduleData = mapOf(
                        "dayOfWeek" to data.dayOfWeek,
                        "startTime" to data.startTime,
                        "endTime" to data.endTime,
                        "classroomId" to data.classroomId
                    )
                )
                _feedback.value = FeedbackMessage.Success("修改成功")
                loadAll()
            } catch (e: Exception) {
                _feedback.value = FeedbackMessage.Error(e.message ?: "修改失败")
            }
        }
    }

    fun deleteSchedule(scheduleId: Long) {
        viewModelScope.launch {
            try {
                apiService.deleteSchedule(scheduleId)
                _feedback.value = FeedbackMessage.Success("删除成功")
                loadAll()
            } catch (e: Exception) {
                _feedback.value = FeedbackMessage.Error(e.message ?: "删除失败")
            }
        }
    }

    fun clearFeedback() {
        _feedback.value = null
    }

    // 冲突检测和表单校验
    private fun validateSchedule(data: ManualScheduleData, editingId: Long?): String? {
        if (data.startTime.isBlank() || data.endTime.isBlank()) return "时间不能为空"
        if (!data.startTime.matches(Regex("\\d{2}:\\d{2}")) || !data.endTime.matches(Regex("\\d{2}:\\d{2}"))) return "时间格式应为HH:mm"
        if (data.startTime >= data.endTime) return "开始时间必须早于结束时间"
        val conflict = _uiState.value.schedules.any { s ->
            (editingId == null || s.id != editingId) &&
            s.dayOfWeek == data.dayOfWeek &&
            (
                (s.classroomId == data.classroomId) ||
                (s.teachingClassId == data.teachingClassId)
            ) &&
            timeOverlap(s.startTime, s.endTime, data.startTime, data.endTime)
        }
        if (conflict) return "该时间段教室或教学班有冲突"
        return null
    }

    private fun timeOverlap(start1: String, end1: String, start2: String, end2: String): Boolean {
        return start1 < end2 && end1 > start2
    }
}

sealed class FeedbackMessage {
    data class Success(val msg: String): FeedbackMessage()
    data class Error(val msg: String): FeedbackMessage()
}

data class ManualScheduleUiState(
    val isLoading: Boolean = false,
    val teachingClasses: List<TeachingClass> = emptyList(),
    val classrooms: List<Classroom> = emptyList(),
    val schedules: List<ClassSchedule> = emptyList(),
    val courses: List<Course> = emptyList()
) 