package com.example.coursescheduleapp.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.coursescheduleapp.model.SelectionDTO
import com.example.coursescheduleapp.repository.SelectionAdminRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AdminSelectionViewModel @Inject constructor(
    private val selectionAdminRepository: SelectionAdminRepository
) : ViewModel() {

    private val _selectionState = MutableStateFlow<AdminSelectionState>(AdminSelectionState.Idle)
    val selectionState: StateFlow<AdminSelectionState> = _selectionState.asStateFlow()

    companion object {
        private const val TAG = "AdminSelectionViewModel"
    }

    /**
     * 加载所有选课记录
     */
    fun loadAllSelections() {
        Log.d(TAG, "开始加载所有选课记录")
        _selectionState.value = AdminSelectionState.Loading
        viewModelScope.launch {
            selectionAdminRepository.getAllSelections()
                .onSuccess { selections ->
                    Log.d(TAG, "成功获取 ${selections.size} 条选课记录")
                    _selectionState.value = AdminSelectionState.Success(selections)
                }
                .onFailure { exception ->
                    Log.e(TAG, "获取选课记录失败", exception)
                    _selectionState.value = AdminSelectionState.Error("获取选课记录失败: ${exception.message}")
                }
        }
    }

    /**
     * 删除单个选课记录
     * @param selectionId 选课记录ID
     */
    fun deleteSelection(selectionId: Long) {
        Log.d(TAG, "开始删除选课记录: $selectionId")
        viewModelScope.launch {
            selectionAdminRepository.deleteSelection(selectionId)
                .onSuccess { message ->
                    Log.d(TAG, "删除成功: $message")
                    // 重新加载列表
                    loadAllSelections()
                }
                .onFailure { exception ->
                    Log.e(TAG, "删除失败", exception)
                    _selectionState.value = AdminSelectionState.Error("删除失败: ${exception.message}")
                }
        }
    }

    /**
     * 批量删除选课记录
     * @param selectionIds 选课记录ID列表
     */
    fun deleteSelections(selectionIds: List<Long>) {
        Log.d(TAG, "开始批量删除选课记录: ${selectionIds.size} 条")
        viewModelScope.launch {
            selectionAdminRepository.deleteSelections(selectionIds)
                .onSuccess { message ->
                    Log.d(TAG, "批量删除成功: $message")
                    // 重新加载列表
                    loadAllSelections()
                }
                .onFailure { exception ->
                    Log.e(TAG, "批量删除失败", exception)
                    _selectionState.value = AdminSelectionState.Error("批量删除失败: ${exception.message}")
                }
        }
    }

    /**
     * 清除错误状态
     */
    fun clearError() {
        _selectionState.value = AdminSelectionState.Idle
    }
}

sealed class AdminSelectionState {
    object Idle : AdminSelectionState()
    object Loading : AdminSelectionState()
    data class Success(val selections: List<SelectionDTO>) : AdminSelectionState()
    data class Error(val message: String) : AdminSelectionState()
}