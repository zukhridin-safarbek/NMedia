package ru.netology.nmedia.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

class CheckNetworkConnection {
    companion object {
        fun isNetworkAvailable(context: Context): Boolean {
            val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val capabilities = cm.getNetworkCapabilities(cm.activeNetwork)
            return (capabilities != null && capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET))
        }
    }
}