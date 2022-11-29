package ru.netology.nmedia.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch
import ru.netology.nmedia.repository.AuthRepositoryImpl

class SignInViewModel : ViewModel() {
    private val repository = AuthRepositoryImpl()
    fun signIn(login: String, password: String) = viewModelScope.launch {
        repository.signIn(login, password)
    }
    val responseCode = repository.responseCode.asLiveData(Dispatchers.Default)
}