package com.example.music_player.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.FirebaseDatabase

class userAuthRepositoryImp : userAuthInterface {

    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()

    // SignUp function: Registers a new user with email and password
    override fun signUp(email: String, password: String, name: String, onResult: (Boolean, String) -> Unit) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    storeUserData(user, email, name, onResult)
                } else {
                    onResult(false, task.exception?.message ?: "Error during sign up")
                }
            }
    }

    private fun storeUserData(user: FirebaseUser?, email: String, name: String?, onResult: (Boolean, String) -> Unit) {
        val userId = user?.uid ?: return
        val userRef = database.getReference("users").child(userId)

        val userData = mapOf(
            "email" to email,
            "name" to name,
            "uid" to userId
        )

        userRef.setValue(userData)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, "User data saved successfully")
                } else {
                    onResult(false, "Failed to save user data")
                }
            }
    }

    // Login function: Authenticates user with email and password
    override fun login(email: String, password: String, onResult: (Boolean, String) -> Unit) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onResult(true, "Login successful")
                } else {
                    onResult(false, task.exception?.message ?: "Error during login")
                }
            }
    }

    // Logout function: Signs out the current user
    override fun logout() {
        auth.signOut()
    }

    override fun isUserLoggedIn(): Boolean {
        return auth.currentUser != null
    }

    // Retrieve current user details from Firebase Database (optional)
    override fun getUserDetails(onResult: (Boolean, Map<String, Any>?) -> Unit) {
        val currentUser = auth.currentUser
        currentUser?.uid?.let { userId ->
            val userRef = database.getReference("users").child(userId)

            userRef.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val userData = task.result?.value as? Map<String, Any>
                    onResult(true, userData)
                } else {
                    onResult(false, null)
                }
            }
        }
    }

    fun getCurrentAuthUser(): FirebaseUser? {
        return auth.currentUser
    }

    // Retrieve user details by user ID
    override fun getUserDetailsById(userId: String, onResult: (Boolean, Map<String, Any>?) -> Unit) {
        val userRef = database.getReference("users").child(userId)

        userRef.get().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val userData = task.result?.value as? Map<String, Any>
                onResult(true, userData)
            } else {
                onResult(false, null)
            }
        }
    }
}
