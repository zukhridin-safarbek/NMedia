package ru.netology.nmedia.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.database.AppAuth
import ru.netology.nmedia.databinding.FragmentSignInBinding
import ru.netology.nmedia.viewmodel.SignInViewModel

class SignInFragment : Fragment() {
    private lateinit var binding: FragmentSignInBinding
    private val authViewModel: SignInViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        binding = FragmentSignInBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.submit.setOnClickListener {
            authViewModel.signIn(binding.loginTILET.text.toString(),
                binding.passwordTILET.text.toString())
            authViewModel.responseCode.observe(viewLifecycleOwner) {
                if (it.code.toString()[0] == '2') {
                    findNavController().navigateUp()
                } else {
                    Toast.makeText(requireContext(),
                        "code - ${it.code}, message - ${it.message}",
                        Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
}