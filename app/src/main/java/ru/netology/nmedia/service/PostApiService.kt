package ru.netology.nmedia.service

import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.*
import ru.netology.nmedia.BuildConfig
import ru.netology.nmedia.database.AppAuth
import ru.netology.nmedia.dto.Media
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.AuthDto
import ru.netology.nmedia.dto.RegistrationWithPhoto
import java.util.concurrent.TimeUnit

private val logging = HttpLoggingInterceptor().apply {
    if (BuildConfig.DEBUG) {
        level = HttpLoggingInterceptor.Level.BODY
    }
}

private val client = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .addInterceptor(logging)
    .addInterceptor { chain ->
        val request =
            AppAuth.getInstance().authStateFlow.value?.token?.let {
                chain.request().newBuilder()
                    .addHeader("Authorization", it)
                    .build()
            } ?: chain.request()
        chain.proceed(request)
    }
    .build()

private val retrofit = Retrofit.Builder()
    .baseUrl(BuildConfig.BASE_URL)
    .client(client)
    .addConverterFactory(GsonConverterFactory.create())
    .build()

interface PostApiService {
    @GET("posts")
    suspend fun getAllPosts(): Response<List<Post>>

    @GET("posts/{id}/newer")
    suspend fun getNewer(@Path("id") id: Long): Response<List<Post>>

    @POST("posts")
    suspend fun save(@Body post: Post): Response<Post>

    @POST("posts/{id}/likes")
    suspend fun likeById(@Path("id") id: Long): Response<Post>

    @DELETE("posts/{id}/likes")
    suspend fun dislikeById(@Path("id") id: Long): Response<Post>

    @DELETE("posts/{id}")
    suspend fun deleteById(@Path("id") id: Long): Response<Unit>

    @Multipart
    @POST("media")
    suspend fun upload(@Part file: MultipartBody.Part): Response<Media>

    @FormUrlEncoded
    @POST("users/authentication")
    suspend fun updateUser(
        @Field("login") login: String,
        @Field("pass") pass: String,
    ): Response<AuthDto>

    @FormUrlEncoded
    @POST("users/registration")
    suspend fun registerUser(
        @Field("login") login: String,
        @Field("pass") pass: String,
        @Field("name") name: String,
    ): Response<AuthDto>

    @Multipart
    @POST("users/registration")
    suspend fun registerUserWithPhoto(
        @Part("login") login: RequestBody,
        @Part("pass") pass: RequestBody,
        @Part("name") name: RequestBody,
        @Part media: MultipartBody.Part
    ): Response<RegistrationWithPhoto>
}

object PostsApi {
    val retrofitService: PostApiService by lazy {
        retrofit.create()
    }
}