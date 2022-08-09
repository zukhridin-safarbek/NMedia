package ru.netology.nmedia.data

import android.net.Uri

data class Post(
    val id: Long,
    val author: String,
    val content: String,
    val publishedDate: String,
    val likedByMe: Boolean = false,
    val likes: Long = 0,
    val shares: Long = 0,
    val videoLink: String? = null
)