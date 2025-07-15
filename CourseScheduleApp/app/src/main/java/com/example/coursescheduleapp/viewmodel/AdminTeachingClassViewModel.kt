package com.example.coursescheduleapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coursescheduleapp.model.TeachingClass
import com.example.coursescheduleapp.repository.TeachingClassAdminRepository
import com.example.coursescheduleapp.repository.UserAdminRepository
import com.example.coursescheduleapp.repository.CourseRepository
import com.example.coursescheduleapp.model.User
import com.example.coursescheduleapp.model.Course
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminTeachingClassViewModel @Inject constructor(
    private val repository: TeachingClassAdminRepository,
    private val userAdminRepository: UserAdminRepository,
    private val courseRepository: CourseRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(TeachingClassUiState())
    val uiState: StateFlow<TeachingClassUiState> = _uiState.asStateFlow()

    private val _allTeachers = MutableStateFlow<List<User>>(emptyList())
    val allTeachers: StateFlow<List<User>> = _allTeachers.asStateFlow()

    private val _allCourses = MutableStateFlow<List<Course>>(emptyList())
    val allCourses: StateFlow<List<Course>> = _allCourses.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _paginationState = MutableStateFlow(PaginationState())
    val paginationState: StateFlow<PaginationState> = _paginationState.asStateFlow()

    init {
        loadTeachingClasses()
        loadAllTeachers()
        loadAllCourses()
    }

    fun loadTeachingClasses() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            val result = repository.getTeachingClasses()
            result.onSuccess { list ->
                val filtered = if (_searchQuery.value.isBlank()) list else list.filter {
                    it.classCode.contains(_searchQuery.value, true)
                }
                val pageSize = _paginationState.value.pageSize
                val page = _paginationState.value.currentPage
                val paged = filtered.drop(page * pageSize).take(pageSize)
                val totalPages = if (pageSize == 0) 1 else ((filtered.size + pageSize - 1) / pageSize)
                _uiState.value = _uiState.value.copy(
                    teachingClasses = paged,
                    isLoading = false,
                    error = null
                )
                _paginationState.value = _paginationState.value.copy(totalElements = filtered.size.toLong(), totalPages = totalPages)
            }.onFailure {
                _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
            }
        }
    }

    fun loadAllTeachers() {
        viewModelScope.launch {
            try {
                val users = userAdminRepository.getUsers(0, 1000).content.filter { it.role == "teacher" }
                _allTeachers.value = users
            } catch (e: Exception) {
                _allTeachers.value = emptyList()
            }
        }
    }

    fun loadAllCourses() {
        viewModelScope.launch {
            try {
                val result = courseRepository.getAllCourses()
                result.onSuccess { _allCourses.value = it }
                    .onFailure { _allCourses.value = emptyList() }
            } catch (e: Exception) {
                _allCourses.value = emptyList()
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }
    fun onSearch() {
        _paginationState.value = _paginationState.value.copy(currentPage = 0)
        loadTeachingClasses()
    }
    fun onPreviousPage() {
        if (_paginationState.value.currentPage > 0) {
            _paginationState.value = _paginationState.value.copy(currentPage = _paginationState.value.currentPage - 1)
            loadTeachingClasses()
        }
    }
    fun onNextPage() {
        val maxPage = (_paginationState.value.totalElements - 1) / _paginationState.value.pageSize
        if (_paginationState.value.currentPage < maxPage) {
            _paginationState.value = _paginationState.value.copy(currentPage = _paginationState.value.currentPage + 1)
            loadTeachingClasses()
        }
    }
    fun onPageChange(page: Int) {
        _paginationState.value = _paginationState.value.copy(currentPage = page)
        loadTeachingClasses()
    }
    fun createTeachingClass(courseId: Long, teachingClass: TeachingClass) {
        viewModelScope.launch {
            val result = repository.createTeachingClass(courseId, teachingClass)
            result.onSuccess {
                loadTeachingClasses()
            }.onFailure {
                _uiState.value = _uiState.value.copy(error = it.message)
            }
        }
    }
    fun updateTeachingClass(classId: Long, teachingClass: TeachingClass) {
        viewModelScope.launch {
            val result = repository.updateTeachingClass(classId, teachingClass)
            result.onSuccess {
                loadTeachingClasses()
            }.onFailure {
                _uiState.value = _uiState.value.copy(error = it.message)
            }
        }
    }
    fun deleteTeachingClass(classId: Long) {
        viewModelScope.launch {
            val result = repository.deleteTeachingClass(classId)
            result.onSuccess {
                loadTeachingClasses()
            }.onFailure {
                _uiState.value = _uiState.value.copy(error = it.message)
            }
        }
    }
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class TeachingClassUiState(
    val teachingClasses: List<TeachingClass> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) 