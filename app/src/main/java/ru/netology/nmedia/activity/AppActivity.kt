package ru.netology.nmedia.activity

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.messaging.FirebaseMessaging
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.model.channelId
import javax.inject.Inject

@AndroidEntryPoint
class AppActivity (
) : AppCompatActivity(R.layout.activity_app) {
    @Inject
    lateinit var firebaseMessaging: FirebaseMessaging
    @Inject
    lateinit var googleApiAvailability: GoogleApiAvailability
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.chanel_remote_name)
            val descriptionText = getString(R.string.chanel_remote_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }

            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
        fbMessaging()
        checkGoogleApiAvailability()

    }

    private fun fbMessaging(
    ){
        firebaseMessaging.token.addOnCompleteListener { task->
            if (!task.isSuccessful){
                return@addOnCompleteListener
            }
            val token = task.result
        }
    }


    private fun checkGoogleApiAvailability() = with(GoogleApiAvailability.getInstance()) {
        val code = isGooglePlayServicesAvailable(this@AppActivity)
        if (code == ConnectionResult.SUCCESS) {
            return@with
        } else {
            if (isUserResolvableError(code)) {
                getErrorDialog(this@AppActivity, code, 9000)?.show()
                return
            }
            Toast.makeText(this@AppActivity,
                "NMedia won't run without Google Play services",
                Toast.LENGTH_LONG).show()
        }
    }

}