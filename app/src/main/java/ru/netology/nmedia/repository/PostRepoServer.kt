package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post
import java.lang.Exception

interface PostRepoServer {
    fun getAllFromServerAsync(callback: Callback<List<Post>>)
    fun likeByIdAsync(id: Long, callback: Callback<Post>)
    fun saveAsync(post: Post, callback: Callback<Post>)
    fun removeByIdAsync(id: Long, callback: Callback<Unit>)
    interface Callback<T>{
        fun onSuccess(posts: T){}
        fun error(e: Exception){}
    }

}