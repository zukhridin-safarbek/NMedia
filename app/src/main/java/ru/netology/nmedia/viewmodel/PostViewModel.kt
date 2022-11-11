package ru.netology.nmedia.viewmodel


import android.app.Application
import androidx.lifecycle.*

import kotlinx.coroutines.launch
import ru.netology.nmedia.FeedModel
import ru.netology.nmedia.FeedModelState
import ru.netology.nmedia.database.AppDb
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.repository.PostRepositoryImpl
import ru.netology.nmedia.service.PostsApi
import java.lang.Exception


private val empty = Post(
    id = 0,
    author = "Netology",
    content = "",
    publishedDate = "now",
    likedByMe = false,
    likes = 0,
    shares = 0,
    videoLink = null
)


class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val serverRepository = PostRepositoryImpl(AppDb.getInstance(application).postDao())
    private var listPosts = emptyList<Post>()
    val draftContent = MutableLiveData<String>()
    val draftVideoLink = MutableLiveData<String>()
    val postsNoInServe = serverRepository.postsNoInServer
    val serverNoConnection = MutableLiveData<Boolean>()
    val data: LiveData<FeedModel> = serverRepository.posts.map { FeedModel(it.map(PostEntity::toDto), it.isEmpty()) }
    val dataEntity: LiveData<List<PostEntity>> = serverRepository.posts
    private val _state = MutableLiveData<FeedModelState>()
    val state: LiveData<FeedModelState>
        get() = _state
    private val edited = MutableLiveData(empty)
    fun edit(post: Post) {
        edited.value = post
    }

    init {
        getData()
    }

     fun reSend(postEntity: PostEntity) = viewModelScope.launch{
         println("top resend viewModel")
         serverRepository.reSendPostToServer(postEntity)
         println("bottom resend viewModel")
    }


    private fun getData() = viewModelScope.launch {
        _state.value = FeedModelState.Loading
        try {
            serverRepository.getAllFromServerAsync()
            listPosts = serverRepository.getAllFromServerAsync()
            _state.value = FeedModelState.Idle
        } catch (e: Exception) {
            _state.value = FeedModelState.Error
        }
    }

    fun refresh() = viewModelScope.launch {
        _state.value = FeedModelState.Refreshing
        try {
            serverRepository.getAllFromServerAsync()
            _state.value = FeedModelState.Idle
        } catch (e: Exception) {
            _state.value = FeedModelState.Error
        }
    }

    fun changeContentAndSave(content: String?, url: String?) = viewModelScope.launch {
        val text = content?.trim()
        val urlText = url?.trim()
        edited.value?.let {
            serverRepository.saveAsync(it.copy(content = text.toString(), authorAvatar = urlText))
        }
        edited.postValue(empty)
    }

    fun likeById(id: Long) = viewModelScope.launch {
        try {
            serverRepository.likeByIdAsync(id)
        } catch (e: Exception) {
            _state.value = FeedModelState.Error
        }
    }

    fun dislikeById(id: Long) = viewModelScope.launch {
        try {
            serverRepository.dislikeByIdAsync(id)
        } catch (e: Exception) {
            _state.value = FeedModelState.Error
        }
    }


    fun shareByID(id: Long) {
        TODO()
    }

    fun removeById(id: Long) = viewModelScope.launch {
        try {
            serverRepository.deleteAsync(id)
        }catch (e: Exception){
            _state.value = FeedModelState.Error
        }

    }


}

