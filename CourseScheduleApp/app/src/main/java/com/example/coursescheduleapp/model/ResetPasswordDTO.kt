package com.example.coursescheduleapp.model
 
data class ResetPasswordDTO(
    val username: String,
    val oldPassword: String,
    val newPassword: String
) 