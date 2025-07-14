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
import androidx.compose.foundation.clickable
import androidx.compose.ui.graphics.Color

import androidx.compose.foundation.background

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

    var menuExpanded by remember { mutableStateOf(false) }
    var showSaveDialog by remember { mutableStateOf(false) }
    var pendingSchedules by remember { mutableStateOf<List<ClassSchedule>>(emptyList()) }
    var showDeleteAllDialog by remember { mutableStateOf(false) }
    var showDetailDialog by remember { mutableStateOf(false) }
    var selectedSchedule: ClassSchedule? by remember { mutableStateOf(null) }
    var showDeleteConfirm by remember { mutableStateOf(false) }

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
                            text = { Text("批量删除全校排课") },
                            onClick = {
                                menuExpanded = false
                                showDeleteAllDialog = true
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("快速安排本教学班课程") },
                            onClick = {
                                menuExpanded = false
                                selectedClassId?.let { classId ->
                                    viewModel.autoArrangeByGeneticForTeachingClass(classId) { ok ->
                                        scope.launch { snackbarHostState.showSnackbar(if (ok) "排课成功" else "排课失败") }
                                    }
                                }
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("快速安排全部教学班课程") },
                            onClick = {
                                menuExpanded = false
                                viewModel.autoArrangeByGeneticForAllTeachingClasses { ok ->
                                    scope.launch { snackbarHostState.showSnackbar(if (ok) "全部排课成功" else "全部排课失败") }
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
                    .heightIn(min = 400.dp, max = 800.dp)
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
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                    ) {
                        // 星期标题行
                        Row(modifier = Modifier.fillMaxWidth()) {
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
                                    .heightIn(min = 90.dp)
                            ) {
                                Box(
                                    modifier = Modifier.width(50.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text("${sectionIdx + 1}", style = MaterialTheme.typography.bodySmall)
                                        val times = time.split("-", limit = 2)
                                        if (times.size == 2) {
                                            Text(times[0], style = MaterialTheme.typography.labelSmall, maxLines = 1)
                                            Text(times[1], style = MaterialTheme.typography.labelSmall, maxLines = 1)
                                        } else {
                                            Text(time, style = MaterialTheme.typography.labelSmall, maxLines = 2)
                                        }
                                    }
                                }
                                for (dayIdx in 1..7) {
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .heightIn(min = 110.dp)
                                            .border(1.dp, MaterialTheme.colorScheme.outline), // 去掉.padding(4.dp)
                                        contentAlignment = Alignment.Center
                                    ) {
                                        val course = currentClassSchedules.find { it.dayOfWeek == dayIdx && sectionTimes.indexOf(it.startTime + "-" + it.endTime) == sectionIdx }
                                        if (course != null) {
                                            Box(
                                                modifier = Modifier
                                                    .matchParentSize() // 填满父Box
                                                    .background(Color(0xFF4DD0E1)) // 高亮色，可自定义
                                                    .clickable {
                                                        selectedSchedule = course
                                                        showDetailDialog = true
                                                    },
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Column(
                                                    horizontalAlignment = Alignment.CenterHorizontally,
                                                    verticalArrangement = Arrangement.Center,
                                                    modifier = Modifier.fillMaxSize()
                                                ) {
                                                    Text(
                                                        course.courseName,
                                                        maxLines = 3,
                                                        overflow = TextOverflow.Ellipsis,
                                                        color = Color.Black
                                                    )
                                                    Text(
                                                        "${course.building}-${course.classroomName}",
                                                        style = MaterialTheme.typography.labelSmall,
                                                        color = Color.Black
                                                    )
                                                }
                                            }
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
    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text("危险操作确认") },
            text = { Text("确定要删除全校所有排课吗？此操作不可恢复！") },
            confirmButton = {
                Button(onClick = {
                    showDeleteAllDialog = false
                    viewModel.deleteAllSchedules { ok ->
                        scope.launch {
                            snackbarHostState.showSnackbar(
                                if (ok) "全校排课已全部删除！" else "删除失败，请重试"
                            )
                        }
                    }
                }) { Text("确认删除") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteAllDialog = false }) { Text("取消") }
            }
        )
    }
    // 详细信息弹窗
    if (showDetailDialog && selectedSchedule != null) {
        // 通过teachingClassId查找教师名
        val teacherName = teachingClasses.find { it.id == selectedSchedule!!.teachingClassId }?.teacherName ?: "-"
        val classroomFullName = "${selectedSchedule!!.building}-${selectedSchedule!!.classroomName}"
        AlertDialog(
            onDismissRequest = { showDetailDialog = false },
            title = { Text(selectedSchedule!!.courseName) },
            text = {
                Column {
                    Text("教师：$teacherName")
                    Text("时间：周${selectedSchedule!!.dayOfWeek} ${selectedSchedule!!.startTime}-${selectedSchedule!!.endTime}")
                    Text("教室：$classroomFullName")
                }
            },
            confirmButton = {
                Row {
                    Button(onClick = { /* TODO: 跳转或弹出编辑表单 */ }) { Text("编辑") }
                    Spacer(Modifier.width(8.dp))
                    Button(
                        onClick = {
                            showDetailDialog = false
                            showDeleteConfirm = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                    ) { Text("删除") }
                }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDetailDialog = false }) { Text("关闭") }
            }
        )
    }
    // 删除确认弹窗
    if (showDeleteConfirm && selectedSchedule != null) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除该课程安排吗？") },
            confirmButton = {
                Button(onClick = {
                    viewModel.deleteSchedule(selectedSchedule!!.id) { ok: Boolean ->
                        scope.launch {
                            snackbarHostState.showSnackbar(if (ok) "删除成功" else "删除失败")
                        }
                        if (ok && selectedClassId != null) {
                            viewModel.loadSchedulesForTeachingClass(selectedClassId!!)
                        }
                    }
                    showDeleteConfirm = false
                }) { Text("确认") }
            },
            dismissButton = {
                OutlinedButton(onClick = { showDeleteConfirm = false }) { Text("取消") }
            }
        )
    }
} 