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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.coursescheduleapp.model.User
import com.example.coursescheduleapp.repository.UserCreateRequest
import com.example.coursescheduleapp.viewmodel.AdminUserViewModel
import com.example.coursescheduleapp.ui.components.PaginationControls
import com.example.coursescheduleapp.ui.components.CommonTopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminUserManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminUserViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val paginationState by viewModel.paginationState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()
    
    var showDialog by remember { mutableStateOf(false) }
    var selectedUser by remember { mutableStateOf<User?>(null) }
    var showDeleteDialog by remember { mutableStateOf<User?>(null) }

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

    Scaffold(
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            CommonTopBar(
                title = "用户管理",
                onBack = onNavigateBack,
                rightContent = {
                    IconButton(onClick = { 
                        selectedUser = null
                        showDialog = true 
                    }) {
                        Icon(Icons.Default.Add, contentDescription = "添加用户")
                    }
                }
            )

            // 搜索栏
            SearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.onSearchQueryChange(it) },
                onSearch = { viewModel.onSearch() },
                active = false,
                onActiveChange = { },
                placeholder = { Text("搜索用户名或姓名") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "搜索") },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(2.dp)
            ) { }

            // 用户列表
            if (uiState.isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else if (uiState.users.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            Icons.Default.Person,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "暂无用户",
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
                    items(uiState.users) { user ->
                        UserCard(
                            user = user,
                            onEdit = { 
                                selectedUser = user
                                showDialog = true
                            },
                            onDelete = { showDeleteDialog = user }
                        )
                    }
                }

                // 分页控件
                PaginationControls(
                    paginationState = paginationState,
                    onPreviousPage = { viewModel.onPreviousPage() },
                    onNextPage = { viewModel.onNextPage() },
                    onPageChange = { viewModel.onPageChange(it) }
                )
            }
        }

        // 添加/编辑用户对话框
        if (showDialog) {
            UserEditDialog(
                user = selectedUser,
                isEdit = selectedUser != null,
                onDismiss = {
                    showDialog = false
                    selectedUser = null
                },
                onConfirm = { userRequest ->
                    if (selectedUser != null) {
                        // 转换为更新请求
                        val updateRequest = com.example.coursescheduleapp.repository.UserUpdateRequest(
                            realName = userRequest.realName,
                            role = userRequest.role,
                            newPassword = userRequest.password.takeIf { it.isNotBlank() },
                            grade = userRequest.grade,
                            className = userRequest.className,
                            title = userRequest.title,
                            department = userRequest.department
                        )
                        viewModel.updateUser(selectedUser!!.userId, updateRequest)
                    } else {
                        viewModel.createUser(userRequest)
                    }
                    showDialog = false
                    selectedUser = null
                }
            )
        }

        // 删除确认对话框
        showDeleteDialog?.let { user ->
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                title = { Text("确认删除") },
                text = { Text("确定要删除用户 ${user.realName} 吗？此操作不可撤销。") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteUser(user.userId)
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
fun UserCard(
    user: User,
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
            // 用户基本信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = user.realName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "@${user.username}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // 角色标签
                val roleColor = when (user.role) {
                    "admin" -> MaterialTheme.colorScheme.error
                    "teacher" -> MaterialTheme.colorScheme.tertiary
                    else -> MaterialTheme.colorScheme.primary
                }
                
                val roleText = when (user.role) {
                    "admin" -> "管理员"
                    "teacher" -> "教师"
                    "student" -> "学生"
                    else -> user.role
                }
                
                Surface(
                    color = roleColor.copy(alpha = 0.1f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = roleText,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = roleColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 角色特定信息
            when (user.role) {
                "student" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        user.studentId?.let { Text("学号: $it") }
                        user.grade?.let { Text("年级: $it") }
                        user.className?.let { Text("班级: $it") }
                    }
                }
                "teacher" -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        user.teacherId?.let { Text("工号: $it") }
                        user.title?.let { Text("职称: $it") }
                        user.department?.let { Text("院系: $it") }
                    }
                }
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