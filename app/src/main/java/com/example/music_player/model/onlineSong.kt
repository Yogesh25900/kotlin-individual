package com.example.music_player.model

data class onlineSong(
    val songId: String,
    val songTitle: String,
    val songUrl: String,
    val artist: String?,
    val albumArt: String? = null, // Optional album cover
    var selected: Boolean = false



)