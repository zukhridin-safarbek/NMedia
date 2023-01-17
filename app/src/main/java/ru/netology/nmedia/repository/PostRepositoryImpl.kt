package ru.netology.nmedia.repository

import android.os.Build
import androidx.annotation.RequiresApi
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
import ru.netology.nmedia.dto.*
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.service.ApiService
import java.io.IOException
import java.lang.Exception
import java.time.OffsetDateTime
import javax.inject.Inject
import kotlin.random.Random

class PostRepositoryImpl @Inject constructor(
    private val postDao: PostDao,
    private val retrofitService: ApiService,
    private val appAuth: AppAuth,
    private val postRemoteKeyDao: PostRemoteKeyDao,
    private val appDb: AppDb,
) : PostRepository {
    private val _posts = MutableLiveData<List<Post>>()
    override val posts: LiveData<List<Post>> = _posts

    @RequiresApi(Build.VERSION_CODES.O)
    @OptIn(ExperimentalPagingApi::class)
    override val data: Flow<PagingData<FeedItem>> =
        Pager(config = PagingConfig(pageSize = 10, enablePlaceholders = false),
            pagingSourceFactory = {
                postDao.getPagingSource()
            },
            remoteMediator = PostRemoteMediator(apiService = retrofitService, postDao)
        ).flow
            .map {
                it.map(PostEntity::toDto)
                    .insertSeparators { prev, _ ->
                        if (prev?.id?.rem(5) == 0L) {
                            Ad(prev.id, "figma.jpg")
                        } else {
                            null
                        }
                    }
                    .insertSeparators { prev, next ->
                        val now = OffsetDateTime.now().plusDays(1).toEpochSecond()
                        if (prev is Post && next is Post) {
                            when {
                                prev.published != "now" -> {
                                    val result = (now % prev.published?.toLong()!! / 60) / 60
                                    when {
                                        result in 0..24 -> Published(Random.nextLong(), "Сегодня")
                                        result in 24..48 -> Published(Random.nextLong(), "Вчера")
                                        result > 48 -> Published(Random.nextLong(), "На прошлой неделе")
                                        else -> {
                                            null
                                        }
                                    }
                                }
                                prev.published == "now" -> {
                                    Published(Random.nextLong(), "Сегодня")
                                }
                                else -> {
                                    null
                                }
                            }
                        } else {
                            null
                        }


                    }
            }


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
            data.map { feedList ->
                feedList.map { feedItem ->
                    if (feedItem is Post) {
                        if (feedItem.isInServer == false && post.id == feedItem.id) {
                            retrofitService.save(post.copy(id = 0L))
                            postDao.insert(PostEntity.fromDto(post.copy(isInServer = true)))
                        }
                    }

                }
            }.collect()
        } catch (e: IOException) {
            throw IOException(e.message)
        } catch (e: Exception) {
            throw UnknownError(e.message)
        }

    }

    override suspend fun getPostById(id: Long): PostEntity {
        try {
            return postDao.getPostById(id)
        } catch (e: Exception) {
            error(e)
        }

    }

    override fun getNewerCount(id: Long): Flow<Int> = flow {
        postDao.getAll().map { list ->
            _posts.postValue(list.map(PostEntity::toDto))
        }
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