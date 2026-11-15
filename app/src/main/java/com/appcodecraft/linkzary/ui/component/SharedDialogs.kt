package com.appcodecraft.linkzary.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.appcodecraft.linkzary.R
import com.appcodecraft.linkzary.data.entity.Collection
import com.appcodecraft.linkzary.data.entity.SavedLink

@Composable
fun OptionItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(24.dp)
            )

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (isDestructive) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = if (isDestructive) MaterialTheme.colorScheme.error.copy(alpha = 0.7f) else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun CollectionOptionItem(
    icon: ImageVector,
    name: String,
    color: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) {
            MaterialTheme.colorScheme.primaryContainer
        } else {
            MaterialTheme.colorScheme.surface
        },
        border = if (isSelected) {
            BorderStroke(2.dp, MaterialTheme.colorScheme.primary)
        } else {
            BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .background(
                        // Addendum #2: brand indigo tonal family only
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )

            Spacer(modifier = Modifier.weight(1f))

            if (isSelected) {
                Icon(
                    imageVector = Icons.Rounded.Check,
                    contentDescription = stringResource(R.string.cd_selected),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun EditLinkDialog(
    link: SavedLink,
    onDismiss: () -> Unit,
    onSave: (SavedLink) -> Unit,
    allTags: List<String> = emptyList()
) {
    var title by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(link.title) }
    var url by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(link.url) }
    var note by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(link.note) }
    var tags by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(link.tags) }
    var customImageUrl by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf(link.customImageUrl.orEmpty()) }

    LinkzaryDialog(
        onDismissRequest = onDismiss,
        icon = Icons.Rounded.Edit,
        title = stringResource(R.string.edit_link_title),
        confirmLabel = stringResource(R.string.common_save),
        onConfirm = {
            onSave(
                link.copy(
                    title = title,
                    url = url,
                    note = note,
                    tags = tags,
                    customImageUrl = customImageUrl.trim().takeIf { it.isNotBlank() }
                )
            )
        },
        confirmEnabled = title.isNotBlank() && url.isNotBlank(),
        dismissLabel = stringResource(R.string.common_cancel),
        onDismiss = onDismiss
    ) {
        LinkEditorForm(
            title = title,
            onTitleChange = { title = it },
            url = url,
            onUrlChange = { url = it },
            note = note,
            onNoteChange = { note = it },
            tags = tags,
            onTagsChange = { tags = it },
            allTags = allTags,
            customImageUrl = customImageUrl,
            onCustomImageUrlChange = { customImageUrl = it }
        )
    }
}

@Composable
fun CollectionSelectionDialog(
    currentCollectionId: Long?,
    allCollections: List<Collection>,
    onDismiss: () -> Unit,
    onCollectionSelected: (Long?) -> Unit,
    onCreateNew: () -> Unit
) {
    LinkzaryDialog(
        onDismissRequest = onDismiss,
        icon = Icons.Rounded.FolderOpen,
        title = stringResource(R.string.move_link_title),
        confirmLabel = null,
        onConfirm = {},
        dismissLabel = stringResource(R.string.common_cancel),
        onDismiss = onDismiss
    ) {
        // v6.2 crash fix: LazyColumn inside LinkzaryDialog's verticalScroll
        // column is measured with infinite height and crashed the moment the
        // move dialog opened (same root cause as the v5.1.1 import-preview and
        // language-picker fixes). Plain Column; the scaffold scrolls.
        Column(
            modifier = Modifier
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CollectionOptionItem(
                icon = Icons.Rounded.FolderOpen,
                name = stringResource(R.string.move_link_no_collection),
                color = MaterialTheme.colorScheme.outline.value.toInt(),
                isSelected = currentCollectionId == null,
                onClick = { onCollectionSelected(null) }
            )

            allCollections.forEach { collection ->
                val colorInt = try {
                    android.graphics.Color.parseColor(collection.color)
                } catch (e: IllegalArgumentException) {
                    0xFF6366F1.toInt()
                }
                CollectionOptionItem(
                    icon = getCollectionGlyph(collection.icon),
                    name = collection.name,
                    color = colorInt,
                    isSelected = currentCollectionId == collection.id,
                    onClick = { onCollectionSelected(collection.id) }
                )
            }

            Surface(
                onClick = { onCreateNew() },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CreateNewFolder,
                        contentDescription = stringResource(R.string.cd_create_new_collection),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )

                    Spacer(modifier = Modifier.width(16.dp))

                    Text(
                        text = stringResource(R.string.move_link_create_new),
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}
