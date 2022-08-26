package ru.netology.nmedia.database

import android.content.ContentValues
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import ru.netology.nmedia.`interface`.PostDao
import ru.netology.nmedia.`object`.PostColumns
import ru.netology.nmedia.data.Post

class PostDaoImpl(private val db: SQLiteDatabase): PostDao {
    companion object{
        val DDL = """
            CREATE TABLE IF NOT EXISTS ${PostColumns.TABLE} (
                ${PostColumns.COLUMN_ID} INTEGER PRIMARY KEY AUTOINCREMENT,
                ${PostColumns.COLUMN_AUTHOR} TEXT NOT NULL,
                ${PostColumns.COLUMN_CONTENT} TEXT NOT NULL,
                ${PostColumns.COLUMN_PUBLISHED} TEXT,
                ${PostColumns.COLUMN_LIKED_BY_ME} BOOLEAN DEFAULT 0 NOT NULL CHECK(${PostColumns.COLUMN_LIKED_BY_ME} in (0,1)),
                ${PostColumns.COLUMN_LIKES} INTEGER DEFAULT 0 NOT NULL,
                ${PostColumns.COLUMN_SHARES} INTEGER DEFAULT 0 NOT NULL,
                ${PostColumns.COLUMN_VIDEO_URL} TEXT
            );
            """.trimIndent()

    }

    override fun getAll(): List<Post> {
        val posts = mutableListOf<Post>()
        db.query(
            PostColumns.TABLE,
            PostColumns.ALL_COLUMNS,
            null,
            null,
            null,
            null,
            "${PostColumns.COLUMN_ID} DESC"
        ).use {
            while (it.moveToNext()){
                posts.add(map(it))
            }
        }
        return posts
    }

    override fun save(post: Post): Post {
        val values = ContentValues().apply {
            if (0L != post.id){
                put(PostColumns.COLUMN_ID, post.id)
            }
            put(PostColumns.COLUMN_AUTHOR, "Netology")
            put(PostColumns.COLUMN_CONTENT, post.content)
            put(PostColumns.COLUMN_PUBLISHED, "now")
            put(PostColumns.COLUMN_LIKED_BY_ME, post.likedByMe)
            put(PostColumns.COLUMN_LIKES, post.likes)
            put(PostColumns.COLUMN_SHARES, post.shares)
            put(PostColumns.COLUMN_VIDEO_URL, post.videoLink)
        }
        val id = db.replace(PostColumns.TABLE, null, values)

        db.query(
            PostColumns.TABLE,
            PostColumns.ALL_COLUMNS,
            "${PostColumns.COLUMN_ID} = ?",
            arrayOf(id.toString()),
            null,
            null,
            null
        ).use {
            it.moveToNext()
            return map(it)
        }
    }

    override fun likedById(id: Long) {
            db.execSQL(
                """
                    UPDATE ${PostColumns.TABLE} SET
                        ${PostColumns.COLUMN_LIKES} = ${PostColumns.COLUMN_LIKES} + CASE WHEN ${PostColumns.COLUMN_LIKED_BY_ME} THEN -1 ELSE 1 END,
                        ${PostColumns.COLUMN_LIKED_BY_ME} = CASE WHEN ${PostColumns.COLUMN_LIKED_BY_ME} THEN 0 ELSE 1 END
                    WHERE ${PostColumns.COLUMN_ID} = ?;
                """.trimIndent(), arrayOf(id)
            )
    }

    override fun removedById(id: Long) {
        db.delete(
            PostColumns.TABLE,
            "${PostColumns.COLUMN_ID} = ?",
            arrayOf(id.toString())
        )
    }

    override fun sharedById(id: Long) {
        db.execSQL(
            """
                    UPDATE ${PostColumns.TABLE}
                    SET ${PostColumns.COLUMN_SHARES} = ${PostColumns.COLUMN_SHARES} + 1
                    WHERE ${PostColumns.COLUMN_ID} = ?;
                """.trimIndent(), arrayOf(id)
        )
    }

    private fun map(cursor: Cursor): Post{
        with(cursor){
            return Post(
                id = getLong(getColumnIndexOrThrow(PostColumns.COLUMN_ID)),
                author = getString(getColumnIndexOrThrow(PostColumns.COLUMN_AUTHOR)),
                content = getString(getColumnIndexOrThrow(PostColumns.COLUMN_CONTENT)),
                publishedDate = getString(getColumnIndexOrThrow(PostColumns.COLUMN_PUBLISHED)),
                likedByMe = getInt(getColumnIndexOrThrow(PostColumns.COLUMN_LIKED_BY_ME)) != 0,
                likes = getLong(getColumnIndexOrThrow(PostColumns.COLUMN_LIKES)),
                shares = getLong(getColumnIndexOrThrow(PostColumns.COLUMN_SHARES)),
                videoLink = getString(getColumnIndexOrThrow(PostColumns.COLUMN_VIDEO_URL))
            )
        }
    }
}