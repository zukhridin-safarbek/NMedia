package ru.netology.nmedia.database

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import ru.netology.nmedia.`object`.PostColumns

class NMediaDBHelper(context: Context, dbVersion: Int, dbName: String, private val DDLs: Array<String>) :
    SQLiteOpenHelper(context, PostColumns.DATABASE_NAME,
    null, PostColumns.DATABASE_VERSION){
    override fun onCreate(db: SQLiteDatabase?) {
        DDLs.forEach {
            db?.execSQL(it)
        }
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db?.execSQL(PostColumns.SQL_DELETE_TABLE)
        onCreate(db)
    }
}