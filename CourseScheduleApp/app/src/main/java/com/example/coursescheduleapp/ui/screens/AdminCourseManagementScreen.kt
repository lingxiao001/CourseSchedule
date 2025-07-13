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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.coursescheduleapp.model.Course
import com.example.coursescheduleapp.repository.CourseCreateRequest
import com.example.coursescheduleapp.repository.CourseUpdateRequest
import com.example.coursescheduleapp.viewmodel.AdminCourseViewModel
import androidx.compose.material.icons.filled.School
import com.example.coursescheduleapp.ui.screens.CommonTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminCourseManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminCourseViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    
    var showDialog by remember { mutableStateOf(false) }
    var selectedCourse by remember { mutableStateOf<Course?>(null) }
    var showDeleteDialog by remember { mutableStateOf<Course?>(null) }

    // 处理错误和成功消息
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            // 这里可以显示Snackbar或Toast
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.successMessage) {
        uiState.successMessage?.let {
            // 这里可以显示Snackbar或Toast
            viewModel.clearSuccessMessage()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 8.dp)
    ) {
        CommonTopBar(
            title = "课程管理",
            onBack = onNavigateBack,
            rightContent = {
                // 右侧可放用户中心入口（如有）
            }
        )

        // 搜索栏+添加按钮同一行
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
                placeholder = { Text("搜索课程名称或代码") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "搜索") },
                modifier = Modifier.weight(1f)
            ) { }
            IconButton(
                onClick = {
                    selectedCourse = null
                    showDialog = true
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加课程")
            }
        }

        // 课程列表
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (uiState.courses.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Icon(
                        Icons.Filled.School,
                        contentDescription = null,
                        modifier = Modifier.size(64.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "暂无课程",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.courses) { course ->
                    CourseCard(
                        course = course,
                        onEdit = { 
                            selectedCourse = course
                            showDialog = true
                        },
                        onDelete = { showDeleteDialog = course }
                    )
                }
            }
            // 分页控件
            PaginationControls(
                paginationState = viewModel.paginationState.collectAsState().value,
                onPreviousPage = { viewModel.onPreviousPage() },
                onNextPage = { viewModel.onNextPage() },
                onPageChange = { viewModel.onPageChange(it) }
            )
        }

        // 添加/编辑课程对话框
        if (showDialog) {
            CourseEditDialog(
                course = selectedCourse,
                isEdit = selectedCourse != null,
                onDismiss = {
                    showDialog = false
                    selectedCourse = null
                },
                onConfirm = { courseRequest ->
                    if (selectedCourse != null) {
                        // 转换为更新请求
                        val updateRequest = CourseUpdateRequest(
                            courseName = courseRequest.courseName,
                            classCode = courseRequest.classCode,
                            description = courseRequest.description,
                            credit = courseRequest.credit,
                            hours = courseRequest.hours
                        )
                        viewModel.updateCourse(selectedCourse!!.id, updateRequest)
                    } else {
                        viewModel.createCourse(courseRequest)
                    }
                    showDialog = false
                    selectedCourse = null
                }
            )
        }

        // 删除确认对话框
        showDeleteDialog?.let { course ->
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                title = { Text("确认删除") },
                text = { Text("确定要删除课程 ${course.courseName} 吗？此操作不可撤销。") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteCourse(course.id)
                            showDeleteDialog = null
                        }
                    ) {
                        Text("删除", color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDeleteDialog = null }) {
                        Text("取消")
                    }
                }
            )
        }
    }
}

@Composable
fun CourseCard(
    course: Course,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // 课程基本信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = course.courseName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = course.classCode,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // 学分和学时标签
                Surface(
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = "${course.credit}学分",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 课程描述
            course.description?.let { desc ->
                if (desc.isNotBlank()) {
                    Text(
                        text = desc,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            // 课程详情
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("学时: ${course.hours}")
                course.createdAt?.let { Text("创建: $it") }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 操作按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onEdit,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("编辑")
                }
                
                OutlinedButton(
                    onClick = onDelete,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("删除")
                }
            }
        }
    }
} 