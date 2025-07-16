package com.example.coursescheduleapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.coursescheduleapp.model.SelectionDTO
import com.example.coursescheduleapp.viewmodel.AdminSelectionState
import com.example.coursescheduleapp.viewmodel.AdminSelectionViewModel
import androidx.compose.ui.platform.LocalContext
import android.util.Log
import androidx.compose.ui.text.style.TextAlign
import java.time.format.DateTimeFormatter
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.foundation.text.KeyboardOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminSelectionManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminSelectionViewModel = hiltViewModel()
) {
    val selectionState by viewModel.selectionState.collectAsState()
    val context = LocalContext.current

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var showDeleteByStudentIdDialog by rememberSaveable { mutableStateOf(false) }
    var studentIdToDelete by rememberSaveable { mutableStateOf("") }
    var showDeleteAllDialog by rememberSaveable { mutableStateOf(false) }
    var showDeleteConfirmation by rememberSaveable { mutableStateOf(false) }
    var selectionToDelete by rememberSaveable { mutableStateOf<Long?>(null) }

    LaunchedEffect(Unit) {
        Log.d("AdminSelectionScreen", "Loading all selections")
        viewModel.loadAllSelections()
    }

    // Filter selections based on search query
    val filteredSelections = when (selectionState) {
        is AdminSelectionState.Success -> {
            val selections = (selectionState as AdminSelectionState.Success).selections
            if (searchQuery.isBlank()) {
                selections
            } else {
                selections.filter { selection ->
                    selection.studentName.contains(searchQuery, ignoreCase = true) ||
                    selection.studentId.toString().contains(searchQuery) ||
                    selection.courseName.contains(searchQuery, ignoreCase = true) ||
                    selection.teacherName.contains(searchQuery, ignoreCase = true)
                }
            }
        }
        else -> emptyList()
    }

    if (showDeleteConfirmation && selectionToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteConfirmation = false
                selectionToDelete = null
            },
            title = { Text("确认删除") },
            text = { Text("确定要删除这条选课记录吗？此操作不可撤销。") },
            confirmButton = {
                Button(
                    onClick = {
                        selectionToDelete?.let { id ->
                            viewModel.deleteSelection(id)
                        }
                        showDeleteConfirmation = false
                        selectionToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteConfirmation = false
                    selectionToDelete = null
                }) {
                    Text("取消")
                }
            }
        )
    }

    if (showDeleteByStudentIdDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteByStudentIdDialog = false },
            title = { Text("按学号删除选课记录") },
            text = {
                Column {
                    Text("请输入要删除选课记录的学生学号")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = studentIdToDelete,
                        onValueChange = { studentIdToDelete = it.filter { char -> char.isDigit() } },
                        label = { Text("学号") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (studentIdToDelete.isNotBlank()) {
                            val studentId = studentIdToDelete.toLongOrNull()
                            studentId?.let { id ->
                                val studentSelections = (selectionState as? AdminSelectionState.Success)?.selections
                                    ?.filter { it.studentId == id }
                                    ?.map { it.id }
                                studentSelections?.let { selections ->
                                    if (selections.isNotEmpty()) {
                                        viewModel.deleteSelections(selections)
                                    }
                                }
                            }
                        }
                        showDeleteByStudentIdDialog = false
                        studentIdToDelete = ""
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteByStudentIdDialog = false
                    studentIdToDelete = ""
                }) {
                    Text("取消")
                }
            }
        )
    }

    if (showDeleteAllDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDialog = false },
            title = { Text("确认删除全部") },
            text = { Text("确定要删除所有选课记录吗？此操作不可撤销！") },
            confirmButton = {
                Button(
                    onClick = {
                        val allSelectionIds = (selectionState as? AdminSelectionState.Success)?.selections
                            ?.map { it.id } ?: emptyList()
                        if (allSelectionIds.isNotEmpty()) {
                            viewModel.deleteSelections(allSelectionIds)
                        }
                        showDeleteAllDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text("全部删除")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteAllDialog = false
                }) {
                    Text("取消")
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("选课记录管理") },
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
                ),
                actions = {
                    if (selectionState is AdminSelectionState.Success) {
                        var showSearch by rememberSaveable { mutableStateOf(false) }
                        if (showSearch) {
                            OutlinedTextField(
                                value = searchQuery,
                                onValueChange = { searchQuery = it },
                                placeholder = { Text("搜索...") },
                                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "搜索") },
                                modifier = Modifier.width(200.dp),
                                singleLine = true,
                                colors = TextFieldDefaults.outlinedTextFieldColors(
                                    containerColor = MaterialTheme.colorScheme.surface
                                )
                            )
                        } else {
                            IconButton(onClick = { showSearch = true }) {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = "搜索"
                                )
                            }
                        }
                    }
                    var showMenu by rememberSaveable { mutableStateOf(false) }
                    IconButton(onClick = { showMenu = true }) {
                        Icon(
                            imageVector = Icons.Default.Menu,
                            contentDescription = "菜单"
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("按学号删除") },
                            onClick = {
                                showDeleteByStudentIdDialog = true
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("删除全部") },
                            onClick = {
                                showDeleteAllDialog = true
                                showMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("刷新") },
                            onClick = {
                                viewModel.loadAllSelections()
                                showMenu = false
                            }
                        )
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            when (selectionState) {
                is AdminSelectionState.Loading -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is AdminSelectionState.Success -> {
                    if (filteredSelections.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            EmptySelectionsView()
                        }
                    } else {
                        SelectionListView(
                            selections = filteredSelections,
                            onDeleteSelection = { selectionId ->
                                selectionToDelete = selectionId
                                showDeleteConfirmation = true
                            },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                is AdminSelectionState.Error -> {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .weight(1f),
                        contentAlignment = Alignment.Center
                    ) {
                        ErrorSelectionsView(
                            message = (selectionState as AdminSelectionState.Error).message,
                            onRetry = { viewModel.loadAllSelections() }
                        )
                    }
                }
                else -> {}
            }
            if (selectionState is AdminSelectionState.Success) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "选课记录总数",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${filteredSelections.size}/${(selectionState as AdminSelectionState.Success).selections.size} 条",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SelectionListView(
    selections: List<SelectionDTO>,
    onDeleteSelection: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {


        items(selections) { selection ->
            SelectionCard(
                selection = selection,
                onDelete = { onDeleteSelection(selection.id) }
            )
        }
    }
}

@Composable
fun SelectionCard(
    selection: SelectionDTO,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = "学生: ${selection.studentName}",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "学号: ${selection.studentId}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "课程: ${selection.courseName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "教师: ${selection.teacherName}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "教学班ID: ${selection.teachingClassId}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.outline
                )
                if (selection.selectionTime != null) {
                    Text(
                        text = "选课时间: ${selection.selectionTime}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}

@Composable
fun EmptySelectionsView(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.List,
                contentDescription = "暂无记录",
                modifier = Modifier.size(64.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "暂无选课记录",
                style = MaterialTheme.typography.headlineSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "系统中还没有任何选课记录",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

@Composable
fun ErrorSelectionsView(
    message: String,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
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
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center
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