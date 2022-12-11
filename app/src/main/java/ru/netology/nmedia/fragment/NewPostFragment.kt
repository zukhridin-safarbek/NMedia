package ru.netology.nmedia.fragment

import android.app.Activity
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.button.MaterialButton
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.database.AppAuth
import ru.netology.nmedia.databinding.FragmentNewPostBinding
import ru.netology.nmedia.fragment.FeedFragment.Companion.checkForDraft
import ru.netology.nmedia.fragment.FeedFragment.Companion.content
import ru.netology.nmedia.fragment.FeedFragment.Companion.link
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.viewmodel.PostViewModel
import javax.inject.Inject

@AndroidEntryPoint
class NewPostFragment : Fragment() {
    lateinit var binding: FragmentNewPostBinding
    @Inject
    lateinit var appAuth: AppAuth
    private val viewModel: PostViewModel by activityViewModels()

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
        if (appAuth.authStateFlow.value?.id != null) {
            binding.signOut.isVisible = true
            binding.signOut.setOnClickListener {
                val dialog = Dialog(requireContext())
                dialog.apply {
                    requestWindowFeature(Window.FEATURE_NO_TITLE)
                    setCancelable(false)
                    setContentView(R.layout.dialog_with_two_buttons)
                    dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                    val btnCancel = findViewById<MaterialButton>(R.id.cancel)
                    val btnYes = findViewById<MaterialButton>(R.id.yes)
                    btnCancel.setOnClickListener {
                        dismiss()
                    }
                    btnYes.setOnClickListener {
                        dismiss()
                        appAuth.removeAuth()
                        findNavController().navigateUp()
                    }
                    show()
                }
            }
        }
        binding.content.requestFocus()
        binding.save.setOnClickListener {
            AndroidUtils.hideKeyboard(requireView())
            if (!binding.content.text.isNullOrEmpty()) {
                if (appAuth.authStateFlow.value?.id != null) {
                    viewModel.changeContentAndSave(binding.content.text.toString(),
                        binding.videoUrl.text.toString())
                    viewModel.draftContent.value = ""
                    viewModel.draftVideoLink.value = ""
                    findNavController().navigateUp()
                } else {
                    val dialog = Dialog(requireContext())
                    dialog.apply {
                        requestWindowFeature(Window.FEATURE_NO_TITLE)
                        setCancelable(false)
                        setContentView(R.layout.dialog_with_two_buttons)
                        dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
                        val btnCancel = findViewById<MaterialButton>(R.id.cancel)
                        val btnYes = findViewById<MaterialButton>(R.id.yes)
                        val title = findViewById<TextView>(R.id.dialog_title)
                        val content = findViewById<TextView>(R.id.dialog_content)
                        title.text = "Sign In to..."
                        content.text = "You can't create post because you're not logged in!"

                        btnCancel.setOnClickListener {
                            dismiss()
                        }
                        btnYes.setOnClickListener {
                            findNavController().navigate(R.id.action_newPostFragment_to_signInFragment)
                            dismiss()
                        }
                        show()
                    }
                }

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