package com.example.coursescheduleapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.coursescheduleapp.ui.screens.*
import com.example.coursescheduleapp.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.ui.unit.dp
import com.example.coursescheduleapp.ui.theme.CourseScheduleAppTheme
import androidx.compose.ui.Alignment
import android.content.Context
import com.google.gson.Gson
import com.example.coursescheduleapp.model.User
import com.example.coursescheduleapp.model.AuthResponse
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.platform.LocalContext
import android.net.Uri
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.sp
import com.example.coursescheduleapp.R
import com.example.coursescheduleapp.CourseScheduleApplication
import com.example.coursescheduleapp.ui.screens.saveUserAvatar
import com.example.coursescheduleapp.ui.screens.loadUserAvatar
import kotlinx.coroutines.launch
import coil.compose.rememberAsyncImagePainter
import java.io.File
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.animation.ExperimentalAnimationApi
import android.content.Intent
import androidx.compose.material.icons.filled.Person
import com.example.coursescheduleapp.ui.PersonalCenterActivity

@OptIn(ExperimentalAnimationApi::class)
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    
    private val authViewModel: AuthViewModel by viewModels()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // 启动时主动读取本地用户信息
        val userJson = getSharedPreferences("user", Context.MODE_PRIVATE).getString("user_json", null)
        if (userJson != null) {
            val user = Gson().fromJson(userJson, User::class.java)
            val authResponse = AuthResponse(user = user)
            authViewModel.setCurrentUser(authResponse)
        }
        enableEdgeToEdge()
        setContent {
            CourseScheduleAppTheme {
                MainScreen(authViewModel)
            }
        }
    }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainScreen(authViewModel: AuthViewModel) {
    val navController = rememberNavController()
    val currentUser by authViewModel.currentUser.collectAsState()
    val context = LocalContext.current
    var avatarUri by remember { mutableStateOf<Uri?>(null) }
    var customName by remember { mutableStateOf(currentUser?.user?.realName ?: "") }

    val onNavigateToCourses = { navController.navigate("courses") }
    val onNavigateToMyCourses = { navController.navigate("my-courses") }
    val onNavigateToSchedule = { navController.navigate("schedule") }
    val onNavigateToUserManagement = { navController.navigate("user-management") }
    val onNavigateToCourseManagement = { navController.navigate("course-management") }
    val onNavigateToTeachingClassManagement = { navController.navigate("teaching-class-management") }
    val onNavigateToClassroomManagement = { navController.navigate("classroom-management") }
    val onNavigateToStats = { navController.navigate("admin-stats") }

    // 顶部栏右侧头像入口点击事件
    val onNavigateToPersonalCenter = {
        context.startActivity(Intent(context, PersonalCenterActivity::class.java))
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        // 不再设置topBar
    ) { innerPadding ->
        AnimatedNavHost(
            navController = navController,
            startDestination = "home",
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(300)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it / 3 },
                    animationSpec = tween(300)
                )
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it / 3 },
                    animationSpec = tween(300)
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(300)
                )
            }
        ) {
            composable("home") {
                HomeScreen(
                    currentUser = currentUser,
                    onNavigateToCourses = onNavigateToCourses,
                    onNavigateToMyCourses = onNavigateToMyCourses,
                    onNavigateToSchedule = onNavigateToSchedule,
                    onNavigateToUserManagement = onNavigateToUserManagement,
                    onNavigateToCourseManagement = onNavigateToCourseManagement,
                    onNavigateToTeachingClassManagement = onNavigateToTeachingClassManagement,
                    onNavigateToClassroomManagement = onNavigateToClassroomManagement,
                    onNavigateToStats = onNavigateToStats,
                    onLogout = { /* 不再主页显示退出登录 */ }
                )
            }
            
            composable("courses") {
                CoursesScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            composable("my-courses") {
                MyCoursesScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
            
            composable("schedule") {
                ScheduleScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("user-management") {
                AdminUserManagementScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("course-management") {
                AdminCourseManagementScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }

            composable("teaching-class-management") {
                AdminTeachingClassManagementScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable("classroom-management") {
                AdminClassroomManagementScreen(onNavigateBack = { navController.popBackStack() })
            }
            composable("admin-stats") {
                AdminStatsScreen(
                    onNavigateBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
fun HomeScreen(
    currentUser: com.example.coursescheduleapp.model.AuthResponse?,
    onNavigateToCourses: () -> Unit,
    onNavigateToMyCourses: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    onNavigateToUserManagement: () -> Unit,
    onNavigateToCourseManagement: () -> Unit,
    onNavigateToTeachingClassManagement: () -> Unit,
    onNavigateToClassroomManagement: () -> Unit,
    onNavigateToStats: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 32.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "欢迎，${currentUser?.user?.realName ?: "用户"}！",
                style = MaterialTheme.typography.headlineMedium,
                modifier = Modifier.weight(1f)
            )
            if (currentUser?.user?.role != null) {
                val roleLabel = when(currentUser.user.role) {
                    "student" -> "学生"
                    "teacher" -> "教师"
                    "admin" -> "管理员"
                    else -> currentUser.user.role
                }
                Surface(
                    color = MaterialTheme.colorScheme.primary,
                    shape = MaterialTheme.shapes.small,
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text(
                        text = "[$roleLabel]",
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
                // 个人中心入口
                IconButton(
                    onClick = {
                        context.startActivity(Intent(context, PersonalCenterActivity::class.java))
                    },
                    modifier = Modifier.padding(start = 4.dp)
                ) {
                    Icon(Icons.Default.Person, contentDescription = "个人中心")
                }
            }
        }
        // 移除DEBUG相关Text
        when (currentUser?.user?.role) {
            "student" -> {
                StudentHomeContent(
                    onNavigateToCourses = onNavigateToCourses,
                    onNavigateToMyCourses = onNavigateToMyCourses,
                    onNavigateToSchedule = onNavigateToSchedule,
                    onLogout = onLogout
                )
            }
            "teacher" -> {
                TeacherHomeContent(
                    onNavigateToMyCourses = onNavigateToMyCourses,
                    onNavigateToSchedule = onNavigateToSchedule,
                    onLogout = onLogout
                )
            }
            "admin" -> {

                AdminHomeContent(
                    onNavigateToUserManagement = onNavigateToUserManagement,
                    onNavigateToCourseManagement = onNavigateToCourseManagement,
                    onNavigateToTeachingClassManagement = onNavigateToTeachingClassManagement,
                    onNavigateToClassroomManagement = onNavigateToClassroomManagement,
                    onNavigateToStats = onNavigateToStats
                )
            }

        }
    }
}

@Composable
fun StudentHomeContent(
    onNavigateToCourses: () -> Unit,
    onNavigateToMyCourses: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = onNavigateToCourses,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("选课系统")
        }
        
        Button(
            onClick = onNavigateToMyCourses,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("我的课程")
        }
        
        Button(
            onClick = onNavigateToSchedule,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("我的课表")
        }
        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("退出登录")
        }
    }
}

@Composable
fun TeacherHomeContent(
    onNavigateToMyCourses: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    onLogout: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = onNavigateToMyCourses,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("我的教学班")
        }
        
        Button(
            onClick = onNavigateToSchedule,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("我的课表")
        }

        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("退出登录")
        }
    }
}

@Composable
fun AdminHomeContent(
    onNavigateToUserManagement: () -> Unit,
    onNavigateToCourseManagement: () -> Unit,
    onNavigateToTeachingClassManagement: () -> Unit,
    onNavigateToClassroomManagement: () -> Unit,
    onNavigateToStats: () -> Unit // 新增参数
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Button(
            onClick = onNavigateToCourseManagement,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("课程管理")
        }
        Button(
            onClick = onNavigateToUserManagement,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("用户管理")
        }
        Button(
            onClick = onNavigateToTeachingClassManagement,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("教学班管理")
        }
        Button(
            onClick = onNavigateToClassroomManagement,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("教室管理")
        }
        Button(
            onClick = onNavigateToStats,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("统计信息")
        }
    }
}

// 为所有页面添加左上角返回按钮的TopBar
@Composable
fun CommonTopBar(title: String, onBack: () -> Unit) {
    Box(modifier = Modifier.fillMaxWidth()) {
        IconButton(onClick = onBack, modifier = Modifier.align(Alignment.CenterStart)) {
            Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "返回")
        }
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}