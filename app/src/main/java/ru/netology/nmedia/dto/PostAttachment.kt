package ru.netology.nmedia.dto

data class PostAttachment(
    val url: String,
    val description: String,
    val type: PostAttachmentTypeEnum,
)

enum class PostAttachmentTypeEnum {
    IMAGE
}
