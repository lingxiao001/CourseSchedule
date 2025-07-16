package com.example.coursescheduleapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.coursescheduleapp.model.User
import com.example.coursescheduleapp.repository.UserCreateRequest
import com.example.coursescheduleapp.repository.UserUpdateRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserEditDialog(
    user: User?,
    isEdit: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (UserCreateRequest) -> Unit
) {
    var username by remember { mutableStateOf(user?.username ?: "") }
    var password by remember { mutableStateOf("") }
    var realName by remember { mutableStateOf(user?.realName ?: "") }
    var role by remember { mutableStateOf(user?.role ?: "student") }
    var studentId by remember { mutableStateOf(user?.studentId?.toString() ?: "") }
    var teacherId by remember { mutableStateOf(user?.teacherId?.toString() ?: "") }
    var grade by remember { mutableStateOf(user?.grade ?: "") }
    var className by remember { mutableStateOf(user?.className ?: "") }
    var title by remember { mutableStateOf(user?.title ?: "") }
    var department by remember { mutableStateOf(user?.department ?: "") }
    var expanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = if (isEdit) "编辑用户" else "添加用户",
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.fillMaxWidth()
                )

                // 用户名
                OutlinedTextField(
                    value = username,
                    onValueChange = { username = it },
                    label = { Text("用户名") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isEdit // 编辑时不允许修改用户名
                )

                // 密码
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(if (isEdit) "新密码（留空则不修改）" else "密码") },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
                )

                // 真实姓名
                OutlinedTextField(
                    value = realName,
                    onValueChange = { realName = it },
                    label = { Text("真实姓名") },
                    modifier = Modifier.fillMaxWidth()
                )

                // 角色选择
                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded },
                ) {
                    OutlinedTextField(
                        value = when (role) {
                            "admin" -> "管理员"
                            "teacher" -> "教师"
                            "student" -> "学生"
                            else -> "学生"
                        },
                        onValueChange = { },
                        readOnly = true,
                        label = { Text("角色") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )
                    DropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        listOf("student", "teacher", "admin").forEach { roleOption ->
                            DropdownMenuItem(
                                text = {
                                    Text(when (roleOption) {
                                        "admin" -> "管理员"
                                        "teacher" -> "教师"
                                        "student" -> "学生"
                                        else -> roleOption
                                    })
                                },
                                onClick = {
                                    role = roleOption
                                    expanded = false
                                    // 清空角色特定字段
                                    if (roleOption != "student") {
                                        studentId = ""
                                        grade = ""
                                        className = ""
                                    }
                                    if (roleOption != "teacher") {
                                        teacherId = ""
                                        title = ""
                                        department = ""
                                    }
                                }
                            )
                        }
                    }
                }

                // 学生特定字段
                if (role == "student") {
                    OutlinedTextField(
                        value = studentId,
                        onValueChange = { studentId = it },
                        label = { Text("学号") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    OutlinedTextField(
                        value = grade,
                        onValueChange = { grade = it },
                        label = { Text("年级") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = className,
                        onValueChange = { className = it },
                        label = { Text("班级") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // 教师特定字段
                if (role == "teacher") {
                    OutlinedTextField(
                        value = teacherId,
                        onValueChange = { teacherId = it },
                        label = { Text("工号") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )

                    OutlinedTextField(
                        value = title,
                        onValueChange = { title = it },
                        label = { Text("职称") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    OutlinedTextField(
                        value = department,
                        onValueChange = { department = it },
                        label = { Text("院系") },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                // 按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("取消")
                    }

                    Button(
                        onClick = {
                            val userRequest = UserCreateRequest(
                                username = username,
                                password = password,
                                realName = realName,
                                role = role,
                                studentId = (if (role == "student") studentId.takeIf { it.isNotBlank() }?.toLongOrNull() else null).toString(),
                                teacherId = (if (role == "teacher") teacherId.takeIf { it.isNotBlank() }?.toLongOrNull() else null).toString(),
                                grade = if (role == "student") grade.takeIf { it.isNotBlank() } else null,
                                className = if (role == "student") className.takeIf { it.isNotBlank() } else null,
                                title = if (role == "teacher") title.takeIf { it.isNotBlank() } else null,
                                department = if (role == "teacher") department.takeIf { it.isNotBlank() } else null
                            )
                            onConfirm(userRequest)
                        },
                        modifier = Modifier.weight(1f),
                        enabled = username.isNotBlank() && realName.isNotBlank() &&
                                (!isEdit || password.isNotBlank()) // 编辑时密码可以为空
                    ) {
                        Text(if (isEdit) "更新" else "创建")
                    }
                }
            }
        }
    }
}