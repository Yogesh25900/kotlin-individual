package com.example.music_player.repository

interface userAuthInterface {
    fun signUp(email: String, password: String, name:String ,onResult: (Boolean, String) -> Unit)
    fun login(email: String, password: String, onResult: (Boolean, String) -> Unit)
    fun logout()
}
