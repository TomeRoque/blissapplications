package com.blissapplications.tomeroque.utilities

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.blissapplications.tomeroque.objects.Emoji

@Dao
interface EmojiDao {

    @Insert
    fun insertAll(emojis: List<Emoji>)

    @Query("SELECT * FROM emojis")
    fun getAllEmojis(): List<Emoji>
}
