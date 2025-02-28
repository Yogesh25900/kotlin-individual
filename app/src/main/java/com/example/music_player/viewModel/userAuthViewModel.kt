package com.example.music_player.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.music_player.repository.userAuthRepositoryImp

class userAuthViewModel(private val repository: userAuthRepositoryImp) : ViewModel() {

    // LiveData for user authentication result
    private val _authResult = MutableLiveData<Pair<Boolean, String>>()
    val authResult: LiveData<Pair<Boolean, String>> = _authResult

    // LiveData for user login status
    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn

    // SignUp function: Registers a new user with email and password
    fun signUp(email: String, password: String, name: String) {
        repository.signUp(email, password, name) { success, message ->
            _authResult.value = Pair(success, message)
        }
    }

    // Login function: Authenticates user with email and password
    fun login(email: String, password: String) {
        repository.login(email, password) { success, message ->
            _authResult.value = Pair(success, message)
            _isLoggedIn.value = success
        }
    }

    // Logout function: Signs out the current user
    fun logout() {
        repository.logout()
        _isLoggedIn.value = false
    }

    // Check if the user is logged in
    fun checkIfLoggedIn() {
        _isLoggedIn.value = repository.isUserLoggedIn()
    }

    // Optional: Retrieve current user details
    fun getUserDetails() {
        repository.getUserDetails { success, userData ->
            if (success) {
                Log.d("UserAuth", "User data: $userData")
            } else {
                Log.d("UserAuth", "Failed to retrieve user data")
            }
        }
    }
}
