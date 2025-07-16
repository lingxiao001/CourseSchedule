package com.example.coursescheduleapp.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack

@Composable
fun CommonTopBar(
    themColor: Color = Color(0xFFB3E5FC),
    title: String, // 顶部栏标题文本
    onBack: (() -> Unit)? = null, // 返回按钮点击事件处理函数，如果提供则显示返回按钮
    modifier: Modifier = Modifier, // 用于修改Row组件外观和行为的修饰符，默认不修改
    rightContent: @Composable (RowScope.() -> Unit)? = null // 右侧内容的可空lambda表达式，允许添加额外的组件
) {
    Row( // 使用Row组件水平排列子组件
        modifier = modifier // 应用传入的modifier
            .fillMaxWidth() // 使Row宽度充满最大可用宽度
            .background(themColor) // 设置背景颜色为
            .padding(horizontal = 2.dp, vertical = 0.dp), // 设置水平和垂直内边距
        verticalAlignment = Alignment.CenterVertically // 垂直居中对齐所有子组件
    ) {
        if (onBack != null) { // 如果提供了返回按钮的点击事件处理函数
            IconButton(onClick = onBack) { // 创建一个点击按钮
                Icon(Icons.Default.ArrowBack, contentDescription = "返回") // 使用默认的返回图标
            }
        }
        Text( // 显示标题文本
            text = title,
            style = MaterialTheme.typography.headlineSmall, // 使用MaterialTheme定义的headlineSmall字体样式
            modifier = Modifier.padding(start = if (onBack != null) 2.dp else 0.dp) // 如果有返回按钮，则设置左边距为4.dp，否则为0
        )
        Spacer(modifier = Modifier.weight(1f)) // 创建一个Spacer组件，占据剩余空间
        rightContent?.invoke(this) // 如果提供了右侧内容，则调用它以添加右侧组件
    }
}