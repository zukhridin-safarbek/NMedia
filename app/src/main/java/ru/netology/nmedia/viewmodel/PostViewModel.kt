package ru.netology.nmedia.viewmodel


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.repository.Callback
import ru.netology.nmedia.repository.PostRepositoryImpl


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
    private val serverRepository = PostRepositoryImpl()
    private var listPosts = emptyList<Post>()
    val draftContent = MutableLiveData<String>()
    val draftVideoLink = MutableLiveData<String>()
    val responseStatusError = MutableLiveData<String>()
    val serverNoConnection = MutableLiveData<Boolean>()
    private val _data = MutableLiveData<FeedModel>()
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
        _data.value = FeedModel(loading = true)
        serverRepository.getAllFromServerAsync(object : Callback<List<Post>> {
            override fun onSuccess(value: List<Post>) {
                _data.value = FeedModel(posts = value, empty = value.isEmpty())
                listPosts = value

            }

            override fun error(e: Exception) {
                _data.value = FeedModel(error = true)
                responseStatusError.postValue(e.message)
                serverNoConnection.postValue(true)
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
            serverRepository.saveAsync(it.copy(content = text.toString(), authorAvatar = urlText),
                object : Callback<Post> {
                    override fun onSuccess(value: Post) {
                        listPosts = listPosts.map { post ->
                            if (post.id == value.id || value.id == 0L) value else post
                        }
                        println(listPosts)
                        _data.value = FeedModel(posts = listPosts)
                    }

                    override fun error(e: Exception) {
                        responseStatusError.postValue(e.message)
                    }
                })
        }
        edited.postValue(empty)
    }

    fun likeById(id: Long) {
        serverRepository.likeByIdAsync(id, object : Callback<Post> {
            override fun onSuccess(value: Post) {
                listPosts = listPosts.map { post ->
                    if (post.id == id) value else post
                }
                _data.value = FeedModel(posts = listPosts)
            }

            override fun error(e: Exception) {
                _data.value = FeedModel(error = true)
                responseStatusError.postValue(e.message)
            }
        })

    }


    fun shareByID(id: Long) {
        TODO()
    }

    fun removeById(id: Long) {
        serverRepository.deleteAsync(id, object : Callback<Unit> {
            override fun onSuccess(value: Unit) {
                println("onSuccess")
                getData()
            }

            override fun error(e: Exception) {
                responseStatusError.postValue(e.message)
            }
        })

    }


    fun refresh() {
        getData()
    }

}

