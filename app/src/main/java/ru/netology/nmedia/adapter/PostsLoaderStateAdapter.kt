package ru.netology.nmedia.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.netology.nmedia.R

class PostsLoaderStateAdapter : LoadStateAdapter<PostsLoaderStateAdapter.ItemViewHolder>() {
    class ItemViewHolder(
        private val parent: ViewGroup,
        view: View,
    ) : RecyclerView.ViewHolder(view) {
        private val progressBar: ProgressBar = itemView.findViewById(R.id.progress)
        fun bind(loadState: LoadState) {
            if (loadState is LoadState.Error) {
                Toast.makeText(parent.context, loadState.error.localizedMessage, Toast.LENGTH_LONG)
                    .show()
            }
            progressBar.isVisible = loadState is LoadState.Loading
        }
    }

    override fun onBindViewHolder(holder: ItemViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): ItemViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.loader_item, parent, false)
        return ItemViewHolder(parent, view)
    }
}