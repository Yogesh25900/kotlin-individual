package com.example.music_player.ui.fragment


import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.music_player.R
import com.example.music_player.databinding.FragmentLoginBinding
import com.example.music_player.repository.userAuthRepositoryImp
import com.example.music_player.viewmodel.userAuthViewModel

class loginFragment : Fragment() {
    private lateinit var  viewModel: userAuthViewModel

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        viewModel = userAuthViewModel(userAuthRepositoryImp())
        viewModel.authResult.observe(viewLifecycleOwner, Observer { result ->
            val (success, message) = result
            if (success) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                openFragment(onlineSongFragment())
                // You can navigate to login or home screen after successful signup

            } else {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        })
        // Login Button Click
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (validateInput(email, password)) {
                viewModel.login(email,password)
            }
        }

            // Navigate to Signup Page
        binding.tvSignup.setOnClickListener {
            openFragment(signupFragment())
        }

    }

    private fun validateInput(email: String, password: String): Boolean {
        return when {
            TextUtils.isEmpty(email) -> {
                binding.etEmail.error = "Email is required"
                false
            }
            TextUtils.isEmpty(password) -> {
                binding.etPassword.error = "Password is required"
                false
            }
            password.length < 6 -> {
                binding.etPassword.error = "Password must be at least 6 characters"
                false
            }
            else -> true
        }
    }



    private fun openFragment(fragment: Fragment) {


        parentFragmentManager.beginTransaction()
            .replace(R.id.framelayout, fragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
