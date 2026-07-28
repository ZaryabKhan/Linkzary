package com.appcodecraft.linkzary.ui.screen.collections

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
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.outlined.FolderOpen
import androidx.compose.material.icons.outlined.SearchOff
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
import com.appcodecraft.linkzary.ui.component.createCollectionGradient
import com.appcodecraft.linkzary.ui.component.getCollectionIconVector
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

    // Pre-compute collection lookup map
    val collectionMap by remember(uiState.collectionsWithCounts) {
        derivedStateOf { uiState.collectionsWithCounts }
    }

    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {

        // Custom header (matches HomeScreen style)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp, top = 20.dp, bottom = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Folder,
                contentDescription = stringResource(R.string.navigation_collections),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = stringResource(R.string.nav_collections),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            // View toggle
            IconButton(
                onClick = { viewModel.toggleGridView() },
                modifier = Modifier
                    .size(40.dp)
                    .background(
                        color = if (isGridView) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.8f)
                        else Color.Transparent,
                        shape = RoundedCornerShape(10.dp)
                    )
            ) {
                Icon(
                    imageVector = if (isGridView) Icons.AutoMirrored.Filled.ViewList else Icons.Default.GridView,
                    contentDescription = if (isGridView) stringResource(R.string.home_switch_list_view) else stringResource(R.string.home_switch_grid_view),
                    tint = if (isGridView) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            }

            // Sort menu
            Box {
                IconButton(
                    onClick = { showSortMenu = true },
                    modifier = Modifier
                        .size(40.dp)
                        .background(
                            color = if (showSortMenu) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.8f)
                            else Color.Transparent,
                            shape = RoundedCornerShape(10.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Sort,
                        contentDescription = stringResource(R.string.home_sort_options),
                        tint = if (showSortMenu) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }

                DropdownMenu(
                    expanded = showSortMenu,
                    onDismissRequest = { showSortMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.collections_sort_name_az)) },
                        onClick = { viewModel.setSortOrder(SortOrder.NAME_ASC); showSortMenu = false },
                        leadingIcon = { Icon(Icons.Default.SortByAlpha, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.collections_sort_name_za)) },
                        onClick = { viewModel.setSortOrder(SortOrder.NAME_DESC); showSortMenu = false },
                        leadingIcon = { Icon(Icons.Default.SortByAlpha, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.collections_sort_most_links)) },
                        onClick = { viewModel.setSortOrder(SortOrder.LINK_COUNT_DESC); showSortMenu = false },
                        leadingIcon = { Icon(Icons.Default.Link, contentDescription = null) }
                    )
                    DropdownMenuItem(
                        text = { Text(stringResource(R.string.collections_sort_recently_created)) },
                        onClick = { viewModel.setSortOrder(SortOrder.DATE_DESC); showSortMenu = false },
                        leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) }
                    )
                }
            }
        }

        // Main content
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 72.dp)
                .padding(horizontal = 16.dp),
            contentPadding = PaddingValues(bottom = 100.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Search bar
            item(key = "search") {
                Box(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = viewModel::updateSearchQuery,
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text(stringResource(R.string.collections_search_placeholder)) },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = stringResource(R.string.common_search),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp)
                    )
                }
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
                        leadingIcon = { Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp)) }
                    )
                    if (uiState.collectionsWithCounts.values.sum() > 0) {
                        FilterChip(
                            onClick = {},
                            label = {
                                Text("${uiState.collectionsWithCounts.values.sum()} ${stringResource(R.string.collections_total_links).lowercase()}")
                            },
                            selected = false,
                            leadingIcon = { Icon(Icons.Default.Link, contentDescription = null, modifier = Modifier.size(16.dp)) }
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
                                    Icons.Outlined.FolderOpen else Icons.Outlined.SearchOff,
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
                    items(
                        items = uiState.collections.chunked(2),
                        key = { chunk -> chunk.joinToString(",") { it.id.toString() } }
                    ) { rowCollections ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowCollections.forEach { collection ->
                                CollectionCard(
                                    collection = collection,
                                    linkCount = collectionMap[collection.id] ?: 0,
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
                    items(
                        items = uiState.collections,
                        key = { it.id }
                    ) { collection ->
                        CollectionCard(
                            collection = collection,
                            linkCount = collectionMap[collection.id] ?: 0,
                            onCardClick = {
                                navController?.navigate(
                                    Screen.CollectionDetail.createRoute(collection.id.toString())
                                )
                            },
                            onMoreClick = { selectedCollection = collection }
                        )
                    }
                }
            }
        }

        // Floating Action Button (overlay, bottom-right)
        FloatingActionButton(
            onClick = { showCreateCollectionDialog = true },
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = stringResource(R.string.collections_create_collection)
            )
        }
    }

    // Error handling
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            viewModel.clearError()
        }
    }

    // Create Collection Dialog
    if (showCreateCollectionDialog) {
        CollectionsCreateCollectionDialog(
            onDismiss = { showCreateCollectionDialog = false },
            onCreate = { name: String, color: String, icon: String ->
                val colorInt = color.toColorInt()
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

// Replaced custom implementation with shared component usage
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionsCreateCollectionDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(0xFF6366F1.toInt()) }
    var selectedIcon by remember { mutableStateOf("Folder") }
    
    val hapticFeedback = LocalHapticFeedback.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CreateNewFolder,
                    contentDescription = stringResource(R.string.collections_create_collection),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.collections_create_collection))
            }
        },
        text = {
            CreateCollectionForm(
                name = name,
                onNameChange = { name = it },
                selectedColor = selectedColor,
                onColorSelected = { selectedColor = it },
                selectedIcon = selectedIcon,
                onIconSelected = { selectedIcon = it }
            )
        },
        confirmButton = {
            TextButton(
                onClick = { 
                    val hexColor = String.format("#%06X", selectedColor and 0xFFFFFF)
                    onCreate(name, hexColor, selectedIcon) 
                },
                enabled = name.isNotBlank()
            ) {
                Text(stringResource(R.string.common_create))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}

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
                        .clip(RoundedCornerShape(10.dp))
                        .background(createCollectionGradient(collection.color)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = getCollectionIconVector(collection.icon),
                        contentDescription = stringResource(R.string.collections_collection_icon),
                        tint = Color.White,
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
                icon = Icons.Default.Edit,
                title = stringResource(R.string.collections_edit_collection),
                subtitle = stringResource(R.string.collections_edit_description),
                onClick = {
                    showEditDialog = true
                }
            )

            OptionItem(
                icon = Icons.Default.Delete,
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
        AlertDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            title = { Text(stringResource(R.string.collections_delete_confirmation_title)) },
            text = {
                Text(
                    stringResource(R.string.collections_delete_confirmation_message, collection.name)
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteConfirmation = false
                    },
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.common_delete))
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { showDeleteConfirmation = false }
                ) {
                    Text(stringResource(R.string.common_cancel))
                }
            }
        )
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
    
    val hapticFeedback = LocalHapticFeedback.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = stringResource(R.string.collections_edit_collection),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.collections_edit_collection))
            }
        },
        text = {
            CreateCollectionForm(
                name = name,
                onNameChange = { name = it },
                selectedColor = selectedColor,
                onColorSelected = { selectedColor = it },
                selectedIcon = selectedIcon,
                onIconSelected = { selectedIcon = it }
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val hexColor = String.format("#%06X", selectedColor and 0xFFFFFF)
                    val updatedCollection = collection.copy(
                        name = name.trim(),
                        color = hexColor,
                        icon = selectedIcon
                    )
                    onSave(updatedCollection)
                },
                enabled = name.isNotBlank() && (
                    name.trim() != collection.name || 
                    String.format("#%06X", selectedColor and 0xFFFFFF) != collection.color ||
                    selectedIcon != collection.icon
                )
            ) {
                Text(stringResource(R.string.common_save))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
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