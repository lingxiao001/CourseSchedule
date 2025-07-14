package com.example.coursescheduleapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coursescheduleapp.model.MyCourseDTO
import com.example.coursescheduleapp.repository.SelectionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SelectionViewModel @Inject constructor(
    private val selectionRepository: SelectionRepository
) : ViewModel() {
    
    private val _myCoursesState = MutableStateFlow<MyCoursesState>(MyCoursesState.Idle)
    val myCoursesState: StateFlow<MyCoursesState> = _myCoursesState.asStateFlow()
    
    fun loadMyCourses(studentId: Long) {
        _myCoursesState.value = MyCoursesState.Loading
        viewModelScope.launch {
            selectionRepository.getMyCourses(studentId)
                .onSuccess { courses ->
                    _myCoursesState.value = MyCoursesState.Success(courses)
                }
                .onFailure { exception ->
                    _myCoursesState.value = MyCoursesState.Error(exception.message ?: "获取我的课程失败")
                }
        }
    }
    
    fun selectCourse(studentId: Long, teachingClassId: Long) {
        viewModelScope.launch {
            selectionRepository.selectCourse(studentId, teachingClassId)
                .onSuccess { message ->
                    // 可以显示成功消息
                }
                .onFailure { exception ->
                    // 可以显示错误消息
                }
        }
    }
    
    fun cancelSelection(studentId: Long, teachingClassId: Long) {
        viewModelScope.launch {
            selectionRepository.cancelSelection(studentId, teachingClassId)
                .onSuccess { message ->
                    // 可以显示成功消息
                }
                .onFailure { exception ->
                    // 可以显示错误消息
                }
        }
    }
    
    fun clearError() {
        _myCoursesState.value = MyCoursesState.Idle
    }
}

sealed class MyCoursesState {
    object Idle : MyCoursesState()
    object Loading : MyCoursesState()
    data class Success(val courses: List<MyCourseDTO>) : MyCoursesState()
    data class Error(val message: String) : MyCoursesState()
} 