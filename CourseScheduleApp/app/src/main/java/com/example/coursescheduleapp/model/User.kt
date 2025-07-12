data class User(
    val id: Long,
    val username: String,
    val realName: String,
    val role: Role,
    val roleId: Long,
    val createdAt: String? = null,
    val updatedAt: String? = null
)

enum class Role {
    student, teacher, admin
}

data class LoginRequest(
    val username: String,
    val password: String
)

data class AuthResponse(
    val user: User,
    val message: String? = null
)

data class UserCreateRequest(
    val username: String,
    val password: String,
    val realName: String,
    val role: Role
)