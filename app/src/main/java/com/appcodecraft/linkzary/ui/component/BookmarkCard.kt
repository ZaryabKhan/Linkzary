package com.appcodecraft.linkzary.ui.component

// Animation imports removed for better performance
import androidx.compose.foundation.background
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
// Scale import removed
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.toColorInt
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.appcodecraft.linkzary.data.entity.SavedLink
import com.appcodecraft.linkzary.ui.theme.LinkzaryTheme
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookmarkCard(
    modifier: Modifier = Modifier,
    link: SavedLink,
    collectionName: String? = null,
    collectionColor: String? = null,
    onCardClick: () -> Unit,
    onMoreClick: () -> Unit
) {
    // Static interaction source for click handling only
    val interactionSource = remember { MutableInteractionSource() }
    
    // Static values for better performance
    val elevation = 1.dp
    
    // Enhanced card background with theme awareness
    val cardBackgroundColor = MaterialTheme.colorScheme.surface

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(
                interactionSource = interactionSource,
                indication = null
            ) { onCardClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = cardBackgroundColor
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
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header with collection badge and more button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                // Enhanced collection badge with better contrast
                if (collectionName != null && collectionColor != null) {
                    Box(
                        modifier = Modifier
                            .background(
                                color = Color(collectionColor.toColorInt()).copy(alpha = 0.9f),
                                shape = RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = collectionName,
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.White,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                // More button and pin indicator
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (link.isPinned) {
                        Icon(
                            imageVector = Icons.Default.PushPin,
                            contentDescription = "Pinned",
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    IconButton(
                        onClick = onMoreClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options",
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Enhanced domain thumbnail with theme-aware background
            val thumbnailBackgroundColor = when (link.id.toInt() % 5) {
                0 -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
                1 -> MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.3f)
                2 -> MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.3f)
                3 -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                else -> MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.2f)
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(thumbnailBackgroundColor),
                contentAlignment = Alignment.Center
            ) {
                // Generate a simple domain preview
                val domain = extractDomain(link.url)
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = domain.take(2).uppercase(),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = domain,
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Title with compact styling
            Text(
                text = link.title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold,
                    lineHeight = 20.sp
                ),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                color = MaterialTheme.colorScheme.onSurface
            )

            // Note if present - very compact
            if (link.note.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = link.note,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // Tags - compact horizontal scrolling
            if (link.tags.isNotBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    link.tags.split(",").take(2).forEach { tag ->
                        if (tag.isNotBlank()) {
                            Text(
                                text = "#${tag.trim()}",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Medium
                                ),
                                color = MaterialTheme.colorScheme.primary,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Compact footer with date only
            Text(
                text = SimpleDateFormat("MMM dd", Locale.getDefault()).format(link.saveDate),
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}

/**
 * Extract domain name from URL for display
 */
fun extractDomain(url: String): String {
    return try {
        val domain = url.substringAfter("://").substringBefore("/")
        domain.removePrefix("www.")
    } catch (e: Exception) {
        url
    }
}

@Preview
@Composable
fun BookmarkCardPreview() {
    LinkzaryTheme {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 2x2 Grid simulation
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BookmarkCard(
                    modifier = Modifier.weight(1f),
                    link = SavedLink(
                        id = 1,
                        title = "Beautiful UI Design Inspiration",
                        url = "https://dribbble.com/shots/example",
                        note = "Great examples of modern design",
                        tags = "design, ui",
                        isPinned = true,
                        saveDate = Date()
                    ),
                    collectionName = "Design",
                    collectionColor = "#FF6B6B",
                    onCardClick = { },
                    onMoreClick = { }
                )

                BookmarkCard(
                    modifier = Modifier.weight(1f),
                    link = SavedLink(
                        id = 2,
                        title = "Advanced React Patterns",
                        url = "https://github.com/react-patterns",
                        note = "",
                        tags = "react, programming",
                        isPinned = false,
                        saveDate = Date()
                    ),
                    collectionName = "Development",
                    collectionColor = "#4ECDC4",
                    onCardClick = { },
                    onMoreClick = { }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                BookmarkCard(
                    modifier = Modifier.weight(1f),
                    link = SavedLink(
                        id = 3,
                        title = "Color Theory Guide",
                        url = "https://colorhunt.co/guide",
                        note = "Essential color combinations",
                        tags = "colors, theory",
                        isPinned = false,
                        saveDate = Date()
                    ),
                    collectionName = "Resources",
                    collectionColor = "#95E1D3",
                    onCardClick = { },
                    onMoreClick = { }
                )

                BookmarkCard(
                    modifier = Modifier.weight(1f),
                    link = SavedLink(
                        id = 4,
                        title = "Typography Best Practices",
                        url = "https://fonts.google.com/knowledge",
                        note = "",
                        tags = "fonts, typography",
                        isPinned = true,
                        saveDate = Date()
                    ),
                    collectionName = "Learning",
                    collectionColor = "#F38BA8",
                    onCardClick = { },
                    onMoreClick = { }
                )
            }
        }
    }
}