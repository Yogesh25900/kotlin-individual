package com.example.music_player.model

data class Song(
    val id: String,
    val name: String,
    val path: String,
    val artistName: String?,
    val albumName: String?,
    val duration: Long  ,// Duration in milliseconds.
    val albumArt: ByteArray? = null, // Store album art as a byte array
    var selected: Boolean = false,  // Add a 'selected' property

    val albumId: Long? = null  // Nullable, since not all songs are in an album



)