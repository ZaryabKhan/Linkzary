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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Construction
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Feedback
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.compose.ui.res.stringResource
import com.appcodecraft.linkzary.BuildConfig
import com.appcodecraft.linkzary.R
import com.appcodecraft.linkzary.utils.LocaleHelper
import com.appcodecraft.linkzary.data.preferences.ThemeMode
import com.appcodecraft.linkzary.data.preferences.UserPreferencesManager
import com.appcodecraft.linkzary.ui.theme.LinkzaryTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    importExportViewModel: ImportExportViewModel = hiltViewModel(),
    userPreferencesManager: UserPreferencesManager
) {
    val context = LocalContext.current
    
    var showAboutDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }
    var showThemeDialog by remember { mutableStateOf(false) }
    var showClearDataDialog by remember { mutableStateOf(false) }
    var showDonationDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    
    val importExportUiState by importExportViewModel.uiState.collectAsState()
    val importProgress by importExportViewModel.importProgress.collectAsState()
    val importResult by importExportViewModel.importResult.collectAsState()
    val importPreview by importExportViewModel.importPreview.collectAsState()
    
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
    
    var selectedImportUri by remember { mutableStateOf<Uri?>(null) }
    
    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let { 
            selectedImportUri = it
            importExportViewModel.previewImport(context, it)
        }
    }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Donation Header
            item {
                DonationHeader(
                onDonateClick = { showDonationDialog = true }
            )
            }
            
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // General Section
            item {
                SettingsSectionHeader(stringResource(R.string.settings_general))
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Palette,
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
            
            item {
                SettingsItem(
                    icon = Icons.Default.Language,
                    title = stringResource(R.string.settings_language),
                    subtitle = LocaleHelper.getLanguageDisplayName(currentLanguage),
                    onClick = {
                        showLanguageDialog = true
                    }
                )
            }
            
            // Data Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSectionHeader(stringResource(R.string.settings_data))
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Upload,
                    title = stringResource(R.string.settings_export_data),
                    subtitle = stringResource(R.string.settings_export_description),
                    onClick = {
                        showExportDialog = true
                    }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Download,
                    title = stringResource(R.string.settings_import_data),
                    subtitle = stringResource(R.string.settings_import_description),
                    onClick = {
                        importLauncher.launch(arrayOf("application/json", "text/csv", "text/plain", "*/*"))
                    }
                )
            }
            
            // Privacy Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSectionHeader(stringResource(R.string.settings_privacy))
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Security,
                    title = stringResource(R.string.settings_privacy_policy),
                    subtitle = stringResource(R.string.settings_privacy_description),
                    onClick = {
                        openPrivacyPolicy(context)
                    }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Delete,
                    title = stringResource(R.string.settings_clear_data),
                    subtitle = stringResource(R.string.settings_clear_data_description),
                    onClick = {
                        showClearDataDialog = true
                    },
                    isDestructive = true
                )
            }
            
            // About Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSectionHeader(stringResource(R.string.settings_about))
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = stringResource(R.string.settings_about_app),
                    subtitle = stringResource(R.string.settings_version, "1.0.0"),
                    onClick = {
                        showAboutDialog = true
                    }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Star,
                    title = stringResource(R.string.settings_rate_app),
                    subtitle = stringResource(R.string.settings_rate_description),
                    onClick = {
                        openLinkzaryInPlayStore(context)
                    }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Feedback,
                    title = stringResource(R.string.settings_send_feedback),
                    subtitle = stringResource(R.string.settings_feedback_description),
                    onClick = {
                        sendFeedbackEmail(context)
                    }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Person,
                    title = stringResource(R.string.settings_developer_profile),
                    subtitle = stringResource(R.string.settings_developer_description),
                    onClick = {
                        openDeveloperProfile(context)
                    }
                )
            }
            
            // Add bottom padding
            item {
                Spacer(modifier = Modifier.height(16.dp))
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
    
    // Donation Dialog
    if (showDonationDialog) {
        DonationDialog(
            onDismiss = { showDonationDialog = false }
        )
    }
}

// Donation Header Component
@Composable
fun DonationHeader(
    onDonateClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onDonateClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                            MaterialTheme.colorScheme.secondary.copy(alpha = 0.1f)
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.Favorite,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(32.dp)
                )
                
                Spacer(modifier = Modifier.height(12.dp))
                
                Text(
                    text = stringResource(R.string.settings_support_title),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = stringResource(R.string.settings_support_description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.8f),
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Button(
                    onClick = onDonateClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.VolunteerActivism,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(R.string.settings_donate_button),
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = stringResource(R.string.settings_donate_disclaimer),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 8.dp)
    )
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
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
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
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
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
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = stringResource(R.string.settings_select_language),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            LazyColumn {
                items(languages) { (name, code) ->
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable {
                                onLanguageSelected(code)
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
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
        },
        confirmButton = {},
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}

// Clear Data Confirmation Dialog
@Composable
fun ClearDataConfirmationDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                imageVector = Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(32.dp)
            )
        },
        title = {
            Text(
                text = stringResource(R.string.settings_clear_data_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
        },
        text = {
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
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(stringResource(R.string.settings_clear_data))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_cancel))
            }
        }
    )
}

// Enhanced About Dialog
@Composable
fun AboutDialog(
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Bookmark,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = stringResource(R.string.settings_about_app),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
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
                
                FeatureRow(Icons.Default.Security, stringResource(R.string.about_feature_secure))
                FeatureRow(Icons.Default.Favorite, stringResource(R.string.about_feature_free))
                FeatureRow(Icons.Default.Palette, stringResource(R.string.about_feature_themes))
                FeatureRow(Icons.Default.Language, stringResource(R.string.about_feature_languages))
                
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
                    icon = Icons.Default.Shuffle,
                    onClick = {
                        openAppInPlayStore(context, "com.appcodecraft.curioshuffle")
                    }
                )
                
                Spacer(modifier = Modifier.height(6.dp))
                
                AppCard(
                    name = stringResource(R.string.app_curiomate),
                    description = stringResource(R.string.app_curiomate_description),
                    icon = Icons.Default.Construction,
                    onClick = {
                        openAppInPlayStore(context, "com.appcodecraft.curiomate")
                    }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.common_close))
            }
        }
    )
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
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(8.dp)
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
                imageVector = Icons.Default.OpenInNew,
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
    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.appcodecraft.com/p/privacy-policy.html"))
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