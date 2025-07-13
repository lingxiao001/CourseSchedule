package com.example.coursescheduleapp.repository

import com.example.coursescheduleapp.model.ResetPasswordDTO
import com.example.coursescheduleapp.network.ApiService
import javax.inject.Inject

class ResetPasswordRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun resetPassword(username: String, oldPwd: String, newPwd: String): Result<Unit> {
        return try {
            val resp = apiService.resetPassword(
                ResetPasswordDTO(username, oldPwd, newPwd)
            )
            if (resp.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(Exception("重置失败，原密码错误或网络异常"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
} 