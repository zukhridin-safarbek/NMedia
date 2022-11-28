package ru.netology.nmedia.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.R
import ru.netology.nmedia.`object`.DataTransferArg
import ru.netology.nmedia.databinding.FragmentEditDetailPostBinding
import ru.netology.nmedia.fragment.DetailFragment.Companion.detailContent
import ru.netology.nmedia.fragment.DetailFragment.Companion.detailLink
import ru.netology.nmedia.fragment.FeedFragment.Companion.content
import ru.netology.nmedia.fragment.FeedFragment.Companion.link
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.viewmodel.PostViewModel

class EditDetailPostFragment: Fragment() {
    private lateinit var binding: FragmentEditDetailPostBinding
    private val viewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentEditDetailPostBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postOnEdit()
        binding.content.requestFocus()
        binding.ok.setOnClickListener {
            AndroidUtils.hideKeyboard(requireView())
            if (!binding.content.text.isNullOrEmpty()){
                viewModel.changeContentAndSave(binding.content.text.toString(), binding.videoUrl.text.toString())
                findNavController().navigateUp()
            }else{
                Toast.makeText(requireContext(), "Empty!", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun postOnEdit(){
        arguments?.detailContent?.let {
            binding.content.setText(it)
        }
        arguments?.detailLink?.let {
            binding.videoUrl.setText(it)
        }
    }

    companion object {
        var Bundle.detailIdPostEdit: String? by DataTransferArg
    }
}