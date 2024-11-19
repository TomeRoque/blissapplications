package com.blissapplications.tomeroque.objects

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "repos")
data class Repo(
    @PrimaryKey val id: Int,
    @ColumnInfo(name = "full_name") val full_name: String,
    @ColumnInfo(name = "is_private") val isPrivate: String // Renamed `private` to `isPrivate`
)