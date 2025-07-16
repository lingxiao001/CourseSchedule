package com.example.coursescheduleapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.coursescheduleapp.model.TeachingClass
import com.example.coursescheduleapp.model.Course
import com.example.coursescheduleapp.viewmodel.TeacherTeachingClassViewModel
import com.example.coursescheduleapp.viewmodel.CoursesState
import com.example.coursescheduleapp.viewmodel.CreateTeachingClassState
import android.content.Context
import androidx.compose.ui.platform.LocalContext
import android.util.Log
import androidx.compose.ui.Alignment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddTeachingClassDialog(
    onDismiss: () -> Unit,
    onSuccess: () -> Unit,
    viewModel: TeacherTeachingClassViewModel = hiltViewModel()
) {
    var selectedCourse by remember { mutableStateOf<Course?>(null) }
    var classCode by remember { mutableStateOf("") }
    var maxStudents by remember { mutableStateOf("30") }
    var error by remember { mutableStateOf("") }
    
    val coursesState by viewModel.coursesState.collectAsState()
    val createState by viewModel.createTeachingClassState.collectAsState()
    
    val context = LocalContext.current
    
    LaunchedEffect(Unit) {
        viewModel.loadAllCourses()
    }
    
    // 处理创建成功的情况
    LaunchedEffect(createState) {
        if (createState is CreateTeachingClassState.Success) {
            viewModel.clearCreateState()
            onSuccess()
        }
    }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("添加教学班") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            ) {
                when (coursesState) {
                    is CoursesState.Loading -> {
                        Box(
                            modifier = Modifier.fillMaxWidth(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    is CoursesState.Success -> {
                        val courses = (coursesState as CoursesState.Success).courses
                        
                        // 课程选择下拉框
                        var expandedCourse by remember { mutableStateOf(false) }
                        ExposedDropdownMenuBox(
                            expanded = expandedCourse,
                            onExpandedChange = { expandedCourse = !expandedCourse }
                        ) {
                            OutlinedTextField(
                                value = selectedCourse?.courseName ?: "",
                                onValueChange = {},
                                label = { Text("选择课程 *") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .menuAnchor(),
                                readOnly = true,
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.ArrowDropDown,
                                        contentDescription = null
                                    )
                                }
                            )
                            ExposedDropdownMenu(
                                expanded = expandedCourse,
                                onDismissRequest = { expandedCourse = false }
                            ) {
                                courses.forEach { course ->
                                    DropdownMenuItem(
                                        text = { Text(course.courseName) },
                                        onClick = {
                                            selectedCourse = course
                                            // 自动生成教学班代码
                                            if (classCode.isEmpty()) {
                                                classCode = "${course.classCode}-${System.currentTimeMillis() % 1000}"
                                            }
                                            expandedCourse = false
                                        }
                                    )
                                }
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // 教学班代码
                        OutlinedTextField(
                            value = classCode,
                            onValueChange = { classCode = it },
                            label = { Text("教学班代码 *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // 最大学生数
                        OutlinedTextField(
                            value = maxStudents,
                            onValueChange = { maxStudents = it.filter { c -> c.isDigit() } },
                            label = { Text("最大学生数 *") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        if (error.isNotBlank()) {
                            Text(
                                text = error,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        
                        if (createState is CreateTeachingClassState.Error) {
                            Text(
                                text = (createState as CreateTeachingClassState.Error).message,
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                    is CoursesState.Error -> {
                        Text(
                            text = "加载课程失败: ${(coursesState as CoursesState.Error).message}",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                    else -> {}
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    when {
                        selectedCourse == null -> {
                            error = "请选择课程"
                        }
                        classCode.isBlank() -> {
                            error = "请输入教学班代码"
                        }
                        maxStudents.isBlank() || maxStudents.toIntOrNull() == null -> {
                            error = "请输入有效的最大学生数"
                        }
                        else -> {
                            error = ""
                            val sharedPref = context.getSharedPreferences("user", Context.MODE_PRIVATE)
                            val userJson = sharedPref.getString("user_json", null)
                            val teacherId = if (userJson != null) {
                                val user = com.google.gson.Gson().fromJson(userJson, com.example.coursescheduleapp.model.User::class.java)
                                user.userId
                            } else {
                                1L
                            }
                            
                            val teachingClass = TeachingClass(
                                courseId = selectedCourse!!.id,
                                teacherId = teacherId,
                                classCode = classCode,
                                maxStudents = maxStudents.toInt(),
                                currentStudents = 0
                            )
                            
                            viewModel.createTeachingClass(selectedCourse!!.id, teachingClass)
                        }
                    }
                },
                enabled = coursesState is CoursesState.Success && createState !is CreateTeachingClassState.Loading
            ) {
                if (createState is CreateTeachingClassState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("创建")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}