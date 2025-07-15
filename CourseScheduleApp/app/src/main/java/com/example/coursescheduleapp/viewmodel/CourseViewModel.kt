package com.example.coursescheduleapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coursescheduleapp.model.Course
import com.example.coursescheduleapp.model.TeachingClass
import com.example.coursescheduleapp.repository.CourseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CourseViewModel @Inject constructor(
    private val courseRepository: CourseRepository
) : ViewModel() {
    
    private val _coursesState = MutableStateFlow<CoursesState>(CoursesState.Idle)
    val coursesState: StateFlow<CoursesState> = _coursesState.asStateFlow()
    
    private val _teachingClassesState = MutableStateFlow<TeachingClassesState>(TeachingClassesState.Idle)
    val teachingClassesState: StateFlow<TeachingClassesState> = _teachingClassesState.asStateFlow()
    
    fun loadAllCourses() {
        _coursesState.value = CoursesState.Loading
        viewModelScope.launch {
            courseRepository.getAllCourses()
                .onSuccess { courses ->
                    _coursesState.value = CoursesState.Success(courses)
                }
                .onFailure { exception ->
                    _coursesState.value = CoursesState.Error(exception.message ?: "获取课程列表失败")
                }
        }
    }
    
    fun loadCourseById(id: Long) {
        _coursesState.value = CoursesState.Loading
        viewModelScope.launch {
            courseRepository.getCourseById(id)
                .onSuccess { course ->
                    _coursesState.value = CoursesState.Success(listOf(course))
                }
                .onFailure { exception ->
                    _coursesState.value = CoursesState.Error(exception.message ?: "获取课程详情失败")
                }
        }
    }
    
    fun loadAllTeachingClasses() {
        _teachingClassesState.value = TeachingClassesState.Loading
        viewModelScope.launch {
            courseRepository.getAllTeachingClasses()
                .onSuccess { teachingClasses ->
                    _teachingClassesState.value = TeachingClassesState.Success(teachingClasses)
                }
                .onFailure { exception ->
                    _teachingClassesState.value = TeachingClassesState.Error(exception.message ?: "获取教学班列表失败")
                }
        }
    }
    
    fun loadTeachingClassesByCourse(courseId: Long) {
        _teachingClassesState.value = TeachingClassesState.Loading
        viewModelScope.launch {
            courseRepository.getTeachingClassesByCourse(courseId)
                .onSuccess { teachingClasses ->
                    _teachingClassesState.value = TeachingClassesState.Success(teachingClasses)
                }
                .onFailure { exception ->
                    _teachingClassesState.value = TeachingClassesState.Error(exception.message ?: "获取课程教学班失败")
                }
        }
    }
    
    fun clearError() {
        _coursesState.value = CoursesState.Idle
        _teachingClassesState.value = TeachingClassesState.Idle
    }
}

sealed class CoursesState {
    object Idle : CoursesState()
    object Loading : CoursesState()
    data class Success(val courses: List<Course>) : CoursesState()
    data class Error(val message: String) : CoursesState()
}

sealed class TeachingClassesState {
    object Idle : TeachingClassesState()
    object Loading : TeachingClassesState()
    data class Success(val teachingClasses: List<TeachingClass>) : TeachingClassesState()
    data class Error(val message: String) : TeachingClassesState()
} 