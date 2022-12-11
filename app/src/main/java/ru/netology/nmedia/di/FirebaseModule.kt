package ru.netology.nmedia.di
import com.google.firebase.messaging.FirebaseMessaging
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
class FirebaseModule{
    @Singleton
    @Provides
    fun provideFBMessaging(): FirebaseMessaging{
        return FirebaseMessaging.getInstance()
    }


}