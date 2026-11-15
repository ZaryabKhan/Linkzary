package com.appcodecraft.linkzary.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.appcodecraft.linkzary.R
import com.appcodecraft.linkzary.data.entity.Collection
import com.appcodecraft.linkzary.ui.theme.collectionContainerDark
import com.appcodecraft.linkzary.ui.theme.collectionContainerLight
import com.appcodecraft.linkzary.ui.theme.collectionHue
import com.appcodecraft.linkzary.ui.theme.collectionOnContainerDark
import com.appcodecraft.linkzary.ui.theme.collectionOnContainerLight
import com.appcodecraft.linkzary.ui.theme.LinkzaryTheme

/**
 * The one glyph mapping: M3 rounded glyphs inside a hue-tinted tile. The
 * user-picked icon ALWAYS wins (bug build 2026-11-15); blank/Folder/unknown
 * falls back to the bookmark glyph.
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

/** Hue container/on-container pair for a collection id, theme-aware (spec v3 §1.1). */
@Composable
fun collectionHuePair(collectionId: Long): Pair<Color, Color> {
    val isDark = isSystemInDarkTheme()
    val accent = MaterialTheme.colorScheme.primary
    val hue = collectionHue(collectionId)
    return if (isDark) {
        collectionContainerDark(hue, accent) to collectionOnContainerDark(hue, accent)
    } else {
        collectionContainerLight(hue, accent) to collectionOnContainerLight(hue, accent)
    }
}

/**
 * CollectionCard v3 (spec v3 §2.4): per-collection hue identity + thumbnail
 * fan. Name + kebab on top; a fan of up to 3 real thumbnails (56dp, -6°,
 * overlapping 8dp) when filled; a hue-tinted glyph tile holding the
 * user-picked icon when empty — never a grey void. 4dp hue strip on the top
 * edge. Count pill bottom-right in collContainer.
 */
@Composable
fun CollectionCard(
    collection: Collection,
    linkCount: Int,
    onCardClick: () -> Unit,
    onMoreClick: () -> Unit,
    modifier: Modifier = Modifier,
    thumbnails: List<String> = emptyList()
) {
    val haptic = androidx.compose.ui.platform.LocalHapticFeedback.current
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val (hueContainer, hueOnContainer) = collectionHuePair(collection.id)

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
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        ) {
            // 4dp hue strip — subtle identity, no "shape party"
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(hueContainer)
            )

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                // Name + kebab
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = collection.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(
                        onClick = onMoreClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Filled.MoreVert,
                            contentDescription = stringResource(R.string.home_more_options),
                            modifier = Modifier.size(18.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Fan (filled) or hue glyph tile (empty)
                if (thumbnails.isNotEmpty()) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy((-8).dp)
                    ) {
                        thumbnails.take(3).forEachIndexed { index, url ->
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(url)
                                    .crossfade(true)
                                    .memoryCachePolicy(coil.request.CachePolicy.ENABLED)
                                    .diskCachePolicy(coil.request.CachePolicy.ENABLED)
                                    .build(),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(56.dp)
                                    .graphicsLayer { rotationZ = -6f * (index + 1) }
                                    .clip(RoundedCornerShape(12.dp))
                            )
                        }
                    }
                } else {
                    // Empty: 56dp hue tile holding the user-picked glyph
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(hueContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = getCollectionGlyph(collection.icon),
                            contentDescription = stringResource(R.string.collections_collection_icon),
                            tint = hueOnContainer,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Count pill bottom-right, hue-tinted
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    HueCountPill(hueContainer, hueOnContainer, linkCount)
                }
            }
        }
    }
}

@Composable
private fun HueCountPill(container: Color, onContainer: Color, count: Int) {
    androidx.compose.material3.Surface(
        shape = RoundedCornerShape(50),
        color = container
    ) {
        Text(
            text = if (count == 1) {
                stringResource(R.string.collections_link_count_single, count)
            } else {
                stringResource(R.string.collections_link_count_plural, count)
            },
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.SemiBold),
            color = onContainer,
            maxLines = 1,
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
        )
    }
}

/**
 * Home shelf mini card (spec v3 §3.1): 148dp wide, name + hue glyph tile +
 * count. The user-picked icon always shows on the tile.
 */
@Composable
fun SmallCollectionCard(
    collection: Collection,
    linkCount: Int,
    onCardClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val (hueContainer, hueOnContainer) = collectionHuePair(collection.id)
    Card(
        modifier = modifier
            .width(148.dp)
            .clickable { onCardClick() },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(hueContainer)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = collection.name,
                style = MaterialTheme.typography.titleSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(hueContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCollectionGlyph(collection.icon),
                        contentDescription = stringResource(R.string.collections_collection_icon),
                        tint = hueOnContainer,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.weight(1f))
                Box(
                    modifier = Modifier
                        .background(hueContainer, RoundedCornerShape(50))
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = linkCount.toString(),
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = hueOnContainer
                    )
                }
            }
        }
    }
}
