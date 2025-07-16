package com.example.coursescheduleapp.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.TextSelectionColors
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.coursescheduleapp.ui.theme.*

@Composable
fun BeautifulCard(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String? = null,
    onClick: (() -> Unit)? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    content: @Composable ColumnScope.() -> Unit = {}
) {
    val cardBackgroundColor = when {
        onClick != null -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.1f)
        else -> backgroundColor
    }
    
    val borderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else Modifier
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBackgroundColor
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp,
            pressedElevation = 12.dp
        ),
        border = BorderStroke(
            width = 1.dp,
            color = borderColor
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.surface,
                            MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                        )
                    )
                )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.SemiBold
                )
                
                subtitle?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                content()
            }
        }
    }
}

@Composable
fun GradientButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    text: String,
    enabled: Boolean = true,
    gradient: Brush = Brush.horizontalGradient(
        colors = listOf(
            PrimaryLight,
            SecondaryLight,
            TertiaryLight
        )
    )
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .background(
                brush = gradient,
                shape = RoundedCornerShape(12.dp)
            ),
        enabled = enabled,
        colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            contentColor = Color.White
        ),
        shape = RoundedCornerShape(12.dp),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 6.dp,
            pressedElevation = 2.dp
        )
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold
        )
    }
}


@Composable
fun CommonSearchBar(
    query: String, // 当前搜索框内的文本值
    onQueryChange: (String) -> Unit, // 文本值改变时调用的函数
    placeholder: String = "Search...", // 搜索框内的占位符文本，默认为"Search..."
    modifier: Modifier = Modifier // 用于修改搜索框外观和行为的修饰符，默认不修改
) {
    OutlinedTextField( // 创建一个带有轮廓的文本字段
        value = query, // 设置搜索框的当前文本值
        onValueChange = onQueryChange, // 设置文本值改变时调用的函数
        modifier = modifier.fillMaxWidth(), // 使搜索框宽度充满最大可用宽度
        placeholder = { // 设置占位符文本及其样式
            Text(
                text = placeholder, // 占位符文本
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f) // 占位符颜色为onSurfaceVariant的70%透明度
            )
        },
        leadingIcon = { // 添加一个搜索图标
            Icon(
                imageVector = Icons.Default.Search, // 使用默认的搜索图标
                contentDescription = "搜索", // 图标的内容描述
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f) // 图标颜色为主色调的70%透明度
            )
        },
        shape = RoundedCornerShape(16.dp), // 设置搜索框的圆角形状为16.dp
        colors = OutlinedTextFieldDefaults.colors( // 设置搜索框的不同状态下的颜色
            focusedBorderColor = MaterialTheme.colorScheme.primary, // 聚焦时的边框颜色为主色调
            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), // 未聚焦时的边框颜色为outline的30%透明度
            focusedContainerColor = MaterialTheme.colorScheme.surface, // 聚焦时的容器颜色为surface
            unfocusedContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f), // 未聚焦时的容器颜色为surface的80%透明度
            cursorColor = MaterialTheme.colorScheme.primary, // 光标颜色为主色调
            selectionColors = TextSelectionColors( // 设置文本选择颜色
                handleColor = MaterialTheme.colorScheme.primary, // 选择手柄颜色为主色调
                backgroundColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.2f) // 选择背景颜色为主色调的20%透明度
            )
        ),
        singleLine = true // 设置搜索框为单行模式
    )
}


@Composable
fun CourseTimeSlot(
    time: String,
    course: String,
    room: String,
    instructor: String,
    modifier: Modifier = Modifier,
    color: Color = PrimaryLight
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(
            width = 2.dp,
            color = color.copy(alpha = 0.6f)
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            color.copy(alpha = 0.1f),
                            color.copy(alpha = 0.05f),
                            MaterialTheme.colorScheme.surface
                        )
                    )
                )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Text(
                        text = time,
                        style = MaterialTheme.typography.labelMedium,
                        color = color,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = course,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "$room • $instructor",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            color = color,
                            shape = RoundedCornerShape(6.dp)
                        )
                )
            }
        }
    }
}

@Composable
fun DayHeader(
    day: String,
    date: String,
    isToday: Boolean = false,
    modifier: Modifier = Modifier
) {
    val backgroundBrush = if (isToday) {
        Brush.horizontalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.primary,
                MaterialTheme.colorScheme.secondary
            )
        )
    } else {
        Brush.horizontalGradient(
            colors = listOf(
                MaterialTheme.colorScheme.surfaceVariant,
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
            )
        )
    }
    
    val textColor = if (isToday) {
        Color.White
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isToday) 8.dp else 4.dp
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(brush = backgroundBrush)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = day,
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = date,
                    style = MaterialTheme.typography.bodySmall,
                    color = textColor.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
fun StatusChip(
    text: String,
    status: ChipStatus,
    modifier: Modifier = Modifier
) {
    val (backgroundBrush, textColor) = when (status) {
        ChipStatus.SUCCESS -> Pair(
            Brush.horizontalGradient(
                colors = listOf(
                    SuccessLight.copy(alpha = 0.2f),
                    SuccessLight.copy(alpha = 0.1f)
                )
            ),
            SuccessLight
        )
        ChipStatus.WARNING -> Pair(
            Brush.horizontalGradient(
                colors = listOf(
                    WarningLight.copy(alpha = 0.2f),
                    WarningLight.copy(alpha = 0.1f)
                )
            ),
            WarningLight
        )
        ChipStatus.ERROR -> Pair(
            Brush.horizontalGradient(
                colors = listOf(
                    ErrorLight.copy(alpha = 0.2f),
                    ErrorLight.copy(alpha = 0.1f)
                )
            ),
            ErrorLight
        )
        ChipStatus.INFO -> Pair(
            Brush.horizontalGradient(
                colors = listOf(
                    InfoLight.copy(alpha = 0.2f),
                    InfoLight.copy(alpha = 0.1f)
                )
            ),
            InfoLight
        )
        ChipStatus.NEUTRAL -> Pair(
            Brush.horizontalGradient(
                colors = listOf(
                    MaterialTheme.colorScheme.surfaceVariant,
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.8f)
                )
            ),
            MaterialTheme.colorScheme.onSurfaceVariant
        )
    }

    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        color = Color.Transparent
    ) {
        Box(
            modifier = Modifier
                .background(brush = backgroundBrush)
                .padding(horizontal = 12.dp, vertical = 6.dp)
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                color = textColor,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

enum class ChipStatus {
    SUCCESS, WARNING, ERROR, INFO, NEUTRAL
}

@Composable
fun FloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable () -> Unit,
    contentDescription: String? = null
) {
    androidx.compose.material3.FloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        containerColor = MaterialTheme.colorScheme.primary,
        contentColor = MaterialTheme.colorScheme.onPrimary,
        elevation = FloatingActionButtonDefaults.elevation(
            defaultElevation = 12.dp,
            pressedElevation = 6.dp
        )
    ) {
        icon()
    }
}

@Composable
fun EmptyState(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    action: @Composable (() -> Unit)? = null
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            fontWeight = FontWeight.SemiBold
        )
        
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        
        action?.invoke()
    }
}