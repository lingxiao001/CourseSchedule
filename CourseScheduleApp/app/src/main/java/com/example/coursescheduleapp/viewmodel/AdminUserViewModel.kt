package com.example.coursescheduleapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coursescheduleapp.model.User
import com.example.coursescheduleapp.repository.UserCreateRequest
import com.example.coursescheduleapp.repository.UserUpdateRequest
import com.example.coursescheduleapp.repository.UserAdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminUserViewModel @Inject constructor(
    private val userAdminRepository: UserAdminRepository
) : ViewModel() {

    // UI状态
    private val _uiState = MutableStateFlow(AdminUserUiState())
    val uiState: StateFlow<AdminUserUiState> = _uiState.asStateFlow()

    // 分页状态
    private val _paginationState = MutableStateFlow(PaginationState())
    val paginationState: StateFlow<PaginationState> = _paginationState.asStateFlow()

    // 搜索状态
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    init {
        loadUsers()
    }

    fun loadUsers() {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                
                val currentPage = _paginationState.value.currentPage
                val pageSize = _paginationState.value.pageSize
                val search = _searchQuery.value.takeIf { it.isNotBlank() }
                
                val response = userAdminRepository.getUsers(
                    page = currentPage,
                    size = pageSize,
                    search = search
                )
                
                _uiState.value = _uiState.value.copy(
                    users = response.content,
                    isLoading = false
                )
                
                _paginationState.value = _paginationState.value.copy(
                    totalElements = response.totalElements,
                    totalPages = response.totalPages,
                    currentPage = response.number,
                    pageSize = response.size,
                    hasNextPage = !response.last,
                    hasPreviousPage = !response.first
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "加载用户列表失败"
                )
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onSearch() {
        _paginationState.value = _paginationState.value.copy(currentPage = 0)
        loadUsers()
    }

    fun onPageChange(page: Int) {
        _paginationState.value = _paginationState.value.copy(currentPage = page)
        loadUsers()
    }

    fun onNextPage() {
        val currentPage = _paginationState.value.currentPage
        val totalPages = _paginationState.value.totalPages
        if (currentPage < totalPages - 1) {
            _paginationState.value = _paginationState.value.copy(currentPage = currentPage + 1)
            loadUsers()
        }
    }

    fun onPreviousPage() {
        val currentPage = _paginationState.value.currentPage
        if (currentPage > 0) {
            _paginationState.value = _paginationState.value.copy(currentPage = currentPage - 1)
            loadUsers()
        }
    }

    fun createUser(userData: UserCreateRequest) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                userAdminRepository.createUser(userData)
                loadUsers() // 重新加载用户列表
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "用户创建成功"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "创建用户失败"
                )
            }
        }
    }

    fun updateUser(userId: Long, userData: UserUpdateRequest) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                userAdminRepository.updateUser(userId, userData)
                loadUsers() // 重新加载用户列表
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "用户更新成功"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "更新用户失败"
                )
            }
        }
    }

    fun deleteUser(userId: Long) {
        viewModelScope.launch {
            try {
                _uiState.value = _uiState.value.copy(isLoading = true, error = null)
                userAdminRepository.deleteUser(userId)
                loadUsers() // 重新加载用户列表
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    successMessage = "用户删除成功"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "删除用户失败"
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

// UI状态数据类
data class AdminUserUiState(
    val users: List<User> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val successMessage: String? = null
)

// 分页状态数据类
data class PaginationState(
    val currentPage: Int = 0,
    val pageSize: Int = 10,
    val totalElements: Long = 0,
    val totalPages: Int = 0,
    val hasNextPage: Boolean = false,
    val hasPreviousPage: Boolean = false
) 