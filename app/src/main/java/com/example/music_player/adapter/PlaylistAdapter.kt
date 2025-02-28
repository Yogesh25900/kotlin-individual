package com.example.music_player.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.music_player.R
import com.example.music_player.model.Playlist

class PlaylistAdapter(
    private val onItemClick: (Playlist) -> Unit, // Item click listener for the whole item
    private val onOpenPlaylistClick: (String) -> Unit // Button click listener for opening playlist
) : ListAdapter<Playlist, PlaylistAdapter.PlaylistViewHolder>(PlaylistDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PlaylistViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_playlist, parent, false)
        return PlaylistViewHolder(view, onOpenPlaylistClick) // Pass the onOpenPlaylistClick to the ViewHolder
    }

    override fun onBindViewHolder(holder: PlaylistViewHolder, position: Int) {
        val playlist = getItem(position)
        holder.bind(playlist)

        holder.itemView.setOnClickListener {
            onItemClick(playlist)  // Call the lambda when the whole item is clicked
        }
    }

    class PlaylistViewHolder(itemView: View, private val onOpenPlaylistClick: (String) -> Unit) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.tvPlaylistName)
        private val btn: Button = itemView.findViewById(R.id.openPlaylistButton)

        fun bind(playlist: Playlist) {
            nameTextView.text = playlist.name

            // Set the click listener for the button
            btn.setOnClickListener {
                playlist.id?.let { id ->
                    // Invoke the callback with the playlist id when the button is clicked
                    onOpenPlaylistClick(id)
                }
            }
        }
    }

    class PlaylistDiffCallback : DiffUtil.ItemCallback<Playlist>() {
        override fun areItemsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Playlist, newItem: Playlist): Boolean {
            return oldItem == newItem
        }
    }
}
