package ru.netology.nmedia.dto

data class Post(
    val id: Long,
    val author: String,
    val authorId: Long,
    val content: String,
    val publishedDate: String? = null,
    val likedByMe: Boolean = false,
    val likes: Long = 0,
    val authorAvatar: String? = null,
    val shares: Long = 0,
    val videoLink: String? = null,
    val isInServer: Boolean? = false,
    val attachment: PostAttachment? = null,
    val ownedByMe: Boolean = false,
)