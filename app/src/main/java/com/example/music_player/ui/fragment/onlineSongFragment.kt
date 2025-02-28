package com.example.music_player.ui.fragment

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.music_player.R
import com.example.music_player.adapter.OnlineSongAdapter
import com.example.music_player.model.onlineSong
import com.example.music_player.repository.MusicPlayerRepositoryImp
import com.example.music_player.viewModel.OnlineSongViewModel

class onlineSongFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var songAdapter: OnlineSongAdapter
    private lateinit var songViewModel: OnlineSongViewModel

    private var mediaPlayer: MediaPlayer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_online_song, container, false)

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerViewOnlineSongs)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        songViewModel = OnlineSongViewModel(MusicPlayerRepositoryImp(requireContext()))

        // Initialize Adapter
        songAdapter = OnlineSongAdapter(
            onClick = { song -> playSong(song) },
            onCloseSelection = { closeSelection() },
            songViewModel = songViewModel
        )

        // Set the RecyclerView with adapter
        recyclerView.adapter = songAdapter

        // Fetch songs from the repository
        songViewModel.fetchSongs()

        // Observe the error LiveData
        songViewModel.error.observe(viewLifecycleOwner, Observer { exception ->
            exception?.let {
                Toast.makeText(requireContext(), "Error fetching songs: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        })

        return view
    }

    private fun playSong(song: onlineSong) {
        stopMediaPlayer() // Stop any currently playing song before playing a new one

        if (song.songUrl.isEmpty()) {
            Toast.makeText(requireContext(), "Invalid song URL", Toast.LENGTH_SHORT).show()
            return
        }

        mediaPlayer = MediaPlayer().apply {
            setDataSource(requireContext(), Uri.parse(song.songUrl))
            setOnPreparedListener { start() }
            setOnCompletionListener {
                Toast.makeText(requireContext(), "Song finished playing", Toast.LENGTH_SHORT).show()
            }
            setOnErrorListener { _, what, extra ->
                Toast.makeText(requireContext(), "MediaPlayer error: $what, $extra", Toast.LENGTH_SHORT).show()
                false
            }
            prepareAsync()
        }
    }

    private fun stopMediaPlayer() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopMediaPlayer() // Release resources when fragment is destroyed
    }

    private fun closeSelection() {
        // Handle closing selection mode
    }
}
