package com.example.coursescheduleapp.ui.screens

import android.annotation.SuppressLint
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.coursescheduleapp.model.CourseWithTeachingClassesDTO
import com.example.coursescheduleapp.model.TeachingClassDetailDTO
import com.example.coursescheduleapp.viewmodel.SelectionOperationState
import com.example.coursescheduleapp.viewmodel.CourseSelectionViewModel
import com.example.coursescheduleapp.viewmodel.ConflictCheckResult

/**
 * 课程选择底部弹窗
 * 显示特定课程的所有教学班供学生选择/退选
 */
@SuppressLint("LongLogTag")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseSelectionBottomSheet(
    course: CourseWithTeachingClassesDTO,
    studentId: Long,
    onDismiss: () -> Unit,
    onSelectionChanged: (teachingClassId: Long, isSelected: Boolean) -> Unit,
    selectionViewModel: CourseSelectionViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    Log.d("CourseSelectionBottomSheet", "显示课程 ${course.courseName} 的底部弹窗")
    
    val selectionOperationState by selectionViewModel.operationState.collectAsState()
    val conflictCheckResult by selectionViewModel.conflictCheckResult.collectAsState()
    var showConfirmationDialog by remember { mutableStateOf(false) }
    var pendingAction by remember { mutableStateOf<Pair<Long, Boolean>?>(null) }
    var showConflictDialog by remember { mutableStateOf(false) }
    var conflictMessages by remember { mutableStateOf<List<String>>(emptyList()) }
    
    // 加载已选课程用于冲突检查
    LaunchedEffect(Unit) {
        selectionViewModel.loadMyCoursesForConflictCheck(studentId)
    }
    
    // 处理选课操作状态变化
    LaunchedEffect(selectionOperationState) {
        when (selectionOperationState) {
            is SelectionOperationState.Success -> {
                val message = (selectionOperationState as SelectionOperationState.Success).message
                Log.d("CourseSelectionBottomSheet", "选课操作成功: $message")
                // 成功提示会在Snackbar中显示
            }
            is SelectionOperationState.Error -> {
                val message = (selectionOperationState as SelectionOperationState.Error).message
                Log.e("CourseSelectionBottomSheet", "选课操作失败: $message")
                // 错误提示会在Snackbar中显示
            }
            else -> {}
        }
    }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxHeight(0.9f)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // 标题栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = course.courseName,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "共${course.totalTeachingClasses}个教学班",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                IconButton(onClick = onDismiss) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "关闭"
                    )
                }
            }
            
            // 课程描述
            course.description?.let { description ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = description,
                        modifier = Modifier.padding(12.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 教学班列表
            Text(
                text = "选择教学班",
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.titleMedium
            )
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(course.teachingClasses) { teachingClass ->
                    TeachingClassCard(
                        teachingClass = teachingClass,
                        onSelectCourse = {
                            Log.d("CourseSelectionBottomSheet", "选择教学班: ${teachingClass.classCode}")
                            
                            if (teachingClass.isSelected) {
                                // 已选课，直接退选
                                pendingAction = Pair(teachingClass.teachingClassId, false)
                                showConfirmationDialog = true
                            } else {
                                // 未选课，检查冲突
                                selectionViewModel.checkCourseConflict(
                                    studentId = studentId,
                                    targetTeachingClass = teachingClass,
                                    onConflictDetected = { result: ConflictCheckResult ->
                                        when (result) {
                                            is ConflictCheckResult.NoConflict -> {
                                                pendingAction = Pair(teachingClass.teachingClassId, true)
                                                showConfirmationDialog = true
                                            }
                                            is ConflictCheckResult.Conflict -> {
                                                conflictMessages = result.conflicts
                                                showConflictDialog = true
                                            }
                                        }
                                    }
                                )
                            }
                        },
                        onCancelSelection = {
                            Log.d("CourseSelectionBottomSheet", "退选教学班: ${teachingClass.classCode}")
                            pendingAction = Pair(teachingClass.teachingClassId, false)
                            showConfirmationDialog = true
                        },
                        isSelecting = selectionOperationState is SelectionOperationState.Loading
                    )
                }
            }
        }
        // 确认对话框
        pendingAction?.let { (teachingClassId, isSelecting) ->
            val teachingClass = course.teachingClasses.find { it.teachingClassId == teachingClassId }
            teachingClass?.let { teachingClassItem ->
                CourseSelectionConfirmationDialog(
                    showDialog = showConfirmationDialog,
                    onDismiss = {
                        showConfirmationDialog = false
                        pendingAction = null
                    },
                    onConfirm = {
                        showConfirmationDialog = false
                        if (isSelecting) {
                            selectionViewModel.selectCourse(studentId, teachingClassId)
                        } else {
                            selectionViewModel.cancelSelection(studentId, teachingClassId)
                        }
                        onSelectionChanged(teachingClassId, isSelecting)
                        pendingAction = null
                    },
                    teachingClassCode = teachingClassItem.classCode,
                    courseName = course.courseName,
                    isSelecting = isSelecting
                )
            }
        }
        // 冲突提示对话框
        if (showConflictDialog) {
            AlertDialog(
                onDismissRequest = {
                    showConflictDialog = false
                    selectionViewModel.clearConflictCheckResult()
                },
                title = {
                    Text(
                        text = "选课冲突",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                },
                text = {
                    Column {
                        Text(
                            text = "无法选择该教学班，存在以下冲突：",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        conflictMessages.forEach { conflict: String ->
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 4.dp),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Text(
                                    text = conflict,
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {
                            showConflictDialog = false
                            selectionViewModel.clearConflictCheckResult()
                        }
                    ) {
                        Text("我知道了")
                    }
                }
            )
        }
    }
}

/**
 * 教学班卡片组件
 * @param teachingClass 教学班数据
 * @param onSelectCourse 选课回调
 * @param onCancelSelection 退选回调
 * @param isSelecting 是否正在操作中
 */
@Composable
fun TeachingClassCard(
    teachingClass: TeachingClassDetailDTO,
    onSelectCourse: () -> Unit,
    onCancelSelection: () -> Unit,
    isSelecting: Boolean
) {
    Log.d("TeachingClassCard", "渲染教学班卡片: ${teachingClass.classCode}")
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 教学班信息和教师
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "教学班: ${teachingClass.classCode}",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "教师: ${teachingClass.teacherName}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // 人数信息
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (teachingClass.remainingCapacity > 0) {
                            MaterialTheme.colorScheme.primaryContainer
                        } else {
                            MaterialTheme.colorScheme.errorContainer
                        }
                    )
                ) {
                    Text(
                        text = "${teachingClass.currentStudents}/${teachingClass.maxStudents}",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = if (teachingClass.remainingCapacity > 0) {
                            MaterialTheme.colorScheme.onPrimaryContainer
                        } else {
                            MaterialTheme.colorScheme.onErrorContainer
                        }
                    )
                }
            }
            
            // 上课时间
            if (teachingClass.schedules.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "上课时间:",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                teachingClass.schedules.forEach { schedule ->
                    Text(
                        text = "${schedule.getDayOfWeekChinese()} ${schedule.startTime}-${schedule.endTime} ${schedule.building} ${schedule.classroomName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 选课/退选按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (teachingClass.isSelected) {
                    // 退选按钮
                    Button(
                        onClick = onCancelSelection,
                        modifier = Modifier.weight(1f),
                        enabled = !isSelecting,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        if (isSelecting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("退选中...")
                        } else {
                            Text("退选")
                        }
                    }
                } else {
                    // 选课按钮
                    Button(
                        onClick = onSelectCourse,
                        modifier = Modifier.weight(1f),
                        enabled = teachingClass.isAvailable && !isSelecting,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (teachingClass.isAvailable) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.surfaceVariant
                            }
                        )
                    ) {
                        if (isSelecting) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("选课中...")
                        } else {
                            Text(
                                text = if (teachingClass.isAvailable) "可选" else "已满"
                            )
                        }
                    }
                }
            }
        }
    }
    

}

/**
 * 确认对话框
 * 用于确认选课或退选操作
 */
@Composable
fun CourseSelectionConfirmationDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    teachingClassCode: String,
    courseName: String,
    isSelecting: Boolean
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = if (isSelecting) "确认选课" else "确认退选",
                    style = MaterialTheme.typography.headlineSmall
                )
            },
            text = {
                Column {
                    Text(
                        text = "课程: $courseName",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "教学班: $teachingClassCode",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = if (isSelecting) {
                            "确认选择该教学班吗？"
                        } else {
                            "确认退选该教学班吗？"
                        },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = onConfirm,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSelecting) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                ) {
                    Text(if (isSelecting) "确认选课" else "确认退选")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
            }
        )
    }


}