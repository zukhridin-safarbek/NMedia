package ru.netology.nmedia.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import ru.netology.nmedia.viewmodel.PostViewModel
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val postVM: PostViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addBinding()
        postControl()
    }

    private fun postControl(){
        val adapter = PostsAdapter({
            postVM.shareByID(it.id)
        },{
            postVM.likeById(it.id)
        })
        binding.list.adapter = adapter
        postVM.data.observe(this){post ->
            adapter.submitList(post)
        }
    }

    private fun addBinding() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
