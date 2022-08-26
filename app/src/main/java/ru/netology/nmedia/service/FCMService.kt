package ru.netology.nmedia.service

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import ru.netology.nmedia.R
import ru.netology.nmedia.channelId
import ru.netology.nmedia.dto.Like
import ru.netology.nmedia.dto.Post
import java.io.InputStream
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL
import kotlin.random.Random

class FCMService : FirebaseMessagingService() {
    private val action = "action"
    private val content = "post"
    private val gson = Gson()

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        Log.d(action, "From: ${remoteMessage.from}")
        if (remoteMessage.data.isNotEmpty()) {
            Log.d(action, "Message data payload: ${remoteMessage.data}")
            sendNotification(gson.fromJson(remoteMessage.data[content], Post::class.java))
        }
    }

    private fun sendNotification(post: Post) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(post.author)
            .setStyle(NotificationCompat.BigTextStyle()
                .bigText(post.content))
            .setContentText(post.publishedDate)
            .setLargeIcon(post.videoLink?.let { getLargeImageFromUrl(it) })
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .build()
        NotificationManagerCompat.from(this)
            .notify(Random.nextInt(100_000), notification)
    }

    override fun onNewToken(token: String) {
        println(token)
    }

    private fun handleLike(content: Like) {
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

    private fun getLargeImageFromUrl(imgUrl: String): Bitmap? {
        return try {
            val url = URL(imgUrl)
            val connection: HttpURLConnection = url.openConnection() as HttpURLConnection
            connection.connect()

            val input: InputStream = connection.inputStream
            BitmapFactory.decodeStream(input)
        }catch (e: Exception){
            e.printStackTrace()
            null
        }
    }
}