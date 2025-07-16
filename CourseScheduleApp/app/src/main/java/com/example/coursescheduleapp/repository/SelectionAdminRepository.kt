package com.example.coursescheduleapp.repository

import com.example.coursescheduleapp.model.SelectionDTO
import com.example.coursescheduleapp.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import okhttp3.ResponseBody
import retrofit2.Response

@Singleton
class SelectionAdminRepository @Inject constructor(
    private val apiService: ApiService
) {

    /**
     * 获取所有选课记录
     * @return 选课记录列表结果
     */
    suspend fun getAllSelections(): Result<List<SelectionDTO>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getAllSelections()
                if (response.isSuccessful) {
                    Result.success(response.body() ?: emptyList())
                } else {
                    Result.failure(Exception("获取选课记录失败: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }

    /**
     * 删除单个选课记录
     * @param selectionId 选课记录ID
     * @return 删除结果
     */
    suspend fun deleteSelection(selectionId: Long): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.deleteSelection(selectionId)
                if (response.isSuccessful) {
                    // Always treat as success regardless of response body
                    Result.success("删除成功")
                } else {
                    Result.failure(Exception("删除失败: ${response.code()}"))
                }
            } catch (e: Exception) {
                // Handle all exceptions gracefully including JSON parsing
                when {
                    e.message?.contains("MalformedJsonException") == true -> Result.success("删除成功")
                    else -> Result.failure(e)
                }
            }
        }
    }

    /**
     * 批量删除选课记录
     * @param selectionIds 选课记录ID列表
     * @return 删除结果
     */
    suspend fun deleteSelections(selectionIds: List<Long>): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.deleteSelections(selectionIds)
                if (response.isSuccessful) {
                    // Always treat as success regardless of response body
                    Result.success("批量删除成功")
                } else {
                    Result.failure(Exception("批量删除失败: ${response.code()}"))
                }
            } catch (e: Exception) {
                // Handle all exceptions gracefully including JSON parsing and HTTP method issues
                when {
                    e.message?.contains("MalformedJsonException") == true -> Result.success("批量删除成功")
                    e.message?.contains("method 'POST' is not supported") == true -> {
                        // If POST fails, try alternative approach
                        Result.failure(Exception("服务端配置问题，请联系管理员"))
                    }
                    else -> Result.failure(e)
                }
            }
        }
    }
}