package com.appcodecraft.linkzary.ui.screen.home

// Animation imports removed for better performance
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.Tag
import androidx.compose.material.icons.filled.Title
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.appcodecraft.linkzary.R
import com.appcodecraft.linkzary.data.entity.SavedLink
import com.appcodecraft.linkzary.navigation.Screen
import com.appcodecraft.linkzary.ui.component.BookmarkCard
import com.appcodecraft.linkzary.ui.component.SmallCollectionCard
import com.appcodecraft.linkzary.ui.theme.LinkzaryTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController? = null,
    sharedUrl: String? = null,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.combinedUiState.collectAsStateWithLifecycle()
    var showAddLinkDialog by remember { mutableStateOf(false) }
    var selectedLink by remember { mutableStateOf<SavedLink?>(null) }
    val isGridView by viewModel.isGridView.collectAsState()
    var showTagFilter by remember { mutableStateOf(false) }
    var selectedTags by remember { mutableStateOf(setOf<String>()) }
    var showSortMenu by remember { mutableStateOf(false) }

    // Local search state to prevent bouncing
    var localSearchQuery by remember { mutableStateOf("") }

    val uriHandler = LocalUriHandler.current
    val scrollState = rememberScrollState()

    // Sync local search query with UI state
    LaunchedEffect(uiState.searchQuery) {
        if (localSearchQuery != uiState.searchQuery) {
            localSearchQuery = uiState.searchQuery
        }
    }

    // Handle shared URL - prevent duplicates
    LaunchedEffect(sharedUrl) {
        if (!sharedUrl.isNullOrBlank()) {
            viewModel.saveSharedLink(sharedUrl)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
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
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Bookmark,
                        contentDescription = stringResource(R.string.home_linkzary_title),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.home_linkzary_title),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Link,
                        contentDescription = stringResource(R.string.collections_count),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.home_saved_links, uiState.links.size),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Enhanced view toggle button
                IconButton(
                            onClick = { viewModel.toggleGridView() },
                    modifier = Modifier
                        .size(48.dp)
                        .background(
                            color = if (isGridView) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f) else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Icon(
                        imageVector = if (isGridView) Icons.AutoMirrored.Filled.ViewList else Icons.Default.GridView,
                        contentDescription = if (isGridView) stringResource(R.string.home_switch_to_list_view) else stringResource(R.string.home_switch_to_grid_view),
                        tint = if (isGridView) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
                
                // Enhanced sort menu
                Box {
                    IconButton(
                        onClick = { showSortMenu = true },
                        modifier = Modifier
                            .size(48.dp)
                            .background(
                                color = if (showSortMenu) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f) else Color.Transparent,
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = stringResource(R.string.home_sort_options),
                            tint = if (showSortMenu) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.home_pinned_first)) },
                            onClick = {
                                viewModel.setSortOrder(LinkSortOrder.PINNED_FIRST)
                                showSortMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.PushPin, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.home_newest_first)) },
                            onClick = {
                                viewModel.setSortOrder(LinkSortOrder.DATE_DESC)
                                showSortMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Schedule, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.home_oldest_first)) },
                            onClick = {
                                viewModel.setSortOrder(LinkSortOrder.DATE_ASC)
                                showSortMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.History, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.home_title_a_z)) },
                            onClick = {
                                viewModel.setSortOrder(LinkSortOrder.TITLE_ASC)
                                showSortMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.SortByAlpha, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.home_title_z_a)) },
                            onClick = {
                                viewModel.setSortOrder(LinkSortOrder.TITLE_DESC)
                                showSortMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.SortByAlpha, contentDescription = null)
                            }
                        )
                    }
                }

                FloatingActionButton(
                    onClick = { showAddLinkDialog = true },
                    modifier = Modifier.size(48.dp),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = stringResource(R.string.home_add_link)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Search bar with improved state management
        OutlinedTextField(
            value = localSearchQuery,
            onValueChange = { newQuery ->
                localSearchQuery = newQuery
                // Use a slight delay to debounce the search
                viewModel.updateSearchQuery(newQuery)
            },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text(stringResource(R.string.home_search_bookmarks)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(R.string.home_search),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            trailingIcon = if (localSearchQuery.isNotEmpty()) {
                {
                    IconButton(
                        onClick = {
                            localSearchQuery = ""
                            viewModel.updateSearchQuery("")
                        }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Clear,
                            contentDescription = stringResource(R.string.home_clear_search),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            } else null,
            shape = RoundedCornerShape(12.dp),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Tag filter section
        val allTags = remember(uiState.links) {
            uiState.links.flatMap { link ->
                link.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            }.distinct().sorted()
        }

        if (allTags.isNotEmpty()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.home_filter_by_tags),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                if (selectedTags.isNotEmpty()) {
                    TextButton(
                        onClick = { selectedTags = setOf() }
                    ) {
                        Text(
                            text = stringResource(R.string.home_clear_tags, selectedTags.size),
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp)
            ) {
                items(allTags) { tag ->
                    FilterChip(
                        onClick = {
                            selectedTags = if (selectedTags.contains(tag)) {
                                selectedTags - tag
                            } else {
                                selectedTags + tag
                            }
                        },
                        label = { Text(tag) },
                        selected = selectedTags.contains(tag),
                        leadingIcon = if (selectedTags.contains(tag)) {
                            {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = stringResource(R.string.home_selected),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Recent Collections section
        if (uiState.recentCollections.isNotEmpty() && localSearchQuery.isBlank()) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = stringResource(R.string.nav_collections),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.home_recent_collections),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(horizontal = 4.dp),
                modifier = Modifier.height(120.dp)
            ) {
                items(uiState.recentCollections) { collection ->
                    SmallCollectionCard(
                            collection = collection,
                            linkCount = uiState.collectionsWithCounts[collection.id] ?: 0,
                            onCardClick = {
                                navController?.navigate(
                                    Screen.CollectionDetail.createRoute(collection.id.toString())
                                )
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
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = if (localSearchQuery.isBlank()) Icons.Default.History else Icons.Default.Search,
                    contentDescription = if (localSearchQuery.isBlank()) stringResource(R.string.home_recent) else stringResource(R.string.home_search),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (localSearchQuery.isBlank()) stringResource(R.string.home_recent_bookmarks) else stringResource(R.string.home_search_results, uiState.links.size),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Filter links by selected tags
        val filteredLinks = remember(uiState.links, selectedTags) {
            if (selectedTags.isEmpty()) {
                uiState.links
            } else {
                uiState.links.filter { link ->
                    val linkTags = link.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    selectedTags.any { selectedTag -> linkTags.contains(selectedTag) }
                }
            }
        }

        // Bookmarks list
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            filteredLinks.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = when {
                                selectedTags.isNotEmpty() -> stringResource(R.string.home_no_bookmarks_tags)
                localSearchQuery.isNotBlank() -> stringResource(R.string.home_no_results_found)
                else -> stringResource(R.string.home_no_bookmarks_yet)
                            },
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (localSearchQuery.isBlank() && selectedTags.isEmpty()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = stringResource(R.string.home_start_saving_links),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }

            else -> {
                if (isGridView) {
                    // Grid view - using Column with Rows for scrollable grid without animations for better performance
                    val chunkedLinks = filteredLinks.chunked(2)
                    Column(
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        chunkedLinks.forEachIndexed { rowIndex, rowLinks ->
                            Row(
                                modifier = Modifier
                            .fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                rowLinks.forEachIndexed { cardIndex, link ->
                                    BookmarkCard(
                                        link = link,
                                        collectionName = uiState.allCollections.find { it.id == link.collectionId }?.name,
                                        collectionColor = uiState.allCollections.find { it.id == link.collectionId }?.color,
                                        onCardClick = {
                                            try {
                                                uriHandler.openUri(link.url)
                                            } catch (_: Exception) {
                                                // Handle error - could show a snackbar
                                            }
                                        },
                                        onMoreClick = {
                                            selectedLink = link
                                        },
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                                // Add spacer for odd number of items
                                if (rowLinks.size == 1) {
                                    Spacer(modifier = Modifier.weight(1f))
                                }
                            }
                        }
                    }
                } else {
                    // List view without animations for better performance
                    Column(
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        filteredLinks.forEach { link ->
                            BookmarkCard(
                                link = link,
                                collectionName = uiState.allCollections.find { it.id == link.collectionId }?.name,
                                collectionColor = uiState.allCollections.find { it.id == link.collectionId }?.color,
                                onCardClick = {
                                    try {
                                        uriHandler.openUri(link.url)
                                    } catch (_: Exception) {
                                        // Handle error - could show a snack bar
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

        // Add bottom padding for better scrolling experience
        Spacer(modifier = Modifier.height(100.dp))
    }

    // Error handling
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // TODO: Show snack bar with error
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
    var isValidUrl by remember { mutableStateOf(true) }
    
    fun validateUrl(input: String): Boolean {
        return input.isNotBlank() && (
            input.startsWith("http://") ||
                input.startsWith("https://") ||
                input.startsWith("www.") ||
                input.contains(".") && input.length > 3
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_link_title),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.add_link_title))
            }
        },
        text = {
            Column {
                OutlinedTextField(
                    value = url,
                    onValueChange = { 
                        url = it
                        isValidUrl = validateUrl(it)
                    },
                    label = { Text(stringResource(R.string.add_link_url_label)) },
            placeholder = { Text(stringResource(R.string.add_link_url_placeholder)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = stringResource(R.string.add_link_url_hint),
                            tint = if (isValidUrl || url.isEmpty()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.error
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = !isValidUrl && url.isNotEmpty(),
                    supportingText = if (!isValidUrl && url.isNotEmpty()) {
                        { Text(stringResource(R.string.add_link_invalid_url), color = MaterialTheme.colorScheme.error) }
                    } else null
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(url) },
                enabled = url.isNotBlank() && isValidUrl
            ) {
                Text(stringResource(R.string.add_link_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.add_link_cancel))
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
    var showCollectionDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = stringResource(R.string.link_options_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Edit option
            OptionItem(
                icon = Icons.Default.Edit,
                title = stringResource(R.string.link_edit_title),
            subtitle = stringResource(R.string.link_edit_subtitle),
                onClick = { showEditDialog = true }
            )

            // Pin/Unpin option
            OptionItem(
                icon = if (link.isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                title = if (link.isPinned) stringResource(R.string.link_unpin_title) else stringResource(R.string.link_pin_title),
            subtitle = if (link.isPinned) stringResource(R.string.link_unpin_subtitle) else stringResource(R.string.link_pin_subtitle),
                onClick = {
                    onTogglePin()
                    onDismiss()
                }
            )

            // Move to Collection option
            OptionItem(
                icon = Icons.AutoMirrored.Filled.DriveFileMove,
                title = stringResource(R.string.link_move_title),
            subtitle = stringResource(R.string.link_move_subtitle),
                onClick = { showCollectionDialog = true }
            )

            // Delete option
            OptionItem(
                icon = Icons.Default.Delete,
                title = stringResource(R.string.link_delete_title),
            subtitle = stringResource(R.string.link_delete_subtitle),
                onClick = { showDeleteConfirmation = true },
                isDestructive = true
            )

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

    // Collection Selection Dialog
    if (showCollectionDialog) {
        CollectionSelectionDialog(
            currentCollectionId = link.collectionId,
            onDismiss = { showCollectionDialog = false },
            onCollectionSelected = { collectionId ->
                onMoveToCollection(collectionId)
                showCollectionDialog = false
                onDismiss()
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text(stringResource(R.string.delete_link_title)) },
        text = { Text(stringResource(R.string.delete_link_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirmation = false
                        onDismiss()
                    }
                ) {
                    Text(
                        text = stringResource(R.string.delete_link_confirm),
                        color = MaterialTheme.colorScheme.error
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmation = false }
                ) {
                    Text(stringResource(R.string.delete_link_cancel))
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
        title = { Text(stringResource(R.string.edit_link_title)) },
        text = {
            Column {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
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
                    onValueChange = { url = it },
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
                    onValueChange = { note = it },
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
                    onValueChange = { tags = it },
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
                Text(stringResource(R.string.edit_link_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.edit_link_cancel))
            }
        }
    )
}

@Composable
fun OptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionSelectionDialog(
    currentCollectionId: Long?,
    onDismiss: () -> Unit,
    onCollectionSelected: (Long?) -> Unit
) {
    val viewModel: HomeViewModel = hiltViewModel()
    val uiState by viewModel.combinedUiState.collectAsStateWithLifecycle()
    var showCreateDialog by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Text(
                text = stringResource(R.string.move_link_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        },
        text = {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // No Collection option
                item {
                    CollectionOption(
                        icon = Icons.Default.FolderOpen,
                        name = stringResource(R.string.move_link_no_collection),
                        color = MaterialTheme.colorScheme.outline.value.toInt(),
                        isSelected = currentCollectionId == null,
                        onClick = { onCollectionSelected(null) }
                    )
                }
                
                // Existing collections
                items(uiState.allCollections) { collection ->
                    CollectionOption(
                        icon = Icons.Default.Folder,
                        name = collection.name,
                        color = collection.color.removePrefix("#").toLongOrNull(16)?.toInt() ?: 0xFF6366F1.toInt(),
                        isSelected = currentCollectionId == collection.id,
                        onClick = { onCollectionSelected(collection.id) }
                    )
                }
                
                // Create new collection option
                item {
                    Surface(
                        onClick = { showCreateDialog = true },
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
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.move_link_cancel))
            }
        }
    )
    
    // Create Collection Dialog
    if (showCreateDialog) {
        CreateCollectionDialog(
            onDismiss = { showCreateDialog = false },
            onCreate = { name, color ->
                // Create collection and move link to it
                viewModel.createCollection(name, color) { collectionId ->
                    onCollectionSelected(collectionId)
                }
                showCreateDialog = false
            }
        )
    }
}

@Composable
fun CollectionOption(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    name: String,
    color: Int,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = name,
                tint = androidx.compose.ui.graphics.Color(color),
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
            
            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = stringResource(R.string.cd_selected),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCollectionDialog(
    onDismiss: () -> Unit,
    onCreate: (String, Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(0xFF6366F1.toInt()) }
    val maxNameLength = 30
    
    val colors = listOf(
        0xFF6366F1.toInt(), // Indigo
        0xFFEF4444.toInt(), // Red
        0xFF10B981.toInt(), // Emerald
        0xFFF59E0B.toInt(), // Amber
        0xFF8B5CF6.toInt(), // Violet
        0xFF06B6D4.toInt(), // Cyan
        0xFFEC4899.toInt(), // Pink
        0xFF84CC16.toInt(), // Lime
        0xFFF97316.toInt(), // Orange
        0xFFEAB308.toInt(), // Yellow
        0xFF22C55E.toInt(), // Green
        0xFF3B82F6.toInt(), // Blue
        0xFFA855F7.toInt(), // Purple
        0xFFE11D48.toInt(), // Rose
        0xFF0EA5E9.toInt(), // Sky
        0xFF64748B.toInt()  // Slate
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { 
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CreateNewFolder,
                    contentDescription = stringResource(R.string.cd_create_collection),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.create_collection_title))
            }
        },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { newValue ->
                        if (newValue.length <= maxNameLength) {
                            name = newValue
                        }
                    },
                    label = { Text(stringResource(R.string.create_collection_name_label)) },
            placeholder = { Text(stringResource(R.string.create_collection_name_placeholder)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = stringResource(R.string.create_collection_name_hint),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    supportingText = {
                        Text(
                            text = "${name.length}/$maxNameLength",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (name.length >= maxNameLength) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = stringResource(R.string.create_collection_choose_color),
                    style = MaterialTheme.typography.labelMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                LazyVerticalGrid(
                    columns = GridCells.Fixed(8),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(120.dp)
                ) {
                    items(colors) { color ->
                        FilterChip(
                            onClick = { selectedColor = color },
                            label = { },
                            selected = selectedColor == color,
                            modifier = Modifier.size(32.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = androidx.compose.ui.graphics.Color(color),
                                selectedContainerColor = androidx.compose.ui.graphics.Color(color)
                            ),
                            border = if (selectedColor == color) {
                                FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = true,
                                    borderColor = MaterialTheme.colorScheme.primary,
                                    borderWidth = 2.dp
                                )
                            } else {
                                FilterChipDefaults.filterChipBorder(
                                    enabled = true,
                                    selected = false
                                )
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(name, selectedColor) },
                enabled = name.isNotBlank()
            ) {
                Text(stringResource(R.string.create_collection_create))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.create_collection_cancel))
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
                Text(stringResource(R.string.home_screen_preview))
            }
        }
    }
}