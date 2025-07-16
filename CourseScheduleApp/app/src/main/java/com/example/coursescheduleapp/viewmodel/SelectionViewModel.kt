package com.example.coursescheduleapp.viewmodel

import android.util.Log
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
    
    fun loadMyCourses(userId: Long, isTeacher: Boolean = false) {
        _myCoursesState.value = MyCoursesState.Loading
        android.util.Log.d("SelectionViewModel", "loadMyCourses called - userId: $userId, isTeacher: $isTeacher")
        viewModelScope.launch {
            try {
                val result = if (isTeacher) {
                    // 教师加载自己的教学班
                    android.util.Log.d("SelectionViewModel", "Loading teacher teaching classes")
                    selectionRepository.getTeacherTeachingClasses(userId)
                } else {
                    // 学生加载自己的课程
                    android.util.Log.d("SelectionViewModel", "Loading student courses")
                    selectionRepository.getMyCourses(userId)
                }
                
                result.onSuccess { courses ->
                    android.util.Log.d("SelectionViewModel", "Successfully loaded ${courses.size} courses")
                    _myCoursesState.value = MyCoursesState.Success(courses)
                }.onFailure { exception ->
                    android.util.Log.e("SelectionViewModel", "Failed to load courses", exception)
                    _myCoursesState.value = MyCoursesState.Error(exception.message ?: "获取数据失败")
                }
            } catch (e: Exception) {
                android.util.Log.e("SelectionViewModel", "Exception in loadMyCourses", e)
                _myCoursesState.value = MyCoursesState.Error("发生错误: ${e.message}")
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