    package com.example.coursescheduleapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coursescheduleapp.model.ClassSchedule
import com.example.coursescheduleapp.model.TeachingClass
import com.example.coursescheduleapp.repository.CourseRepository
import com.example.coursescheduleapp.repository.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository,
    private val courseRepository: CourseRepository // 新增依赖
) : ViewModel() {
    
    private val _scheduleState = MutableStateFlow<ScheduleState>(ScheduleState.Idle)
    val scheduleState: StateFlow<ScheduleState> = _scheduleState.asStateFlow()

    // 新增：选中课程和弹窗显示状态
    private val _selectedSchedule = MutableStateFlow<ClassSchedule?>(null)
    val selectedSchedule: StateFlow<ClassSchedule?> = _selectedSchedule.asStateFlow()
    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog.asStateFlow()

    // 新增：教学班缓存和选中教学班
    private val _teachingClassMap = MutableStateFlow<Map<Long, TeachingClass>>(emptyMap())
    val teachingClassMap: StateFlow<Map<Long, TeachingClass>> = _teachingClassMap.asStateFlow()
    private val _selectedTeachingClass = MutableStateFlow<TeachingClass?>(null)
    val selectedTeachingClass: StateFlow<TeachingClass?> = _selectedTeachingClass.asStateFlow()

    init {
        // 初始化时拉取所有教学班，缓存到本地
        viewModelScope.launch {
            courseRepository.getAllTeachingClasses().onSuccess { list ->
                _teachingClassMap.value = list.associateBy { it.id }
            }
        }
    }

    fun onCourseClick(schedule: ClassSchedule?) {
        _selectedSchedule.value = schedule
        _showDialog.value = schedule != null
        // 根据选中课表的 teachingClassId 查找教学班
        val teachingClass = schedule?.let { _teachingClassMap.value[it.teachingClassId] }
        _selectedTeachingClass.value = teachingClass
    }
    fun closeDialog() {
        _showDialog.value = false
    }
    
    fun loadSchedulesByTeacher(teacherId: Long) {
        _scheduleState.value = ScheduleState.Loading
        viewModelScope.launch {
            scheduleRepository.getSchedulesByTeacher(teacherId)
                .onSuccess { schedules ->
                    _scheduleState.value = ScheduleState.Success(schedules)
                }
                .onFailure { exception ->
                    _scheduleState.value = ScheduleState.Error(exception.message ?: "获取课表失败")
                }
        }
    }
    
    fun loadSchedulesByTeachingClass(teachingClassId: Long) {
        _scheduleState.value = ScheduleState.Loading
        viewModelScope.launch {
            scheduleRepository.getSchedulesByTeachingClass(teachingClassId)
                .onSuccess { schedules ->
                    _scheduleState.value = ScheduleState.Success(schedules)
                }
                .onFailure { exception ->
                    _scheduleState.value = ScheduleState.Error(exception.message ?: "获取课表失败")
                }
        }
    }
    
    fun loadSchedulesByStudent(studentId: Long) {
        _scheduleState.value = ScheduleState.Loading
        viewModelScope.launch {
            scheduleRepository.getSchedulesByStudent(studentId)
                .onSuccess { schedules ->
                    _scheduleState.value = ScheduleState.Success(schedules)
                }
                .onFailure { exception ->
                    _scheduleState.value = ScheduleState.Error(exception.message ?: "获取课表失败")
                }
        }
    }
    
    fun clearError() {
        _scheduleState.value = ScheduleState.Idle
    }
}

sealed class ScheduleState {
    object Idle : ScheduleState()
    object Loading : ScheduleState()
    data class Success(val schedules: List<ClassSchedule>) : ScheduleState()
    data class Error(val message: String) : ScheduleState()
} 