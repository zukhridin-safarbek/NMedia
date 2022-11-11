package ru.netology.nmedia.repository

import android.accounts.NetworkErrorException
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.service.PostsApi
import java.io.IOException
import java.lang.Exception
import java.lang.RuntimeException

class PostRepositoryImpl(private val postDao: PostDao) : PostRepository {
    override val posts: LiveData<List<PostEntity>> = postDao.getAll().map {
        println(it)
        it
    }

    override suspend fun likeByIdAsync(id: Long) {
        postDao.likedById(id)
        PostsApi.retrofitService.likeById(id)
    }

    override suspend fun dislikeByIdAsync(id: Long) {
        postDao.likedById(id)
        PostsApi.retrofitService.dislikeById(id)
    }


    override suspend fun shareById(id: Long) {
        TODO()
    }


    override suspend fun saveAsync(post: Post) {
        try {
            PostsApi.retrofitService.save(post).isSuccessful.also {
                    postDao.save(PostEntity(
                        post.id,
                        post.author,
                        post.content,
                        publishedDate = post.publishedDate,
                        likedByMe = post.likedByMe,
                        likes = post.likes,
                        authorAvatar = post.authorAvatar,
                        shares = post.shares,
                        videoLink = post.videoLink,
                        isInServer = true,
                    ))
            }
        } catch (e: IOException) {
            postDao.save(post = PostEntity.fromDto(post))
            println(e.message)
        } catch (e: Exception) {
            throw UnknownError(e.message)
        }


    }

    override suspend fun deleteAsync(id: Long) {
        PostsApi.retrofitService.deleteById(id)
        postDao.removedById(id)
    }


    override suspend fun getAllFromServerAsync(): List<Post> {
        try {
            val response = PostsApi.retrofitService.getAllPosts()
            if (!response.isSuccessful) {
                throw RuntimeException(response.message())
            }
            return response.body()?.also { list ->
                postDao.deleteAll()
                val postFromDto = list.map(PostEntity::fromDto)
                postFromDto.map { postDao.insert(it.copy(isInServer = true)) }

            }
                ?: throw RuntimeException("Body is null")
        } catch (e: IOException) {
            throw NetworkErrorException(e.message)
        } catch (e: Exception) {
            throw UnknownError(e.message)
        }
    }

    override suspend fun reSendPostToServer(post: Post) {
        println("before resend")
        try {
            println("save")
            posts.value?.map { postIt ->
                if (postIt.isInServer == false && post.id == postIt.id) {
                    PostsApi.retrofitService.save(post.copy(id = 0L))
                    postDao.insert(PostEntity.fromDto(post.copy(isInServer = true)))
                }
            }
        } catch (e: IOException) {
            println(e.message)
        } catch (e: Exception) {
            throw UnknownError(e.message)
        }
        println("after resend")

    }
}