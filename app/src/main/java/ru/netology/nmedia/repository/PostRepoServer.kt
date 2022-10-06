package ru.netology.nmedia.repository

import ru.netology.nmedia.dto.Post

interface PostRepoServer {
    fun getAllFromServer(): List<Post>
}