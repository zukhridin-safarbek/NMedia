package ru.netology.nmedia

import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import ru.netology.nmedia.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private var likesCount = 1_199_999
    private lateinit var vm: MainViewModel
    private lateinit var handler: Handler
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        addBinding()
        like()
        with(vm){
            sharesShortForm()
            sharesShortForm.observe(this@MainActivity){
                binding.postSharesCount.text = it
            }
        }
    }

    private fun like() {
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
                vm.likesShortForm.observe(this) {
                    binding.postLikesCount.text = it
                }
            }
        }
        with(binding) {
            vm.resultIsLiked.observe(this@MainActivity) {
                likeIcon.setImageResource(it)
            }
            shareIcon.setOnClickListener {
                vm.sharesCount()
                 handler.postDelayed({
                     vm.sharesShortForm.observe(this@MainActivity){
                         postSharesCount.text = (it)
                     }
                 }, 500)
                vm.sharesLongForm.observe(this@MainActivity){
                    postSharesCount.text = (it)
                }
            }
            likeIcon.setOnClickListener {
                vm.countLikes(likesCount)
                with(vm) {
                    handler.postDelayed({
                        vm.likesShortForm.observe(this@MainActivity) {
                            binding.postLikesCount.text = it
                        }
                    }, 500)
                    likesLongForm.observe(this@MainActivity) {
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
