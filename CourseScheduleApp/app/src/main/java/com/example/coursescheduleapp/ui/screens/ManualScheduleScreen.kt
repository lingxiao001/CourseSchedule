package com.example.coursescheduleapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.coursescheduleapp.viewmodel.ManualScheduleViewModel
import com.example.coursescheduleapp.model.ClassSchedule
import com.example.coursescheduleapp.model.TeachingClass
import com.example.coursescheduleapp.model.Classroom
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState

import kotlinx.coroutines.launch
import com.example.coursescheduleapp.viewmodel.FeedbackMessage
import androidx.compose.foundation.clickable
import com.example.coursescheduleapp.model.Course
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState

@Composable
fun ManualScheduleScreen(
    onNavigateBack: () -> Unit,
    viewModel: ManualScheduleViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val feedback by viewModel.feedback.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    var showDialog by remember { mutableStateOf(false) }
    var editingSchedule by remember { mutableStateOf<ClassSchedule?>(null) }
    var classSearch by remember { mutableStateOf("") }
    var roomSearch by remember { mutableStateOf("") }

    // 监听反馈并弹出Snackbar
    LaunchedEffect(feedback) {
        feedback?.let {
            when (it) {
                is FeedbackMessage.Success -> snackbarHostState.showSnackbar(it.msg)
                is FeedbackMessage.Error -> snackbarHostState.showSnackbar(it.msg)
            }
            viewModel.clearFeedback()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(16.dp).padding(paddingValues)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onNavigateBack) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                }
                Text("手动排课", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(start = 8.dp))
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = {
                    editingSchedule = null
                    showDialog = true
                }) {
                    Icon(Icons.Default.Add, contentDescription = "新增排课")
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            // 搜索框
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = classSearch,
                    onValueChange = { classSearch = it },
                    label = { Text("按教学班搜索") },
                    modifier = Modifier.weight(1f)
                )
                OutlinedTextField(
                    value = roomSearch,
                    onValueChange = { roomSearch = it },
                    label = { Text("按教室搜索") },
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else {
                val filteredSchedules = uiState.schedules.filter { schedule ->
                    (classSearch.isBlank() || schedule.courseName.contains(classSearch, true) || schedule.classCode.contains(classSearch, true)) &&
                    (roomSearch.isBlank() || schedule.building.contains(roomSearch, true) || schedule.classroomName.contains(roomSearch, true))
                }
                if (filteredSchedules.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text("暂无排课信息")
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        contentPadding = PaddingValues(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(filteredSchedules) { schedule ->
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                            ) {
                                Column(modifier = Modifier.padding(16.dp)) {
                                    Text("课程: ${schedule.courseName}")
                                    Text("教学班: ${schedule.classCode}")
                                    Text("星期: ${schedule.dayOfWeek}")
                                    Text("时间: ${schedule.startTime} - ${schedule.endTime}")
                                    Text("教室: ${schedule.building}-${schedule.classroomName}")
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.End
                                    ) {
                                        IconButton(onClick = {
                                            editingSchedule = schedule
                                            showDialog = true
                                        }) {
                                            Icon(Icons.Default.Edit, contentDescription = "编辑")
                                        }
                                        IconButton(onClick = { viewModel.deleteSchedule(schedule.id) }) {
                                            Icon(Icons.Default.Delete, contentDescription = "删除")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (showDialog) {
                ManualScheduleDialog(
                    editingSchedule = editingSchedule,
                    teachingClasses = uiState.teachingClasses,
                    classrooms = uiState.classrooms,
                    courses = uiState.courses,
                    onDismiss = { showDialog = false },
                    onConfirm = { scheduleData ->
                        if (editingSchedule == null) {
                            viewModel.addSchedule(scheduleData)
                        } else {
                            viewModel.updateSchedule(editingSchedule!!.id, scheduleData)
                        }
                        showDialog = false
                    }
                )
            }
        }
    }
}

@Composable
fun ManualScheduleDialog(
    editingSchedule: ClassSchedule?,
    teachingClasses: List<TeachingClass>,
    classrooms: List<Classroom>,
    courses: List<Course>,
    onDismiss: () -> Unit,
    onConfirm: (ManualScheduleData) -> Unit
) {
    var selectedTeachingClass by remember { mutableStateOf(editingSchedule?.teachingClassId ?: teachingClasses.firstOrNull()?.id) }
    var selectedDayOfWeek by remember { mutableStateOf(editingSchedule?.dayOfWeek ?: 1) }
    var startTime by remember { mutableStateOf(editingSchedule?.startTime ?: "08:00") }
    var endTime by remember { mutableStateOf(editingSchedule?.endTime ?: "10:00") }
    var selectedClassroom by remember { mutableStateOf(editingSchedule?.classroomId ?: classrooms.firstOrNull()?.id) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (editingSchedule == null) "新增排课" else "编辑排课") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                // 教学班选择
                val courseMap = courses.associateBy { it.id }
                val initialClassText = if (editingSchedule != null) teachingClasses.find { it.id == editingSchedule.teachingClassId }?.let { "${it.classCode} - ${courseMap[it.courseId]?.courseName ?: "未知课程"}" } ?: "" else ""
                AutoCompleteTextField(
                    label = "教学班",
                    options = teachingClasses.map { it.id to "${it.classCode} - ${courseMap[it.courseId]?.courseName ?: "未知课程"}" },
                    selected = selectedTeachingClass,
                    onSelectedChange = { selectedTeachingClass = it },
                    initialText = initialClassText
                )
                // 星期选择
                val initialDayText = if (editingSchedule != null) "星期${editingSchedule.dayOfWeek}" else ""
                AutoCompleteTextField(
                    label = "星期",
                    options = (1..7).map { it to "星期$it" },
                    selected = selectedDayOfWeek,
                    onSelectedChange = { selectedDayOfWeek = it },
                    initialText = initialDayText
                )
                // 时间段
                OutlinedTextField(
                    value = startTime,
                    onValueChange = { startTime = it },
                    label = { Text("开始时间 (如08:00)") }
                )
                OutlinedTextField(
                    value = endTime,
                    onValueChange = { endTime = it },
                    label = { Text("结束时间 (如10:00)") }
                )
                // 教室选择
                val initialClassroomText = if (editingSchedule != null) classrooms.find { it.id == editingSchedule.classroomId }?.let { "${it.building}-${it.classroomName}" } ?: "" else ""
                AutoCompleteTextField(
                    label = "教室",
                    options = classrooms.map { it.id to "${it.building}-${it.classroomName}" },
                    selected = selectedClassroom,
                    onSelectedChange = { selectedClassroom = it },
                    initialText = initialClassroomText
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (selectedTeachingClass != null && selectedClassroom != null) {
                    onConfirm(
                        ManualScheduleData(
                            teachingClassId = selectedTeachingClass!!,
                            dayOfWeek = selectedDayOfWeek,
                            startTime = startTime,
                            endTime = endTime,
                            classroomId = selectedClassroom!!
                        )
                    )
                }
            }) {
                Text("保存")
            }
        },
        dismissButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@Composable
fun <T> AutoCompleteTextField(
    label: String,
    options: List<Pair<T, String>>,
    selected: T?,
    onSelectedChange: (T) -> Unit,
    initialText: String = ""
) {
    var searchText by remember { mutableStateOf(initialText) }
    val selectedText = options.find { it.first == selected }?.second
    val displayText = if (searchText.isNotEmpty() || selected == null) searchText else selectedText ?: ""
    val finalDisplayText = if (searchText.isEmpty()) "" else displayText
    val filteredOptions = if (searchText.isEmpty()) options else options.filter { it.second.contains(searchText, ignoreCase = true) }
    var showSuggestions by remember { mutableStateOf(false) }
    Box {
        OutlinedTextField(
            value = finalDisplayText,
            onValueChange = {
                searchText = it
                showSuggestions = true
            },
            label = { Text(label) },
            readOnly = false,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        if (showSuggestions && filteredOptions.isNotEmpty()) {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 56.dp)
            ) {
                Column(
                    modifier = Modifier
                        .heightIn(max = 200.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    filteredOptions.forEach { (value, text) ->
                        Text(
                            text = text,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                                .clickable {
                                    onSelectedChange(value)
                                    searchText = text
                                    showSuggestions = false
                                }
                        )
                    }
                }
            }
        }
    }
}

// 数据结构用于新增/编辑
data class ManualScheduleData(
    val teachingClassId: Long,
    val dayOfWeek: Int,
    val startTime: String,
    val endTime: String,
    val classroomId: Long
) 