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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.MeetingRoom
import androidx.compose.material.icons.filled.EditCalendar
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.Construction
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.coursescheduleapp.model.CourseWithTeachingClassesDTO
import com.example.coursescheduleapp.viewmodel.CourseSelectionViewModel


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
            CompositionLocalProvider(LocalCurrentUser provides authViewModel.currentUser.collectAsState().value) {
                CourseScheduleAppTheme {
                    MainTabScreen(authViewModel)
                }
            }
        }
    }
}

// 新增底部导航主界面
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun MainTabScreen(authViewModel: AuthViewModel) {
    val context = LocalContext.current
    val currentUser by authViewModel.currentUser.collectAsState()
    var selectedTab by remember { mutableStateOf(0) }
    val tabItems = listOf(
        Triple("首页", Icons.Default.Home, "home"),
        Triple("消息", Icons.Default.Message, "message"),
        Triple("个人", Icons.Default.Person, "profile")
    )
    var shouldNavigateProfile by remember { mutableStateOf(false) }
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val showBottomBar = navBackStackEntry?.destination?.route == "main-home"

    if (shouldNavigateProfile) {
        LaunchedEffect(Unit) {
            context.startActivity(Intent(context, PersonalCenterActivity::class.java))
            selectedTab = 0
            shouldNavigateProfile = false
        }
    }

    Scaffold(
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    modifier = Modifier.height(52.dp)
                ) {
                    tabItems.forEachIndexed { index, item ->
                        NavigationBarItem(
                            icon = { Icon(item.second, contentDescription = item.first, modifier = Modifier.size(22.dp)) },
                            label = { Text(item.first, modifier = Modifier.padding(top = 0.dp, bottom = 2.dp), style = MaterialTheme.typography.labelMedium) },
                            selected = selectedTab == index,
                            onClick = {
                                if (index == 2) {
                                    shouldNavigateProfile = true
                                } else {
                                    selectedTab = index
                                    // 切换Tab时始终回到main-home
                                    navController.popBackStack("main-home", inclusive = false)
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        AnimatedNavHost(
            navController = navController,
            startDestination = "main-home",
            modifier = Modifier.padding(innerPadding),
            enterTransition = {
                slideInHorizontally(
                    initialOffsetX = { it },
                    animationSpec = tween(350)
                )
            },
            exitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { -it / 2 },
                    animationSpec = tween(350)
                )
            },
            popEnterTransition = {
                slideInHorizontally(
                    initialOffsetX = { -it / 2 },
                    animationSpec = tween(350)
                )
            },
            popExitTransition = {
                slideOutHorizontally(
                    targetOffsetX = { it },
                    animationSpec = tween(350)
                )
            }
        ) {
            composable("main-home") {
                when (selectedTab) {
                    0 -> HomeTabContent(
                        currentUser = currentUser,
                        onNavigateToUserManagement = { navController.navigate("user-management") },
                        onNavigateToCourseManagement = { navController.navigate("course-management") },
                        onNavigateToTeachingClassManagement = { navController.navigate("teaching-class-management") },
                        onNavigateToClassroomManagement = { navController.navigate("classroom-management") },
                        onNavigateToStats = { navController.navigate("admin-stats") },
                        onNavigateToManualSchedule = { navController.navigate("manual-schedule") },
                        onNavigateToQuickScheduleManager = { navController.navigate("quick-schedule-manager") },
                        onNavigateToCourses = { navController.navigate("courses") },
                        onNavigateToMyCourses = { navController.navigate("my-courses") },
                        onNavigateToSchedule = { navController.navigate("schedule") },
                        onNavigateToSelectCourse = { navController.navigate("select-course") },
                        onLogout = { /* 可实现退出逻辑 */ }
                    )
                    1 -> MessageScreen()
                    // 2 -> ProfileScreen...（已自动跳转）
                }
            }
            composable("user-management") { AdminUserManagementScreen(onNavigateBack = { navController.popBackStack() }) }
            composable("course-management") { AdminCourseManagementScreen(onNavigateBack = { navController.popBackStack() }) }
            composable("teaching-class-management") { AdminTeachingClassManagementScreen(onNavigateBack = { navController.popBackStack() }) }
            composable("classroom-management") { AdminClassroomManagementScreen(onNavigateBack = { navController.popBackStack() }) }
            composable("admin-stats") { AdminStatsScreen(onNavigateBack = { navController.popBackStack() }) }
            composable("manual-schedule") { ManualScheduleScreen(onNavigateBack = { navController.popBackStack() }) }
            composable("quick-schedule-manager") { QuickScheduleManagerScreen(onNavigateBack = { navController.popBackStack() }) }
            composable("courses") { CoursesScreen(onNavigateBack = { navController.popBackStack() }) }
            composable("my-courses") { MyCoursesScreen(onNavigateBack = { navController.popBackStack() }) }
            composable("schedule") { ScheduleScreen(onNavigateBack = { navController.popBackStack() }) }
            composable("select-course") { 
                val currentUser = LocalCurrentUser.current
                val studentId = currentUser?.user?.studentId?.toLongOrNull()?:0L
                var selectedCourse by remember { mutableStateOf<CourseWithTeachingClassesDTO?>(null) }
                
                Box {
                    CourseListScreen(
                        onNavigateBack = { navController.popBackStack() },
                        onCourseClick = { course ->
                            selectedCourse = course
                        },
                        studentId = studentId
                    )
                    
                    selectedCourse?.let { course ->
                        CourseSelectionBottomSheetWrapper(
                            course = course,
                            studentId = studentId,
                            onDismiss = { selectedCourse = null },
                            onSelectionChanged = { _, _ ->
                                // 重新加载课程列表以更新状态
                                navController.popBackStack()
                                navController.navigate("select-course")
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * 课程选择底部弹窗包装器
 * 处理底部弹窗的显示和状态管理
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseSelectionBottomSheetWrapper(
    course: CourseWithTeachingClassesDTO,
    studentId: Long,
    onDismiss: () -> Unit,
    onSelectionChanged: (Long, Boolean) -> Unit,
    selectionViewModel: CourseSelectionViewModel = androidx.hilt.navigation.compose.hiltViewModel()
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        modifier = Modifier.fillMaxHeight(0.9f)
    ) {
        CourseSelectionBottomSheet(
            course = course,
            studentId = studentId,
            onDismiss = onDismiss,
            onSelectionChanged = onSelectionChanged,
            selectionViewModel = selectionViewModel
        )
    }
}

// 首页Tab内容，顶部浅蓝色欢迎框，去除个人中心入口
@Composable
fun HomeTabContent(
    currentUser: AuthResponse?,
    onNavigateToUserManagement: () -> Unit = {},
    onNavigateToCourseManagement: () -> Unit = {},
    onNavigateToTeachingClassManagement: () -> Unit = {},
    onNavigateToClassroomManagement: () -> Unit = {},
    onNavigateToStats: () -> Unit = {},
    onNavigateToManualSchedule: () -> Unit = {},
    onNavigateToQuickScheduleManager: () -> Unit = {},
    onNavigateToCourses: () -> Unit = {},
    onNavigateToMyCourses: () -> Unit = {},
    onNavigateToSchedule: () -> Unit = {},
    onNavigateToSelectCourse: () -> Unit = {},
    onLogout: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Surface(
            color = Color(0xFFB3E5FC),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .padding(top = 16.dp, bottom = 16.dp)
                .wrapContentHeight()
                .fillMaxWidth()
        ) {
            Text(
                text = "欢迎，${currentUser?.user?.realName ?: "用户"}！",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF1565C0),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
        }
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (currentUser?.user?.role) {
                "student" -> {
                    StudentHomeContent(
                        onNavigateToCourses = onNavigateToCourses,
                        onNavigateToMyCourses = onNavigateToMyCourses,
                        onNavigateToSchedule = onNavigateToSchedule,
                        onNavigateToSelectCourse = onNavigateToSelectCourse
                    )
                }
                "teacher" -> {
                    TeacherHomeContent(
                        onNavigateToMyCourses = onNavigateToMyCourses,
                        onNavigateToSchedule = onNavigateToSchedule,
                    )
                }
                "admin" -> {
                    AdminHomeContent(
                        onNavigateToUserManagement = onNavigateToUserManagement,
                        onNavigateToCourseManagement = onNavigateToCourseManagement,
                        onNavigateToTeachingClassManagement = onNavigateToTeachingClassManagement,
                        onNavigateToClassroomManagement = onNavigateToClassroomManagement,
                        onNavigateToStats = onNavigateToStats,
                        onNavigateToManualSchedule = onNavigateToManualSchedule,
                        onNavigateToQuickScheduleManager = onNavigateToQuickScheduleManager
                    )
                }
            }
        }
    }
}

// 消息Tab内容
@Composable
fun MessageScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Text("消息功能开发中，敬请期待！", style = MaterialTheme.typography.titleMedium)
    }
}

// 个人Tab内容，点击跳转个人中心
@Composable
fun ProfileScreen(onNavigate: () -> Unit) {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        Button(onClick = onNavigate) {
            Icon(Icons.Default.Person, contentDescription = "个人中心")
            Spacer(Modifier.width(8.dp))
            Text("进入个人中心")
        }
    }
}

@Composable
fun StudentHomeContent(
    onNavigateToCourses: () -> Unit,
    onNavigateToMyCourses: () -> Unit,
    onNavigateToSchedule: () -> Unit,
    onNavigateToSelectCourse: () -> Unit,
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(top = 8.dp)
    ) {
        item {
            Row {
                val cardModifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
                    .height(120.dp)
                FuncCard(
                    title = "我的课表",
                    subtitle = "查看本学期课表",
                    icon = Icons.Default.CalendarMonth,
                    modifier = cardModifier,
                    onClick = onNavigateToSchedule
                )
                FuncCard(
                    title = "选课",
                    subtitle = "选择可选课程",
                    icon = Icons.Default.MenuBook,
                    modifier = cardModifier,
                    onClick = onNavigateToSelectCourse
                )
            }
        }
        item {
            Row {
                val cardModifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
                    .height(120.dp)
                FuncCard(
                    title = "我的课程",
                    subtitle = "查看已选课程",
                    icon = Icons.Default.MenuBook,
                    modifier = cardModifier,
                    onClick = onNavigateToMyCourses
                )
                // 移除"可选课程"卡片，保留空白区域维持布局
                Spacer(
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp)
                        .height(120.dp)
                )
            }
        }
    }
}

@Composable
fun TeacherHomeContent(
    onNavigateToMyCourses: () -> Unit,
    onNavigateToSchedule: () -> Unit,
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

    }
}

@Composable
fun AdminHomeContent(
    onNavigateToUserManagement: () -> Unit,
    onNavigateToCourseManagement: () -> Unit,
    onNavigateToTeachingClassManagement: () -> Unit,
    onNavigateToClassroomManagement: () -> Unit,
    onNavigateToStats: () -> Unit, // 新增参数
    onNavigateToManualSchedule: () -> Unit, // 新增参数
    onNavigateToQuickScheduleManager: () -> Unit // 新增参数
) {


    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(top = 8.dp)
    ) {
        item {
            Row {
                val cardModifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
                    .height(120.dp)

                FuncCard(
                    title = "用户管理",
                    subtitle = "管理所有用户",
                    icon = Icons.Default.Person,
                    modifier = cardModifier,
                    onClick = onNavigateToUserManagement
                )
                FuncCard(
                    title = "课程管理",
                    subtitle = "管理课程信息",
                    icon = Icons.Default.MenuBook,
                    modifier = cardModifier,
                    onClick = onNavigateToCourseManagement
                )
            }
        }
        item {
            Row {
                val cardModifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
                    .height(120.dp)

                FuncCard(
                    title = "教学班管理",
                    subtitle = "管理教学班",
                    icon = Icons.Default.Group,
                    modifier = cardModifier,
                    onClick = onNavigateToTeachingClassManagement
                )
                FuncCard(
                    title = "教室管理",
                    subtitle = "管理教室资源",
                    icon = Icons.Default.MeetingRoom,
                    modifier = cardModifier,
                    onClick = onNavigateToClassroomManagement
                )
            }
        }
        item {
            Row {
                val cardModifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
                    .height(120.dp)

                FuncCard(
                    title = "手动排课",
                    subtitle = "自定义排课",
                    icon = Icons.Default.EditCalendar,
                    modifier = cardModifier,
                    onClick = onNavigateToManualSchedule
                )
                FuncCard(
                    title = "快速排课管理",
                    subtitle = "智能排课",
                    icon = Icons.Default.Bolt,
                    modifier = cardModifier,
                    onClick = onNavigateToQuickScheduleManager
                )
            }
        }
        item {
            Row {
                val cardModifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
                    .height(120.dp)

                FuncCard(
                    title = "统计信息",
                    subtitle = "查看数据统计",
                    icon = Icons.Default.BarChart,
                    modifier = cardModifier,
                    onClick = onNavigateToStats
                )
                FuncCard(
                    title = "待开发...",
                    subtitle = "更多功能敬请期待",
                    icon = Icons.Default.Construction,
                    modifier = cardModifier,
                    onClick = {}
                )
            }
        }
    }
}

@Composable
fun FuncCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        shape = RoundedCornerShape(18.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(icon, contentDescription = title, modifier = Modifier.size(36.dp))
            Spacer(Modifier.height(8.dp))
            Text(title, style = MaterialTheme.typography.titleMedium)
            Text(subtitle, style = MaterialTheme.typography.bodySmall, color = Color.Gray)
        }
    }
}
