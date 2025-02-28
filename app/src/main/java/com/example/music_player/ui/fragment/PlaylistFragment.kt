package com.example.music_player.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.music_player.R
import com.example.music_player.repository.PlaylistRepository
import com.example.music_player.adapter.PlaylistAdapter
import com.example.music_player.model.Playlist
import com.example.music_player.viewModel.PlaylistViewModel

class PlaylistFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var playlistAdapter: PlaylistAdapter
    private lateinit var createPlaylistButton: Button
    private lateinit var playlistNameEditText: EditText
    private var selectedSongIds: List<String> = emptyList()

    private lateinit var playlistViewModel: PlaylistViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_playlist, container, false)

        // Initialize ViewModel
        playlistViewModel = PlaylistViewModel(PlaylistRepository())

        recyclerView = rootView.findViewById(R.id.recyclerView)
        createPlaylistButton = rootView.findViewById(R.id.createPlaylistButton)
        playlistNameEditText = rootView.findViewById(R.id.playlistNameEditText)

        // Get selected song IDs from arguments
        arguments?.let {
            selectedSongIds = it.getStringArrayList("selectedSongIds") ?: emptyList()
        }

        // Set up RecyclerView
        recyclerView.layoutManager = LinearLayoutManager(context)
        playlistAdapter = PlaylistAdapter(
            onItemClick = { playlist ->
                // This is where you handle the click on the playlist item itself
                // For example, navigate to the playlist details
                addSongsToPlaylist(playlist.id, selectedSongIds)
            },
            onOpenPlaylistClick = { playlistId ->
                // Find the playlist object from the list using the playlistId
                val playlist = playlistAdapter.currentList.find { it.id == playlistId }

                // If the playlist is found, open it
                playlist?.let { openPlaylist(it) }
            }
        )




        recyclerView.adapter = playlistAdapter

        // Fetch and observe playlists from ViewModel
        playlistViewModel.fetchPlaylists()
        playlistViewModel.playlistsLiveData.observe(viewLifecycleOwner, Observer { playlists ->
            playlistAdapter.submitList(playlists)
        })

        // Create new playlist
        createPlaylistButton.setOnClickListener {
            val playlistName = playlistNameEditText.text.toString()
            if (playlistName.isNotEmpty()) {
                playlistViewModel.createPlaylist(playlistName)
                Toast.makeText(requireContext(), "Playlist created", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Please enter a playlist name", Toast.LENGTH_SHORT).show()
            }
        }

        return rootView
    }

    private fun addSongsToPlaylist(playlistId: String?, songIds: List<String>) {
        if (playlistId == null) return

        playlistViewModel.addSongsToPlaylist(playlistId, songIds)
            .observe(viewLifecycleOwner, Observer { success ->
                if (success) {
                    Toast.makeText(requireContext(), "Songs added to playlist!", Toast.LENGTH_SHORT).show()
                    parentFragmentManager.popBackStack()
                } else {
                    Toast.makeText(requireContext(), "Failed to add songs", Toast.LENGTH_SHORT).show()
                }
            })
    }
    private fun openPlaylist(playlist: Playlist) {
        val playlistDetailFragment = PlaylistDetailFragment()
        val bundle = Bundle().apply {
            putParcelable("playlist", playlist) // Pass the entire playlist object
        }

        playlistDetailFragment.arguments = bundle

        // Use the FragmentManager to navigate to the new fragment
        parentFragmentManager.beginTransaction()
            .replace(R.id.framelayout, playlistDetailFragment) // Replace with the actual container ID
            .addToBackStack(null)
            .commit()
    }


}
