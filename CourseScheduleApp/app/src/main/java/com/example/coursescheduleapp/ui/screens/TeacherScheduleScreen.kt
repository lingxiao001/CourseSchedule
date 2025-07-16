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
import java.time.LocalTime
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherScheduleScreen(
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
                    ScheduleTable(
                        schedules = schedules,
                        modifier = Modifier.padding(padding)
                    )
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
fun ScheduleTable(
    schedules: List<ClassSchedule>,
    modifier: Modifier = Modifier
) {
    val weekDays = listOf("周一", "周二", "周三", "周四", "周五", "周六", "周日")
    val timeSlots = listOf(
        "08:00-09:35", "09:50-11:25", "11:40-12:25",
        "14:00-15:35", "15:50-17:25", "19:00-20:35", "20:50-22:25"
    )
    
    // 将课程按星期和时间段分组
    val scheduleMap = remember(schedules) {
        schedules.groupBy { it.dayOfWeek }
            .mapValues { (_, daySchedules) ->
                daySchedules.groupBy { schedule ->
                    "${schedule.startTime}-${schedule.endTime}"
                }
            }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // 表头
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(8.dp)
        ) {
            Text(
                text = "时间/星期",
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            weekDays.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )
            }
        }
        
        // 时间段行
        timeSlots.forEach { timeSlot ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                // 时间段
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = timeSlot,
                        style = MaterialTheme.typography.bodySmall,
                        textAlign = TextAlign.Center
                    )
                }
                
                // 每天的课程
                (1..7).forEach { day ->
                    val course = scheduleMap[day]?.get(timeSlot)?.firstOrNull()
                    ScheduleCell(
                        schedule = course,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

@Composable
fun ScheduleCell(
    schedule: ClassSchedule?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .background(
                color = if (schedule != null) {
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                } else {
                    Color.Transparent
                }
            )
            .border(1.dp, MaterialTheme.colorScheme.outline)
            .padding(4.dp)
            .aspectRatio(1f),
        contentAlignment = Alignment.Center
    ) {
        if (schedule != null) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = schedule.courseName,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
                Text(
                    text = schedule.classCode,
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center,
                    maxLines = 1
                )
                Text(
                    text = "${schedule.building} ${schedule.classroomName}",
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    maxLines = 2
                )
            }
        }
    }
}

@Composable
fun EmptyScheduleView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.CalendarToday,
                contentDescription = "暂无课表",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "暂无课表",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "您还没有安排任何课程",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun ErrorScheduleView(
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