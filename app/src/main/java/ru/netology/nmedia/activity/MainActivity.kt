package ru.netology.nmedia.activity

import android.os.Bundle
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.Group
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import ru.netology.nmedia.R
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.viewmodel.PostViewModel
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.data.Post
import ru.netology.nmedia.databinding.ActivityMainBinding
import ru.netology.nmedia.util.AndroidUtils

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
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
                postVM.shareByID(post.id)
            }

            override fun onEdit(post: Post) {
                postVM.edit(post)
                postVM.edited.observe(this@MainActivity) {
                    if (it.id != 0L) {
                        binding.editedModeGroup.visibility = View.VISIBLE
                        binding.editedTextView.text = it.content
                        binding.content.requestFocus()
                        binding.content.setText(it.content)
                    }
                }
            }

            override fun onRemove(post: Post) {
                postVM.removeById(post.id)
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

        binding.closeEditMode.setOnClickListener {
            binding.editedModeGroup.visibility = View.GONE
            binding.content.setText("")
            binding.content.clearFocus()
        }


        binding.save.setOnClickListener {
            with(binding.content) {
                val text = text.toString()
                if (text.isBlank()) {
                    Toast.makeText(this@MainActivity, R.string.empty_content, Toast.LENGTH_LONG)
                        .show()
                    return@setOnClickListener
                }
                postVM.changeContent(text, binding.editedModeGroup)
                binding.editedModeGroup.visibility = View.GONE
                postVM.save()
                setText("")
                clearFocus()
                AndroidUtils.hideKeyboard(it)

            }
        }
    }

    private fun addBinding() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
