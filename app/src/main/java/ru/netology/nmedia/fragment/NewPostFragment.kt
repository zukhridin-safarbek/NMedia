package ru.netology.nmedia.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.MutableLiveData
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.databinding.FragmentNewPostBinding
import ru.netology.nmedia.`object`.DataTransferArg
import ru.netology.nmedia.fragment.FeedFragment.Companion.content
import ru.netology.nmedia.fragment.FeedFragment.Companion.link
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.viewmodel.PostViewModel


class NewPostFragment : Fragment() {
    lateinit var binding: FragmentNewPostBinding
    private val viewModel: PostViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    private fun root() {
        binding = FragmentNewPostBinding.inflate(layoutInflater)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        root()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        postOnEdit()
        viewModel.draftContent.observe(viewLifecycleOwner) {
            binding.content.setText(it)
        }
        viewModel.draftVideoLink.observe(viewLifecycleOwner) {
            binding.videoUrl.setText(it)
        }
        binding.content.requestFocus()
        binding.ok.setOnClickListener {
            AndroidUtils.hideKeyboard(requireView())
            if (!binding.content.text.isNullOrEmpty()) {
                viewModel.changeContentAndSave(binding.content.text.toString(),
                    binding.videoUrl.text.toString())
                viewModel.draftContent.value = ""
                viewModel.draftVideoLink.value = ""
                findNavController().navigateUp()

            } else {
                Toast.makeText(requireContext(), "Empty!", Toast.LENGTH_SHORT).show()
            }
        }
        val callBack = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!binding.content.text.isNullOrEmpty() || !binding.videoUrl.text.isNullOrEmpty()) {
                    viewModel.draftContent.value = binding.content.text.toString()
                    viewModel.draftVideoLink.value = binding.videoUrl.text.toString()
                }
                findNavController().navigateUp()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callBack)
    }

    private fun postOnEdit() {
        arguments?.content?.let {
            binding.content.setText(it)
        }
        arguments?.link?.let {
            binding.videoUrl.setText(it)
        }
    }

    companion object {
        var Bundle.contentText: String? by DataTransferArg
        var Bundle.linkText: String? by DataTransferArg
    }

}