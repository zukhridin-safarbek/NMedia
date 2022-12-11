package ru.netology.nmedia.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.launch
import ru.netology.nmedia.repository.AuthRepositoryImpl
import javax.inject.Inject

@ExperimentalCoroutinesApi
@HiltViewModel
class SignInViewModel @Inject constructor(
    private val repository: AuthRepositoryImpl,
) : ViewModel() {
    fun signIn(login: String, password: String) = viewModelScope.launch {
        repository.signIn(login, password)
    }

    val responseCode = repository.responseCode.asLiveData(Dispatchers.Default)
}