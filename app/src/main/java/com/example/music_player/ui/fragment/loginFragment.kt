package com.example.music_player.ui.fragment


import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.music_player.R
import com.example.music_player.databinding.FragmentLoginBinding

class loginFragment : Fragment() {

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

        // Login Button Click
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()

            if (validateInput(email, password)) {
                performLogin(email, password)
            }
        }

            // Navigate to Signup Page
        binding.tvSignup.setOnClickListener {
            openSignupFragment()
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

    private fun performLogin(email: String, password: String) {
        // Simulating login logic (Replace with Firebase/Auth logic)
        if (email == "test@example.com" && password == "password") {
            Toast.makeText(requireContext(), "Login Successful", Toast.LENGTH_SHORT).show()
            // Navigate to Home or Dashboard
        } else {
            Toast.makeText(requireContext(), "Invalid Credentials", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openSignupFragment() {


        parentFragmentManager.beginTransaction()
            .replace(R.id.framelayout, signupFragment())
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
