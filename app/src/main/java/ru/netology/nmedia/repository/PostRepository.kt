package ru.netology.nmedia

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import ru.netology.nmedia.data.Post

interface PostRepository {
    fun getAll(): LiveData<List<Post>>
    fun likeById(id: Long)
    fun shareById(id: Long)
}

class PostRepositoryInMemoryImpl : PostRepository{
    private var posts = listOf(
        Post(
            id = 5,
            author = "Нетология. Университет интернет-профессий будущего",
            content = "Знаний хватить на всех: на следующей неделе разбираемся с р Знаний хватить на всех: на следующей неделе разбираемся с рЗнаний хватить на всех: на следующей неделе разбираемся с р",
            publishedDate = "21 september at 8 pm",
            likedByMe = false,
            likes = 675688,
            shares = 1242342
        ),
        Post(
            id = 4,
            author = "Нетология. Университет интернет-профессий будущего",
            content = "Знаний хватить на всех: на следующей неделе разбираемся с р Нетология. Университет интернет-профессий будущего Нетология. Университет интернет-профессий будущего",
            publishedDate = "21 september at 8 pm",
            likedByMe = false,
            likes = 6756768,
            shares = 12436
        ),
        Post(
            id = 3,
            author = "Нетология. Университет интернет-профессий будущего",
            content = "Знаний хватить на всех: на следующей неделе разбираемся с р Нетология. Университет интернет-профессий будущего Нетология. Университет интернет-профессий будущего",
            publishedDate = "21 september at 8 pm",
            likedByMe = false,
            likes = 67528,
            shares = 124156
        ),
        Post(
            id = 2,
            author = "Нетология. Университет интернет-профессий будущего",
            content = "Знаний хватить на всех: на следующей неделе разбираемся с р Нетология. Университет интернет-профессий будущего Нетология. Университет интернет-профессий будущего",
            publishedDate = "21 september at 8 pm",
            likedByMe = false,
            likes = 768,
            shares = 12421
        ),
        Post(
            id = 1,
            author = "Нетология. Университет интернет-профессий будущего",
            content = "Привет, этоо новая Нетология! Когда-то Нетология начиналась Нетология. Университет интернет-профессий будущего Нетология. Университет интернет-профессий будущего",
            publishedDate = "21 may at 12 pm",
            likedByMe = false,
            likes = 600768,
            shares = 12378
        )
    )

    private val data = MutableLiveData(posts)
    override fun getAll(): LiveData<List<Post>>  = data

    override fun likeById(id: Long) {
        var like: Long
        posts = posts.map { post ->
            like = post.likes
            if (!post.likedByMe){
                like++
            }else {
                like--
            }
            if (post.id != id){
                post
            }else{
                post.copy(likedByMe = !post.likedByMe, likes = like)
            }
        }
        data.value = posts
    }

    override fun shareById(id: Long) {
        var share: Long
        posts = posts.map { post ->
            share = post.shares
            share++
            if (post.id != id){
                post
            }else{
                post.copy(shares = share)
            }
        }
        data.value = posts
    }

}