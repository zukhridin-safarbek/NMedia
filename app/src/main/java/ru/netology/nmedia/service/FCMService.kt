package ru.netology.nmedia.service

import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.ktx.remoteMessage
import com.google.gson.Gson
import ru.netology.nmedia.R
import ru.netology.nmedia.channelId
import ru.netology.nmedia.dto.Action
import ru.netology.nmedia.dto.Like
import ru.netology.nmedia.dto.Post
import ru.netology.nmedia.exception.MyIllegalArgumentException
import java.lang.Exception
import kotlin.random.Random

class FCMService : FirebaseMessagingService() {
    private val action = "action"
    private val content = "content"
    private val gson = Gson()

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        remoteMessage.data[action]?.let {
            try {
                when(Action.valueOf(it.uppercase())){
                    Action.LIKE -> handleLike(gson.fromJson(remoteMessage.data[content], Like::class.java))
                }
            }catch (e: Exception){
                Log.e(action, "nu enum constants how $it")
                null
            }

        }
    }

    override fun onNewToken(token: String) {
        println(token)
    }
    private fun handleLike(content: Like){
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(
                getString(
                        R.string.notification_user_liked,
                        content.userName,
                        content.postAuthor,
                )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        NotificationManagerCompat.from(this)
            .notify(Random.nextInt(100_000), notification)
    }
}