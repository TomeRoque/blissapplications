package com.blissapplications.tomeroque.di

import android.content.Context
import androidx.room.Room
import com.blissapplications.tomeroque.utilities.AppDatabase
import com.blissapplications.tomeroque.utilities.AvatarDao
import com.blissapplications.tomeroque.utilities.EmojiDao
import com.blissapplications.tomeroque.utilities.ReposDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class DBModule() {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "emoji_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideEmojiDao(appDatabase: AppDatabase): EmojiDao {
        return appDatabase.emojiDao()
    }

    @Provides
    @Singleton
    fun provideAvatarDao(appDatabase: AppDatabase): AvatarDao {
        return appDatabase.avatarDao()
    }

    @Provides
    @Singleton
    fun provideReposDao(appDatabase: AppDatabase): ReposDao {
        return appDatabase.reposDao()
    }
}
