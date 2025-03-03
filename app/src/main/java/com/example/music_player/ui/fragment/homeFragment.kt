package com.example.music_player.ui.fragment

import android.Manifest
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.music_player.R
import com.example.music_player.adapter.SongAdapter
import com.example.music_player.databinding.FragmentHomeBinding
import com.example.music_player.model.Song
import com.example.music_player.repository.MusicPlayerRepositoryImp
import com.example.music_player.service.MusicPlayerService
import com.example.music_player.utils.PermissionUtils
import com.example.music_player.viewModel.MusicPlayerViewModel

class homeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private val REQUEST_CODE_STORAGE_PERMISSION = 1001

    private lateinit var musicPlayerViewModel: MusicPlayerViewModel
    private lateinit var songAdapter: SongAdapter

    private var isUserSeeking = false
    private var selectionMode = false

    private val selectedSongsIds = mutableListOf<String>() // Track selected song IDs
    lateinit  var selectedSonglist: List<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        setHasOptionsMenu(true);

        setupItemTouchHelper()


        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        // Inflate the menu
        inflater.inflate(R.menu.toolbar_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.nav_home -> {
                // Handle menu item click
                openPlaylistFragment()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
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

        // Obser
        //
        // ve song list
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


        musicPlayerViewModel.selectedSongs.observe(viewLifecycleOwner, Observer { songIds ->
            // Update UI based on the list of selected song IDs
            // For example, updating an adapter or showing a list of selected songs
            selectedSonglist = songIds
            Log.d("YourFragment", "Selected songs updated from home: $songIds")

            // You can use this list of song IDs to fetch the full Song objects if needed,
            // or just update your UI directly with the list of IDs.
        })

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


            musicPlayerViewModel.selectedSongs.observe(viewLifecycleOwner, Observer { selectedSongs ->
                // Handle the updated list of selected songs
                // For example, update the UI with the selected songs
                Log.d("MusicPlayer", "Selected songs for playlist from home: ${selectedSongs.joinToString()}")

            })
            // Log the selected songs' list (for debugging)

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

    private fun openPlaylistFragment() {

        val bundle = Bundle().apply {
            putStringArrayList("selectedSongIds", ArrayList(selectedSonglist))
        }

        val playlistFragment = PlaylistFragment().apply {
            arguments = bundle
        }

        parentFragmentManager.beginTransaction()
            .replace(R.id.framelayout, playlistFragment)
            .addToBackStack(null)
            .commit()
    }


    private fun setupItemTouchHelper() {
        val itemTouchHelperCallback = object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean = false
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.adapterPosition
                val songPath = songAdapter.songs[position].path

                if (!PermissionUtils.hasPermissions(requireContext())) {
                    PermissionUtils.requestManageExternalStoragePermission(requireContext())
                    Toast.makeText(requireContext(), "Please allow storage access to delete songs", Toast.LENGTH_LONG).show()
                    return
                }

                // Show confirmation dialog before deleting
                val context = requireContext()
                AlertDialog.Builder(context)
                    .setTitle("Delete Song")
                    .setMessage("Are you sure you want to delete this song?")
                    .setPositiveButton("Yes") { _, _ ->
                        // Proceed with deletion
                        songAdapter.deleteSong(position)  // Delete song and refresh RecyclerView
                    }
                    .setNegativeButton("No") { _, _ ->
                        // If user clicks "No", notify RecyclerView and reset the swipe position
                        songAdapter.notifyItemChanged(position)
                        // Optionally, you can reset the translation to ensure the item isn't swiped off the screen
                        viewHolder.itemView.translationX = 0f
                    }
                    .show()
            }


            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)

                val itemView = viewHolder.itemView

                // Only apply background if the item is swiped to the left (negative dX)
                if (dX < 0) {
                    val background = ColorDrawable(Color.RED)
                    background.setBounds(itemView.left + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
                    background.draw(c)
                }

                // Optionally, you can draw a delete icon or text on top of the red background:
                if (dX < 0) {
                    val deleteIcon = ContextCompat.getDrawable(recyclerView.context, R.drawable.baseline_delete_outline_24) // your delete icon
                    deleteIcon?.let {
                        val iconMargin = (itemView.height - it.intrinsicHeight) / 2
                        val iconLeft = itemView.right - iconMargin - it.intrinsicWidth
                        val iconRight = itemView.right - iconMargin
                        val iconTop = itemView.top + (itemView.height - it.intrinsicHeight) / 2
                        val iconBottom = iconTop + it.intrinsicHeight
                        it.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                        it.draw(c)
                    }
                }
            }

        }

        ItemTouchHelper(itemTouchHelperCallback).attachToRecyclerView(binding.songRecyclerView)
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
