package com.example.coursescheduleapp.ui

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.coursescheduleapp.viewmodel.AuthState
import com.example.coursescheduleapp.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import com.example.coursescheduleapp.MainActivity
import kotlinx.coroutines.flow.StateFlow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.platform.LocalContext
import android.content.Context
import androidx.compose.ui.text.font.FontWeight

@AndroidEntryPoint
class LoginActivity : ComponentActivity() {
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val authState by authViewModel.authState.collectAsState()
            LoginScreen(
                authState = authViewModel.authState,
                onLogin = { username, password -> authViewModel.login(username, password) },
                onRegister = { username, password, realName, role, studentId, grade, className, teacherId, title, department ->
                    authViewModel.register(username, password, realName, role, studentId, grade, className, teacherId, title, department)
                },
                onSuccess = {
                    val user = (authViewModel.currentUser.value?.user)
                    if (user != null) {
                        val userJson = com.google.gson.Gson().toJson(user)
                        applicationContext.getSharedPreferences("user", Context.MODE_PRIVATE)
                            .edit().putString("user_json", userJson).apply()
                    }
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            )

            LaunchedEffect(authState) {
                if (authState is AuthState.Success) {
                    Toast.makeText(this@LoginActivity, "登录成功", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                    finish()
                } else if (authState is AuthState.Error) {
                    Toast.makeText(this@LoginActivity, (authState as AuthState.Error).message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}

@Composable
fun LoginScreen(
    authState: StateFlow<AuthState>,
    onLogin: (String, String) -> Unit,
    onRegister: (String, String, String, String, Long?, String?, String?, Long?, String?, String?) -> Unit,
    onSuccess: () -> Unit
) {
    val context = LocalContext.current
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var realName by remember { mutableStateOf("") }
    var isRegisterMode by remember { mutableStateOf(false) }
    var selectedRole by remember { mutableStateOf("student") }
    var studentId by remember { mutableStateOf("") }
    var grade by remember { mutableStateOf("") }
    var className by remember { mutableStateOf("") }
    var teacherId by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var department by remember { mutableStateOf("") }

    val state by authState.collectAsState()

    LaunchedEffect(state) {
        if (state is AuthState.Success) {
            onSuccess()
        }
    }

    MaterialTheme(
        colorScheme = lightColorScheme(
            primary = Color(0xFF4A90E2), // 浅蓝色主色调
            onPrimary = Color.White,
            background = Color(0xFFF4F8FB),
            surface = Color.White,
            onSurface = Color(0xFF222222)
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(48.dp)) // 顶部留白，Logo更靠上
                // Logo
                Surface(
                    shape = CircleShape,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(90.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            text = "CS",
                            style = MaterialTheme.typography.headlineLarge,
                            color = Color.White
                        )
                    }
                }
                Spacer(modifier = Modifier.height(32.dp)) // Logo与卡片间距
                // 登录/注册卡片
                Surface(
                    shape = RoundedCornerShape(24.dp),
                    shadowElevation = 8.dp,
                    color = MaterialTheme.colorScheme.surface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(horizontal = 24.dp, vertical = 32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (isRegisterMode) "注册账号" else "欢迎登录",
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(bottom = 24.dp)
                        )
                        OutlinedTextField(
                            value = username,
                            onValueChange = { username = it },
                            label = { Text("用户名") },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            singleLine = true,
                            shape = MaterialTheme.shapes.medium
                        )
                        OutlinedTextField(
                            value = password,
                            onValueChange = { password = it },
                            label = { Text("密码") },
                            visualTransformation = PasswordVisualTransformation(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(bottom = 16.dp),
                            singleLine = true,
                            shape = MaterialTheme.shapes.medium
                        )
                        if (isRegisterMode) {
                            OutlinedTextField(
                                value = realName,
                                onValueChange = { realName = it },
                                label = { Text("真实姓名") },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                singleLine = true,
                                shape = MaterialTheme.shapes.medium
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp),
                                horizontalArrangement = Arrangement.SpaceEvenly
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = selectedRole == "student",
                                        onClick = { selectedRole = "student" }
                                    )
                                    Text("学生")
                                }
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    RadioButton(
                                        selected = selectedRole == "teacher",
                                        onClick = { selectedRole = "teacher" }
                                    )
                                    Text("教师")
                                }
                            }
                            if (selectedRole == "student") {
                                OutlinedTextField(
                                    value = studentId,
                                    onValueChange = { studentId = it },
                                    label = { Text("学号") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp),
                                    singleLine = true,
                                    shape = MaterialTheme.shapes.medium
                                )
                                OutlinedTextField(
                                    value = grade,
                                    onValueChange = { grade = it },
                                    label = { Text("年级") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp),
                                    singleLine = true,
                                    shape = MaterialTheme.shapes.medium
                                )
                                OutlinedTextField(
                                    value = className,
                                    onValueChange = { className = it },
                                    label = { Text("班级") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp),
                                    singleLine = true,
                                    shape = MaterialTheme.shapes.medium
                                )
                            }
                            if (selectedRole == "teacher") {
                                OutlinedTextField(
                                    value = teacherId,
                                    onValueChange = { teacherId = it },
                                    label = { Text("教师ID") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp),
                                    singleLine = true,
                                    shape = MaterialTheme.shapes.medium
                                )
                                OutlinedTextField(
                                    value = title,
                                    onValueChange = { title = it },
                                    label = { Text("职称") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp),
                                    singleLine = true,
                                    shape = MaterialTheme.shapes.medium
                                )
                                OutlinedTextField(
                                    value = department,
                                    onValueChange = { department = it },
                                    label = { Text("部门") },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(bottom = 8.dp),
                                    singleLine = true,
                                    shape = MaterialTheme.shapes.medium
                                )
                            }
                        }
                        Divider(modifier = Modifier.padding(vertical = 16.dp))
                        Button(
                            onClick = {
                                if (isRegisterMode) {
                                    if (selectedRole == "student") {
                                        if (studentId.isBlank() || grade.isBlank() || className.isBlank()) {
                                            Toast.makeText(context, "请填写学号、年级和班级", Toast.LENGTH_SHORT).show()
                                        } else {
                                            onRegister(
                                                username, password, realName, selectedRole,
                                                studentId.toLongOrNull(), grade, className, null, null, null
                                            )
                                        }
                                    } else if (selectedRole == "teacher") {
                                        onRegister(
                                            username, password, realName, selectedRole,
                                            null, null, null,
                                            teacherId.toLongOrNull(), title, department
                                        )
                                    }
                                } else {
                                    onLogin(username, password)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp),
                            enabled = state !is AuthState.Loading,
                            shape = MaterialTheme.shapes.large,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = Color.White
                            )
                        ) {
                            if (state is AuthState.Loading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    color = MaterialTheme.colorScheme.onPrimary
                                )
                            } else {
                                Text(if (isRegisterMode) "注册" else "登录")
                            }
                        }
                        TextButton(
                            onClick = { isRegisterMode = !isRegisterMode },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Text(if (isRegisterMode) "已有账号？点击登录" else "没有账号？点击注册")
                        }
                        if (state is AuthState.Error) {
                            Text(
                                text = (state as AuthState.Error).message,
                                color = Color(0xFFD32F2F), // 鲜明红色
                                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold),
                                modifier = Modifier
                                    .padding(top = 20.dp, bottom = 4.dp)
                                    .fillMaxWidth()
                                    .wrapContentWidth(Alignment.CenterHorizontally)
                            )
                        }
                    }
                }
            }
        }
    }
} 