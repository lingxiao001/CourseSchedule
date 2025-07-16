package com.example.coursescheduleapp.ui

import CourseScheduleAppTheme
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.example.coursescheduleapp.ui.theme.CourseScheduleAppTheme
import com.example.coursescheduleapp.ui.screens.AdminUserManagementScreen
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AdminUserManagementActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CourseScheduleAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AdminUserManagementScreen(
                        onNavigateBack = { finish() }
                    )
                }
            }
        }
    }
}