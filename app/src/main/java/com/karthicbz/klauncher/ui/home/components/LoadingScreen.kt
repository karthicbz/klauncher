package com.karthicbz.klauncher.ui.home.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.dp
//import androidx.tv.foundation.lazy.list.TvLazyColumn
import androidx.tv.material3.*

/**
 * Skeleton loading screen shown before app data is ready.
 * Uses a sweep gradient shimmer that travels left-to-right.
 */
@OptIn(ExperimentalTvMaterial3Api::class)
@Composable
fun LoadingScreen(modifier: Modifier = Modifier) {
    val shimmer = rememberInfiniteTransition(label = "skeleton_shimmer")
    val translateX by shimmer.animateFloat(
        initialValue = -300f,
        targetValue = 2000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "skeleton_translate"
    )

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(48.dp),
        verticalArrangement = Arrangement.spacedBy(28.dp)
    ) {
        repeat(3) {
            item {
                Column {
                    // Category title placeholder
                    Box(
                        modifier = Modifier
                            .size(180.dp, 24.dp)
                            .skeletonPlaceholder(translateX, MaterialTheme.colorScheme.onSurface)
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    // App card placeholders
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        items(5) {
                            Box(
                                modifier = Modifier
                                    .size(160.dp, 90.dp)
                                    .skeletonPlaceholder(translateX, MaterialTheme.colorScheme.onSurface)
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalTvMaterial3Api::class)
private fun Modifier.skeletonPlaceholder(
    translateX: Float,
    baseColor: androidx.compose.ui.graphics.Color
): Modifier = this.background(
    Brush.linearGradient(
        colors = listOf(
            baseColor.copy(alpha = 0.10f),
            baseColor.copy(alpha = 0.30f),
            baseColor.copy(alpha = 0.10f)
        ),
        start = Offset(translateX, 0f),
        end = Offset(translateX + 200f, 0f)
    )
)
