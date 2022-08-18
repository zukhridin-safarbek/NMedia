package ru.netology.nmedia.viewmodel


import android.app.Application
import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.netology.nmedia.`interface`.PostDao
import ru.netology.nmedia.`object`.PostColumns
import ru.netology.nmedia.data.Post
import ru.netology.nmedia.database.AppDb
import ru.netology.nmedia.database.NMediaDBHelper
import ru.netology.nmedia.database.NMediaDBManager
import ru.netology.nmedia.database.PostDaoImpl
import ru.netology.nmedia.repository.*


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
    private val repository: PostRepository = PostRepositorySQLiteImpl(
        AppDb.getInstance(application).postDao
    )
    val data = repository.getAll()
    private val edited = MutableLiveData(empty)
    fun edit(post: Post){
        edited.value = post
    }
    fun changeContentAndSave(content: String?, url: String?){
            val text = content?.trim()
            val urlText = url?.trim()
            if (edited.value?.content == text && edited.value?.videoLink == urlText){
                return
            }
        edited.value?.let {
            repository.save(it.copy(content = text.toString(), videoLink = urlText))
        }
        edited.value = empty
    }
    fun likeById(id: Long) = repository.likeById(id)
    fun shareByID(id: Long) = repository.shareById(id)
    fun removeById(id: Long) = repository.removeById(id)

}

