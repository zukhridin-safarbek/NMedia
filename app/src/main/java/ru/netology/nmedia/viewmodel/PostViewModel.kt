package ru.netology.nmedia.viewmodel


import android.net.Uri
import androidx.lifecycle.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.database.AppAuth
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModel
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.repository.PostRepository
import java.io.File
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
        repository.reSendPostToServer(post)
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
        edited.value?.let { post ->

            photo.value?.let {
                println("File ${it.file} : Uri ${it.uri}")
                if (it.uri != null || it.file != null) {
                    repository.saveWithAttachment(post.copy(author = url.toString(), content = content?.trim().toString(),
                        authorId = appAuth.authStateFlow.value?.id ?: 0L,
                        authorAvatar = appAuth.authStateFlow.value?.avatar
                            ?: "avatar is null"), it)
                    savePhoto(null, null)
                } else {
                    repository.saveAsync(post.copy(author = url.toString(),authorId = appAuth.authStateFlow.value?.id ?: 0L,
                        authorAvatar = appAuth.authStateFlow.value?.avatar, content = content?.trim().toString()))
                }

            } ?: repository.saveAsync(post.copy(author = url.toString(),authorId = appAuth.authStateFlow.value?.id ?: 0L,
                authorAvatar = appAuth.authStateFlow.value?.avatar, content = content?.trim().toString()))
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
        println(id)
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

