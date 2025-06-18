# RSS Integration for Linkzary

This document describes the RSS feed integration added to the Linkzary bookmark management app.

## Overview

The RSS integration allows users to:
- Add RSS feeds from any website
- View RSS feed items in a beautiful card layout
- Filter feeds by All, Unread, and Favorites
- Search through RSS content
- Mark items as read/favorite
- Open RSS articles in external browser

## Features Implemented

### 1. RSS Parser Integration
- **Library**: RSS-Parser Kotlin Multiplatform library (v6.0.7)
- **Functionality**: Parses RSS feeds and extracts article metadata
- **Error Handling**: Graceful handling of malformed feeds

### 2. Database Schema
- **Entity**: `RssFeedItem` with comprehensive fields
- **DAO**: `RssFeedItemDao` with optimized queries
- **Repository**: `RssRepository` for business logic

### 3. UI Components

#### RssFeedCard
- Modern Material 3 design matching the app's aesthetic
- Full-width preview images with fallback support
- RSS feed icon and source identification
- Category tags with rounded corners
- Author and publication date display
- Favorite/pin functionality
- Responsive layout for different screen sizes

#### RssScreen
- Search functionality across titles, descriptions, and authors
- Filter chips for All/Unread/Favorites
- Add RSS feed dialog with URL validation
- Empty state with call-to-action
- Loading states and error handling

### 4. Navigation Integration
- Added RSS tab to bottom navigation
- RSS feed icon (filled/outlined variants)
- Proper navigation state management

## Technical Implementation

### Dependencies Added
```kotlin
// libs.versions.toml
rssParser = "6.0.7"
rss-parser = { group = "com.prof18.rssparser", name = "rssparser", version.ref = "rssParser" }

// build.gradle.kts
implementation(libs.rss.parser)
```

### Database Migration
- Updated database version from 1 to 2
- Added `rss_feed_items` table
- Integrated `RssFeedItemDao` into database module

### Architecture
```
UI Layer (Compose)
├── RssScreen.kt - Main RSS feed screen
├── RssFeedCard.kt - Individual feed item card
└── RssViewModel.kt - State management

Domain Layer
├── RssRepository.kt - Business logic
└── RssFeedItem.kt - Data model

Data Layer
├── RssFeedItemDao.kt - Database operations
└── DatabaseModule.kt - Dependency injection
```

## Usage Instructions

### Adding RSS Feeds
1. Navigate to the RSS tab in bottom navigation
2. Tap the "+" button in the top-right corner
3. Enter a valid RSS feed URL (e.g., `https://github.blog/feed/`)
4. Tap "Add" to fetch and save feed items

### Managing Feed Items
- **Read Article**: Tap on any card to open in browser and mark as read
- **Favorite**: Tap the "more" button (⋮) to toggle favorite status
- **Search**: Use the search bar to find specific content
- **Filter**: Use filter chips to view All/Unread/Favorites

### Sample RSS Feeds
The app includes demo data with popular RSS feeds:
- Android Developers Blog
- Kotlin Blog
- GitHub Blog
- Stack Overflow Blog
- O'Reilly Radar

## Card Design Features

The `RssFeedCard` matches the provided UI design with:

### Visual Elements
- **Header**: RSS icon + feed source name
- **Preview Image**: Full-width with rounded corners (160dp height)
- **Content**: Title, description, and category tags
- **Footer**: Author attribution and publication date
- **Actions**: Favorite pin and more options menu

### Design Specifications
- **Card Shape**: 16dp rounded corners
- **Padding**: 16dp internal padding
- **Spacing**: 12dp between major sections, 8dp between text elements
- **Typography**: Material 3 typography scale
- **Colors**: Adaptive Material 3 color scheme
- **Images**: Coil for async loading with crossfade animation

### Responsive Features
- Text overflow handling with ellipsis
- Maximum line limits (title: 2, description: 3)
- Category tags limited to 3 with overflow handling
- Proper content scaling for different screen sizes

## Error Handling

- **Network Errors**: Graceful handling of connection issues
- **Malformed URLs**: URL validation in add feed dialog
- **Parsing Errors**: User-friendly error messages
- **Empty States**: Helpful guidance for new users

## Performance Optimizations

- **Lazy Loading**: LazyColumn for efficient list rendering
- **Image Caching**: Coil handles image caching automatically
- **Database Indexing**: Optimized queries for fast retrieval
- **State Management**: Efficient StateFlow usage

## Future Enhancements

1. **Feed Management**: Edit/delete RSS feed sources
2. **Offline Reading**: Cache articles for offline access
3. **Notifications**: Push notifications for new articles
4. **Export/Import**: OPML support for feed management
5. **Reading Progress**: Track reading progress within articles
6. **Dark Mode**: Enhanced dark theme support
7. **Widgets**: Home screen widgets for quick access

## Testing

To test the RSS integration:

1. **Build the project**: `./gradlew assembleDebug`
2. **Install on device/emulator**
3. **Navigate to RSS tab**
4. **Add sample feeds** from the demo data
5. **Test all functionality**: search, filter, favorites, etc.

## Troubleshooting

### Common Issues
- **Build Errors**: Ensure all dependencies are properly synced
- **Network Issues**: Check internet connection for RSS parsing
- **Database Errors**: Clear app data if migration issues occur

### Debug Tips
- Enable logging in `RssRepository` for feed parsing issues
- Use Android Studio's Database Inspector to verify data storage
- Check Logcat for detailed error messages

This RSS integration transforms Linkzary from a simple bookmark manager into a comprehensive content aggregation platform, allowing users to stay updated with their favorite sources while maintaining the app's clean, modern design aesthetic.