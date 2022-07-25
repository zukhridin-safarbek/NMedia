package ru.netology.nmedia

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    private val resultIsLikedMutable = MutableLiveData<Int>()
    val resultIsLiked: LiveData<Int> = resultIsLikedMutable
    private val likesLongFormMutable = MutableLiveData<String>()
    val likesLongForm: LiveData<String> = likesLongFormMutable
    private val likesShortFormMutable = MutableLiveData<String>()
    val likesShortForm: LiveData<String> = likesShortFormMutable
    private val sharesLongFormMutable = MutableLiveData<String>()
    val sharesLongForm: LiveData<String> = sharesLongFormMutable
    private var isLiked = false
    private var shares: Int = 1_236_466
    private var like: Long = 1_199_999
    var likeVM = like.toString()

    init {
        Log.e("AAA", "VM created")
    }


    fun likesShortFormFunc() {
        if (isLiked) {
            likesShortFormMutable.value =
                ChangeIntegerToShortForm.changeIntToShortFormWithChar(like + 1)
        } else {
            likesShortFormMutable.value =
                ChangeIntegerToShortForm.changeIntToShortFormWithChar(like)
        }
    }

    fun countLikes() {
        if (!isLiked) {
            like++
            isLiked = true
            resultIsLikedMutable.value = R.drawable.ic_like_svgrepo_com
            if (like < 1000) {
                likesLongFormMutable.value = like.toString()
            } else {
                likesLongFormMutable.value = like.toString()
                likesShortFormMutable.value =
                    ChangeIntegerToShortForm.changeIntToShortFormWithChar(like)
            }
        } else {
            like--
            isLiked = false
            resultIsLikedMutable.value = R.drawable.ic_heart_svgrepo_com
            if (like < 1000) {
                likesLongFormMutable.value = like.toString()
            } else {
                likesLongFormMutable.value = like.toString()
                likesShortFormMutable.value =
                    ChangeIntegerToShortForm.changeIntToShortFormWithChar(like)
            }
        }
    }

    fun sharesShortForm() {
        sharesLongFormMutable.value = shares.toString()
    }

    fun sharesCount() {
        shares++
        sharesLongFormMutable.value = shares.toString()
    }


    override fun onCleared() {
        Log.e("AAA", "VM cleared")
        super.onCleared()
    }
}