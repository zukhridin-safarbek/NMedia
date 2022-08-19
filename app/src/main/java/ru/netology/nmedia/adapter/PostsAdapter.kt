package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.R
import ru.netology.nmedia.data.Post
import ru.netology.nmedia.databinding.CardPostLayoutBinding
import ru.netology.nmedia.fragment.ItemListener

interface OnInteractionListener {
    fun onLike(post: Post)
    fun onShare(post: Post)
    fun onEdit(post: Post)
    fun onRemove(post: Post)
    fun playVideo(post: Post)
}

class PostsAdapter(
    private val onInteractionListener: OnInteractionListener,
    private val listener: ItemListener
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
            binding.apply {
                postAuthor.text = post.author
                postPublishedDate.text = post.publishedDate
                postContent.text = post.content
                likeIcon.text = post.likes.toString()
                shareIcon.text = post.shares.toString()
                likeIcon.isChecked = post.likedByMe
                viewIcon.text = (12434).toString()
                if (!post.videoLink.isNullOrBlank()){
                    playBtn.visibility = View.VISIBLE
                    playBtn.setOnClickListener {
                        onInteractionListener.playVideo(post)
                    }
                }else{
                    playBtn.visibility = View.GONE
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
                listener.onClick(post)
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
