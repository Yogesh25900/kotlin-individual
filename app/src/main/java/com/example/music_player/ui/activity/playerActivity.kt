package com.example.music_player.ui.activity

import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.example.music_player.R
import com.example.music_player.databinding.ActivityPlayerBinding
import com.example.music_player.repository.MusicPlayerRepositoryImp
import com.example.music_player.viewModel.MusicPlayerViewModel

class playerActivity : AppCompatActivity() {

    private lateinit var musicPlayerViewModel: MusicPlayerViewModel
    private var _binding: ActivityPlayerBinding? = null
    private val binding get() = _binding!!

    private var isUserSeeking = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val repo = MusicPlayerRepositoryImp(this)
        musicPlayerViewModel = MusicPlayerViewModel.getInstance(repo)

        musicPlayerViewModel.currentSong.observe(this) { song ->
            song?.let { updateUI() }
        }


        // Observe song progress and update SeekBar
        musicPlayerViewModel.songProgress.observe(this, Observer { progress ->
            if (!isUserSeeking) {
                binding.seekBar.progress = progress.toInt()
                binding.txtStartDuration.text = formatTime(progress)
            }
        })

        // SeekBar change listener
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

        // Observe play/pause state
        musicPlayerViewModel.isPlaying.observe(this) { isPlaying ->
            binding.fabPlayPause.setImageResource(if (isPlaying) R.drawable.ic_pause else R.drawable.ic_play)
        }

        binding.fabPlayPause.setOnClickListener {
            if (musicPlayerViewModel.isPlaying.value == true) {
                musicPlayerViewModel.pauseSong()
            } else {
                musicPlayerViewModel.resumeSong()
            }
        }

        binding.btnNext.setOnClickListener {
            musicPlayerViewModel.nextSong()

            Toast.makeText(this,"next button clicked",Toast.LENGTH_LONG).show()

        }

        binding.btnPrevious.setOnClickListener {
            musicPlayerViewModel.previousSong()

            Toast.makeText(this,"previous  button clicked",Toast.LENGTH_LONG).show()

        }
    }

    override fun onDestroy() {
        super.onDestroy()
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
