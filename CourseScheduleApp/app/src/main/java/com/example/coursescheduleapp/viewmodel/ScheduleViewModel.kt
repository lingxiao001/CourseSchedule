package com.example.coursescheduleapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coursescheduleapp.model.ClassSchedule
import com.example.coursescheduleapp.repository.ScheduleRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ScheduleViewModel @Inject constructor(
    private val scheduleRepository: ScheduleRepository
) : ViewModel() {
    
    private val _scheduleState = MutableStateFlow<ScheduleState>(ScheduleState.Idle)
    val scheduleState: StateFlow<ScheduleState> = _scheduleState.asStateFlow()
    
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