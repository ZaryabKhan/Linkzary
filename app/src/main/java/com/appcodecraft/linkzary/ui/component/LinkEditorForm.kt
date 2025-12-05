package com.appcodecraft.linkzary.ui.component

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Title
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
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        OutlinedTextField(
            value = title,
            onValueChange = onTitleChange,
            label = { Text(stringResource(R.string.edit_link_title_label)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Title,
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
                    imageVector = Icons.Default.Link,
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
                    imageVector = Icons.AutoMirrored.Filled.Notes,
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
                    imageVector = Icons.Default.Tag,
                    contentDescription = stringResource(R.string.edit_link_tags_label),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
    }
}
