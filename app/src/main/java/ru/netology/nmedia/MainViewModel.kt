package ru.netology.nmedia

import android.util.Log
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.text.DecimalFormat

class MainViewModel : ViewModel() {
    val resultIsLiked = MutableLiveData<Int>()
    val likesLongForm = MutableLiveData<String>()
    val likesShortForm = MutableLiveData<String>()
    private var isLiked = false
    init {
        Log.e("AAA", "VM created")
    }
    fun likesShortFormFunc(like: Int){
        if (isLiked){
            likesShortForm.value = ChangeIntegerToShortForm.changeIntToShortFormWithChar(like + 1)
        }else {
            likesShortForm.value = ChangeIntegerToShortForm.changeIntToShortFormWithChar(like)
        }
    }
    fun countLikes(likesCount: Int){
        var like = likesCount
        if (!isLiked){
            like++
            isLiked = true
            resultIsLiked.value = R.drawable.ic_like_svgrepo_com
            if (likesCount < 1000){
                likesLongForm.value = like.toString()
            }else {
                likesLongForm.value = like.toString()
                likesShortForm.value = ChangeIntegerToShortForm.changeIntToShortFormWithChar(like)
            }
        }else{
            like = (likesCount + 1) - 1
            isLiked = false
            resultIsLiked.value = R.drawable.ic_heart_svgrepo_com
            if (likesCount < 1000){
                likesLongForm.value = like.toString()
            }else {
                likesLongForm.value = like.toString()
                likesShortForm.value = ChangeIntegerToShortForm.changeIntToShortFormWithChar(like)
            }
        }
        println(like)
    }
    override fun onCleared() {
        Log.e("AAA", "VM cleared")
        super.onCleared()
    }
}