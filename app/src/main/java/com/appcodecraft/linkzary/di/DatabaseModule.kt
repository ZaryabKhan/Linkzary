package com.appcodecraft.linkzary.di

import android.content.Context
import androidx.room.Room
import com.appcodecraft.linkzary.data.dao.CollectionDao
import com.appcodecraft.linkzary.data.dao.SavedLinkDao
import com.appcodecraft.linkzary.data.database.LinkzaryDatabase
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
        ).build()
    }

    @Provides
    fun provideSavedLinkDao(database: LinkzaryDatabase): SavedLinkDao {
        return database.savedLinkDao()
    }

    @Provides
    fun provideCollectionDao(database: LinkzaryDatabase): CollectionDao {
        return database.collectionDao()
    }
}