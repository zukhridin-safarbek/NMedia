package ru.netology.nmedia

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData

interface PostRepository {
    fun get(): LiveData<Post>
    fun like()
    fun share()
}

class PostRepositoryInMemoryImpl : PostRepository{
    private var post = Post(
        id = 1,
        author = "Нетология. Университет интернет-профессий будущего",
        content = "Привет, этоо новая Нетология! Когда-то Нетология начиналась",
        publishedDate = "21 may at 12 pm",
        likedByMe = false,
        likes = 678,
        shares = 124
    )

    private val data = MutableLiveData(post)
    override fun get(): LiveData<Post>  = data

    override fun like() {
        var like = post.likes
        if (!post.likedByMe){
            like++
        }else {
            like--
        }
        post = post.copy(likedByMe = !post.likedByMe, likes = like)
        data.value = post
    }

    override fun share() {
        var share = post.shares
        share++
        post = post.copy(shares = share)
        data.value = post
    }

}