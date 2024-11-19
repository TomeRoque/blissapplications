package com.blissapplications.tomeroque.utilities

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.blissapplications.tomeroque.objects.Repo

@Dao
interface ReposDao {

    @Insert
    fun insertAll(repos: List<Repo>)

    @Query("SELECT * FROM repos")
    fun getAllRepos(): List<Repo>

}