package ru.netology.nmedia.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import java.lang.Exception

interface PostRepository {
    val posts: LiveData<List<PostEntity>>
    suspend fun likeByIdAsync(id: Long)
    suspend fun dislikeByIdAsync(id: Long)
    suspend fun shareById(id: Long)
    suspend fun saveAsync(post: Post)
    suspend fun deleteAsync(id: Long)
    suspend fun getAllFromServerAsync(): List<Post>
    suspend fun reSendPostToServer(post: Post)
}