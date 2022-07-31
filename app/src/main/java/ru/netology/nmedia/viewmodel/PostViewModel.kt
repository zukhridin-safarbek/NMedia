package ru.netology.nmedia.viewmodel


import android.view.View
import androidx.constraintlayout.widget.Group
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
    val edited = MutableLiveData(empty)
    fun changeContent(content: String, group: Group){
        edited.value?.let {
            val text = content.trim()
            if (it.content == text){
                return
            }
            edited.value = it.copy(content = text)
        }
        group.visibility = View.GONE
    }
    fun edit(post: Post){
        edited.value = post
    }
    fun save(){
        edited.value?.let {
            repository.save(it)
        }
        edited.value = empty
    }
    fun likeById(id: Long) = repository.likeById(id)
    fun shareByID(id: Long) = repository.shareById(id)
    fun removeById(id: Long) = repository.removeById(id)

}

