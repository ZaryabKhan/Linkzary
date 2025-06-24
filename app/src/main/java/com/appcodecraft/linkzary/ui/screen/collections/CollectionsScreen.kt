package com.appcodecraft.linkzary.ui.screen.collections

// Animation imports removed for better performance
// Delay import removed

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
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.graphics.toColorInt
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.appcodecraft.linkzary.data.entity.Collection
import com.appcodecraft.linkzary.navigation.Screen
import com.appcodecraft.linkzary.ui.component.CollectionCard
import com.appcodecraft.linkzary.ui.component.createCollectionGradient
import com.appcodecraft.linkzary.ui.component.getCollectionIcon
import com.appcodecraft.linkzary.ui.screen.home.OptionItem
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Header with enhanced controls
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
                        imageVector = Icons.Default.Folder,
                        contentDescription = "Collections",
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Collections",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = "Collections count",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${uiState.collections.size} ${if (uiState.collections.size == 1) "collection" else "collections"}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (uiState.collectionsWithCounts.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "•",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.Link,
                            contentDescription = "Total links",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${uiState.collectionsWithCounts.values.sum()} total links",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Enhanced view mode toggle
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
                        contentDescription = if (isGridView) "Switch to list view" else "Switch to grid view",
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
                            contentDescription = "Sort options",
                            tint = if (showSortMenu) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Name A-Z") },
                            onClick = {
                                viewModel.setSortOrder(SortOrder.NAME_ASC)
                                showSortMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.SortByAlpha, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Name Z-A") },
                            onClick = {
                                viewModel.setSortOrder(SortOrder.NAME_DESC)
                                showSortMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.SortByAlpha, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Most Links") },
                            onClick = {
                                viewModel.setSortOrder(SortOrder.LINK_COUNT_DESC)
                                showSortMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Link, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Recently Created") },
                            onClick = {
                                viewModel.setSortOrder(SortOrder.DATE_DESC)
                                showSortMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Schedule, contentDescription = null)
                            }
                        )
                    }
                }

                FloatingActionButton(
                    onClick = { showCreateCollectionDialog = true },
                    modifier = Modifier.size(48.dp),
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Create collection"
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
            placeholder = { Text("Search collections...") },
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

        // Collections content
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        CircularProgressIndicator()
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Loading collections...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            uiState.collections.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(32.dp)
                    ) {
                        Icon(
                            imageVector = if (uiState.searchQuery.isBlank())
                                Icons.Outlined.FolderOpen else Icons.Outlined.SearchOff,
                            contentDescription = null,
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = if (uiState.searchQuery.isBlank())
                                "No collections yet" else "No collections found",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (uiState.searchQuery.isBlank())
                                "Create your first collection to organize and manage your saved links"
                            else "Try adjusting your search terms",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )

                        if (uiState.searchQuery.isBlank()) {
                            Spacer(modifier = Modifier.height(24.dp))

                            Button(
                                onClick = { showCreateCollectionDialog = true },
                                modifier = Modifier.fillMaxWidth(0.6f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Add,
                                    contentDescription = null,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Create Collection")
                            }
                        }
                    }
                }
            }

            else -> {
                if (isGridView) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(uiState.collections) { collection ->
                            CollectionCard(
                                collection = collection,
                                linkCount = uiState.collectionsWithCounts[collection.id] ?: 0,
                                onCardClick = {
                                    navController?.navigate(
                                        Screen.CollectionDetail.createRoute(collection.id.toString())
                                    )
                                },
                                onMoreClick = {
                                    selectedCollection = collection
                                }
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 16.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        items(uiState.collections) { collection ->
                            CollectionCard(
                                collection = collection,
                                linkCount = uiState.collectionsWithCounts[collection.id] ?: 0,
                                onCardClick = {
                                    navController?.navigate(
                                        Screen.CollectionDetail.createRoute(collection.id.toString())
                                    )
                                },
                                onMoreClick = {
                                    selectedCollection = collection
                                }
                            )
                        }
                    }
                }
            }
        }

        // Error handling with Snackbar
        uiState.error?.let { error ->
            LaunchedEffect(error) {
                // Show error message
                viewModel.clearError()
            }
        }
    }

    // Create Collection Dialog
    if (showCreateCollectionDialog) {
        CollectionsCreateCollectionDialog(
            onDismiss = { showCreateCollectionDialog = false },
            onCreate = { name: String, color: String ->
                val colorInt = color.toColorInt()
                viewModel.createCollection(name, colorInt)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionsCreateCollectionDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf("#6366F1") }
    val maxNameLength = 30

    val colors = listOf(
        "#6366F1", // Indigo
        "#EF4444", // Red
        "#10B981", // Emerald
        "#F59E0B", // Amber
        "#8B5CF6", // Violet
        "#06B6D4", // Cyan
        "#EC4899", // Pink
        "#84CC16", // Lime
        "#F97316", // Orange
        "#EAB308", // Yellow
        "#22C55E", // Green
        "#3B82F6", // Blue
        "#A855F7", // Purple
        "#E11D48", // Rose
        "#0EA5E9", // Sky
        "#64748B"  // Slate
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CreateNewFolder,
                    contentDescription = "Create collection",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Create Collection")
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
                    label = { Text("Collection Name") },
                    placeholder = { Text("Enter collection name") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = "Collection name",
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
                    text = "Choose Color",
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
                                containerColor = Color(color.toColorInt()),
                                selectedContainerColor = Color(color.toColorInt())
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
                Text("Create")
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
                        imageVector = getCollectionIcon(collection.name),
                        contentDescription = "Collection icon",
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
                        text = "Collection Options",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Options
            OptionItem(
                icon = Icons.Default.Edit,
                title = "Edit Collection",
                subtitle = "Change name and color",
                onClick = {
                    showEditDialog = true
                }
            )

            OptionItem(
                icon = Icons.Default.Delete,
                title = "Delete Collection",
                subtitle = "Remove collection and all its links",
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
            title = { Text("Delete Collection?") },
            text = {
                Text(
                    "Are you sure you want to delete \"${collection.name}\"? This action cannot be undone and will remove all links in this collection."
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
                    Text("Delete")
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
fun EditCollectionDialog(
    collection: Collection,
    onDismiss: () -> Unit,
    onSave: (Collection) -> Unit
) {
    var name by remember { mutableStateOf(collection.name) }
    var selectedColor by remember { mutableStateOf(collection.color) }
    val maxNameLength = 30

    val colors = listOf(
        "#6366F1", // Indigo
        "#EF4444", // Red
        "#10B981", // Emerald
        "#F59E0B", // Amber
        "#8B5CF6", // Violet
        "#06B6D4", // Cyan
        "#EC4899", // Pink
        "#84CC16", // Lime
        "#F97316", // Orange
        "#EAB308", // Yellow
        "#22C55E", // Green
        "#3B82F6", // Blue
        "#A855F7", // Purple
        "#E11D48", // Rose
        "#0EA5E9", // Sky
        "#64748B"  // Slate
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Edit,
                    contentDescription = "Edit collection",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text("Edit Collection")
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
                    label = { Text("Collection Name") },
                    placeholder = { Text("Enter collection name") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = "Collection name",
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
                    text = "Choose Color",
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
                                containerColor = Color(color.toColorInt()),
                                selectedContainerColor = Color(color.toColorInt())
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
                onClick = {
                    val updatedCollection = collection.copy(
                        name = name.trim(),
                        color = selectedColor
                    )
                    onSave(updatedCollection)
                },
                enabled = name.isNotBlank() && (name.trim() != collection.name || selectedColor != collection.color)
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
fun CollectionsScreenPreview() {
    LinkzaryTheme {
        Surface {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("Collections Screen Preview")
            }
        }
    }
}