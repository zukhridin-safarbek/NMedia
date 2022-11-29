package ru.netology.nmedia.repository

import android.accounts.NetworkErrorException
import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.database.AppAuth
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.PostAttachment
import ru.netology.nmedia.dto.PostAttachmentTypeEnum
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.service.PostsApi
import java.io.IOException
import java.lang.Exception
import java.lang.RuntimeException

class PostRepositoryImpl(private val postDao: PostDao) : PostRepository {
    override val posts: Flow<List<PostEntity>> = postDao.getAll().flowOn(Dispatchers.Default)


    override suspend fun likeByIdAsync(id: Long) {
        postDao.likedById(id)
        PostsApi.retrofitService.likeById(id)
        BuildConfig.BASE_URL
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
            PostsApi.retrofitService.save(post)
        } catch (e: IOException) {
            postDao.save(post = PostEntity.fromDto(post))
            println(e.message)
        } catch (e: Exception) {
            throw UnknownError(e.message)
        }


    }

    override suspend fun saveWithAttachment(post: Post, photo: PhotoModel) {
        try {
            println("before media")
            val media = upload(photo)
            println("after media")
            PostsApi.retrofitService.save(post.copy(attachment = PostAttachment(url = media.id,
                type = PostAttachmentTypeEnum.IMAGE))).isSuccessful.also {
                postDao.save(PostEntity.fromDto(post.copy(attachment = PostAttachment(url = media.id,
                    description = null,
                    type = PostAttachmentTypeEnum.IMAGE),
                    authorAvatar = AppAuth.getInstance().authStateFlow.value?.avatar
                        ?: "Avatar null", isInServer = true)))
            }
            println("AppAuth.getInstance().authStateFlow.value?.avatar" + AppAuth.getInstance().authStateFlow.value?.avatar)
        } catch (e: IOException) {
            println("IoExceptiopn my")
            postDao.save(post = PostEntity.fromDto(post))
            println(e.message)
        } catch (e: Exception) {
            println(e.message)
            e.printStackTrace()

        }
    }

    private suspend fun upload(photo: PhotoModel): Media {
        println("before response")
        println("photo.file?.name: ${photo.file?.name}")
        val response = PostsApi.retrofitService.upload(MultipartBody.Part.createFormData("file",
            photo.file?.name,
            requireNotNull(photo.file?.asRequestBody())))
        println("after response")
        println(photo.file?.asRequestBody())
        println("res " + response.isSuccessful)
        if (!response.isSuccessful) {
            println("response is no success")
            println(response.message())
        }
        return requireNotNull(response.body())


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
            posts.map { postList ->
                postList.map { postIt ->
                    if (postIt.isInServer == false && post.id == postIt.id) {
                        PostsApi.retrofitService.save(post.copy(id = 0L))
                        postDao.insert(PostEntity.fromDto(post.copy(isInServer = true)))
                    }
                }
            }.collect()
        } catch (e: IOException) {
            println(e.message)
        } catch (e: Exception) {
            throw UnknownError(e.message)
        }
        println("after resend")

    }

    override fun getNewerCount(id: Long): Flow<Int> = flow {
        while (true) {
            try {
                delay(10_000L)
                val response = PostsApi.retrofitService.getNewer(id)
                if (!response.isSuccessful) {
                    throw Exception(response.message())
                }
                val body = response.body() ?: throw Exception(response.message())
                postDao.insert(body.map(PostEntity::fromDto).map {
                    it.copy(showed = false)
                })
                emit(body.size)
            } catch (e: CancellationException) {
                throw e
            } catch (e: IOException) {
                println(e.message)
            } catch (e: Exception) {
                throw UnknownError(e.message)
            }
        }
    }.flowOn(Dispatchers.Default)

    suspend fun newer() {
        postDao.changeNewerShowed()
    }
}