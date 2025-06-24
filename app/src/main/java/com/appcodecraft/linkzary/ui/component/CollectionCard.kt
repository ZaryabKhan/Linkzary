package com.appcodecraft.linkzary.ui.component

// Animation imports removed for better performance
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.compose.ui.res.stringResource
import com.appcodecraft.linkzary.R
import com.appcodecraft.linkzary.data.entity.Collection
import com.appcodecraft.linkzary.ui.theme.LinkzaryTheme
import java.util.Date

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
    // Static interaction source for click handling only
    val interactionSource = remember { MutableInteractionSource() }
    
    // Static values for better performance
    val elevation = 2.dp
    val containerColor = MaterialTheme.colorScheme.surface

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onCardClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 4.dp,
            pressedElevation = 8.dp,
            hoveredElevation = 6.dp
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Enhanced color indicator with improved styling
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(collection.color.toColorInt()),
                                Color(collection.color.toColorInt()).copy(alpha = 0.8f)
                            )
                        )
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // Collection info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = collection.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                Text(
                    text = if (linkCount == 1) {
                        stringResource(R.string.collections_link_count_single, linkCount)
                    } else {
                        stringResource(R.string.collections_link_count_plural, linkCount)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                )
            }
            
            // More button
            IconButton(
                onClick = onMoreClick,
                modifier = Modifier.size(24.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.MoreVert,
                    contentDescription = stringResource(R.string.home_more_options),
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
                    contentDescription = stringResource(R.string.collections_collection_icon),
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