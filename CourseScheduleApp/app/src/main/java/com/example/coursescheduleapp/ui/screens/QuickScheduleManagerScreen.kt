@file:OptIn(ExperimentalMaterial3Api::class)
package com.example.coursescheduleapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import com.example.coursescheduleapp.viewmodel.QuickScheduleManagerViewModel
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.coursescheduleapp.model.TeachingClass
import androidx.compose.foundation.border
import androidx.compose.material.icons.filled.Menu
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.material3.SnackbarHostState
import kotlinx.coroutines.launch
import com.example.coursescheduleapp.model.ClassSchedule
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.material.icons.filled.Refresh
import android.util.Log

// 全局节次设置（可用ViewModel或CompositionLocal全局管理，这里用rememberSaveable模拟全局）
val defaultSections = listOf(
    "08:00-08:40", "08:50-09:30", "10:00-10:40", "10:50-11:30",
    "13:30-14:10", "14:20-15:00", "15:30-16:10", "16:20-17:00",
    "18:00-18:40", "18:50-19:30"
)

@Composable
fun QuickScheduleManagerScreen(
    onNavigateBack: () -> Unit,
    viewModel: QuickScheduleManagerViewModel = hiltViewModel()
) {
    var selectedClassId by remember { mutableStateOf<Long?>(null) }
    var showScheduleSetting by remember { mutableStateOf(false) }
    val sectionTimes by viewModel.sectionTimes.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val daysOfWeek = listOf("一", "二", "三", "四", "五", "六", "日")
    // teachingClasses如无ViewModel字段则用空列表兜底
    val teachingClasses by viewModel.teachingClasses.collectAsState(initial = emptyList())
    val courseNameMap by viewModel.courseNameMap.collectAsState(initial = emptyMap())
    val currentClassSchedules by viewModel.currentClassSchedules.collectAsState()
    val allClassrooms by viewModel.allClassrooms.collectAsState()
    val allSchedules by viewModel.allSchedules.collectAsState()
    val classCustomSectionTimes by viewModel.classCustomSectionTimes.collectAsState()

    var arrangePreview by remember { mutableStateOf<List<ClassSchedule>?>(null) }
    var arrangeConflicts by remember { mutableStateOf<List<String>>(emptyList()) }
    var showArrangeDialog by remember { mutableStateOf(false) }
    var arrangeSectionCount by remember { mutableStateOf(5) } // 默认5节
    var arrangeClassrooms by remember { mutableStateOf<List<Long>>(emptyList()) }
    var menuExpanded by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var pendingSchedules by remember { mutableStateOf<List<ClassSchedule>>(emptyList()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("快速排课管理") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { menuExpanded = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "更多操作")
                    }
                    DropdownMenu(
                        expanded = menuExpanded,
                        onDismissRequest = { menuExpanded = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("课表设置") },
                            onClick = { menuExpanded = false; showScheduleSetting = true }
                        )
                        DropdownMenuItem(
                            text = { Text("批量删除本班排课") },
                            onClick = {
                                menuExpanded = false
                                selectedClassId?.let {
                                    viewModel.deleteAllSchedulesForClass(it) { ok ->
                                        scope.launch { snackbarHostState.showSnackbar(if (ok) "删除成功" else "删除失败") }
                                    }
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("快速安排") },
                            onClick = {
                                menuExpanded = false
                                selectedClassId?.let { classId ->
                                    // 弹窗让用户选择节次数量和可用教室
                                    arrangeSectionCount = (classCustomSectionTimes[classId]?.size ?: sectionTimes.size).coerceAtLeast(1)
                                    arrangeClassrooms = allClassrooms.map { it.id }
                                    showArrangeDialog = true
                                }
                            }
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            // 教学班选择
            Text("选择教学班：")
            AutoCompleteTextField(
                label = "教学班",
                options = teachingClasses.map { it.id to "${it.classCode} - ${courseNameMap[it.courseId] ?: "未知课程"}" },
                selected = selectedClassId,
                onSelectedChange = { id ->
                    selectedClassId = id
                    if (id != null) viewModel.loadSchedulesForTeachingClass(id)
                }
            )
            Spacer(modifier = Modifier.height(8.dp))
            // 节次与时间（可后续与课表设置联动）
            // 假设课表数据结构：Map<Pair<Int, Int>, String>，key=(dayOfWeek, section)，value=课程名
            val scheduleMap = remember(currentClassSchedules, sectionTimes) {
                mutableStateMapOf<Pair<Int, Int>, String>().apply {
                    currentClassSchedules.forEach { schedule ->
                        // 更健壮的节次匹配：startTime或endTime包含在sectionTimes任一项中
                        val sectionIdx = sectionTimes.indexOfFirst {
                            it.contains(schedule.startTime) || it.contains(schedule.endTime)
                        }
                        if (sectionIdx >= 0) {
                            this[schedule.dayOfWeek to (sectionIdx + 1)] =
                                "${schedule.courseName}@${schedule.building}${schedule.classroomName}"
                        }
                    }
                }
            }
            // 课表预览
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text("课表预览：", modifier = Modifier.weight(1f))
                if (selectedClassId != null) {
                    IconButton(onClick = { viewModel.loadSchedulesForTeachingClass(selectedClassId!!) }) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新课表")
                    }
                }
            }
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 400.dp, max = 800.dp) // 整体更高
                    .border(1.dp, MaterialTheme.colorScheme.outline)
            ) {
                if (selectedClassId == null || scheduleMap.isEmpty()) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(Icons.Default.Menu, contentDescription = null, modifier = Modifier.size(80.dp), tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f))
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("本周没有课程哦", style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                    }
                } else {
                    // 垂直滚动
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // 星期标题行
                        Row(modifier = Modifier.fillMaxWidth()) {
                            // 左上角空白，宽度与时间列一致
                            Spacer(modifier = Modifier.width(50.dp))
                            daysOfWeek.forEach { day: String ->
                                Box(
                                    modifier = Modifier.weight(1f).padding(vertical = 6.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("周$day", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                        // 节次行
                        sectionTimes.forEachIndexed { sectionIdx, time ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 90.dp) // 行更高
                            ) {
                                // 节次与时间列加宽，时间多行显示，无'-'
                                Box(
                                    modifier = Modifier.width(50.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("${sectionIdx + 1}", style = MaterialTheme.typography.bodySmall)
                                        val times = time.split("-", limit = 2)
                                        if (times.size == 2) {
                                            Text(times[0], style = MaterialTheme.typography.labelSmall,     maxLines = 1)
                                            Text(times[1], style = MaterialTheme.typography.labelSmall, maxLines = 1)
                                        } else {
                                            Text(time, style = MaterialTheme.typography.labelSmall, maxLines = 2)
                                        }
                                    }
                                }
                                // 每天的课程单元格
                                for (dayIdx in 1..7) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 110.dp)
                                            .border(1.dp, MaterialTheme.colorScheme.outline)
                                            .padding(4.dp),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val course = scheduleMap[dayIdx to (sectionIdx + 1)]
                                        if (course != null) {
                                            Text(
                                                course,
                                                style = MaterialTheme.typography.bodySmall,
                                                maxLines = 6,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
    
        }
    }
    if (showScheduleSetting) {
        var tempSections by remember { mutableStateOf(sectionTimes.toMutableList()) }
        AlertDialog(
            onDismissRequest = { showScheduleSetting = false },
            title = { Text("课表设置") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("节次数量：")
                        IconButton(onClick = {
                            if (tempSections.size > 1) tempSections = tempSections.dropLast(1).toMutableList()
                        }) {
                            Icon(Icons.Default.Remove, contentDescription = "减少节数")
                        }
                        Text(tempSections.size.toString(), modifier = Modifier.width(32.dp))
                        IconButton(onClick = {
                            if (tempSections.size < 20) tempSections = (tempSections + "08:00-08:45").toMutableList()
                        }) {
                            Icon(Icons.Default.Add, contentDescription = "增加节数")
                        }
                    }
                    tempSections.forEachIndexed { idx, time ->
                        OutlinedTextField(
                            value = time,
                            onValueChange = { newValue ->
                                tempSections = tempSections.toMutableList().also { it[idx] = newValue }
                            },
                            label = { Text("第${idx + 1}节时间（如08:00-08:45）") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.saveSectionSetting(tempSections.toList())
                    showScheduleSetting = false
                }) { Text("确定") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showScheduleSetting = false }) { Text("取消") }
            }
        )
    }
    if (showArrangeDialog && selectedClassId != null) {
        AlertDialog(
            onDismissRequest = { showArrangeDialog = false },
            title = { Text("快速安排设置") },
            text = {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("本周排课节数：")
                        IconButton(onClick = { if (arrangeSectionCount > 1) arrangeSectionCount-- }) {
                            Icon(Icons.Default.Remove, contentDescription = "减少节数")
                        }
                        Text(arrangeSectionCount.toString(), modifier = Modifier.width(32.dp))
                        IconButton(onClick = { if (arrangeSectionCount < sectionTimes.size) arrangeSectionCount++ }) {
                            Icon(Icons.Default.Add, contentDescription = "增加节数")
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val customSections = sectionTimes.take(arrangeSectionCount)
                    viewModel.setClassCustomSectionTimes(selectedClassId!!, customSections)
                    Log.d("QuickSchedule", "点击快速排课确认，准备生成预览")
                    viewModel.autoArrangeSchedulesWithPreview(
                        selectedClassId!!,
                        customSections,
                        allSchedules,
                        allClassrooms.map { it.id },
                        null
                    ) { result ->
                        arrangePreview = result.arranged
                        arrangeConflicts = result.conflicts
                        showArrangeDialog = false
                        if (result.arranged.isNotEmpty()) {
                            pendingSchedules = result.arranged
                            showSaveDialog = true
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar(
                                    if (result.conflicts.isEmpty()) "无可用排课" else "排课有冲突：${result.conflicts.joinToString()}"
                                )
                            }
                        }
                    }
                }) { Text("确认安排") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showArrangeDialog = false }) { Text("取消") }
            }
        )
    }
    // 保存排课确认弹窗
    if (showSaveDialog && pendingSchedules.isNotEmpty()) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = { Text("确认保存排课") },
            text = {
                Column {
                    Text("将为本班生成如下排课：")
                    pendingSchedules.forEach {
                        Text("周${it.dayOfWeek} ${it.startTime}-${it.endTime} @教室${it.classroomId}")
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    Log.d("QuickSchedule", "用户确认保存排课，开始保存")
                    viewModel.saveArrangedSchedules(pendingSchedules) { ok ->
                        showSaveDialog = false
                        scope.launch {
                            snackbarHostState.showSnackbar(if (ok) "排课成功" else "排课失败")
                        }
                        if (ok && selectedClassId != null) {
                            viewModel.loadSchedulesForTeachingClass(selectedClassId!!)
                        }
                    }
                }) { Text("确认保存") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showSaveDialog = false }) { Text("取消") }
            }
        )
    }
} 