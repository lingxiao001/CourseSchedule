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
import com.example.coursescheduleapp.model.Classroom
import com.example.coursescheduleapp.viewmodel.AdminClassroomViewModel
import com.example.coursescheduleapp.ui.components.CommonTopBar
import com.example.coursescheduleapp.ui.components.PaginationControls


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminClassroomManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminClassroomViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val paginationState by viewModel.paginationState.collectAsState()
    val searchQuery by viewModel.searchQuery.collectAsState()

    var showDialog by remember { mutableStateOf(false) }
    var selectedClassroom by remember { mutableStateOf<Classroom?>(null) }
    var showDeleteDialog by remember { mutableStateOf<Classroom?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 8.dp)
    ) {
        CommonTopBar(
            title = "教室管理",
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
                placeholder = { Text("搜索教学楼/教室") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "搜索") },
                modifier = Modifier.weight(1f)
            ) { }
            IconButton(
                onClick = {
                    selectedClassroom = null
                    showDialog = true
                },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "添加教室")
            }
        }
        if (uiState.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
        } else if (uiState.classrooms.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(Icons.Default.MeetingRoom, contentDescription = null, modifier = Modifier.size(64.dp))
                    Text("暂无教室", style = MaterialTheme.typography.headlineSmall)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(uiState.classrooms) { classroom ->
                    ClassroomCard(
                        classroom = classroom,
                        onEdit = {
                            selectedClassroom = classroom
                            showDialog = true
                        },
                        onDelete = { showDeleteDialog = classroom }
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
            ClassroomEditDialog(
                classroom = selectedClassroom,
                isEdit = selectedClassroom != null,
                onDismiss = {
                    showDialog = false
                    selectedClassroom = null
                },
                onConfirm = { classroom ->
                    if (selectedClassroom != null) {
                        viewModel.updateClassroom(selectedClassroom!!.id, classroom)
                    } else {
                        viewModel.createClassroom(classroom)
                    }
                    showDialog = false
                    selectedClassroom = null
                }
            )
        }
        showDeleteDialog?.let { classroom ->
            AlertDialog(
                onDismissRequest = { showDeleteDialog = null },
                title = { Text("确认删除") },
                text = { Text("确定要删除教室 ${classroom.building}-${classroom.classroomName} 吗？此操作不可撤销。") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.deleteClassroom(classroom.id)
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
fun ClassroomCard(
    classroom: Classroom,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
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
                    Text(text = "${classroom.building}-${classroom.classroomName}", style = MaterialTheme.typography.titleMedium)
                    Text(text = "容量: ${classroom.capacity}", style = MaterialTheme.typography.bodyMedium)
                }
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