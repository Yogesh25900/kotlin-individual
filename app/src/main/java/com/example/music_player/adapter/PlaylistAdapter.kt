package com.example.music_player.adapter

import android.content.Context
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

    class PlaylistViewHolder(
        itemView: View,
        private val onOpenPlaylistClick: (String) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {
        private val nameTextView: TextView = itemView.findViewById(R.id.tvPlaylistName)
        private val openButton: Button = itemView.findViewById(R.id.openPlaylistButton)

        fun bind(playlist: Playlist) {
            nameTextView.text = playlist.name

            // Set the click listener for the open button
            openButton.setOnClickListener {
                playlist.id?.let { id ->
                    onOpenPlaylistClick(id)  // Open the playlist when the button is clicked
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

    // Method to show a delete confirmation dialog when swiped
    fun showDeleteConfirmationDialog(context: Context, playlistId: String, position: Int, onConfirm: () -> Unit,    onCancel: () -> Unit
    ) {
        android.app.AlertDialog.Builder(context)
            .setTitle("Delete Playlist")
            .setMessage("Are you sure you want to delete this playlist?")
            .setPositiveButton("Yes") { _, _ ->
                onConfirm()  // Call the confirm deletion callback
            }
            .setNegativeButton("No") { dialog, _ ->
                // Notify adapter that the swipe was canceled
                onCancel()

                notifyItemChanged(position)

                dialog.dismiss()
            }
            .show()
    }
}
