package com.example.coursescheduleapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coursescheduleapp.model.Course
import com.example.coursescheduleapp.repository.CourseAdminRepository
import com.example.coursescheduleapp.repository.CourseCreateRequest
import com.example.coursescheduleapp.repository.CourseUpdateRequest
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminCourseViewModel @Inject constructor(
    private val courseRepository: CourseAdminRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AdminCourseUiState())
    val uiState: StateFlow<AdminCourseUiState> = _uiState.asStateFlow()

    // 分页相关状态
    private val _paginationState = MutableStateFlow(PaginationState())
    val paginationState: StateFlow<PaginationState> = _paginationState.asStateFlow()

    // 搜索相关状态
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadCourses()
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onSearch() {
        _paginationState.value = _paginationState.value.copy(currentPage = 0)
        loadCourses()
    }

    fun onPreviousPage() {
        if (_paginationState.value.currentPage > 0) {
            _paginationState.value = _paginationState.value.copy(currentPage = _paginationState.value.currentPage - 1)
            loadCourses()
        }
    }

    fun onNextPage() {
        val maxPage = (_paginationState.value.totalElements - 1) / _paginationState.value.pageSize
        if (_paginationState.value.currentPage < maxPage) {
            _paginationState.value = _paginationState.value.copy(currentPage = _paginationState.value.currentPage + 1)
            loadCourses()
        }
    }

    fun onPageChange(page: Int) {
        _paginationState.value = _paginationState.value.copy(currentPage = page)
        loadCourses()
    }

    fun loadCourses() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            val list = courseRepository.getAllCourses()
            val filtered = if (_searchQuery.value.isBlank()) list else list.filter {
                it.courseName.contains(_searchQuery.value, true) ||
                it.classCode.contains(_searchQuery.value, true)
            }
            val pageSize = _paginationState.value.pageSize
            val page = _paginationState.value.currentPage
            val paged = filtered.drop(page * pageSize).take(pageSize)
            val totalPages = if (pageSize == 0) 1 else ((filtered.size + pageSize - 1) / pageSize)
            _uiState.value = _uiState.value.copy(
                courses = paged,
                isLoading = false,
                error = null
            )
            _paginationState.value = _paginationState.value.copy(totalElements = filtered.size.toLong(), totalPages = totalPages)
        }
    }

    fun createCourse(courseRequest: CourseCreateRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                courseRepository.createCourse(courseRequest)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "课程创建成功"
                )
                loadCourses() // 重新加载列表
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "创建课程失败"
                )
            }
        }
    }

    fun updateCourse(id: Long, courseRequest: CourseUpdateRequest) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                courseRepository.updateCourse(id, courseRequest)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "课程更新成功"
                )
                loadCourses() // 重新加载列表
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "更新课程失败"
                )
            }
        }
    }

    fun deleteCourse(id: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                courseRepository.deleteCourse(id)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "课程删除成功"
                )
                loadCourses() // 重新加载列表
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "删除课程失败"
                )
            }
        }
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSuccessMessage() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }
}

data class AdminCourseUiState(
    val courses: List<Course> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
) 