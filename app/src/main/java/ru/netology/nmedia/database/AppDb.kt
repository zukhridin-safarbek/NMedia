package ru.netology.nmedia.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import ru.netology.nmedia.`interface`.PostDao
import ru.netology.nmedia.`object`.PostColumns

class AppDb private constructor(db: SQLiteDatabase){
    val postDao: PostDao = PostDaoImpl(db)

    companion object{
        @Volatile
        private var instance: AppDb? = null

        fun getInstance(context: Context): AppDb{
            return instance ?: synchronized(this){
                instance ?: AppDb(
                    buildDatabase(context, arrayOf(PostDaoImpl.DDL))
                ).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context, DDLs: Array<String>) = NMediaDBHelper(
            context, PostColumns.DATABASE_VERSION, PostColumns.DATABASE_NAME, DDLs
        ).writableDatabase
    }
}