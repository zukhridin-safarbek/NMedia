package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.model.PhotoModel

interface PostRepository {
    val data: Flow<PagingData<Post>>
    val posts: LiveData<List<Post>>
    suspend fun likeByIdAsync(id: Long)
    suspend fun dislikeByIdAsync(id: Long)
    suspend fun shareById(id: Long)
    suspend fun saveAsync(post: Post)
    suspend fun saveWithAttachment(post: Post, photo: PhotoModel)
    suspend fun deleteAsync(id: Long)
    suspend fun reSendPostToServer(post: Post)
    suspend fun newer()
    fun getNewerCount(id: Long):Flow<Int>
}