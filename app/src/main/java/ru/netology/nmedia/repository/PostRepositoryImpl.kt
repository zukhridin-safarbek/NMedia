package ru.netology.nmedia.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.paging.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.database.AppAuth
import ru.netology.nmedia.database.AppDb
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.PostAttachment
import ru.netology.nmedia.dto.PostAttachmentTypeEnum
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.service.ApiService
import java.io.IOException
import java.lang.Exception
import javax.inject.Inject

class PostRepositoryImpl @Inject constructor(
    private val postDao: PostDao,
    private val retrofitService: ApiService,
    private val appAuth: AppAuth,
    private val postRemoteKeyDao: PostRemoteKeyDao,
    private val appDb: AppDb
) : PostRepository {
    private val _posts = MutableLiveData<List<Post>>()
    override val posts: LiveData<List<Post>> = _posts
//    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<Post>> =
        Pager(config = PagingConfig(pageSize = 10, enablePlaceholders = false),
            pagingSourceFactory = {
               PostPagingSource(apiService = retrofitService)
            },
//            remoteMediator = PostPagingSource(retrofitService, postDao, postRemoteKeyDao, appDb)
        ).flow
//            .map {
//            it.map(PostEntity::toDto)
//        }


    override suspend fun likeByIdAsync(id: Long) {
        postDao.likedById(id)
        retrofitService.likeById(id)
        BuildConfig.BASE_URL
    }


    override suspend fun dislikeByIdAsync(id: Long) {
        postDao.likedById(id)
        retrofitService.dislikeById(id)
    }


    override suspend fun shareById(id: Long) {
        TODO()
    }


    override suspend fun saveAsync(post: Post) {
        try {
            retrofitService.save(post)
        } catch (e: IOException) {
            postDao.save(post = PostEntity.fromDto(post))
        } catch (e: Exception) {
            throw UnknownError(e.message)
        }


    }

    override suspend fun saveWithAttachment(post: Post, photo: PhotoModel) {
            println("retrofitService")
        try {
            val media = upload(photo)
            println("retrofitService 2  ")
            retrofitService.save(post.copy(attachment = PostAttachment(url = media.id,
                type = PostAttachmentTypeEnum.IMAGE))).isSuccessful.also {
                println("also")
                postDao.save(PostEntity.fromDto(post.copy(attachment = PostAttachment(url = media.id,
                    description = null,
                    type = PostAttachmentTypeEnum.IMAGE),
                    authorAvatar = appAuth.authStateFlow.value?.avatar
                        ?: "Avatar null", isInServer = true)))
            }
        } catch (e: IOException) {
            postDao.save(post = PostEntity.fromDto(post))
        } catch (e: Exception) {
            e.printStackTrace()

        }
    }

    private suspend fun upload(photo: PhotoModel): Media {
        val response = retrofitService.upload(MultipartBody.Part.createFormData("file",
            photo.file?.name,
            requireNotNull(photo.file?.asRequestBody())))
        if (!response.isSuccessful) {
        }
        return requireNotNull(response.body())
    }

    override suspend fun deleteAsync(id: Long) {
        retrofitService.deleteById(id)
        postDao.removedById(id)
    }

    override suspend fun reSendPostToServer(post: Post) {
        try {
            data.map { postList ->
                postList.map { postIt ->
                    if (postIt.isInServer == false && post.id == postIt.id) {
                        retrofitService.save(post.copy(id = 0L))
                        postDao.insert(PostEntity.fromDto(post.copy(isInServer = true)))
                    }
                }
            }.collect()
        } catch (e: IOException) {
            throw IOException(e.message)
        } catch (e: Exception) {
            throw UnknownError(e.message)
        }

    }

    override fun getNewerCount(id: Long): Flow<Int> = flow {
        while (true) {
            try {
                delay(10_000L)
                val response = retrofitService.getNewer(id)
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
            } catch (e: Exception) {
                throw UnknownError(e.message)
            }
        }
    }.flowOn(Dispatchers.Default)

    override suspend fun newer() {
        postDao.changeNewerShowed()
    }
}