package ru.netology.nmedia.repository

import retrofit2.Call
import retrofit2.Response
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.service.PostsApi
import java.lang.Exception
import java.lang.RuntimeException

class PostRepositoryImpl : PostRepository {
    private var posts = emptyList<Post>()

    override fun likeByIdAsync(id: Long, callback: Callback<Post>) {
        var newPost = Post(0, "", "")
        posts = posts.map { post ->
            if (post.id != id) post else post.copy(
                likedByMe = !post.likedByMe,
                likes = if (post.likedByMe) {
                    PostsApi.retrofitService.dislikeById(id).enqueue(object : Callback<Post>,
                        retrofit2.Callback<Post> {
                        override fun onResponse(call: Call<Post>, response: Response<Post>) {
                            when (response.code().toString()[0].digitToInt()) {
                                4 -> {
                                    callback.error(RuntimeException("Ошибка 404, неправильный запрос или пост не найдено!"))
                                }
                                5 -> {
                                    callback.error(RuntimeException("Ошибка 505, ошибка сервера, попробуйте ешё раз!"))
                                }
                                2 -> {
                                    newPost = post.copy(likedByMe = !post.likedByMe,
                                        likes = post.likes - 1)
                                    callback.onSuccess(newPost)
                                }
                            }

                        }

                        override fun onFailure(call: Call<Post>, t: Throwable) {
                            callback.error(RuntimeException(t))
                        }

                    })
                    post.likes - 1
                } else {
                    PostsApi.retrofitService.likeById(id).enqueue(object : Callback<Post>,
                        retrofit2.Callback<Post> {
                        override fun onResponse(call: Call<Post>, response: Response<Post>) {
                            when (response.code().toString()[0].digitToInt()) {
                                4 -> {
                                    callback.error(RuntimeException("Ошибка 404, неправильный запрос или пост не найдено!"))
                                }
                                5 -> {
                                    callback.error(RuntimeException("Ошибка 505, ошибка сервера, попробуйте ешё раз!"))
                                }
                                2 -> {
                                    newPost = post.copy(likedByMe = !post.likedByMe,
                                        likes = post.likes + 1)
                                    callback.onSuccess(newPost)
                                }
                            }


                        }

                        override fun onFailure(call: Call<Post>, t: Throwable) {
                            callback.error(RuntimeException(t))
                        }

                    })
                    post.likes + 1
                }
            )
        }
    }


    override fun shareById(id: Long) {
        TODO()
    }


    override fun saveAsync(post: Post, callback: Callback<Post>) {
            PostsApi.retrofitService.save(post).enqueue(object : Callback<Post>,
                retrofit2.Callback<Post> {
                override fun onResponse(call: Call<Post>, response: Response<Post>) {
                    when (response.code().toString()[0].digitToInt()) {
                        4 -> {
                            callback.error(RuntimeException("Ошибка 404, неправильный запрос или пост не найдено!"))
                        }
                        5 -> {
                            callback.error(RuntimeException("Ошибка 505, ошибка сервера, попробуйте ешё раз!"))
                        }
                        2 -> {
                            val body = response.body() ?: throw RuntimeException("body is null")
                            posts.map {
                                val list = mutableListOf<Post>()
                                list.add(it)
                                list.add(body)
                                try {
                                    callback.onSuccess(body)
                                } catch (e: Exception) {
                                    callback.error(e)
                                }
                                posts = list
                            }
                        }
                    }

                }
                override fun onFailure(call: Call<Post>, t: Throwable) {
                    callback.error(RuntimeException(t))
                }

            })

    }

    override fun deleteAsync(id: Long, callback: Callback<Unit>) {
        PostsApi.retrofitService.deleteById(id).enqueue(object : Callback<Unit>,
            retrofit2.Callback<Unit> {
            override fun onResponse(call: Call<Unit>, response: Response<Unit>) {
                when (response.code().toString()[0].digitToInt()) {
                    4 -> {
                        callback.error(RuntimeException("Ошибка 404, неправильный запрос или пост не найдено!"))
                    }
                    5 -> {
                        callback.error(RuntimeException("Ошибка 505, ошибка сервера, попробуйте ешё раз!"))
                    }
                    2 -> {
                        callback.onSuccess(Unit)
                    }
                }
            }

            override fun onFailure(call: Call<Unit>, t: Throwable) {
                callback.error(RuntimeException(t))
            }

        })
    }


    override fun getAllFromServerAsync(callback: Callback<List<Post>>) {
        PostsApi.retrofitService.getAllPosts().enqueue(object : Callback<List<Post>>,
            retrofit2.Callback<List<Post>> {
            override fun onResponse(call: Call<List<Post>>, response: Response<List<Post>>) {
                println(response.code())
                when (response.code().toString()[0].digitToInt()) {
                    4 -> {
                        callback.error(RuntimeException("Ошибка 404, неправильный запрос или пост не найдено!"))
                    }
                    5 -> {
                        callback.error(RuntimeException("Ошибка 505, ошибка сервера, попробуйте ешё раз!"))
                    }
                    2 -> {
                        val body = response.body() ?: throw RuntimeException("body is null")
                        try {
                            posts = body
                            callback.onSuccess(body)
                        } catch (e: Exception) {
                            callback.error(e)
                        }
                    }
                }

            }

            override fun onFailure(call: Call<List<Post>>, t: Throwable) {
                callback.error(RuntimeException("Не удалось подключиться к серверу!"))
            }

        })
    }
}