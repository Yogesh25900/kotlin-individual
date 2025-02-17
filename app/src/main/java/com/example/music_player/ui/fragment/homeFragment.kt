package com.example.music_player.ui.fragment

import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.music_player.R
import com.example.music_player.adapter.SongAdapter
import com.example.music_player.databinding.FragmentHomeBinding
import com.example.music_player.repository.MusicPlayerRepositoryImp
import com.example.music_player.service.MusicPlayerService
import com.example.music_player.viewModel.MusicPlayerViewModel

class homeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!  // Safe access to binding

    private lateinit var musicPlayerViewModel: MusicPlayerViewModel
    private lateinit var songAdapter: SongAdapter

    private var isUserSeeking = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val repo = MusicPlayerRepositoryImp(requireContext())
        musicPlayerViewModel = MusicPlayerViewModel.getInstance(repo)

        // Initialize RecyclerView with an empty list of songs initially
        songAdapter = SongAdapter(emptyList()) { song ->
            musicPlayerViewModel.playSong(song)  // Handle song click

            val serviceIntent = Intent(requireContext(), MusicPlayerService::class.java)
            serviceIntent.putExtra("SONG_NAME", song.name)  // Pass the song name
            requireContext().startService(serviceIntent)  // Start the service or update it

            // Reset SeekBar to 0
            binding.miniPlayerSeekBar.progress = 0
            musicPlayerViewModel.startProgressUpdater()  // Start updating the SeekBar
        }

        binding.songRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.songRecyclerView.adapter = songAdapter

        // Observe the song list from ViewModel
        musicPlayerViewModel.songs.observe(viewLifecycleOwner) { songs ->
            songAdapter.songs = songs
            songAdapter.notifyDataSetChanged()  // Update RecyclerView with new songs

        }

        // Observe current song changes
        musicPlayerViewModel.currentSong.observe(viewLifecycleOwner) { song ->
            if (song != null) {
                binding.miniPlayer.visibility = View.VISIBLE
                binding.miniPlayerSongTitle.text = song.name
                binding.miniPlayerArtist.text = song.artistName

                // Load album art if available
                if (song.albumArt != null) {
                    val bitmap = BitmapFactory.decodeByteArray(song.albumArt, 0, song.albumArt.size)
                    binding.miniPlayerAlbumArt.setImageBitmap(bitmap)
                } else {
                    binding.miniPlayerAlbumArt.setImageResource(R.drawable.player)
                }

                // Set total duration text
                binding.miniPlayerDuration.text = formatTime(song.duration)
                binding.miniPlayerSeekBar.max = song.duration.toInt()

                // Reset SeekBar to 0
                binding.miniPlayerSeekBar.progress = 0

                // Start updating progress from ViewModel
                musicPlayerViewModel.startProgressUpdater()
            } else {
                binding.miniPlayer.visibility = View.GONE
            }
        }


        // Observe the play/pause state
        musicPlayerViewModel.isPlaying.observe(viewLifecycleOwner) { isPlaying ->
            binding.miniPlayerPlayPause.setImageResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play)
        }

        binding.miniPlayerPlayPause.setOnClickListener {
            if (musicPlayerViewModel.isPlaying.value == true) {
                musicPlayerViewModel.pauseSong()  // Pause the song
            } else {
                musicPlayerViewModel.currentSong.value?.let { song ->
                    musicPlayerViewModel.resumeSong()  // Resume playback
                }
            }
        }

        // Observe song progress and update SeekBar
        musicPlayerViewModel.songProgress.observe(viewLifecycleOwner, Observer { progress ->
            if (!isUserSeeking) {
                binding.miniPlayerSeekBar.progress = progress.toInt()
                binding.miniPlayerCurrentTime.text = formatTime(progress)  // Format the current time
            }
        })

        binding.miniPlayerSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    isUserSeeking = true
                    binding.miniPlayerCurrentTime.text = formatTime(progress.toLong())  // Show current time in formatted way
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
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // Prevent memory leaks
    }

    fun formatTime(milliseconds: Long): String {
        val seconds = (milliseconds / 1000) % 60
        val minutes = (milliseconds / 1000) / 60
        return String.format("%02d:%02d", minutes, seconds)
    }
}
