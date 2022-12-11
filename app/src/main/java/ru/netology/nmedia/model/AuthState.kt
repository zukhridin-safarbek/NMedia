package ru.netology.nmedia.model

data class AuthState(
    val id: Long,
    val token: String,
    val avatar: String,
    val pushToken: String = "default null"
)
