package com.example.coursescheduleapp.repository

import com.example.coursescheduleapp.model.*
import com.example.coursescheduleapp.network.ApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton
import com.google.gson.Gson
import okhttp3.ResponseBody
import org.json.JSONObject

@Singleton
class AuthRepository @Inject constructor(
    private val apiService: ApiService
) {
    
    suspend fun login(username: String, password: String): Result<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.login(LoginRequest(username, password))
                if (response.isSuccessful) {
                    val rawJson = response.body()?.string()
                    println("DEBUG: 登录返回原始JSON = $rawJson")
                    if (rawJson != null) {
                        val json = JSONObject(rawJson)
                        val userId = json.optLong("userId")
                        val usernameField = json.optString("username")
                        val realName = if (json.has("realName")) json.optString("realName") else json.optString("real_name")
                        val role = json.optString("role")
                        val studentId = json.optLong("studentId")
                        val teacherId = json.optLong("teacherId")
                        val grade = if (json.has("grade")) json.optString("grade") else null
                        val className = if (json.has("className")) json.optString("className") else null
                        val title = if (json.has("title")) json.optString("title") else null
                        val department = if (json.has("department")) json.optString("department") else null
                        val createdAt = if (json.has("createdAt")) json.optString("createdAt") else null
                        val user = User(
                            userId = userId,
                            username = usernameField,
                            realName = realName,
                            role = role,
                            studentId = studentId,
                            teacherId = teacherId,
                            grade = grade,
                            className = className,
                            title = title,
                            department = department,
                            createdAt = createdAt
                        )
                        Result.success(AuthResponse(user = user))
                    } else {
                        Result.failure(Exception("后端返回内容为空"))
                    }
                } else {
                    if (response.code() == 401) {
                        Result.failure(Exception("账号或密码错误"))
                    } else {
                        Result.failure(Exception("登录失败: ${response.code()}"))
                    }
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun register(
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
    ): Result<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val request = UserCreateRequest(
                    username = username,
                    password = password,
                    realName = realName,
                    role = role,
                    studentId = if (role == "student") studentId?.toString() else null,
                    grade = if (role == "student") grade else null,
                    className = if (role == "student") className else null,
                    teacherId = if (role == "teacher") teacherId?.toString() else null,
                    title = if (role == "teacher") title else null,
                    department = if (role == "teacher") department else null
                )
                val response = apiService.register(request)
                if (response.isSuccessful) {
                    val rawJson = response.body()?.string()
                    println("DEBUG: 注册返回原始JSON = $rawJson")
                    if (rawJson != null) {
                        val json = JSONObject(rawJson)
                        val userId = json.optLong("userId")
                        val usernameField = json.optString("username")
                        val realNameField = if (json.has("realName")) json.optString("realName") else json.optString("real_name")
                        val roleField = json.optString("role")
                        val studentId = json.optLong("studentId")
                        val teacherId = json.optLong("teacherId")
                        val grade = if (json.has("grade")) json.optString("grade") else null
                        val className = if (json.has("className")) json.optString("className") else null
                        val title = if (json.has("title")) json.optString("title") else null
                        val department = if (json.has("department")) json.optString("department") else null
                        val createdAt = if (json.has("createdAt")) json.optString("createdAt") else null
                        val user = User(
                            userId = userId,
                            username = usernameField,
                            realName = realNameField,
                            role = roleField,
                            studentId = studentId,
                            teacherId = teacherId,
                            grade = grade,
                            className = className,
                            title = title,
                            department = department,
                            createdAt = createdAt
                        )
                        Result.success(AuthResponse(user = user))
                    } else {
                        Result.failure(Exception("后端返回内容为空"))
                    }
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "注册失败: ${response.code()}"
                    Result.failure(Exception(errorMsg))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
    
    suspend fun getCurrentUser(): Result<AuthResponse> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getCurrentUser()
                if (response.isSuccessful) {
                    val rawJson = response.body()?.string()
                    println("DEBUG: 获取当前用户原始JSON = $rawJson")
                    if (rawJson != null) {
                        val json = JSONObject(rawJson)
                        val userId = json.optLong("userId")
                        val usernameField = json.optString("username")
                        val realName = if (json.has("realName")) json.optString("realName") else json.optString("real_name")
                        val role = json.optString("role")
                        val studentId = json.optLong("studentId")
                        val teacherId = json.optLong("teacherId")
                        val grade = if (json.has("grade")) json.optString("grade") else null
                        val className = if (json.has("className")) json.optString("className") else null
                        val title = if (json.has("title")) json.optString("title") else null
                        val department = if (json.has("department")) json.optString("department") else null
                        val createdAt = if (json.has("createdAt")) json.optString("createdAt") else null
                        val user = User(
                            userId = userId,
                            username = usernameField,
                            realName = realName,
                            role = role,
                            studentId = studentId,
                            teacherId = teacherId,
                            grade = grade,
                            className = className,
                            title = title,
                            department = department,
                            createdAt = createdAt
                        )
                        Result.success(AuthResponse(user = user))
                    } else {
                        Result.failure(Exception("后端返回内容为空"))
                    }
                } else {
                    Result.failure(Exception("获取用户信息失败: ${response.code()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }
} 