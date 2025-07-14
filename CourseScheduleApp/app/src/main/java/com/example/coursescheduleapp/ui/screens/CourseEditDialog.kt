package com.example.coursescheduleapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.coursescheduleapp.model.Course
import com.example.coursescheduleapp.repository.CourseCreateRequest
import com.example.coursescheduleapp.repository.CourseUpdateRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseEditDialog(
    course: Course?,
    isEdit: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (CourseCreateRequest) -> Unit
) {
    var courseName by remember { mutableStateOf(course?.courseName ?: "") }
    var classCode by remember { mutableStateOf(course?.classCode ?: "") }
    var description by remember { mutableStateOf(course?.description ?: "") }
    var credit by remember { mutableStateOf(course?.credit?.toString() ?: "1.0") }
    var hours by remember { mutableStateOf(course?.hours?.toString() ?: "16") }

    var courseNameError by remember { mutableStateOf<String?>(null) }
    var classCodeError by remember { mutableStateOf<String?>(null) }
    var creditError by remember { mutableStateOf<String?>(null) }
    var hoursError by remember { mutableStateOf<String?>(null) }

    fun validateForm(): Boolean {
        var isValid = true
        
        if (courseName.isBlank()) {
            courseNameError = "课程名称不能为空"
            isValid = false
        } else {
            courseNameError = null
        }
        
        if (classCode.isBlank()) {
            classCodeError = "课程代码不能为空"
            isValid = false
        } else {
            classCodeError = null
        }
        
        val creditValue = credit.toDoubleOrNull()
        if (creditValue == null || creditValue <= 0) {
            creditError = "请输入有效的学分"
            isValid = false
        } else {
            creditError = null
        }
        
        val hoursValue = hours.toIntOrNull()
        if (hoursValue == null || hoursValue <= 0) {
            hoursError = "请输入有效的学时"
            isValid = false
        } else {
            hoursError = null
        }
        
        return isValid
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) "编辑课程" else "添加课程") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 课程名称
                OutlinedTextField(
                    value = courseName,
                    onValueChange = { courseName = it },
                    label = { Text("课程名称") },
                    isError = courseNameError != null,
                    supportingText = courseNameError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )

                // 课程代码
                OutlinedTextField(
                    value = classCode,
                    onValueChange = { classCode = it },
                    label = { Text("课程代码") },
                    isError = classCodeError != null,
                    supportingText = classCodeError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )

                // 课程描述
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("课程描述") },
                    modifier = Modifier.fillMaxWidth(),
                    minLines = 3,
                    maxLines = 5
                )

                // 学分
                OutlinedTextField(
                    value = credit,
                    onValueChange = { credit = it },
                    label = { Text("学分") },
                    isError = creditError != null,
                    supportingText = creditError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )

                // 学时
                OutlinedTextField(
                    value = hours,
                    onValueChange = { hours = it },
                    label = { Text("学时") },
                    isError = hoursError != null,
                    supportingText = hoursError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (validateForm()) {
                        val courseRequest = CourseCreateRequest(
                            courseName = courseName.trim(),
                            classCode = classCode.trim(),
                            description = description.trim(),
                            credit = credit.toDouble(),
                            hours = hours.toInt()
                        )
                        onConfirm(courseRequest)
                    }
                }
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
} 