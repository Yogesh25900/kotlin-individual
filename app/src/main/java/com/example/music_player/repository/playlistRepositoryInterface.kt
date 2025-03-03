package com.example.music_player.repository

import androidx.lifecycle.LiveData
import com.example.music_player.model.Playlist

interface playlistRepositoryInterface {
    // Method to fetch all playlists
    fun fetchPlaylists()

    // Method to create a new playlist
    fun createPlaylist(name: String)

    // Method to delete a playlist
    fun deletePlaylist(playlistId: String)

    // Method to get all playlists
    fun getAllPlaylists(): LiveData<List<Playlist>>

    // Method to add songs to a playlist
    fun addSongsToPlaylist(playlistId: String, songIds: List<String>): LiveData<Boolean>
}
