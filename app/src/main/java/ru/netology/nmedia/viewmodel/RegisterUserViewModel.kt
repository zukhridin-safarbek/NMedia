package ru.netology.nmedia.viewmodel

import android.net.Uri
import androidx.lifecycle.*
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import ru.netology.nmedia.model.PhotoModel
import ru.netology.nmedia.repository.AuthRepositoryImpl
import java.io.File
import kotlin.coroutines.coroutineContext

class RegisterUserViewModel : ViewModel() {
    private val repository = AuthRepositoryImpl()
    private val _photo = MutableLiveData<PhotoModel?>(null)
    val photo: LiveData<PhotoModel?>
        get() = _photo

    fun registerUser(login: String, password: String, name: String) = viewModelScope.launch {
        repository.registerUser(login, password, name)
    }
    val responseCode = repository.responseCode.asLiveData(Dispatchers.Default)

    fun registerWithPhoto(login: String, password: String, name: String) = viewModelScope.launch {
        photo.value?.let { photoModel ->
                repository.registerUserWithPhoto(login, password, name, photoModel)
                savePhoto(null, null)

        }

    }

    fun savePhoto(uri: Uri?, file: File?) {
        _photo.value = PhotoModel(uri, file)
    }
}