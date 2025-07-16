package com.example.coursescheduleapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.coursescheduleapp.model.MyCourseDTO
import com.example.coursescheduleapp.viewmodel.SelectionViewModel
import com.example.coursescheduleapp.viewmodel.MyCoursesState
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import android.content.Context

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyCoursesScreen(
    onNavigateBack: () -> Unit,
    selectionViewModel: SelectionViewModel = hiltViewModel()
) {
    val myCoursesState by selectionViewModel.myCoursesState.collectAsState()
    val state = myCoursesState
    
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        try {
            // 从SharedPreferences获取当前用户ID
            val sharedPref = context.getSharedPreferences("user", Context.MODE_PRIVATE)
            val userJson = sharedPref.getString("user_json", null)
            val userId = if (userJson != null) {
                val user = com.google.gson.Gson().fromJson(userJson, com.example.coursescheduleapp.model.User::class.java)
                user.userId
            } else {
                1L // 后备方案
            }
            Log.d("MyCoursesScreen", "Loading teacher courses for userId: $userId")
            selectionViewModel.loadMyCourses(userId, isTeacher = true)
        } catch (e: Exception) {
            Log.e("MyCoursesScreen", "Error loading teacher courses", e)
            // 使用后备方案
            selectionViewModel.loadMyCourses(1L, isTeacher = true)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的教学班") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer
                )
            )
        }
    ) { padding ->
        when (state) {
            is MyCoursesState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is MyCoursesState.Success -> {
                val courses = (state as MyCoursesState.Success).courses
                if (courses.isEmpty()) {
                    EmptyTeachingClassesView()
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(courses) { course ->
                            SimpleTeachingClassCard(course = course)
                        }
                    }
                }
            }
            is MyCoursesState.Error -> {
                ErrorTeachingClassesView(
                    message = (state as MyCoursesState.Error).message,
                    onRetry = { 
                        val sharedPref = context.getSharedPreferences("user", Context.MODE_PRIVATE)
                        val userJson = sharedPref.getString("user_json", null)
                        val userId = if (userJson != null) {
                            val user = com.google.gson.Gson().fromJson(userJson, com.example.coursescheduleapp.model.User::class.java)
                            user.userId
                        } else {
                            1L
                        }
                        selectionViewModel.loadMyCourses(userId, isTeacher = true) 
                    }
                )
            }
            else -> {}
        }
    }
}

@Composable
fun SimpleTeachingClassCard(course: MyCourseDTO) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = course.courseName ?: "未知课程",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "教学班代码: ${course.classCode ?: "未知代码"}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "教师: ${course.teacherName ?: "未知教师"}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            Text(
                text = "学分: ${course.credit ?: 0.0}",
                style = MaterialTheme.typography.bodyMedium
            )
            
            course.selectionTime?.let { selectionTime ->
                Text(
                    text = "创建时间: $selectionTime",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        }
    }
}

@Composable
fun EmptyTeachingClassesView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.School,
                contentDescription = "暂无教学班",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "暂无教学班",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "您还没有创建任何教学班",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun ErrorTeachingClassesView(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Error,
                contentDescription = "错误",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.error
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error
            )
            Button(
                onClick = onRetry,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "重试",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("重试")
            }
        }
    }
} 