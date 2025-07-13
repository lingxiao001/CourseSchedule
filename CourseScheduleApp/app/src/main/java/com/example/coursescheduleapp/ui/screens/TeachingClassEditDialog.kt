package com.example.coursescheduleapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.coursescheduleapp.model.TeachingClass
import com.example.coursescheduleapp.model.User
import com.example.coursescheduleapp.model.Course
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TeachingClassEditDialog(
    teachingClass: TeachingClass?,
    isEdit: Boolean,
    allTeachers: List<User>,
    allCourses: List<Course>,
    allTeachingClasses: List<TeachingClass>,
    onDismiss: () -> Unit,
    onConfirm: (TeachingClass) -> Unit
) {
    var selectedCourse by remember { mutableStateOf<Course?>(allCourses.find { it.id == teachingClass?.courseId }) }
    var selectedTeacher by remember { mutableStateOf<User?>(allTeachers.find { it.userId == teachingClass?.teacherId }) }
    var classCode by remember { mutableStateOf(teachingClass?.classCode ?: "") }
    var error by remember { mutableStateOf("") }
    var maxStudentCount by remember { mutableStateOf(teachingClass?.maxStudents?.toString() ?: "") }

    // 自动生成教学班代码
    fun autoGenerateClassCode(course: Course?): String {
        if (course == null) return ""
        val count = allTeachingClasses.count { it.courseId == course.id } + 1
        return "${course.classCode}-$count"
    }

    // 监听课程选择变化自动生成classCode
    LaunchedEffect(selectedCourse) {
        if (!isEdit && selectedCourse != null) {
            classCode = autoGenerateClassCode(selectedCourse)
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (isEdit) "编辑教学班" else "添加教学班") },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                // 课程下拉框
                var expandedCourse by remember { mutableStateOf(false) }
                OutlinedTextField(
                    value = selectedCourse?.courseName ?: "",
                    onValueChange = {},
                    label = { Text("课程名称") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { expandedCourse = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }
                )
                DropdownMenu(expanded = expandedCourse, onDismissRequest = { expandedCourse = false }) {
                    allCourses.forEach { course ->
                        DropdownMenuItem(text = { Text(course.courseName) }, onClick = {
                            selectedCourse = course
                            expandedCourse = false
                        })
                    }
                }
                // 教师下拉框
                var expandedTeacher by remember { mutableStateOf(false) }
                OutlinedTextField(
                    value = selectedTeacher?.realName ?: "",
                    onValueChange = {},
                    label = { Text("教师姓名") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { expandedTeacher = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                        }
                    }
                )
                DropdownMenu(expanded = expandedTeacher, onDismissRequest = { expandedTeacher = false }) {
                    allTeachers.forEach { teacher ->
                        DropdownMenuItem(text = { Text(teacher.realName) }, onClick = {
                            selectedTeacher = teacher
                            expandedTeacher = false
                        })
                    }
                }
                // 教学班代码
                OutlinedTextField(
                    value = classCode,
                    onValueChange = { classCode = it },
                    label = { Text("教学班代码") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    singleLine = true
                )
                // 最大学生数输入框
                OutlinedTextField(
                    value = maxStudentCount,
                    onValueChange = { maxStudentCount = it.filter { c -> c.isDigit() } },
                    label = { Text("最大学生数") },
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
                if (classCode.isBlank() || selectedCourse == null || selectedTeacher == null || maxStudentCount.isBlank()) {
                    error = "请填写完整信息"
                } else {
                    onConfirm(
                        TeachingClass(
                            id = teachingClass?.id ?: 0L,
                            classCode = classCode,
                            courseId = selectedCourse!!.id,
                            teacherId = selectedTeacher!!.userId,
                            maxStudents = maxStudentCount.toIntOrNull() ?: 0
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