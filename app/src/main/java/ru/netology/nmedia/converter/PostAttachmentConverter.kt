package ru.netology.nmedia.converter

import androidx.room.TypeConverter
import ru.netology.nmedia.dto.PostAttachment
import ru.netology.nmedia.dto.PostAttachmentTypeEnum

class PostAttachmentConverter {
    companion object {
        @TypeConverter
        fun fromAttachment(attachment: PostAttachment): String {
            val url = attachment.url
            val description = attachment.description
            val type = attachment.type
            return "$url,$description,$type"
        }

        @TypeConverter
        fun fromAttachmentToList(attachment: PostAttachment?): List<String>? {
            var list: List<String>? = null
            list = listOf(attachment?.url.toString(),
                attachment?.description.toString(),
                attachment?.type.toString())
            return list
        }

        @TypeConverter
        fun toAttachment(data: List<String>?): PostAttachment {
            val url = data?.get(0).orEmpty()
            val description = data?.get(1).orEmpty()
            val type = PostAttachmentTypeEnum.valueOf(data?.get(2).orEmpty())
            return PostAttachment(url, description, type)
        }
    }
}