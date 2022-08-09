package ru.netology.nmedia.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.activity.result.launch
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Group
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.viewmodel.PostViewModel
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.data.Post
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.databinding.ActivityNewOrEditPostBinding
import ru.netology.nmedia.databinding.CardPostLayoutBinding
import ru.netology.nmedia.util.AndroidUtils

class MainActivity : AppCompatActivity() {
    private val newPostContract =
        registerForActivityResult(NewOrEditPostActivity.ContractNewOrEditPost()) { result ->
            result ?: return@registerForActivityResult
            postVM.changeContentAndSave(result.first, result.second)
        }
    private lateinit var binding: ActivityMainBinding
    private lateinit var bindingCardPost: CardPostLayoutBinding
    private val postVM: PostViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addBinding()
        postControl()
    }


    private fun postControl() {
        val adapter = PostsAdapter(object : OnInteractionListener {
            override fun onLike(post: Post) {
                postVM.likeById(post.id)
            }


            override fun onShare(post: Post) {
                val intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, post.content)
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(intent, getString(R.string.share_post))
                startActivity(shareIntent)
                if (intent.action == Intent.ACTION_SEND) {
                    postVM.shareByID(post.id)
                }
            }

            override fun onEdit(post: Post) {
                postVM.edit(post)
                newPostContract.launch(Pair(post.content, post.videoLink))
            }

            override fun onRemove(post: Post) {
                postVM.removeById(post.id)
            }

            override fun playVideo(post: Post) {
                if (Uri.parse(post.videoLink).isAbsolute) {
                    startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(post.videoLink)))
                } else {
                    Snackbar.make(binding.root, "it is no link!", Snackbar.LENGTH_SHORT)
                        .setAction(R.string.edit_post) {
                            onEdit(post)
                        }
                        .show()
                }
            }

        })


        binding.list.adapter = adapter

        postVM.data.observe(this) { post ->
            val newPost = adapter.itemCount < post.size
            adapter.submitList(post) {
                if (newPost) {
                    binding.list.smoothScrollToPosition(0)
                }
            }
        }

        binding.addOrEditBtn.setOnClickListener {
            newPostContract.launch(Pair("", ""))
        }

    }

    private fun addBinding() {
        bindingCardPost = CardPostLayoutBinding.inflate(layoutInflater)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
