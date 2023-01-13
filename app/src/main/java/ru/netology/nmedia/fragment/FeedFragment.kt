package ru.netology.nmedia.fragment

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collectLatest
import okhttp3.internal.wait
import ru.netology.nmedia.R
import ru.netology.nmedia.`object`.DataTransferArg
import ru.netology.nmedia.adapter.OnInteractionListener
import ru.netology.nmedia.adapter.PostsAdapter
import ru.netology.nmedia.adapter.PostsLoaderStateAdapter
import ru.netology.nmedia.database.AppAuth
import ru.netology.nmedia.databinding.CardPostLayoutBinding
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.model.FeedModelState
import ru.netology.nmedia.util.CheckNetworkConnection
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.PostViewModel
import javax.inject.Inject

@AndroidEntryPoint
@OptIn(ExperimentalCoroutinesApi::class)
class FeedFragment : Fragment(), ItemListener {
    private lateinit var binding: FragmentFeedBinding
    private lateinit var bindingCardPost: CardPostLayoutBinding
    private lateinit var bundle: Bundle

    @Inject
    lateinit var appAuth: AppAuth
    private lateinit var postsAdapter: PostsAdapter
    private val authViewModel by viewModels<AuthViewModel>()

    private val viewModel: PostViewModel by activityViewModels()

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
        authViewModel.authState.observe(viewLifecycleOwner) {
            binding.unauthenticated.isVisible = !authViewModel.authenticated
            binding.signOut.isVisible = authViewModel.authenticated
            binding.signIn.setOnClickListener {
                findNavController().navigate(R.id.action_feedFragment_to_signInFragment)
            }
            binding.signUp.setOnClickListener { findNavController().navigate(R.id.action_feedFragment_to_registrationFragment) }
            binding.signOut.setOnClickListener {
                postsAdapter.refresh()
                appAuth.removeAuth()
            }
        }

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
        newerPosts()
    }

    private fun controlAdapter() {
        postsAdapter = PostsAdapter(object : OnInteractionListener {
            override fun onLike(post: Post) {
                if (appAuth.authStateFlow.value?.id != null) {
                    if (post.likedByMe) viewModel.dislikeById(post.id) else viewModel.likeById(post.id)
                } else {
                    Snackbar.make(requireView(),
                        "You can't like because you're not logged in!",
                        Snackbar.LENGTH_SHORT).apply {
                        setAction(getString(R.string.sign_in)
                        ) {
                            findNavController().navigate(R.id.action_feedFragment_to_signInFragment)
                        }
                        show()
                    }
                }

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

            override fun showPhoto(post: Post) {
                findNavController().navigate(R.id.action_feedFragment_to_showPhoto,
                    Bundle().apply {
                        postId = post.id.toString()
                    })
            }

            override fun reSendPostToServerClick(post: Post) {
                viewModel.reSend(post)
            }

        }, this)
    }

    private fun controlSwipeRefreshLayout() {
        lifecycleScope.launchWhenCreated {
            postsAdapter.loadStateFlow.collectLatest { state ->
                state.refresh is LoadState.Loading || state.append is LoadState.Loading || state.prepend is LoadState.Loading
            }
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            postsAdapter.refresh()
            lifecycleScope.launchWhenCreated {
                postsAdapter.loadStateFlow.collectLatest { state ->
                    binding.swipeRefreshLayout.isRefreshing = state.refresh is LoadState.Loading
                }
            }

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

    private fun newerPosts() {
        lifecycleScope.launchWhenCreated {
//            viewModel.newerCount.collectLatest { newerCount ->
//                if (newerCount > 0) {
//                    binding.goUpNewer.visibility = View.VISIBLE
//                    binding.goUpNewer.text = "${getString(R.string.go_up_newer)} +$newerCount"
//                    binding.goUpNewer.setOnClickListener {
//                        viewModel.newer()
//                        adapter.refresh()
//                        binding.list.smoothScrollToPosition(0)
//                        binding.goUpNewer.visibility = View.GONE
//                    }
//                } else {
//                    binding.goUpNewer.visibility = View.GONE
//                }
//            }
        }
    }

    private fun postLoadOnCreate() {
        binding.list.adapter = postsAdapter.withLoadStateHeaderAndFooter(header = PostsLoaderStateAdapter(),
            footer = PostsLoaderStateAdapter())
        lifecycleScope.launchWhenCreated {
            viewModel.data.collectLatest {
                postsAdapter.submitData(it)
            }
        }
        if (appAuth.authStateFlow.value?.id != null) {
            binding.addOrEditBtn.visibility = View.VISIBLE
        }

        lifecycleScope.launchWhenCreated {
            postsAdapter.loadStateFlow.collectLatest {
                binding.progress.isVisible = it.refresh is LoadState.Loading
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
        var Bundle.imageUrl: String? by DataTransferArg
        var Bundle.checkForDraft: String? by DataTransferArg
    }
}

interface ItemListener {
    fun postItemOnClick(post: Post)
}
