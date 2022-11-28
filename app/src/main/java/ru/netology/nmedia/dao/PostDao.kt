package ru.netology.nmedia.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.entity.PostEntity

@Dao
interface PostDao {
    @Query("SELECT * FROM PostEntity WHERE showed = 1 ORDER BY id DESC")
    fun getAll(): Flow<List<PostEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: PostEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(post: List<PostEntity>)

    @Query("UPDATE PostEntity SET content = :content, videoLink = :videoLink WHERE id = :id")
    suspend fun updateContentById(id: Long, content: String, videoLink: String?)

    @Query("UPDATE PostEntity SET showed = 1")
    suspend fun changeNewerShowed()


    suspend fun save(post: PostEntity) =
        if (post.id == 0L) insert(post) else updateContentById(post.id, post.content, post.videoLink)
    @Query("""
        UPDATE PostEntity SET
        likes = likes + CASE WHEN likedByMe THEN -1 ELSE 1 END,
        likedByMe = CASE WHEN likedByMe THEN 0 ELSE 1 END
        WHERE id = :id
    """)
    suspend fun likedById(id: Long)

    @Query("""
        DELETE FROM PostEntity WHERE id = :id
    """)
    suspend fun removedById(id: Long)

    @Query("""
        DELETE FROM PostEntity
    """)
    suspend fun deleteAll()

    @Query("""
        UPDATE PostEntity
        SET shares = shares + 1
        WHERE id = :id
    """)
    suspend fun sharedById(id: Long)
}