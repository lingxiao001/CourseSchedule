package com.example.coursescheduleapp.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.coursescheduleapp.viewmodel.PaginationState

/**
 * 通用分页控件
 */
@Composable
fun PaginationControls(
    paginationState: PaginationState,
    onPreviousPage: () -> Unit,
    onNextPage: () -> Unit,
    onPageChange: (Int) -> Unit
) {
    val themeColor = Color(0xFFB3E5FC)//可以传参获取全局通用颜色？

    val currentPage = paginationState.currentPage
    val totalPages = paginationState.totalPages
    Card(

        modifier = Modifier
            .fillMaxWidth() // 设置宽度为最大可用宽度
            .wrapContentHeight()
            .padding(0.dp), // 设置外边距
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp), // 设置阴影高度
        colors = CardDefaults.cardColors(
                containerColor = themeColor// 你想要的背景色
                )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth() // 设置宽度为最大可用宽度
                .padding(1.dp), // 设置外边距
            horizontalArrangement = Arrangement.Center, // 水平排列方式为两端对齐
            verticalAlignment = Alignment.CenterVertically // 垂直排列方式为居中对齐
        ) {
            // 总信息文本
            Text(
                text = "共 ${paginationState.totalElements} 条记录",
                style = MaterialTheme.typography.bodySmall, // 设置字体样式为 bodySmall
                color = MaterialTheme.colorScheme.onSurfaceVariant // 设置文本颜色
            )
            // 分页按钮
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp), // 水平排列方式为分散对齐，间距为8 dp
                verticalAlignment = Alignment.CenterVertically // 垂直排列方式为居中对齐
            ) {
                IconButton(
                    onClick = { if (currentPage > 0) onPreviousPage() },
                    enabled = currentPage > 0 // 仅当当前页码大于0时启用按钮
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "上一页") // 上一页图标
                }
                Text(
                    text = "${currentPage + 1} / $totalPages", // 显示当前页码和总页数
                    style = MaterialTheme.typography.bodyMedium // 设置字体样式为 bodyMedium
                )
                IconButton(
                    onClick = { if (currentPage < totalPages - 1) onNextPage() },
                    enabled = currentPage < totalPages - 1 // 仅当当前页码小于总页数减一时启用按钮
                ) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "下一页") // 下一页图标
                }
            }
        }
    }
} 