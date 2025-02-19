package com.example.music_player.ui.fragment

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.music_player.R
import com.example.music_player.adapter.SongAdapter
import com.example.music_player.databinding.FragmentHomeBinding
import com.example.music_player.model.Song
import com.example.music_player.repository.MusicPlayerRepositoryImp
import com.example.music_player.service.MusicPlayerService
import com.example.music_player.viewModel.MusicPlayerViewModel

class homeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var musicPlayerViewModel: MusicPlayerViewModel
    private lateinit var songAdapter: SongAdapter

    private var isUserSeeking = false
    private var selectionMode = false

    private val selectedSongsIds = mutableListOf<String>() // Track selected song IDs

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize Repository and ViewModel
        val repo = MusicPlayerRepositoryImp(requireContext())
        musicPlayerViewModel = MusicPlayerViewModel.getInstance(repo)

        // Initialize Song RecyclerView Adapter
        songAdapter = SongAdapter(
            emptyList(),
            ::onSongClick,
            ::onCloseSelection,
            musicPlayerViewModel // Pass the ViewModel to the adapter
        )
        binding.songRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.songRecyclerView.adapter = songAdapter

        // Observe song list
        musicPlayerViewModel.songs.observe(viewLifecycleOwner) { songs ->
            Log.d("HomeFragment", "Song List: $songs") // Log song list
            songAdapter.songs = songs
            songAdapter.notifyDataSetChanged()
        }

        // Observe current song changes
        musicPlayerViewModel.currentSong.observe(viewLifecycleOwner) { song ->
            updateMiniPlayer(song)
        }

        // Observe play/pause state
        musicPlayerViewModel.isPlaying.observe(viewLifecycleOwner) { isPlaying ->
            binding.miniPlayerPlayPause.setImageResource(
                if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play
            )
        }

        // Play/Pause button
        binding.miniPlayerPlayPause.setOnClickListener {
            if (musicPlayerViewModel.isPlaying.value == true) {
                musicPlayerViewModel.pauseSong()
            } else {
                musicPlayerViewModel.currentSong.value?.let { musicPlayerViewModel.resumeSong() }
            }
        }

        // Observe progress updates
        musicPlayerViewModel.songProgress.observe(viewLifecycleOwner) { progress ->
            if (!isUserSeeking) {
                binding.miniPlayerSeekBar.progress = progress.toInt()
                binding.miniPlayerCurrentTime.text = formatTime(progress)
            }
        }

        // SeekBar change listener
        binding.miniPlayerSeekBar.setOnSeekBarChangeListener(object :
            SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    isUserSeeking = true
                    binding.miniPlayerCurrentTime.text = formatTime(progress.toLong())
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
                isUserSeeking = true
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                isUserSeeking = false
                musicPlayerViewModel.seekTo(seekBar?.progress?.toLong() ?: 0)
            }
        })

        // Close selection button
        binding.btnCloseSelection.setOnClickListener {
            // Reset selection mode and hide the button
            Log.d("HomeFragment", "Close selection mode clicked") // Log close action
            songAdapter.selectionMode = false
            songAdapter.songs.forEach { it.selected = false } // Reset selected state
            selectedSongsIds.clear() // Clear selected song IDs
            songAdapter.notifyDataSetChanged()
            binding.btnCloseSelection.visibility = View.GONE
        }
    }

    // Handle song item click
// Handle song item click
    // Handle song item click
    private fun onSongClick(song: Song) {
        Log.d("MusicPlayer", "Song clicked: ${song.name}, selectionMode: $selectionMode")

        if (selectionMode) {
            // Toggle song selection state
            song.selected = !song.selected
            if (song.selected) {
                // Add the selected song's ID to the list
                selectedSongsIds.add(song.id)
                Log.d("MusicPlayer", "Song selected: ${song.name}")
            } else {
                // Remove the unselected song's ID from the list
                selectedSongsIds.remove(song.id)
                Log.d("MusicPlayer", "Song unselected: ${song.name}")
            }

            // Find the position of the song in the list
            val position = songAdapter.songs.indexOf(song)

            // Notify that a specific item has changed (to update selection state visually)
            songAdapter.notifyItemChanged(position)

            // Log the selected songs' list (for debugging)
            Log.d("MusicPlayer", "Selected songs for playlist: ${selectedSongsIds.joinToString()}")

            // Exit selection mode if no items are selected
            if (selectedSongsIds.isEmpty()) {
                toggleSelectionMode()
            }
        } else {
            // If not in selection mode, play the song as usual
            Log.d("MusicPlayer", "Playing song: ${song.name}")
            musicPlayerViewModel.playSong(song)
            val serviceIntent = Intent(requireContext(), MusicPlayerService::class.java)
            serviceIntent.putExtra("SONG_NAME", song.name)
            requireContext().startService(serviceIntent)

            binding.miniPlayerSeekBar.progress = 0
            musicPlayerViewModel.startProgressUpdater()
        }
    }


    // Update mini player UI
    private fun updateMiniPlayer(song: Song?) {
        if (song != null) {
            binding.miniPlayer.visibility = View.VISIBLE
            binding.miniPlayerSongTitle.text = song.name
            binding.miniPlayerArtist.text = song.artistName

            if (song.albumArt != null) {
                val bitmap = BitmapFactory.decodeByteArray(song.albumArt, 0, song.albumArt.size)
                binding.miniPlayerAlbumArt.setImageBitmap(bitmap)
            } else {
                binding.miniPlayerAlbumArt.setImageResource(R.drawable.player)
            }

            binding.miniPlayerDuration.text = formatTime(song.duration)
            binding.miniPlayerSeekBar.max = song.duration.toInt()
            binding.miniPlayerSeekBar.progress = 0
            musicPlayerViewModel.startProgressUpdater()
        } else {
            binding.miniPlayer.visibility = View.GONE
        }
    }

    // Format time into MM:SS
    private fun formatTime(milliseconds: Long): String {
        val seconds = (milliseconds / 1000) % 60
        val minutes = (milliseconds / 1000) / 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    private fun toggleSelectionMode() {
        selectionMode = !selectionMode
        Log.d("MusicPlayer", "Selection mode toggled. Current state: $selectionMode")

        // Update adapter's selection mode
        songAdapter.selectionMode = selectionMode

        // Show or hide the close selection button
        binding.btnCloseSelection.visibility = if (selectionMode) View.VISIBLE else View.GONE

        // Notify the adapter to refresh the item view states
        songAdapter.notifyDataSetChanged()
    }



    // Handle close selection action
// Handle close selection action
// Handle close selection action
    private fun onCloseSelection() {
        Log.d("HomeFragment", "Close selection triggered") // Log close action

        // Reset the selection mode
        selectionMode = false
        songAdapter.selectionMode = false

        // Deselect all songs and clear the selected songs list
        songAdapter.songs.forEach { it.selected = false }
        selectedSongsIds.clear()

        // Notify the adapter that the selection state has changed
        songAdapter.notifyDataSetChanged()

        // Hide the close selection button
        binding.btnCloseSelection.visibility = View.GONE
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
