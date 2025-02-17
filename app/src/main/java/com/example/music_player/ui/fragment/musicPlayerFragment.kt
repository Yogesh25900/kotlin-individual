package com.example.music_player.ui.fragment

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.example.music_player.R
import com.example.music_player.databinding.FragmentMusicPlayerBinding
import com.example.music_player.model.Song
import com.example.music_player.repository.MusicPlayerRepositoryImp
import com.example.music_player.viewModel.MusicPlayerViewModel
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity



class musicPlayerFragment : Fragment() {


        private var _binding: FragmentMusicPlayerBinding? = null
        private val binding get() = _binding!!

        private lateinit var musicPlayerViewModel: MusicPlayerViewModel
        private var isUserSeeking = false

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
        ): View {
            _binding = FragmentMusicPlayerBinding.inflate(inflater, container, false)
            return binding.root
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)

            val repo = MusicPlayerRepositoryImp(requireContext())
            musicPlayerViewModel = MusicPlayerViewModel.getInstance(repo)

            musicPlayerViewModel.currentSong.observe(viewLifecycleOwner) { song ->
                song?.let { updateUI() }
            }

            musicPlayerViewModel.songProgress.observe(viewLifecycleOwner) { progress ->
                if (!isUserSeeking) {
                    binding.seekBar.progress = progress.toInt()
                    binding.txtStartDuration.text = formatTime(progress)
                }
            }

            binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
                override fun onProgressChanged(seekBar: SeekBar, progress: Int, fromUser: Boolean) {
                    if (fromUser) {
                        isUserSeeking = true
                        binding.txtStartDuration.text = formatTime(progress.toLong())
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

            musicPlayerViewModel.isPlaying.observe(viewLifecycleOwner) { isPlaying ->
                binding.fabPlayPause.setImageResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play)
            }

            binding.fabPlayPause.setOnClickListener {
                if (musicPlayerViewModel.isPlaying.value == true) {
                    musicPlayerViewModel.pauseSong()
                } else {
                    musicPlayerViewModel.currentSong.value?.let { song ->
                        musicPlayerViewModel.resumeSong()  // Resume playback
                    }
                }
            }

            binding.btnNext.setOnClickListener {
                musicPlayerViewModel.nextSong()
                Toast.makeText(requireContext(), "Next button clicked", Toast.LENGTH_LONG).show()
            }

            binding.btnPrevious.setOnClickListener {
                musicPlayerViewModel.previousSong()
                Toast.makeText(requireContext(), "Previous button clicked", Toast.LENGTH_LONG)
                    .show()
            }
        }

        override fun onDestroyView() {
            super.onDestroyView()
            _binding = null
        }

        private fun updateUI() {
            musicPlayerViewModel.getCurrentSong()?.let { song ->
                binding.txtSongTitle.text = song.name
                binding.txtArtistName.text = song.artistName

                song.albumArt?.let {
                    val bitmap = BitmapFactory.decodeByteArray(it, 0, it.size)
                    binding.imgThumbnail.setImageBitmap(bitmap)
                } ?: binding.imgThumbnail.setImageResource(R.drawable.music)

                binding.seekBar.max = song.duration.toInt()
                binding.txtEndDuration.text = formatTime(song.duration)
            }
        }

        private fun formatTime(milliseconds: Long): String {
            val minutes = (milliseconds / 1000) / 60
            val seconds = (milliseconds / 1000) % 60
            return String.format("%02d:%02d", minutes, seconds)
        }

    }
