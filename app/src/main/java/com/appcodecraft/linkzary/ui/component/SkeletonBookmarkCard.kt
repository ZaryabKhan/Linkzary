package com.appcodecraft.linkzary.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp

/**
 * Shimmering placeholder card that mirrors [BookmarkCard] geometry exactly
 * (20dp corner radius, 16:9 preview block, favicon row, two title lines,
 * footer pill), so swapping skeletons for real cards does not jump (audit #2).
 * Shimmer = 1.2s alpha pulse on SurfaceVariant; no external library needed.
 */
@Composable
fun SkeletonBookmarkCard(
    modifier: Modifier = Modifier,
    isGrid: Boolean = false
) {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val alpha by transition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmerAlpha"
    )
    val shimmerColor = MaterialTheme.colorScheme.surfaceContainerHigh
    val strokeWidth = Modifier.height(14.dp)

    Surface(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLowest
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 16:9 preview block (4:3 in grid, matching the real card)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(if (isGrid) 4f / 3f else 16f / 9f)
                    .graphicsLayer { this.alpha = alpha }
                    .background(shimmerColor)
            )

            Column(modifier = Modifier.padding(16.dp)) {
                // Favicon + domain row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(20.dp)
                            .graphicsLayer { this.alpha = alpha }
                            .background(shimmerColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .graphicsLayer { this.alpha = alpha }
                            .size(width = 90.dp, height = 12.dp)
                            .background(shimmerColor, RoundedCornerShape(6.dp))
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Two title lines
                Box(
                    modifier = Modifier
                        .graphicsLayer { this.alpha = alpha }
                        .fillMaxWidth(0.75f)
                        .then(strokeWidth)
                        .background(shimmerColor, RoundedCornerShape(7.dp))
                )
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .graphicsLayer { this.alpha = alpha }
                        .fillMaxWidth(0.45f)
                        .then(strokeWidth)
                        .background(shimmerColor, RoundedCornerShape(7.dp))
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Footer: right-aligned pill where the relative time sits
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Box(
                        modifier = Modifier
                            .graphicsLayer { this.alpha = alpha }
                            .size(width = 40.dp, height = 14.dp)
                            .background(shimmerColor, RoundedCornerShape(7.dp))
                    )
                }
            }
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
fun SkeletonBookmarkCardPreview() {
    androidx.compose.material3.MaterialTheme {
        Column(
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            repeat(3) { SkeletonBookmarkCard() }
        }
    }
}
