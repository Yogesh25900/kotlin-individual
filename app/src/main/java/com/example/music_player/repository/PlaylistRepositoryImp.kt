package com.example.music_player.repository

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.music_player.model.Playlist
import com.example.music_player.model.Song
import com.google.gson.Gson

class PlaylistRepositoryImp(private val context: Context):PlaylistRepositoryInterface {

    private val sharedPreferences = context.getSharedPreferences("MusicPlayerPrefs", Context.MODE_PRIVATE)

    // Fetch playlists (from SharedPreferences or DB)
    override fun getPlaylists(): LiveData<List<Playlist>> {
        val playlistsLiveData = MutableLiveData<List<Playlist>>()
        val playlistsJson = sharedPreferences.getString("playlists", "[]")
        val playlists = Gson().fromJson(playlistsJson, Array<Playlist>::class.java).toList()
        playlistsLiveData.value = playlists
        return playlistsLiveData
    }

    // Add a new playlist
    override fun addPlaylist(playlist: Playlist) {
        val playlists = getPlaylists().value?.toMutableList() ?: mutableListOf()
        playlists.add(playlist)
        savePlaylists(playlists)
    }

    override fun addSongToPlaylist(playlistId: Int, song: Song) {

        val playlists = getPlaylists().value?.toMutableList() ?: mutableListOf()
        val playlist = playlists.find { it.id == playlistId }

        playlist?.let {
            it.songs.add(song)
            savePlaylists(playlists)
        }
    }

    // Save playlists to SharedPreferences or DB
    private fun savePlaylists(playlists: List<Playlist>) {
        val playlistsJson = Gson().toJson(playlists)
        sharedPreferences.edit().putString("playlists", playlistsJson).apply()
    }
}
