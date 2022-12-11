package ru.netology.nmedia.fragment

import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.net.toFile
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.github.dhaval2404.imagepicker.ImagePicker
import kotlinx.coroutines.flow.map
import ru.netology.nmedia.database.AppAuth
import ru.netology.nmedia.databinding.FragmentRegistrationBinding
import ru.netology.nmedia.viewmodel.RegisterUserViewModel

class RegistrationFragment : Fragment() {
    private lateinit var binding: FragmentRegistrationBinding
    private val viewModel: RegisterUserViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )
    private val imageLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            when (it.resultCode) {
                ImagePicker.RESULT_ERROR -> {
                    Toast.makeText(requireContext(), "${it.resultCode}", Toast.LENGTH_SHORT).show()
                }
                Activity.RESULT_OK -> {
                    val uri = it.data?.data
                    println(uri)
                    println(uri?.toFile())
                    viewModel.savePhoto(uri, uri?.toFile())
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentRegistrationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.signUp.setOnClickListener {
            if (binding.passwordTIET.text.toString() == binding.confirmPasswordTIET.text.toString()) {
                viewModel.registerWithPhoto(binding.loginTIET.text.toString(),
                    binding.passwordTIET.text.toString(),
                    binding.nameTIET.text.toString())
                viewModel.responseCode.observe(viewLifecycleOwner) {
                    if (it.code.toString()[0] == '2') {
                        findNavController().navigateUp()
                        viewModel.savePhoto(null, null)
                    } else {
                        Toast.makeText(requireContext(),
                            "code - ${it.code}, message - ${it.message}",
                            Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                binding.confirmPasswordTIL.error = "Passwords not equal"
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
    }
}