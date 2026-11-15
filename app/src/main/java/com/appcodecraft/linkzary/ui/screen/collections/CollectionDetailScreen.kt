package com.appcodecraft.linkzary.ui.screen.collections

import androidx.compose.foundation.background
import androidx.compose.ui.input.nestedscroll.nestedScroll
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items as lazyColumnItems
import androidx.compose.foundation.lazy.itemsIndexed as lazyColumnItemsIndexed
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.DriveFileMove
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.automirrored.rounded.ViewList
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Clear
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CreateNewFolder
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.FolderOpen
import androidx.compose.material.icons.rounded.GridView
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.SearchOff
import androidx.compose.material.icons.rounded.Tag
import androidx.compose.material.icons.rounded.Title
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.appcodecraft.linkzary.R
import com.appcodecraft.linkzary.data.entity.SavedLink
import com.appcodecraft.linkzary.ui.component.LinkCard
import com.appcodecraft.linkzary.ui.component.EditLinkDialog
import com.appcodecraft.linkzary.ui.component.OptionItem
import com.appcodecraft.linkzary.ui.component.CollectionSelectionDialog
import com.appcodecraft.linkzary.ui.component.getCollectionGlyph
import com.appcodecraft.linkzary.ui.screen.home.HomeViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CollectionDetailScreen(
    collectionId: String,
    navController: NavController,
    viewModel: CollectionDetailViewModel = hiltViewModel()
) {
    // Get the HomeViewModel for collection creation
    val homeViewModel: HomeViewModel = hiltViewModel()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current

    // Opens a URL without crashing: uriHandler.openUri throws
    // IllegalArgumentException on malformed or scheme-less input (the
    // v1.1.5 crash cluster). Uses the fully guarded UrlOpener (direct
    // ACTION_VIEW, trims input, fixes missing scheme) and toasts on failure
    // so problems surface instead of silently doing nothing.
    val openSafely: (String) -> Unit = { url ->
        com.appcodecraft.linkzary.util.UrlOpener.openOrToast(context, url)
    }

    var selectedLink by remember { mutableStateOf<SavedLink?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    val isGridView = uiState.isGridView
    var showAddLinkDialog by remember { mutableStateOf(false) }
    var showEditCollectionDialog by remember { mutableStateOf(false) }

    val detailListState = androidx.compose.foundation.lazy.rememberLazyListState()

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

    // Spec v2 §2.1/§5: root paints its surface behind the status bar; the
    // hero header (container tone, never alpha tint) sits directly on top.
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainerLow)) {
        // Spec v3 §2.2: FAB hide/return scroll behavior
        val fabBehavior = com.appcodecraft.linkzary.ui.component.rememberLinkzaryFabScrollBehavior()
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
        // Hero header (spec v3 §3.3): tinted with THIS collection's hue pair
        // (hash→harmonized HSL), not a single indigo for all collections.
        val huePair = uiState.collection?.id?.let {
            com.appcodecraft.linkzary.ui.component.collectionHuePair(it)
        }
        val heroContainer = huePair?.first ?: MaterialTheme.colorScheme.primaryContainer
        val onHeroContainer = huePair?.second ?: MaterialTheme.colorScheme.onPrimaryContainer
        com.appcodecraft.linkzary.ui.component.LinkzaryHeroHeader(
            title = uiState.collection?.name ?: stringResource(R.string.collections_collection),
            countLabel = stringResource(R.string.collections_links_count, filteredLinks.size),
            heroContainer = heroContainer,
            onHeroContainer = onHeroContainer,
            onBack = { navController.navigateUp() },
            actions = {
                IconButton(onClick = { showEditCollectionDialog = true }) {
                    Icon(
                        imageVector = Icons.Rounded.Edit,
                        contentDescription = stringResource(R.string.collections_edit_collection),
                        tint = onHeroContainer
                    )
                }
                com.appcodecraft.linkzary.ui.component.ViewModeToggle(
                    isGridView = isGridView,
                    onToggle = { viewModel.toggleGridView() }
                )
            }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {

        Spacer(modifier = Modifier.height(12.dp))

        // One search style app-wide (spec v2 §2.2)
        com.appcodecraft.linkzary.ui.component.LinkzarySearchBar(
            query = searchQuery,
            onQueryChange = { searchQuery = it },
            placeholder = stringResource(R.string.collections_search_links)
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Content - wrapped with weight to fill remaining space
        Box(
            modifier = Modifier
                .weight(1f)
                .nestedScroll(fabBehavior.nestedScrollConnection)
        ) {
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
                        text = uiState.error ?: stringResource(R.string.common_unknown_error),
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
                            imageVector = if (searchQuery.isNotEmpty()) Icons.Rounded.SearchOff else Icons.Rounded.Link,
                            contentDescription = null,
                            modifier = Modifier.size(48.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (searchQuery.isNotEmpty()) stringResource(R.string.collections_no_matching_links) else stringResource(R.string.collections_no_links),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = if (searchQuery.isNotEmpty()) stringResource(R.string.collections_try_different_search) else stringResource(R.string.collections_add_links_to_start),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
            else -> {
                if (isGridView) {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 96.dp)
                    ) {
                        itemsIndexed(filteredLinks, key = { _, link -> link.id }) { index, link ->
                            LinkCard(
                                link = link,
                                collectionId = uiState.collection?.id,
                                collectionName = uiState.collection?.name,
                                isMultiSelectMode = uiState.isMultiSelectMode,
                                isSelected = uiState.selectedLinks.contains(link.id),
                                onCardClick = {
                                    if (uiState.isMultiSelectMode) {
                                        viewModel.toggleLinkSelection(link.id)
                                    } else {
                                        openSafely(link.url)
                                    }
                                },
                                onMoreClick = {
                                    selectedLink = link
                                },
                                onLongPress = {
                                    viewModel.startMultiSelectWithToggle(link.id)
                                },
                                onSelectClick = {
                                    viewModel.toggleLinkSelection(link.id)
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .staggeredEntrance(index)
                            )
                        }
                    }
                } else {
                    LazyColumn(
                        state = detailListState,
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(bottom = 96.dp)
                    ) {
                        lazyColumnItemsIndexed(filteredLinks, key = { _, link -> link.id }) { index, link ->
                            LinkCard(
                                link = link,
                                collectionId = uiState.collection?.id,
                                collectionName = uiState.collection?.name,
                                isMultiSelectMode = uiState.isMultiSelectMode,
                                isSelected = uiState.selectedLinks.contains(link.id),
                                onCardClick = {
                                    if (uiState.isMultiSelectMode) {
                                        viewModel.toggleLinkSelection(link.id)
                                    } else {
                                        openSafely(link.url)
                                    }
                                },
                                onMoreClick = {
                                    selectedLink = link
                                },
                                onLongPress = {
                                    viewModel.startMultiSelectWithToggle(link.id)
                                },
                                onSelectClick = {
                                    viewModel.toggleLinkSelection(link.id)
                                },
                                modifier = Modifier.staggeredEntrance(index)
                            )
                        }
                    }
                }
            }
        }
        }
        } // Column (padded content)
    }

        // Add Floating Action Button for adding bookmarks directly to this collection
        // (standard M3 extended FAB)
        if (!uiState.isMultiSelectMode) {
            com.appcodecraft.linkzary.ui.component.LinkzaryExtendedFab(
                label = stringResource(R.string.home_add_link),
                onClick = { showAddLinkDialog = true },
                behavior = fabBehavior,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
            )
        }

        // Contextual Action Bar for Multi-Select Mode
        if (uiState.isMultiSelectMode) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp),
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(16.dp),
                shadowElevation = 8.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = stringResource(R.string.multi_select_count_selected, uiState.selectedLinks.size),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = { viewModel.batchDeleteLinks() },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = stringResource(R.string.multi_select_delete_selected),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }

                        IconButton(
                            onClick = { viewModel.exitMultiSelectMode() },
                            modifier = Modifier.size(40.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = stringResource(R.string.multi_select_cancel_selection),
                                tint = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }
        }
    }

    // Batch operation message
    LaunchedEffect(uiState.batchOperationMessage) {
        uiState.batchOperationMessage?.let {
            viewModel.clearBatchOperationMessage()
        }
    }

    // Edit Collection Dialog
    if (showEditCollectionDialog && uiState.collection != null) {
        EditCollectionDialog(
            collection = uiState.collection!!,
            onDismiss = { showEditCollectionDialog = false },
            onSave = { updatedCollection ->
                viewModel.updateCollection(updatedCollection)
                showEditCollectionDialog = false
            }
        )
    }

    // Link options bottom sheet
    selectedLink?.let { link ->
        CollectionLinkOptionsBottomSheet(
            link = link,
            onDismiss = { selectedLink = null },
            onEdit = { updatedLink ->
                viewModel.editLink(updatedLink)
                selectedLink = null
            },
            onDelete = {
                viewModel.deleteLink(link.id)
                selectedLink = null
            },
            onMoveToCollection = { collectionId ->
                if (collectionId != null) {
                    viewModel.moveToCollection(link.id, collectionId)
                }
                selectedLink = null
            },
            homeViewModel = homeViewModel
        )
    }


    // Add Link Dialog
    if (showAddLinkDialog) {
        AddLinkToCollectionDialog(
            onDismiss = { showAddLinkDialog = false },
            onSave = { url ->
                viewModel.addLinkToCollection(url, collectionId.toLongOrNull())
                showAddLinkDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddLinkToCollectionDialog(
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

    com.appcodecraft.linkzary.ui.component.LinkzaryDialog(
        onDismissRequest = onDismiss,
        icon = Icons.Rounded.Link,
        title = stringResource(R.string.home_add_link),
        confirmLabel = stringResource(R.string.common_save),
        onConfirm = { onSave(url) },
        confirmEnabled = isValidUrl && url.isNotBlank(),
        dismissLabel = stringResource(R.string.common_cancel),
        onDismiss = onDismiss
    ) {
        OutlinedTextField(
            value = url,
            onValueChange = {
                url = it
                isValidUrl = validateUrl(it)
            },
            label = { Text(stringResource(R.string.home_url)) },
            placeholder = { Text("https://example.com") },
            modifier = Modifier.fillMaxWidth(),
            isError = !isValidUrl && url.isNotBlank(),
            supportingText = {
                if (!isValidUrl && url.isNotBlank()) {
                    Text(stringResource(R.string.add_link_invalid_url))
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
        )
    }
}

// Using LinkOptionsBottomSheet from HomeScreen.kt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CollectionLinkOptionsBottomSheet(
    link: SavedLink,
    onDismiss: () -> Unit,
    onEdit: (SavedLink) -> Unit,
    onDelete: () -> Unit,
    onMoveToCollection: (Long?) -> Unit,
    homeViewModel: HomeViewModel
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
                text = stringResource(R.string.collections_link_options),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 24.dp)
            )

            // Edit option
            OptionItem(
                icon = Icons.Rounded.Edit,
                title = stringResource(R.string.collections_edit_link),
                subtitle = stringResource(R.string.collections_edit_link_description),
                onClick = { showEditDialog = true }
            )

            // Move to Collection option
            OptionItem(
                icon = Icons.AutoMirrored.Rounded.DriveFileMove,
                title = stringResource(R.string.collections_move_to_collection),
                subtitle = stringResource(R.string.collections_move_description),
                onClick = { showCollectionDialog = true }
            )

            // Delete option
            OptionItem(
                icon = Icons.Rounded.Delete,
                title = stringResource(R.string.common_delete),
                subtitle = stringResource(R.string.collections_delete_link_description),
                onClick = { showDeleteConfirmation = true },
                isDestructive = true
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    // Edit Dialog
    if (showEditDialog) {
        val editorTags by homeViewModel.allTags.collectAsStateWithLifecycle()
        EditLinkDialog(
            link = link,
            onDismiss = { showEditDialog = false },
            onSave = { updatedLink ->
                onEdit(updatedLink)
                showEditDialog = false
                onDismiss()
            },
            allTags = editorTags
        )
    }

    // Collection Selection Dialog
    if (showCollectionDialog) {
        var showCreateDialog by remember { mutableStateOf(false) }
        val allCollections by homeViewModel.combinedUiState.collectAsStateWithLifecycle()

        CollectionSelectionDialog(
            currentCollectionId = link.collectionId,
            allCollections = allCollections.allCollections,
            onDismiss = { showCollectionDialog = false },
            onCollectionSelected = { collectionId ->
                onMoveToCollection(collectionId)
                showCollectionDialog = false
                onDismiss()
            },
            onCreateNew = { showCreateDialog = true }
        )

        if (showCreateDialog) {
            com.appcodecraft.linkzary.ui.component.CreateCollectionDialog(
                onDismiss = { showCreateDialog = false },
                onCreate = { name: String, color: Int, icon: String ->
                    // Use the HomeViewModel from the parent composable
                    homeViewModel.createCollection(name = name, color = color, icon = icon) { newCollectionId ->
                        // Move the link to the new collection
                        // Always move (handles both null and non-null old collectionId)
                        if (link.collectionId != newCollectionId) {
                            onMoveToCollection(newCollectionId)
                        }
                    }
                    showCreateDialog = false
                }
            )
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirmation) {
        com.appcodecraft.linkzary.ui.component.LinkzaryDialog(
            onDismissRequest = { showDeleteConfirmation = false },
            icon = Icons.Rounded.Delete,
            title = stringResource(R.string.collections_delete_link),
            confirmLabel = stringResource(R.string.common_delete),
            onConfirm = {
                onDelete()
                showDeleteConfirmation = false
                onDismiss()
            },
            dismissLabel = stringResource(R.string.common_cancel),
            onDismiss = { showDeleteConfirmation = false },
            destructive = true
        ) {
            Text(stringResource(R.string.collections_delete_link_confirmation))
        }
    }
}

/**
 * Staggered fade/rise entrance for link cards (spec §5/§6): each item fades
 * in and rises 40dp, delayed 40ms per index (capped at 8 steps) so lists
 * cascade instead of popping in all at once.
 */
@Composable
private fun Modifier.staggeredEntrance(index: Int): Modifier {
    val entrance = remember { androidx.compose.animation.core.Animatable(0f) }
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay((index.coerceAtMost(8)) * 40L)
        entrance.animateTo(
            1f,
            animationSpec = androidx.compose.animation.core.tween(250)
        )
    }
    return graphicsLayer {
        alpha = entrance.value
        translationY = (1f - entrance.value) * 40f
    }
}
