package com.example.coursescheduleapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coursescheduleapp.model.Classroom
import com.example.coursescheduleapp.repository.ClassroomAdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminClassroomViewModel @Inject constructor(
    private val repository: ClassroomAdminRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ClassroomUiState())
    val uiState: StateFlow<ClassroomUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _paginationState = MutableStateFlow(PaginationState())
    val paginationState: StateFlow<PaginationState> = _paginationState.asStateFlow()

    init {
        loadClassrooms()
    }

    fun loadClassrooms() {
        _uiState.value = _uiState.value.copy(isLoading = true)
        viewModelScope.launch {
            val result = repository.getClassrooms()
            result.onSuccess { list ->
                val filtered = if (_searchQuery.value.isBlank()) list else list.filter {
                    it.building.contains(_searchQuery.value, true) ||
                    it.classroomName.contains(_searchQuery.value, true)
                }
                val pageSize = _paginationState.value.pageSize
                val page = _paginationState.value.currentPage
                val paged = filtered.drop(page * pageSize).take(pageSize)
                val totalPages = if (pageSize == 0) 1 else ((filtered.size + pageSize - 1) / pageSize)
                _uiState.value = _uiState.value.copy(
                    classrooms = paged,
                    isLoading = false,
                    error = null
                )
                _paginationState.value = _paginationState.value.copy(totalElements = filtered.size.toLong(), totalPages = totalPages)
            }.onFailure {
                _uiState.value = _uiState.value.copy(isLoading = false, error = it.message)
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }
    fun onSearch() {
        _paginationState.value = _paginationState.value.copy(currentPage = 0)
        loadClassrooms()
    }
    fun onPreviousPage() {
        if (_paginationState.value.currentPage > 0) {
            _paginationState.value = _paginationState.value.copy(currentPage = _paginationState.value.currentPage - 1)
            loadClassrooms()
        }
    }
    fun onNextPage() {
        val maxPage = (_paginationState.value.totalElements - 1) / _paginationState.value.pageSize
        if (_paginationState.value.currentPage < maxPage) {
            _paginationState.value = _paginationState.value.copy(currentPage = _paginationState.value.currentPage + 1)
            loadClassrooms()
        }
    }
    fun onPageChange(page: Int) {
        _paginationState.value = _paginationState.value.copy(currentPage = page)
        loadClassrooms()
    }
    fun createClassroom(classroom: Classroom) {
        viewModelScope.launch {
            val result = repository.createClassroom(classroom)
            result.onSuccess {
                loadClassrooms()
            }.onFailure {
                _uiState.value = _uiState.value.copy(error = it.message)
            }
        }
    }
    fun updateClassroom(id: Long, classroom: Classroom) {
        viewModelScope.launch {
            val result = repository.updateClassroom(id, classroom)
            result.onSuccess {
                loadClassrooms()
            }.onFailure {
                _uiState.value = _uiState.value.copy(error = it.message)
            }
        }
    }
    fun deleteClassroom(id: Long) {
        viewModelScope.launch {
            val result = repository.deleteClassroom(id)
            result.onSuccess {
                loadClassrooms()
            }.onFailure {
                _uiState.value = _uiState.value.copy(error = it.message)
            }
        }
    }
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
}

data class ClassroomUiState(
    val classrooms: List<Classroom> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
) 