package com.example.music_player.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.music_player.R
import com.example.music_player.model.Song
import com.bumptech.glide.Glide
import com.example.music_player.viewModel.MusicPlayerViewModel

class SongAdapter(
    var songs: List<Song>,
    private val onClick: (Song) -> Unit,
    private val onCloseSelection: () -> Unit,
    private val songViewModel: MusicPlayerViewModel // Inject ViewModel
) : RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    var selectionMode = false // Tracks selection mode
    private val selectedSongsIds = mutableListOf<String>() // List to track selected song IDs

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]

        // Regular click logic (plays the song)
        holder.itemView.setOnClickListener {
            if (selectionMode) {
                song.selected = !song.selected
                holder.updateSelectionUI(song)  // Update the selection UI
                if (song.selected) {
                    selectedSongsIds.add(song.id) // Add song to selected list
                } else {
                    selectedSongsIds.remove(song.id) // Remove song from selected list
                }
                updateSelectedSongs() // Update ViewModel with selected songs
                notifyItemChanged(position)

                // Exit selection mode if no songs are selected
                if (selectedSongsIds.isEmpty()) {
                    closeSelectionMode()
                }
            } else {
                onSongClick(song)  // Regular click behavior (e.g., play song)
            }
        }

        // Long press logic for toggling selection mode
        holder.itemView.setOnLongClickListener {
            Log.d("MusicPlayer", "Long press detected for song: ${song.name}")
            toggleSelectionMode(song)  // Toggle selection mode on long press
            true // Indicating the long click was handled
        }

        // Bind song data
        holder.bind(song)
    }

    override fun getItemCount(): Int = songs.size

    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val songNameTextView: TextView = itemView.findViewById(R.id.txtPlaylistName)
        private val artistTextView: TextView = itemView.findViewById(R.id.txtArtistName)
        private val albumArtImageView: ImageView = itemView.findViewById(R.id.imgThumbnail)
        private val selectCheckBox: ImageView = itemView.findViewById(R.id.imgSelect)

        fun bind(song: Song) {
            songNameTextView.text = song.name
            artistTextView.text = song.artistName

            Glide.with(itemView.context)
                .load(song.albumArt ?: R.drawable.music) // Default image if null
                .placeholder(R.drawable.home)
                .into(albumArtImageView)

            // Show or hide the selection checkbox based on the selection mode
            selectCheckBox.visibility = if (selectionMode) View.VISIBLE else View.GONE
            updateSelectionUI(song) // Update the selection UI based on the song's state
        }

        // Updates the checkbox UI to show whether the song is selected or not
        fun updateSelectionUI(song: Song) {
            selectCheckBox.setImageResource(
                if (song.selected) R.drawable.uncheck else R.drawable.checkbox
            )
        }
    }

    // Toggle selection mode on long press
    private fun toggleSelectionMode(song: Song) {
        if (!selectionMode) {
            // Enter selection mode and mark the song as selected
            selectionMode = true
            song.selected = true
            selectedSongsIds.add(song.id) // Add to selected list
            Log.d("MusicPlayer", "Entering selection mode: ${song.name}")
            updateSelectedSongs() // Update ViewModel with selected songs
            notifyDataSetChanged()
        }
    }

    // Close selection mode and reset selection states
    fun closeSelectionMode() {
        selectionMode = false
        songs.forEach { it.selected = false }  // Reset selection state
        selectedSongsIds.clear() // Clear the selected songs list
        notifyDataSetChanged()
        onCloseSelection() // Trigger the callback to close selection
    }

    // Update selected songs in ViewModel
    private fun updateSelectedSongs() {
        val selectedSongIds = songs.filter { it.selected }.map { it.id }  // Extract IDs of selected songs
        songViewModel.updateSelectedSongs(selectedSongIds)  // Pass the IDs to ViewModel
    }


    // Update songs list and refresh the adapter
    fun updateSongs(newSongs: List<Song>) {
        songs = newSongs
        notifyDataSetChanged()
    }

    // Get selected songs IDs
    fun getSelectedSongs(): MutableList<String> = selectedSongsIds

    // Handle song click (play song)
    private fun onSongClick(song: Song) {
        Log.d("MusicPlayer", "Playing song: ${song.name}")
        onClick(song) // Invoke the onClick callback passed into the adapter
    }
}
