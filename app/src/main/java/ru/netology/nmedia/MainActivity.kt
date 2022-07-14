package ru.netology.nmedia

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import ru.netology.nmedia.databinding.ActivityMainBinding
import java.text.DecimalFormat
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

class MainActivity : AppCompatActivity() {
        lateinit var binding: ActivityMainBinding
        var  likesCount = 2299
        var forDecLikesCount = likesCount
        var pressedTime = 0L
        lateinit var handler: Handler
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        if (likesCount == 0){
            binding.postLikesCount.text = ""
        }else binding.postLikesCount.text = "${PrettyCount().prettyCount(likesCount)}"
        binding.likeIcon.setOnClickListener {
               if (pressedTime + 500 > System.currentTimeMillis()){
                   likesCount++
                   forDecLikesCount++
                   handler = Handler(mainLooper)
                   handler.postDelayed({
                       binding.postLikesCount.text = "${PrettyCount().prettyCount(likesCount)}"

                   },1000)
                   binding.postLikesCount.text = "${forDecLikesCount}"

               }
            pressedTime = System.currentTimeMillis()
               }


    }


}