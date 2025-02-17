package com.example.music_player.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.music_player.R
import com.example.music_player.model.Song
import com.bumptech.glide.Glide // or any other image loading library



class SongAdapter(var songs: List<Song>, private val onClick: (Song) -> Unit) :
    RecyclerView.Adapter<SongAdapter.SongViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        // Inflate the view from XML layout file
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_song, parent, false)
        return SongViewHolder(view)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.bind(song)
    }

    override fun getItemCount(): Int = songs.size

    inner class SongViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val songNameTextView: TextView = itemView.findViewById(R.id.txtSongName)
        private val artistTextView: TextView = itemView.findViewById(R.id.txtArtistName)
        private val albumArtImageView: ImageView = itemView.findViewById(R.id.imgThumbnail)

        fun bind(song: Song) {
            // Set the text for the song name and artist
            songNameTextView.text = song.name
            artistTextView.text = song.artistName

            // Check if album art is available and set the ImageView accordingly
            song.albumArt?.let {
                Glide.with(itemView.context)
                    .load(it)  // Load byte array into image
                    .placeholder(R.drawable.home)  // Default image
                    .into(albumArtImageView)
            } ?: run {
                // Fallback to a default image if no album art is found
                albumArtImageView.setImageResource(R.drawable.music)
            }

            // Set click listener to handle item click
            itemView.setOnClickListener { onClick(song) }
        }
    }
}
