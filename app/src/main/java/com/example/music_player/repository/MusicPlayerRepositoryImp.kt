package com.example.music_player.repository
import android.Manifest
import java.io.File

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.music_player.model.Song
import com.example.music_player.model.onlineSong
import com.google.firebase.Firebase
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.io.IOException

class MusicPlayerRepositoryImp(private val context: Context) : MusicPlayerInterface {

    private var mediaPlayer: MediaPlayer? = MediaPlayer()
    private var songList: List<Song> = listOf()
    private var currentIndex = 0  // Track current song index
    private var isPlaying = false  // Track playing state
    private val database: FirebaseDatabase = FirebaseDatabase.getInstance()





    override fun deleteSong(songPath: String): Boolean {
        Log.d("DeleteSong", "Attempting to delete song at path: $songPath")

        // Check if file exists
        val songFile = File(songPath)
        if (!songFile.exists()) {
            Log.d("DeleteSong", "File does not exist at path: $songPath")
            return false
        }

        // Check Permissions
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                Log.d("DeleteSong", "No write permissions for the song file at: $songPath")
                return false
            }
        } else {
            if (!Environment.isExternalStorageManager()) {
                Log.d("DeleteSong", "No Manage External Storage permission for: $songPath")
                return false
            }
        }

        // Try deleting the file
        val deleted = songFile.delete()
        if (deleted) {
            Log.d("DeleteSong", "Song deleted successfully: $songPath")
            return true
        } else {
            Log.d("DeleteSong", "Failed to delete song. It might be in use or protected.")
            return false
        }
    }




    init {
        mediaPlayer = MediaPlayer()
        songList = fetchSongs()  // Ensure songs are fetched during initialization
        mediaPlayer?.setOnCompletionListener {
            // Automatically play next song when the current song ends
            nextSong()
        }
    }


    private val _onlinesongList = MutableLiveData<List<onlineSong>>()
    val onlinesonglist: LiveData<List<onlineSong>> get() = _onlinesongList


    fun fetchSongsFromFirebase(onError: (Exception) -> Unit) {
        val songsRef = database.getReference("songs")

        songsRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val songs = mutableListOf<onlineSong>()

                for (document in snapshot.children) {
                    val id = document.child("songId").getValue(String::class.java) ?: ""
                    val title = document.child("songTitle").getValue(String::class.java) ?: "Unknown Title"
                    val url = document.child("songUrl").getValue(String::class.java) ?: ""
                    val artist = document.child("artist").getValue(String::class.java) ?: "Unknown Artist"

                    songs.add(onlineSong(
                        id, title,url,artist))

                    // Log the details of each song
                    Log.d("FetchSongs", "Song found - ID: $id, Title: $title, Artist: $artist, URL: $url, Duration: 0, Selected: true")
                }

                // Log the total number of songs found
                if (songs.isNotEmpty()) {
                    Log.d("FetchSongs", "Total songs fetched: ${songs.size}")
                } else {
                    Log.d("FetchSongs", "No songs found")
                }

                // Update the LiveData with the fetched songs
                _onlinesongList.value = songs
            }

            override fun onCancelled(error: DatabaseError) {
                // Log any error that occurs
                Log.e("FetchSongs", "Error fetching songs: ${error.message}")
                onError(error.toException())
            }
        })
    }

    // Fetch songs from local storage
    override fun fetchSongs(): List<Song> {
        val songs = mutableListOf<Song>()
        val contentResolver = context.contentResolver
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION
        )

        val cursor = contentResolver.query(uri, projection, null, null, null)

        cursor?.use {
            val idColumn = it.getColumnIndex(MediaStore.Audio.Media._ID)
            val titleColumn = it.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val dataColumn = it.getColumnIndex(MediaStore.Audio.Media.DATA)
            val artistColumn = it.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val albumColumn = it.getColumnIndex(MediaStore.Audio.Media.ALBUM)
            val durationColumn = it.getColumnIndex(MediaStore.Audio.Media.DURATION)

            while (it.moveToNext()) {
                val id = it.getString(idColumn)
                val title = it.getString(titleColumn)
                val data = it.getString(dataColumn)
                val artist = it.getString(artistColumn) ?: "Unknown Artist"
                val album = it.getString(albumColumn) ?: "Unknown Album"
                val duration = it.getLong(durationColumn)
                val albumArt = getAlbumArt(data)

                songs.add(Song(id, title, data, artist, album, duration, albumArt))
            }
        }

        songList = songs // Store the fetched songs
        return songs
    }

    private fun getAlbumArt(songPath: String): ByteArray? {
        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(songPath)
            retriever.embeddedPicture
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            retriever.release()
        }
    }

    // Play the selected song
    override fun playSong(song: Song) {
        try {
            mediaPlayer?.reset()
            mediaPlayer?.setDataSource(song.path)
            mediaPlayer?.prepare()
            mediaPlayer?.start()
            isPlaying = true
            Log.d("MusicPlayerRepository", "Playing song: ${song.name}")

            currentIndex = songList.indexOf(song)  // Update the current index

        } catch (e: IOException) {
            Log.e("MusicPlayerRepository", "Error playing song: ${song.name}", e)
        }
    }

    // Play current song
    override fun playCurrentSong() {
        if (songList.isNotEmpty() && currentIndex in songList.indices) {
            playSong(songList[currentIndex])
        } else {
            Log.e("MusicPlayerRepository", "No songs available to play.")
        }
    }

    // Pause the currently playing song
    override fun pauseSong() {
        if (mediaPlayer?.isPlaying == true) {
            mediaPlayer?.pause()
            isPlaying = false
            Log.d("MusicPlayerRepository", "Paused song at: ${mediaPlayer?.currentPosition}")
        }
    }

    // Resume the song
    override fun resumeSong() {
        if (mediaPlayer?.isPlaying == false) {
            mediaPlayer?.start()
            isPlaying = true
            Log.d("MusicPlayerRepository", "Resumed song at: ${mediaPlayer?.currentPosition}")
        }
    }

    // Stop the current song
    override fun stopSong() {
        mediaPlayer?.apply {
            if (mediaPlayer?.isPlaying == true) {
                mediaPlayer?.stop()
                mediaPlayer?.reset()


        } else {
                Log.e("MusicPlayerRepository", "Error stopping song - no song playing")
            }
        }
    }

    // Get song duration
    override fun getSongDuration(): Long {
        return mediaPlayer?.duration?.toLong() ?: 0L
    }

    // Get current position of song
    override fun getCurrentPosition(): Long {
        return mediaPlayer?.currentPosition?.toLong() ?: 0L
    }

    // Seek to a specific position
    override fun seekTo(position: Long) {
        mediaPlayer?.seekTo(position.toInt())
    }

    // Update song progress
    fun updateProgress() {
        if (isPlaying) {
            val currentPosition = mediaPlayer?.currentPosition?.toLong() ?: 0L
            // Notify progress update
            // You can use LiveData or callbacks to update UI
        }
    }

    override fun getCurrentSong(): Song? {
        return if (songList.isNotEmpty() && currentIndex in songList.indices) {
            songList[currentIndex]
        } else {
            null
        }
    }

    // Play next song
    override fun nextSong() {
        if (songList.isNotEmpty()) {
            currentIndex = (currentIndex + 1) % songList.size // Move to the next song
            playSong(songList[currentIndex])
            Log.d("MusicPlayerRepository", "Playing next song: ${songList[currentIndex].name}")
        } else {
            Log.e("MusicPlayerRepository", "No songs available for next.")
        }
    }

    // Play previous song
    override fun previousSong() {
        if (songList.isNotEmpty()) {
            currentIndex = if (currentIndex > 0) currentIndex - 1 else songList.size - 1 // Move to previous song
            playSong(songList[currentIndex])
            Log.d("MusicPlayerRepository", "Playing previous song: ${songList[currentIndex].name}")
        } else {
            Log.e("MusicPlayerRepository", "No songs available for previous.")
        }
    }

    // Release media player resources
    fun releasePlayer() {
        mediaPlayer?.release()
        mediaPlayer = null
    }
    // Repository Method
    override fun getSongsByIds(songIds: List<String>, callback: (List<Song>) -> Unit) {
        val songs = mutableListOf<Song>()
        val contentResolver = context.contentResolver
        val uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DURATION
        )

        // Create a selection string with the song IDs
        val selection = "${MediaStore.Audio.Media._ID} IN (${songIds.joinToString(",")})"

        val cursor = contentResolver.query(uri, projection, selection, null, null)

        cursor?.use {
            val idColumn = it.getColumnIndex(MediaStore.Audio.Media._ID)
            val titleColumn = it.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val dataColumn = it.getColumnIndex(MediaStore.Audio.Media.DATA)
            val artistColumn = it.getColumnIndex(MediaStore.Audio.Media.ARTIST)
            val albumColumn = it.getColumnIndex(MediaStore.Audio.Media.ALBUM)
            val durationColumn = it.getColumnIndex(MediaStore.Audio.Media.DURATION)

            while (it.moveToNext()) {
                val id = it.getString(idColumn)
                val title = it.getString(titleColumn)
                val data = it.getString(dataColumn)
                val artist = it.getString(artistColumn) ?: "Unknown Artist"
                val album = it.getString(albumColumn) ?: "Unknown Album"
                val duration = it.getLong(durationColumn)
                val albumArt = getAlbumArt(data)

                songs.add(Song(id, title, data, artist, album, duration, albumArt))
            }
        }

        callback(songs) // Pass the song list to the callback
    }




}
