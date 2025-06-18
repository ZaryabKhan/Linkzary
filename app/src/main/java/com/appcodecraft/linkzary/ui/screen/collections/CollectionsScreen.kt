package com.appcodecraft.linkzary.ui.screen.collections

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
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
import androidx.navigation.NavController
import com.appcodecraft.linkzary.data.entity.Collection
import com.appcodecraft.linkzary.navigation.Screen
import com.appcodecraft.linkzary.ui.component.CollectionCard
import com.appcodecraft.linkzary.ui.theme.LinkzaryTheme
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionsScreen(
    navController: NavController? = null,
    viewModel: CollectionsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showCreateCollectionDialog by remember { mutableStateOf(false) }
    var selectedCollection by remember { mutableStateOf<Collection?>(null) }

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
                text = "Collections",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
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
        
        // Collections grid
        when {
            uiState.isLoading -> {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
            
            uiState.collections.isEmpty() -> {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = if (uiState.searchQuery.isBlank()) 
                                "No collections yet" else "No collections found",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (uiState.searchQuery.isBlank()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Create your first collection to organize links",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
            
            else -> {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    contentPadding = PaddingValues(bottom = 16.dp)
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
    
    // Error handling
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            // TODO: Show snackbar with error
            viewModel.clearError()
        }
    }
    
    // Create Collection Dialog
    if (showCreateCollectionDialog) {
        CreateCollectionDialog(
            onDismiss = { showCreateCollectionDialog = false },
            onCreate = { name, color ->
                viewModel.createCollection(name, color)
                showCreateCollectionDialog = false
            }
        )
    }
    
    // Collection Options Bottom Sheet
    selectedCollection?.let { collection ->
        CollectionOptionsBottomSheet(
            collection = collection,
            onDismiss = { selectedCollection = null },
            onEdit = { updatedCollection ->
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
fun CreateCollectionDialog(
    onDismiss: () -> Unit,
    onCreate: (String, Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(0xFF6366F1.toInt()) }
    
    val colors = listOf(
        0xFF6366F1.toInt(), // Indigo
        0xFFEF4444.toInt(), // Red
        0xFF10B981.toInt(), // Emerald
        0xFFF59E0B.toInt(), // Amber
        0xFF8B5CF6.toInt(), // Violet
        0xFF06B6D4.toInt(), // Cyan
        0xFFEC4899.toInt(), // Pink
        0xFF84CC16.toInt()  // Lime
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create Collection") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Collection Name") },
                    placeholder = { Text("Enter collection name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "Choose Color",
                    style = MaterialTheme.typography.labelMedium
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    colors.forEach { color ->
                        FilterChip(
                            onClick = { selectedColor = color },
                            label = { },
                            selected = selectedColor == color,
                            modifier = Modifier.size(32.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = androidx.compose.ui.graphics.Color(color),
                                selectedContainerColor = androidx.compose.ui.graphics.Color(color)
                            )
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
    // TODO: Implement bottom sheet with options
    // For now, just dismiss
    LaunchedEffect(Unit) {
        onDismiss()
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
                Text("Collections Screen Preview")
            }
        }
    }
}