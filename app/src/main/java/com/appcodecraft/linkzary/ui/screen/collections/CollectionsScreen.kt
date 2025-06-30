package com.appcodecraft.linkzary.ui.screen.collections

// Animation imports removed for better performance
// Delay import removed

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.Palette
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
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
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
import androidx.compose.ui.res.stringResource
import com.appcodecraft.linkzary.R
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
                        contentDescription = stringResource(R.string.navigation_collections),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(32.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.nav_collections),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = stringResource(R.string.collections_collections_count),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = stringResource(R.string.collections_count_info, uiState.collections.size, uiState.collectionsWithCounts.values.sum()),
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
                            contentDescription = stringResource(R.string.collections_total_links),
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
                        contentDescription = if (isGridView) stringResource(R.string.home_switch_list_view) else stringResource(R.string.home_switch_grid_view),
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
                            text = { Text(stringResource(R.string.collections_sort_name_az)) },
                            onClick = {
                                viewModel.setSortOrder(SortOrder.NAME_ASC)
                                showSortMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.SortByAlpha, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.collections_sort_name_za)) },
                            onClick = {
                                viewModel.setSortOrder(SortOrder.NAME_DESC)
                                showSortMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.SortByAlpha, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.collections_sort_most_links)) },
                            onClick = {
                                viewModel.setSortOrder(SortOrder.LINK_COUNT_DESC)
                                showSortMenu = false
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Link, contentDescription = null)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.collections_sort_recently_created)) },
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
                        contentDescription = stringResource(R.string.collections_create_collection)
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
            placeholder = { Text(stringResource(R.string.collections_search_placeholder)) },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = stringResource(R.string.common_search)
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
                            text = stringResource(R.string.collections_loading),
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
                                stringResource(R.string.collections_empty_state) else stringResource(R.string.collections_no_results),
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Text(
                            text = if (uiState.searchQuery.isBlank())
                                stringResource(R.string.collections_empty_description)
                            else stringResource(R.string.collections_no_results_description),
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
                                Text(stringResource(R.string.collections_create_collection))
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
    var showCustomColorPicker by remember { mutableStateOf(false) }
    var customColor by remember { mutableStateOf(Color(0xFF6366F1)) }
    val maxNameLength = 30
    val hapticFeedback = LocalHapticFeedback.current

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
                    contentDescription = stringResource(R.string.collections_create_collection),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.collections_create_collection))
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
                    label = { Text(stringResource(R.string.collections_collection_name)) },
                    placeholder = { Text(stringResource(R.string.collections_enter_name)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = stringResource(R.string.collections_collection_name),
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
                    text = stringResource(R.string.collections_choose_color),
                    style = MaterialTheme.typography.labelMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (showCustomColorPicker) {
                    // Custom color picker UI
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        // Color preview
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .background(customColor, RoundedCornerShape(8.dp))
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Red slider
                        Text("Red", style = MaterialTheme.typography.bodySmall)
                        Slider(
                            value = customColor.red,
                            onValueChange = { 
                                customColor = customColor.copy(red = it)
                                selectedColor = String.format("#%02X%02X%02X", 
                                    (customColor.red * 255).toInt(),
                                    (customColor.green * 255).toInt(),
                                    (customColor.blue * 255).toInt())
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = Color.Red,
                                activeTrackColor = Color.Red.copy(alpha = 0.5f)
                            )
                        )
                        
                        // Green slider
                        Text("Green", style = MaterialTheme.typography.bodySmall)
                        Slider(
                            value = customColor.green,
                            onValueChange = { 
                                customColor = customColor.copy(green = it)
                                selectedColor = String.format("#%02X%02X%02X", 
                                    (customColor.red * 255).toInt(),
                                    (customColor.green * 255).toInt(),
                                    (customColor.blue * 255).toInt())
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = Color.Green,
                                activeTrackColor = Color.Green.copy(alpha = 0.5f)
                            )
                        )
                        
                        // Blue slider
                        Text("Blue", style = MaterialTheme.typography.bodySmall)
                        Slider(
                            value = customColor.blue,
                            onValueChange = { 
                                customColor = customColor.copy(blue = it)
                                selectedColor = String.format("#%02X%02X%02X", 
                                    (customColor.red * 255).toInt(),
                                    (customColor.green * 255).toInt(),
                                    (customColor.blue * 255).toInt())
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = Color.Blue,
                                activeTrackColor = Color.Blue.copy(alpha = 0.5f)
                            )
                        )
                        
                        // Back button
                        TextButton(
                            onClick = { showCustomColorPicker = false },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Back to Presets")
                        }
                    }
                } else {
                    // Predefined colors grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(8),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(120.dp)
                    ) {
                        items(colors) { color ->
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(32.dp)
                            ) {
                                FilterChip(
                                    onClick = { 
                                        if (selectedColor != color) {
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                            selectedColor = color 
                                        }
                                    },
                                    label = { },
                                    selected = selectedColor == color,
                                    modifier = Modifier.size(32.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = Color(color.toColorInt()),
                                        selectedContainerColor = Color(color.toColorInt())
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = selectedColor == color,
                                        borderColor = if (selectedColor == color) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        borderWidth = if (selectedColor == color) 2.dp else 0.dp
                                    )
                                )
                                
                                // Selection indicator
                                if (selectedColor == color) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                        
                        // Custom color option
                        item {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { 
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        showCustomColorPicker = true 
                                        // Initialize custom color picker with current selection
                                        try {
                                            customColor = Color(selectedColor.toColorInt())
                                        } catch (e: Exception) {
                                            customColor = Color(0xFF6366F1) // Default to Indigo if parsing fails
                                        }
                                    }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = "Custom Color",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onCreate(name, selectedColor) },
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
                        imageVector = getCollectionIcon(collection.name),
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditCollectionDialog(
    collection: Collection,
    onDismiss: () -> Unit,
    onSave: (Collection) -> Unit
) {
    var name by remember { mutableStateOf(collection.name) }
    var selectedColor by remember { mutableStateOf(collection.color) }
    var showCustomColorPicker by remember { mutableStateOf(false) }
    var customColor by remember { 
        mutableStateOf(try {
            Color(collection.color.toColorInt())
        } catch (e: Exception) {
            Color(0xFF6366F1) // Default to Indigo if parsing fails
        })
    }
    val maxNameLength = 30
    val hapticFeedback = LocalHapticFeedback.current

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
                    contentDescription = stringResource(R.string.collections_edit_collection),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.collections_edit_collection))
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
                    label = { Text(stringResource(R.string.collections_collection_name)) },
                    placeholder = { Text(stringResource(R.string.collections_enter_collection_name)) },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = stringResource(R.string.collections_collection_name),
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
                    text = stringResource(R.string.collections_choose_color),
                    style = MaterialTheme.typography.labelMedium
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (showCustomColorPicker) {
                    // Custom color picker UI
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 8.dp)
                    ) {
                        // Color preview
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                                .background(customColor, RoundedCornerShape(8.dp))
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        // Red slider
                        Text("Red", style = MaterialTheme.typography.bodySmall)
                        Slider(
                            value = customColor.red,
                            onValueChange = { 
                                customColor = customColor.copy(red = it)
                                selectedColor = String.format("#%02X%02X%02X", 
                                    (customColor.red * 255).toInt(),
                                    (customColor.green * 255).toInt(),
                                    (customColor.blue * 255).toInt())
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = Color.Red,
                                activeTrackColor = Color.Red.copy(alpha = 0.5f)
                            )
                        )
                        
                        // Green slider
                        Text("Green", style = MaterialTheme.typography.bodySmall)
                        Slider(
                            value = customColor.green,
                            onValueChange = { 
                                customColor = customColor.copy(green = it)
                                selectedColor = String.format("#%02X%02X%02X", 
                                    (customColor.red * 255).toInt(),
                                    (customColor.green * 255).toInt(),
                                    (customColor.blue * 255).toInt())
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = Color.Green,
                                activeTrackColor = Color.Green.copy(alpha = 0.5f)
                            )
                        )
                        
                        // Blue slider
                        Text("Blue", style = MaterialTheme.typography.bodySmall)
                        Slider(
                            value = customColor.blue,
                            onValueChange = { 
                                customColor = customColor.copy(blue = it)
                                selectedColor = String.format("#%02X%02X%02X", 
                                    (customColor.red * 255).toInt(),
                                    (customColor.green * 255).toInt(),
                                    (customColor.blue * 255).toInt())
                            },
                            colors = SliderDefaults.colors(
                                thumbColor = Color.Blue,
                                activeTrackColor = Color.Blue.copy(alpha = 0.5f)
                            )
                        )
                        
                        // Back button
                        TextButton(
                            onClick = { showCustomColorPicker = false },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Back to Presets")
                        }
                    }
                } else {
                    // Predefined colors grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(8),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(120.dp)
                    ) {
                        items(colors) { color ->
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.size(32.dp)
                            ) {
                                FilterChip(
                                    onClick = { 
                                        if (selectedColor != color) {
                                            hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                            selectedColor = color 
                                        }
                                    },
                                    label = { },
                                    selected = selectedColor == color,
                                    modifier = Modifier.size(32.dp),
                                    colors = FilterChipDefaults.filterChipColors(
                                        containerColor = Color(color.toColorInt()),
                                        selectedContainerColor = Color(color.toColorInt())
                                    ),
                                    border = FilterChipDefaults.filterChipBorder(
                                        enabled = true,
                                        selected = selectedColor == color,
                                        borderColor = if (selectedColor == color) MaterialTheme.colorScheme.primary else Color.Transparent,
                                        borderWidth = if (selectedColor == color) 2.dp else 0.dp
                                    )
                                )
                                
                                // Selection indicator
                                if (selectedColor == color) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }
                        
                        // Custom color option
                        item {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .size(32.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                    .clickable { 
                                        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                        showCustomColorPicker = true 
                                        // Initialize custom color picker with current selection
                                        try {
                                            customColor = Color(selectedColor.toColorInt())
                                        } catch (e: Exception) {
                                            customColor = Color(0xFF6366F1) // Default to Indigo if parsing fails
                                        }
                                    }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Palette,
                                    contentDescription = "Custom Color",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
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