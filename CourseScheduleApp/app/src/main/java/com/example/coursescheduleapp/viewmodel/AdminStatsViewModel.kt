package com.example.coursescheduleapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coursescheduleapp.network.ApiService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminStatsViewModel @Inject constructor(
    private val apiService: ApiService
) : ViewModel() {

    private val _statsState = MutableStateFlow<StatsState>(StatsState.Loading)
    val statsState: StateFlow<StatsState> = _statsState.asStateFlow()

    init {
        loadStats()
    }

    fun loadStats() {
        _statsState.value = StatsState.Loading
        viewModelScope.launch {
            try {
                val response = apiService.getStats()
                if (response.isSuccessful) {
                    val stats = response.body() ?: emptyMap()
                    _statsState.value = StatsState.Success(stats)
                } else {
                    _statsState.value = StatsState.Error("获取统计失败: ${response.code()}")
                }
            } catch (e: Exception) {
                _statsState.value = StatsState.Error(e.message ?: "网络请求失败")
            }
        }
    }
}

sealed class StatsState {
    object Loading : StatsState()
    data class Success(val stats: Map<String, Long>) : StatsState()
    data class Error(val message: String) : StatsState()
} 