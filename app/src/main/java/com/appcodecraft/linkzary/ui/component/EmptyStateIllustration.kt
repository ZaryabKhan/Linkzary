package com.appcodecraft.linkzary.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp

/**
 * Empty-state illustration per mockup 01 (audit #1): three layered link-card
 * shapes with a bookmark badge, drawn with theme colors so it works in dark
 * mode. Sized to the 160dp spec max.
 */
@Composable
fun EmptyStateIllustration(modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val surface = MaterialTheme.colorScheme.surface
    val onPrimary = MaterialTheme.colorScheme.onPrimary

    Box(
        modifier = modifier.size(width = 160.dp, height = 128.dp),
        contentAlignment = Alignment.Center
    ) {
        // Back layer
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = (-8).dp, y = 2.dp)
                .rotate(-6f)
                .size(width = 132.dp, height = 92.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(primaryContainer.copy(alpha = 0.55f))
        )
        // Middle layer
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(x = 6.dp, y = (-2).dp)
                .rotate(4f)
                .size(width = 132.dp, height = 92.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(primaryContainer)
        )
        // Front card: image block + two text lines + chip row
        Box(
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = 2.dp)
                .size(width = 138.dp, height = 96.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(surface)
        ) {
            Column(modifier = Modifier.align(Alignment.Center)) {
                Box(
                    modifier = Modifier
                        .size(width = 114.dp, height = 40.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(primaryContainer.copy(alpha = 0.45f))
                )
                Box(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier
                        .size(width = 66.dp, height = 10.dp)
                        .clip(RoundedCornerShape(5.dp))
                        .background(primary.copy(alpha = 0.5f))
                )
            }
        }
        // Bookmark badge, bottom-right of the front card
        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 6.dp, y = 2.dp)
                .size(44.dp)
                .clip(CircleShape)
                .background(primary),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Bookmark,
                contentDescription = null,
                tint = onPrimary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
