package com.example.music_player.repository

import android.content.Context
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.provider.MediaStore
import android.util.Log
import com.example.music_player.model.Song
import java.io.IOException

class MusicPlayerRepositoryImp(private val context: Context) : MusicPlayerInterface {

    private var mediaPlayer: MediaPlayer? = MediaPlayer()
    private var songList: List<Song> = listOf()
    private var currentIndex = 0  // Track current song index
    private var isPlaying = false  // Track playing state

    init {
        mediaPlayer = MediaPlayer()
        songList = fetchSongs()  // Ensure songs are fetched during initialization
        mediaPlayer?.setOnCompletionListener {
            // Automatically play next song when the current song ends
            nextSong()
        }
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
}
