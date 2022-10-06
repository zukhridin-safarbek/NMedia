package ru.netology.nmedia.fragment

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.`object`.DataTransferArg
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.viewmodel.PostViewModel
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.databinding.CardPostLayoutBinding

class FeedFragment : Fragment(), ItemListener {
    private lateinit var binding: FragmentFeedBinding
    private lateinit var bindingCardPost: CardPostLayoutBinding
    private lateinit var bundle: Bundle
    private var postsList = emptyList<Post>()
    private val viewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        addBinding()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postControl()
        if (!viewModel.draftContent.value.isNullOrEmpty() || !viewModel.draftVideoLink.value.isNullOrEmpty()) {
            binding.addOrEditBtn.backgroundTintList = ColorStateList.valueOf(Color.RED)
            binding.addOrEditBtn.drawable.setTint(Color.WHITE)
        }
    }


    private fun postControl() {
        val adapter = PostsAdapter(object : OnInteractionListener {
            override fun onLike(post: Post) {
                viewModel.likeById(post.id)
            }


            override fun onShare(post: Post) {
                Toast.makeText(requireContext(),
                    "Backend don't give link for share by id",
                    Toast.LENGTH_SHORT).show()
//                val intent = Intent().apply {
//                    action = Intent.ACTION_SEND
//                    putExtra(Intent.EXTRA_TEXT, post.content)
//                    type = "text/plain"
//                }
//                val shareIntent = Intent.createChooser(intent, getString(R.string.share_post))
//                startActivity(shareIntent)
//                if (intent.action == Intent.ACTION_SEND) {
//                    viewModel.shareByID(post.id)
//                }
            }

            override fun onEdit(post: Post) {
                viewModel.edit(post)
                findNavController().navigate(R.id.action_feedFragment_to_newPostFragment,
                    Bundle().apply {
                        content = post.content
                        link = post.videoLink
                        checkForDraft = "clickedEditBtn"
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

        }, this)


        binding.list.adapter = adapter
        viewModel.data.observe(viewLifecycleOwner) { state ->
            postsList = state.posts
            adapter.submitList(postsList)
            binding.progress.isVisible = state.loading
            println(state.loading)
            binding.emptyText.isVisible = state.empty
        }




        binding.addOrEditBtn.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_newPostFragment,
                Bundle().apply {
                    checkForDraft = "clickedAddBtn"
                })
        }

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refresh()
            binding.progress.isVisible = false
            viewModel.data.observe(viewLifecycleOwner) { state ->
                binding.swipeRefreshLayout.isRefreshing = state.loading
                binding.progress.isVisible = false
            }
        }

    }

    private fun addBinding() {
        bindingCardPost = CardPostLayoutBinding.inflate(layoutInflater)
        binding = FragmentFeedBinding.inflate(layoutInflater)
        bundle = Bundle()
    }

    override fun onClick(post: Post) {
        findNavController().navigate(R.id.action_feedFragment_to_detailFragment, Bundle().apply {
            postId = post.id.toString()
        })
    }

    companion object {
        var Bundle.content: String? by DataTransferArg
        var Bundle.link: String? by DataTransferArg
        var Bundle.postId: String? by DataTransferArg
        var Bundle.checkForDraft: String? by DataTransferArg
    }
}

interface ItemListener {
    fun onClick(post: Post)
}
