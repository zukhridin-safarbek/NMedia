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
    fun likeByIdAsync(id: Long, callback: Callback<Post>)
    fun shareById(id: Long)
    fun saveAsync(post: Post, callback: Callback<Post>)
    fun deleteAsync(id: Long, callback: Callback<Unit>)
    fun getAllFromServerAsync(callback: Callback<List<Post>>)
}
interface Callback<T>{
    fun onSuccess(value: T){}
    fun error(e: Exception){}
}