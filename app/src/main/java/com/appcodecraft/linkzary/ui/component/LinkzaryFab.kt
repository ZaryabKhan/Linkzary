package com.appcodecraft.linkzary.ui.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow

/**
 * The one FAB (bug build 2026-11-15): a standard M3 [ExtendedFloatingActionButton]
 * — icon + full label, sized to its content so the text can never clip ("Add lin…",
 * "New colle…"), and no scroll morph to break on list scrolling. Call sites keep
 * the 16dp edge padding; screens own the 96dp bottom content clearance.
 */
@Composable
fun LinkzaryFab(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ExtendedFloatingActionButton(
        onClick = onClick,
        modifier = modifier,
        icon = {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null
            )
        },
        text = {
            // The label itself is the semantic content; no override needed.
            Text(
                text = label,
                maxLines = 1,
                overflow = TextOverflow.Visible
            )
        }
    )
}
