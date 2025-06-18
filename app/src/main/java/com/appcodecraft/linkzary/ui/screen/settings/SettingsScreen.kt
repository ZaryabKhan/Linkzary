package com.appcodecraft.linkzary.ui.screen.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.appcodecraft.linkzary.ui.theme.LinkzaryTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen() {
    var showAboutDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))
        
        // Header
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(24.dp))
        
        LazyColumn(
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // General Section
            item {
                SettingsSectionHeader("General")
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Palette,
                    title = "Theme",
                    subtitle = "System default",
                    onClick = {
                        // TODO: Implement theme selection
                    }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Language,
                    title = "Language",
                    subtitle = "English",
                    onClick = {
                        // TODO: Implement language selection
                    }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Notifications,
                    title = "Notifications",
                    subtitle = "Manage notification preferences",
                    onClick = {
                        // TODO: Implement notification settings
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
                        showImportDialog = true
                    }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.CloudSync,
                    title = "Sync",
                    subtitle = "Backup and sync across devices",
                    onClick = {
                        // TODO: Implement sync settings
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
                        // TODO: Open privacy policy
                    }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Delete,
                    title = "Clear All Data",
                    subtitle = "Delete all bookmarks and collections",
                    onClick = {
                        // TODO: Implement clear data with confirmation
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
                        // TODO: Open Play Store rating
                    }
                )
            }
            
            item {
                SettingsItem(
                    icon = Icons.Default.Feedback,
                    title = "Send Feedback",
                    subtitle = "Help us improve the app",
                    onClick = {
                        // TODO: Open feedback form
                    }
                )
            }
            
            // Add bottom padding
            item {
                Spacer(modifier = Modifier.height(16.dp))
            }
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
            onExport = {
                // TODO: Implement export
                showExportDialog = false
            }
        )
    }
    
    // Import Dialog
    if (showImportDialog) {
        ImportDataDialog(
            onDismiss = { showImportDialog = false },
            onImport = {
                // TODO: Implement import
                showImportDialog = false
            }
        )
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

@Composable
fun AboutDialog(
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("About Linkzary") },
        text = {
            Column {
                Text("Linkzary is a modern bookmark manager that helps you organize and save your favorite links.")
                Spacer(modifier = Modifier.height(8.dp))
                Text("Version: 1.0.0")
                Text("Built with ❤️ using Jetpack Compose")
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("OK")
            }
        }
    )
}

@Composable
fun ExportDataDialog(
    onDismiss: () -> Unit,
    onExport: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Export Data") },
        text = {
            Text("Export all your bookmarks and collections to a JSON file. This file can be used to backup your data or import it on another device.")
        },
        confirmButton = {
            TextButton(onClick = onExport) {
                Text("Export")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
fun ImportDataDialog(
    onDismiss: () -> Unit,
    onImport: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Import Data") },
        text = {
            Text("Import bookmarks and collections from a JSON file. This will add to your existing data without removing anything.")
        },
        confirmButton = {
            TextButton(onClick = onImport) {
                Text("Import")
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
fun SettingsScreenPreview() {
    LinkzaryTheme {
        Surface {
            SettingsScreen()
        }
    }
}