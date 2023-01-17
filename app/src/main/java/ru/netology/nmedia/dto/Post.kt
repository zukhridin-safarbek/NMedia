package ru.netology.nmedia.dto

sealed interface FeedItem {
    val id: Long
}
data class Post(
    override val id: Long,
    val author: String,
    val authorId: Long,
    val content: String,
    val published: String? = null,
    val likedByMe: Boolean = false,
    val likes: Long = 0,
    val authorAvatar: String? = null,
    val shares: Long = 0,
    val videoLink: String? = null,
    val isInServer: Boolean? = false,
    val attachment: PostAttachment? = null,
    val ownedByMe: Boolean = false,
): FeedItem

data class Ad(
    override val id: Long,
    val image: String
): FeedItem

data class Published(
    override val id: Long,
    val published: String
): FeedItem