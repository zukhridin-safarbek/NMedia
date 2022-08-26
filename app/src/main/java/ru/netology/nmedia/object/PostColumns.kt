package ru.netology.nmedia.`object`

object PostColumns {
    const val TABLE = "posts"
    const val COLUMN_ID = "id"
    const val COLUMN_AUTHOR = "author"
    const val COLUMN_CONTENT = "content"
    const val COLUMN_PUBLISHED = "published"
    const val COLUMN_LIKED_BY_ME = "likedByMe"
    const val COLUMN_LIKES = "likes"
    const val COLUMN_SHARES = "shares"
    const val COLUMN_VIDEO_URL = "videoUrl"

    const val DATABASE_NAME = "NMedia.db"
    const val DATABASE_VERSION = 1
    val ALL_COLUMNS = arrayOf(
        COLUMN_ID,
        COLUMN_AUTHOR,
        COLUMN_CONTENT,
        COLUMN_PUBLISHED,
        COLUMN_LIKED_BY_ME,
        COLUMN_LIKES,
        COLUMN_SHARES,
        COLUMN_VIDEO_URL
    )
    const val SQL_DELETE_TABLE = "DROP TABLE IF EXISTS $TABLE"
}