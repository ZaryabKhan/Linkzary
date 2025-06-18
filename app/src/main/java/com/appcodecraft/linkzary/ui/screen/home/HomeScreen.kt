package com.appcodecraft.linkzary.ui.screen.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ViewModule
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.appcodecraft.linkzary.data.entity.Collection
import com.appcodecraft.linkzary.data.entity.SavedLink
import com.appcodecraft.linkzary.ui.component.BookmarkCard
import com.appcodecraft.linkzary.ui.component.SmallCollectionCard
import com.appcodecraft.linkzary.ui.theme.LinkzaryTheme
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    sharedUrl: String? = null,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showAddLinkDialog by remember { mutableStateOf(false) }
    var selectedLink by remember { mutableStateOf<SavedLink?>(null) }
    var isGridView by remember { mutableStateOf(true) }
    val uriHandler = LocalUriHandler.current

    // Handle shared URL - prevent duplicates
    LaunchedEffect(sharedUrl) {
        if (!sharedUrl.isNullOrBlank()) {
            viewModel.saveSharedLink(sharedUrl)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Linkzary",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${uiState.links.size} saved links",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // View toggle button
                IconButton(
                    onClick = { isGridView = !isGridView }
                ) {
                    Icon(
                        imageVector = if (isGridView) Icons.Default.ViewList else Icons.Default.ViewModule,
                        contentDescription = if (isGridView) "List view" else "Grid view"
                    )
                }
                
                FloatingActionButton(
                    onClick = { showAddLinkDialog = true },
                    modifier = Modifier.size(48.dp),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add link"
                    )
                }
            }
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Search bar
        OutlinedTextField(
            value = uiState.searchQuery,
            onValueChange = viewModel::updateSearchQuery,
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search bookmarks...") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = "Search"
                )
            },
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        // Recent Collections section
        if (uiState.recentCollections.isNotEmpty() && uiState.searchQuery.isBlank()) {
            Text(
                text = "Recent Collections",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(uiState.recentCollections) { collection ->
                    SmallCollectionCard(
                        collection = collection,
                        linkCount = uiState.collectionsWithCounts[collection.id] ?: 0,
                        onCardClick = {
                            // TODO: Navigate to collection detail
                        }
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(24.dp))
        }
        
        // Recent Bookmarks section
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = if (uiState.searchQuery.isBlank()) "Recent Bookmarks" else "Search Results (${uiState.links.size})",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }
        
        Spacer(modifier = Modifier.height(12.dp))
        
        // Bookmarks list
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            uiState.links.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (uiState.searchQuery.isBlank()) 
                                "No bookmarks yet" else "No results found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (uiState.searchQuery.isBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Start saving your favorite links",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            else -> {
                if (isGridView) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(uiState.links) { link ->
                             BookmarkCard(
                                 link = link,
                                 collectionName = uiState.allCollections.find { it.id == link.collectionId }?.name,
                                 collectionColor = uiState.allCollections.find { it.id == link.collectionId }?.color,
                                 onCardClick = {
                                     try {
                                         uriHandler.openUri(link.url)
                                     } catch (e: Exception) {
                                         // Handle error - could show a snackbar
                                     }
                                 },
                                 onMoreClick = {
                                     selectedLink = link
                                 }
                             )
                         }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp)
                    ) {
                        items(uiState.links) { link ->
                             BookmarkCard(
                                 link = link,
                                 collectionName = uiState.allCollections.find { it.id == link.collectionId }?.name,
                                 collectionColor = uiState.allCollections.find { it.id == link.collectionId }?.color,
                                 onCardClick = {
                                     try {
                                         uriHandler.openUri(link.url)
                                     } catch (e: Exception) {
                                         // Handle error - could show a snackbar
                                     }
                                 },
                                 onMoreClick = {
                                     selectedLink = link
                                 }
                             )
                         }
                    }
                }
            }
        }
    }
    
    // Error handling
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // TODO: Show snackbar with error
            viewModel.clearError()
        }
    }
    
    // Add Link Dialog
    if (showAddLinkDialog) {
        AddLinkDialog(
            onDismiss = { showAddLinkDialog = false },
            onSave = { url ->
                viewModel.saveLink(url)
                showAddLinkDialog = false
            }
        )
    }
    
    // Link Options Bottom Sheet
    selectedLink?.let { link ->
        LinkOptionsBottomSheet(
            link = link,
            onDismiss = { selectedLink = null },
            onEdit = { updatedLink ->
                viewModel.updateLink(updatedLink)
                selectedLink = null
            },
            onDelete = {
                viewModel.deleteLink(link)
                selectedLink = null
            },
            onTogglePin = {
                viewModel.togglePinStatus(link)
                selectedLink = null
            },
            onMoveToCollection = { collectionId ->
                viewModel.moveToCollection(link.id, collectionId)
                selectedLink = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLinkDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var url by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Link") },
        text = {
            OutlinedTextField(
                value = url,
                onValueChange = { url = it },
                label = { Text("URL") },
                placeholder = { Text("https://example.com") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(url) },
                enabled = url.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkOptionsBottomSheet(
    link: SavedLink,
    onDismiss: () -> Unit,
    onEdit: (SavedLink) -> Unit,
    onDelete: () -> Unit,
    onTogglePin: () -> Unit,
    onMoveToCollection: (Long?) -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    
    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Link Options",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            // Edit option
            TextButton(
                onClick = { showEditDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Edit Link")
            }
            
            // Pin/Unpin option
            TextButton(
                onClick = {
                    onTogglePin()
                    onDismiss()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (link.isPinned) "Unpin" else "Pin to Top")
            }
            
            // Delete option
            TextButton(
                onClick = { showDeleteConfirmation = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Delete",
                    color = MaterialTheme.colorScheme.error
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
    
    // Edit Dialog
    if (showEditDialog) {
        EditLinkDialog(
            link = link,
            onDismiss = { showEditDialog = false },
            onSave = { updatedLink ->
                onEdit(updatedLink)
                showEditDialog = false
                onDismiss()
            }
        )
    }
    
    // Delete Confirmation Dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text("Delete Link") },
            text = { Text("Are you sure you want to delete this link? This action cannot be undone.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirmation = false
                        onDismiss()
                    }
                ) {
                    Text(
                        text = "Delete",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmation = false }
                ) {
                    Text("Cancel")
                }
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditLinkDialog(
    link: SavedLink,
    onDismiss: () -> Unit,
    onSave: (SavedLink) -> Unit
) {
    var title by remember { mutableStateOf(link.title) }
    var url by remember { mutableStateOf(link.url) }
    var note by remember { mutableStateOf(link.note) }
    var tags by remember { mutableStateOf(link.tags) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Edit Link") },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("URL") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = note,
                    onValueChange = { note = it },
                    label = { Text("Note (optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 3
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = tags,
                    onValueChange = { tags = it },
                    label = { Text("Tags (comma separated)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onSave(
                        link.copy(
                            title = title,
                            url = url,
                            note = note,
                            tags = tags
                        )
                    )
                },
                enabled = title.isNotBlank() && url.isNotBlank()
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Preview
@Composable
fun HomeScreenPreview() {
    LinkzaryTheme {
        Surface {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Home Screen with Enhanced Grid Layout")
            }
        }
    }
}