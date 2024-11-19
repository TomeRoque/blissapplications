package com.blissapplications.tomeroque.utilities

import android.util.Log
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.blissapplications.tomeroque.objects.Repo
import kotlinx.coroutines.flow.Flow

//Repos pagging code

class RepoPagingSource(private val repos: List<Repo>) : PagingSource<Int, Repo>() {
    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Repo> {
        val position = params.key ?: 0
        val pageSize = params.loadSize

        Log.e("PagingSource", "Loading page starting at position $position with size $pageSize")

        return try {
            val nextRepos = repos.drop(position).take(pageSize)
            LoadResult.Page(
                data = nextRepos,
                prevKey = if (position > 0) position - pageSize else null,
                nextKey = if (position + pageSize < repos.size) position + pageSize else null
            )
        } catch (e: Exception) {
            LoadResult.Error(e)
        }
    }

    override fun getRefreshKey(state: PagingState<Int, Repo>): Int? {
        return state.anchorPosition
    }
}