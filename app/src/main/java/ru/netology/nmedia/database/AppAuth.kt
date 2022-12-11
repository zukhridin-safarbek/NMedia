package ru.netology.nmedia.database

import android.content.Context
import androidx.core.content.edit
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.ktx.messaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import ru.netology.nmedia.model.AuthState
import ru.netology.nmedia.model.PushToken
import ru.netology.nmedia.service.Api
import java.lang.Exception
import java.lang.IllegalStateException

class AppAuth private constructor(context: Context) {
    private val prefs = context.getSharedPreferences("auth", Context.MODE_PRIVATE)
    private val idKey = "id"
    private val tokenKey = "token"
    private val avatarKey = "avatar"
    private val _authStateFlow = MutableStateFlow<AuthState?>(null)

    init {
        val id = prefs.getLong(idKey, 0)
        val token = prefs.getString(tokenKey, null)
        val avatar = prefs.getString(avatarKey, null)

        if (id == 0L || token == null) {
            prefs.edit { clear() }
        } else {
            _authStateFlow.value = AuthState(id, token, avatar ?: "")
        }
    }

    val authStateFlow: StateFlow<AuthState?> = _authStateFlow.asStateFlow()

    @Synchronized
    fun setAuth(id: Long, token: String, avatar: String) {
        _authStateFlow.value = AuthState(id, token, avatar)
        with(prefs.edit()) {
            putLong(idKey, id)
            putString(tokenKey, token)
            putString(avatarKey, avatar)
            apply()
        }
        sendPushToken()
    }

    @Synchronized
    fun removeAuth() {
        _authStateFlow.value = null
        prefs.edit { clear() }
        sendPushToken()
    }

    companion object {
        @Volatile
        private var instance: AppAuth? = null

        fun getInstance(): AppAuth = synchronized(this) {
            instance
                ?: throw IllegalStateException("AppAuth is not installed, you must call AppAuth.initialApp(Context context) first.")
        }

        fun initApp(context: Context): AppAuth = instance ?: synchronized(this) {
            instance ?: buildAuth(context).also { instance = it }
        }

        private fun buildAuth(context: Context): AppAuth = AppAuth(context)
    }

    fun sendPushToken(token: String? = null){
        CoroutineScope(Dispatchers.Default).launch {
            try {
                val pushToken = PushToken(token ?: Firebase.messaging.token.await())
                val response = Api.retrofitService.saveToken(pushToken)
                println("response.body(): ${response.message()}")
            }catch (e: Exception){
                e.printStackTrace()
            }
        }
    }
}