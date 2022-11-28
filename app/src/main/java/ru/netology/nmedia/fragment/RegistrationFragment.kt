package ru.netology.nmedia.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import ru.netology.nmedia.database.AppAuth
import ru.netology.nmedia.databinding.FragmentRegistrationBinding
import ru.netology.nmedia.viewmodel.RegisterUserViewModel

class RegistrationFragment : Fragment() {
    private lateinit var binding: FragmentRegistrationBinding
    private val viewModel: RegisterUserViewModel by viewModels(
        ownerProducer = ::requireParentFragment
    )

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
                viewModel.registerUser(binding.loginTIET.text.toString(),
                    binding.passwordTIET.text.toString(),
                    binding.nameTIET.text.toString())
                Handler(Looper.getMainLooper()).postDelayed(Runnable {
                    if (AppAuth.getInstance().authStateFlow.value?.id != null) {
                        findNavController().navigateUp()
                    }
                }, 200)
            } else {
                binding.confirmPasswordTIL.error = "Passwords not equal"
            }
        }
    }
}