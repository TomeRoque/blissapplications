package com.blissapplications.tomeroque.utilities

import android.content.Context
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.PagingSource
import androidx.paging.PagingState
import androidx.paging.cachedIn
import com.blissapplications.tomeroque.objects.Avatar
import com.blissapplications.tomeroque.objects.Emoji
import com.blissapplications.tomeroque.objects.Repo
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

@HiltViewModel
class ViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val emojiDao: EmojiDao,
    private val avatarDao: AvatarDao,
    private val reposDao: ReposDao,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _emojis = mutableStateOf<List<Emoji>>(emptyList())
    val emojis: State<List<Emoji>> get() = _emojis

    private val _avatars = MutableStateFlow<List<Avatar>>(emptyList())
    val avatars: StateFlow<List<Avatar>> get() = _avatars

    fun updateEmojis(newEmojis: List<Emoji>) {
        _emojis.value = newEmojis // Updating this will trigger recompositions
    }

    fun saveEmojis(emojis: List<Emoji>) {
        viewModelScope.launch(Dispatchers.IO) {
            emojiDao.insertAll(emojis)
            updateEmojis(emojis)
        }
    }

    suspend fun getAllEmojis(): List<Emoji> {
        return withContext(Dispatchers.IO) {
            emojiDao.getAllEmojis()
        }
    }

    fun saveAvatar(avatar: Avatar){
        viewModelScope.launch(Dispatchers.IO) {
            avatarDao.insertAvatar(avatar)
        }
    }

    suspend fun getAllAvatars(): List<Avatar> {
        return withContext(Dispatchers.IO) {
            avatarDao.getAllAvatars()
        }
    }

    suspend fun checkAvatarExists(login: String): Boolean {
        return withContext(Dispatchers.IO) {
            val avatar = avatarDao.getAvatarByLogin(login)
            avatar != null
        }
    }

    fun deleteAvatar(login: String) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                avatarDao.deleteAvatar(login)
                Log.d("AvatarDelete", "Avatar with login $login deleted successfully.")
            } catch (e: Exception) {
                Log.e("AvatarDeleteError", "Error deleting avatar", e)
            }
        }
    }

    fun saveRepos(repos: List<Repo>) {
        viewModelScope.launch(Dispatchers.IO) {
            reposDao.insertAll(repos)
        }
    }

    suspend fun getAllRepos(): List<Repo> {
        return withContext(Dispatchers.IO) {
            reposDao.getAllRepos()
        }
    }

    private val _repoList = MutableStateFlow<List<Repo>>(emptyList())
    val repoList: StateFlow<List<Repo>> = _repoList

    fun setRepos(repos: List<Repo>) {
        _repoList.value = repos
    }

    fun getRepoFlow(): Flow<PagingData<Repo>> {
        return _repoList.flatMapLatest { repos ->
            Pager(
                config = PagingConfig(
                    pageSize = 10,
                    enablePlaceholders = false
                )
            ) {
                RepoPagingSource(repos)
            }.flow
        }.cachedIn(viewModelScope)
    }
}
