package ru.netology.nmedia.fragment

import android.app.Activity
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import ru.netology.nmedia.R
import ru.netology.nmedia.databinding.FragmentNewPostBinding
import ru.netology.nmedia.fragment.FeedFragment.Companion.checkForDraft
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

    private val imageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            when (it.resultCode) {
                ImagePicker.RESULT_ERROR -> {
                    Toast.makeText(requireContext(), "${it.resultCode}", Toast.LENGTH_SHORT).show()
                }
                Activity.RESULT_OK -> {
                    val uri = it.data?.data
                    viewModel.savePhoto(uri, uri?.toFile())
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        root()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkClickedBtn()
        binding.content.requestFocus()
        binding.save.setOnClickListener {
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
        viewModel.photo.observe(viewLifecycleOwner) {
            binding.imageViewContainer.isVisible = it?.uri != null
            binding.preview.setImageURI(it?.uri)
        }
        binding.clearPhoto.setOnClickListener {
            viewModel.savePhoto(null, null)
        }
        binding.cameraBtn.setOnClickListener {
            ImagePicker.Builder(this).apply {
                cameraOnly()
                galleryMimeTypes(
                    arrayOf("image/jpeg", "image/png")
                )
                maxResultSize(2048, 2048)
                crop(1f, 1f)
                createIntent(imageLauncher::launch)
            }
        }
        binding.galleryBtn.setOnClickListener {
            ImagePicker.Builder(this).apply {
                galleryOnly()
                galleryMimeTypes(
                    arrayOf("image/jpeg", "image/png")
                )
                crop(1f, 1f)
                maxResultSize(2048, 2048)
                createIntent(imageLauncher::launch)
            }
        }

        if (arguments?.checkForDraft != "clickedEditBtn") {
            ownOnBack()
        }
    }


    private fun checkClickedBtn() {
        when (arguments?.checkForDraft) {
            "clickedEditBtn" -> postOnEdit()
            "clickedAddBtn" -> draft()
        }
    }

    private fun postOnEdit() {
        arguments?.content?.let {
            binding.content.setText(it)
        }
        arguments?.link?.let {
            binding.videoUrl.setText(it)
        }
    }

    private fun draft() {
        viewModel.draftContent.observe(viewLifecycleOwner) {
            binding.content.setText(it)
        }
        viewModel.draftVideoLink.observe(viewLifecycleOwner) {
            binding.videoUrl.setText(it)
        }
    }

    private fun ownOnBack() {
        val callBack = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!binding.content.text.isNullOrEmpty() || !binding.videoUrl.text.isNullOrEmpty()) {
                    viewModel.draftContent.value = binding.content.text.toString()
                    viewModel.draftVideoLink.value = binding.videoUrl.text.toString()
                } else {
                    viewModel.draftContent.value = ""
                    viewModel.draftVideoLink.value = ""
                }
                findNavController().navigateUp()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callBack)
    }

}