package ru.netology.nmedia.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import ru.netology.nmedia.repository.AuthRepositoryImpl

class RegisterUserViewModel : ViewModel() {
    private val repository = AuthRepositoryImpl()

    fun registerUser(login: String, password: String, name: String) = viewModelScope.launch {
        repository.registerUser(login, password, name)
    }
}