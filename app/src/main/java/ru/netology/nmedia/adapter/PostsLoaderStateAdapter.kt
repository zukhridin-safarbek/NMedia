package ru.netology.nmedia.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.qualifiers.ApplicationContext
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.LoaderItemBinding

class PostsLoaderStateAdapter(@ApplicationContext val context: Context) : LoadStateAdapter<PostsLoaderStateAdapter.ItemViewHolder>() {
    class ItemViewHolder(
        private val binding: LoaderItemBinding,
        val context: Context
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(loadState: LoadState) {
            if (loadState is LoadState.Error) {
                Toast.makeText(context, loadState.error.localizedMessage, Toast.LENGTH_LONG)
                    .show()
            }
            binding.progress.isVisible = loadState is LoadState.Loading
        }
    }

    override fun onBindViewHolder(holder: ItemViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): ItemViewHolder  = ItemViewHolder(
        LoaderItemBinding.inflate(LayoutInflater.from(parent.context), parent, false), context)
}