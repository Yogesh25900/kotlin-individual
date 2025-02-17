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

class MusicPlayerViewModel private constructor(private val musicPlayerRepository: MusicPlayerRepositoryImp) : ViewModel() {

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

    init {
        fetchSongs()
    }

    private fun fetchSongs() {
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
