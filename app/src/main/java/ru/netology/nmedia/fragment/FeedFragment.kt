package ru.netology.nmedia.fragment

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.coroutineScope
import ru.netology.nmedia.FeedModelState
import ru.netology.nmedia.R
import ru.netology.nmedia.`object`.DataTransferArg
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.viewmodel.PostViewModel
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.databinding.CardPostLayoutBinding
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.util.CheckNetworkConnection

class FeedFragment : Fragment(), ItemListener {
    private lateinit var binding: FragmentFeedBinding
    private lateinit var bindingCardPost: CardPostLayoutBinding
    private lateinit var bundle: Bundle
    private var postsList = emptyList<PostEntity>()
    private lateinit var adapter: PostsAdapter
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
        checkAndSetDraftSettings()
    }

    private fun checkAndSetDraftSettings() {
        if (!viewModel.draftContent.value.isNullOrEmpty() || !viewModel.draftVideoLink.value.isNullOrEmpty()) {
            binding.addOrEditBtn.backgroundTintList = ColorStateList.valueOf(Color.RED)
            binding.addOrEditBtn.drawable.setTint(Color.WHITE)
        }
    }


    private fun postControl() {
        controlAdapter()
        postLoadOnCreate()
        openAddOrEditScreen()
        controlSwipeRefreshLayout()

    }

    private fun controlAdapter() {
        adapter = PostsAdapter(object : OnInteractionListener {
            override fun onLike(post: Post) {
                if (post.likedByMe) viewModel.dislikeById(post.id) else viewModel.likeById(post.id)
            }


            override fun onShare(post: Post) {
                Toast.makeText(requireContext(),
                    "Backend don't give link for share by id",
                    Toast.LENGTH_SHORT).show()
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

            override fun reSendPostToServerClick(post: PostEntity) {
                viewModel.reSend(post)
            }

        }, this)
    }

    private fun controlSwipeRefreshLayout() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refresh()

        }
    }

    private fun openAddOrEditScreen() {
        binding.addOrEditBtn.setOnClickListener {
            findNavController().navigate(R.id.action_feedFragment_to_newPostFragment,
                Bundle().apply {
                    checkForDraft = "clickedAddBtn"
                })
        }
    }

    private fun postLoadOnCreate() {
        binding.list.adapter = adapter
        viewModel.dataEntity.observe(viewLifecycleOwner) { state ->
            postsList = state
            adapter.submitList(postsList)
            if (postsList.isNotEmpty() || viewModel.serverNoConnection.value == false) {
                binding.addOrEditBtn.visibility = View.VISIBLE
            }
        }
        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.progress.isVisible = state is FeedModelState.Loading
            binding.swipeRefreshLayout.isRefreshing = state is FeedModelState.Refreshing
            if (state is FeedModelState.Error || !CheckNetworkConnection.isNetworkAvailable(
                    requireContext())
            ) {
                Snackbar.make(binding.root, "Error Feed Model", Snackbar.LENGTH_SHORT)
                    .setAction(R.string.retry) { viewModel.refresh() }
                    .show()
            }

        }
    }

    private fun addBinding() {
        bindingCardPost = CardPostLayoutBinding.inflate(layoutInflater)
        binding = FragmentFeedBinding.inflate(layoutInflater)
        bundle = Bundle()
    }

    override fun postItemOnClick(post: Post) {
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
    fun postItemOnClick(post: Post)
}
