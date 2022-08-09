package ru.netology.nmedia.viewmodel


import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import ru.netology.nmedia.PostRepository
import ru.netology.nmedia.PostRepositoryInMemoryImpl
import ru.netology.nmedia.data.Post


private val empty = Post(
    id = 0,
    author = "Netology",
    content = "",
    publishedDate = "now",
    likedByMe = false,
    likes = 0,
    shares = 0
)
class PostViewModel : ViewModel() {
    private val repository: PostRepository = PostRepositoryInMemoryImpl()
    val data = repository.getAll()
    private val edited = MutableLiveData(empty)
    fun edit(post: Post){
        edited.value = post
    }
    fun changeContentAndSave(content: String?, url: String?){
            val text = content?.trim()
            val urlText = url?.trim()
            if (edited.value?.content == text && edited.value?.videoLink == urlText){
                return
            }
        edited.value?.let {
            repository.save(it.copy(content = text.toString(), videoLink = urlText))
        }
        edited.value = empty
    }
    fun likeById(id: Long) = repository.likeById(id)
    fun shareByID(id: Long) = repository.shareById(id)
    fun removeById(id: Long) = repository.removeById(id)

}

