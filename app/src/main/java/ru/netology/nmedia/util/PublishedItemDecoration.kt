package ru.netology.nmedia.util

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import dagger.hilt.android.qualifiers.ApplicationContext

class PublishedItemDecoration(@ApplicationContext context: Context,private val resId: Int) : RecyclerView.ItemDecoration() {

}