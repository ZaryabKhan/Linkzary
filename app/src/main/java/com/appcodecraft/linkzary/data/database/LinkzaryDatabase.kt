package com.appcodecraft.linkzary.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.appcodecraft.linkzary.data.converter.DateConverter
import com.appcodecraft.linkzary.data.dao.CollectionDao
import com.appcodecraft.linkzary.data.dao.RssFeedDao
import com.appcodecraft.linkzary.data.dao.SavedLinkDao
import com.appcodecraft.linkzary.data.entity.Collection
import com.appcodecraft.linkzary.data.entity.RssFeed
import com.appcodecraft.linkzary.data.entity.RssFeedItem
import com.appcodecraft.linkzary.data.entity.SavedLink

@Database(
    entities = [SavedLink::class, Collection::class, RssFeed::class, RssFeedItem::class],
    version = 6,
    exportSchema = true
)
@TypeConverters(DateConverter::class)
abstract class LinkzaryDatabase : RoomDatabase() {
    abstract fun savedLinkDao(): SavedLinkDao
    abstract fun collectionDao(): CollectionDao
    abstract fun rssFeedDao(): RssFeedDao

    companion object {
        const val DATABASE_NAME = "linkzary_database"
        
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE saved_links ADD COLUMN previewImageUrl TEXT DEFAULT NULL")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE saved_links ADD COLUMN textContent TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE saved_links ADD COLUMN isOfflineAvailable INTEGER NOT NULL DEFAULT 0")
            }
        }

        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE collections ADD COLUMN icon TEXT NOT NULL DEFAULT 'Folder'")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE saved_links ADD COLUMN readStatus TEXT NOT NULL DEFAULT 'UNREAD'")
            }
        }

        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add link health and reading progress columns
                db.execSQL("ALTER TABLE saved_links ADD COLUMN isAlive INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE saved_links ADD COLUMN lastChecked INTEGER DEFAULT NULL")
                db.execSQL("ALTER TABLE saved_links ADD COLUMN readingProgress REAL NOT NULL DEFAULT 0.0")

                // Create RSS feeds table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS rss_feeds (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        url TEXT NOT NULL,
                        title TEXT NOT NULL DEFAULT '',
                        siteUrl TEXT DEFAULT NULL,
                        description TEXT DEFAULT NULL,
                        addedDate INTEGER NOT NULL,
                        lastFetched INTEGER DEFAULT NULL,
                        isActive INTEGER NOT NULL DEFAULT 1
                    )
                """)

                // Create RSS feed items table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS rss_feed_items (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        feedId INTEGER NOT NULL,
                        title TEXT NOT NULL,
                        url TEXT NOT NULL,
                        description TEXT DEFAULT NULL,
                        publishedDate INTEGER DEFAULT NULL,
                        isRead INTEGER NOT NULL DEFAULT 0,
                        isSaved INTEGER NOT NULL DEFAULT 0,
                        fetchedDate INTEGER NOT NULL,
                        FOREIGN KEY (feedId) REFERENCES rss_feeds(id) ON DELETE CASCADE
                    )
                """)
            }
        }
    }
}