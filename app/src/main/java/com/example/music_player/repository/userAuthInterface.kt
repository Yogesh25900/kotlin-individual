package com.example.music_player.repository

interface userAuthInterface {

    // SignUp function: Registers a new user with email and password
    fun signUp(email: String, password: String, name: String, onResult: (Boolean, String) -> Unit)

    // Login function: Authenticates user with email and password
    fun login(email: String, password: String, onResult: (Boolean, String) -> Unit)

    // Logout function: Signs out the current user
    fun logout()

    // Function to check if the user is logged in
    fun isUserLoggedIn(): Boolean

    // Retrieve current user details from Firebase Database (optional)
    fun getUserDetails(onResult: (Boolean, Map<String, Any>?) -> Unit)

    // Retrieve user details by user ID
    fun getUserDetailsById(userId: String, onResult: (Boolean, Map<String, Any>?) -> Unit)
}
