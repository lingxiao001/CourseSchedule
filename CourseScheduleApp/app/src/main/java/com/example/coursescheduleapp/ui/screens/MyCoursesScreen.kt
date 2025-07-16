package com.example.coursescheduleapp.ui.screens

import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
<<<<<<< HEAD
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Schedule
=======
import androidx.compose.material.icons.filled.*
import androidx.compose.foundation.clickable
>>>>>>> origin/branch1
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshContainer
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
<<<<<<< HEAD
import androidx.compose.ui.text.style.TextAlign
=======
import androidx.compose.ui.text.font.FontWeight
>>>>>>> origin/branch1
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.coursescheduleapp.model.MyCourseDTO
import com.example.coursescheduleapp.viewmodel.SelectionViewModel
import com.example.coursescheduleapp.viewmodel.MyCoursesState
<<<<<<< HEAD
import androidx.compose.ui.platform.LocalContext
import androidx.hilt.navigation.compose.hiltViewModel
=======
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import android.content.Context
>>>>>>> origin/branch1

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MyCoursesScreen(
    onNavigateBack: () -> Unit,
    selectionViewModel: SelectionViewModel = hiltViewModel()
) {
    val myCoursesState by selectionViewModel.myCoursesState.collectAsState()
    val state = myCoursesState
    val context = LocalContext.current
    
    // 获取实际的学生ID
    val studentId = remember {
        val sharedPrefs = context.getSharedPreferences("user", Context.MODE_PRIVATE)
        val userJson = sharedPrefs.getString("user_json", null)
        if (userJson != null) {
            com.google.gson.Gson().fromJson(userJson, com.example.coursescheduleapp.model.User::class.java)
                .studentId?.toLongOrNull() ?: 1L
        } else {
            1L
        }
    }
    
    var isRefreshing by remember { mutableStateOf(false) }
    
    val context = LocalContext.current
    LaunchedEffect(Unit) {
<<<<<<< HEAD
        selectionViewModel.loadMyCourses(studentId)
=======
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
>>>>>>> origin/branch1
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
<<<<<<< HEAD
                
                if (courses.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Schedule,
                                contentDescription = "暂无课程",
                                modifier = Modifier.size(64.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "暂无选课记录",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = "您还没有选择任何课程，\n前往选课中心选择课程吧！",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                } else {
                    var isRefreshing by remember { mutableStateOf(false) }
                    val refreshState = rememberPullToRefreshState()
                    
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(padding)
                    ) {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            item {
                                Text(
                                    text = "本学期已选课程 (${courses.size}门)",
                                    style = MaterialTheme.typography.titleMedium,
                                    modifier = Modifier.padding(bottom = 8.dp)
                                )
                            }
                            items(courses) { course ->
                                MyCourseCard(course = course)
                            }
                        }
                        
                        // Pull to refresh
                        if (refreshState.isRefreshing) {
                            LaunchedEffect(true) {
                                isRefreshing = true
                                selectionViewModel.loadMyCourses(studentId)
                                isRefreshing = false
                            }
                        }
                        
                        PullToRefreshContainer(
                            state = refreshState,
                            modifier = Modifier.align(Alignment.TopCenter)
                        )
                    }
                }
            }
            is MyCoursesState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = (state as MyCoursesState.Error).message,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = { 
                            selectionViewModel.loadMyCourses(studentId) 
                        }) {
                            Text("重试")
=======
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
>>>>>>> origin/branch1
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
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
<<<<<<< HEAD
            // 课程标题和学分
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = course.courseName,
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "课程代码: ${course.classCode}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Text(
                        text = "${course.credits}学分",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }
            
            Divider(
                modifier = Modifier.padding(vertical = 12.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
            
            // 教学班信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "教学班: ${course.classCode}",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Medium
                    )
                    Text(
                        text = "任课教师: ${course.teacherName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            
            // 选课时间和状态
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                course.selectionTime?.let { selectionTime ->
                    Text(
                        text = "选课时间: $selectionTime",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text(
                        text = "已选",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
=======
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
>>>>>>> origin/branch1
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