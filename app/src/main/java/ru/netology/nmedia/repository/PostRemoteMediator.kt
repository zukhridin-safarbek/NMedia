package ru.netology.nmedia.repository

import androidx.paging.*
import androidx.room.withTransaction
import retrofit2.HttpException
import retrofit2.Response
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.database.AppDb
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.PostRemoteKeyEntity
import ru.netology.nmedia.service.ApiService
import java.io.IOException
import java.lang.Exception

@OptIn(ExperimentalPagingApi::class)
class PostRemoteMediator(
    private val apiService: ApiService,
    private val postDao: PostDao,
) : RemoteMediator<Int, PostEntity>() {
    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, PostEntity>,
    ): MediatorResult {
        try {
            val result = when (loadType) {
                LoadType.REFRESH -> {
                    apiService.getLatest(state.config.initialLoadSize)
                }
                LoadType.PREPEND -> {
                    val id = state.firstItemOrNull()?.id ?: return MediatorResult.Success(false)
                    apiService.getAfter(id, state.config.initialLoadSize)
                }
                LoadType.APPEND -> {
                    val id = state.lastItemOrNull()?.id ?: return MediatorResult.Success(false)
                    apiService.getBefore(id, state.config.initialLoadSize)
                }
            }

            if (!result.isSuccessful) {
                throw HttpException(result)
            }
            val data = result.body().orEmpty()
            val nextKey = if (data.isEmpty()) null else data.last().id
            postDao.insert(data.map(PostEntity::fromDto))
            return MediatorResult.Success(data.isEmpty())
        } catch (e: IOException) {
            return MediatorResult.Error(e)
        }
    }
}