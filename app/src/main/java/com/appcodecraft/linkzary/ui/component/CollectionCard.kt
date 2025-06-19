package com.appcodecraft.linkzary.ui.component

import androidx.compose.animation.core.*
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.appcodecraft.linkzary.data.entity.Collection
import com.appcodecraft.linkzary.ui.theme.LinkzaryTheme
import java.util.*
import kotlin.math.abs
import androidx.core.graphics.toColorInt

// Helper function to create gradient from collection color
@Composable
fun createCollectionGradient(colorHex: String): Brush {
    val baseColor = Color(colorHex.toColorInt())
    val lighterColor = baseColor.copy(alpha = 0.7f)
    val darkerColor = baseColor.copy(alpha = 0.9f)
    
    return Brush.linearGradient(
        colors = listOf(lighterColor, darkerColor)
    )
}

// Helper function to get collection icon based on name
fun getCollectionIcon(name: String): ImageVector {
    return when {
        name.contains("tech", ignoreCase = true) || 
        name.contains("code", ignoreCase = true) || 
        name.contains("dev", ignoreCase = true) -> Icons.Default.Link
        else -> Icons.Default.Folder
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionCard(
    collection: Collection,
    linkCount: Int,
    onCardClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Animation states
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()
    val isPressed by interactionSource.collectIsPressedAsState()
    
    // Animated values
    val scale by animateFloatAsState(
        targetValue = when {
            isPressed -> 0.97f
            isHovered -> 1.02f
            else -> 1f
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "collection_card_scale"
    )
    
    val elevation by animateDpAsState(
        targetValue = when {
            isPressed -> 1.dp
            isHovered -> 6.dp
            else -> 2.dp
        },
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "collection_card_elevation"
    )
    
    val containerColor by animateColorAsState(
        targetValue = if (isHovered) 
            MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        else 
            MaterialTheme.colorScheme.surface,
        animationSpec = tween(durationMillis = 200),
        label = "collection_container_color"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onCardClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color indicator with gradient and icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(createCollectionGradient(collection.color)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCollectionIcon(collection.name),
                    contentDescription = "Collection icon",
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Collection info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = collection.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Text(
                    text = "$linkCount ${if (linkCount == 1) "link" else "links"}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            // More button
            IconButton(
                onClick = onMoreClick,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = "More options",
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun SmallCollectionCard(
    collection: Collection,
    linkCount: Int,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .width(120.dp)
            .clickable { onCardClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Color indicator with gradient and icon
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(createCollectionGradient(collection.color)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCollectionIcon(collection.name),
                    contentDescription = "Collection icon",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Collection name
            Text(
                text = collection.name,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            
            // Link count
            Text(
                text = linkCount.toString(),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}

@Preview
@Composable
fun CollectionCardPreview() {
    LinkzaryTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            CollectionCard(
                collection = Collection(
                    id = 1,
                    name = "Design Inspiration",
                    color = "#FF6B6B",
                    createdDate = Date()
                ),
                linkCount = 15,
                onCardClick = { },
                onMoreClick = { }
            )
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                SmallCollectionCard(
                    collection = Collection(
                        id = 2,
                        name = "Tech",
                        color = "#4ECDC4",
                        createdDate = Date()
                    ),
                    linkCount = 8,
                    onCardClick = { }
                )
                
                SmallCollectionCard(
                    collection = Collection(
                        id = 3,
                        name = "Articles",
                        color = "#45B7D1",
                        createdDate = Date()
                    ),
                    linkCount = 23,
                    onCardClick = { }
                )
            }
        }
    }
}