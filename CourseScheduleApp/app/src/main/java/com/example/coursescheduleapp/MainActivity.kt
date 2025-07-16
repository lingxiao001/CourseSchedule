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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.background

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val userJson = getSharedPreferences("user", Context.MODE_PRIVATE).getString("user_json", null)
        if (userJson != null) {
            val user = Gson().fromJson(userJson, User::class.java)
            val authResponse = AuthResponse(user = user)
            authViewModel.setCurrentUser(authResponse)
        }
        enableEdgeToEdge()
        setContent {
            CourseScheduleAppTheme {
                MainTabScreen(authViewModel)
            }
        }
    }
}

// 保持oldMainActivity的导航结构，但使用新的彩色卡片样式
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
                                    navController.popBackStack("main-home", inclusive = false)
                                }
                            }
                        )
                    }
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "main-home",
            modifier = Modifier.padding(innerPadding)
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
                        onNavigateToSchedule = { navController.navigate("schedule") }
                    )
                    1 -> MessageScreen()
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
        }
    }
}

// 保持oldMainActivity的HomeTabContent结构，但使用新的彩色卡片
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
    onNavigateToSchedule: () -> Unit = {}
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Surface(
            color = Color(0xFFB3E5FC),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .padding(top = 16.dp, bottom = 16.dp)
                .wrapContentHeight()
                .fillMaxWidth()
        ) {
            Text(
                text = "欢迎，${currentUser?.user?.realName ?: "用户"}！",
                style = MaterialTheme.typography.headlineMedium,
                color = Color(0xFF1565C0),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp)
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
                        onNavigateToSchedule = onNavigateToSchedule
                    )
                }
                "teacher" -> {
                    TeacherHomeContent(
                        onNavigateToMyCourses = onNavigateToMyCourses,
                        onNavigateToSchedule = onNavigateToSchedule
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
                ColorfulFuncCard(
                    title = "我的课表",
                    subtitle = "查看本学期全部课程安排",
                    icon = Icons.Default.CalendarMonth,
                    modifier = cardModifier,
                    onClick = onNavigateToSchedule,
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer
                )
                ColorfulFuncCard(
                    title = "选课系统",
                    subtitle = "浏览并选择课程",
                    icon = Icons.Default.MenuBook,
                    modifier = cardModifier,
                    onClick = onNavigateToCourses,
                    backgroundColor = MaterialTheme.colorScheme.secondaryContainer
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
    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(top = 8.dp)
    ) {
        item {
            Row {
                val cardModifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
                    .height(120.dp)
                ColorfulFuncCard(
                    title = "我的教学班",
                    subtitle = "管理教学班级",
                    icon = Icons.Default.Group,
                    modifier = cardModifier,
                    onClick = onNavigateToMyCourses,
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer
                )
                ColorfulFuncCard(
                    title = "我的课表",
                    subtitle = "查看教学安排",
                    icon = Icons.Default.CalendarMonth,
                    modifier = cardModifier,
                    onClick = onNavigateToSchedule,
                    backgroundColor = MaterialTheme.colorScheme.secondaryContainer
                )
            }
        }
    }
}

@Composable
fun AdminHomeContent(
    onNavigateToUserManagement: () -> Unit,
    onNavigateToCourseManagement: () -> Unit,
    onNavigateToTeachingClassManagement: () -> Unit,
    onNavigateToClassroomManagement: () -> Unit,
    onNavigateToStats: () -> Unit,
    onNavigateToManualSchedule: () -> Unit,
    onNavigateToQuickScheduleManager: () -> Unit
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

                ColorfulFuncCard(
                    title = "用户管理",
                    subtitle = "管理所有用户",
                    icon = Icons.Default.Person,
                    modifier = cardModifier,
                    onClick = onNavigateToUserManagement,
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer
                )
                ColorfulFuncCard(
                    title = "课程管理",
                    subtitle = "管理课程信息",
                    icon = Icons.Default.MenuBook,
                    modifier = cardModifier,
                    onClick = onNavigateToCourseManagement,
                    backgroundColor = MaterialTheme.colorScheme.secondaryContainer
                )
            }
        }
        item {
            Row {
                val cardModifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
                    .height(120.dp)

                ColorfulFuncCard(
                    title = "教学班管理",
                    subtitle = "管理教学班",
                    icon = Icons.Default.Group,
                    modifier = cardModifier,
                    onClick = onNavigateToTeachingClassManagement,
                    backgroundColor = MaterialTheme.colorScheme.tertiaryContainer
                )
                ColorfulFuncCard(
                    title = "教室管理",
                    subtitle = "管理教室资源",
                    icon = Icons.Default.MeetingRoom,
                    modifier = cardModifier,
                    onClick = onNavigateToClassroomManagement,
                    backgroundColor = MaterialTheme.colorScheme.errorContainer
                )
            }
        }
        item {
            Row {
                val cardModifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
                    .height(120.dp)

                ColorfulFuncCard(
                    title = "手动排课",
                    subtitle = "自定义排课",
                    icon = Icons.Default.EditCalendar,
                    modifier = cardModifier,
                    onClick = onNavigateToManualSchedule,
                    backgroundColor = MaterialTheme.colorScheme.primaryContainer
                )
                ColorfulFuncCard(
                    title = "快速排课管理",
                    subtitle = "智能排课",
                    icon = Icons.Default.Bolt,
                    modifier = cardModifier,
                    onClick = onNavigateToQuickScheduleManager,
                    backgroundColor = MaterialTheme.colorScheme.secondaryContainer
                )
            }
        }
        item {
            Row {
                val cardModifier = Modifier
                    .weight(1f)
                    .padding(8.dp)
                    .height(120.dp)

                ColorfulFuncCard(
                    title = "统计信息",
                    subtitle = "查看数据统计",
                    icon = Icons.Default.BarChart,
                    modifier = cardModifier,
                    onClick = onNavigateToStats,
                    backgroundColor = MaterialTheme.colorScheme.errorContainer
                )
                ColorfulFuncCard(
                    title = "待开发...",
                    subtitle = "更多功能敬请期待",
                    icon = Icons.Default.Construction,
                    modifier = cardModifier,
                    onClick = {},
                    backgroundColor = MaterialTheme.colorScheme.surfaceVariant
                )
            }
        }
    }
}

// 新的彩色功能卡片，融合新旧设计
@Composable
fun ColorfulFuncCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    backgroundColor: Color = MaterialTheme.colorScheme.primaryContainer
) {
    Card(
        modifier = modifier.clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = backgroundColor
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                icon, 
                contentDescription = title, 
                modifier = Modifier.size(40.dp),
                tint = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(12.dp))
            Text(
                title, 
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(Modifier.height(4.dp))
            Text(
                subtitle, 
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
            )
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