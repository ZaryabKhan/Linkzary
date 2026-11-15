package com.appcodecraft.linkzary.ui.component

// Animation imports removed for better performance
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
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
import androidx.compose.material.icons.rounded.Book
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.Code
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Flight
import androidx.compose.material.icons.rounded.Gamepad
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Map
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.School
import androidx.compose.material.icons.rounded.ShoppingCart
import androidx.compose.material.icons.rounded.SportsSoccer
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Work
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import com.appcodecraft.linkzary.R
import com.appcodecraft.linkzary.data.entity.Collection
import com.appcodecraft.linkzary.ui.theme.LinkzaryTheme
import java.util.Date

/**
 * The one glyph mapping (spec v2 §4, audit D4): M3 rounded glyphs inside a
 * harmonized tonal tile, glyph colored onContainer — no stock-blue folder
 * anywhere. "Unsorted" collections get a bookmark glyph in primary, not a
 * folder.
 */
fun getCollectionGlyph(iconName: String?): ImageVector {
    if (iconName.isNullOrBlank() || iconName == "Folder") {
        return Icons.Rounded.Bookmark
    }
    return when (iconName) {
        "Work" -> Icons.Rounded.Work
        "School" -> Icons.Rounded.School
        "Home" -> Icons.Rounded.Home
        "Star" -> Icons.Rounded.Star
        "Favorite" -> Icons.Rounded.Favorite
        "Code" -> Icons.Rounded.Code
        "Book" -> Icons.Rounded.Book
        "Music" -> Icons.Rounded.MusicNote
        "Movie" -> Icons.Rounded.Movie
        "Image" -> Icons.Rounded.Image
        "Map" -> Icons.Rounded.Map
        "Shopping" -> Icons.Rounded.ShoppingCart
        "Travel" -> Icons.Rounded.Flight
        "Sports" -> Icons.Rounded.SportsSoccer
        "Game" -> Icons.Rounded.Gamepad
        else -> Icons.Rounded.Bookmark
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
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current

    // Addendum (2026-11-15) #1/#2: shape party REVOKED — one card shape, one
    // fixed icon slot for every collection. Icon color = brand indigo tonal
    // family only (primaryContainer/onPrimaryContainer); no per-card hues.
    // #3: '0 links' collections get a visibly muted treatment.
    val isMuted = linkCount == 0
    val tileContainer = if (isMuted) {
        MaterialTheme.colorScheme.surfaceContainerHigh
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    val onTileContainer = if (isMuted) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }
    val titleColor = if (isMuted) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.onSurface
    }
    val countColor = if (isMuted) {
        MaterialTheme.colorScheme.outline
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onCardClick,
                onLongClick = {
                    haptic.performHapticFeedback(androidx.compose.ui.hapticfeedback.HapticFeedbackType.LongPress)
                    onMoreClick()
                }
            ),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            // Spec v2 §3: harmonized container tone on surfaceContainerLowest-tier card
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Fixed icon slot (addendum #1): 48dp tile, fixed 16dp-corner
            // shape, 24dp glyph centered — content can never overflow.
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(tileContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCollectionGlyph(collection.icon),
                    contentDescription = null,
                    tint = onTileContainer,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Name: two lines allowed so it never truncates below 2 words at
            // grid width (spec v2 §2.6); the kebab moved to long-press.
            Text(
                text = collection.name,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = titleColor,
                maxLines = 2,
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
                color = countColor
            )
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
    // Addendum #2: brand indigo tonal family only for icon tiles
    val tileContainer = MaterialTheme.colorScheme.primaryContainer
    val onTileContainer = MaterialTheme.colorScheme.onPrimaryContainer
    Card(
        modifier = modifier
            .width(130.dp)
            .clickable { onCardClick() },
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(tileContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = getCollectionGlyph(collection.icon),
                    contentDescription = stringResource(R.string.collections_collection_icon),
                    tint = onTileContainer,
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
                text = if (linkCount == 1) {
                    stringResource(R.string.collections_link_count_single, linkCount)
                } else {
                    stringResource(R.string.collections_link_count_plural, linkCount)
                },
                style = MaterialTheme.typography.bodyMedium,
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