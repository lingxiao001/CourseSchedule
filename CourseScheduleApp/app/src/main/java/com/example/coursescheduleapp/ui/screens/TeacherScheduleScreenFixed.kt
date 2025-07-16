package com.example.coursescheduleapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.coursescheduleapp.model.ClassSchedule
import com.example.coursescheduleapp.viewmodel.ScheduleViewModel
import com.example.coursescheduleapp.viewmodel.ScheduleState
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import android.content.Context

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherScheduleScreenFixed(
    onNavigateBack: () -> Unit,
    scheduleViewModel: ScheduleViewModel = hiltViewModel()
) {
    val scheduleState by scheduleViewModel.scheduleState.collectAsState()
    
    val context = LocalContext.current
    LaunchedEffect(Unit) {
        try {
            val sharedPref = context.getSharedPreferences("user", Context.MODE_PRIVATE)
            val userJson = sharedPref.getString("user_json", null)
            val userId = if (userJson != null) {
                val user = com.google.gson.Gson().fromJson(userJson, com.example.coursescheduleapp.model.User::class.java)
                user.userId
            } else {
                1L
            }
            Log.d("TeacherScheduleScreen", "Loading teacher schedule for userId: $userId")
            scheduleViewModel.loadSchedulesByTeacher(userId)
        } catch (e: Exception) {
            Log.e("TeacherScheduleScreen", "Error loading teacher schedule", e)
            scheduleViewModel.loadSchedulesByTeacher(1L)
        }
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的课表") },
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
        when (scheduleState) {
            is ScheduleState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is ScheduleState.Success -> {
                val schedules = (scheduleState as ScheduleState.Success).schedules
                if (schedules.isEmpty()) {
                    EmptyScheduleView()
                } else {
                    Column(
                        modifier = Modifier.padding(padding)
                    ) {
                        // 显示调试信息
                        Text(
                            text = "共找到 ${schedules.size} 门课程安排",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium
                        )
                        
                        // 显示原始课程列表
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            items(schedules) { schedule ->
                                ScheduleItemCard(schedule = schedule)
                            }
                        }
                    }
                }
            }
            is ScheduleState.Error -> {
                ErrorScheduleView(
                    message = (scheduleState as ScheduleState.Error).message,
                    onRetry = { 
                        val sharedPref = context.getSharedPreferences("user", Context.MODE_PRIVATE)
                        val userJson = sharedPref.getString("user_json", null)
                        val userId = if (userJson != null) {
                            val user = com.google.gson.Gson().fromJson(userJson, com.example.coursescheduleapp.model.User::class.java)
                            user.userId
                        } else {
                            1L
                        }
                        scheduleViewModel.loadSchedulesByTeacher(userId) 
                    }
                )
            }
            else -> {}
        }
    }
}

@Composable
fun ScheduleItemCard(schedule: ClassSchedule) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // 课程信息
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = schedule.courseName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "教学班: ${schedule.classCode}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                
                // 星期和时间
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "星期${schedule.dayOfWeek}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${schedule.startTime} - ${schedule.endTime}",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 教室信息
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = "教室",
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${schedule.building} ${schedule.classroomName}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }
    }
}
//
//@Composable
//fun EmptyScheduleView() {
//    Box(
//        modifier = Modifier.fillMaxSize(),
//        contentAlignment = Alignment.Center
//    ) {
//        Column(
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            Icon(
//                imageVector = Icons.Default.CalendarToday,
//                contentDescription = "暂无课表",
//                modifier = Modifier.size(64.dp),
//                tint = MaterialTheme.colorScheme.onSurfaceVariant
//            )
//            Text(
//                text = "暂无课表",
//                style = MaterialTheme.typography.headlineSmall,
//                color = MaterialTheme.colorScheme.onSurfaceVariant
//            )
//            Text(
//                text = "您还没有安排任何课程，请联系管理员进行排课",
//                style = MaterialTheme.typography.bodyMedium,
//                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
//            )
//        }
//    }
//}
//
//@Composable
//fun ErrorScheduleView(
//    message: String,
//    onRetry: () -> Unit
//) {
//    Box(
//        modifier = Modifier.fillMaxSize(),
//        contentAlignment = Alignment.Center
//    ) {
//        Column(
//            horizontalAlignment = Alignment.CenterHorizontally,
//            verticalArrangement = Arrangement.spacedBy(16.dp)
//        ) {
//            Icon(
//                imageVector = Icons.Default.Error,
//                contentDescription = "错误",
//                modifier = Modifier.size(64.dp),
//                tint = MaterialTheme.colorScheme.error
//            )
//            Text(
//                text = message,
//                style = MaterialTheme.typography.bodyMedium,
//                color = MaterialTheme.colorScheme.error
//            )
//            Button(
//                onClick = onRetry,
//                colors = ButtonDefaults.buttonColors(
//                    containerColor = MaterialTheme.colorScheme.primary
//                )
//            ) {
//                Icon(
//                    imageVector = Icons.Default.Refresh,
//                    contentDescription = "重试",
//                    modifier = Modifier.size(16.dp)
//                )
//                Spacer(modifier = Modifier.width(8.dp))
//                Text("重试")
//            }
//        }
//    }
//}