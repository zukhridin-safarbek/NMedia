package ru.netology.nmedia.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import ru.netology.nmedia.dto.Post

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long,
    val author: String,
    val content: String,
    val publishedDate: String? = null,
    val likedByMe: Boolean = false,
    val likes: Long = 0,
    val authorAvatar: String? = null,
    val shares: Long = 0,
    val videoLink: String? = null
){
    fun toDto() = Post(id, author, content, publishedDate, likedByMe, likes, authorAvatar, shares, videoLink)

    companion object{
        fun fromDto(post: Post) = PostEntity(post.id, post.author, post.content, post.publishedDate, post.likedByMe, post.likes, post.authorAvatar, post.shares, post.videoLink)
    }
}
