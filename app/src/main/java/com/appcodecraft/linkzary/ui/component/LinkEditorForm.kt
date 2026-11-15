package com.appcodecraft.linkzary.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Tag
import androidx.compose.material.icons.rounded.Title
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.appcodecraft.linkzary.R

@Composable
fun LinkEditorForm(
    title: String,
    onTitleChange: (String) -> Unit,
    url: String,
    onUrlChange: (String) -> Unit,
    note: String,
    onNoteChange: (String) -> Unit,
    tags: String,
    onTagsChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    allTags: List<String> = emptyList(),
    customImageUrl: String,
    onCustomImageUrlChange: (String) -> Unit
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text(stringResource(R.string.edit_link_title_label)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Title,
                    contentDescription = stringResource(R.string.edit_link_title_label),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = url,
            onValueChange = onUrlChange,
            label = { Text(stringResource(R.string.edit_link_url_label)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Link,
                    contentDescription = stringResource(R.string.edit_link_url_label),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = note,
            onValueChange = onNoteChange,
            label = { Text(stringResource(R.string.edit_link_note_label)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Notes,
                    contentDescription = stringResource(R.string.edit_link_note_label),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            modifier = Modifier.fillMaxWidth(),
            maxLines = 3
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = tags,
            onValueChange = onTagsChange,
            label = { Text(stringResource(R.string.edit_link_tags_label)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Tag,
                    contentDescription = stringResource(R.string.edit_link_tags_label),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = customImageUrl,
            onValueChange = onCustomImageUrlChange,
            label = { Text(stringResource(R.string.edit_link_custom_image_label)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Rounded.Image,
                    contentDescription = stringResource(R.string.edit_link_custom_image_label),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            placeholder = { Text(stringResource(R.string.edit_link_custom_image_hint)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        // Suggest existing tags that are not already on this link
        val currentTags = tags.split(",").map { it.trim().lowercase() }.filter { it.isNotEmpty() }
        val suggestions = allTags.filter { candidate ->
            candidate.lowercase() !in currentTags &&
                (tags.isBlank() || candidate.contains(tags.substringAfterLast(",").trim(), ignoreCase = true) ||
                    tags.substringAfterLast(",").trim().isEmpty())
        }.take(6)

        if (suggestions.isNotEmpty()) {
            Spacer(modifier = Modifier.height(6.dp))
            androidx.compose.foundation.lazy.LazyRow(
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(6.dp)
            ) {
                items(suggestions.size) { index ->
                    val tag = suggestions[index]
                    androidx.compose.material3.SuggestionChip(
                        onClick = {
                            val prefix = if (tags.isBlank() || tags.endsWith(",")) tags else "$tags,"
                            onTagsChange("$prefix$tag")
                        },
                        label = { Text(tag, style = MaterialTheme.typography.labelSmall) }
                    )
                }
            }
        }
    }
}
