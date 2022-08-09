package ru.netology.nmedia.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContract
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.ActivityNewOrEditPostBinding

class NewOrEditPostActivity : AppCompatActivity() {
    lateinit var binding: ActivityNewOrEditPostBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityNewOrEditPostBinding.inflate(layoutInflater)
        setContentView(binding.root)
        val postContent = intent?.extras?.getString("PostContent")
        val postUrl = intent?.extras?.getString("PostUrl")
        binding.content.setText(postContent)
        binding.videoUrl.setText(postUrl)
        binding.content.requestFocus()
        binding.add.setOnClickListener {
            val intent = Intent()
                val text = binding.content.text.toString()
                val url = binding.videoUrl.text.toString()
                if (text.isBlank()){
                    Toast.makeText(this, R.string.empty_content, Toast.LENGTH_SHORT).show()
                    setResult(RESULT_CANCELED, intent)
                }else{
                    intent.putExtra("postContent", text)
                    intent.putExtra("postUrl", url)

                    setResult(RESULT_OK, intent)
                }
            finish()
            }
        }
    class ContractNewOrEditPost : ActivityResultContract<Pair<String, String?>, Pair<String?, String?>?>() {
        override fun createIntent(context: Context, input: Pair<String, String?>): Intent =
            Intent(context, NewOrEditPostActivity::class.java).apply {
                putExtra("PostContent", input.first)
                putExtra("PostUrl", input.second)
            }


        override fun parseResult(resultCode: Int, intent: Intent?): Pair<String?, String?>? =
            if (resultCode == Activity.RESULT_OK){
                Pair(intent?.getStringExtra("postContent"), intent?.getStringExtra("postUrl"))
            }else{
                null
            }

    }
}