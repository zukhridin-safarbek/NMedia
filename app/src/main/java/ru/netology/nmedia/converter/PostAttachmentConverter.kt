package ru.netology.nmedia.converter

import androidx.room.ColumnInfo
import androidx.room.TypeConverter
import ru.netology.nmedia.dto.PostAttachment
import ru.netology.nmedia.dto.PostAttachmentTypeEnum

data class PostAttachmentConverter(
    var url: String,
) {
    companion object {

    }
}