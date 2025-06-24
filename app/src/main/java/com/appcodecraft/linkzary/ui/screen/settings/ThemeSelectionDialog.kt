package com.appcodecraft.linkzary.ui.screen.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.SettingsBrightness
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.appcodecraft.linkzary.data.preferences.ThemeMode

@Composable
fun ThemeSelectionDialog(
    currentTheme: ThemeMode,
    onDismiss: () -> Unit,
    onThemeSelected: (ThemeMode) -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Choose Theme",
                style = MaterialTheme.typography.headlineSmall
            )
        },
        text = {
            Column(
                modifier = Modifier.selectableGroup()
            ) {
                ThemeOption(
                    icon = Icons.Default.LightMode,
                    title = "Light",
                    subtitle = "Always use light theme",
                    selected = currentTheme == ThemeMode.LIGHT,
                    onClick = { onThemeSelected(ThemeMode.LIGHT) }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                ThemeOption(
                    icon = Icons.Default.DarkMode,
                    title = "Dark",
                    subtitle = "Always use dark theme",
                    selected = currentTheme == ThemeMode.DARK,
                    onClick = { onThemeSelected(ThemeMode.DARK) }
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                ThemeOption(
                    icon = Icons.Default.SettingsBrightness,
                    title = "System default",
                    subtitle = "Follow system theme setting",
                    selected = currentTheme == ThemeMode.SYSTEM,
                    onClick = { onThemeSelected(ThemeMode.SYSTEM) }
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ThemeOption(
    icon: ImageVector,
    title: String,
    subtitle: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .selectable(
                selected = selected,
                onClick = onClick,
                role = Role.RadioButton
            )
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RadioButton(
            selected = selected,
            onClick = null
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurface
        )
        
        Spacer(modifier = Modifier.width(16.dp))
        
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}