package com.appcodecraft.linkzary.ui.screen.share

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.appcodecraft.linkzary.R
import com.appcodecraft.linkzary.ui.component.CollectionOption
import com.appcodecraft.linkzary.ui.component.CreateCollectionForm
import com.appcodecraft.linkzary.ui.component.LinkEditorForm
import com.appcodecraft.linkzary.ui.component.getCollectionIconVector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareScreen(
    sharedUrl: String,
    onFinish: () -> Unit,
    viewModel: ShareViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val allCollections by viewModel.allCollections.collectAsState()
    
    var showCollectionSheet by remember { mutableStateOf(false) }
    var showCreateCollectionSheet by remember { mutableStateOf(false) }

    LaunchedEffect(sharedUrl) {
        viewModel.onUrlShared(sharedUrl)
    }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onFinish()
        }
    }

    // Main Dialog-like layout
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.5f)) // Dim background
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 32.dp), // Avoid edge-to-edge
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = stringResource(R.string.add_link_title),
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold
                    )
                    IconButton(onClick = onFinish) {
                        Icon(Icons.Default.Close, contentDescription = "Close")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.isLoading && uiState.title.isEmpty()) {
                     Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    // Link Editor Form
                    LinkEditorForm(
                        title = uiState.title,
                        onTitleChange = viewModel::updateTitle,
                        url = uiState.url,
                        onUrlChange = viewModel::updateUrl,
                        note = uiState.note,
                        onNoteChange = viewModel::updateNote,
                        tags = uiState.tags,
                        onTagsChange = viewModel::updateTags
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    // Collection Selector Button
                    val selectedCollection = allCollections.find { it.id == uiState.selectedCollectionId }
                    val collectionName = selectedCollection?.name ?: stringResource(R.string.move_link_no_collection)
                    val collectionColor = selectedCollection?.color?.let { 
                        try { android.graphics.Color.parseColor(it) } catch (e: Exception) { 0xFF6366F1.toInt() }
                    } ?: MaterialTheme.colorScheme.outline.value.toInt()
                    
                    Surface(
                        onClick = { showCollectionSheet = true },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (selectedCollection != null) Icons.Default.Folder else Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = Color(collectionColor),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Collection",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = collectionName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.weight(1f))
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.DriveFileMove,
                                contentDescription = "Change",
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(24.dp))

                    // Action Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(onClick = onFinish) {
                            Text(stringResource(R.string.add_link_cancel))
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = viewModel::saveLink,
                            enabled = uiState.title.isNotBlank() && uiState.url.isNotBlank() && !uiState.isLoading
                        ) {
                            if (uiState.isLoading) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    color = MaterialTheme.colorScheme.onPrimary,
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            Text(stringResource(R.string.add_link_save))
                        }
                    }
                    
                    if (uiState.error != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = uiState.error!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        }
    }

    // Collection Selection Bottom Sheet
    if (showCollectionSheet) {
        ModalBottomSheet(onDismissRequest = { showCollectionSheet = false }) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.move_link_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(bottom = 16.dp)
                )

                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 32.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // No Collection option
                    item {
                        CollectionOption(
                            icon = Icons.Default.FolderOpen,
                            name = stringResource(R.string.move_link_no_collection),
                            color = MaterialTheme.colorScheme.outline.value.toInt(),
                            isSelected = uiState.selectedCollectionId == null,
                            onClick = {
                                viewModel.selectCollection(null)
                                showCollectionSheet = false
                            }
                        )
                    }

                    // Existing collections
                    items(allCollections) { collection ->
                        val colorInt = remember(collection.color) {
                            try {
                                android.graphics.Color.parseColor(collection.color)
                            } catch (e: IllegalArgumentException) {
                                0xFF6366F1.toInt()
                            }
                        }
                        CollectionOption(
                            icon = getCollectionIconVector(collection.icon),
                            name = collection.name,
                            color = colorInt,
                            isSelected = uiState.selectedCollectionId == collection.id,
                            onClick = {
                                viewModel.selectCollection(collection.id)
                                showCollectionSheet = false
                            }
                        )
                    }

                    // Create new collection option
                    item {
                        Surface(
                            onClick = { 
                                showCollectionSheet = false
                                showCreateCollectionSheet = true 
                            },
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
                                    imageVector = Icons.Default.CreateNewFolder,
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
        }
    }

    // Create Collection Bottom Sheet/Dialog
    if (showCreateCollectionSheet) {
        var newCollectionName by remember { mutableStateOf("") }
        var newCollectionColor by remember { mutableStateOf(0xFF6366F1.toInt()) }
        var newCollectionIcon by remember { mutableStateOf("Folder") }
        
        androidx.compose.material3.AlertDialog(
             onDismissRequest = { showCreateCollectionSheet = false },
             title = {
                 Row(verticalAlignment = Alignment.CenterVertically) {
                     Icon(Icons.Default.CreateNewFolder, null, tint = MaterialTheme.colorScheme.primary)
                     Spacer(Modifier.width(8.dp))
                     Text(stringResource(R.string.create_collection_title))
                 }
             },
             text = {
                 CreateCollectionForm(
                     name = newCollectionName,
                     onNameChange = { newCollectionName = it },
                     selectedColor = newCollectionColor,
                     onColorSelected = { newCollectionColor = it },
                     selectedIcon = newCollectionIcon,
                     onIconSelected = { newCollectionIcon = it }
                 )
             },
             confirmButton = {
                 TextButton(
                     onClick = {
                         viewModel.createCollection(newCollectionName, newCollectionColor, newCollectionIcon)
                         showCreateCollectionSheet = false
                     },
                     enabled = newCollectionName.isNotBlank()
                 ) {
                     Text(stringResource(R.string.create_collection_create))
                 }
             },
             dismissButton = {
                 TextButton(onClick = { showCreateCollectionSheet = false }) {
                     Text(stringResource(R.string.create_collection_cancel))
                 }
             }
        )
    }
}
