package ru.netology.nmedia.viewmodel


import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.database.AppDb
import ru.netology.nmedia.repository.*
import java.io.IOException
import kotlin.concurrent.thread


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

data class FeedModel(
    val posts: List<Post> = emptyList(),
    val loading: Boolean = false,
    val error: Boolean = false,
    val empty: Boolean = false,
)

class PostViewModel(application: Application) : AndroidViewModel(application) {
    private val repository: PostRepository = PostRepositoryRoomImpl(
        AppDb.getInstance(application).postDao()
    )
    private val serverRepository = PostRepositoryImpl()

    val draftContent = MutableLiveData<String>()
    val draftVideoLink = MutableLiveData<String>()
    val _data = MutableLiveData<FeedModel>()
    val data: LiveData<FeedModel>
        get() = _data
    private val edited = MutableLiveData(empty)
    fun edit(post: Post) {
        edited.value = post
    }

    init {
        getData()
    }

    private fun getData() {
        thread {
            _data.postValue(FeedModel(loading = true))
            try {
                val posts = serverRepository.getAllFromServer()
                _data.postValue(FeedModel(posts = posts, empty = posts.isEmpty()))
            } catch (e: IOException) {
                e.printStackTrace()
                FeedModel(error = true)
            }.also { _data::postValue }
        }
    }


    fun changeContentAndSave(content: String?, url: String?) {
        thread {
            val text = content?.trim()
            val urlText = url?.trim()
            if (edited.value?.content == text && edited.value?.videoLink == urlText) {
                return@thread
            }
            edited.value?.let {
                serverRepository.save(it.copy(content = text.toString(), videoLink = urlText))
            }
            edited.postValue(empty)
            getData()
        }
    }

    fun likeById(id: Long) = thread {
        serverRepository.likeById(id)
        getData()

    }

    fun shareByID(id: Long) {
        TODO()
    }

    fun removeById(id: Long) = thread {
        serverRepository.removeById(id)
        getData()
    }

    fun refresh() {
        thread {
            getData()
        }
    }

}

