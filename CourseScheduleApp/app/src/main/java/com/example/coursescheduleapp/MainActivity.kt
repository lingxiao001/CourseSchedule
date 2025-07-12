package com.example.coursescheduleapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.coursescheduleapp.model.Role
import com.example.coursescheduleapp.ui.screens.*
import com.example.coursescheduleapp.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.ui.unit.dp
import com.example.coursescheduleapp.ui.theme.CourseScheduleAppTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val authViewModel: AuthViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CourseScheduleAppTheme {
                MainScreen(authViewModel)
            }
        }
    }
}

@Composable
fun MainScreen(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val currentUser by authViewModel.currentUser.collectAsState()
    
    Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("home") {
                HomeScreen(
                    currentUser = currentUser,
                    onNavigateToCourses = { navController.navigate("courses") },
                    onNavigateToMyCourses = { navController.navigate("my-courses") },
                    onNavigateToSchedule = { navController.navigate("schedule") },
                    onLogout = {
                        authViewModel.logout()
                        // 这里应该跳转到登录界面
                    }
                )
            }
            
            composable("courses") {
                CoursesScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            composable("my-courses") {
                MyCoursesScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            composable("schedule") {
                ScheduleScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun HomeScreen(
    currentUser: com.example.coursescheduleapp.model.AuthResponse?,
    onNavigateToCourses: () -> Unit,
    onNavigateToMyCourses: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "欢迎，${currentUser?.user?.realName ?: "用户"}！",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        when (currentUser?.user?.role) {
            Role.student -> {
                StudentHomeContent(
                    onNavigateToCourses = onNavigateToCourses,
                    onNavigateToMyCourses = onNavigateToMyCourses,
                    onNavigateToSchedule = onNavigateToSchedule,
                    onLogout = onLogout
                )
            }
            Role.teacher -> {
                TeacherHomeContent(
                    onNavigateToMyCourses = onNavigateToMyCourses,
                    onNavigateToSchedule = onNavigateToSchedule,
                    onLogout = onLogout
                )
            }
            Role.admin -> {
                AdminHomeContent(
                    onNavigateToCourses = onNavigateToCourses,
                    onLogout = onLogout
                )
            }
            else -> {
                Text("未知用户角色")
            }
        }
    }
}

@Composable
fun StudentHomeContent(
    onNavigateToCourses: () -> Unit,
    onNavigateToMyCourses: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = onNavigateToCourses,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("选课系统")
        }
        
        Button(
            onClick = onNavigateToMyCourses,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("我的课程")
        }
        
        Button(
            onClick = onNavigateToSchedule,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("我的课表")
        }
        
        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("退出登录")
        }
    }
}

@Composable
fun TeacherHomeContent(
    onNavigateToMyCourses: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = onNavigateToMyCourses,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("我的教学班")
        }
        
        Button(
            onClick = onNavigateToSchedule,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("我的课表")
        }
        
        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("退出登录")
        }
    }
}

@Composable
fun AdminHomeContent(
    onNavigateToCourses: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = onNavigateToCourses,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("课程管理")
        }
        
        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("退出登录")
        }
    }
}