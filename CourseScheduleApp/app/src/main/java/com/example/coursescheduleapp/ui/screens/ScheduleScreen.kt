package com.example.coursescheduleapp.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.coursescheduleapp.model.ClassSchedule
import com.example.coursescheduleapp.viewmodel.ScheduleViewModel
import com.example.coursescheduleapp.viewmodel.ScheduleState
import com.example.coursescheduleapp.LocalCurrentUser
import androidx.compose.runtime.getValue

/**
 * 学生课表展示页面
 * 采用表格+周视图，支持点击课程弹窗详情
 * 仅负责UI渲染和交互，所有状态由ViewModel管理
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScheduleScreen(
    onNavigateBack: () -> Unit,
    scheduleViewModel: ScheduleViewModel = hiltViewModel()
) {
    // 课表数据状态
    val scheduleState = scheduleViewModel.scheduleState.collectAsState().value
    // 选中课程与弹窗状态
    val selectedSchedule = scheduleViewModel.selectedSchedule.collectAsState().value
    val showDialog = scheduleViewModel.showDialog.collectAsState().value
    val selectedTeachingClass = scheduleViewModel.selectedTeachingClass.collectAsState().value

    val currentUser = LocalCurrentUser.current
    val studentId = currentUser?.user?.studentId?.toLongOrNull()
    // 调试输出当前用户和 studentId
    LaunchedEffect(studentId) {
        println("[调试] LaunchedEffect studentId: $studentId, currentUser: $currentUser")
        if (studentId != null) {
            scheduleViewModel.loadSchedulesByStudent(studentId)
        } else {
            println("[调试] studentId 为空，未触发课表加载！currentUser: $currentUser")
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
                }
            )
        }
    ) { padding ->
        when (scheduleState) {
            is ScheduleState.Loading -> {
                // 加载中
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            is ScheduleState.Success -> {
                // 课表数据
                val schedules = (scheduleState as ScheduleState.Success).schedules
                // 定义节次时间段（可根据实际需求调整或从后端获取）
                // 每个元素为 Pair(开始时间, 结束时间)
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
                val grid = List(maxPeriods) { MutableList<List<ClassSchedule>?>(daysOfWeek) { null } }
                schedules.forEach { schedule ->
                    // 根据startTime匹配periodIdx
                    val periodIdx = periodTimes.indexOfFirst { schedule.startTime.startsWith(it.first) }.let { if (it == -1) 0 else it }
                    val dayIdx = (schedule.dayOfWeek - 1).coerceIn(0, daysOfWeek - 1)
                    val list = grid[periodIdx][dayIdx]?.toMutableList() ?: mutableListOf()
                    list.add(schedule)
                    grid[periodIdx][dayIdx] = list
                }
                // 渲染课表表格
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(8.dp)
                ) {
                    // ======= 表头：周一~周日 =======
                    Row(modifier = Modifier.fillMaxWidth()) {
                        // 左上角空白
                        Box(modifier = Modifier.width(60.dp))
                        for (d in 1..daysOfWeek) {
                            Box(
                                modifier = Modifier.weight(1f),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(getDayOfWeekText(d), style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                    }
                    // ======= 表体：节次+课程，支持垂直滚动 =======
                    val scrollState = rememberScrollState()
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(scrollState) // 使课表主体可滚动
                    ) {
                        for (period in 0 until maxPeriods) {
                            Row(modifier = Modifier.fillMaxWidth()) {
                                // 节次列（左侧）
                                Box(
                                    modifier = Modifier.width(60.dp), // 左侧宽度
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("第${period + 1}节", style = MaterialTheme.typography.bodySmall)
                                        // 节次时间段，换行显示
                                        val (start, end) = periodTimes[period]
                                        Text("$start\n$end", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.outline, lineHeight = 16.sp)
                                    }
                                }
                                // 每天的单元格
                                for (day in 0 until daysOfWeek) {
                                    Card(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(120.dp) // 单元格高度
                                            .padding(2.dp)
                                            .clickable {
                                                val cellSchedules = grid[period][day]
                                                if (!cellSchedules.isNullOrEmpty()) {
                                                    scheduleViewModel.onCourseClick(cellSchedules[0])
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
                                                    // 只显示课程名、教室
                                                    Text(
                                                        text = sch.courseName,
                                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                                        maxLines = 4,
                                                        color = MaterialTheme.colorScheme.primary
                                                    )
                                                    Text(
                                                        text = "${sch.building}-${sch.classroomName}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = MaterialTheme.colorScheme.secondary,
                                                        maxLines = 3
                                                    )
                                                }
                                            }
                                        } else {
                                            // 空白单元格美化
                                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                                Text("-", color = MaterialTheme.colorScheme.outline, style = MaterialTheme.typography.labelSmall)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
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
                                // 新增：显示教师姓名
                                Text("教师: ${selectedTeachingClass?.teacherName ?: "-"}")
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
            is ScheduleState.Error -> {
                // 加载失败
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Text(
                            text = (scheduleState as ScheduleState.Error).message,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(onClick = {
                            if (studentId != null) {
                                scheduleViewModel.loadSchedulesByStudent(studentId)
                            }
                        }) {
                            Text("重试")
                        }
                    }
                }
            }
            else -> {}
        }
    }
}

/**
 * 工具函数：将数字转为周几文本
 */
fun getDayOfWeekText(dayOfWeek: Int): String {
    return when (dayOfWeek) {
        1 -> "周一"
        2 -> "周二"
        3 -> "周三"
        4 -> "周四"
        5 -> "周五"
        6 -> "周六"
        7 -> "周日"
        else -> "未知"
    }
} 