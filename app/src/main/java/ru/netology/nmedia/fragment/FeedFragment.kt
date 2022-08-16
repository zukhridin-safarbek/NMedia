package ru.netology.nmedia.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.`object`.DataTransferArg
import ru.netology.nmedia.activity.NewPostFragment
import ru.netology.nmedia.activity.NewPostFragment.Companion.contentText
import ru.netology.nmedia.activity.NewPostFragment.Companion.linkText
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.viewmodel.PostViewModel
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.data.Post
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.databinding.CardPostLayoutBinding

class FeedFragment : Fragment() {
    private lateinit var binding: FragmentFeedBinding
    private lateinit var bindingCardPost: CardPostLayoutBinding
    private lateinit var bundle: Bundle
    private val viewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        addBinding()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postControl()
        postOnSave()
    }

    private fun postOnSave(){
        var content: String?
        var link: String?
        arguments?.let {
            it.contentText.let { text->
                content = text
                println(text)
            }
            it.linkText.let { text->
                link = text
            }
            if (!content.isNullOrEmpty()){
                viewModel.changeContentAndSave(content, link)
            }
        }
    }


    private fun postControl() {
        val adapter = PostsAdapter(object : OnInteractionListener {
            override fun onLike(post: Post) {
                viewModel.likeById(post.id)
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
                    viewModel.shareByID(post.id)
                }
            }

            override fun onEdit(post: Post) {
                viewModel.edit(post)
                findNavController().navigate(R.id.action_feedFragment_to_newPostFragment, Bundle().apply {
                    content = post.content
                    link = post.videoLink
                    postId = post.id.toString()
                })
            }

            override fun onRemove(post: Post) {
                viewModel.removeById(post.id)
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

        viewModel.data.observe(viewLifecycleOwner) { post ->
            val newPost = adapter.itemCount < post.size
            adapter.submitList(post) {
                if (newPost) {
                    binding.list.smoothScrollToPosition(0)
                }
             }
        }

        binding.addOrEditBtn.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_newPostFragment)
        }

    }

    private fun addBinding() {
        bindingCardPost = CardPostLayoutBinding.inflate(layoutInflater)
        binding = FragmentFeedBinding.inflate(layoutInflater)
        bundle = Bundle()
    }
    companion object {
        var Bundle.content: String? by DataTransferArg
        var Bundle.link: String? by DataTransferArg
        var Bundle.postId: String? by DataTransferArg
    }
}
