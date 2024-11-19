package com.blissapplications.tomeroque.utilities

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.blissapplications.tomeroque.objects.Avatar

@Dao
interface AvatarDao {

    @Query("SELECT * FROM avatars WHERE login = :login LIMIT 1")
    fun getAvatarByLogin(login: String): Avatar?

    @Insert
    fun insertAvatar(avatar: Avatar)

    @Insert
    fun insertAllAvatars(avatars: List<Avatar>)


    @Query("SELECT * FROM avatars")
    fun getAllAvatars(): List<Avatar>

    @Query("DELETE FROM avatars WHERE login = :login")
    fun deleteAvatar(login: String)

}
