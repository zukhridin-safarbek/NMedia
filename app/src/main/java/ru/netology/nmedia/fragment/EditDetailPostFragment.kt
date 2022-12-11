package ru.netology.nmedia.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nmedia.R
import ru.netology.nmedia.`object`.DataTransferArg
import ru.netology.nmedia.database.AppAuth
import ru.netology.nmedia.databinding.FragmentEditDetailPostBinding
import ru.netology.nmedia.databinding.FragmentNewPostBinding
import ru.netology.nmedia.fragment.DetailFragment.Companion.detailContent
import ru.netology.nmedia.fragment.DetailFragment.Companion.detailLink
import ru.netology.nmedia.util.AndroidUtils
import ru.netology.nmedia.viewmodel.PostViewModel
import javax.inject.Inject

@AndroidEntryPoint
class EditDetailPostFragment : Fragment() {
    private lateinit var binding: FragmentEditDetailPostBinding
    @Inject
    lateinit var appAuth: AppAuth
    private val viewModel: PostViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
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
            if (!binding.content.text.isNullOrEmpty()) {
                if (appAuth.authStateFlow.value?.id != null) {
                    viewModel.changeContentAndSave(binding.content.text.toString(),
                        binding.videoUrl.text.toString())
                    findNavController().navigateUp()
                } else {
                    Snackbar.make(requireView(),
                        "You can't create post because you're not logged in!",
                        Snackbar.LENGTH_SHORT).apply {
                        setAction(getString(R.string.sign_in),
                            View.OnClickListener {
                                findNavController().navigate(R.id.action_editDetailPostFragment_to_signInFragment)
                            })
                        show()
                    }
                }
            } else {
                Toast.makeText(requireContext(), "Empty!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun postOnEdit() {
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