package ru.netology.nmedia.database.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import ru.netology.nmedia.`object`.PostColumns
import ru.netology.nmedia.dao.PostDao
import ru.netology.nmedia.database.AppDb
import javax.inject.Singleton


@InstallIn(SingletonComponent::class)
@Module
class DbModule {

    @Singleton
    @Provides
    fun provideDb(
        @ApplicationContext
        context: Context,
    ): AppDb = Room.databaseBuilder(
        context, AppDb::class.java, PostColumns.DATABASE_NAME
    )
        .build()

    @Provides
    fun providePostDao(
        appDb: AppDb,
    ): PostDao = appDb.postDao()
}