package ru.netology.nmedia.viewmodel


import android.app.Application
import android.content.Context
import android.net.Uri
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map

import kotlinx.coroutines.launch
import ru.netology.nmedia.database.AppAuth
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.database.AppDb
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.repository.PostRepository
import ru.netology.nmedia.repository.PostRepositoryImpl
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
    private val appAuth: AppAuth
) : ViewModel() {
    private var listPosts = emptyList<Post>()
    val draftContent = MutableLiveData<String>()
    val draftVideoLink = MutableLiveData<String>()
    val serverNoConnection = MutableLiveData<Boolean>()
    val data: LiveData<FeedModel> = appAuth.authStateFlow.flatMapLatest {
        repository.posts.map { posts ->
            FeedModel(posts = posts.map { postEntity ->
                postEntity.toDto().copy(ownedByMe = postEntity.authorId == it?.id)

            }, posts.isEmpty())
        }
            .catch { e -> e.printStackTrace() }
    }.asLiveData(Dispatchers.Default)

    private val _state = MutableLiveData<FeedModelState>()
    val state: LiveData<FeedModelState>
        get() = _state
    private val edited = MutableLiveData(empty)
    val newerCount: LiveData<Int> = data.switchMap {
        repository.getNewerCount(it.posts.firstOrNull()?.id ?: 0L)
            .asLiveData(Dispatchers.Default, 1000L)
    }
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
        println("top resend viewModel")
        repository.reSendPostToServer(post)
        println("bottom resend viewModel")
    }


    private fun getData() = viewModelScope.launch {
        _state.value = FeedModelState.Loading
        try {
            repository.getAllFromServerAsync()
            listPosts = repository.getAllFromServerAsync()
            _state.value = FeedModelState.Idle
        } catch (e: Exception) {
            _state.value = FeedModelState.Error
        }
    }

    fun refresh() = viewModelScope.launch {
        _state.value = FeedModelState.Refreshing
        try {
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

