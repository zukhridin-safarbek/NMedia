package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.R
import ru.netology.nmedia.data.Post
import ru.netology.nmedia.databinding.CardPostLayoutBinding

typealias OnLikeListener = (post: Post) -> Unit
typealias OnShareListener = (post: Post) -> Unit
class PostsAdapter(
    private val onShareListener: OnShareListener,
    private val onLikeListener: OnLikeListener
) : ListAdapter<Post, PostsAdapter.PostViewHolder>(PostDiffCallback()) {
    class PostViewHolder(
        private val binding: CardPostLayoutBinding,
        private val onLikeListener: OnLikeListener,
        private val onShareListener: OnShareListener
    ) : RecyclerView.ViewHolder(binding.root) {
        private var itemPost: Post? = null
        private val likeOnClickListener: View.OnClickListener = View.OnClickListener {
            itemPost?.let { onLikeListener(it) }
        }
        private val shareOnClickListener : View.OnClickListener = View.OnClickListener {
            itemPost?.let { onShareListener(it) }
        }

        init {
            binding.likeIcon.setOnClickListener(likeOnClickListener)
            binding.shareIcon.setOnClickListener(shareOnClickListener)
        }

        fun bind(post: Post) {
            binding.apply {
                postAuthor.text = post.author
                postPublishedDate.text = post.publishedDate
                postContent.text = post.content
                postLikesCount.text = post.likes.toString()
                postSharesCount.text = post.shares.toString()
                likeIcon.setImageResource(
                    if (post.likedByMe) R.drawable.ic_like_svgrepo_com else R.drawable.ic_heart_svgrepo_com
                )
                itemPost = post
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostViewHolder {
        val binding =
            CardPostLayoutBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PostViewHolder(binding, onLikeListener, onShareListener)
    }


    override fun onBindViewHolder(holder: PostViewHolder, position: Int) {
        val post = getItem(position)
        holder.bind(post)
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
