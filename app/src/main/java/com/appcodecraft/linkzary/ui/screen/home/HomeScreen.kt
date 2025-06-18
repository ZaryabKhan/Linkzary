package com.appcodecraft.linkzary.ui.screen.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

    // Handle shared URL
    LaunchedEffect(sharedUrl) {
        if (!sharedUrl.isNullOrBlank()) {
            viewModel.saveLink(sharedUrl)
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
            Text(
                text = "Linkzary",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
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
                text = if (uiState.searchQuery.isBlank()) "Recent Bookmarks" else "Search Results",
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
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
                ) {
                    items(uiState.links) { link ->
                        BookmarkCard(
                            link = link,
                            collectionName = null, // TODO: Get collection name
                            collectionColor = null, // TODO: Get collection color
                            onCardClick = {
                                // TODO: Open link or show details
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
    // TODO: Implement bottom sheet with options
    // For now, just dismiss
    LaunchedEffect(Unit) {
        onDismiss()
    }
}

@Preview
@Composable
fun HomeScreenPreview() {
    LinkzaryTheme {
        Surface {
            // Preview with mock data would go here
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Home Screen Preview")
            }
        }
    }
}