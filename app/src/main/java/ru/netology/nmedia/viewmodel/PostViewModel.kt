package ru.netology.nmedia.viewmodel


import android.net.Uri
import androidx.lifecycle.*
import androidx.lifecycle.switchMap
import androidx.paging.PagingData
import androidx.paging.flatMap
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

import kotlinx.coroutines.launch
import ru.netology.nmedia.database.AppAuth
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.repository.PostRepository
import java.io.File
import java.lang.Exception
import javax.inject.Inject


private val empty = Post(
    id = 0,
    author = "",
    authorAvatar = "",
    authorId = 0L,
    content = "",
    publishedDate = "now",
    likedByMe = false,
    likes = 0,
    shares = 0,
    videoLink = null,
    ownedByMe = false
)

@ExperimentalCoroutinesApi
@HiltViewModel
class PostViewModel @Inject constructor(
    private val repository: PostRepository,
    private val appAuth: AppAuth,
) : ViewModel() {
    val draftContent = MutableLiveData<String>()
    val draftVideoLink = MutableLiveData<String>()

    val serverNoConnection = MutableLiveData<Boolean>()
    val data: Flow<PagingData<Post>> = appAuth.authStateFlow
        .flatMapLatest { myId ->
            repository.data.map { posts ->
                posts.map {
                    it.toDto().copy(ownedByMe = it.authorId == myId?.id)
                }
            }
        }.flowOn(Dispatchers.Default)
    private val _state = MutableLiveData<FeedModelState>()
    val state: LiveData<FeedModelState>
        get() = _state
    private val edited = MutableLiveData(empty)
    val newerCount: Flow<Int> = data.flatMapLatest {
        repository.getNewerCount(repository.dataForNewer().firstOrNull()?.id ?: 0L)
    }.flowOn(Dispatchers.Default)
    private val _photo = MutableLiveData<PhotoModel?>(null)
    val photo: LiveData<PhotoModel?>
        get() = _photo


    fun edit(post: Post) {
        edited.value = post
    }

    init {
        getData()
    }

    fun reSend(post: Post) = viewModelScope.launch {
        repository.reSendPostToServer(post)
    }


    private fun getData() = viewModelScope.launch {
        _state.value = FeedModelState.Loading
        try {
            repository.getAllFromServerAsync()
            _state.value = FeedModelState.Idle
        } catch (e: Exception) {
            _state.value = FeedModelState.Error
        }
    }

    fun refresh() = viewModelScope.launch {
        _state.value = FeedModelState.Refreshing
        try {
            newer()
            repository.getAllFromServerAsync()
            _state.value = FeedModelState.Idle
        } catch (e: Exception) {
            _state.value = FeedModelState.Error
        }
    }

    fun changeContentAndSave(content: String?, url: String?) = viewModelScope.launch {
        val text = content?.trim()
        val urlText = url?.trim()
        edited.value?.let { post ->
            photo.value?.let {
                repository.saveWithAttachment(post.copy(content = text.toString(),
                    authorId = appAuth.authStateFlow.value?.id ?: 0L,
                    authorAvatar = appAuth.authStateFlow.value?.avatar
                        ?: "avatar is null"), it)
                savePhoto(null, null)
            } ?: repository.saveAsync(post.copy(content = text.toString()))
        }
        edited.postValue(empty)
    }

    fun likeById(id: Long) = viewModelScope.launch {
        try {
            repository.likeByIdAsync(id)
        } catch (e: Exception) {
            _state.value = FeedModelState.Error
        }
    }

    fun dislikeById(id: Long) = viewModelScope.launch {
        try {
            repository.dislikeByIdAsync(id)
        } catch (e: Exception) {
            _state.value = FeedModelState.Error
        }
    }


    fun shareByID(id: Long) {
        TODO()
    }

    fun removeById(id: Long) = viewModelScope.launch {
        try {
            repository.deleteAsync(id)
        } catch (e: Exception) {
            _state.value = FeedModelState.Error
        }

    }

    fun savePhoto(uri: Uri?, file: File?) {
        _photo.value = PhotoModel(uri, file)
    }

    fun newer() = viewModelScope.launch {
        repository.newer()
    }

}

