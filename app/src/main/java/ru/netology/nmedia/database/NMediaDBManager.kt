package ru.netology.nmedia.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import ru.netology.nmedia.`object`.PostColumns

class NMediaDBManager(context: Context) {
//    private val dbHelper = NMediaDBHelper(context)
//    var db: SQLiteDatabase? = null
//
//    fun openDb(){
//        db = dbHelper.writableDatabase
//    }
//    fun insertToDb(content: String, videoUrl: String){
//        val values = ContentValues().apply {
//            put(PostColumns.COLUMN_AUTHOR, "Netology")
//            put(PostColumns.COLUMN_CONTENT, content)
//            put(PostColumns.COLUMN_PUBLISHED, "now")
//            put(PostColumns.COLUMN_VIDEO_URL, videoUrl)
//        }
//        db?.insert(PostColumns.DATABASE_NAME, null, values)
//    }
//    fun closeDb(){
//        dbHelper.close()
//    }
}