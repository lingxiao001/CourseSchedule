package com.example.coursescheduleapp.ui.components

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
    val currentPage = paginationState.currentPage
    val totalPages = paginationState.totalPages
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 总信息
            Text(
                text = "共 ${paginationState.totalElements} 条记录",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            // 分页按钮
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = { if (currentPage > 0) onPreviousPage() },
                    enabled = currentPage > 0
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "上一页")
                }
                Text(
                    text = "${currentPage + 1} / $totalPages",
                    style = MaterialTheme.typography.bodyMedium
                )
                IconButton(
                    onClick = { if (currentPage < totalPages - 1) onNextPage() },
                    enabled = currentPage < totalPages - 1
                ) {
                    Icon(Icons.Default.ArrowForward, contentDescription = "下一页")
                }
            }
        }
    }
} 