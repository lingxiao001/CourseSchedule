package com.example.coursescheduleapp.ui.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.coursescheduleapp.model.CourseWithTeachingClassesDTO
import com.example.coursescheduleapp.viewmodel.*

/**
 * 课程列表界面
 * 按课程分组显示所有可选课程
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseListScreen(
    onNavigateBack: () -> Unit,
    onCourseClick: (CourseWithTeachingClassesDTO) -> Unit,
    studentId: Long,
    courseListViewModel: CourseListViewModel = hiltViewModel()
) {
    Log.d("CourseListScreen", "课程列表界面初始化，学生ID: $studentId")
    
    val coursesState by courseListViewModel.coursesState.collectAsState()
    var searchQuery by remember { mutableStateOf("") }
    
    // 第一次加载时获取课程列表
    LaunchedEffect(Unit) {
        Log.d("CourseListScreen", "开始加载课程列表")
        courseListViewModel.loadCourses(studentId)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("选课中心") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 搜索栏
            SearchBar(
                query = searchQuery,
                onQueryChange = { searchQuery = it },
                onSearch = {},
                active = false,
                onActiveChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("搜索课程名称") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "搜索") }
            ) {}
            
            // 课程列表内容
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f)
            ) {
                when (coursesState) {
                    is CourseListState.Loading -> {
                        Log.d("CourseListScreen", "正在加载课程列表")
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center
                        ) {
                            CircularProgressIndicator()
                        }
                    }
                    is CourseListState.Success -> {
                        val courses = (coursesState as CourseListState.Success).courses
                        Log.d("CourseListScreen", "成功加载 ${courses.size} 门课程")
                        
                        // 根据搜索关键词过滤课程
                        val filteredCourses = courses.filter { course ->
                            course.courseName.contains(searchQuery, ignoreCase = true) ||
                            course.courseCode.contains(searchQuery, ignoreCase = true)
                        }
                        
                        if (filteredCourses.isEmpty()) {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (searchQuery.isNotEmpty()) {
                                        "未找到匹配的课程"
                                    } else {
                                        "暂无可选课程"
                                    },
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        } else {
                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                items(filteredCourses) { course ->
                                    CourseCard(
                                        course = course,
                                        onClick = { onCourseClick(course) }
                                    )
                                }
                            }
                        }
                    }
                    is CourseListState.Error -> {
                        val errorMessage = (coursesState as CourseListState.Error).message
                        Log.e("CourseListScreen", "加载课程列表失败: $errorMessage")
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(32.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = "加载失败",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = errorMessage,
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                            Button(
                                onClick = {
                                    Log.d("CourseListScreen", "用户点击重新加载")
                                    courseListViewModel.loadCourses(studentId)
                                }
                            ) {
                                Text("重新加载")
                            }
                        }
                    }
                    else -> {}
                }
            }
        }
    }
}

/**
 * 课程卡片组件
 * @param course 课程数据
 * @param onClick 点击回调
 */
@Composable
fun CourseCard(
    course: CourseWithTeachingClassesDTO,
    onClick: () -> Unit
) {
    Log.d("CourseCard", "渲染课程卡片: ${course.courseName}")
    
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 课程名称和学分
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = course.courseName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Text(
                        text = "课程代码: ${course.courseCode}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                // 学分卡片
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                    )
                ) {
                    Text(
                        text = "${course.credit}学分",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                }
            }
            
            // 课程描述
            course.description?.let { description ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2
                )
            }
            
            Spacer(modifier = Modifier.height(12.dp))
            
            // 教学班数量和选择状态
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "共${course.totalTeachingClasses}个教学班",
                    style = MaterialTheme.typography.bodyMedium
                )
                
                // 选择状态指示器
                when {
                    course.isFullySelected -> {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Text(
                                text = "可选",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    course.isPartiallySelected -> {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.secondaryContainer
                            )
                        ) {
                            Text(
                                text = "已选${course.selectedTeachingClasses}/${course.totalTeachingClasses}",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer
                            )
                        }
                    }
                    else -> {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                text = "未选择",
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}