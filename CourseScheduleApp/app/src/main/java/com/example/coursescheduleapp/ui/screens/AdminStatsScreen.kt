package com.example.coursescheduleapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import com.example.coursescheduleapp.viewmodel.AdminStatsViewModel
import com.example.coursescheduleapp.viewmodel.StatsState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll

@Composable
fun AdminStatsScreen(
    onNavigateBack: () -> Unit,
    viewModel: AdminStatsViewModel = hiltViewModel()
) {
    val statsState by viewModel.statsState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onNavigateBack) {
                Icon(Icons.Default.ArrowBack, contentDescription = "返回")
            }
            Text("系统统计", style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(start = 8.dp))
        }
        Spacer(modifier = Modifier.height(16.dp))
        when (statsState) {
            is StatsState.Loading -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            is StatsState.Error -> {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text((statsState as StatsState.Error).message, color = MaterialTheme.colorScheme.error)
                }
            }
            is StatsState.Success -> {
                val stats = (statsState as StatsState.Success).stats
                val statsItems = listOf(
                    "userCount" to "用户总数",
                    "studentCount" to "学生总数",
                    "teacherCount" to "教师总数",
                    "courseCount" to "课程总数",
                    "classroomCount" to "教室总数",
                    "teachingClassCount" to "教学班总数"
                )
                // 添加滚动条
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
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
} 