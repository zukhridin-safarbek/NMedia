package ru.netology.nmedia.`interface`

import ru.netology.nmedia.dto.Post

interface PushSender {
    fun pushSender(post: Post)
}