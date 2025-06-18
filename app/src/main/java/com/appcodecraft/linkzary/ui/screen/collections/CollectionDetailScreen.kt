package com.appcodecraft.linkzary.ui.screen.collections

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.*
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.appcodecraft.linkzary.data.entity.SavedLink
import com.appcodecraft.linkzary.ui.component.BookmarkCard

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionDetailScreen(
    collectionId: String,
    navController: NavController,
    viewModel: CollectionDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val uriHandler = LocalUriHandler.current

    var selectedLink by remember { mutableStateOf<SavedLink?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var isGridView by remember { mutableStateOf(false) }

    LaunchedEffect(collectionId) {
        viewModel.loadCollectionDetails(collectionId)
    }

    // Filter links based on search query
    val filteredLinks = remember(uiState.links, searchQuery) {
        if (searchQuery.isBlank()) {
            uiState.links
        } else {
            uiState.links.filter { link ->
                link.title.contains(searchQuery, ignoreCase = true) ||
                        link.url.contains(searchQuery, ignoreCase = true) ||
                        link.note.contains(searchQuery, ignoreCase = true) ||
                        link.tags.contains(searchQuery, ignoreCase = true)
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Top bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.navigateUp() }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }

            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = uiState.collection?.name ?: "Collection",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${filteredLinks.size} links",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = { isGridView = !isGridView }
            ) {
                Icon(
                    imageVector = if (isGridView) Icons.AutoMirrored.Filled.ViewList else Icons.Default.GridView,
                    contentDescription = if (isGridView) "List view" else "Grid view"
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("Search links...") },
            leadingIcon = {
                Icon(Icons.Default.Search, contentDescription = "Search")
            },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Clear")
                    }
                }
            },
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Content
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            uiState.error != null -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        text = uiState.error ?: "Unknown error",
                        modifier = Modifier.padding(16.dp),
                        color = MaterialTheme.colorScheme.onErrorContainer
                    )
                }
            }
            filteredLinks.isEmpty() -> {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = if (searchQuery.isNotEmpty()) Icons.Default.SearchOff else Icons.Default.Link,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No matching links" else "No links in this collection",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (searchQuery.isNotEmpty()) "Try a different search term" else "Add some links to get started",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(filteredLinks) { link ->
                        BookmarkCard(
                            link = link,
                            collectionName = uiState.collection?.name ?: "",
                            collectionColor = uiState.collection?.color ?: "#FF6200EE",
                            onCardClick = { uriHandler.openUri(link.url) },
                            onMoreClick = {
                                selectedLink = link
                            }
                        )
                    }
                }
            }
        }
    }
    
    // Link options bottom sheet
    selectedLink?.let { link ->
        ModalBottomSheet(
            onDismissRequest = { selectedLink = null }
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
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                // Pin/Unpin option
                ListItem(
                    headlineContent = { Text(if (link.isPinned) "Unpin" else "Pin to Top") },
                    supportingContent = { Text(if (link.isPinned) "Remove from pinned links" else "Keep at the top of your list") },
                    leadingContent = {
                        Icon(
                            imageVector = if (link.isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                            contentDescription = null
                        )
                    },
                    modifier = Modifier.clickable {
                        viewModel.togglePin(link.id)
                        selectedLink = null
                    }
                )

                // Delete option
                ListItem(
                    headlineContent = { Text("Delete") },
                    supportingContent = { Text("Remove this link permanently") },
                    leadingContent = {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error
                        )
                    },
                    modifier = Modifier.clickable {
                        viewModel.deleteLink(link.id)
                        selectedLink = null
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}