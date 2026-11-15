package com.appcodecraft.linkzary.ui.component

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/** Which status dot the [DriveBackupCard] shows (spec v5.2 §2). */
enum class DriveCardState { IDLE, SUCCESS, ERROR, WORKING }

/**
 * Drive Backup hero card (spec v5.2, binding): the card finally uses chromatic
 * M3 roles so it reads saturated in every theme — brand indigo with
 * "Use Linkzary colors" off, the user's dynamic palette with it on.
 *
 * Fill: linearGradient(primaryContainer → primary @28% alpha, 135°).
 * 124dp tall / 24dp radius, 44dp primary icon tile with onPrimary cloud glyph,
 * 17sp/700 title, status line with an 8dp dot (success = tertiary,
 * error = error, working = onPrimary pulsing), and two 40dp pills:
 * filled primary "Back up now" + outlined onPrimaryContainer "Restore".
 * Tokens only — zero hard-coded hex.
 */
@Composable
fun DriveBackupCard(
    title: String,
    statusText: String,
    state: DriveCardState,
    backUpLabel: String,
    restoreLabel: String,
    onBackUpNow: () -> Unit,
    onRestore: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isWorking = state == DriveCardState.WORKING

    // Press: card scales 0.98. No animation loop beyond the working dot pulse.
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale = if (pressed) 0.98f else 1f

    val dotColor = when (state) {
        DriveCardState.SUCCESS -> MaterialTheme.colorScheme.tertiary
        DriveCardState.ERROR -> MaterialTheme.colorScheme.error
        DriveCardState.WORKING, DriveCardState.IDLE -> MaterialTheme.colorScheme.onPrimary
    }

    // Gradient stops read in composition (drawBehind is not composable scope).
    val primaryContainer = MaterialTheme.colorScheme.primaryContainer
    val primary = MaterialTheme.colorScheme.primary

    // Subtle pulse on the dot only while a Drive action runs.
    val pulse = rememberInfiniteTransition(label = "driveDotPulse")
    val dotAlpha by pulse.animateFloat(
        initialValue = 1f,
        targetValue = 0.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "driveDotAlpha"
    )
    val resolvedDotAlpha = if (isWorking) dotAlpha else 1f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .clickable(interactionSource = interactionSource, indication = null) { /* press feedback only */ },
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .height(124.dp)
                .drawBehind {
                    // 135° brand gradient: primaryContainer → primary @28%.
                    // CSS-style 135° runs top-left → bottom-right.
                    drawRect(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                primaryContainer,
                                primary.copy(alpha = 0.28f)
                            ),
                            start = Offset.Zero,
                            end = Offset(size.width, size.height)
                        )
                    )
                }
                .padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Saturation anchor: 44dp primary tile, onPrimary cloud glyph.
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .background(
                            color = MaterialTheme.colorScheme.primary,
                            shape = RoundedCornerShape(14.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CloudSync,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = 17.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    // v6.2 alignment fix: dot rides on the FIRST status line
                    // (top-aligned, optically centered on the 12.5sp line) so
                    // a wrapped second line can't detach it from the text.
                    Row(verticalAlignment = Alignment.Top) {
                        Box(
                            modifier = Modifier
                                .padding(top = 4.dp)
                                .size(8.dp)
                                .graphicsLayer { alpha = resolvedDotAlpha }
                                .background(color = dotColor, shape = RoundedCornerShape(50))
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = statusText,
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 12.5.sp),
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.85f),
                            maxLines = 2
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // v6.2 alignment fix: stable right column — pills share one
                // consistent 4dp rhythm and a common right edge.
                Column(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Button(
                        onClick = onBackUpNow,
                        enabled = !isWorking,
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary,
                            disabledContainerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.5f),
                            disabledContentColor = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f)
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 14.dp, vertical = 0.dp
                        ),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text(
                            text = backUpLabel,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = onRestore,
                        enabled = !isWorking,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(
                            width = 1.5.dp,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(
                                alpha = if (isWorking) 0.5f else 1f
                            )
                        ),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                            disabledContentColor = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.5f)
                        ),
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(
                            horizontal = 14.dp, vertical = 0.dp
                        ),
                        modifier = Modifier.height(40.dp)
                    ) {
                        Text(
                            text = restoreLabel,
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
