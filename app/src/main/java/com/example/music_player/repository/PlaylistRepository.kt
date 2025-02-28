package com.example.music_player.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.music_player.model.Playlist
import com.google.firebase.database.*

class PlaylistRepository {

    private val firebaseDatabase: FirebaseDatabase = FirebaseDatabase.getInstance()
    private val playlistsRef: DatabaseReference = firebaseDatabase.reference.child("playlists")

    // LiveData to hold the list of playlists
    private val _playlistsLiveData = MutableLiveData<List<Playlist>>()
    val playlistsLiveData: LiveData<List<Playlist>> = _playlistsLiveData

    // Fetch playlists from Firebase
    fun fetchPlaylists() {
        Log.d("PlaylistRepository", "Fetching playlists...")
        playlistsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val playlists = mutableListOf<Playlist>()
                for (playlistSnapshot in snapshot.children) {
                    val playlist = playlistSnapshot.getValue(Playlist::class.java)
                    playlist?.let { playlists.add(it) }
                }

                Log.d("PlaylistRepository", "Fetched playlists: ${playlists.size}")
                playlists.forEach {
                    Log.d("PlaylistRepository", "Playlist: ${it.name}, ID: ${it.id}")
                }

                _playlistsLiveData.postValue(playlists)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("PlaylistRepository", "Error fetching playlists: ${error.message}")
            }
        })
    }

    // Create a new playlist in Firebase
    fun createPlaylist(name: String) {
        val playlistId = playlistsRef.push().key ?: return
        // Initialize songIds as an empty Map
        val songIdsMap = mutableMapOf<String, Boolean>()

        val newPlaylist = Playlist(id = playlistId, name = name, songIds = songIdsMap)

        playlistsRef.child(playlistId).setValue(newPlaylist)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // You can update the local LiveData directly without fetching everything again
                    _playlistsLiveData.value?.let {
                        val updatedList = it.toMutableList()
                        updatedList.add(newPlaylist)
                        _playlistsLiveData.postValue(updatedList)
                    }
                } else {
                    // Handle failure (e.g., show a Toast or log the error)
                }
            }
    }


    fun getAllPlaylists(): LiveData<List<Playlist>> {
        val playlistsLiveData = MutableLiveData<List<Playlist>>()

        playlistsRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val playlists = mutableListOf<Playlist>()
                for (playlistSnapshot in snapshot.children) {
                    val playlist = playlistSnapshot.getValue(Playlist::class.java)
                    playlist?.let { playlists.add(it) }
                }
                playlistsLiveData.value = playlists
            }

            override fun onCancelled(error: DatabaseError) {}
        })

        return playlistsLiveData
    }

    fun addSongsToPlaylist(playlistId: String, songIds: List<String>): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()

        playlistsRef.child(playlistId).child("songIds").get().addOnSuccessListener { dataSnapshot ->
            // Fetch the existing songIds from Firebase as a Map
            val currentSongIds = dataSnapshot.getValue(object : GenericTypeIndicator<Map<String, Boolean>>() {}) ?: mutableMapOf()

            // Add new songs without duplicates (to avoid duplicates, you can use a Set)
            val updatedSongIds = currentSongIds.toMutableMap()

            // Add new songs to the map
            songIds.forEach { songId ->
                updatedSongIds[songId] = true // You can set the value as needed (e.g., true)
            }

            // Update the songIds in Firebase
            playlistsRef.child(playlistId).child("songIds").setValue(updatedSongIds)
                .addOnSuccessListener {
                    result.postValue(true)
                }
                .addOnFailureListener {
                    result.postValue(false)
                }
        }.addOnFailureListener {
            result.postValue(false)
        }

        return result
    }


}
