package com.example.coursescheduleapp.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.*
import com.example.coursescheduleapp.CourseScheduleApplication
import com.example.coursescheduleapp.model.AuthResponse
import com.example.coursescheduleapp.ui.theme.CourseScheduleAppTheme
import android.content.Context
import com.google.gson.Gson
import com.example.coursescheduleapp.model.User
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.sp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.foundation.shape.RoundedCornerShape
import coil.compose.rememberAsyncImagePainter
import java.io.File
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.example.coursescheduleapp.R
import com.example.coursescheduleapp.ui.screens.saveUserAvatar
import com.example.coursescheduleapp.ui.screens.loadUserAvatar
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import android.content.Intent
import com.example.coursescheduleapp.ui.LoginActivity
import com.example.coursescheduleapp.model.ResetPasswordDTO
import com.example.coursescheduleapp.network.ApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import androidx.compose.ui.text.input.PasswordVisualTransformation
import android.util.Log
import com.example.coursescheduleapp.repository.ResetPasswordRepository
import android.widget.Toast
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject


@AndroidEntryPoint
class PersonalCenterActivity : ComponentActivity() {
    @Inject
    lateinit var resetPasswordRepository: com.example.coursescheduleapp.repository.ResetPasswordRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userJson = getSharedPreferences("user", Context.MODE_PRIVATE).getString("user_json", null)
        val user = userJson?.let { Gson().fromJson(it, User::class.java) }
        val authResponse = user?.let { AuthResponse(user = it) }
        setContent {
            CourseScheduleAppTheme {
                MaterialTheme {
                    val context = LocalContext.current
                    val app = context.applicationContext as CourseScheduleApplication
                    val userId = authResponse?.user?.userId ?: -1L
                    var avatarPath by remember { mutableStateOf<String?>(null) }
                    val scope = rememberCoroutineScope()

                    // 加载本地头像
                    LaunchedEffect(userId) {
                        if (userId > 0) {
                            avatarPath = loadUserAvatar(userId, app.db.userAvatarDao())
                        }
                    }

                    val pickImageLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
                        if (uri != null && userId > 0) {
                            scope.launch {
                                saveUserAvatar(context, userId, uri, app.db.userAvatarDao())
                                avatarPath = loadUserAvatar(userId, app.db.userAvatarDao())
                            }
                        }
                    }

                    val u = authResponse?.user
                    var showResetDialog by remember { mutableStateOf(false) }
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // 顶部居中标题和左上角返回按钮
                        Box(modifier = Modifier.fillMaxWidth()) {
                            IconButton(onClick = { finish() }, modifier = Modifier.align(Alignment.CenterStart)) {
                                Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "返回")
                            }
                            Text(
                                text = "个人中心",
                                style = MaterialTheme.typography.headlineSmall,
                                modifier = Modifier.align(Alignment.Center)
                            )
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                        // 卡片式个人信息展示
                        Card(
                            modifier = Modifier.fillMaxWidth(0.95f),
                            elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
                            shape = RoundedCornerShape(20.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(100.dp)
                                        .clip(CircleShape)
                                        .clickable { pickImageLauncher.launch("image/*") },
                                    contentAlignment = Alignment.Center
                                ) {
                                    if (avatarPath != null) {
                                        Image(
                                            painter = rememberAsyncImagePainter(File(avatarPath!!)),
                                            contentDescription = "头像",
                                            modifier = Modifier.size(100.dp).clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    } else {
                                        Image(
                                            painter = painterResource(id = R.drawable.ic_launcher_foreground),
                                            contentDescription = "默认头像",
                                            modifier = Modifier.size(100.dp).clip(CircleShape),
                                            contentScale = ContentScale.Crop
                                        )
                                    }
                                }
                                Spacer(modifier = Modifier.height(16.dp))
                                if (u != null) {
                                    Text("账号ID：${u.userId}", style = MaterialTheme.typography.bodyMedium)
                                    Text("登录账号：${u.username}", style = MaterialTheme.typography.bodyMedium)
                                    Text("真实姓名：${u.realName}", style = MaterialTheme.typography.bodyMedium)
                                    val roleLabel = when(u.role) {
                                        "student" -> "学生"
                                        "teacher" -> "教师"
                                        "admin" -> "管理员"
                                        else -> u.role
                                    }
                                    Text("用户角色：$roleLabel", style = MaterialTheme.typography.bodyMedium)
                                }
                            }
                        }
                        Spacer(modifier = Modifier.height(32.dp))
                        Button(
                            onClick = { showResetDialog = true },
                            modifier = Modifier.fillMaxWidth(0.7f),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text("重置密码")
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = {
                                // 清除本地用户信息
                                context.getSharedPreferences("user", Context.MODE_PRIVATE).edit().clear().apply()
                                // 跳转到登录页
                                val intent = Intent(context, LoginActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                                context.startActivity(intent)
                                // 关闭当前页面
                                finish()
                            },
                            modifier = Modifier.fillMaxWidth(0.7f),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text("退出登录")
                        }
                    }
                    if (showResetDialog) {
                        var oldPwd by remember { mutableStateOf("") }
                        var newPwd by remember { mutableStateOf("") }
                        var confirmPwd by remember { mutableStateOf("") }
                        var errorMsg by remember { mutableStateOf<String?>(null) }
                        val scope2 = rememberCoroutineScope()
                        AlertDialog(
                            onDismissRequest = { showResetDialog = false },
                            title = { Text("重置密码") },
                            text = {
                                Column {
                                    OutlinedTextField(
                                        value = oldPwd, onValueChange = { oldPwd = it },
                                        label = { Text("原密码") }, singleLine = true, visualTransformation = PasswordVisualTransformation()
                                    )
                                    OutlinedTextField(
                                        value = newPwd, onValueChange = { newPwd = it },
                                        label = { Text("新密码") }, singleLine = true, visualTransformation = PasswordVisualTransformation()
                                    )
                                    OutlinedTextField(
                                        value = confirmPwd, onValueChange = { confirmPwd = it },
                                        label = { Text("确认新密码") }, singleLine = true, visualTransformation = PasswordVisualTransformation()
                                    )
                                    if (errorMsg != null) Text(errorMsg!!, color = Color.Red, fontSize = 13.sp)
                                }
                            },
                            confirmButton = {
                                Button(onClick = {
                                    if (newPwd != confirmPwd) {
                                        errorMsg = "两次输入的新密码不一致"
                                        return@Button
                                    }
                                    scope2.launch {
                                        val username = u?.username ?: ""
                                        val result = resetPasswordRepository.resetPassword(username, oldPwd, newPwd)
                                        if (result.isSuccess) {
                                            showResetDialog = false
                                            errorMsg = null
                                            Toast.makeText(context, "密码重置成功", Toast.LENGTH_SHORT).show()
                                        } else {
                                            errorMsg = result.exceptionOrNull()?.message ?: "重置失败"
                                        }
                                    }
                                }) { Text("确认") }
                            },
                            dismissButton = {
                                OutlinedButton(onClick = { showResetDialog = false }) { Text("取消") }
                            }
                        )
                    }
                }
            }
        }
    }
} 