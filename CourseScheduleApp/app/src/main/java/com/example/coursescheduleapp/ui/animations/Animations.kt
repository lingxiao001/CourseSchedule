package com.example.coursescheduleapp.ui.animations

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.graphicsLayer

// Smooth easing curves
val smoothEasing = CubicBezierEasing(0.25f, 0.1f, 0.25f, 1f)
val bounceEasing = CubicBezierEasing(0.68f, -0.55f, 0.265f, 1.55f)
val quickEasing = CubicBezierEasing(0.4f, 0.0f, 0.2f, 1f)

// Common animation durations
object AnimationDurations {
    const val QUICK = 150
    const val NORMAL = 300
    const val SLOW = 500
    const val EXTRA_SLOW = 750
}

@Composable
fun SlideInFromBottom(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(
                durationMillis = AnimationDurations.NORMAL,
                easing = smoothEasing
            )
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = AnimationDurations.NORMAL,
                easing = smoothEasing
            )
        ),
        exit = slideOutVertically(
            targetOffsetY = { it },
            animationSpec = tween(
                durationMillis = AnimationDurations.QUICK,
                easing = quickEasing
            )
        ) + fadeOut(
            animationSpec = tween(
                durationMillis = AnimationDurations.QUICK,
                easing = quickEasing
            )
        ),
        modifier = modifier
    ) {
        content()
    }
}

@Composable
fun SlideInFromRight(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInHorizontally(
            initialOffsetX = { it },
            animationSpec = tween(
                durationMillis = AnimationDurations.NORMAL,
                easing = smoothEasing
            )
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = AnimationDurations.NORMAL,
                easing = smoothEasing
            )
        ),
        exit = slideOutHorizontally(
            targetOffsetX = { it },
            animationSpec = tween(
                durationMillis = AnimationDurations.QUICK,
                easing = quickEasing
            )
        ) + fadeOut(
            animationSpec = tween(
                durationMillis = AnimationDurations.QUICK,
                easing = quickEasing
            )
        ),
        modifier = modifier
    ) {
        content()
    }
}

@Composable
fun ScaleInAnimation(
    visible: Boolean,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(
            initialScale = 0.8f,
            animationSpec = tween(
                durationMillis = AnimationDurations.NORMAL,
                easing = bounceEasing
            )
        ) + fadeIn(
            animationSpec = tween(
                durationMillis = AnimationDurations.NORMAL,
                easing = smoothEasing
            )
        ),
        exit = scaleOut(
            targetScale = 0.8f,
            animationSpec = tween(
                durationMillis = AnimationDurations.QUICK,
                easing = quickEasing
            )
        ) + fadeOut(
            animationSpec = tween(
                durationMillis = AnimationDurations.QUICK,
                easing = quickEasing
            )
        ),
        modifier = modifier
    ) {
        content()
    }
}

@Composable
fun FadeInAnimation(
    visible: Boolean,
    modifier: Modifier = Modifier,
    delayMillis: Int = 0,
    content: @Composable () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(
            animationSpec = tween(
                durationMillis = AnimationDurations.NORMAL,
                delayMillis = delayMillis,
                easing = smoothEasing
            )
        ),
        exit = fadeOut(
            animationSpec = tween(
                durationMillis = AnimationDurations.QUICK,
                easing = quickEasing
            )
        ),
        modifier = modifier
    ) {
        content()
    }
}

@Composable
fun Modifier.bounceClick(): Modifier {
    var isPressed by remember { mutableStateOf(false) }
    
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1f,
        animationSpec = tween(
            durationMillis = AnimationDurations.QUICK,
            easing = quickEasing
        )
    )
    
    return this
        .scale(scale)
        .graphicsLayer {
            scaleX = scale
            scaleY = scale
        }
}

@Composable
fun Modifier.shimmerEffect(): Modifier {
    var startAnimation by remember { mutableStateOf(false) }
    
    val transition = rememberInfiniteTransition()
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1500,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        )
    )
    
    LaunchedEffect(Unit) {
        startAnimation = true
    }
    
    return this.graphicsLayer {
        translationX = translateAnim
    }
}

@Composable
fun StaggeredAnimation(
    itemCount: Int,
    modifier: Modifier = Modifier,
    staggerDelay: Int = 100,
    content: @Composable (index: Int) -> Unit
) {
    repeat(itemCount) { index ->
        FadeInAnimation(
            visible = true,
            delayMillis = index * staggerDelay,
            modifier = modifier
        ) {
            content(index)
        }
    }
}

@Composable
fun PulsatingAnimation(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = smoothEasing
            ),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Box(
        modifier = modifier.scale(scale)
    ) {
        content()
    }
}

@Composable
fun SwipeAnimation(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()
    val offsetX by infiniteTransition.animateFloat(
        initialValue = -50f,
        targetValue = 50f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 2000,
                easing = smoothEasing
            ),
            repeatMode = RepeatMode.Reverse
        )
    )
    
    Box(
        modifier = modifier.graphicsLayer {
            translationX = offsetX
        }
    ) {
        content()
    }
}

// Custom page transition animations
@OptIn(ExperimentalAnimationApi::class)
fun slideInFromRightTransition(): ContentTransform {
    return slideInHorizontally(
        initialOffsetX = { it },
        animationSpec = tween(
            durationMillis = AnimationDurations.NORMAL,
            easing = smoothEasing
        )
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = AnimationDurations.NORMAL,
            easing = smoothEasing
        )
    ) with slideOutHorizontally(
        targetOffsetX = { -it },
        animationSpec = tween(
            durationMillis = AnimationDurations.NORMAL,
            easing = smoothEasing
        )
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = AnimationDurations.NORMAL,
            easing = smoothEasing
        )
    )
}

@OptIn(ExperimentalAnimationApi::class)
fun slideUpTransition(): ContentTransform {
    return slideInVertically(
        initialOffsetY = { it },
        animationSpec = tween(
            durationMillis = AnimationDurations.NORMAL,
            easing = smoothEasing
        )
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = AnimationDurations.NORMAL,
            easing = smoothEasing
        )
    ) with slideOutVertically(
        targetOffsetY = { -it },
        animationSpec = tween(
            durationMillis = AnimationDurations.NORMAL,
            easing = smoothEasing
        )
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = AnimationDurations.NORMAL,
            easing = smoothEasing
        )
    )
}