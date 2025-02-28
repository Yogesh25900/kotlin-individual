package com.example.music_player.ui.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.music_player.R
import com.example.music_player.model.Playlist
import com.example.music_player.adapter.SongAdapter
import com.example.music_player.viewModel.MusicPlayerViewModel
import com.example.music_player.repository.MusicPlayerRepositoryImp


class PlaylistDetailFragment : Fragment() {

    private lateinit var playlistTitleTextView: TextView
    private lateinit var musicPlayerViewModel: MusicPlayerViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var songAdapter: SongAdapter
    private var playlist: Playlist? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_playlist_detail, container, false)

        // Get the playlist from arguments
        arguments?.let {
            playlist = it.getParcelable("playlist") ?: return@let
        }

        // Initialize ViewModel
        musicPlayerViewModel = MusicPlayerViewModel(MusicPlayerRepositoryImp(requireContext()))

        // Safely display playlist name
        playlistTitleTextView = rootView.findViewById(R.id.playlistTitleTextView)
        playlistTitleTextView.text = playlist?.name ?: "Playlist"

        // Initialize RecyclerView
        recyclerView = rootView.findViewById(R.id.songsRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)

        // Initialize the adapter with an empty list initially
        songAdapter = SongAdapter(
            songs = emptyList(), // Initially no songs
            onClick = { song ->
                // Handle song click (e.g., play the song)
                Log.d("PlaylistDetailFragment", "Song clicked: ${song.name}")
            },
            onCloseSelection = {
                // Handle close selection (e.g., exit selection mode)
                Log.d("PlaylistDetailFragment", "Selection mode closed")
            },
            songViewModel = musicPlayerViewModel
        )
        recyclerView.adapter = songAdapter

        // If playlist has songIds, fetch the corresponding songs from ViewModel
        playlist?.songIds?.keys?.toList()?.let { songIds ->  // Convert Set to List
            musicPlayerViewModel.getSongsByIds(songIds).observe(viewLifecycleOwner, Observer { songList ->
                // Log the received song list for debugging
                Log.d("PlaylistDetailFragment", "Song List: $songList")

                // Update the RecyclerView adapter with the new song list
                songAdapter.updateSongs(songList)
            })
        }

        return rootView
    }
}
