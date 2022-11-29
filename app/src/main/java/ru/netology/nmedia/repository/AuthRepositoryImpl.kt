package ru.netology.nmedia.repository

import kotlinx.coroutines.flow.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import ru.netology.nmedia.database.AppAuth
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.model.ResponseModel
import ru.netology.nmedia.service.PostsApi
import kotlin.math.log

interface AuthRepository {
    suspend fun signIn(login: String, password: String)
    suspend fun registerUser(login: String, password: String, name: String)
    suspend fun registerUserWithPhoto(
        login: String,
        password: String,
        name: String,
        photo: PhotoModel,
    )

    val responseCode: SharedFlow<ResponseModel>
}

class AuthRepositoryImpl() : AuthRepository {
    private val _responseCode = MutableSharedFlow<ResponseModel>()
    override var responseCode: SharedFlow<ResponseModel> = _responseCode.asSharedFlow()
    override suspend fun signIn(login: String, password: String) {
        try {
            val response = PostsApi.retrofitService.updateUser(login, password)
            if (response.isSuccessful) {
                val id = response.body()?.id
                val token = response.body()?.token
                if (id != null && token != null) {
                    AppAuth.getInstance().setAuth(id, token, AppAuth.getInstance().authStateFlow.value?.avatar ?: "avatar is null")
                }
            }
            _responseCode.emit(ResponseModel(response.code(), response.message() ?: "null"))
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun registerUser(login: String, password: String, name: String) {
        try {
            val response = PostsApi.retrofitService.registerUser(login, password, name)
            if (response.isSuccessful) {
                val id = response.body()?.id
                val token = response.body()?.token
                if (id != null && token != null) {
                    AppAuth.getInstance().setAuth(id, token, "null")
                }
            }
            _responseCode.emit(ResponseModel(response.code(), response.message() ?: "null"))
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    override suspend fun registerUserWithPhoto(
        login: String,
        password: String,
        name: String,
        photo: PhotoModel,
    ) {
        println("photo.file?.name" + photo.file?.name)

        try {
            val response =
                PostsApi.retrofitService.registerUserWithPhoto(login.toRequestBody("text/plain".toMediaType()),
                    password.toRequestBody("text/plain".toMediaType()),
                    name.toRequestBody("text/plain".toMediaType()),
                    MultipartBody.Part.createFormData("file",
                        photo.file?.name,
                        requireNotNull(photo.file?.asRequestBody())))
            if (response.isSuccessful) {
                val id = response.body()?.id
                val token = response.body()?.token
                if (id != null && token != null) {
                    AppAuth.getInstance().setAuth(id, token, photo.uri.toString())
                }
            }
            _responseCode.emit(ResponseModel(response.code(), response.message() ?: "null"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

}