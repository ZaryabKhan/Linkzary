package com.appcodecraft.linkzary.data.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import android.content.Context
import com.appcodecraft.linkzary.data.converter.DateConverter
import com.appcodecraft.linkzary.data.dao.CollectionDao
import com.appcodecraft.linkzary.data.dao.SavedLinkDao
import com.appcodecraft.linkzary.data.entity.Collection
import com.appcodecraft.linkzary.data.entity.SavedLink

@Database(
    entities = [SavedLink::class, Collection::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverter::class)
abstract class LinkzaryDatabase : RoomDatabase() {
    abstract fun savedLinkDao(): SavedLinkDao
    abstract fun collectionDao(): CollectionDao

    companion object {
        const val DATABASE_NAME = "linkzary_database"
    }
}