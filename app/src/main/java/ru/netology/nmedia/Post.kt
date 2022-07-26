package ru.netology.nmedia

data class Post(
    val id: Long,
    val author: String,
    val content: String,
    val publishedDate: String,
    val likedByMe: Boolean = false,
    val likes: Long = 0,
    val shares: Long = 0
)