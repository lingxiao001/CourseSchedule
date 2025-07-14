package com.example.coursescheduleapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coursescheduleapp.model.AuthResponse
import com.example.coursescheduleapp.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.content.Context
import com.google.gson.Gson
import com.example.coursescheduleapp.model.User

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
    
    fun register(
        username: String,
        password: String,
        realName: String,
        role: String,
        studentId: Long? = null,
        grade: String? = null,
        className: String? = null,
        teacherId: Long? = null,
        title: String? = null,
        department: String? = null
    ) {
        _authState.value = AuthState.Loading
        viewModelScope.launch {
            authRepository.register(
                username, password, realName, role,
                studentId, grade, className, teacherId, title, department
            )
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

    fun setCurrentUser(authResponse: AuthResponse) {
        _currentUser.value = authResponse
    }

    fun saveUserToPrefs(context: Context, user: User?) {
        if (user == null) return
        val userJson = Gson().toJson(user)
        context.getSharedPreferences("user", Context.MODE_PRIVATE)
            .edit().putString("user_json", userJson).apply()
    }
}

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Success(val response: AuthResponse) : AuthState()
    data class Error(val message: String) : AuthState()
} 