package com.example.music_player.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.music_player.R
import com.example.music_player.model.onlineSong
import com.example.music_player.viewModel.OnlineSongViewModel

class OnlineSongAdapter(
    private val onClick: (onlineSong) -> Unit,
    private val onCloseSelection: () -> Unit,
    private val songViewModel: OnlineSongViewModel
) : RecyclerView.Adapter<OnlineSongAdapter.SongViewHolder>() {

    var selectionMode = false
    private val selectedSongsIds = mutableListOf<String>()
    private var songs: List<onlineSong> = emptyList()

    init {
        songViewModel.songList.observeForever { updatedSongs ->
            songs = updatedSongs
            notifyDataSetChanged()
        }
    }

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
                holder.updateSelectionUI(song)
                if (song.selected) {
                    selectedSongsIds.add(song.songId)
                } else {
                    selectedSongsIds.remove(song.songId)
                }
                notifyItemChanged(position)

                // Exit selection mode if no songs are selected
                if (selectedSongsIds.isEmpty()) {
                    closeSelectionMode()
                }
            } else {
                onSongClick(song)
            }
        }

        // Long press logic for toggling selection mode
        holder.itemView.setOnLongClickListener {
            Log.d("MusicPlayer", "Long press detected for song: ${song.songTitle}")
            toggleSelectionMode(song)
            true
        }

        holder.bind(song)
    }

    override fun getItemCount(): Int = songs.size

    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val songNameTextView: TextView = itemView.findViewById(R.id.txtPlaylistName)
        private val artistTextView: TextView = itemView.findViewById(R.id.txtArtistName)
        private val albumArtImageView: ImageView = itemView.findViewById(R.id.imgThumbnail)
        private val selectCheckBox: ImageView = itemView.findViewById(R.id.imgSelect)

        fun bind(song: onlineSong) {
            songNameTextView.text = song.songTitle
            artistTextView.text = song.artist ?: "Unknown Artist"

            Glide.with(itemView.context)
                .load(song.albumArt ?: R.drawable.music)
                .placeholder(R.drawable.home)
                .into(albumArtImageView)

            selectCheckBox.visibility = if (selectionMode) View.VISIBLE else View.GONE
            updateSelectionUI(song)
        }

        fun updateSelectionUI(song: onlineSong) {
            selectCheckBox.setImageResource(
                if (song.selected) R.drawable.uncheck else R.drawable.checkbox
            )
        }
    }

    private fun toggleSelectionMode(song: onlineSong) {
        if (!selectionMode) {
            selectionMode = true
            song.selected = true
            selectedSongsIds.add(song.songId)
            notifyDataSetChanged()
        }
    }

    fun closeSelectionMode() {
        selectionMode = false
        songs.forEach { it.selected = false }
        selectedSongsIds.clear()
        notifyDataSetChanged()
        onCloseSelection()
    }

    fun getSelectedSongs(): MutableList<String> = selectedSongsIds

    private fun onSongClick(song: onlineSong) {
        Log.d("MusicPlayer", "Playing song: ${song.songTitle}")
        onClick(song)
    }
}
