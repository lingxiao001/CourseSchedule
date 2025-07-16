package com.example.coursescheduleapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.coursescheduleapp.model.TeachingClass
import com.example.coursescheduleapp.viewmodel.AdminTeachingClassViewModel
import com.example.coursescheduleapp.ui.components.CommonTopBar
import com.example.coursescheduleapp.ui.components.PaginationControls
import com.example.coursescheduleapp.model.User
import com.example.coursescheduleapp.model.Course


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminTeachingClassManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminTeachingClassViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val paginationState by viewModel.paginationState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    val allTeachers by viewModel.allTeachers.collectAsState()
    val allCourses by viewModel.allCourses.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var selectedTeachingClass by remember { mutableStateOf<TeachingClass?>(null) }
    var showDeleteDialog by remember { mutableStateOf<TeachingClass?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
    ) {
        CommonTopBar(
            title = "教学班管理",
            onBack = onNavigateBack,
            rightContent = {}
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            SearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.onSearchQueryChange(it) },
                onSearch = { viewModel.onSearch() },
                active = false,
                onActiveChange = { },
                placeholder = { Text("搜索教学班代码") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "搜索") },
                modifier = Modifier.weight(1f)
            ) { }
            IconButton(
                onClick = {
                    selectedTeachingClass = null
                    showDialog = true
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加教学班")
            }
        }
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        } else if (uiState.teachingClasses.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.Groups, contentDescription = null, modifier = Modifier.size(64.dp))
                    Text("暂无教学班", style = MaterialTheme.typography.headlineSmall)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.teachingClasses) { teachingClass ->
                    TeachingClassCard(
                        teachingClass = teachingClass,
                        allUsers = allTeachers, // Pass allTeachers as allUsers
                        allCourses = allCourses,
                        onEdit = {
                            selectedTeachingClass = teachingClass
                            showDialog = true
                        },
                        onDelete = { showDeleteDialog = teachingClass }
                    )
                }
            }
            PaginationControls(
                paginationState = paginationState,
                onPreviousPage = { viewModel.onPreviousPage() },
                onNextPage = { viewModel.onNextPage() },
                onPageChange = { viewModel.onPageChange(it) }
            )
        }
        if (showDialog) {
            TeachingClassEditDialog(
                teachingClass = selectedTeachingClass,
                isEdit = selectedTeachingClass != null,
                allTeachers = allTeachers,
                allCourses = allCourses,
                allTeachingClasses = uiState.teachingClasses,
                onDismiss = {
                    showDialog = false
                    selectedTeachingClass = null
                },
                onConfirm = { teachingClass ->
                    if (selectedTeachingClass != null) {
                        viewModel.updateTeachingClass(selectedTeachingClass!!.id, teachingClass)
                    } else {
                        viewModel.createTeachingClass(teachingClass.courseId, teachingClass)
                    }
                    showDialog = false
                    selectedTeachingClass = null
                }
            )
        }
        showDeleteDialog?.let { teachingClass ->
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                title = { Text("确认删除") },
                text = { Text("确定要删除教学班 ${teachingClass.classCode} 吗？此操作不可撤销。") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteTeachingClass(teachingClass.id)
                            showDeleteDialog = null
                        }
                    ) { Text("删除", color = MaterialTheme.colorScheme.error) }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = null }) { Text("取消") }
                }
            )
        }
    }
}

@Composable
fun TeachingClassCard(
    teachingClass: TeachingClass,
    allUsers: List<User>,
    allCourses: List<Course>,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    // 优先用teachingClass.teacherName，否则在allUsers中查找teacherId==teachingClass.teacherId.toString()的user
    val teacherName = teachingClass.teacherName
        ?: allUsers.find { it.teacherId == teachingClass.teacherId }?.realName
        ?: "未知"
    val courseName = allCourses.find { it.id == teachingClass.courseId }?.courseName ?: "未知"
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = teachingClass.classCode, style = MaterialTheme.typography.titleMedium)
                    Text(text = "教师: $teacherName", style = MaterialTheme.typography.bodyMedium)
                    Text(text = "课程: $courseName", style = MaterialTheme.typography.bodyMedium)
                }
                Text("学生数: ${teachingClass.currentStudents}/${teachingClass.maxStudents}", style = MaterialTheme.typography.bodySmall)
            }
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(onClick = onEdit, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("编辑")
                }
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("删除")
                }
            }
        }
    }
} 