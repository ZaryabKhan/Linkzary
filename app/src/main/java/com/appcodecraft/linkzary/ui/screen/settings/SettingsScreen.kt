package com.appcodecraft.linkzary.ui.screen.settings

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
                SettingsSectionHeader("General")
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Palette,
                    title = "Theme",
                    subtitle = when (currentThemeMode) {
                        ThemeMode.LIGHT -> "Light"
                        ThemeMode.DARK -> "Dark"
                        ThemeMode.SYSTEM -> "System default"
                    },
                    onClick = {
                        showThemeDialog = true
                    }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Language,
                    title = "Language",
                    subtitle = "English",
                    onClick = {
                        showLanguageDialog = true
                    }
                )
            }
            
            // Data Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSectionHeader("Data")
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Upload,
                    title = "Export Data",
                    subtitle = "Export your bookmarks and collections",
                    onClick = {
                        showExportDialog = true
                    }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Download,
                    title = "Import Data",
                    subtitle = "Import bookmarks from file",
                    onClick = {
                        importLauncher.launch(arrayOf("application/json", "text/csv", "text/plain", "*/*"))
                    }
                )
            }
            
            // Privacy Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSectionHeader("Privacy")
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Security,
                    title = "Privacy Policy",
                    subtitle = "View our privacy policy",
                    onClick = {
                        openPrivacyPolicy(context)
                    }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Delete,
                    title = "Clear All Data",
                    subtitle = "Delete all bookmarks and collections",
                    onClick = {
                        showClearDataDialog = true
                    },
                    isDestructive = true
                )
            }
            
            // About Section
            item {
                Spacer(modifier = Modifier.height(16.dp))
                SettingsSectionHeader("About")
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Info,
                    title = "About Linkzary",
                    subtitle = "Version 1.0.0",
                    onClick = {
                        showAboutDialog = true
                    }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Star,
                    title = "Rate App",
                    subtitle = "Rate us on Google Play",
                    onClick = {
                        openLinkzaryInPlayStore(context)
                    }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Feedback,
                    title = "Send Feedback",
                    subtitle = "Help us improve the app",
                    onClick = {
                        sendFeedbackEmail(context)
                    }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Person,
                    title = "Visit Developer Profile",
                    subtitle = "More apps by AppCodeCraft",
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
            onLanguageSelected = { language ->
                // Language selection will be implemented later
                showLanguageDialog = false
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
                    text = "Support Linkzary",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    textAlign = TextAlign.Center
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Help us keep Linkzary free and ad-free! Your support means the world to us.",
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
                        text = "Donate Now",
                        fontWeight = FontWeight.SemiBold
                    )
                }
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = "Donations are voluntary and don't unlock features",
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
        "English" to "en",
        "Français" to "fr",
        "Português (Brasil)" to "pt-br",
        "Español" to "es",
        "Deutsch" to "de",
        "Italiano" to "it",
        "日本語" to "ja",
        "한국어" to "ko",
        "中文" to "zh"
    )
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Select Language",
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
                Text("Cancel")
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
                text = "Clear All Data?",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.error
            )
        },
        text = {
            Column {
                Text(
                    text = "This action will permanently delete:",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text("• All saved links and bookmarks")
                Text("• All collections and their contents")
                Text("• All tags and notes")
                Text("• All app preferences")
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "This action cannot be undone!",
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
                Text("Clear All Data")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
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
                    modifier = Modifier.size(32.dp)
                )
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "About Linkzary",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        text = {
            LazyColumn {
                item {
                    Column {
                        Text(
                            text = "Linkzary is a beautiful, minimal link saver that helps you organize and manage your favorite bookmarks with ease.",
                            style = MaterialTheme.typography.bodyLarge
                        )
                        
                        Spacer(modifier = Modifier.height(16.dp))
                        
                        // App Info
                        InfoRow("Version", "1.0.0")
                        InfoRow("Developer", "AppCodeCraft")
                        InfoRow("Built with", "Jetpack Compose & Kotlin")
                        
                        Spacer(modifier = Modifier.height(20.dp))
                        
                        // More Apps Section
                        Text(
                            text = "More Apps from Developer",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        
                        Spacer(modifier = Modifier.height(12.dp))
                        
                        // App Cards
                        AppCard(
                            name = "CurioShuffle",
                            description = "Discover amazing content with smart shuffling",
                            icon = Icons.Default.Shuffle,
                            onClick = {
                                openAppInPlayStore(context, "com.appcodecraft.curioshuffle")
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        AppCard(
                            name = "CurioMate",
                            description = "CurioMate offers a collection of utility tools to assist with everyday tasks.",
                            icon = Icons.Default.Construction,
                            onClick = {
                                openAppInPlayStore(context, "com.appcodecraft.curiomate")
                            }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close")
            }
        }
    )
}

@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
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