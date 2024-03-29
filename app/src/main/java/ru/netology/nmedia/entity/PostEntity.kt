package ru.netology.nmedia.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.dto.PostAttachment

@Entity
data class PostEntity(
    @PrimaryKey(autoGenerate = true)
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
    val showed: Boolean = true,
    @Embedded
    val attachment: PostAttachment? = null,
) {
    fun toDto() = Post(id,
        author,
        authorId,
        content,
        publishedDate,
        likedByMe,
        likes,
        authorAvatar,
        shares,
        videoLink,
        isInServer,
        attachment)

    companion object {
        fun fromDto(post: Post) = PostEntity(post.id,
            post.author,
            post.authorId,
            post.content,
            post.publishedDate,
            post.likedByMe,
            post.likes,
            post.authorAvatar,
            post.shares,
            post.videoLink,
            post.isInServer,
            attachment = post.attachment)
    }
}
