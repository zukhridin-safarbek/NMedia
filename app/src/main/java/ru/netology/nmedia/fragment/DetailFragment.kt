package ru.netology.nmedia.fragment

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.android.material.snackbar.Snackbar
import ru.netology.nmedia.R
import ru.netology.nmedia.`object`.DataTransferArg
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.databinding.FragmentDetailBinding
import ru.netology.nmedia.dto.PostAttachmentTypeEnum
import ru.netology.nmedia.fragment.EditDetailPostFragment.Companion.detailIdPostEdit
import ru.netology.nmedia.fragment.FeedFragment.Companion.postId
import ru.netology.nmedia.viewmodel.PostViewModel

interface OnInteractionListener {
     fun onLike(post: Post)
     fun onShare(post: Post)
     fun onEdit(post: Post)
     fun onRemove(post: Post)
     fun playVideo(post: Post)
     fun menuBtn(post: Post, view: View)
}

class DetailFragment : Fragment(), OnInteractionListener {
    private lateinit var binding: FragmentDetailBinding
    private var postId: Long? = null
    private val viewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        binding = FragmentDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        arguments?.postId?.let {
            postId = it.toLong()
        }
        arguments?.detailIdPostEdit?.let {
            postId = it.toLong()
        }
        viewModel.data.observe(viewLifecycleOwner) { state ->
            state.posts.map { post ->
                if (postId == post.id) {
                    with(binding) {
                        postAuthor.text = post.author
                        postPublishedDate.text = post.publishedDate
                        postContent.text = post.content
                        likeIcon.text = post.likes.toString()
                        shareIcon.text = post.shares.toString()
                        likeIcon.isChecked = post.likedByMe
                        viewIcon.text = (12434).toString()
                        ru.netology.nmedia.adapter.getAvatarFromServer("${post.authorAvatar}",
                            postAvatar)
                        if (post.attachment != null && post.attachment.component3() == PostAttachmentTypeEnum.IMAGE) {
                            ru.netology.nmedia.adapter.getContentImageFromServer("http://10.0.2.2:9999/images/${post.attachment.component1()}",
                                postContentImage)
                        } else {
                            groupContentImageAndSeparator.visibility = View.GONE
                        }
                        if (!post.videoLink.isNullOrBlank()) {
                            playBtn.visibility = View.VISIBLE
                            playBtn.setOnClickListener {
                                playVideo(post)
                            }
                        } else {
                            playBtn.visibility = View.GONE
                        }

                        likeIcon.setOnClickListener {
                            onLike(post)
                        }
                        shareIcon.setOnClickListener {
                            onShare(post)
                        }
                        postMenuBtn.setOnClickListener {
                            menuBtn(post, it)
                        }
                    }
                }
            }

        }

    }

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
        findNavController().navigate(R.id.action_detailFragment_to_editDetailPostFragment,
            Bundle().apply {
                detailContent = post.content
                detailLink = post.videoLink
            })
    }

    override fun onRemove(post: Post) {
        viewModel.removeById(post.id)
        findNavController().navigateUp()
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

    override fun menuBtn(post: Post, view: View) {
        PopupMenu(view.context, view).apply {
            inflate(R.menu.options_post)
            setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.remove -> {
                        onRemove(post)
                        true
                    }
                    R.id.edit -> {
                        onEdit(post)
                        true
                    }
                    else -> false
                }
            }
        }.show()
    }

    companion object {
        var Bundle.detailContent: String? by DataTransferArg
        var Bundle.detailLink: String? by DataTransferArg
    }

    fun getAvatarFromServer(name: String, view: ImageView) {
        val url = "http://10.0.2.2:9999/avatars/$name"
        if ("http" in name) {
            Glide.with(view)
                .load(name)
                .timeout(10000)
                .fitCenter()
                .circleCrop()
                .error(R.drawable.ic_baseline_close_24)
                .into(view)
        } else {
            Glide.with(view)
                .load(url)
                .timeout(10000)
                .fitCenter()
                .circleCrop()
                .error(R.drawable.ic_baseline_close_24)
                .into(view)
        }

    }

    fun getContentImageFromServer(url: String, view: ImageView) {
        Glide.with(view)
            .load(url)
            .timeout(10000)
            .error(R.drawable.ic_baseline_close_24)
            .into(view)
    }

}