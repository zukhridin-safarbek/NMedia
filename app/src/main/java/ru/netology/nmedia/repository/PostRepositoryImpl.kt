package ru.netology.nmedia.repository

import android.widget.Toast
import androidx.lifecycle.LiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import ru.netology.nmedia.dto.Post
import java.io.IOException
import java.lang.RuntimeException
import java.util.concurrent.TimeUnit

class PostRepositoryImpl : PostRepository, PostRepoServer {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()
    private val typeToken = object : TypeToken<List<Post>>() {}
    private var posts = emptyList<Post>()

    companion object {
        private const val BASE_URL = "http://10.0.2.2:9999"
        private val jsonType = "application/json".toMediaType()
    }

    override fun getAll(): LiveData<List<Post>> {
        TODO()
    }

    override fun likeById(id: Long) {
        posts.map { post ->
            if (post.id == id) {
                if (post.likedByMe) {
                    likeUpdater(post, post.id)
                } else {
                    likeUpdater(post, post.id)
                }
            }
        }

    }

    private fun likeUpdater(post: Post, id: Long) {
        val json = gson.toJson(post)
        val body: RequestBody = RequestBody.create(jsonType, json)
        val request: Request = if (post.likedByMe) {
            Request.Builder().url("${BASE_URL}/api/posts/$id/likes").delete(body).build()
        } else {
            Request.Builder().url("${BASE_URL}/api/posts/$id/likes").post(body).build()
        }
        client.newCall(request)
            .execute()
    }

    override fun likeByIdAsync(id: Long, callback: PostRepoServer.Callback<Post>) {
        posts.map { post ->
            if (post.id == id) {
                val json = gson.toJson(post)
                val body: RequestBody = RequestBody.create(jsonType, json)
                val request: Request = if (post.likedByMe) {
                    Request.Builder().url("${BASE_URL}/api/posts/$id/likes").delete(body).build()
                } else {
                    Request.Builder().url("${BASE_URL}/api/posts/$id/likes").post(body).build()
                }
                client.newCall(request)
                    .enqueue(object : Callback {
                        override fun onFailure(call: Call, e: IOException) {
                            callback.error(e)
                        }

                        override fun onResponse(call: Call, response: Response) {
                            callback.onSuccess(post)
                        }
                    })
            }
        }
    }


    override fun shareById(id: Long) {
        TODO()
    }

    override fun removeById(id: Long) {
        posts.map { post ->
            if (post.id != 0L) {
                val json = gson.toJson(post)
                val body: RequestBody = RequestBody.create(jsonType, json)
                val request: Request =
                    Request.Builder().url("${BASE_URL}/api/posts/$id").delete(body).build()
                client.newCall(request)
                    .execute()
            }
        }
    }

    override fun removeByIdAsync(id: Long, callback: PostRepoServer.Callback<Unit>) {
        posts.map {post ->
        if (post.id != 0L){
            val json = gson.toJson(post)
            val body: RequestBody = RequestBody.create(jsonType, json)
            val request: Request =
                Request.Builder().url("${BASE_URL}/api/posts/$id").delete(body).build()
            client.newCall(request)
                .enqueue(object : Callback {
                    override fun onFailure(call: Call, e: IOException) {
                        callback.error(e)
                    }

                    override fun onResponse(call: Call, response: Response) {
                        callback.onSuccess(Unit)
                    }
                })
        }
        }
    }

    override fun save(post: Post) {
        val json = gson.toJson(post)
        val body: RequestBody = RequestBody.create(jsonType, json)
        val request: Request = Request.Builder()
            .url("${BASE_URL}/api/posts")
            .post(body)
            .build()
        client.newCall(request)
            .execute()
    }

    override fun saveAsync(post: Post, callback: PostRepoServer.Callback<Post>) {
        val json = gson.toJson(post)
        val body: RequestBody = RequestBody.create(jsonType, json)
        val request: Request = Request.Builder()
            .url("${BASE_URL}/api/posts")
            .post(body)
            .build()
        client.newCall(request)
            .enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    callback.error(e)
                }

                override fun onResponse(call: Call, response: Response) {
                    callback.onSuccess(post)
                }
            })
    }


    override fun getAllFromServerAsync(callback: PostRepoServer.Callback<List<Post>>) {
        val request: Request = Request.Builder()
            .url("${BASE_URL}/api/posts")
            .build()
        return client.newCall(request)
            .enqueue(object : Callback {
                override fun onResponse(call: Call, response: Response) {
                    try {
                        val body =
                            response.body?.string() ?: throw RuntimeException("Body is null")
                        callback.onSuccess(gson.fromJson(body, typeToken.type))
                        posts = gson.fromJson(body, typeToken.type)
                    } catch (e: Exception) {
                        callback.error(e)
                    }
                }

                override fun onFailure(call: Call, e: IOException) {
                    callback.error(e)
                }
            })
    }
}