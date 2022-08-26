package ru.netology.nmedia.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity

@Dao
interface PostDao {
    @Query("SELECT * FROM PostEntity ORDER BY id DESC")
    fun getAll(): LiveData<List<PostEntity>>

    @Insert
    fun insert(post: PostEntity)

    @Query("UPDATE PostEntity SET content = :content, videoLink = :videoLink WHERE id = :id")
    fun updateContentById(id: Long, content: String, videoLink: String?)

    fun save(post: PostEntity) =
        if (post.id == 0L) insert(post) else updateContentById(post.id, post.content, post.videoLink)
    @Query("""
        UPDATE PostEntity SET
        likes = likes + CASE WHEN likedByMe THEN -1 ELSE 1 END,
        likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END
        WHERE id = :id
    """)
    fun likedById(id: Long)

    @Query("""
        DELETE FROM PostEntity WHERE id = :id
    """)
    fun removedById(id: Long)

    @Query("""
        UPDATE PostEntity
        SET shares = shares + 1
        WHERE id = :id
    """)
    fun sharedById(id: Long)
}