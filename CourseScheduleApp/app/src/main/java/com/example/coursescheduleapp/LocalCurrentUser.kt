package com.example.coursescheduleapp

import androidx.compose.runtime.staticCompositionLocalOf
import com.example.coursescheduleapp.model.AuthResponse

val LocalCurrentUser = staticCompositionLocalOf<AuthResponse?> { null } 