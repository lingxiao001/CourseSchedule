<<<<<<< Updated upstream
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import android.util.Log
import com.example.coursescheduleapp.repository.ResetPasswordRepository
import android.widget.Toast
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.coursescheduleapp.viewmodel.AuthViewModel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import android.app.Activity
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.background
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.Work
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.draw.rotate


// 个人中心页面
@AndroidEntryPoint
class PersonalCenterActivity : ComponentActivity() {
    
    @Inject
    lateinit var resetPasswordRepository: ResetPasswordRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userJson = getSharedPreferences("user", Context.MODE_PRIVATE).getString("user_json", null)
        val user = userJson?.let { Gson().fromJson(it, User::class.java) }
        val authResponse = user?.let { AuthResponse(user = it) }
        setContent {
            CourseScheduleAppTheme {
                MaterialTheme(
                    colorScheme = lightColorScheme(
                        primary = Color(0xFF6366F1),
                        onPrimary = Color.White,
                        secondary = Color(0xFF8B5CF6),
                        onSecondary = Color.White,
                        background = Color(0xFFF8FAFC),
                        surface = Color.White,
                        onSurface = Color(0xFF1E293B)
                    )
                ) {
                    val context = LocalContext.current
                    val authViewModel: AuthViewModel = hiltViewModel()
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
                    val showDialog = remember { mutableStateOf(false) }
                    
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                brush = Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF6366F1),
                                        Color(0xFF8B5CF6),
                                        Color(0xFFA855F7)
                                    )
                                )
                            )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            // Header Section
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 48.dp, start = 24.dp, end = 24.dp)
                            ) {
                                IconButton(
                                    onClick = { finish() },
                                    modifier = Modifier
                                        .align(Alignment.CenterStart)
                                        .background(
                                            color = Color.White.copy(alpha = 0.2f),
                                            shape = CircleShape
                                        )
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ArrowBack,
                                        contentDescription = "返回",
                                        tint = Color.White
                                    )
                                }
                                
                                Text(
                                    text = "个人中心",
                                    style = MaterialTheme.typography.headlineMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White,
                                    modifier = Modifier.align(Alignment.Center)
                                )
                            }

                            Spacer(modifier = Modifier.height(40.dp))

                            // Profile Card
                            Surface(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp),
                                shape = RoundedCornerShape(24.dp),
                                shadowElevation = 12.dp,
                                color = MaterialTheme.colorScheme.surface
                            ) {
                                Column(
                                    modifier = Modifier.padding(32.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    // Avatar with camera overlay
                                    Box(
                                        modifier = Modifier
                                            .size(120.dp)
                                            .clip(CircleShape)
                                            .clickable { pickImageLauncher.launch("image/*") },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (avatarPath != null) {
                                            Image(
                                                painter = rememberAsyncImagePainter(File(avatarPath!!)),
                                                contentDescription = "头像",
                                                modifier = Modifier
                                                    .size(120.dp)
                                                    .clip(CircleShape),
                                                contentScale = ContentScale.Crop
                                            )
                                        } else {
                                            Surface(
                                                modifier = Modifier.size(120.dp),
                                                shape = CircleShape,
                                                color = MaterialTheme.colorScheme.primaryContainer
                                            ) {
                                                Box(contentAlignment = Alignment.Center) {
                                                    Icon(
                                                        imageVector = Icons.Default.Person,
                                                        contentDescription = "默认头像",
                                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                                        modifier = Modifier.size(48.dp)
                                                    )
                                                }
                                            }
                                        }
                                        
                                        // Camera overlay
                                        Surface(
                                            modifier = Modifier
                                                .size(36.dp)
                                                .align(Alignment.BottomEnd),
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.primary
                                        ) {
                                            Box(contentAlignment = Alignment.Center) {
                                                Icon(
                                                    painter = painterResource(id = R.drawable.ic_launcher_foreground),
                                                    contentDescription = "更换头像",
                                                    tint = Color.White,
                                                    modifier = Modifier.size(20.dp)
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.height(24.dp))

                                    // User Info
                                    if (u != null) {
                                        Column(
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.spacedBy(12.dp)
                                        ) {
                                            Text(
                                                text = u.realName,
                                                style = MaterialTheme.typography.headlineMedium,
                                                fontWeight = FontWeight.Bold,
                                                color = MaterialTheme.colorScheme.onSurface
                                            )
                                            
                                            val roleLabel = when(u.role) {
                                                "student" -> "学生"
                                                "teacher" -> "教师"
                                                "admin" -> "管理员"
                                                else -> u.role
                                            }
                                            
                                            Surface(
                                                shape = RoundedCornerShape(16.dp),
                                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                            ) {
                                                Text(
                                                    text = roleLabel,
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Medium,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                                                )
                                            }

                                            // User Details Cards
                                            Spacer(modifier = Modifier.height(8.dp))
                                            
                                            // Username Card
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                                ),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(16.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Person,
                                                        contentDescription = "用户名",
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Column {
                                                        Text(
                                                            text = "用户名",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                        Text(
                                                            text = u.username,
                                                            style = MaterialTheme.typography.bodyLarge,
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                    }
                                                }
                                            }

                                            Spacer(modifier = Modifier.height(8.dp))

                                            // User ID Card
                                            Card(
                                                modifier = Modifier.fillMaxWidth(),
                                                colors = CardDefaults.cardColors(
                                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                                ),
                                                shape = RoundedCornerShape(12.dp)
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .padding(16.dp),
                                                    verticalAlignment = Alignment.CenterVertically
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Email,
                                                        contentDescription = "用户ID",
                                                        tint = MaterialTheme.colorScheme.primary,
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(12.dp))
                                                    Column {
                                                        Text(
                                                            text = "用户ID",
                                                            style = MaterialTheme.typography.bodySmall,
                                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                                        )
                                                        Text(
                                                            text = "${u.userId}",
                                                            style = MaterialTheme.typography.bodyLarge,
                                                            fontWeight = FontWeight.Medium
                                                        )
                                                    }
                                                }
                                            }

                                            // Role-specific info
                                            if (u.role == "student" && u.studentId != null) {
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Card(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                                    ),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(16.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.School,
                                                            contentDescription = "学号",
                                                            tint = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(12.dp))
                                                        Column {
                                                            Text(
                                                                text = "学号",
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                            Text(
                                                                text = "${u.studentId}",
                                                                style = MaterialTheme.typography.bodyLarge,
                                                                fontWeight = FontWeight.Medium
                                                            )
                                                        }
                                                    }
                                                }
                                            }

                                            if (u.role == "teacher" && u.teacherId != null) {
                                                Spacer(modifier = Modifier.height(8.dp))
                                                Card(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    colors = CardDefaults.cardColors(
                                                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                                                    ),
                                                    shape = RoundedCornerShape(12.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .fillMaxWidth()
                                                            .padding(16.dp),
                                                        verticalAlignment = Alignment.CenterVertically
                                                    ) {
                                                        Icon(
                                                            imageVector = Icons.Default.Work,
                                                            contentDescription = "教师工号",
                                                            tint = MaterialTheme.colorScheme.primary,
                                                            modifier = Modifier.size(20.dp)
                                                        )
                                                        Spacer(modifier = Modifier.width(12.dp))
                                                        Column {
                                                            Text(
                                                                text = "教师工号",
                                                                style = MaterialTheme.typography.bodySmall,
                                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                                            )
                                                            Text(
                                                                text = "${u.teacherId}",
                                                                style = MaterialTheme.typography.bodyLarge,
                                                                fontWeight = FontWeight.Medium
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(24.dp))

                            // Action Buttons
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 24.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                // Settings Button
                                Card(
                                    onClick = { showResetDialog = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(20.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Lock,
                                                contentDescription = "重置密码",
                                                tint = MaterialTheme.colorScheme.primary,
                                                modifier = Modifier.padding(8.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "重置密码",
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Medium
                                            )
                                            Text(
                                                text = "修改您的账户密码",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Default.ArrowBack,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                            modifier = Modifier.rotate(180f)
                                        )
                                    }
                                }

                                // Logout Button
                                Card(
                                    onClick = { showDialog.value = true },
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.errorContainer
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(20.dp),
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Surface(
                                            shape = CircleShape,
                                            color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.ExitToApp,
                                                contentDescription = "退出登录",
                                                tint = MaterialTheme.colorScheme.error,
                                                modifier = Modifier.padding(8.dp)
                                            )
                                        }
                                        Spacer(modifier = Modifier.width(16.dp))
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = "退出登录",
                                                style = MaterialTheme.typography.bodyLarge,
                                                fontWeight = FontWeight.Medium,
                                                color = MaterialTheme.colorScheme.onErrorContainer
                                            )
                                            Text(
                                                text = "安全退出当前账户",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onErrorContainer
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Default.ArrowBack,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.onErrorContainer,
                                            modifier = Modifier.rotate(180f)
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(40.dp))
                        }
                    }
                    
                    if (showDialog.value) {
                        AlertDialog(
                            onDismissRequest = { showDialog.value = false },
                            title = { Text("确认退出登录？") },
                            text = { Text("退出后需要重新登录才能使用应用。") },
                            confirmButton = {
                                TextButton(onClick = {
                                    showDialog.value = false
                                    authViewModel.logout(context)
                                    context.startActivity(android.content.Intent(context, LoginActivity::class.java))
                                    if (context is Activity) {
                                        context.finish()
                                    }
                                }) { Text("确认") }
                            },
                            dismissButton = {
                                TextButton(onClick = { showDialog.value = false }) { Text("取消") }
                            }
                        )
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
                                        Log.d("PersonalCenterActivity", "开始重置密码，用户名: $username")
                                        val result = resetPasswordRepository.resetPassword(username, oldPwd, newPwd)
                                        if (result.isSuccess) {
                                            showResetDialog = false
                                            errorMsg = null
                                            Toast.makeText(context, "密码重置成功", Toast.LENGTH_SHORT).show()
                                            Log.d("PersonalCenterActivity", "密码重置成功")
                                        } else {
                                            val error = result.exceptionOrNull()?.message ?: "重置失败"
                                            errorMsg = error
                                            Log.e("PersonalCenterActivity", "密码重置失败: $error")
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
=======
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import android.util.Log
import com.example.coursescheduleapp.repository.ResetPasswordRepository
import android.widget.Toast
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.coursescheduleapp.viewmodel.AuthViewModel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import android.app.Activity


// 个人中心页面
@AndroidEntryPoint
class PersonalCenterActivity : ComponentActivity() {
    
    @Inject
    lateinit var resetPasswordRepository: ResetPasswordRepository
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userJson = getSharedPreferences("user", Context.MODE_PRIVATE).getString("user_json", null)
        val user = userJson?.let { Gson().fromJson(it, User::class.java) }
        val authResponse = user?.let { AuthResponse(user = it) }
        setContent {
            CourseScheduleAppTheme {
                MaterialTheme {
                    val context = LocalContext.current
                    val authViewModel: AuthViewModel = hiltViewModel()
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
                        val showDialog = remember { mutableStateOf(false) }
                        Button(
                            onClick = {
                                showDialog.value = true
                            },
                            modifier = Modifier.fillMaxWidth(0.7f),
                            shape = RoundedCornerShape(50)
                        ) {
                            Text("退出登录")
                        }
                        if (showDialog.value) {
                            AlertDialog(
                                onDismissRequest = { showDialog.value = false },
                                title = { Text("确认退出登录？") },
                                text = { Text("退出后需要重新登录才能使用应用。") },
                                confirmButton = {
                                    TextButton(onClick = {
                                        showDialog.value = false
                                        authViewModel.logout(context)
                                        context.startActivity(android.content.Intent(context, LoginActivity::class.java))
                                        if (context is Activity) {
                                            context.finish()
                                        }
                                    }) { Text("确认") }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDialog.value = false }) { Text("取消") }
                                }
                            )
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
                                        Log.d("PersonalCenterActivity", "开始重置密码，用户名: $username")
                                        val result = resetPasswordRepository.resetPassword(username, oldPwd, newPwd)
                                        if (result.isSuccess) {
                                            showResetDialog = false
                                            errorMsg = null
                                            Toast.makeText(context, "密码重置成功", Toast.LENGTH_SHORT).show()
                                            Log.d("PersonalCenterActivity", "密码重置成功")
                                        } else {
                                            val error = result.exceptionOrNull()?.message ?: "重置失败"
                                            errorMsg = error
                                            Log.e("PersonalCenterActivity", "密码重置失败: $error")
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
>>>>>>> Stashed changes
} 