package com.appcodecraft.linkzary.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.appcodecraft.linkzary.ui.theme.CookieShape

/**
 * LinkzaryDialog (spec v4 §2.1) — THE one dialog scaffold for all dialogs:
 *
 *   ┌──────────────────────────────────┐  surfaceContainerLowest, 28dp radius,
 *   │ (icon tile) Title                │  width = min(360dp, screen−32dp)
 *   │  optional 1-line subtitle        │  48dp cookie-8 clipped icon tile
 *   ├──────────────────────────────────┤  content, 24dp padding, scrollable
 *   │  fields / lists / body…          │
 *   ├──────────────────────────────────┤
 *   │ [ Dismiss ]        [ Confirm ]   │  dismiss LEFT outlined pill,
 *   └──────────────────────────────────┘  confirm RIGHT filled pill (40dp)
 *
 * Destructive dialogs ([destructive] = true): icon tile and confirm button
 * switch to errorContainer/onErrorContainer — never plain grey TextButtons.
 * Only this file defines dialog chrome; screens supply content only.
 */
@Composable
fun LinkzaryDialog(
    onDismissRequest: () -> Unit,
    title: String,
    confirmLabel: String?,
    onConfirm: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    subtitle: String? = null,
    dismissLabel: String? = null,
    onDismiss: (() -> Unit)? = null,
    destructive: Boolean = false,
    confirmEnabled: Boolean = true,
    content: (@Composable () -> Unit)? = null
) {
    val container = if (destructive) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.primaryContainer
    }
    val onContainer = if (destructive) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onPrimaryContainer
    }

    val dialogHaptics = rememberLinkzaryHaptics()

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = modifier
                .padding(horizontal = 16.dp)
                .widthIn(max = 360.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surfaceContainerLowest
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                // Header: 48dp shape-clipped icon tile + title (+ optional subtitle)
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (icon != null) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CookieShape)
                                .background(container),
                            contentAlignment = Alignment.Center
                        ) {
                            androidx.compose.material3.Icon(
                                imageVector = icon,
                                contentDescription = null,
                                tint = onContainer,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                    }
                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (subtitle != null) {
                            Text(
                                text = subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (content != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 420.dp)
                            .verticalScroll(rememberScrollState())
                    ) {
                        content()
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Footer: dismiss outlined pill LEFT, confirm filled pill RIGHT
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (dismissLabel != null) {
                        OutlinedButton(
                            onClick = { onDismiss?.invoke() },
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.height(40.dp)
                        ) {
                            Text(dismissLabel)
                        }
                        Spacer(modifier = Modifier.weight(1f))
                    }
                    if (confirmLabel != null) {
                        Button(
                            onClick = {
                                // v4.1: CONFIRM haptic on dialog confirm,
                                // destructive included (delete = confirm too).
                                dialogHaptics(LinkzaryHaptic.CONFIRM)
                                onConfirm()
                            },
                            enabled = confirmEnabled,
                            shape = RoundedCornerShape(50),
                            modifier = Modifier.height(40.dp),
                            colors = if (destructive) {
                                ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer,
                                    contentColor = MaterialTheme.colorScheme.onErrorContainer
                                )
                            } else {
                                ButtonDefaults.buttonColors()
                            }
                        ) {
                            Text(confirmLabel)
                        }
                    }
                }
            }
        }
    }
}
