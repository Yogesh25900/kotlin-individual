package com.example.music_player.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.music_player.R

class signupFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_signup, container, false)

        val etName = view.findViewById<EditText>(R.id.etName)
        val etEmail = view.findViewById<EditText>(R.id.etEmail)
        val etPassword = view.findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = view.findViewById<EditText>(R.id.etConfirmPassword)
        val btnSignup = view.findViewById<Button>(R.id.btnSignup)
     val tvlogin= view.findViewById<TextView>(R.id.tvlogin)
        btnSignup.setOnClickListener {
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (name.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(requireContext(), "All fields are required", Toast.LENGTH_SHORT).show()
            } else if (password != confirmPassword) {
                Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Signup Successful!", Toast.LENGTH_SHORT).show()
                // TODO: Handle user registration (e.g., save to database or Firebase)
            }
        }

        tvlogin.setOnClickListener {
                openLoginFragment()
        }

        return view
    }

    private fun openLoginFragment() {


        parentFragmentManager.beginTransaction()
            .replace(R.id.framelayout, loginFragment())
            .addToBackStack(null)
            .commit()
    }

}
