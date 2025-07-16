package com.example.coursescheduleapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.coursescheduleapp.viewmodel.TeacherTeachingClassViewModel
import com.example.coursescheduleapp.viewmodel.TeacherClassesState
import com.example.coursescheduleapp.model.MyCourseDTO
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import android.util.Log

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherMyClassesScreen(
    onNavigateBack: () -> Unit,
    onAddTeachingClass: () -> Unit,
    onViewStudents: (Long, String) -> Unit,
    viewModel: TeacherTeachingClassViewModel = hiltViewModel()
) {
    val teacherClassesState by viewModel.teacherClassesState.collectAsState()
    
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        try {
            val sharedPref = context.getSharedPreferences("user", Context.MODE_PRIVATE)
            val userJson = sharedPref.getString("user_json", null)
            val teacherId = if (userJson != null) {
                val user = com.google.gson.Gson().fromJson(userJson, com.example.coursescheduleapp.model.User::class.java)
                user.teacherId
            } else {
                1L
            }
            Log.d("TeacherMyClassesScreen", "Loading teacher classes for userId: $teacherId")
            viewModel.loadTeacherTeachingClasses(teacherId)
        } catch (e: Exception) {
            Log.e("TeacherMyClassesScreen", "Error loading teacher classes", e)
            viewModel.loadTeacherTeachingClasses(1L)
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
                actions = {
                    IconButton(onClick = onAddTeachingClass) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "添加教学班"
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
        when (teacherClassesState) {
            is TeacherClassesState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is TeacherClassesState.Success -> {
                val classes = (teacherClassesState as TeacherClassesState.Success).classes
                if (classes.isEmpty()) {
                    EmptyClassesView(
                        onAddClass = onAddTeachingClass,
                        modifier = Modifier.padding(padding)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(classes) { teachingClass ->
                            TeachingClassCard(
                                teachingClass = teachingClass,
                                onViewStudents = { 
                                    onViewStudents(teachingClass.courseId, teachingClass.courseName)
                                }
                            )
                        }
                    }
                }
            }
            is TeacherClassesState.Error -> {
                ErrorClassesView(
                    message = (teacherClassesState as TeacherClassesState.Error).message,
                    onRetry = {
                        val sharedPref = context.getSharedPreferences("user", Context.MODE_PRIVATE)
                        val userJson = sharedPref.getString("user_json", null)
                        val userId = if (userJson != null) {
                            val user = com.google.gson.Gson().fromJson(userJson, com.example.coursescheduleapp.model.User::class.java)
                            user.userId
                        } else {
                            1L
                        }
                        viewModel.loadTeacherTeachingClasses(userId)
                    },
                    modifier = Modifier.padding(padding)
                )
            }
            else -> {}
        }
    }
}

@Composable
fun TeachingClassCard(
    teachingClass: MyCourseDTO,
    onViewStudents: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onViewStudents() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(
                text = teachingClass.courseName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "教学班: ${teachingClass.classCode}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "学分: ${teachingClass.credits}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Text(
                        text = "教师: ${teachingClass.teacherName}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    // 这里可以添加学生人数，但需要扩展MyCourseDTO
                    IconButton(
                        onClick = onViewStudents,
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.People,
                            contentDescription = "查看学生",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun EmptyClassesView(
    onAddClass: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
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
            Button(
                onClick = onAddClass,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "添加",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("添加教学班")
            }
        }
    }
}

@Composable
fun ErrorClassesView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
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