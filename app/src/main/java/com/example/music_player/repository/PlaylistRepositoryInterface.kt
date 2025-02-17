package com.example.music_player.repository

import androidx.lifecycle.LiveData
import com.example.music_player.model.Playlist
import com.example.music_player.model.Song

interface PlaylistRepositoryInterface {
    // Fetch playlists (from SharedPreferences or DB)
    fun getPlaylists(): LiveData<List<Playlist>>

    // Add a new playlist
    fun addPlaylist(playlist: Playlist)

    // Add a song to a playlist
    fun addSongToPlaylist(playlistId: Int, song: Song)
}
