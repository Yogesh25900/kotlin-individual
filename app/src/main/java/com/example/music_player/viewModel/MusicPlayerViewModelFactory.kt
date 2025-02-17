package com.example.music_player.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.music_player.repository.MusicPlayerRepositoryImp

class MusicPlayerViewModelFactory(private val repository: MusicPlayerRepositoryImp) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
