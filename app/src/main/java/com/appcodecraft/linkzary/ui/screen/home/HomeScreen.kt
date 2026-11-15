package com.appcodecraft.linkzary.ui.screen.home

import android.util.Log
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DriveFileMove
import androidx.compose.material.icons.automirrored.filled.Notes
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.OpenInBrowser
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CreateNewFolder
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Link
import androidx.compose.material.icons.filled.MarkEmailUnread
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SortByAlpha
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalFocusManager
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.appcodecraft.linkzary.R
import com.appcodecraft.linkzary.data.entity.SavedLink
import com.appcodecraft.linkzary.navigation.Screen
import com.appcodecraft.linkzary.ui.component.BookmarkCard
import com.appcodecraft.linkzary.ui.component.CreateCollectionForm
import com.appcodecraft.linkzary.ui.component.EditLinkDialog
import com.appcodecraft.linkzary.ui.component.EmptyStateIllustration
import com.appcodecraft.linkzary.ui.component.OptionItem
import com.appcodecraft.linkzary.ui.component.CollectionSelectionDialog
import com.appcodecraft.linkzary.ui.component.SkeletonBookmarkCard
import com.appcodecraft.linkzary.ui.component.SmallCollectionCard
import com.appcodecraft.linkzary.ui.component.getCollectionGlyph
import com.appcodecraft.linkzary.ui.theme.LinkzaryTheme

// region Dialogs and Bottom Sheets
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

    // Spec §6 editor: ModalBottomSheet (16dp corners = shapes.medium, drag
    // handle) instead of AlertDialog; save is a full-width primary pill pinned
    // to the sheet bottom.
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = MaterialTheme.shapes.medium,
        dragHandle = {
            Box(
                modifier = Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 32.dp, height = 4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f),
                        shape = androidx.compose.foundation.shape.CircleShape
                    )
            )
        },
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .navigationBarsPadding()
        ) {
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
                Text(
                    stringResource(R.string.add_link_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                shape = MaterialTheme.shapes.small,
                singleLine = true,
                isError = !isValidUrl && url.isNotEmpty(),
                supportingText = if (!isValidUrl && url.isNotEmpty()) {
                    { Text(stringResource(R.string.add_link_invalid_url), color = MaterialTheme.colorScheme.error) }
                } else null
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Full-width primary save pill, pinned to the sheet bottom
            Button(
                onClick = { onSave(url) },
                enabled = url.isNotBlank() && isValidUrl,
                shape = androidx.compose.foundation.shape.CircleShape,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    stringResource(R.string.add_link_save),
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }

            TextButton(
                onClick = onDismiss,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.add_link_cancel))
            }

            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateCollectionDialog(
    onDismiss: () -> Unit,
    onCreate: (String, Int, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var selectedColor by remember { mutableStateOf(0xFF6366F1.toInt()) }
    var selectedIcon by remember { mutableStateOf("Folder") }
    val maxNameLength = 30
    val hapticFeedback = LocalHapticFeedback.current

    val colors = listOf(
        0xFF6366F1.toInt(),
        0xFFEF4444.toInt(),
        0xFF10B981.toInt(),
        0xFFF59E0B.toInt(),
        0xFF8B5CF6.toInt(),
        0xFF06B6D4.toInt(),
        0xFFEC4899.toInt(),
        0xFF84CC16.toInt(),
        0xFFF97316.toInt(),
        0xFFEAB308.toInt(),
        0xFF22C55E.toInt(),
        0xFF3B82F6.toInt(),
        0xFFA855F7.toInt(),
        0xFFE11D48.toInt(),
        0xFF0EA5E9.toInt(),
        0xFF64748B.toInt()
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
                onClick = { onCreate(name, selectedColor, selectedIcon) },
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


// region Reminders
/**
 * Two-step reminder picker: pick a date, then a time.
 * Calls [onReminderSelected] with the epoch millis of the chosen moment.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReminderPickerDialog(
    onDismiss: () -> Unit,
    onReminderSelected: (Long) -> Unit
) {
    val state = remember { mutableStateOf(0) } // 0 = date step, 1 = time step
    var pickedDateMillis by remember { mutableStateOf(0L) }

    if (state.value == 0) {
        val datePickerState = rememberDatePickerState(initialSelectedDateMillis = System.currentTimeMillis())
        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            pickedDateMillis = millis
                            state.value = 1
                        }
                    },
                    enabled = datePickerState.selectedDateMillis != null
                ) { Text(stringResource(R.string.common_next)) }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    } else {
        val timeState = rememberTimePickerState(initialHour = 9, initialMinute = 0, is24Hour = true)
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(stringResource(R.string.reminder_pick_time)) },
            text = {
                Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                    TimePicker(state = timeState)
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    // DatePicker returns UTC midnight for the selected date,
                    // but Calendar here runs in the device timezone: reusing
                    // those millis directly shifted the reminder a full day in
                    // timezones west of UTC ("wrong date" bug). Take the
                    // UTC date parts and rebuild them in local time instead.
                    val pickedUtc = java.util.Calendar.getInstance(
                        java.util.TimeZone.getTimeZone("UTC")
                    ).apply { timeInMillis = pickedDateMillis }
                    val calendar = java.util.Calendar.getInstance().apply {
                        set(
                            pickedUtc.get(java.util.Calendar.YEAR),
                            pickedUtc.get(java.util.Calendar.MONTH),
                            pickedUtc.get(java.util.Calendar.DAY_OF_MONTH),
                            timeState.hour,
                            timeState.minute,
                            0
                        )
                        set(java.util.Calendar.MILLISECOND, 0)
                        // If the picked time already passed today, fire tomorrow.
                        if (timeInMillis < System.currentTimeMillis()) {
                            add(java.util.Calendar.DATE, 1)
                        }
                    }
                    onReminderSelected(calendar.timeInMillis)
                }) { Text(stringResource(R.string.reminder_schedule)) }
            },
            dismissButton = {
                TextButton(onClick = { state.value = 0 }) { Text(stringResource(R.string.common_back)) }
            }
        )
    }
}
// endregion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LinkOptionsBottomSheet(
    link: SavedLink,
    onDismiss: () -> Unit,
    onEdit: (SavedLink) -> Unit,
    onDelete: () -> Unit,
    onTogglePin: () -> Unit,
    onMoveToCollection: (Long?) -> Unit,
    onReaderMode: () -> Unit,
    onToggleOffline: (SavedLink) -> Unit,
    onToggleReadStatus: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showDeleteConfirmation by remember { mutableStateOf(false) }
    var showCollectionDialog by remember { mutableStateOf(false) }
    var showReminderPicker by remember { mutableStateOf(false) }
    val context = androidx.compose.ui.platform.LocalContext.current

    // Ask for the notification permission when the user first schedules a reminder
    val notificationPermissionLauncher = androidx.activity.compose.rememberLauncherForActivityResult(
        androidx.activity.result.contract.ActivityResultContracts.RequestPermission()
    ) { }
    androidx.compose.runtime.LaunchedEffect(showReminderPicker) {
        if (showReminderPicker && android.os.Build.VERSION.SDK_INT >= 33) {
            notificationPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
        }
    }

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

            OptionItem(
                icon = Icons.Default.Edit,
                title = stringResource(R.string.link_edit_title),
                subtitle = stringResource(R.string.link_edit_subtitle),
                onClick = { showEditDialog = true }
            )

            if (link.isOfflineAvailable) {
                OptionItem(
                    icon = Icons.AutoMirrored.Filled.Notes,
                    title = stringResource(R.string.reader_mode),
                    subtitle = stringResource(R.string.reader_mode_subtitle),
                    onClick = {
                        onReaderMode()
                        onDismiss()
                    }
                )
            }

            OptionItem(
                icon = Icons.Default.Star,
                title = if (link.isOfflineAvailable) stringResource(R.string.remove_from_offline) else stringResource(R.string.save_for_offline),
                subtitle = if (link.isOfflineAvailable) "Remove offline content" else "Save content for offline reading",
                onClick = {
                    onToggleOffline(link)
                    onDismiss()
                }
            )

            OptionItem(
                icon = when (link.readStatus) {
                    "UNREAD" -> Icons.Default.Done
                    "READ" -> Icons.Default.Archive
                    "ARCHIVED" -> Icons.Default.MarkEmailUnread
                    else -> Icons.Default.Done
                },
                title = when (link.readStatus) {
                    "UNREAD" -> stringResource(R.string.link_mark_read_title)
                    "READ" -> stringResource(R.string.link_archive_title)
                    "ARCHIVED" -> stringResource(R.string.link_mark_unread_title)
                    else -> stringResource(R.string.link_mark_read_title)
                },
                subtitle = when (link.readStatus) {
                    "UNREAD" -> stringResource(R.string.link_mark_read_subtitle)
                    "READ" -> stringResource(R.string.link_archive_subtitle)
                    "ARCHIVED" -> stringResource(R.string.link_mark_unread_subtitle)
                    else -> stringResource(R.string.link_mark_read_subtitle)
                },
                onClick = {
                    onToggleReadStatus()
                    onDismiss()
                }
            )

            OptionItem(
                icon = if (link.isPinned) Icons.Default.PushPin else Icons.Outlined.PushPin,
                title = if (link.isPinned) stringResource(R.string.link_unpin_title) else stringResource(R.string.link_pin_title),
                subtitle = if (link.isPinned) stringResource(R.string.link_unpin_subtitle) else stringResource(R.string.link_pin_subtitle),
                onClick = {
                    onTogglePin()
                    onDismiss()
                }
            )

            OptionItem(
                icon = Icons.Default.Alarm,
                title = if (link.reminderAt != null) stringResource(R.string.reminder_remove) else stringResource(R.string.reminder_set),
                subtitle = if (link.reminderAt != null) {
                    stringResource(R.string.reminder_set_for, viewModel.formatReminderDate(link))
                } else {
                    stringResource(R.string.reminder_subtitle)
                },
                onClick = {
                    if (link.reminderAt != null) {
                        viewModel.setReminder(link, null)
                        onDismiss()
                    } else {
                        showReminderPicker = true
                    }
                }
            )

            OptionItem(
                icon = Icons.Default.OpenInBrowser,
                title = stringResource(R.string.link_open_browser_title),
                subtitle = stringResource(R.string.link_open_browser_subtitle),
                onClick = {
                    // ACTION_VIEW with an https scheme lets Android route the
                    // link to the user's default (or chosen) browser instead of
                    // an in-app WebView. UrlOpener guards the launch so a
                    // malformed URL can never crash the sheet, and toasts when
                    // no app can handle the link.
                    com.appcodecraft.linkzary.util.UrlOpener.openOrToast(context, link.url)
                    onDismiss()
                }
            )

            OptionItem(
                icon = Icons.Default.Share,
                title = stringResource(R.string.share_link_title),
                subtitle = stringResource(R.string.share_link_subtitle),
                onClick = {
                    val shareIntent = android.content.Intent(android.content.Intent.ACTION_SEND).apply {
                        type = "text/plain"
                        putExtra(android.content.Intent.EXTRA_TEXT, "${link.title}\n${link.url}")
                    }
                    context.startActivity(android.content.Intent.createChooser(shareIntent, null))
                    onDismiss()
                }
            )

            OptionItem(
                icon = Icons.AutoMirrored.Filled.DriveFileMove,
                title = stringResource(R.string.link_move_title),
                subtitle = stringResource(R.string.link_move_subtitle),
                onClick = { showCollectionDialog = true }
            )

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

    if (showEditDialog) {
        val editorTags by viewModel.allTags.collectAsStateWithLifecycle()
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

    if (showCollectionDialog) {
        var showCreateDialog by remember { mutableStateOf(false) }
        val allCollections by viewModel.combinedUiState.collectAsStateWithLifecycle()

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
            CreateCollectionDialog(
                onDismiss = { showCreateDialog = false },
                onCreate = { name, color, icon ->
                    viewModel.createCollection(name = name, color = color, icon = icon) { collectionId ->
                        onMoveToCollection(collectionId)
                    }
                    showCreateDialog = false
                }
            )
        }
    }

    if (showReminderPicker) {
        ReminderPickerDialog(
            onDismiss = { showReminderPicker = false },
            onReminderSelected = { triggerAtMillis ->
                viewModel.setReminder(link, triggerAtMillis)
                showReminderPicker = false
                onDismiss()
            }
        )
    }

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

@Preview
/**
 * Scale-in leading icon for selected chips (spec §6): pops in over 150ms
 * each time the chip becomes selected (the composable enters composition
 * only while selected, so the animation fires on every selection).
 */
@Composable
private fun ChipLeadingCheck(
    imageVector: ImageVector,
    contentDescription: String? = null
) {
    val scale = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        scale.animateTo(1f, animationSpec = tween(150))
    }
    Icon(
        imageVector = imageVector,
        contentDescription = contentDescription,
        tint = MaterialTheme.colorScheme.onSecondaryContainer,
        modifier = Modifier
            .size(16.dp)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
            }
    )
}

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
// endregion

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController? = null,
    sharedUrl: String? = null,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.combinedUiState.collectAsStateWithLifecycle()
    val context = androidx.compose.ui.platform.LocalContext.current
    var showAddLinkDialog by remember { mutableStateOf(false) }
    var selectedLink by remember { mutableStateOf<SavedLink?>(null) }
    val isGridView by viewModel.isGridView.collectAsState()
    var selectedTags by remember { mutableStateOf(setOf<String>()) }
    var showSortMenu by remember { mutableStateOf(false) }
    var showCollectionPicker by remember { mutableStateOf(false) }
    var showCreateCollectionDialog by remember { mutableStateOf(false) }

    // Local search state
    var localSearchQuery by remember { mutableStateOf("") }
    var isSearchFocused by remember { mutableStateOf(false) }
    val focusManager = LocalFocusManager.current
    val searchHistory by viewModel.searchHistory.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // First-run coach-mark (audit #8): exactly once, only with zero links
    val hasSeenCoachMark by viewModel.hasSeenCoachMark.collectAsStateWithLifecycle()
    val showCoachMark = !uiState.isLoading && uiState.links.isEmpty() && !hasSeenCoachMark

    // Subtle crossfade when the grid/list toggle flips (audit #7)
    val listCrossfade = remember { Animatable(1f) }
    LaunchedEffect(isGridView) {
        listCrossfade.snapTo(0f)
        listCrossfade.animateTo(1f, tween(durationMillis = 250))
    }

    // Sync local search query with UI state
    LaunchedEffect(uiState.searchQuery) {
        if (localSearchQuery != uiState.searchQuery) {
            localSearchQuery = uiState.searchQuery
        }
    }

    // Handle shared URL
    LaunchedEffect(sharedUrl) {
        if (!sharedUrl.isNullOrBlank()) {
            viewModel.saveSharedLink(sharedUrl)
        }
    }

    // Compute filtered and chunked links with derivedStateOf for better recomposition
    val filteredLinks by remember(uiState.links, selectedTags) {
        derivedStateOf {
            if (selectedTags.isEmpty()) {
                uiState.links
            } else {
                uiState.links.filter { link ->
                    val linkTags = link.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    selectedTags.any { selectedTag -> linkTags.contains(selectedTag) }
                }
            }
        }
    }

    val chunkedLinks by remember(filteredLinks) {
        derivedStateOf { filteredLinks.chunked(2) }
    }

    // All tags for filter
    val allTags by remember(uiState.links) {
        derivedStateOf {
            uiState.links.flatMap { link ->
                link.tags.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            }.distinct().sorted()
        }
    }

    // Pre-compute collection lookup map for O(1) lookups instead of linear search
    val collectionMap by remember(uiState.allCollections) {
        derivedStateOf {
            uiState.allCollections.associateBy { it.id }
        }
    }

    // M3 TopAppBar (audit #5 + spec §6): pinned — Home must NOT collapse,
    // content scrolls under a fixed header (enterAlways caused jank).
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    // List state drives the FAB morph (cookie scallop at rest, pill when scrolled)
    val listState = androidx.compose.foundation.lazy.rememberLazyListState()
    val isScrolled by remember {
        derivedStateOf {
            listState.firstVisibleItemIndex > 0 ||
                listState.firstVisibleItemScrollOffset > 200
        }
    }

    // Outer Box only positions overlays (action bar, coach-mark, FAB);
    // the app bar and the list must be siblings in a Column so they stack
    // instead of drawing over each other (hotfix for the batch-2 overlap).
    // Spec §6: Home canvas = surfaceContainerLow (cards sit on surfaceContainerLowest).
    Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceContainerLow)) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection)
    ) {

    if (!uiState.isMultiSelectMode) {
        com.appcodecraft.linkzary.ui.component.LinkzaryTopBar(
            title = stringResource(R.string.home_linkzary_title),
            leadingGlyph = Icons.Default.Bookmark,
            actions = {
                // View toggle (spec v2 §2.4)
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
                            imageVector = Icons.AutoMirrored.Filled.Sort,
                            contentDescription = stringResource(R.string.home_sort_options),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.home_pinned_first)) },
                            onClick = { viewModel.setSortOrder(LinkSortOrder.PINNED_FIRST); showSortMenu = false },
                            leadingIcon = { Icon(Icons.Default.PushPin, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.home_newest_first)) },
                            onClick = { viewModel.setSortOrder(LinkSortOrder.DATE_DESC); showSortMenu = false },
                            leadingIcon = { Icon(Icons.Default.Schedule, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.home_oldest_first)) },
                            onClick = { viewModel.setSortOrder(LinkSortOrder.DATE_ASC); showSortMenu = false },
                            leadingIcon = { Icon(Icons.Default.History, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.home_title_a_z)) },
                            onClick = { viewModel.setSortOrder(LinkSortOrder.TITLE_ASC); showSortMenu = false },
                            leadingIcon = { Icon(Icons.Default.SortByAlpha, contentDescription = null) }
                        )
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.home_title_z_a)) },
                            onClick = { viewModel.setSortOrder(LinkSortOrder.TITLE_DESC); showSortMenu = false },
                            leadingIcon = { Icon(Icons.Default.SortByAlpha, contentDescription = null) }
                        )
                    }
                }
            },
            scrollBehavior = scrollBehavior,
            containerColor = MaterialTheme.colorScheme.surfaceContainerLow
        )
    }

    // Main content with LazyColumn
    LazyColumn(
        state = listState,
        modifier = Modifier
            .fillMaxSize()
            // No app bar in multi-select: pad for the status bar ourselves
            .then(if (uiState.isMultiSelectMode) Modifier.statusBarsPadding() else Modifier)
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(bottom = if (uiState.isMultiSelectMode) 140.dp else 100.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
            // Search bar (spec v2 §2.2): the one shared search, same field on
            // every screen. History entries drop in below while focused.
            item(key = "search") {
                com.appcodecraft.linkzary.ui.component.LinkzarySearchBar(
                    query = localSearchQuery,
                    onQueryChange = { newQuery ->
                        localSearchQuery = newQuery
                        viewModel.updateSearchQuery(newQuery)
                    },
                    onClear = {
                        localSearchQuery = ""
                        viewModel.updateSearchQuery("")
                    },
                    onFocusChange = { focused ->
                        if (!focused && localSearchQuery.isNotBlank()) {
                            viewModel.saveSearchQuery(localSearchQuery)
                        }
                        isSearchFocused = focused
                    },
                    placeholder = stringResource(R.string.home_search_bookmarks),
                    modifier = Modifier.fillMaxWidth(),
                    suggestionContent = {
                        // History entries while focused with an empty query
                        if (isSearchFocused && localSearchQuery.isEmpty()) {
                            searchHistory.take(5).forEach { historyQuery ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable {
                                            localSearchQuery = historyQuery
                                            viewModel.updateSearchQuery(historyQuery)
                                            isSearchFocused = false
                                            focusManager.clearFocus()
                                        }
                                        .padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Icon(
                                        Icons.Default.History,
                                        contentDescription = null,
                                        modifier = Modifier.size(16.dp),
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text(
                                        text = historyQuery,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        modifier = Modifier.weight(1f)
                                    )
                                    IconButton(
                                        onClick = { viewModel.removeSearchQuery(historyQuery) },
                                        modifier = Modifier.size(24.dp)
                                    ) {
                                        Icon(
                                            Icons.Default.Close,
                                            contentDescription = "Remove",
                                            modifier = Modifier.size(14.dp),
                                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                )
            }

            // Read status filter chips
            item(key = "filters") {
                Box(modifier = Modifier.fillMaxWidth()) {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 0.dp)
                ) {
                    item {
                        FilterChip(
                            onClick = { viewModel.setReadStatusFilter(ReadStatusFilter.ALL) },
                            label = {
                                val count = uiState.unreadCount + uiState.readCount + uiState.archivedCount
                                Text(stringResource(R.string.filter_all) + " ($count)")
                            },
                            selected = uiState.readStatusFilter == ReadStatusFilter.ALL,
                            leadingIcon = if (uiState.readStatusFilter == ReadStatusFilter.ALL) {
                                { ChipLeadingCheck(Icons.Default.Check) }
                            } else null
                        )
                    }
                    item {
                        FilterChip(
                            onClick = { viewModel.setReadStatusFilter(ReadStatusFilter.UNREAD) },
                            label = { Text(stringResource(R.string.filter_unread) + " (${uiState.unreadCount})") },
                            selected = uiState.readStatusFilter == ReadStatusFilter.UNREAD,
                            leadingIcon = if (uiState.readStatusFilter == ReadStatusFilter.UNREAD) {
                                { ChipLeadingCheck(Icons.Default.MarkEmailUnread) }
                            } else null
                        )
                    }
                    item {
                        FilterChip(
                            onClick = { viewModel.setReadStatusFilter(ReadStatusFilter.READ) },
                            label = { Text(stringResource(R.string.filter_read) + " (${uiState.readCount})") },
                            selected = uiState.readStatusFilter == ReadStatusFilter.READ,
                            leadingIcon = if (uiState.readStatusFilter == ReadStatusFilter.READ) {
                                { ChipLeadingCheck(Icons.Default.Done) }
                            } else null
                        )
                    }
                    item {
                        FilterChip(
                            onClick = { viewModel.setReadStatusFilter(ReadStatusFilter.ARCHIVED) },
                            label = { Text(stringResource(R.string.filter_archived) + " (${uiState.archivedCount})") },
                            selected = uiState.readStatusFilter == ReadStatusFilter.ARCHIVED,
                            leadingIcon = if (uiState.readStatusFilter == ReadStatusFilter.ARCHIVED) {
                                { ChipLeadingCheck(Icons.Default.Archive) }
                            } else null
                        )
                    }
                }
                // Addendum #6: fade edge signals the row scrolls horizontally —
                // the 'Archived' chip never looks hard-clipped.
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterEnd)
                        .width(24.dp)
                        .height(48.dp)
                        .background(
                            brush = androidx.compose.ui.graphics.Brush.horizontalGradient(
                                colors = listOf(
                                    Color.Transparent,
                                    MaterialTheme.colorScheme.surfaceContainerLow
                                )
                            )
                        )
                )
                } // Box (fade wrapper)
            }

            // Tag filter section (audit #7: fades/expands in and out)
            item(key = "tags") {
                AnimatedVisibility(
                    visible = allTags.isNotEmpty(),
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
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
                                TextButton(onClick = { selectedTags = setOf() }) {
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
                            contentPadding = PaddingValues(horizontal = 0.dp)
                        ) {
                            items(allTags) { tag ->
                                FilterChip(
                                    onClick = {
                                        selectedTags = if (selectedTags.contains(tag)) selectedTags - tag else selectedTags + tag
                                    },
                                    label = { Text(tag) },
                                    selected = selectedTags.contains(tag),
                                    leadingIcon = if (selectedTags.contains(tag)) {
                                        { ChipLeadingCheck(Icons.Default.Check, stringResource(R.string.home_selected)) }
                                    } else null
                                )
                            }
                        }
                    }
                }
            }

            // Recent collections (audit #7: fades/expands in and out)
            item(key = "collections") {
                AnimatedVisibility(
                    visible = uiState.recentCollections.isNotEmpty() && localSearchQuery.isBlank(),
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Folder,
                                contentDescription = stringResource(R.string.nav_collections),
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
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
                            contentPadding = PaddingValues(horizontal = 0.dp),
                            modifier = Modifier.height(130.dp)
                        ) {
                            items(uiState.recentCollections) { collection ->
                                SmallCollectionCard(
                                    collection = collection,
                                    linkCount = uiState.collectionsWithCounts[collection.id] ?: 0,
                                    onCardClick = {
                                        navController?.navigate(Screen.CollectionDetail.createRoute(collection.id.toString()))
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // Bookmarks section header
            item(key = "bookmarks-header") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (localSearchQuery.isBlank()) Icons.Default.History else Icons.Default.Search,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (localSearchQuery.isBlank()) stringResource(R.string.home_recent_bookmarks)
                        else stringResource(R.string.home_search_results, uiState.links.size),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            // Loading state: skeleton cards shaped like BookmarkCard (audit #2)
            if (uiState.isLoading) {
                items(count = 3, key = { i -> "loading-$i" }) {
                    SkeletonBookmarkCard(modifier = Modifier.padding(bottom = 12.dp))
                }
            }

            // Empty state (audit #1): illustration + headline + CTA + share hint.
            // Search/tag-filter misses keep their simple text variants below.
            if (!uiState.isLoading && filteredLinks.isEmpty()) {
                item(key = "empty") {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (localSearchQuery.isBlank() && selectedTags.isEmpty()) {
                            // First run: the redesigned empty state
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(horizontal = 16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                EmptyStateIllustration()
                                Spacer(modifier = Modifier.height(24.dp))
                                Text(
                                    text = stringResource(R.string.home_no_bookmarks_yet),
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = stringResource(R.string.home_empty_share_hint),
                                    style = MaterialTheme.typography.bodyMedium,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(24.dp))
                                Button(
                                    onClick = { showAddLinkDialog = true },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(56.dp),
                                    shape = RoundedCornerShape(28.dp)
                                ) {
                                    Text(
                                        text = stringResource(R.string.home_empty_cta),
                                        style = MaterialTheme.typography.titleMedium
                                    )
                                }
                            }
                        } else {
                            // Filtered/search empty state stays plain text
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = when {
                                        selectedTags.isNotEmpty() -> stringResource(R.string.home_no_bookmarks_tags)
                                        else -> stringResource(R.string.home_no_results_found)
                                    },
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            // Bookmarks list / grid
            if (!uiState.isLoading && filteredLinks.isNotEmpty()) {
                if (isGridView) {
                    items(
                        items = chunkedLinks,
                        key = { chunk -> chunk.joinToString(",") { it.id.toString() } }
                    ) { rowLinks ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .graphicsLayer { alpha = listCrossfade.value }
                                .animateItem(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            rowLinks.forEach { link ->
                                BookmarkCard(
                                    link = link,
                                    collectionName = collectionMap[link.collectionId]?.name,
                                    collectionColor = collectionMap[link.collectionId]?.color,
                                    isMultiSelectMode = uiState.isMultiSelectMode,
                                    isSelected = uiState.selectedLinks.contains(link.id),
                                    isGrid = true,
                                    onCardClick = {
                                        if (uiState.isMultiSelectMode) {
                                            viewModel.toggleLinkSelection(link.id)
                                        } else {
                                            com.appcodecraft.linkzary.util.UrlOpener.openOrToast(context, link.url)
                                        }
                                    },
                                    onMoreClick = { selectedLink = link },
                                    onLongPress = { viewModel.startMultiSelectWithToggle(link.id) },
                                    onSelectClick = { viewModel.toggleLinkSelection(link.id) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            if (rowLinks.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                } else {
                    items(
                        items = filteredLinks,
                        key = { it.id }
                    ) { link ->
                        BookmarkCard(
                            link = link,
                            collectionName = collectionMap[link.collectionId]?.name,
                            collectionColor = collectionMap[link.collectionId]?.color,
                            isMultiSelectMode = uiState.isMultiSelectMode,
                            isSelected = uiState.selectedLinks.contains(link.id),
                            modifier = Modifier
                                .graphicsLayer { alpha = listCrossfade.value }
                                .animateItem(),
                            onCardClick = {
                                if (uiState.isMultiSelectMode) {
                                    viewModel.toggleLinkSelection(link.id)
                                } else {
                                    com.appcodecraft.linkzary.util.UrlOpener.openOrToast(context, link.url)
                                }
                            },
                            onMoreClick = { selectedLink = link },
                            onLongPress = { viewModel.startMultiSelectWithToggle(link.id) },
                            onSelectClick = { viewModel.toggleLinkSelection(link.id) }
                        )
                    }
                }
            }
        }
    }
    // Column (app bar + list) closed; outer Box stays open for overlays

        // Multi-select action bar (overlay)
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
                        IconButton(onClick = { showCollectionPicker = true }, modifier = Modifier.size(40.dp)) {
                            Icon(Icons.AutoMirrored.Filled.DriveFileMove, contentDescription = stringResource(R.string.multi_select_move_to_collection), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                        IconButton(onClick = { viewModel.batchDeleteLinks() }, modifier = Modifier.size(40.dp)) {
                            Icon(Icons.Default.Delete, contentDescription = stringResource(R.string.multi_select_delete_selected), tint = MaterialTheme.colorScheme.error)
                        }
                        IconButton(onClick = { viewModel.exitMultiSelectMode() }, modifier = Modifier.size(40.dp)) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.multi_select_cancel_selection), tint = MaterialTheme.colorScheme.onPrimaryContainer)
                        }
                    }
                }
            }
        }

    // First-run coach-mark (audit #8): one scrim + card, dismissed once and never shown again
    if (showCoachMark) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.5f))
                .pointerInput(Unit) { detectTapGestures { viewModel.markCoachMarkSeen() } },
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                shadowElevation = 8.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = stringResource(R.string.coach_mark_title),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(R.string.coach_mark_body),
                        style = MaterialTheme.typography.bodyMedium,
                        textAlign = TextAlign.Center,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                    Button(
                        onClick = { viewModel.markCoachMarkSeen() },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Text(stringResource(R.string.coach_mark_got_it))
                    }
                }
            }
        }
    }

    // Floating Action Button (overlay, bottom-right)
    // Spec v2 §2.3: the one LinkzaryFab — primaryContainer cookie scallop at
    // rest, labeled pill when scrolled.
    if (!uiState.isMultiSelectMode && !showCoachMark) {
        com.appcodecraft.linkzary.ui.component.LinkzaryFab(
            label = stringResource(R.string.home_add_link),
            onClick = { showAddLinkDialog = true },
            isScrolled = isScrolled,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        )
    }

    // Snackbar host
    SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier.align(Alignment.BottomCenter)
    )
    } // close outer Box

    // Error handling
    uiState.error?.let { error ->
        LaunchedEffect(error) {
            snackbarHostState.showSnackbar(error)
            viewModel.clearError()
        }
    }

    LaunchedEffect(uiState.batchOperationMessage) {
        uiState.batchOperationMessage?.let { message ->
            snackbarHostState.showSnackbar(message)
            viewModel.clearBatchOperationMessage()
        }
    }

    // Collection Picker Bottom Sheet
    if (showCollectionPicker) {
        ModalBottomSheet(onDismissRequest = { showCollectionPicker = false }) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.batch_move_bookmarks_to, uiState.selectedLinks.size),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(16.dp))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
                    Surface(
                        onClick = { viewModel.batchMoveToCollection(null); showCollectionPicker = false },
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Folder, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                            Spacer(modifier = Modifier.width(16.dp))
                            Text(stringResource(R.string.batch_uncategorized), style = MaterialTheme.typography.bodyLarge)
                        }
                    }
                    uiState.allCollections.forEach { collection ->
                        Surface(
                            onClick = { viewModel.batchMoveToCollection(collection.id); showCollectionPicker = false },
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                                val color = remember(collection.color) {
                                    try { Color(android.graphics.Color.parseColor(collection.color)) } catch (e: Exception) { Color.Gray }
                                }
                                Box(modifier = Modifier.size(24.dp).background(color = color, shape = CircleShape))
                                Spacer(modifier = Modifier.width(16.dp))
                                Text(collection.name, style = MaterialTheme.typography.bodyLarge)
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
                TextButton(onClick = { showCollectionPicker = false }, modifier = Modifier.fillMaxWidth()) {
                    Text(stringResource(R.string.common_cancel))
                }
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // Create Collection Dialog
    if (showCreateCollectionDialog) {
        CreateCollectionDialog(
            onDismiss = { showCreateCollectionDialog = false },
            onCreate = { name, color, icon ->
                viewModel.createCollection(name, color, icon) { showCreateCollectionDialog = false }
            }
        )
    }

    // Add Link Dialog
    if (showAddLinkDialog) {
        AddLinkDialog(
            onDismiss = { showAddLinkDialog = false },
            onSave = { url -> viewModel.saveLink(url); showAddLinkDialog = false }
        )
    }

    // Link Options Bottom Sheet
    selectedLink?.let { link ->
        LinkOptionsBottomSheet(
            link = link,
            onDismiss = { selectedLink = null },
            onEdit = { updatedLink -> viewModel.updateLink(updatedLink); selectedLink = null },
            onDelete = { viewModel.deleteLink(link); selectedLink = null },
            onTogglePin = { viewModel.togglePinStatus(link); selectedLink = null },
            onMoveToCollection = { collectionId -> viewModel.moveToCollection(link.id, collectionId); selectedLink = null },
            onReaderMode = { navController?.navigate(Screen.Reader.createRoute(link.id)); selectedLink = null },
            onToggleOffline = { linkToToggle -> viewModel.toggleOfflineStatus(linkToToggle); selectedLink = null },
            onToggleReadStatus = { viewModel.toggleReadStatus(link); selectedLink = null },
            viewModel = viewModel
        )
    }
}
