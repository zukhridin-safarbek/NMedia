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
import java.lang.Exception
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
        _data.postValue(FeedModel(loading = true))
        serverRepository.getAllFromServerAsync(object : PostRepoServer.Callback<List<Post>> {
            override fun onSuccess(posts: List<Post>) {
                _data.postValue(FeedModel(posts = posts, empty = posts.isEmpty()))
            }

            override fun error(e: Exception) {
                _data.postValue(FeedModel(error = true))
            }
        })
    }


    fun changeContentAndSave(content: String?, url: String?) {
             val text = content?.trim()
                val urlText = url?.trim()
                if (edited.value?.content == text && edited.value?.videoLink == urlText) {
                    return
                }
                edited.value?.let {
                     serverRepository.saveAsync(it.copy(content = text.toString(), videoLink = urlText),
                        object : PostRepoServer.Callback<Post> {
                            override fun onSuccess(posts: Post) {
                                super.onSuccess(posts)
                            }
                            override fun error(e: Exception) {
                                super.error(e)
                            }
                        })
                }
                edited.postValue(empty)
                getData()
    }

    fun likeById(id: Long) =
        serverRepository.likeByIdAsync(id, object : PostRepoServer.Callback<Post> {
            override fun onSuccess(posts: Post) {
                super.onSuccess(posts)
                getData()
            }

            override fun error(e: Exception) {
                super.error(e)
            }
        })



    fun shareByID(id: Long) {
        TODO()
    }

    fun removeById(id: Long) =
        serverRepository.removeByIdAsync(id, object : PostRepoServer.Callback<Unit> {
            override fun onSuccess(posts: Unit) {
                super.onSuccess(posts)
                getData()
            }

            override fun error(e: Exception) {
                super.error(e)
            }
        })


    fun refresh() {
            getData()
    }

}

