package com.appcodecraft.linkzary.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.unit.Velocity

/**
 * FAB scroll behavior (spec v3 §2.2): the FAB slides out while the user scrolls
 * DOWN and slides back on scroll UP, via a [NestedScrollConnection] — no custom
 * scroll listener fighting the scaffold. Attach [nestedScrollConnection] to the
 * screen's scroll container root.
 */
class LinkzaryFabScrollBehavior {
    var isHidden by mutableStateOf(false)
        private set

    val nestedScrollConnection = object : NestedScrollConnection {
        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource
        ): Offset {
            if (consumed.y < -0.5f) {
                isHidden = true // content scrolled down → tuck the FAB away
            } else if (consumed.y > 0.5f) {
                isHidden = false // scrolled back up → bring it home
            }
            return Offset.Zero
        }

        override suspend fun onPreFling(available: Velocity): Velocity = Velocity.Zero
    }
}

@Composable
fun rememberLinkzaryFabScrollBehavior(): LinkzaryFabScrollBehavior =
    remember { LinkzaryFabScrollBehavior() }

/**
 * THE one FAB (spec v3 §2.2): M3 [ExtendedFloatingActionButton] — 24dp icon +
 * full label, wrap-content width so text can never clip — with slide
 * hide/return driven by [behavior] (omit on screens without lists). The cookie
 * scallop is retired from the structure; press feedback is a springy scale.
 */
@Composable
fun LinkzaryExtendedFab(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    behavior: LinkzaryFabScrollBehavior? = null
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    AnimatedVisibility(
        visible = behavior?.isHidden != true,
        enter = slideInVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            initialOffsetY = { it }
        ),
        exit = slideOutVertically(
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            ),
            targetOffsetY = { it }
        ),
        modifier = modifier
    ) {
        ExtendedFloatingActionButton(
            onClick = onClick,
            interactionSource = interactionSource,
            modifier = Modifier.graphicsLayer {
                // Press feedback: quick springy squash (the scallop's ghost).
                val s = if (pressed) 0.95f else 1f
                scaleX = s
                scaleY = s
            },
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
}
