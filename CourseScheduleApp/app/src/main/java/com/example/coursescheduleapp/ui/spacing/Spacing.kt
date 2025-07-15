package com.example.coursescheduleapp.ui.spacing

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class Spacing(
    val extraSmall: Dp = 4.dp,
    val small: Dp = 8.dp,
    val medium: Dp = 16.dp,
    val large: Dp = 24.dp,
    val extraLarge: Dp = 32.dp,
    val huge: Dp = 48.dp,
    val massive: Dp = 64.dp,
    
    // Screen spacing
    val screenHorizontal: Dp = 16.dp,
    val screenVertical: Dp = 16.dp,
    
    // Component spacing
    val cardPadding: Dp = 16.dp,
    val cardSpacing: Dp = 8.dp,
    val listItemPadding: Dp = 12.dp,
    val buttonPadding: Dp = 16.dp,
    val chipPadding: Dp = 8.dp,
    
    // Section spacing
    val sectionSpacing: Dp = 24.dp,
    val componentSpacing: Dp = 12.dp,
    val itemSpacing: Dp = 8.dp,
    
    // Grid spacing
    val gridSpacing: Dp = 12.dp,
    val columnSpacing: Dp = 16.dp,
    
    // Text spacing
    val textLineSpacing: Dp = 4.dp,
    val paragraphSpacing: Dp = 16.dp,
    
    // Icon spacing
    val iconTextSpacing: Dp = 8.dp,
    val iconButtonSpacing: Dp = 12.dp
)

val LocalSpacing = staticCompositionLocalOf { Spacing() }

object AppSpacing {
    val current: Spacing
        @Composable
        get() = LocalSpacing.current
}

// Padding helpers
object PaddingHelpers {
    val screenPadding
        @Composable
        get() = PaddingValues(
            horizontal = AppSpacing.current.screenHorizontal,
            vertical = AppSpacing.current.screenVertical
        )
    
    val cardPadding
        @Composable
        get() = PaddingValues(AppSpacing.current.cardPadding)
    
    val listItemPadding
        @Composable
        get() = PaddingValues(
            horizontal = AppSpacing.current.listItemPadding,
            vertical = AppSpacing.current.listItemPadding / 2
        )
    
    val buttonPadding
        @Composable
        get() = PaddingValues(
            horizontal = AppSpacing.current.buttonPadding,
            vertical = AppSpacing.current.buttonPadding / 2
        )
    
    val sectionPadding
        @Composable
        get() = PaddingValues(
            top = AppSpacing.current.sectionSpacing,
            bottom = AppSpacing.current.medium
        )
}

// Layout constants
object LayoutConstants {
    val minTouchTargetSize = 48.dp
    val maxCardWidth = 600.dp
    val maxContentWidth = 800.dp
    val fabSize = 56.dp
    val appBarHeight = 64.dp
    val bottomNavHeight = 80.dp
    val tabHeight = 48.dp
    
    // Course schedule specific
    val dayHeaderHeight = 48.dp
    val timeSlotHeight = 80.dp
    val courseCardMinHeight = 60.dp
    val scheduleGridSpacing = 8.dp
}

// Responsive breakpoints
object Breakpoints {
    val compact = 600.dp
    val medium = 840.dp
    val expanded = 1200.dp
}