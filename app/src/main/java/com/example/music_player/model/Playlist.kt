package com.example.music_player.model

data class Playlist(
    val id: Int = Companion.generateId(),  // Auto-incrementing ID
    val name: String,
    var songs: MutableList<Song> = mutableListOf()
) {
    companion object {
        private var currentId = 0

        // Generates an auto-incrementing ID
        fun generateId(): Int {
            return ++currentId  // Increment and return the new ID
        }
    }
}
