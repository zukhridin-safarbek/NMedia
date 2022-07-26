package ru.netology.nmedia

import android.os.Bundle
import android.os.Handler
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import ru.netology.nmedia.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var handler: Handler
    private val postVM: PostViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addBinding()
        postControl()
    }

    private fun postControl(){
        postVM.data.observe(this){post ->
            with(binding){
                postHeaderTitle.text = post.author
                postHeaderPublishedDate.text = post.publishedDate
                postDescription.text = post.content
                likeIcon.setImageResource(
                    if (post.likedByMe) R.drawable.ic_like_svgrepo_com else R.drawable.ic_heart_svgrepo_com
                )
                postLikesCount.text = ChangeIntegerToShortForm.changeIntToShortFormWithChar(post.likes)
                postSharesCount.text = ChangeIntegerToShortForm.changeIntToShortFormWithChar(post.shares)
            }
        }
        binding.likeIcon.setOnClickListener {
            postVM.like()
        }

        binding.shareIcon.setOnClickListener {
            postVM.share()
        }
    }

    private fun addBinding() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
