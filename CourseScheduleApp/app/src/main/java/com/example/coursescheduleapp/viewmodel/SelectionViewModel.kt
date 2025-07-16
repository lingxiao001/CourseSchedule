package com.example.coursescheduleapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coursescheduleapp.model.AvailableCourseDTO
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

    private val _availableCoursesState = MutableStateFlow<AvailableCoursesState>(AvailableCoursesState.Idle)
    val availableCoursesState: StateFlow<AvailableCoursesState> = _availableCoursesState.asStateFlow()

    private val _selectionOperationState = MutableStateFlow<SelectionOperationState>(SelectionOperationState.Idle)
    val selectionOperationState: StateFlow<SelectionOperationState> = _selectionOperationState.asStateFlow()

    companion object {
        private const val TAG = "SelectionViewModel"
    }

    /**
     * 加载学生的已选课程
     * @param studentId 学生ID
     */
    fun loadMyCourses(studentId: Long) {
        Log.d(TAG, "开始加载学生ID: $studentId 的已选课程")
        _myCoursesState.value = MyCoursesState.Loading
        viewModelScope.launch {
            selectionRepository.getMyCourses(studentId)
                .onSuccess { courses ->
                    Log.d(TAG, "成功获取 ${courses.size} 门已选课程")
                    _myCoursesState.value = MyCoursesState.Success(courses)
                }
                .onFailure { exception ->
                    Log.e(TAG, "获取已选课程失败: ${exception.message}")
                    _myCoursesState.value = MyCoursesState.Error(exception.message ?: "获取我的课程失败")
                }
        }
    }

    /**
     * 加载可选课程列表
     * @param studentId 学生ID
     */
    fun loadAvailableCourses(studentId: Long) {
        Log.d(TAG, "开始加载学生ID: $studentId 的可选课程")
        _availableCoursesState.value = AvailableCoursesState.Loading
        viewModelScope.launch {
            selectionRepository.getAvailableCourses(studentId)
                .onSuccess { courses ->
                    Log.d(TAG, "成功获取 ${courses.size} 门可选课程")
                    _availableCoursesState.value = AvailableCoursesState.Success(courses)
                }
                .onFailure { exception ->
                    Log.e(TAG, "获取可选课程失败: ${exception.message}")
                    _availableCoursesState.value = AvailableCoursesState.Error(exception.message ?: "获取可选课程失败")
                }
        }
    }

    /**
     * 学生选课操作
     * @param studentId 学生ID
     * @param teachingClassId 教学班ID
     */
    fun selectCourse(studentId: Long, teachingClassId: Long) {
        Log.d(TAG, "学生ID: $studentId 开始选课，教学班ID: $teachingClassId")
        _selectionOperationState.value = SelectionOperationState.Loading
        viewModelScope.launch {
            selectionRepository.selectCourse(studentId, teachingClassId)
                .onSuccess { message ->
                    Log.d(TAG, "选课成功: $message")
                    _selectionOperationState.value = SelectionOperationState.Success(message)
                    // 选课成功后刷新课程列表
                    loadMyCourses(studentId)
                    loadAvailableCourses(studentId)
                }
                .onFailure { exception ->
                    Log.e(TAG, "选课失败: ${exception.message}")
                    _selectionOperationState.value = SelectionOperationState.Error(exception.message ?: "选课失败")
                }
        }
    }

    /**
     * 学生退课操作
     * @param studentId 学生ID
     * @param teachingClassId 教学班ID
     */
    fun cancelSelection(studentId: Long, teachingClassId: Long) {
        Log.d(TAG, "学生ID: $studentId 开始退课，教学班ID: $teachingClassId")
        _selectionOperationState.value = SelectionOperationState.Loading
        viewModelScope.launch {
            selectionRepository.cancelSelection(studentId, teachingClassId)
                .onSuccess { message ->
                    Log.d(TAG, "退课成功: $message")
                    _selectionOperationState.value = SelectionOperationState.Success(message)
                    // 退课成功后刷新课程列表
                    loadMyCourses(studentId)
                    loadAvailableCourses(studentId)
                }
                .onFailure { exception ->
                    Log.e(TAG, "退课失败: ${exception.message}")
                    _selectionOperationState.value = SelectionOperationState.Error(exception.message ?: "退课失败")
                }
        }
    }

    /**
     * 清除选课操作状态
     */
    fun clearSelectionOperationState() {
        _selectionOperationState.value = SelectionOperationState.Idle
    }

    /**
     * 清除错误状态
     */
    fun clearError() {
        _myCoursesState.value = MyCoursesState.Idle
        _availableCoursesState.value = AvailableCoursesState.Idle
    }
}

sealed class MyCoursesState {
    object Idle : MyCoursesState()
    object Loading : MyCoursesState()
    data class Success(val courses: List<MyCourseDTO>) : MyCoursesState()
    data class Error(val message: String) : MyCoursesState()
}

sealed class AvailableCoursesState {
    object Idle : AvailableCoursesState()
    object Loading : AvailableCoursesState()
    data class Success(val courses: List<AvailableCourseDTO>) : AvailableCoursesState()
    data class Error(val message: String) : AvailableCoursesState()
}

sealed class SelectionOperationState {
    object Idle : SelectionOperationState()
    object Loading : SelectionOperationState()
    data class Success(val message: String) : SelectionOperationState()
    data class Error(val message: String) : SelectionOperationState()
} 