package com.appcodecraft.linkzary.ui.screen.settings

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.ChevronRight
import androidx.compose.material.icons.rounded.ColorLens
import androidx.compose.material.icons.rounded.CloudDownload
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.Construction
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.Download
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Feedback
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material.icons.rounded.InsertChart
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.MergeType
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material.icons.rounded.RssFeed
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Share
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material.icons.rounded.Shuffle
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Upload
import androidx.compose.material.icons.rounded.VolunteerActivism
import androidx.compose.material.icons.rounded.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.appcodecraft.linkzary.BuildConfig
import com.appcodecraft.linkzary.R
import com.appcodecraft.linkzary.ui.component.LinkzaryHaptic
import com.appcodecraft.linkzary.ui.component.rememberLinkzaryHaptics
import com.appcodecraft.linkzary.data.preferences.ThemeMode
import com.appcodecraft.linkzary.data.preferences.UserPreferencesManager
import com.appcodecraft.linkzary.ui.theme.LinkzaryTheme
import com.appcodecraft.linkzary.utils.LocaleHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    importExportViewModel: ImportExportViewModel = hiltViewModel(),
    donationViewModel: DonationViewModel = hiltViewModel(),
    userPreferencesManager: UserPreferencesManager,
    onNavigateToDonation: () -> Unit = {},
    onNavigateToTags: () -> Unit = {},
    onNavigateToStats: () -> Unit = {},
    onNavigateToRss: () -> Unit = {}
) {
    val context = LocalContext.current
    
    var showAboutDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    // Removed showDonationDialog as we now navigate to DonationScreen
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    
    val importExportUiState by importExportViewModel.uiState.collectAsState()
    val importProgress by importExportViewModel.importProgress.collectAsState()
    val importResult by importExportViewModel.importResult.collectAsState()
    val importPreview by importExportViewModel.importPreview.collectAsState()
    
    val metadataRefreshProgress by viewModel.metadataRefreshProgress.collectAsState()
    val isMetadataDialogHidden by viewModel.isMetadataDialogHidden.collectAsState()

    val duplicateCount by viewModel.duplicateCount.collectAsState()
    val mergeResult by viewModel.mergeResult.collectAsState()
    val driveStatus by viewModel.driveStatus.collectAsState()

    var showMergeDialog by remember { mutableStateOf(false) }

    // Which Drive action to continue after the account picker or the
    // permission dialog closes: the retry closure captures the tapped action
    // (backup or restore). Without this the user granted access but nothing
    // continued — backup/restore looked dead.
    var retryPendingDriveAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    // Drive consent resolution: AuthorizationClient.authorize() hands us a
    // PendingIntent's IntentSender when access isn't granted yet; launching it
    // shows the consent dialog. Granted (or denied) — retry the pending action
    // so the user sees a real success/failure instead of silence.
    val driveAuthLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartIntentSenderForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            retryPendingDriveAction?.invoke()
        } else {
            viewModel.reportDriveSignInFailure()
        }
        retryPendingDriveAction = null
    }

    // Google account picker for Drive backup
    val driveSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        try {
            com.google.android.gms.auth.api.signin.GoogleSignIn
                .getSignedInAccountFromIntent(result.data)
                .getResult(java.lang.Exception::class.java)
            // Account stored — continue the action the user originally tapped.
            retryPendingDriveAction?.invoke()
            retryPendingDriveAction = null
        } catch (e: Exception) {
            viewModel.reportDriveSignInFailure()
        }
    }
    
    val hasDonated by donationViewModel.hasDonated.collectAsState()
    
    val currentThemeMode by userPreferencesManager.themeMode.collectAsState()
    val currentLanguage by userPreferencesManager.currentLanguage.collectAsState()
    
    val jsonExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { 
            importExportViewModel.exportToJson(context, it)
        }
    }
    
    val csvExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/csv")
    ) { uri ->
        uri?.let { 
            importExportViewModel.exportToCsv(context, it)
        }
    }

    val htmlExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("text/html")
    ) { uri ->
        uri?.let {
            importExportViewModel.exportToHtml(context, it)
        }
    }
    
    var selectedImportUri by remember { mutableStateOf<Uri?>(null) }
    
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { 
            selectedImportUri = it
            importExportViewModel.previewImport(context, it)
        }
    }
    
    // Standard header, enterAlways collapse (spec v2 §2.1/§5 Settings row)
    val settingsScrollBehavior = androidx.compose.material3.TopAppBarDefaults.enterAlwaysScrollBehavior()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
            .nestedScroll(settingsScrollBehavior.nestedScrollConnection)
            .padding(horizontal = 16.dp)
    ) {
        com.appcodecraft.linkzary.ui.component.LinkzaryTopBar(
            title = stringResource(R.string.settings_title),
            leadingGlyph = Icons.Rounded.Settings,
            scrollBehavior = settingsScrollBehavior,
            containerColor = MaterialTheme.colorScheme.surface
        )
        androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(40.dp))

        LazyColumn(
            // v5 breathable rhythm: 16dp row gaps inside and between sections.
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // ── Welcome block (spec v5 §1): quiet Donation card on TOP, loud
            // Drive hero below, 12dp shared gap — they read as one composed
            // header. 48dp gap after separates them from the list.
            item {
                Column {
                    if (hasDonated) {
                        com.appcodecraft.linkzary.ui.component.DonationCardThanks(
                            title = stringResource(R.string.donation_v3_thanks_title),
                            subtitle = stringResource(R.string.donation_v3_thanks_subtitle)
                        )
                    } else {
                        com.appcodecraft.linkzary.ui.component.DonationCardCompact(
                            title = stringResource(R.string.donation_v3_title),
                            subtitle = stringResource(R.string.settings_donation_one_time_copy),
                            donateLabel = stringResource(R.string.settings_donate),
                            onDonateClick = onNavigateToDonation
                        )
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                    // v5.2 spec: chromatic Drive hero (DriveBackupCard.kt) replaces
                    // the washed-out v5 hero. Launchers/retry logic untouched.
                    com.appcodecraft.linkzary.ui.component.DriveBackupCard(
                        title = stringResource(R.string.settings_drive_hero_title),
                        statusText = driveHeroStatusText(driveStatus),
                        state = driveCardState(driveStatus),
                        backUpLabel = stringResource(R.string.settings_drive_back_up_now),
                        restoreLabel = stringResource(R.string.settings_drive_restore_short),
                        onBackUpNow = {
                        if (viewModel.isDriveConnected(context)) {
                            retryPendingDriveAction = {
                                viewModel.backupToDrive(context) { sender -> driveAuthLauncher.launch(androidx.activity.result.IntentSenderRequest.Builder(sender).build()) }
                            }
                            viewModel.backupToDrive(context) { sender -> driveAuthLauncher.launch(androidx.activity.result.IntentSenderRequest.Builder(sender).build()) }
                        } else {
                            retryPendingDriveAction = {
                                viewModel.backupToDrive(context) { sender -> driveAuthLauncher.launch(androidx.activity.result.IntentSenderRequest.Builder(sender).build()) }
                            }
                            driveSignInLauncher.launch(
                                com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(
                                    context,
                                    com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(
                                        com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
                                    )
                                        .requestScopes(
                                            com.google.android.gms.common.api.Scope(
                                                com.google.api.services.drive.DriveScopes.DRIVE_APPDATA
                                            )
                                        )
                                        .requestEmail()
                                        .build()
                                ).signInIntent
                            )
                        }
                    },
                    onRestore = {
                        if (viewModel.isDriveConnected(context)) {
                            retryPendingDriveAction = {
                                viewModel.restoreFromDrive(context) { sender -> driveAuthLauncher.launch(androidx.activity.result.IntentSenderRequest.Builder(sender).build()) }
                            }
                            viewModel.restoreFromDrive(context) { sender -> driveAuthLauncher.launch(androidx.activity.result.IntentSenderRequest.Builder(sender).build()) }
                        } else {
                            retryPendingDriveAction = {
                                viewModel.restoreFromDrive(context) { sender -> driveAuthLauncher.launch(androidx.activity.result.IntentSenderRequest.Builder(sender).build()) }
                            }
                            driveSignInLauncher.launch(
                                com.google.android.gms.auth.api.signin.GoogleSignIn.getClient(
                                    context,
                                    com.google.android.gms.auth.api.signin.GoogleSignInOptions.Builder(
                                        com.google.android.gms.auth.api.signin.GoogleSignInOptions.DEFAULT_SIGN_IN
                                    )
                                        .requestScopes(
                                            com.google.android.gms.common.api.Scope(
                                                com.google.api.services.drive.DriveScopes.DRIVE_APPDATA
                                            )
                                        )
                                        .requestEmail()
                                        .build()
                                ).signInIntent
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                )
                }
            }

            // ── 48dp gap: welcome block → first section (spec v5 §1)
            item {
                Spacer(modifier = Modifier.height(32.dp))
            }

            // ── 2. Appearance (spec v4 §3 #2)
            item {
                SettingsSectionHeader(stringResource(R.string.settings_section_appearance))
            }

            item {
                SettingsItem(
                    icon = Icons.Rounded.Palette,
                    title = stringResource(R.string.settings_theme),
                    subtitle = when (currentThemeMode) {
                        ThemeMode.LIGHT -> stringResource(R.string.settings_theme_light)
                        ThemeMode.DARK -> stringResource(R.string.settings_theme_dark)
                        ThemeMode.SYSTEM -> stringResource(R.string.settings_theme_system)
                    },
                    onClick = {
                        showThemeDialog = true
                    }
                )
            }

            // "Use Linkzary colors" (audit #4): force brand indigo over dynamic color
            item {
                val useBrandColors by userPreferencesManager.useBrandColors.collectAsState()
                SettingsToggleItem(
                    icon = Icons.Rounded.ColorLens,
                    title = stringResource(R.string.settings_use_brand_colors),
                    subtitle = stringResource(R.string.settings_use_brand_colors_desc),
                    checked = useBrandColors,
                    onCheckedChange = { userPreferencesManager.setUseBrandColors(it) }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Rounded.Language,
                    title = stringResource(R.string.settings_language),
                    subtitle = LocaleHelper.getLanguageDisplayName(currentLanguage),
                    onClick = {
                        showLanguageDialog = true
                    }
                )
            }

            // Haptic feedback (v4.1, v5 §1): subtle M3 haptics on key touches.
            // Spec v5 slot: Appearance, after Language. Self-contained item so
            // Pixel's v5+ layouts can move it freely.
            item {
                val hapticsEnabled by userPreferencesManager.hapticsEnabled.collectAsState()
                SettingsToggleItem(
                    icon = Icons.Rounded.Vibration,
                    title = stringResource(R.string.settings_haptic_feedback),
                    subtitle = stringResource(R.string.settings_haptic_feedback_desc),
                    checked = hapticsEnabled,
                    onCheckedChange = { userPreferencesManager.setHapticsEnabled(it) }
                )
            }

            // ── 3. Library (spec v4 §3 #3)
            item {
                Spacer(modifier = Modifier.height(28.dp))
                SettingsSectionHeader(stringResource(R.string.settings_section_library))
            }

            item {
                SettingsItem(
                    icon = Icons.Rounded.Star,
                    title = stringResource(R.string.tags),
                    subtitle = "Manage and rename your tags",
                    onClick = onNavigateToTags
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Rounded.RssFeed,
                    title = stringResource(R.string.settings_rss_feeds),
                    subtitle = stringResource(R.string.settings_rss_feeds_description),
                    onClick = onNavigateToRss
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Rounded.MergeType,
                    title = stringResource(R.string.settings_merge_duplicates),
                    subtitle = stringResource(
                        R.string.settings_merge_duplicates_description,
                        duplicateCount ?: 0
                    ),
                    onClick = {
                        viewModel.refreshDuplicateCount()
                        showMergeDialog = true
                    }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Rounded.Refresh,
                    title = "Force Metadata Refresh", // Hardcoded string as requested/placeholder
                    subtitle = "Update images and titles for all saved links",
                    onClick = {
                        viewModel.refreshMetadata()
                    }
                )
            }

            // ── 4. Data (spec v4 §3 #4) — Clear data last, in error tones
            item {
                Spacer(modifier = Modifier.height(28.dp))
                SettingsSectionHeader(stringResource(R.string.settings_data))
            }

            item {
                SettingsItem(
                    icon = Icons.Rounded.Upload,
                    title = stringResource(R.string.settings_export_data),
                    subtitle = stringResource(R.string.settings_export_description),
                    onClick = {
                        showExportDialog = true
                    }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Rounded.Download,
                    title = stringResource(R.string.settings_import_data),
                    subtitle = stringResource(R.string.settings_import_description),
                    onClick = {
                        showImportDialog = true
                    }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Rounded.Delete,
                    title = stringResource(R.string.settings_clear_data),
                    subtitle = stringResource(R.string.settings_clear_data_description),
                    onClick = {
                        showClearDataDialog = true
                    },
                    isDestructive = true
                )
            }

            // ── 5. Support section REMOVED (spec v5 §2): donation moved to
            // the top welcome block; Rate/Share moved into About.

            // ── 6. About (spec v4 §3 #6) — keeps Rate/Share per build order
            item {
                Spacer(modifier = Modifier.height(28.dp))
                SettingsSectionHeader(stringResource(R.string.settings_about))
            }

            item {
                SettingsItem(
                    icon = Icons.Rounded.Star,
                    title = stringResource(R.string.settings_rate_app),
                    subtitle = stringResource(R.string.settings_rate_description),
                    onClick = {
                        openLinkzaryInPlayStore(context)
                    }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Rounded.Share,
                    title = stringResource(R.string.settings_share_app),
                    subtitle = stringResource(R.string.settings_share_app_description),
                    onClick = {
                        shareApp(context)
                    }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Rounded.Info,
                    title = stringResource(R.string.settings_about_app),
                    subtitle = stringResource(R.string.settings_version, BuildConfig.VERSION_NAME),
                    onClick = {
                        showAboutDialog = true
                    }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Rounded.Security,
                    title = stringResource(R.string.settings_privacy_policy),
                    subtitle = stringResource(R.string.settings_privacy_description),
                    onClick = {
                        openPrivacyPolicy(context)
                    }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Rounded.Feedback,
                    title = stringResource(R.string.settings_send_feedback),
                    subtitle = stringResource(R.string.settings_feedback_description),
                    onClick = {
                        sendFeedbackEmail(context)
                    }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Rounded.Person,
                    title = stringResource(R.string.settings_developer_profile),
                    subtitle = stringResource(R.string.settings_developer_description),
                    onClick = {
                        openDeveloperProfile(context)
                    }
                )
            }

            item {
                SettingsItem(
                    icon = Icons.Rounded.InsertChart,
                    title = stringResource(R.string.settings_stats),
                    subtitle = stringResource(R.string.settings_stats_description),
                    onClick = onNavigateToStats
                )
            }

            // Add bottom padding
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
    
    // Metadata Refresh Progress (runs in background, dismissible)
    if (metadataRefreshProgress != null && !isMetadataDialogHidden) {
        com.appcodecraft.linkzary.ui.component.LinkzaryDialog(
            onDismissRequest = { viewModel.dismissMetadataDialog() },
            icon = Icons.Rounded.Refresh,
            title = "Refreshing Metadata",
            confirmLabel = "Hide",
            onConfirm = { viewModel.dismissMetadataDialog() }
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(16.dp))
                Text("${metadataRefreshProgress!!.first} / ${metadataRefreshProgress!!.second} links processed")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Refresh continues in background",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    // Language Dialog
    if (showLanguageDialog) {
        LanguageSelectionDialog(
            onDismiss = { showLanguageDialog = false },
            onLanguageSelected = { languageCode ->
                userPreferencesManager.setCurrentLanguage(languageCode)
                LocaleHelper.setLocale(context, languageCode)
                showLanguageDialog = false
                
                // Restart activity to apply language changes
                (context as? Activity)?.let { activity ->
                    LocaleHelper.restartActivity(activity)
                }
            }
        )
    }
    
    // Theme Dialog
    if (showThemeDialog) {
        ThemeSelectionDialog(
            currentTheme = currentThemeMode,
            onDismiss = { showThemeDialog = false },
            onThemeSelected = { theme ->
                userPreferencesManager.setThemeMode(theme)
                showThemeDialog = false
            }
        )
    }
    
    // Clear Data Dialog
    if (showClearDataDialog) {
        ClearDataConfirmationDialog(
            onDismiss = { showClearDataDialog = false },
            onConfirm = {
                viewModel.clearAllData()
                showClearDataDialog = false
            }
        )
    }

    // Merge Duplicates Dialog
    if (showMergeDialog) {
        com.appcodecraft.linkzary.ui.component.LinkzaryDialog(
            onDismissRequest = { showMergeDialog = false },
            icon = Icons.Rounded.MergeType,
            title = stringResource(R.string.merge_duplicates_title),
            confirmLabel = stringResource(R.string.merge_duplicates_confirm),
            onConfirm = {
                viewModel.mergeDuplicates()
                showMergeDialog = false
            },
            confirmEnabled = (duplicateCount ?: 0) > 0,
            dismissLabel = stringResource(R.string.common_cancel),
            onDismiss = { showMergeDialog = false }
        ) {
            Text(stringResource(R.string.merge_duplicates_message, duplicateCount ?: 0))
        }
    }
    
    // About Dialog
    if (showAboutDialog) {
        AboutDialog(
            onDismiss = { showAboutDialog = false }
        )
    }
    
    // Export Dialog
    if (showExportDialog) {
        ExportDataDialog(
            onDismiss = { showExportDialog = false },
            onExportJson = {
                jsonExportLauncher.launch("linkzary_backup_${System.currentTimeMillis()}.json")
                showExportDialog = false
            },
            onExportCsv = {
                csvExportLauncher.launch("linkzary_backup_${System.currentTimeMillis()}.csv")
                showExportDialog = false
            },
            onExportHtml = {
                htmlExportLauncher.launch("linkzary_bookmarks_${System.currentTimeMillis()}.html")
                showExportDialog = false
            }
        )
    }
    
    // Import Dialog
    if (showImportDialog) {
        ImportDataDialog(
            onDismiss = { showImportDialog = false },
            onSelectFile = {
                importLauncher.launch(arrayOf("application/json", "text/csv", "text/plain", "text/html", "*/*"))
                showImportDialog = false
            }
        )
    }
    
    // Import Preview Dialog
    importPreview?.let { preview ->
        ImportPreviewDialog(
            preview = preview,
            onDismiss = {
                importExportViewModel.clearImportState()
                selectedImportUri = null
            },
            onConfirmImport = { mode ->
                selectedImportUri?.let { uri ->
                    importExportViewModel.importData(context, uri, mode)
                }
            }
        )
    }
    
    // Import Progress Dialog
    importProgress?.let { progress ->
        ImportProgressDialog(
            progress = progress,
            onDismiss = {
                importExportViewModel.clearImportState()
            }
        )
    }
    
    // Import Result Dialog
    importResult?.let { result ->
        ImportResultDialog(
            result = result,
            onDismiss = {
                importExportViewModel.clearImportState()
            }
        )
    }
    
    // Handle export success/error
    LaunchedEffect(importExportUiState.exportSuccess, importExportUiState.exportError) {
        if (importExportUiState.exportSuccess) {
            // Show success message
            importExportViewModel.clearExportState()
        }
        if (importExportUiState.exportError != null) {
            // Show error message
            importExportViewModel.clearExportState()
        }
    }
    
    // Handle preview error
    importExportUiState.previewError?.let { error ->
        LaunchedEffect(error) {
            // Show error message
            importExportViewModel.clearPreviewError()
        }
    }
    
    // Donation navigation is now handled by onNavigateToDonation callback
}

// Donation Header Component
@Composable
fun DonationHeader(
    onDonateClick: () -> Unit,
    hasDonated: Boolean = false
) {
    // Spec v3 §2.3: delegated to the shared DonationCard — tertiaryContainer /
    // onTertiaryContainer tokens only, zero hard-coded hex, quiet by design.
    if (hasDonated) {
        com.appcodecraft.linkzary.ui.component.DonationCardThanks(
            title = stringResource(R.string.donation_v3_thanks_title),
            subtitle = stringResource(R.string.donation_v3_thanks_subtitle)
        )
    } else {
        com.appcodecraft.linkzary.ui.component.DonationCard(
            title = stringResource(R.string.donation_v3_title),
            subtitle = stringResource(R.string.donation_v3_subtitle),
            buttonText = stringResource(R.string.donation_v3_button),
            footerText = stringResource(R.string.donation_v3_footer),
            onDonateClick = onDonateClick
        )
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    // Spec v5 §1: light uppercase overline — 12sp/600, onSurfaceVariant at
    // 55%, letterspaced 0.08em. The loud primary bold headers are gone.
    Text(
        text = title.uppercase(),
        style = MaterialTheme.typography.labelSmall.copy(
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            letterSpacing = 0.08.em
        ),
        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.55f),
        modifier = Modifier.padding(bottom = 0.dp)
    )
}

/** One-line Drive status for the hero card (reads the existing [DriveBackupStatus]). */
private fun driveHeroStatusText(status: DriveBackupStatus?): String = when (status) {
    is DriveBackupStatus.InProgress -> "Working…"
    is DriveBackupStatus.Success -> "Backup complete"
    is DriveBackupStatus.Restored -> "Restored ${status.importedLinks} links"
    is DriveBackupStatus.Error -> status.message
    is DriveBackupStatus.NeedsSignIn -> "Sign-in needed"
    is DriveBackupStatus.NeedsPermission -> "Tap again to grant Drive access"
    null -> "Save all links and collections to your own Drive"
}

/** Dot state for the v5.2 DriveBackupCard (success/error/working dot colors). */
private fun driveCardState(status: DriveBackupStatus?): com.appcodecraft.linkzary.ui.component.DriveCardState = when (status) {
    is DriveBackupStatus.InProgress -> com.appcodecraft.linkzary.ui.component.DriveCardState.WORKING
    is DriveBackupStatus.Success, is DriveBackupStatus.Restored -> com.appcodecraft.linkzary.ui.component.DriveCardState.SUCCESS
    is DriveBackupStatus.Error, is DriveBackupStatus.NeedsSignIn, is DriveBackupStatus.NeedsPermission ->
        com.appcodecraft.linkzary.ui.component.DriveCardState.ERROR
    null -> com.appcodecraft.linkzary.ui.component.DriveCardState.IDLE
}

/** Share-app intent (spec v4 §3 #5, Support section). */
fun shareApp(context: Context) {
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(
            Intent.EXTRA_TEXT,
            "Linkzary — a calm bookmark manager: https://play.google.com/store/apps/details?id=com.appcodecraft.linkzary"
        )
    }
    context.startActivity(Intent.createChooser(intent, context.getString(R.string.settings_share_app_choice)))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Spec v5 §1: 24dp glyph in a 36dp surfaceContainerHigh circle.
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (isDestructive)
                        MaterialTheme.colorScheme.error
                    else
                        MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
            
            Spacer(modifier = Modifier.width(16.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = if (isDestructive) 
                        MaterialTheme.colorScheme.error 
                    else 
                        MaterialTheme.colorScheme.onSurface
                )
                
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.Rounded.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/** Settings row with a Switch, mirroring [SettingsItem] visuals. */
@Composable
fun SettingsToggleItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val toggleHaptics = rememberLinkzaryHaptics()
    Card(
        onClick = {
            // v4.1: subtle CLOCK_TICK on toggle flip (gated).
            toggleHaptics(LinkzaryHaptic.TICK)
            onCheckedChange(!checked)
        },
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 80.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Spec v5 §1: 24dp glyph in a 36dp surfaceContainerHigh circle.
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .background(
                        color = MaterialTheme.colorScheme.surfaceContainerHigh,
                        shape = CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Switch(
                checked = checked,
                onCheckedChange = { newValue ->
                    toggleHaptics(LinkzaryHaptic.TICK)
                    onCheckedChange(newValue)
                }
            )
        }
    }
}

// Language Selection Dialog
@Composable
fun LanguageSelectionDialog(
    onDismiss: () -> Unit,
    onLanguageSelected: (String) -> Unit
) {
    val languages = listOf(
        stringResource(R.string.language_english) to "en",
        stringResource(R.string.language_french) to "fr",
        stringResource(R.string.language_portuguese) to "pt-br",
        stringResource(R.string.language_spanish) to "es",
        stringResource(R.string.language_german) to "de",
        stringResource(R.string.language_italian) to "it",
        stringResource(R.string.language_arabic) to "ar"
    )
    
    com.appcodecraft.linkzary.ui.component.LinkzaryDialog(
        onDismissRequest = onDismiss,
        icon = Icons.Rounded.Language,
        title = stringResource(R.string.settings_select_language),
        confirmLabel = null,
        onConfirm = {},
        dismissLabel = stringResource(R.string.common_cancel),
        onDismiss = onDismiss
    ) {
        // v5.1.1 crash fix: LazyColumn inside the dialog's verticalScroll
        // column crashed on open (infinite height constraint). The scaffold
        // scrolls, so this is a plain Column now.
        Column {
            languages.forEach { (name, code) ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                        .clickable {
                            onLanguageSelected(code)
                        },
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                    )
                ) {
                    Text(
                        text = name,
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}

// Clear Data Confirmation Dialog
@Composable
fun ClearDataConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    com.appcodecraft.linkzary.ui.component.LinkzaryDialog(
        onDismissRequest = onDismiss,
        icon = Icons.Rounded.Warning,
        title = stringResource(R.string.settings_clear_data_title),
        confirmLabel = stringResource(R.string.settings_clear_data),
        onConfirm = onConfirm,
        dismissLabel = stringResource(R.string.common_cancel),
        onDismiss = onDismiss,
        destructive = true
    ) {
        Column {
            Text(
                text = stringResource(R.string.settings_clear_data_warning),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(stringResource(R.string.settings_clear_data_item1))
            Text(stringResource(R.string.settings_clear_data_item2))
            Text(stringResource(R.string.settings_clear_data_item3))
            Text(stringResource(R.string.settings_clear_data_item4))
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = stringResource(R.string.settings_clear_data_irreversible),
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}

// Enhanced About Dialog
@Composable
fun AboutDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    
    com.appcodecraft.linkzary.ui.component.LinkzaryDialog(
        onDismissRequest = onDismiss,
        icon = Icons.Rounded.Bookmark,
        title = stringResource(R.string.settings_about_app),
        confirmLabel = stringResource(R.string.common_close),
        onConfirm = onDismiss
    ) {
        Column {
                Text(
                    text = stringResource(R.string.settings_about_description),
                    style = MaterialTheme.typography.bodyMedium
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // Key Features Section
                Text(
                    text = stringResource(R.string.about_key_features),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                FeatureRow(Icons.Rounded.Security, stringResource(R.string.about_feature_secure))
                FeatureRow(Icons.Rounded.Favorite, stringResource(R.string.about_feature_free))
                FeatureRow(Icons.Rounded.Palette, stringResource(R.string.about_feature_themes))
                FeatureRow(Icons.Rounded.Language, stringResource(R.string.about_feature_languages))
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // App Info
                Text(
                    text = stringResource(R.string.about_app_information),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                InfoRow(stringResource(R.string.settings_version_label), BuildConfig.VERSION_NAME)
                InfoRow(stringResource(R.string.settings_developer_label), "AppCodeCraft")
                InfoRow(stringResource(R.string.about_built_with), "Jetpack Compose & Kotlin")
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // More Apps Section
                Text(
                    text = stringResource(R.string.settings_more_apps),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // App Cards
                AppCard(
                    name = stringResource(R.string.app_curioshuffle),
                    description = stringResource(R.string.app_curioshuffle_description),
                    icon = Icons.Rounded.Shuffle,
                    onClick = {
                        openAppInPlayStore(context, "com.appcodecraft.curioshuffle")
                    }
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                AppCard(
                    name = stringResource(R.string.app_curiomate),
                    description = stringResource(R.string.app_curiomate_description),
                    icon = Icons.Rounded.Construction,
                    onClick = {
                        openAppInPlayStore(context, "com.appcodecraft.curiomate")
                    }
                )
            }
        }
    }

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun FeatureRow(icon: ImageVector, feature: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = feature,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
fun AppCard(
    name: String,
    description: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        shape = MaterialTheme.shapes.large
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Icon(
                imageVector = Icons.Rounded.OpenInNew,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}



// Helper Functions
fun openAppInPlayStore(context: Context, packageName: String) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=$packageName"))
        context.startActivity(intent)
    } catch (e: Exception) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=$packageName"))
        context.startActivity(intent)
    }
}

fun openLinkzaryInPlayStore(context: Context) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.appcodecraft.linkzary"))
        context.startActivity(intent)
    } catch (e: Exception) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.appcodecraft.linkzary"))
        context.startActivity(intent)
    }
}

fun sendFeedbackEmail(context: Context) {
    val appVersion = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    } catch (e: Exception) {
        "Unknown"
    }
    
    val deviceInfo = "\n\n--- Device Info ---\n" +
            "App Version: $appVersion\n" +
            "Android Version: ${android.os.Build.VERSION.RELEASE}\n" +
            "Device Model: ${android.os.Build.MODEL}\n" +
            "Device Manufacturer: ${android.os.Build.MANUFACTURER}"
    
    val intent = Intent(Intent.ACTION_SENDTO).apply {
        data = Uri.parse("mailto:")
        putExtra(Intent.EXTRA_EMAIL, arrayOf("appcodecraft@gmail.com"))
        putExtra(Intent.EXTRA_SUBJECT, "Linkzary Feedback")
        putExtra(Intent.EXTRA_TEXT, "Hi AppCodeCraft team,\n\nI would like to share my feedback about Linkzary:\n\n[Please write your feedback here]$deviceInfo")
    }
    
    try {
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback to generic intent if no email app is available
        val fallbackIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_EMAIL, arrayOf("appcodecraft@gmail.com"))
            putExtra(Intent.EXTRA_SUBJECT, "Linkzary Feedback")
            putExtra(Intent.EXTRA_TEXT, "Hi AppCodeCraft team,\n\nI would like to share my feedback about Linkzary:\n\n[Please write your feedback here]$deviceInfo")
        }
        context.startActivity(Intent.createChooser(fallbackIntent, "Send Feedback"))
    }
}

fun openPrivacyPolicy(context: Context) {
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.appcodecraft.com/privacy"))
    context.startActivity(intent)
}

fun openDeveloperProfile(context: Context) {
    try {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://dev?id=AppCodeCraft"))
        context.startActivity(intent)
    } catch (e: Exception) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/dev?id=6994476958831569782"))
        context.startActivity(intent)
    }
}

@Preview
@Composable
fun SettingsScreenPreview() {
    val context = LocalContext.current
    LinkzaryTheme {
        Surface {
            // Create a real instance for preview
            SettingsScreen(
                userPreferencesManager = UserPreferencesManager(context)
            )
        }
    }
}

// Preview Components
@Preview(showBackground = true)
@Composable
fun DonationHeaderPreview() {
    LinkzaryTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Non-donated state
                Text(
                    text = "Non-Donated State:",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                DonationHeader(
                    onDonateClick = { },
                    hasDonated = false
                )
                
                // Donated state
                Text(
                    text = "Donated State (Centered):",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                DonationHeader(
                    onDonateClick = { },
                    hasDonated = true
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DonationHeaderDarkPreview() {
    LinkzaryTheme {
        Surface {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                DonationHeader(
                    onDonateClick = { },
                    hasDonated = true
                )
                
                DonationHeader(
                    onDonateClick = { },
                    hasDonated = false
                )
            }
        }
    }
}
