# Linkzary App - Enhanced Theming Guide

## Overview

The Linkzary app now features a comprehensive theming system that provides excellent visual experience in both light and dark modes. The theming follows Material Design 3 principles with carefully selected colors that enhance readability and user experience.

## Color Palette

### Light Theme
- **Primary**: Modern Indigo (`#4F46E5`) - Used for primary actions and highlights
- **Secondary**: Bright Indigo (`#6366F1`) - Used for secondary elements
- **Tertiary**: Cyan Accent (`#06B6D4`) - Used for accent elements
- **Background**: Light Gray (`#FAFAFA`) - Main background color
- **Surface**: Pure White (`#FFFFFF`) - Card and component backgrounds
- **Error**: Modern Red (`#EF4444`) - Error states and destructive actions

### Dark Theme
- **Primary**: Light Indigo (`#818CF8`) - Adjusted for dark backgrounds
- **Secondary**: Purple Accent (`#8B5CF6`) - Complementary secondary color
- **Tertiary**: Bright Cyan (`#22D3EE`) - Vibrant accent for dark mode
- **Background**: Deep Slate (`#0F172A`) - Rich dark background
- **Surface**: Slate Surface (`#1E293B`) - Elevated component backgrounds
- **Error**: Light Red (`#F87171`) - Error states optimized for dark mode

## Typography Scale

The app uses a comprehensive typography scale that ensures excellent readability and visual hierarchy:

### Display Styles
- **Display Large**: 57sp, Bold - For major headings
- **Display Medium**: 45sp, Bold - For section headers
- **Display Small**: 36sp, Bold - For subsection headers

### Headline Styles
- **Headline Large**: 32sp, SemiBold - For page titles
- **Headline Medium**: 28sp, SemiBold - For section titles
- **Headline Small**: 24sp, SemiBold - For component headers

### Title Styles
- **Title Large**: 22sp, Medium - For card titles
- **Title Medium**: 16sp, Medium - For list item titles
- **Title Small**: 14sp, Medium - For small component titles

### Body Styles
- **Body Large**: 16sp, Normal - For main content
- **Body Medium**: 14sp, Normal - For secondary content
- **Body Small**: 12sp, Normal - For captions and metadata

### Label Styles
- **Label Large**: 14sp, Medium - For button text
- **Label Medium**: 12sp, Medium - For form labels
- **Label Small**: 11sp, Medium - For small interactive elements

## Key Features

### 1. Accessibility
- High contrast ratios for better readability
- Proper color combinations for text and backgrounds
- Support for system accessibility settings

### 2. Consistency
- Unified color scheme across all components
- Consistent spacing and typography
- Proper semantic color usage

### 3. Modern Design
- Material Design 3 compliance
- Contemporary color palette
- Clean and professional appearance

### 4. Dynamic Color Support
- Automatic adaptation to system theme
- Support for Android 12+ dynamic colors
- Fallback to custom colors on older devices

## Implementation Details

### Theme Files
- `Color.kt`: Defines all color values for light and dark themes
- `Theme.kt`: Configures Material3 color schemes
- `Type.kt`: Defines typography scale and text styles
- `ThemePreview.kt`: Showcases theme components and colors

### Usage Examples

```kotlin
// Using theme colors
Text(
    text = "Welcome to Linkzary",
    color = MaterialTheme.colorScheme.primary,
    style = MaterialTheme.typography.headlineMedium
)

// Using surface colors for cards
Card(
    colors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surface
    )
) {
    // Card content
}

// Using semantic colors for states
Button(
    colors = ButtonDefaults.buttonColors(
        containerColor = MaterialTheme.colorScheme.error
    )
) {
    Text("Delete")
}
```

## Best Practices

### 1. Color Usage
- Use `MaterialTheme.colorScheme.primary` for main actions
- Use `MaterialTheme.colorScheme.surface` for card backgrounds
- Use `MaterialTheme.colorScheme.error` for destructive actions
- Use `MaterialTheme.colorScheme.onSurface` for text on surfaces

### 2. Typography
- Use appropriate text styles for content hierarchy
- Combine text styles with semantic colors
- Maintain consistent line heights and spacing

### 3. Component Styling
- Leverage Material3 component defaults
- Override colors using theme-aware values
- Maintain consistency across similar components

## Testing

To test the theming:

1. **Light Mode**: Default system theme or force light mode
2. **Dark Mode**: Enable dark mode in system settings or force dark mode
3. **Dynamic Colors**: Test on Android 12+ devices with Material You
4. **Accessibility**: Test with high contrast and large text settings

## Preview

Use the `ThemePreview.kt` file to see all theme components in action:
- Color palette showcase
- Typography scale demonstration
- Component styling examples
- Both light and dark mode previews

The enhanced theming system ensures that Linkzary provides a beautiful, consistent, and accessible user experience across all devices and system configurations.