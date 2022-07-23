package ru.netology.nmedia

import android.os.Bundle
import android.os.Handler
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import ru.netology.nmedia.databinding.ActivityMainBinding
import java.text.DecimalFormat

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var  likesCount = 1_199_999
    private lateinit var vm: MainViewModel
    private lateinit var handler: Handler
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addBinding()
        like()
    }

    private fun like(){
        vm = ViewModelProvider(this).get(MainViewModel::class.java)
        handler = Handler(mainLooper)
        when {
            likesCount == 0 -> {
                binding.postLikesCount.text = ""
            }
            likesCount < 1000 -> {
                binding.postLikesCount.text = likesCount.toString()
            }
            else -> {
                vm.likesShortFormFunc(likesCount)
                 vm.likesShortForm.observe(this){
                     binding.postLikesCount.text = it
                }
            }
        }
        with(binding){
            vm.resultIsLiked.observe(this@MainActivity){
                likeIcon.setImageResource(it)
            }
            ChangeIntegerToShortForm.countShares(shareIcon, postSharesCount)
            likeIcon.setOnClickListener {
                vm.countLikes(likesCount)
                with(vm){
                    handler.postDelayed({
                        vm.likesShortForm.observe(this@MainActivity){
                            binding.postLikesCount.text = it
                        }
                    }, 500)
                    likesLongForm.observe(this@MainActivity){
                        postLikesCount.text = it
                    }
                }

            }
        }
    }
    private fun addBinding() {
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}