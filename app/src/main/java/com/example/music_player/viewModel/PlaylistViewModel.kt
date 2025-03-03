package com.example.music_player.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.music_player.model.Playlist
import com.example.music_player.repository.PlaylistRepository

class PlaylistViewModel(private val repository: PlaylistRepository) : ViewModel() {

    val playlistsLiveData: LiveData<List<Playlist>> = repository.playlistsLiveData

    fun fetchPlaylists() {
        repository.fetchPlaylists()
    }

    fun createPlaylist(name: String) {
        repository.createPlaylist(name)
    }

    fun addSongsToPlaylist(playlistId: String, songIds: List<String>): LiveData<Boolean> {
        return repository.addSongsToPlaylist(playlistId, songIds)
    }

    fun deletePlaylist(playlistId: String) {
        // Call the repository method to delete the playlist
        repository.deletePlaylist(playlistId)


    }

}
