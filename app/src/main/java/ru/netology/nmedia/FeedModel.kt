package ru.netology.nmedia

import ru.netology.nmedia.dto.Post

data class FeedModel(
    val posts: List<Post> = emptyList(),
    val empty: Boolean = false,
)

sealed interface FeedModelState{
    object Idle: FeedModelState
    object Error: FeedModelState
    object Loading: FeedModelState
    object Refreshing: FeedModelState
}