package com.example.music_player.repository

import com.example.music_player.model.Song


// Define an interface for the music actions
interface MusicPlayerInterface {

    // Fetch all songs from local storage
    fun fetchSongs(): List<Song>
    fun getCurrentSong(): Song?

    fun deleteSong(songPath: String): Boolean

    // Play the selected song
    fun playSong(song: Song)

    // Pause the currently playing song
    fun pauseSong()

    fun nextSong()
    fun previousSong()
    fun playCurrentSong()
        // Stop the current song
    fun stopSong()

    // Get the duration of the current song
    fun getSongDuration(): Long

    // Get the current position of the song
    fun getCurrentPosition(): Long
   fun getSongsByIds(songIds: List<String>, callback: (List<Song>) -> Unit)

    // Seek to a specific position in the song
    fun seekTo(position: Long)

    fun resumeSong()
}
