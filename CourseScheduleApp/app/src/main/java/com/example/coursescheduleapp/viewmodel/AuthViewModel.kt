package com.example.coursescheduleapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coursescheduleapp.model.AuthResponse
import com.example.coursescheduleapp.model.Role
import com.example.coursescheduleapp.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {
    
    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()
    
    private val _currentUser = MutableStateFlow<AuthResponse?>(null)
    val currentUser: StateFlow<AuthResponse?> = _currentUser.asStateFlow()
    
    fun login(username: String, password: String) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            authRepository.login(username, password)
                .onSuccess { response ->
                    _currentUser.value = response
                    _authState.value = AuthState.Success(response)
                }
                .onFailure { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "登录失败")
                }
        }
    }
    
    fun register(username: String, password: String, realName: String, role: Role) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            authRepository.register(username, password, realName, role)
                .onSuccess { response ->
                    _currentUser.value = response
                    _authState.value = AuthState.Success(response)
                }
                .onFailure { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "注册失败")
                }
        }
    }
    
    fun getCurrentUser() {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            authRepository.getCurrentUser()
                .onSuccess { response ->
                    _currentUser.value = response
                    _authState.value = AuthState.Success(response)
                }
                .onFailure { exception ->
                    _authState.value = AuthState.Error(exception.message ?: "获取用户信息失败")
                }
        }
    }
    
    fun logout() {
        _currentUser.value = null
        _authState.value = AuthState.Idle
    }
    
    fun clearError() {
        _authState.value = AuthState.Idle
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val response: AuthResponse) : AuthState()
    data class Error(val message: String) : AuthState()
} 