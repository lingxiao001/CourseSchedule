package com.example.coursescheduleapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coursescheduleapp.model.CourseWithTeachingClassesDTO
import com.example.coursescheduleapp.repository.SelectionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 课程列表ViewModel
 * 管理按课程分组的课程数据
 */
@HiltViewModel
class CourseListViewModel @Inject constructor(
    private val selectionRepository: SelectionRepository
) : ViewModel() {

    private val _coursesState = MutableStateFlow<CourseListState>(CourseListState.Loading)
    val coursesState: StateFlow<CourseListState> = _coursesState.asStateFlow()

    private var currentStudentId: Long = 0

    /**
     * 加载课程列表
     * @param studentId 学生ID
     */
    fun loadCourses(studentId: Long) {
        Log.d("CourseListViewModel", "开始加载课程列表，学生ID: $studentId")
        currentStudentId = studentId
        
        viewModelScope.launch {
            try {
                _coursesState.value = CourseListState.Loading
                
                val result = selectionRepository.getAvailableCoursesGroupedByCourse(studentId)
                
                result.onSuccess { courses ->
                    Log.d("CourseListViewModel", "成功加载 ${courses.size} 门课程")
                    _coursesState.value = CourseListState.Success(courses)
                }.onFailure { exception ->
                    Log.e("CourseListViewModel", "加载课程列表失败", exception)
                    _coursesState.value = CourseListState.Error("加载失败: ${exception.message}")
                }
                
            } catch (e: Exception) {
                Log.e("CourseListViewModel", "加载课程列表失败", e)
                _coursesState.value = CourseListState.Error("加载失败: ${e.message}")
            }
        }
    }

    /**
     * 重新加载当前课程列表
     */
    fun reloadCourses() {
        if (currentStudentId > 0) {
            loadCourses(currentStudentId)
        }
    }
}

/**
 * 课程列表状态
 */
sealed class CourseListState {
    object Loading : CourseListState()
    data class Success(val courses: List<CourseWithTeachingClassesDTO>) : CourseListState()
    data class Error(val message: String) : CourseListState()
}