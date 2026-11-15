package com.appcodecraft.linkzary.ui.screen.collections

import androidx.compose.foundation.background
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Sort
import androidx.compose.material.icons.automirrored.rounded.ViewList
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Folder
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SortByAlpha
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.appcodecraft.linkzary.R
import com.appcodecraft.linkzary.data.entity.Collection
import com.appcodecraft.linkzary.navigation.Screen
import com.appcodecraft.linkzary.ui.component.CollectionCard
import com.appcodecraft.linkzary.ui.component.CreateCollectionForm
import com.appcodecraft.linkzary.ui.component.getCollectionGlyph
import com.appcodecraft.linkzary.ui.component.getCollectionGlyph
import com.appcodecraft.linkzary.ui.component.OptionItem
import com.appcodecraft.linkzary.ui.theme.LinkzaryTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionsScreen(
    navController: NavController? = null,
    viewModel: CollectionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showCreateCollectionDialog by remember { mutableStateOf(false) }
    var selectedCollection by remember { mutableStateOf<Collection?>(null) }
    val isGridView by viewModel.isGridView.collectAsState()
    var showSortMenu by remember { mutableStateOf(false) }

    val collectionsListState = androidx.compose.foundation.lazy.rememberLazyListState()

    // Spec v3 §2.2: FAB hide/return scroll behavior, wired to the list below.
    val fabBehavior = com.appcodecraft.linkzary.ui.component.rememberLinkzaryFabScrollBehavior()

    // Pre-compute collection lookup map
    val collectionMap by remember(uiState.collectionsWithCounts) {
        derivedStateOf { uiState.collectionsWithCounts }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainerLow)) {

        Column(modifier = Modifier.fillMaxSize()) {
        // The one standard header (spec v2 §2.1) — no screen-local header Row
        com.appcodecraft.linkzary.ui.component.LinkzaryTopBar(
            title = stringResource(R.string.nav_collections),
            leadingGlyph = Icons.Rounded.Folder,
            actions = {
                com.appcodecraft.linkzary.ui.component.ViewModeToggle(
                    isGridView = isGridView,
                    onToggle = { viewModel.toggleGridView() }
                )

                Spacer(modifier = Modifier.width(4.dp))

                // Sort menu
                Box {
                    IconButton(
                        onClick = { showSortMenu = true }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Sort,
                            contentDescription = stringResource(R.string.home_sort_options),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.collections_sort_name_az)) },
                            onClick = { viewModel.setSortOrder(SortOrder.NAME_ASC); showSortMenu = false },
                            leadingIcon = { Icon(Icons.Rounded.SortByAlpha, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.collections_sort_name_za)) },
                            onClick = { viewModel.setSortOrder(SortOrder.NAME_DESC); showSortMenu = false },
                            leadingIcon = { Icon(Icons.Rounded.SortByAlpha, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.collections_sort_most_links)) },
                            onClick = { viewModel.setSortOrder(SortOrder.LINK_COUNT_DESC); showSortMenu = false },
                            leadingIcon = { Icon(Icons.Rounded.Link, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.collections_sort_recently_created)) },
                            onClick = { viewModel.setSortOrder(SortOrder.DATE_DESC); showSortMenu = false },
                            leadingIcon = { Icon(Icons.Rounded.Schedule, contentDescription = null) }
                        )
                    }
                }
            }
        )

        // Main content — header is a real sibling now, so no hardcoded top pad
        LazyColumn(
            state = collectionsListState,
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(fabBehavior.nestedScrollConnection)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // One search style app-wide (spec v2 §2.2)
            item(key = "search") {
                com.appcodecraft.linkzary.ui.component.LinkzarySearchBar(
                    query = uiState.searchQuery,
                    onQueryChange = viewModel::updateSearchQuery,
                    placeholder = stringResource(R.string.collections_search_placeholder),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            // Sort info chips
            item(key = "info") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilterChip(
                        onClick = {},
                        label = {
                            val count = uiState.collections.size
                            Text(stringResource(R.string.nav_collections) + " ($count)")
                        },
                        selected = true,
                        leadingIcon = { Icon(Icons.Rounded.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    if (uiState.collectionsWithCounts.values.sum() > 0) {
                        FilterChip(
                            onClick = {},
                            label = {
                                Text("${uiState.collectionsWithCounts.values.sum()} ${stringResource(R.string.collections_total_links).lowercase()}")
                            },
                            selected = false,
                            leadingIcon = { Icon(Icons.Rounded.Link, contentDescription = null, modifier = Modifier.size(16.dp)) }
                        )
                    }
                }
            }

            // Loading state
            if (uiState.isLoading) {
                item(key = "loading") {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }

            // Empty state
            if (!uiState.isLoading && uiState.collections.isEmpty()) {
                item(key = "empty") {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = if (uiState.searchQuery.isBlank())
                                    Icons.Rounded.FolderOpen else Icons.Rounded.SearchOff,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                text = if (uiState.searchQuery.isBlank())
                                    stringResource(R.string.collections_empty_state)
                                else stringResource(R.string.collections_no_results),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            if (uiState.searchQuery.isBlank()) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = stringResource(R.string.collections_empty_description),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Collections grid/list
            if (!uiState.isLoading && uiState.collections.isNotEmpty()) {
                if (isGridView) {
                    item(key = "grid-spacer") {
                        Spacer(modifier = Modifier.height(4.dp))
                    }
                    itemsIndexed(
                        items = uiState.collections.chunked(2),
                        key = { _, chunk -> chunk.joinToString(",") { it.id.toString() } }
                    ) { rowIndex, rowCollections ->
                        // Addendum #3: equal card heights per row — the row
                        // sizes to its tallest card, both cards stretch.
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(IntrinsicSize.Min),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowCollections.forEachIndexed { colIndex, collection ->
                                CollectionCard(
                                    collection = collection,
                                    linkCount = collectionMap[collection.id] ?: 0,
                                    thumbnails = uiState.collectionThumbnails[collection.id] ?: emptyList(),
                                    onCardClick = {
                                        navController?.navigate(
                                            Screen.CollectionDetail.createRoute(collection.id.toString())
                                        )
                                    },
                                    onMoreClick = { selectedCollection = collection },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowCollections.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                } else {
                    itemsIndexed(
                        items = uiState.collections,
                        key = { _, collection -> collection.id }
                    ) { index, collection ->
                        CollectionCard(
                            collection = collection,
                            linkCount = collectionMap[collection.id] ?: 0,
                            thumbnails = uiState.collectionThumbnails[collection.id] ?: emptyList(),
                            onCardClick = {
                                navController?.navigate(
                                    Screen.CollectionDetail.createRoute(collection.id.toString())
                                )
                            },
                            onMoreClick = { selectedCollection = collection }
                        )
                    }
                }

                // Patched spec §2.6: muted empty state for the region below
                // the grid — 64dp tonal circle + dim bookmark, left-column-
                // centered, no dashed add tile (the FAB owns creation).
                item(key = "grid-empty-tail") {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.5f)
                            .padding(top = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(64.dp)
                                .clip(RoundedCornerShape(32.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainerHigh),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Bookmark,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = stringResource(R.string.collections_nothing_else_yet),
                            style = MaterialTheme.typography.titleSmall,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                        Text(
                            text = stringResource(R.string.collections_nothing_else_hint),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }

        } // Column (header + content)

        // The one FAB — spec v3 §2.2 Extended FAB with scroll hide/return
        com.appcodecraft.linkzary.ui.component.LinkzaryExtendedFab(
            label = stringResource(R.string.fab_new_collection),
            onClick = { showCreateCollectionDialog = true },
            behavior = fabBehavior,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }

    // Error handling
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            viewModel.clearError()
        }
    }

    // Create Collection Dialog — shared component (spec v4 §2.2)
    if (showCreateCollectionDialog) {
        com.appcodecraft.linkzary.ui.component.CreateCollectionDialog(
            onDismiss = { showCreateCollectionDialog = false },
            onCreate = { name: String, colorInt: Int, icon: String ->
                viewModel.createCollection(name, colorInt, icon)
                showCreateCollectionDialog = false
            }
        )
    }

    // Collection Options Bottom Sheet
    selectedCollection?.let { collection ->
        CollectionOptionsBottomSheet(
            collection = collection,
            onDismiss = { selectedCollection = null },
            onEdit = { updatedCollection: Collection ->
                viewModel.updateCollection(updatedCollection)
                selectedCollection = null
            },
            onDelete = {
                viewModel.deleteCollection(collection)
                selectedCollection = null
            }
        )
    }
}

// CollectionsCreateCollectionDialog removed (spec v4 §2.2): the shared
// com.appcodecraft.linkzary.ui.component.CreateCollectionDialog replaces it.

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionOptionsBottomSheet(
    collection: Collection,
    onDismiss: () -> Unit,
    onEdit: (Collection) -> Unit,
    onDelete: () -> Unit
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        dragHandle = { BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 32.dp)
        ) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(14.dp))
                        // Addendum #2: brand indigo tonal family only
                        .background(MaterialTheme.colorScheme.primaryContainer),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCollectionGlyph(collection.icon),
                        contentDescription = stringResource(R.string.collections_collection_icon),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = collection.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = stringResource(R.string.collections_options),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Options
            OptionItem(
                icon = Icons.Rounded.Edit,
                title = stringResource(R.string.collections_edit_collection),
                subtitle = stringResource(R.string.collections_edit_description),
                onClick = {
                    showEditDialog = true
                }
            )

            OptionItem(
                icon = Icons.Rounded.Delete,
                title = stringResource(R.string.collections_delete_collection),
                subtitle = stringResource(R.string.collections_delete_description),
                onClick = {
                    showDeleteConfirmation = true
                },
                isDestructive = true
            )
        }
    }

    // Edit Collection Dialog
    if (showEditDialog) {
        EditCollectionDialog(
            collection = collection,
            onDismiss = { showEditDialog = false },
            onSave = { updatedCollection: Collection ->
                onEdit(updatedCollection)
                showEditDialog = false
            }
        )
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmation) {
        com.appcodecraft.linkzary.ui.component.LinkzaryDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            icon = Icons.Rounded.Delete,
            title = stringResource(R.string.collections_delete_confirmation_title),
            confirmLabel = stringResource(R.string.common_delete),
            onConfirm = {
                onDelete()
                showDeleteConfirmation = false
            },
            dismissLabel = stringResource(R.string.common_cancel),
            onDismiss = { showDeleteConfirmation = false },
            destructive = true
        ) {
            Text(stringResource(R.string.collections_delete_confirmation_message, collection.name))
        }
    }
}

// Replaced custom implementation with shared component usage
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCollectionDialog(
    collection: Collection,
    onDismiss: () -> Unit,
    onSave: (Collection) -> Unit
) {
    var name by remember { mutableStateOf(collection.name) }
    var selectedColor by remember {
        mutableStateOf(try {
            android.graphics.Color.parseColor(collection.color)
        } catch (e: Exception) {
            0xFF6366F1.toInt()
        })
    }
    var selectedIcon by remember { mutableStateOf(collection.icon) }

    com.appcodecraft.linkzary.ui.component.LinkzaryDialog(
        onDismissRequest = onDismiss,
        icon = Icons.Rounded.Edit,
        title = stringResource(R.string.collections_edit_collection),
        confirmLabel = stringResource(R.string.common_save),
        onConfirm = {
            val hexColor = String.format("#%06X", selectedColor and 0xFFFFFF)
            val updatedCollection = collection.copy(
                name = name.trim(),
                color = hexColor,
                icon = selectedIcon
            )
            onSave(updatedCollection)
        },
        confirmEnabled = name.isNotBlank() && (
            name.trim() != collection.name ||
            String.format("#%06X", selectedColor and 0xFFFFFF) != collection.color ||
            selectedIcon != collection.icon
        ),
        dismissLabel = stringResource(R.string.common_cancel),
        onDismiss = onDismiss
    ) {
        CreateCollectionForm(
            name = name,
            onNameChange = { name = it },
            selectedColor = selectedColor,
            onColorSelected = { selectedColor = it },
            selectedIcon = selectedIcon,
            onIconSelected = { selectedIcon = it }
        )
    }
}

@Preview
@Composable
fun CollectionsScreenPreview() {
    LinkzaryTheme {
        Surface {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(stringResource(R.string.collections_screen_preview))
            }
        }
    }
}