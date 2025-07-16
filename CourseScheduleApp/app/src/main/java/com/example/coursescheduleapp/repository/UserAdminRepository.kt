package com.example.coursescheduleapp.repository

import com.example.coursescheduleapp.model.User
import com.example.coursescheduleapp.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UserAdminRepository @Inject constructor(
    private val apiService: ApiService
) {
    suspend fun getUsers(
        page: Int = 0,
        size: Int = 10,
        search: String? = null
    ): UserListResponse = withContext(Dispatchers.IO) {
        val response = apiService.getUsers(page, size, search)
        if (response.isSuccessful) {
            response.body()!!
        } else {
            throw Exception("获取用户列表失败: ${response.code()}")
        }
    }

    suspend fun createUser(user: UserCreateRequest): User = withContext(Dispatchers.IO) {
        val response = apiService.createUser(user)
        if (response.isSuccessful) {
            response.body()!!
        } else {
            throw Exception("创建用户失败: ${response.code()}")
        }
    }

    suspend fun updateUser(userId: Long, user: UserUpdateRequest): User = withContext(Dispatchers.IO) {
        val response = apiService.updateUser(userId, user)
        if (response.isSuccessful) {
            response.body()!!
        } else {
            throw Exception("更新用户失败: ${response.code()}")
        }
    }

    suspend fun deleteUser(userId: Long) = withContext(Dispatchers.IO) {
        val response = apiService.deleteUser(userId)
        if (!response.isSuccessful) {
            throw Exception("删除用户失败: ${response.code()}")
        }
    }
}

// 分页响应数据类
data class UserListResponse(
    val content: List<User>,
    val totalElements: Long,
    val totalPages: Int,
    val size: Int,
    val number: Int,
    val first: Boolean,
    val last: Boolean
)

// 创建用户请求
data class UserCreateRequest(
    val username: String,
    val password: String,
    val realName: String,
    val role: String,
    val studentId: String? = null,
    val teacherId: String? = null,
    val grade: String? = null,
    val className: String? = null,
    val title: String? = null,
    val department: String? = null
)

// 更新用户请求
data class UserUpdateRequest(
    val realName: String? = null,
    val role: String? = null,
    val newPassword: String? = null,
    val grade: String? = null,
    val className: String? = null,
    val title: String? = null,
    val department: String? = null
) 