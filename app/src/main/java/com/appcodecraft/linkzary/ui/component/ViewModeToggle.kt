package com.appcodecraft.linkzary.ui.component

import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.appcodecraft.linkzary.R

/**
 * The one list/grid control (spec v2 §2.4, audit D8): a two-segment M3
 * SegmentedButton, 40dp, pill-shaped. Active segment = secondaryContainer +
 * onSecondaryContainer with a 0.9→1 icon scale-in; inactive transparent +
 * onSurfaceVariant. Uses only scheme roles, so it works on any surface.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ViewModeToggle(
    isGridView: Boolean,
    onToggle: () -> Unit,
    modifier: Modifier = Modifier
) {
    SingleChoiceSegmentedButtonRow(
        modifier = modifier.height(40.dp)
    ) {
        SegmentedButton(
            selected = !isGridView,
            onClick = { if (isGridView) onToggle() },
            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
            icon = {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ViewList,
                    contentDescription = stringResource(R.string.home_switch_to_list_view),
                    modifier = Modifier.graphicsLayer {
                        scaleX = if (!isGridView) 1f else 0.9f
                        scaleY = if (!isGridView) 1f else 0.9f
                    }
                )
            }
        ) {
            Text(" ")
        }
        SegmentedButton(
            selected = isGridView,
            onClick = { if (!isGridView) onToggle() },
            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
            icon = {
                Icon(
                    imageVector = Icons.Default.GridView,
                    contentDescription = stringResource(R.string.home_switch_to_grid_view),
                    modifier = Modifier.graphicsLayer {
                        scaleX = if (isGridView) 1f else 0.9f
                        scaleY = if (isGridView) 1f else 0.9f
                    }
                )
            }
        ) {
            Text(" ")
        }
    }
}
