package com.blissapplications.tomeroque.utilities

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.blissapplications.tomeroque.objects.Avatar
import com.blissapplications.tomeroque.objects.Emoji
import com.blissapplications.tomeroque.objects.Repo

@Database(entities = [Emoji::class, Avatar::class, Repo::class], version = 3)
abstract class AppDatabase : RoomDatabase() {

    abstract fun emojiDao(): EmojiDao
    abstract fun avatarDao(): AvatarDao
    abstract fun reposDao(): ReposDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "emoji_database"
                )
                    .fallbackToDestructiveMigration()  // Ensure this is called
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}