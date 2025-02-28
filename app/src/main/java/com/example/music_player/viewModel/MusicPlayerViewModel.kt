package com.example.music_player.viewModel

import android.util.Log
import androidx.lifecycle.*
import com.example.music_player.model.Song
import com.example.music_player.repository.MusicPlayerRepositoryImp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
class MusicPlayerViewModel(private val musicPlayerRepository: MusicPlayerRepositoryImp) : ViewModel() {

    // LiveData to observe songs
    private val _songs = MutableLiveData<List<Song>>()
    val songs: LiveData<List<Song>> get() = _songs

    private val _currentSong = MutableLiveData<Song?>()
    val currentSong: LiveData<Song?> get() = _currentSong

    private val _currentPosition = MutableLiveData<Long>()
    val currentPosition: LiveData<Long> get() = _currentPosition

    private val _songDuration = MutableLiveData<Long>()
    val songDuration: LiveData<Long> get() = _songDuration

    private val _isPlaying = MutableLiveData<Boolean>()
    val isPlaying: LiveData<Boolean> get() = _isPlaying

    private val _songProgress = MutableLiveData<Long>(0)
    val songProgress: LiveData<Long> = _songProgress

    private var progressUpdaterJob: Job? = null

    // LiveData for selected songs, changed to store List<String> (IDs only)
    private val _selectedSongs = MutableLiveData<List<String>>(emptyList()) // LiveData for song IDs
    val selectedSongs: LiveData<List<String>> get() = _selectedSongs

    init {
        fetchSongs()
    }

    fun fetchSongs() {
        val fetchedSongs = musicPlayerRepository.fetchSongs()
        Log.d("MusicPlayerViewModel", "Fetched songs: $fetchedSongs")
        _songs.postValue(fetchedSongs)
    }

    fun playSong(song: Song) {
        if (_currentSong.value != song) {
            // Handle playing a new song
            musicPlayerRepository.playSong(song)
            _currentSong.postValue(song)
            _songDuration.postValue(song.duration)
            _isPlaying.postValue(true)
            _songProgress.postValue(0L)
        } else {
            // Resume playback if the song is already playing
            musicPlayerRepository.resumeSong()
            _isPlaying.postValue(true)
        }
        startProgressUpdater()
    }

    private val _songDeletionResult = MutableLiveData<Boolean>()
    val songDeletionResult: LiveData<Boolean> get() = _songDeletionResult

    // Function to delete a song
    fun deleteSong(songPath: String) {
        val isDeleted = musicPlayerRepository.deleteSong(songPath)
        if (isDeleted) {
            // After deletion, update the list of songs
            val updatedSongs = _songs.value?.filter { it.path != songPath } ?: emptyList()
            _songs.postValue(updatedSongs)
        }
        _songDeletionResult.value = isDeleted
    }




    fun pauseSong() {
        _currentPosition.postValue(musicPlayerRepository.getCurrentPosition())
        musicPlayerRepository.pauseSong()
        _isPlaying.postValue(false)
        stopProgressUpdater()
    }

    fun resumeSong() {
        musicPlayerRepository.resumeSong()
        _isPlaying.postValue(true)
        startProgressUpdater()
    }

    fun nextSong() {
        Log.d("MusicPlayerViewModel", "Next button clicked")
        musicPlayerRepository.nextSong()
        _currentSong.postValue(musicPlayerRepository.getCurrentSong()) // Update UI
        _isPlaying.postValue(true)
    }

    fun previousSong() {
        Log.d("MusicPlayerViewModel", "Previous button clicked")
        musicPlayerRepository.previousSong()
        _currentSong.postValue(musicPlayerRepository.getCurrentSong()) // Update UI
        _isPlaying.postValue(true)
    }

    fun startProgressUpdater() {
        progressUpdaterJob?.cancel()  // Cancel any ongoing job
        progressUpdaterJob = viewModelScope.launch(Dispatchers.IO) {
            while (isActive && _isPlaying.value == true) {
                val currentPos = musicPlayerRepository.getCurrentPosition()
                _songProgress.postValue(currentPos)
                _currentPosition.postValue(currentPos)
                delay(1000) // Update every second
            }
        }
    }

    private fun stopProgressUpdater() {
        progressUpdaterJob?.cancel()
    }

    fun seekTo(position: Long) {
        musicPlayerRepository.seekTo(position)
        _currentPosition.postValue(position)
        _songProgress.postValue(position)
    }

    fun getCurrentSong(): Song? {
        return currentSong.value
    }

    override fun onCleared() {
        super.onCleared()
        musicPlayerRepository.releasePlayer()
        stopProgressUpdater()
    }

    // Update selected songs by their IDs only
    fun updateSelectedSongs(newSelectedSongIds: List<String>) {
        _selectedSongs.value = newSelectedSongIds  // Update with only song IDs
//        Log.d("update", "Selected song IDs updated: ${newSelectedSongIds.joinToString()}")
    }

    // Toggle song selection (now based on song IDs)
    fun toggleSongSelection(song: Song) {
        val currentList = _selectedSongs.value?.toMutableList() ?: mutableListOf()
        val songId = song.id  // Assuming `song.id` is a string
        if (currentList.contains(songId)) {
            currentList.remove(songId)
        } else {
            currentList.add(songId)
        }
        updateSelectedSongs(currentList)
    }
    // ViewModel Method
    fun getSongsByIds(songIds: List<String>): LiveData<List<Song>> {
        val songListLiveData = MutableLiveData<List<Song>>()

        // Call the repository method to get songs by IDs
        musicPlayerRepository.getSongsByIds(songIds) { songList ->
            // Post the result to LiveData
            songListLiveData.postValue(songList)
        }

        return songListLiveData
    }

    private fun getSongById(songId: String): Song? {
        // You can query the database or fetch from a list
        // For example, returning a dummy song:
        return Song(id = songId, name = "Song Name", artistName = "Artist", albumArt = null, path = "", albumName = "", albumId = 0L, duration = 0L)
    }




    // Singleton Pattern Implementation
    companion object {
        @Volatile
        private var INSTANCE: MusicPlayerViewModel? = null

        fun getInstance(musicPlayerRepository: MusicPlayerRepositoryImp): MusicPlayerViewModel {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: MusicPlayerViewModel(musicPlayerRepository).also { INSTANCE = it }
            }
        }
    }
}
