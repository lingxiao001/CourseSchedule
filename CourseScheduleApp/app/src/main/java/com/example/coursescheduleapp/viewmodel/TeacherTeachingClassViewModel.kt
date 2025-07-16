package com.example.coursescheduleapp.viewmodel

import android.annotation.SuppressLint
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coursescheduleapp.model.TeachingClass
import com.example.coursescheduleapp.model.MyCourseDTO
import com.example.coursescheduleapp.model.Course
import com.example.coursescheduleapp.model.SelectionDTO
import com.example.coursescheduleapp.repository.TeachingClassAdminRepository
import com.example.coursescheduleapp.repository.CourseRepository
import com.example.coursescheduleapp.repository.SelectionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeacherTeachingClassViewModel @Inject constructor(
    private val teachingClassRepository: TeachingClassAdminRepository,
    private val courseRepository: CourseRepository,
    private val selectionRepository: SelectionRepository
) : ViewModel() {

    private val _teacherClassesState = MutableStateFlow<TeacherClassesState>(TeacherClassesState.Idle)
    val teacherClassesState: StateFlow<TeacherClassesState> = _teacherClassesState.asStateFlow()

    private val _coursesState = MutableStateFlow<CoursesState>(CoursesState.Idle)
    val coursesState: StateFlow<CoursesState> = _coursesState.asStateFlow()

    private val _createTeachingClassState = MutableStateFlow<CreateTeachingClassState>(CreateTeachingClassState.Idle)
    val createTeachingClassState: StateFlow<CreateTeachingClassState> = _createTeachingClassState.asStateFlow()

    private val _studentListState = MutableStateFlow<StudentListState>(StudentListState.Idle)
    val studentListState: StateFlow<StudentListState> = _studentListState.asStateFlow()

    companion object {
        private const val TAG = "TeacherTeachingClassViewModel"
    }

    /**
     * 加载教师的所有教学班
     * @param teacherId 教师ID
     */
    @SuppressLint("LongLogTag")
    fun loadTeacherTeachingClasses(teacherId: Long) {
        Log.d(TAG, "开始加载教师ID: $teacherId 的教学班")
        _teacherClassesState.value = TeacherClassesState.Loading
        viewModelScope.launch {
            selectionRepository.getTeacherTeachingClasses(teacherId)
                .onSuccess { classes ->
                    Log.d(TAG, "成功获取 ${classes.size} 个教学班")
                    _teacherClassesState.value = TeacherClassesState.Success(classes)
                }
                .onFailure { exception ->
                    Log.e(TAG, "获取教师教学班失败", exception)
                    _teacherClassesState.value = TeacherClassesState.Error("获取教学班失败: ${exception.message}")
                }
        }
    }

    /**
     * 加载所有课程列表（用于创建教学班时选择课程）
     */
    @SuppressLint("LongLogTag")
    fun loadAllCourses() {
        Log.d(TAG, "开始加载所有课程")
        _coursesState.value = CoursesState.Loading
        viewModelScope.launch {
            courseRepository.getAllCourses()
                .onSuccess { courses ->
                    Log.d(TAG, "成功获取 ${courses.size} 门课程")
                    _coursesState.value = CoursesState.Success(courses)
                }
                .onFailure { exception ->
                    Log.e(TAG, "获取课程列表失败", exception)
                    _coursesState.value = CoursesState.Error("获取课程列表失败: ${exception.message}")
                }
        }
    }

    /**
     * 创建新的教学班
     * @param courseId 课程ID
     * @param teachingClass 教学班信息
     */
    @SuppressLint("LongLogTag")
    fun createTeachingClass(courseId: Long, teachingClass: TeachingClass) {
        Log.d(TAG, "开始创建教学班，课程ID: $courseId")
        _createTeachingClassState.value = CreateTeachingClassState.Loading
        viewModelScope.launch {
            teachingClassRepository.createTeachingClass(courseId, teachingClass)
                .onSuccess { createdClass ->
                    Log.d(TAG, "教学班创建成功: ${createdClass.classCode}")
                    _createTeachingClassState.value = CreateTeachingClassState.Success(createdClass)
                    // 刷新教师教学班列表
                    loadTeacherTeachingClasses(teachingClass.teacherId)
                }
                .onFailure { exception ->
                    Log.e(TAG, "创建教学班失败", exception)
                    _createTeachingClassState.value = CreateTeachingClassState.Error("创建教学班失败: ${exception.message}")
                }
        }
    }

    /**
     * 获取教学班的学生列表
     * @param teachingClassId 教学班ID
     */
    @SuppressLint("LongLogTag")
    fun loadStudentsByTeachingClass(teachingClassId: Long) {
        Log.d(TAG, "开始加载教学班ID: $teachingClassId 的学生列表")
        _studentListState.value = StudentListState.Loading
        viewModelScope.launch {
            selectionRepository.getStudentsByTeachingClass(teachingClassId)
                .onSuccess { students ->
                    Log.d(TAG, "成功获取 ${students.size} 个学生")
                    _studentListState.value = StudentListState.Success(students)
                }
                .onFailure { exception ->
                    Log.e(TAG, "获取学生列表失败", exception)
                    _studentListState.value = StudentListState.Error("获取学生列表失败: ${exception.message}")
                }
        }
    }

    /**
     * 清除创建状态
     */
    fun clearCreateState() {
        _createTeachingClassState.value = CreateTeachingClassState.Idle
    }

    /**
     * 清除错误状态
     */
    fun clearError() {
        _teacherClassesState.value = TeacherClassesState.Idle
        _coursesState.value = CoursesState.Idle
        _createTeachingClassState.value = CreateTeachingClassState.Idle
        _studentListState.value = StudentListState.Idle
    }
}

sealed class TeacherClassesState {
    object Idle : TeacherClassesState()
    object Loading : TeacherClassesState()
    data class Success(val classes: List<MyCourseDTO>) : TeacherClassesState()
    data class Error(val message: String) : TeacherClassesState()
}


sealed class CreateTeachingClassState {
    object Idle : CreateTeachingClassState()
    object Loading : CreateTeachingClassState()
    data class Success(val teachingClass: TeachingClass) : CreateTeachingClassState()
    data class Error(val message: String) : CreateTeachingClassState()
}

sealed class StudentListState {
    object Idle : StudentListState()
    object Loading : StudentListState()
    data class Success(val students: List<SelectionDTO>) : StudentListState()
    data class Error(val message: String) : StudentListState()
}