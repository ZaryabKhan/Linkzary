package com.appcodecraft.linkzary.di

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import com.appcodecraft.linkzary.data.dao.CollectionDao
import com.appcodecraft.linkzary.data.dao.RssFeedDao
import com.appcodecraft.linkzary.data.dao.SavedLinkDao
import com.appcodecraft.linkzary.data.database.LinkzaryDatabase
import com.appcodecraft.linkzary.data.service.ImportExportService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): LinkzaryDatabase {
        return Room.databaseBuilder(
            context,
            LinkzaryDatabase::class.java,
            LinkzaryDatabase.DATABASE_NAME
        )
                        .addMigrations(
                LinkzaryDatabase.MIGRATION_1_2,
                LinkzaryDatabase.MIGRATION_2_3,
                LinkzaryDatabase.MIGRATION_3_4,
                LinkzaryDatabase.MIGRATION_4_5,
                LinkzaryDatabase.MIGRATION_5_6
            )
            .setJournalMode(RoomDatabase.JournalMode.WRITE_AHEAD_LOGGING)
            .setQueryExecutor(java.util.concurrent.Executors.newFixedThreadPool(4))
            .enableMultiInstanceInvalidation()
            .build()
    }

    @Provides
    fun provideSavedLinkDao(database: LinkzaryDatabase): SavedLinkDao {
        return database.savedLinkDao()
    }

    @Provides
    fun provideCollectionDao(database: LinkzaryDatabase): CollectionDao {
        return database.collectionDao()
    }

    @Provides
    fun provideRssFeedDao(database: LinkzaryDatabase): RssFeedDao {
        return database.rssFeedDao()
    }

    @Provides
    @Singleton
    fun provideImportExportService(): ImportExportService {
        return ImportExportService()
    }
}