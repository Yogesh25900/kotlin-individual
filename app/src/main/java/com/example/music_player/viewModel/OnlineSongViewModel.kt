package com.example.music_player.viewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.music_player.model.Song
import com.example.music_player.model.onlineSong
import com.example.music_player.repository.MusicPlayerRepositoryImp
import kotlinx.coroutines.launch

class OnlineSongViewModel(private val songRepository: MusicPlayerRepositoryImp) : ViewModel() {

    val songList: LiveData<List<onlineSong>> = songRepository.onlinesonglist
    val error = MutableLiveData<Exception?>()

    fun fetchSongs() {
        songRepository.fetchSongsFromFirebase { exception ->
            error.value = exception
        }
    }
}
