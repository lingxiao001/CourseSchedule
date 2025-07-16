package com.example.coursescheduleapp.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.coursescheduleapp.model.ClassSchedule
import com.example.coursescheduleapp.viewmodel.ScheduleViewModel
import com.example.coursescheduleapp.viewmodel.ScheduleState
import android.util.Log
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import com.example.coursescheduleapp.model.Teacher

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeacherScheduleScreen(
    onNavigateBack: () -> Unit,
    scheduleViewModel: ScheduleViewModel = hiltViewModel()
) {
    val scheduleState by scheduleViewModel.scheduleState.collectAsState()
    val selectedSchedule = scheduleViewModel.selectedSchedule.collectAsState().value
    val showDialog = scheduleViewModel.showDialog.collectAsState().value
    val selectedTeachingClass = scheduleViewModel.selectedTeachingClass.collectAsState().value
    
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
            Log.d("TeacherScheduleScreen", "Loading teacher schedule for teacherId: $teacherId")
            scheduleViewModel.loadSchedulesByTeacher(teacherId)
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
                    TeacherScheduleTable(
                        schedules = schedules,
                        modifier = Modifier.padding(padding),
                        onCourseClick = { schedule ->
                            scheduleViewModel.onCourseClick(schedule)
                        }
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
        
        // 课程详情弹窗
        if (showDialog && selectedSchedule != null) {
            AlertDialog(
                onDismissRequest = { scheduleViewModel.closeDialog() },
                title = { Text(selectedSchedule.courseName, fontWeight = FontWeight.Bold) },
                text = {
                    Column(modifier = Modifier.fillMaxWidth()) {
                        Text("教学班: ${selectedSchedule.classCode}")
                        Text("教室: ${selectedSchedule.building} ${selectedSchedule.classroomName}")
                        Text("时间: 周${selectedSchedule.dayOfWeek} ${selectedSchedule.startTime}-${selectedSchedule.endTime}")
                        Text("选课人数: ${selectedTeachingClass?.currentStudents ?: 0}/${selectedTeachingClass?.maxStudents ?: 0}")
                    }
                },
                confirmButton = {
                    TextButton(onClick = { scheduleViewModel.closeDialog() }) {
                        Text("关闭")
                    }
                }
            )
        }
    }
}

@Composable
fun TeacherScheduleTable(
    schedules: List<ClassSchedule>,
    modifier: Modifier = Modifier,
    onCourseClick: (ClassSchedule) -> Unit = {}
) {
    val periodTimes = listOf(
        "08:00" to "09:30", // 第1节
        "10:00" to "11:30", // 第2节
        "13:30" to "15:00", // 第3节
        "15:30" to "17:00", // 第4节
        "18:00" to "19:30"  // 第5节
    )
    val maxPeriods = periodTimes.size // 表格行数=节次数
    val daysOfWeek = 7 // 表格列数=一周天数

    // 构建课表二维数组 grid[period][day]，每个单元格为课程列表
    val grid = remember(schedules) {
        List(maxPeriods) { MutableList<List<ClassSchedule>?>(daysOfWeek) { null } }
            .apply {
                schedules.forEach { schedule ->
                    val periodIdx = periodTimes.indexOfFirst { schedule.startTime.startsWith(it.first) }.let { if (it == -1) 0 else it }
                    val dayIdx = (schedule.dayOfWeek - 1).coerceIn(0, daysOfWeek - 1)
                    val list = this[periodIdx][dayIdx]?.toMutableList() ?: mutableListOf()
                    list.add(schedule)
                    this[periodIdx][dayIdx] = list
                }
            }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(8.dp)
    ) {
        // 表头：时间/周一~周日
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(modifier = Modifier.width(60.dp)) // 左上角空白
            for (d in 1..daysOfWeek) {
                Box(
                    modifier = Modifier.weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text(getDayOfWeekText(d), style = MaterialTheme.typography.bodyMedium)
                }
            }
        }

        // 表体：节次+课程，支持垂直滚动
        val scrollState = rememberScrollState()
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(scrollState)
        ) {
            for (period in 0 until maxPeriods) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    // 节次列（左侧）
                    Box(
                        modifier = Modifier.width(60.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("第${period + 1}节", style = MaterialTheme.typography.bodySmall)
                            val (start, end) = periodTimes[period]
                            Text(
                                "$start\n$end",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline,
                                lineHeight = 16.sp
                            )
                        }
                    }
                    // 每天的单元格
                    for (day in 0 until daysOfWeek) {
                        Card(
                            modifier = Modifier
                                .weight(1f)
                                .height(120.dp)
                                .padding(2.dp)
                                .clickable {
                                    val cellSchedules = grid[period][day]
                                    if (!cellSchedules.isNullOrEmpty()) {
                                        onCourseClick(cellSchedules[0])
                                    }
                                },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(10.dp),
                            elevation = CardDefaults.cardElevation(2.dp)
                        ) {
                            val cellSchedules = grid[period][day]
                            if (!cellSchedules.isNullOrEmpty()) {
                                Column(
                                    modifier = Modifier.padding(4.dp)
                                ) {
                                    for (sch in cellSchedules) {
                                        Text(
                                            text = sch.courseName,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            maxLines = 2,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Text(
                                            text = "${sch.building}-${sch.classroomName}",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.secondary,
                                            maxLines = 2
                                        )
                                        Text(
                                            text = sch.classCode,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = MaterialTheme.colorScheme.tertiary,
                                            maxLines = 1
                                        )
                                    }
                                }
                            } else {
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("-", color = MaterialTheme.colorScheme.outline, style = MaterialTheme.typography.labelSmall)
                                }
                            }
                        }
                    }
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

