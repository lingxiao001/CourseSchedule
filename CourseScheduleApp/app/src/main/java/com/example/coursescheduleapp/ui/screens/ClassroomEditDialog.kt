package com.example.coursescheduleapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.coursescheduleapp.model.Classroom

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClassroomEditDialog(
    classroom: Classroom?,
    isEdit: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (Classroom) -> Unit
) {
    var building by remember { mutableStateOf(classroom?.building ?: "") }
    var classroomName by remember { mutableStateOf(classroom?.classroomName ?: "") }
    var capacity by remember { mutableStateOf(classroom?.capacity?.toString() ?: "") }
    var error by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) "编辑教室" else "添加教室") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = building,
                    onValueChange = { building = it },
                    label = { Text("教学楼") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = classroomName,
                    onValueChange = { classroomName = it },
                    label = { Text("教室") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    singleLine = true
                )
                OutlinedTextField(
                    value = capacity,
                    onValueChange = { capacity = it.filter { c -> c.isDigit() } },
                    label = { Text("容量") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    singleLine = true
                )
                if (error.isNotBlank()) {
                    Text(error, color = MaterialTheme.colorScheme.error)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = {
                if (building.isBlank() || classroomName.isBlank() || capacity.isBlank()) {
                    error = "请填写完整信息"
                } else {
                    onConfirm(
                        Classroom(
                            id = classroom?.id ?: 0L,
                            building = building,
                            classroomName = classroomName,
                            capacity = capacity.toIntOrNull() ?: 0
                        )
                    )
                }
            }) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("取消") }
        }
    )
} 