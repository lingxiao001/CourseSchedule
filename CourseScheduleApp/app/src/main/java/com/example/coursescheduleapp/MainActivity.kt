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
import androidx.compose.ui.graphics.Brush
import androidx.compose.foundation.background
import com.example.coursescheduleapp.ui.screens.*
import com.example.coursescheduleapp.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import androidx.compose.ui.unit.dp
import com.example.coursescheduleapp.ui.theme.CourseScheduleAppTheme
import com.example.coursescheduleapp.ui.components.BeautifulCard
import android.content.Context
import com.google.gson.Gson
import com.example.coursescheduleapp.model.User
import com.example.coursescheduleapp.model.AuthResponse
import androidx.compose.foundation.clickable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Message
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.coursescheduleapp.LocalCurrentUser
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.rememberNavController

@OptIn(ExperimentalAnimationApi::class)
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
            CompositionLocalProvider(LocalCurrentUser provides authViewModel.currentUser.collectAsState().value) {
                CourseScheduleAppTheme {
                    VueDashboardScreen(authViewModel)
                }
            }
        }
    }
}

// Simplified Vue-like Dashboard Screen
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun VueDashboardScreen(authViewModel: AuthViewModel) {
    val context = LocalContext.current
    val currentUser by authViewModel.currentUser.collectAsState()
    val navController = rememberNavController()
    var selectedTab by remember { mutableStateOf(0) }
    
    Scaffold(
        bottomBar = {
            NavigationBar(
                modifier = Modifier.height(72.dp),
                containerColor = MaterialTheme.colorScheme.surface
            ) {
                val items = when (currentUser?.user?.role) {
                    "student" -> listOf(
                        Triple("首页", Icons.Default.Home, "dashboard"),
                        Triple("课表", Icons.Default.CalendarMonth, "schedule"),
                        Triple("课程", Icons.Default.MenuBook, "courses"),
                        Triple("我的", Icons.Default.Person, "profile")
                    )
                    "teacher" -> listOf(
                        Triple("首页", Icons.Default.Home, "dashboard"),
                        Triple("课表", Icons.Default.CalendarMonth, "schedule"),
                        Triple("班级", Icons.Default.Group, "my-courses"),
                        Triple("我的", Icons.Default.Person, "profile")
                    )
                    "admin" -> listOf(
                        Triple("首页", Icons.Default.Home, "dashboard"),
                        Triple("管理", Icons.Default.Construction, "management"),
                        Triple("统计", Icons.Default.BarChart, "stats"),
                        Triple("我的", Icons.Default.Person, "profile")
                    )
                    else -> listOf(
                        Triple("首页", Icons.Default.Home, "dashboard"),
                        Triple("课表", Icons.Default.CalendarMonth, "schedule")
                    )
                }
                
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(item.second, contentDescription = item.first) },
                        label = { Text(item.first) },
                        selected = selectedTab == index,
                        onClick = { selectedTab = index }
                    )
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    )
                )
        ) {
            when (selectedTab) {
                0 -> DashboardHomeContent(currentUser)
                1 -> when (currentUser?.user?.role) {
                    "student" -> ScheduleScreen(onNavigateBack = { navController.popBackStack() })
                    "teacher" -> ScheduleScreen(onNavigateBack = { navController.popBackStack() })
                    "admin" -> AdminUserManagementScreen(onNavigateBack = { navController.popBackStack() })
                    else -> ScheduleScreen(onNavigateBack = { navController.popBackStack() })
                }
                2 -> when (currentUser?.user?.role) {
                    "student" -> CoursesScreen(onNavigateBack = { navController.popBackStack() })
                    "teacher" -> MyCoursesScreen(onNavigateBack = { navController.popBackStack() })
                    "admin" -> AdminStatsScreen(onNavigateBack = { navController.popBackStack() })
                    else -> CoursesScreen(onNavigateBack = { navController.popBackStack() })
                }
                3 -> {
                    LaunchedEffect(Unit) {
                        context.startActivity(Intent(context, PersonalCenterActivity::class.java))
                    }
                    DashboardHomeContent(currentUser)
                }
            }
        }
    }
}

@Composable
fun DashboardHomeContent(currentUser: AuthResponse?) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        // Welcome Header
        WelcomeHeader(currentUser)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Quick Stats
        QuickStatsSection(currentUser)
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Main Content Cards
        when (currentUser?.user?.role) {
            "student" -> StudentDashboardCards()
            "teacher" -> TeacherDashboardCards()
            "admin" -> AdminDashboardCards()
            else -> DefaultDashboardCards()
        }
    }
}

@Composable
fun WelcomeHeader(currentUser: AuthResponse?) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "欢迎, ${currentUser?.user?.realName ?: "用户"}",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "今天是个好日子!",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        
        Surface(
            modifier = Modifier.size(56.dp),
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = when (currentUser?.user?.role) {
                        "student" -> Icons.Default.MenuBook
                        "teacher" -> Icons.Default.Person
                        "admin" -> Icons.Default.Construction
                        else -> Icons.Default.Person
                    },
                    contentDescription = "User",
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
}

@Composable
fun QuickStatsSection(currentUser: AuthResponse?) {
    val stats = when (currentUser?.user?.role) {
        "student" -> listOf(
            "今日课程" to "4节",
            "本周课程" to "20节",
            "已选课程" to "6门"
        )
        "teacher" -> listOf(
            "今日教学" to "2节",
            "本周教学" to "12节",
            "教学班" to "3个"
        )
        "admin" -> listOf(
            "总用户" to "1,250",
            "课程" to "85门",
            "教室" to "45间"
        )
        else -> listOf("欢迎使用" to "系统")
    }
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        stats.forEach { (title, value) ->
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = value,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
fun StudentDashboardCards() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        BeautifulCard(
            title = "今日课程",
            subtitle = "查看今天的课程安排",
            onClick = { /* Navigate */ }
        ) {
            Text("今日有4节课", style = MaterialTheme.typography.bodyMedium)
        }
        
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.MenuBook, contentDescription = null)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("课程浏览", fontWeight = FontWeight.Bold)
                }
            }
            
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Person, contentDescription = null)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("我的课程", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun TeacherDashboardCards() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        BeautifulCard(
            title = "今日教学",
            subtitle = "今天的教学安排",
            onClick = { /* Navigate */ }
        ) {
            Text("今日有2节课", style = MaterialTheme.typography.bodyMedium)
        }
        
        BeautifulCard(
            title = "我的教学班",
            subtitle = "管理教学班级",
            onClick = { /* Navigate */ }
        ) {
            Text("3个教学班", style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Composable
fun AdminDashboardCards() {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Person, contentDescription = null)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("用户管理", fontWeight = FontWeight.Bold)
                }
            }
            
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.MenuBook, contentDescription = null)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("课程管理", fontWeight = FontWeight.Bold)
                }
            }
        }
        
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.Group, contentDescription = null)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("班级管理", fontWeight = FontWeight.Bold)
                }
            }
            
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer
                )
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(Icons.Default.BarChart, contentDescription = null)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("数据统计", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun DefaultDashboardCards() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "欢迎使用课表管理系统",
            style = MaterialTheme.typography.headlineMedium
        )
    }
}