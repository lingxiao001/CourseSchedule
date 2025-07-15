package com.example.coursescheduleapp.viewmodel

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coursescheduleapp.model.TeachingClassDetailDTO
import com.example.coursescheduleapp.model.MyCourseDTO
import com.example.coursescheduleapp.repository.SelectionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * 课程选择专用ViewModel
 * 专门处理教学班的选择和退选操作
 */
@HiltViewModel
class CourseSelectionViewModel @Inject constructor(
    private val selectionRepository: SelectionRepository
) : ViewModel() {
    
    private val _operationState = MutableStateFlow<SelectionOperationState>(SelectionOperationState.Idle)
    val operationState: StateFlow<SelectionOperationState> = _operationState.asStateFlow()
    
    private val _conflictCheckResult = MutableStateFlow<ConflictCheckResult?>(null)
    val conflictCheckResult: StateFlow<ConflictCheckResult?> = _conflictCheckResult.asStateFlow()
    
    private val _myCourses = MutableStateFlow<List<MyCourseDTO>?>(null)
    val myCourses: StateFlow<List<MyCourseDTO>?> = _myCourses.asStateFlow()
    
    companion object {
        private const val TAG = "CourseSelectionViewModel"
    }
    
    /**
     * 加载学生已选课程用于冲突检查
     */
    @SuppressLint("LongLogTag")
    fun loadMyCoursesForConflictCheck(studentId: Long) {
        viewModelScope.launch {
            selectionRepository.getMyCourses(studentId)
                .onSuccess { courses ->
                    _myCourses.value = courses
                    Log.d(TAG, "加载${courses.size}个已选课程用于冲突检查")
                }
                .onFailure { exception ->
                    Log.e(TAG, "加载已选课程失败: ${exception.message}")
                }
        }
    }

    /**
     * 检查选课冲突（简化版本，基于课程名称提示）
     */
    @SuppressLint("LongLogTag")
    fun checkCourseConflict(
        studentId: Long,
        targetTeachingClass: TeachingClassDetailDTO,
        onConflictDetected: (ConflictCheckResult) -> Unit
    ) {
        val mySelectedCourses = _myCourses.value ?: run {
            Log.d(TAG, "未加载已选课程，开始加载...")
            loadMyCoursesForConflictCheck(studentId)
            return
        }
        
        Log.d(TAG, "开始检查教学班 ${targetTeachingClass.classCode} 的冲突")
        
        val conflicts = mutableListOf<String>()
        
        // 检查是否选择了同一门课程的不同教学班
        val targetCourseName = targetTeachingClass.courseName
        val sameCourseSelected = mySelectedCourses.find { selectedCourse ->
            selectedCourse.courseName == targetCourseName
        }
        
        if (sameCourseSelected != null) {
            conflicts.add(
                "您已选择了 ${sameCourseSelected.courseName} 课程，不能重复选择"
            )
        }
        
        // 检查时间冲突（使用现有的schedules）
        val targetSchedules = targetTeachingClass.schedules
        if (targetSchedules.isNotEmpty()) {
            // 这里我们简化处理，只检查目标教学班的自身时间冲突提示
            // 实际的时间冲突检查需要更复杂的逻辑
        }
        
        val result = if (conflicts.isEmpty()) {
            ConflictCheckResult.NoConflict
        } else {
            ConflictCheckResult.Conflict(conflicts)
        }
        
        _conflictCheckResult.value = result
        onConflictDetected(result)
    }


    /**
     * 清除冲突检查结果
     */
    fun clearConflictCheckResult() {
        _conflictCheckResult.value = null
    }

    /**
     * 选课操作，带冲突检查
     */
    fun selectCourseWithConflictCheck(
        studentId: Long,
        teachingClass: TeachingClassDetailDTO,
        onConflictDetected: (ConflictCheckResult) -> Unit,
        onSuccess: () -> Unit
    ) {
        checkCourseConflict(studentId, teachingClass) { result ->
            when (result) {
                is ConflictCheckResult.NoConflict -> {
                    // 无冲突，执行选课
                    selectCourse(studentId, teachingClass.teachingClassId)
                    onSuccess()
                }
                is ConflictCheckResult.Conflict -> {
                    // 有冲突，回调通知
                    onConflictDetected(result)
                }
            }
        }
    }

    /**
     * 选课操作
     */
    @SuppressLint("LongLogTag")
    fun selectCourse(studentId: Long, teachingClassId: Long) {
        Log.d(TAG, "学生ID: $studentId 开始选课，教学班ID: $teachingClassId")
        _operationState.value = SelectionOperationState.Loading
        
        viewModelScope.launch {
            selectionRepository.selectCourse(studentId, teachingClassId)
                .onSuccess { message ->
                    Log.d(TAG, "选课成功: $message")
                    _operationState.value = SelectionOperationState.Success(message)
                    // 选课成功后重新加载已选课程
                    loadMyCoursesForConflictCheck(studentId)
                }
                .onFailure { exception ->
                    Log.e(TAG, "选课失败: ${exception.message}")
                    _operationState.value = SelectionOperationState.Error(exception.message ?: "选课失败")
                }
        }
    }
    
    /**
     * 退课操作
     */
    @SuppressLint("LongLogTag")
    fun cancelSelection(studentId: Long, teachingClassId: Long) {
        Log.d(TAG, "学生ID: $studentId 开始退课，教学班ID: $teachingClassId")
        _operationState.value = SelectionOperationState.Loading
        
        viewModelScope.launch {
            selectionRepository.cancelSelection(studentId, teachingClassId)
                .onSuccess { message ->
                    Log.d(TAG, "退课成功: $message")
                    _operationState.value = SelectionOperationState.Success(message)
                }
                .onFailure { exception ->
                    Log.e(TAG, "退课失败: ${exception.message}")
                    _operationState.value = SelectionOperationState.Error(exception.message ?: "退课失败")
                }
        }
    }
    
    /**
     * 清除操作状态
     */
    fun clearOperationState() {
        _operationState.value = SelectionOperationState.Idle
    }
}

/**
 * 冲突检查结果
 */
sealed class ConflictCheckResult {
    object NoConflict : ConflictCheckResult()
    data class Conflict(val conflicts: List<String>) : ConflictCheckResult()
}
