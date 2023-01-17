package ru.netology.nmedia.adapter

import android.annotation.SuppressLint
import android.net.Uri
import android.os.Build
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.PopupMenu
import androidx.core.view.isVisible
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.AdCardBinding
import ru.netology.nmedia.databinding.CardPostLayoutBinding
import ru.netology.nmedia.databinding.PublishedCardBinding
import ru.netology.nmedia.dto.*
import ru.netology.nmedia.fragment.ItemListener
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneOffset

interface OnInteractionListener {
    fun onLike(post: Post)
    fun onShare(post: Post)
    fun onEdit(post: Post)
    fun onRemove(post: Post)
    fun showPhoto(post: Post)
    fun reSendPostToServerClick(post: Post)
}

class PostsAdapter(
    private val onInteractionListener: OnInteractionListener,
    private val listener: ItemListener,
) : PagingDataAdapter<FeedItem, RecyclerView.ViewHolder>(PostDiffCallback()) {
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

        @SuppressLint("SetTextI18n")
        @RequiresApi(Build.VERSION_CODES.O)
        fun bind(post: Post, listener: ItemListener) {

            binding.apply {
                postAuthor.text = "${post.author} : ${post.authorAvatar}"
                if (post.published != "now"){
                    val day = (OffsetDateTime.of(post.published?.let {
                        LocalDateTime.ofEpochSecond(it.toLong(), 0, ZoneOffset.UTC)
                    }, ZoneOffset.UTC)).dayOfMonth
                    val month = (OffsetDateTime.of(post.published?.let {
                        LocalDateTime.ofEpochSecond(it.toLong(), 0, ZoneOffset.UTC)
                    }, ZoneOffset.UTC)).month
                    val year = (OffsetDateTime.of(post.published?.let {
                        LocalDateTime.ofEpochSecond(it.toLong(), 0, ZoneOffset.UTC)
                    }, ZoneOffset.UTC)).year
                    postPublishedDate.text = "$day-$month-$year"
                }else{
                    postPublishedDate.text = post.published
                }
                postContent.text = post.content
                likeIcon.text = post.likes.toString()
                shareIcon.text = post.shares.toString()
                likeIcon.isChecked = post.likedByMe
                viewIcon.text = (12434).toString()
                getAvatar("${post.authorAvatar}", postAvatar)
                postMenuBtn.isVisible = post.ownedByMe

                if (post.attachment != null && post.attachment.component3() == PostAttachmentTypeEnum.IMAGE) {
                    groupContentImageAndSeparator.visibility = View.VISIBLE
                    getContentImageFromServer(post.attachment.component1(),
                        postContentImage)
                    postContentImage.setOnClickListener {
                        onInteractionListener.showPhoto(post)
                    }
                } else {
                    groupContentImageAndSeparator.visibility = View.GONE
                }
                if (post.isInServer == false) {
                    reSend.visibility = View.VISIBLE
                    reSend.setOnClickListener {
                        onInteractionListener.reSendPostToServerClick(post)
                    }
                } else {
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

    override fun getItemViewType(position: Int): Int =
        when (getItem(position)) {
            is Ad -> R.layout.ad_card
            is Post -> R.layout.card_post_layout
            is Published -> R.layout.published_card
            null -> error("Unknown item type")
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder =

        when (viewType) {

            R.layout.card_post_layout -> {
                val binding =
                    CardPostLayoutBinding.inflate(LayoutInflater.from(parent.context),
                        parent,
                        false)
                PostViewHolder(binding, onInteractionListener)
            }
            R.layout.ad_card -> {
                val binding =
                    AdCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                AdViewHolder(binding)
            }

            R.layout.published_card -> {
                val binding =
                    PublishedCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                PublishedViewHolder(binding)
            }
            else -> {
                error("unknown item type: $viewType")
            }


        }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is Ad -> (holder as? AdViewHolder)?.bind(item)
            is Post -> (holder as? PostViewHolder)?.bind(item, listener)
            is Published -> (holder as? PublishedViewHolder)?.bind(item)
            null -> error("unknown item type")
        }
    }

    class AdViewHolder(
        private val binding: AdCardBinding,
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(ad: Ad) {
            binding.image.load(ad.image)
        }
    }

    class PublishedViewHolder(private val binding: PublishedCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(published: Published) {
            binding.publishedView.text = published.published
        }
    }

}

private fun ImageView.load(url: String) {
    Glide.with(this)
        .load("http://10.0.2.2:9999/media/$url")
        .timeout(10000)
        .error(R.drawable.ic_baseline_close_24)
        .into(this)
}

class PostDiffCallback : DiffUtil.ItemCallback<FeedItem>() {
    override fun areItemsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        if (oldItem::class != newItem::class) {
            return false
        }
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: FeedItem, newItem: FeedItem): Boolean {
        return oldItem == newItem
    }

}

fun getAvatar(name: String, view: ImageView) {
    val images = listOf("https://webstockreview.net/images/human-clipart-old-boy-8.png",
        "https://upload.wikimedia.org/wikipedia/commons/thumb/1/1b/Icons8_flat_businesswoman.svg/1200px-Icons8_flat_businesswoman.svg.png",
        "https://get.pxhere.com/photo/avatar-people-person-business-user-man-character-set-icon-portrait-office-profile-pictograph-social-adult-suit-technology-individual-head-face-design-concept-emblem-symbol-smile-formal-elements-facial-expression-cartoon-male-forehead-cheek-chin-human-behavior-standing-gentleman-businessperson-mouth-clip-art-communication-conversation-public-speaking-finger-illustration-facial-hair-happiness-organization-graphics-1447663.jpg")
    val url = "http://10.0.2.2:9999/avatars/$name"
    if ("http" in name) {
        Glide.with(view)
            .load(name)
            .timeout(10000)
            .fitCenter()
            .circleCrop()
            .error(images.shuffled()[0])
            .into(view)
    } else if ("file" in name && name[0] == ('f') && name != "") {
        Glide.with(view)
            .load(Uri.parse(name))
            .fitCenter()
            .circleCrop()
            .error(images.shuffled()[0])
            .into(view)
    } else {
        Glide.with(view)
            .load(url)
            .timeout(10000)
            .fitCenter()
            .circleCrop()
            .error(images.shuffled()[0])
            .into(view)
    }

}

fun getContentImageFromServer(url: String, view: ImageView) {
    Glide.with(view)
        .load("http://10.0.2.2:9999/media/$url")
        .timeout(10000)
        .error(R.drawable.ic_baseline_close_24)
        .into(view)
}
