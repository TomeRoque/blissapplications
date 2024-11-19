package com.blissapplications.tomeroque.objects

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "emojis")
data class Emoji(
    @PrimaryKey val name: String,
    val url: String
)