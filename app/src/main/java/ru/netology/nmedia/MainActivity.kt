package ru.netology.nmedia

import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import ru.netology.nmedia.databinding.ActivityMainBinding
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var  likesCount = 1_199_999
    private var sharesCount = 0
    private lateinit var handler: Handler
    private var isLiked: Boolean = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addBinding()
        countLikes()
        countShares()
    }

    private fun countShares() {
        with(binding){
            postSharesCount.text = ""
            shareIcon.setOnClickListener {
                sharesCount++
                postSharesCount.text = sharesCount.toString()
            }
        }
    }

    private fun addBinding() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }

    private fun countLikes() {
        with(binding){
            when {
                likesCount == 0 -> {
                    postLikesCount.text = ""
                }
                likesCount < 1000 -> {
                    postLikesCount.text = likesCount.toString()
                }
                else -> postLikesCount.text = "${ChangeIntegerToShortForm().changeIntToShortFormWithChar(likesCount)}"
            }
            likeIcon.setOnClickListener {
                if (!isLiked){
                    likesCount++
                    isLiked = true
                    handler = Handler(mainLooper)
                    likeIcon.setImageResource(R.drawable.ic_like_svgrepo_com)
                    postLikesCount.text = DecimalFormat("#,##0").format(likesCount)
                    handler.postDelayed({
                        if (likesCount < 1000){
                            postLikesCount.text = likesCount.toString()
                        }else postLikesCount.text = "${ChangeIntegerToShortForm().changeIntToShortFormWithChar(likesCount)}"
                    }, 1500)
                }else{
                    likesCount--
                    isLiked = false
                    handler = Handler(mainLooper)
                    postLikesCount.text = DecimalFormat("#,##0").format(likesCount)
                    likeIcon.setImageResource(R.drawable.ic_heart_svgrepo_com)
                    handler.postDelayed({
                        if (likesCount < 1000){
                            postLikesCount.text = likesCount.toString()
                        }else postLikesCount.text = "${ChangeIntegerToShortForm().changeIntToShortFormWithChar(likesCount)}"
                    }, 1500)
                }
            }
        }
    }
}
