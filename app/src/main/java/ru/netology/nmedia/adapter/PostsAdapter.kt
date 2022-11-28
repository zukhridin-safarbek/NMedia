package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isInvisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nmedia.R
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.databinding.CardPostLayoutBinding
import ru.netology.nmedia.dto.PostAttachmentTypeEnum
import ru.netology.nmedia.entity.PostEntity
import ru.netology.nmedia.fragment.ItemListener

interface OnInteractionListener {
    fun onLike(post: Post)
    fun onShare(post: Post)
    fun onEdit(post: Post)
    fun onRemove(post: Post)
    fun playVideo(post: Post)
    fun reSendPostToServerClick(post: Post)
}

class PostsAdapter(
    private val onInteractionListener: OnInteractionListener,
    private val listener: ItemListener,
) : ListAdapter<Post, PostsAdapter.PostViewHolder>(PostDiffCallback()) {
    class PostViewHolder(
        private val binding: CardPostLayoutBinding,
        private val onInteractionListener: OnInteractionListener,
    ) : RecyclerView.ViewHolder(binding.root) {
        private var itemPost: Post? = null
        private val likeOnClickListener: View.OnClickListener = View.OnClickListener {
            itemPost?.let { post ->
                onInteractionListener.onLike(post)
            }
        }
        private val shareOnClickListener: View.OnClickListener = View.OnClickListener {
            itemPost?.let { post -> onInteractionListener.onShare(post) }
        }

        init {
            binding.likeIcon.setOnClickListener(likeOnClickListener)
            binding.shareIcon.setOnClickListener(shareOnClickListener)
        }

        fun bind(post: Post, listener: ItemListener) {
            val date = listOf("вчера в 14:29", "вчера в 8:53", "сегодня в 00:35")
            binding.apply {
                postAuthor.text = "${post.author} : ${post.isInServer}"
                postPublishedDate.text = date.shuffled()[0]
                postContent.text = post.attachment?.component2() ?: post.content
                likeIcon.text = post.likes.toString()
                shareIcon.text = post.shares.toString()
                likeIcon.isChecked = post.likedByMe
                viewIcon.text = (12434).toString()
                getAvatarFromServer("${post.authorAvatar}", postAvatar)
                if (!post.videoLink.isNullOrBlank()) {
                    playBtn.visibility = View.VISIBLE
                    playBtn.setOnClickListener {
                        onInteractionListener.playVideo(post)
                    }
                } else {
                    playBtn.visibility = View.GONE
                }
                groupContentImageAndSeparator.visibility = View.GONE
                if (post.attachment != null && post.attachment.component3() == PostAttachmentTypeEnum.IMAGE) {
                    getContentImageFromServer("http://10.0.2.2:9999/images/${post.attachment.component1()}",
                        postContentImage)
                } else {
                    groupContentImageAndSeparator.visibility = View.GONE
                }
                if (post.isInServer == false){
                    reSend.visibility = View.VISIBLE
                    reSend.setOnClickListener {
                        onInteractionListener.reSendPostToServerClick(post)
                    }
                }else{
                    reSend.visibility = View.GONE
                }
                itemPost = post
                postMenuBtn.setOnClickListener {
                    PopupMenu(it.context, it).apply {
                        inflate(R.menu.options_post)
                        setOnMenuItemClickListener { item ->
                            when (item.itemId) {
                                R.id.remove -> {
                                    onInteractionListener.onRemove(post)
                                    true
                                }
                                R.id.edit -> {
                                    onInteractionListener.onEdit(post)
                                    true
                                }
                                else -> false
                            }
                        }
                    }.show()
                }
            }
            itemView.setOnClickListener {
                listener.postItemOnClick(post)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding =
            CardPostLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onInteractionListener)
    }


    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post, listener)
    }


}

class PostDiffCallback : DiffUtil.ItemCallback<Post>() {
    override fun areItemsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Post, newItem: Post): Boolean {
        return oldItem == newItem
    }

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
