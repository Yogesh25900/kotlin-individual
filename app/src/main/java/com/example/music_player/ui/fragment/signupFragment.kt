package com.example.music_player.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import com.example.music_player.R
import com.example.music_player.databinding.FragmentSignupBinding
import com.example.music_player.repository.userAuthRepositoryImp
import com.example.music_player.viewmodel.userAuthViewModel

class signupFragment : Fragment() {
    private lateinit var  viewModel: userAuthViewModel

    private var _binding: FragmentSignupBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout using ViewBinding
        _binding = FragmentSignupBinding.inflate(inflater, container, false)



        viewModel = userAuthViewModel(userAuthRepositoryImp())
        viewModel.authResult.observe(viewLifecycleOwner, Observer { result ->
            val (success, message) = result
            if (success) {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
                // You can navigate to login or home screen after successful signup
                openLoginFragment()
            } else {
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
            }
        })
        // Access UI components directly through binding
        binding.btnSignup.setOnClickListener {
            val name = binding.etName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString().trim()
            val confirmPassword = binding.etConfirmPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show()
            } else if (password != confirmPassword) {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Signup Successful!", Toast.LENGTH_SHORT).show()
                viewModel.signUp(email, password, name)

            }
        }

        // Set up the login TextView to navigate to the login fragment
        binding.tvlogin.setOnClickListener {
            openLoginFragment()
        }

        return binding.root
    }

    private fun openLoginFragment() {
        parentFragmentManager.beginTransaction()
            .replace(R.id.framelayout, loginFragment())
            .addToBackStack(null)
            .commit()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        // Nullify the binding to avoid memory leaks
        _binding = null
    }
}
