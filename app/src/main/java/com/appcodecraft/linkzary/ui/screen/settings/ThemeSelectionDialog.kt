package com.appcodecraft.linkzary.ui.screen.settings

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.appcodecraft.linkzary.R
import com.appcodecraft.linkzary.data.preferences.ThemeMode
import com.appcodecraft.linkzary.ui.component.LinkzaryDialog

/**
 * v5.1: the light/dark/system radio list is now a standard M3
 * SingleChoiceSegmentedButtonRow — three segments, selected segment shows the
 * filled/checked indicator. Same placement (Appearance → Theme dialog).
 */
@Composable
fun ThemeSelectionDialog(
    currentTheme: ThemeMode,
    onDismiss: () -> Unit,
    onThemeSelected: (ThemeMode) -> Unit
) {
    com.appcodecraft.linkzary.ui.component.LinkzaryDialog(
        onDismissRequest = onDismiss,
        icon = Icons.Rounded.Palette,
        title = "Choose Theme",
        confirmLabel = null,
        onConfirm = {},
        dismissLabel = stringResource(R.string.common_cancel),
        onDismiss = onDismiss
    ) {
        SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
            SegmentedButton(
                selected = currentTheme == ThemeMode.LIGHT,
                onClick = { onThemeSelected(ThemeMode.LIGHT) },
                shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                label = { Text(stringResource(R.string.settings_theme_light)) }
            )
            SegmentedButton(
                selected = currentTheme == ThemeMode.DARK,
                onClick = { onThemeSelected(ThemeMode.DARK) },
                shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                label = { Text(stringResource(R.string.settings_theme_dark)) }
            )
            SegmentedButton(
                selected = currentTheme == ThemeMode.SYSTEM,
                onClick = { onThemeSelected(ThemeMode.SYSTEM) },
                shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                label = { Text(stringResource(R.string.settings_theme_system)) }
            )
        }
    }
}
