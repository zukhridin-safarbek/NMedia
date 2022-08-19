package ru.netology.nmedia.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.netology.nmedia.data.Post

interface PostRepository {

    fun getAll(): LiveData<List<Post>>
    fun likeById(id: Long)
    fun shareById(id: Long)
    fun removeById(id: Long)
    fun save(post: Post)
}

class PostRepositoryInMemoryImpl : PostRepository {
    private var nextId: Long = 1
    private var posts = listOf(
        Post(
            id = nextId++,
            author = "Нетология. Университет интернет-профессий будущего",
            content = "Знаний хватить на всех: на следующей неделе разбираемся с р Знаний хватить на всех: на следующей неделе разбираемся с рЗнаний хватить на всех: на следующей неделе разбираемся с р",
            publishedDate = "21 september at 8 pm",
            likedByMe = false,
            likes = 675688,
            shares = 1242342
        ),
        Post(
            id = nextId++,
            author = "Нетология. Университет интернет-профессий будущего",
            content = "Знаний хватить на всех: на следующей неделе разбираемся с р Нетология. Университет интернет-профессий будущего Нетология. Университет интернет-профессий будущего",
            publishedDate = "21 september at 8 pm",
            likedByMe = false,
            likes = 6756768,
            shares = 12436
        ),
        Post(
            id = nextId++,
            author = " Нетология. Университет интернет-профессий будущего",
            content = "Знаний хватить на всех: на следующей неделе разбираемся с р Нетология. Университет интернет-профессий будущего Нетология. Университет интернет-профессий будущего",
            publishedDate = "21 september at 8 pm",
            likedByMe = false,
            likes = 67528,
            shares = 124156
        ),
        Post(
            id = nextId++,
            author = "Нетология. Университет интернет-профессий будущего",
            content = "Знаний хватить на всех: на следующей неделе разбираемся с р Нетология. Университет интернет-профессий будущего Нетология. Университет интернет-профессий будущего",
            publishedDate = "21 september at 8 pm",
            likedByMe = false,
            likes = 768,
            shares = 12421
        ),
        Post(
            id = nextId++,
            author = "Нетология. Университет интернет-профессий будущего",
            content = "Привет, этоо новая Нетология! Когда-то Нетология начиналась Нетология. Университет интернет-профессий будущего Нетология. Университет интернет-профессий будущего",
            publishedDate = "21 may at 12 pm",
            likedByMe = false,
            likes = 600768,
            shares = 12378
        )
    )


    private val data = MutableLiveData(posts)
    override fun getAll(): LiveData<List<Post>> = data

    override fun likeById(id: Long) {
        var like: Long
        posts = posts.map { post ->
            like = post.likes
            println(post.id)
            if (!post.likedByMe) {
                like++
            } else {
                like--
            }
            if (post.id != id) {
                post
            } else {
                post.copy(likedByMe = !post.likedByMe, likes = like)
            }
        }
        data.value = posts
    }

    override fun shareById(id: Long) {
        var share: Long
        posts = posts.map { post ->
            share = post.shares
            share++
            if (post.id != id) {
                post
            } else {
                post.copy(shares = share)
            }
        }
        data.value = posts
    }

    override fun removeById(id: Long) {
        posts = posts.filter { it.id != id }
        data.value = posts
    }

    override fun save(post: Post) {
        posts = if (post.id == 0L) {
            listOf(post.copy(id = nextId++, author = "Netology", publishedDate = "now")) + posts
        } else {
            posts.map {
                if (post.id != it.id) it else it.copy(content = post.content,
                    videoLink = post.videoLink)
            }
        }
        data.value = posts
    }


}

class PostRepositorySharedPrefsImpl(private val context: Context) : PostRepository {
    private val gson = Gson()
    private val prefs = context.getSharedPreferences("repo", Context.MODE_PRIVATE)
    private val type = TypeToken.getParameterized(List::class.java, Post::class.java).type
    private val key = "posts"
    private var nextId: Long = 1
    private var posts = emptyList<Post>()
    private val data = MutableLiveData(posts)


    init {
        prefs.getString(key, null)?.let {
            posts = gson.fromJson(it, type)
            data.value = posts
        }
    }


    override fun getAll(): LiveData<List<Post>> = data

    override fun likeById(id: Long) {
        posts = posts.map { post ->
            if (post.id != id) post else post.copy(
                likedByMe = !post.likedByMe,
                likes = if (post.likedByMe) post.likes - 1 else post.likes + 1
            )

        }
        data.value = posts
        sync()
    }

    override fun shareById(id: Long) {
        var share: Long
        posts = posts.map { post ->
            share = post.shares
            share++
            if (post.id != id) {
                post
            } else {
                post.copy(shares = share)
            }
        }
        data.value = posts
        sync()
    }

    override fun removeById(id: Long) {
        posts = posts.filter { it.id != id }
        data.value = posts
        sync()
    }

    override fun save(post: Post) {
        posts = if (post.id == 0L) {
            listOf(post.copy(id = nextId++, author = "Netology", publishedDate = "now")) + posts
        } else {
            posts.map {
                if (post.id != it.id) it else it.copy(content = post.content,
                    videoLink = post.videoLink)
            }
        }
        data.value = posts
        sync()
    }

    private fun sync() {
        with(prefs.edit()) {
            putString(key, gson.toJson(posts))
            apply()
        }
    }

}

class PostRepositoryFileImpl(private val context: Context) : PostRepository {
    private val gson = Gson()
    private val type = TypeToken.getParameterized(List::class.java, Post::class.java).type
    private val filename = "posts.json"
    private var nextId: Long = 1
    private var posts = emptyList<Post>()
    private val data = MutableLiveData(posts)


    init {
        val file = context.filesDir.resolve(filename)
        if (file.exists()) {
            context.openFileInput(filename).bufferedReader().use {
                posts = gson.fromJson(it, type)
                data.value = posts
            }
        } else sync()
    }


    override fun getAll(): LiveData<List<Post>> = data

    override fun likeById(id: Long) {
        posts = posts.map { post ->
            if (post.id != id) post else post.copy(
                likedByMe = !post.likedByMe,
                likes = if (post.likedByMe) post.likes - 1 else post.likes + 1
            )

        }
        data.value = posts
        sync()
    }

    override fun shareById(id: Long) {
        var share: Long
        posts = posts.map { post ->
            share = post.shares
            share++
            if (post.id != id) {
                post
            } else {
                post.copy(shares = share)
            }
        }
        data.value = posts
        sync()
    }

    override fun removeById(id: Long) {
        posts = posts.filter { it.id != id }
        data.value = posts
        sync()
    }

    override fun save(post: Post) {
        posts = if (post.id == 0L) {
            listOf(post.copy(id = nextId++, author = "Netology", publishedDate = "now")) + posts
        } else {
            posts.map {
                if (post.id != it.id) it else it.copy(content = post.content,
                    videoLink = post.videoLink)
            }
        }
        data.value = posts
        sync()
    }

    private fun sync() {
        context.openFileOutput(filename, Context.MODE_PRIVATE).bufferedWriter().use {
            it.write(gson.toJson(posts))
        }
    }

}