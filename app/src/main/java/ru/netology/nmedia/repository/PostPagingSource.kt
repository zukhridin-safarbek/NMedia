package ru.netology.nmedia.repository

import androidx.paging.*
import androidx.room.withTransaction
import retrofit2.HttpException
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.dao.PostRemoteKeyDao
import ru.netology.nmedia.database.AppDb
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.entity.PostRemoteKeyEntity
import ru.netology.nmedia.service.ApiService
import java.io.IOException
import java.lang.Exception
class PostPagingSource(
    private val apiService: ApiService,
) : PagingSource<Long, Post>() {
    override fun getRefreshKey(state: PagingState<Long, Post>): Long? = null
    override suspend fun load(params: LoadParams<Long>): LoadResult<Long, Post> {
        try{
            val result = when (params) {
                is LoadParams.Refresh -> apiService.getLatest(params.loadSize)
                is LoadParams.Append -> apiService.getBefore(id = params.key,
                    count = params.loadSize)
                is LoadParams.Prepend -> return LoadResult.Page(data = emptyList(),
                    nextKey = null,
                    prevKey = params.key)
            }

            if (!result.isSuccessful) {
                throw HttpException(result)
            }
            val data = result.body().orEmpty()
            return LoadResult.Page(data, prevKey = params.key, data.lastOrNull()?.id)
        }catch (e: IOException){
            return LoadResult.Error(e)
        }
    }
}