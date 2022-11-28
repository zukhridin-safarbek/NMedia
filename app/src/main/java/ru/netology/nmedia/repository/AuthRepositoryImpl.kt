package ru.netology.nmedia.repository

import ru.netology.nmedia.database.AppAuth
import ru.netology.nmedia.service.PostsApi

interface AuthRepository {
    suspend fun signIn(login: String, password: String)
    suspend fun registerUser(login: String, password: String, name: String)
}

class AuthRepositoryImpl() : AuthRepository {
    override suspend fun signIn(login: String, password: String) {
        val response = PostsApi.retrofitService.updateUser(login, password)
         if (response.isSuccessful) {
            val id = response.body()?.id
            val token = response.body()?.token
            if (id != null && token != null) {
                AppAuth.getInstance().setAuth(id, token)
            }
        }
    }

    override suspend fun registerUser(login: String, password: String, name: String) {
        val response = PostsApi.retrofitService.registerUser(login, password, name)
        if (response.isSuccessful) {
            val id = response.body()?.id
            val token = response.body()?.token
            if (id != null && token != null) {
                AppAuth.getInstance().setAuth(id, token)
            }
        }
    }
}