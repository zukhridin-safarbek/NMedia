package ru.netology.nmedia.activity

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging
import ru.netology.nmedia.R
import ru.netology.nmedia.channelId
import ru.netology.nmedia.databinding.FragmentFeedBinding
import ru.netology.nmedia.viewmodel.PostViewModel

class AppActivity : AppCompatActivity(R.layout.activity_app) {
    private lateinit var binding: FragmentFeedBinding
    private val viewModel: PostViewModel by viewModels(
    )
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val name = getString(R.string.chanel_remote_name)
            val descriptionText = getString(R.string.chanel_remote_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }

        checkGoogleApiAvailability()
    }

    private fun checkGoogleApiAvailability() = with(GoogleApiAvailability.getInstance()) {
        val code = isGooglePlayServicesAvailable(this@AppActivity)
        if (code == ConnectionResult.SUCCESS){
            return@with
        }else{
            if (isUserResolvableError(code)){
                getErrorDialog(this@AppActivity, code, 9000)?.show()
                return
            }
            Toast.makeText(this@AppActivity, "NMedia won't run without Google Play services", Toast.LENGTH_LONG).show()
        }
    }

}