package com.appcodecraft.linkzary.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Flight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Gamepad
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SportsSoccer
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.appcodecraft.linkzary.R

@Composable
fun CreateCollectionForm(
    name: String,
    onNameChange: (String) -> Unit,
    selectedColor: Int,
    onColorSelected: (Int) -> Unit,
    selectedIcon: String = "Folder",
    onIconSelected: (String) -> Unit = {},
    modifier: Modifier = Modifier
) {
    var showCustomColorPicker by remember { mutableStateOf(false) }
    var customColor by remember { mutableStateOf(Color(selectedColor)) }
    val maxNameLength = 30
    val hapticFeedback = LocalHapticFeedback.current

    val colors = listOf(
        0xFF6366F1.toInt(), // Indigo
        0xFFEF4444.toInt(), // Red
        0xFF10B981.toInt(), // Emerald
        0xFFF59E0B.toInt(), // Amber
        0xFF8B5CF6.toInt(), // Violet
        0xFF06B6D4.toInt(), // Cyan
        0xFFEC4899.toInt(), // Pink
        0xFF84CC16.toInt(), // Lime
        0xFFF97316.toInt(), // Orange
        0xFFEAB308.toInt(), // Yellow
        0xFF22C55E.toInt(), // Green
        0xFF3B82F6.toInt(), // Blue
        0xFFA855F7.toInt(), // Purple
        0xFFE11D48.toInt(), // Rose
        0xFF0EA5E9.toInt(), // Sky
        0xFF64748B.toInt()  // Slate
    )

    // Available icons
    val icons = listOf(
        "Folder" to Icons.Default.Folder,
        "Work" to Icons.Default.Work,
        "School" to Icons.Default.School,
        "Home" to Icons.Default.Home,
        "Star" to Icons.Default.Star,
        "Favorite" to Icons.Default.Favorite,
        "Code" to Icons.Default.Code,
        "Book" to Icons.Default.Book,
        "Music" to Icons.Default.MusicNote,
        "Movie" to Icons.Default.Movie,
        "Image" to Icons.Default.Image,
        "Map" to Icons.Default.Map,
        "Shopping" to Icons.Default.ShoppingCart,
        "Travel" to Icons.Default.Flight,
        "Sports" to Icons.Default.SportsSoccer,
        "Game" to Icons.Default.Gamepad
    )

    Column(modifier = modifier) {
        OutlinedTextField(
            value = name,
            onValueChange = { newValue ->
                if (newValue.length <= maxNameLength) {
                    onNameChange(newValue)
                }
            },
            label = { Text(stringResource(R.string.create_collection_name_label)) },
            placeholder = { Text(stringResource(R.string.create_collection_name_placeholder)) },
            leadingIcon = {
                // Show selected icon here dynamically
                val currentIcon = icons.find { it.first == selectedIcon }?.second ?: Icons.Default.Folder
                Icon(
                    imageVector = currentIcon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            supportingText = {
                Text(
                    text = "${name.length}/$maxNameLength",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (name.length >= maxNameLength) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        )

        Spacer(modifier = Modifier.height(16.dp))
        
        // Icon Selection Section
        Text(
            text = "Choose Icon",
            style = MaterialTheme.typography.labelMedium
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        LazyVerticalGrid(
            columns = GridCells.Fixed(8),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.height(80.dp)
        ) {
            items(icons) { (iconName, iconVector) ->
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(32.dp)
                ) {
                    FilterChip(
                        onClick = {
                            if (selectedIcon != iconName) {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                onIconSelected(iconName)
                            }
                        },
                        label = { },
                        selected = selectedIcon == iconName,
                        modifier = Modifier.size(32.dp),
                        leadingIcon = {
                             Icon(
                                imageVector = iconVector,
                                contentDescription = iconName,
                                modifier = Modifier.size(16.dp),
                                tint = if (selectedIcon == iconName) MaterialTheme.colorScheme.onSecondaryContainer else MaterialTheme.colorScheme.onSurfaceVariant
                             )
                        },
                        colors = FilterChipDefaults.filterChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha=0.3f),
                            selectedContainerColor = MaterialTheme.colorScheme.secondaryContainer
                        ),
                        border = null
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = stringResource(R.string.create_collection_choose_color),
            style = MaterialTheme.typography.labelMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (showCustomColorPicker) {
            // Custom color picker UI
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            ) {
                // Color preview
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(40.dp)
                        .background(customColor, RoundedCornerShape(8.dp))
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Red slider
                Text("Red", style = MaterialTheme.typography.bodySmall)
                Slider(
                    value = customColor.red,
                    onValueChange = {
                        customColor = customColor.copy(red = it)
                        onColorSelected(android.graphics.Color.rgb(
                            (customColor.red * 255).toInt(),
                            (customColor.green * 255).toInt(),
                            (customColor.blue * 255).toInt()
                        ))
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = Color.Red,
                        activeTrackColor = Color.Red.copy(alpha = 0.5f)
                    )
                )

                // Green slider
                Text("Green", style = MaterialTheme.typography.bodySmall)
                Slider(
                    value = customColor.green,
                    onValueChange = {
                        customColor = customColor.copy(green = it)
                        onColorSelected(android.graphics.Color.rgb(
                            (customColor.red * 255).toInt(),
                            (customColor.green * 255).toInt(),
                            (customColor.blue * 255).toInt()
                        ))
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = Color.Green,
                        activeTrackColor = Color.Green.copy(alpha = 0.5f)
                    )
                )

                // Blue slider
                Text("Blue", style = MaterialTheme.typography.bodySmall)
                Slider(
                    value = customColor.blue,
                    onValueChange = {
                        customColor = customColor.copy(blue = it)
                        onColorSelected(android.graphics.Color.rgb(
                            (customColor.red * 255).toInt(),
                            (customColor.green * 255).toInt(),
                            (customColor.blue * 255).toInt()
                        ))
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = Color.Blue,
                        activeTrackColor = Color.Blue.copy(alpha = 0.5f)
                    )
                )

                // Back button
                TextButton(
                    onClick = { showCustomColorPicker = false },
                    modifier = Modifier.align(Alignment.End)
                ) {
                    Text("Back to Presets")
                }
            }
        } else {
            // Predefined colors grid
            LazyVerticalGrid(
                columns = GridCells.Fixed(8),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(120.dp)
            ) {
                items(colors) { color ->
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(32.dp)
                    ) {
                        FilterChip(
                            onClick = {
                                if (selectedColor != color) {
                                    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onColorSelected(color)
                                }
                            },
                            label = { },
                            selected = selectedColor == color,
                            modifier = Modifier.size(32.dp),
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = Color(color),
                                selectedContainerColor = Color(color)
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = selectedColor == color,
                                borderColor = if (selectedColor == color) MaterialTheme.colorScheme.primary else Color.Transparent,
                                borderWidth = if (selectedColor == color) 2.dp else 0.dp
                            )
                        )

                        // Selection indicator
                        if (selectedColor == color) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = "Selected",
                                tint = Color.White,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // Custom color option
                item {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .size(32.dp)
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                            .clickable {
                                hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
                                showCustomColorPicker = true
                                // Initialize custom color picker with current selection
                                customColor = Color(selectedColor)
                            }
                    ) {
                        Icon(
                            imageVector = Icons.Default.Palette,
                            contentDescription = "Custom Color",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
