package ru.netology.nmedia.`interface`

import ru.netology.nmedia.data.Post

interface PostDao {
    fun getAll(): List<Post>
    fun save(post: Post): Post
    fun likedById(id: Long)
    fun removedById(id: Long)
    fun sharedById(id: Long)
}