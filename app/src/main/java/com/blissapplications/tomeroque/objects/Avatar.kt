package com.blissapplications.tomeroque.objects

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "avatars")
data class Avatar(
    @PrimaryKey val login: String,
    val id: Int,
    val avatar_url: String
)