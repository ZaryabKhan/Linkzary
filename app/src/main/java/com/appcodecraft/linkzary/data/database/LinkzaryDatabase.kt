package com.appcodecraft.linkzary.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.appcodecraft.linkzary.data.converter.DateConverter
import com.appcodecraft.linkzary.data.dao.CollectionDao
import com.appcodecraft.linkzary.data.dao.SavedLinkDao
import com.appcodecraft.linkzary.data.entity.Collection
import com.appcodecraft.linkzary.data.entity.SavedLink

@Database(
    entities = [SavedLink::class, Collection::class],
    version = 3,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class LinkzaryDatabase : RoomDatabase() {
    abstract fun savedLinkDao(): SavedLinkDao
    abstract fun collectionDao(): CollectionDao

    companion object {
        const val DATABASE_NAME = "linkzary_database"
        
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add previewImageUrl column to saved_links table
                db.execSQL("ALTER TABLE saved_links ADD COLUMN previewImageUrl TEXT DEFAULT NULL")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add textContent and isOfflineAvailable columns
                db.execSQL("ALTER TABLE saved_links ADD COLUMN textContent TEXT DEFAULT NULL")
                db.execSQL("ALTER TABLE saved_links ADD COLUMN isOfflineAvailable INTEGER NOT NULL DEFAULT 0")
            }
        }
    }
}