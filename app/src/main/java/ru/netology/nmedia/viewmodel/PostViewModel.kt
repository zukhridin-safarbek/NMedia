package ru.netology.nmedia.viewmodel


import android.content.Context
import android.net.Uri
import androidx.lifecycle.*
import androidx.lifecycle.switchMap
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.flatMap
import androidx.paging.map
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
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
import ru.netology.nmedia.util.CheckNetworkConnection
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
    val noConnection = MutableLiveData<String?>(null)
    val posts: LiveData<List<Post>> = repository.posts
    val data: Flow<PagingData<Post>> = appAuth.authStateFlow.flatMapLatest { myId ->
        repository.data.map { posts ->
            posts.map {
                it.copy(ownedByMe = it.authorId == myId?.id)
            }
        }
    }.flowOn(Dispatchers.Default)
    private val edited = MutableLiveData(empty)
//    val newerCount: Flow<Int> = data.flatMapLatest {
//        repository.getNewerCount(repository.dataForNewer().firstOrNull()?.id ?: 0L)
//    }.flowOn(Dispatchers.Default)
    private val _photo = MutableLiveData<PhotoModel?>(null)
    val photo: LiveData<PhotoModel?>
        get() = _photo

    fun edit(post: Post) {
        edited.value = post
    }

    fun reSend(post: Post) = viewModelScope.launch {
        repository.reSendPostToServer(post)
    }

    fun changeContentAndSave(content: String?, url: String?) = viewModelScope.launch {
        val text = content?.trim()
        edited.value?.let { post ->
            photo.value?.let {
                println(it.file)
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
            LoadState.Error(e)
        }
    }

    fun dislikeById(id: Long) = viewModelScope.launch {
        try {
            repository.dislikeByIdAsync(id)
        } catch (e: Exception) {
            LoadState.Error(e)
        }
    }


    fun shareByID(id: Long) {
        TODO()
    }

    fun removeById(id: Long) = viewModelScope.launch {
        try {
            repository.deleteAsync(id)
        } catch (e: Exception) {
            LoadState.Error(e)
        }

    }

    fun savePhoto(uri: Uri?, file: File?) {
        _photo.value = PhotoModel(uri, file)
    }

    private fun newer() = viewModelScope.launch {
        repository.newer()
    }

}

