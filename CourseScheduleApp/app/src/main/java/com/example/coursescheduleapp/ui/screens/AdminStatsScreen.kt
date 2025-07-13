package com.example.coursescheduleapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.coursescheduleapp.network.ApiService
import kotlinx.coroutines.launch
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.platform.LocalContext
import dagger.hilt.android.EntryPointAccessors

@Composable
fun AdminStatsScreen(onNavigateBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var stats by remember { mutableStateOf<Map<String, Long>>(emptyMap()) }
    var loading by remember { mutableStateOf(true) }
    var error by remember { mutableStateOf<String?>(null) }

    // 获取ApiService
    val apiService = EntryPointAccessors.fromApplication<ApiService>(context.applicationContext)

    LaunchedEffect(Unit) {
        loading = true
        error = null
        try {
            val response = apiService.getStats()
            if (response.isSuccessful) {
                stats = response.body() ?: emptyMap()
            } else {
                error = "获取统计失败: ${response.code()}"
            }
        } catch (e: Exception) {
            error = e.message
        } finally {
            loading = false
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "返回")
            }
            Text("系统统计", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(start = 8.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        if (loading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else if (error != null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text(error ?: "未知错误", color = MaterialTheme.colorScheme.error)
            }
        } else {
            val statsItems = listOf(
                "userCount" to "用户总数",
                "studentCount" to "学生总数",
                "teacherCount" to "教师总数",
                "courseCount" to "课程总数",
                "classroomCount" to "教室总数",
                "teachingClassCount" to "教学班总数"
            )
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                statsItems.forEach { (key, label) ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(label, style = MaterialTheme.typography.titleMedium)
                            Text(
                                text = stats[key]?.toString() ?: "-",
                                style = MaterialTheme.typography.headlineLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
} 