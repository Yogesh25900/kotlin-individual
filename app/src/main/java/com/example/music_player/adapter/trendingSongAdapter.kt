package com.example.music_player.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.music_player.R
import com.example.music_player.model.trendySong

class trendingSongAdapter(private val podcasts: List<trendySong>) :
        RecyclerView.Adapter<trendingSongAdapter.PodcastViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PodcastViewHolder {
            val view = LayoutInflater.from(parent.context).inflate(R.layout.item_trendysong, parent, false)
            return PodcastViewHolder(view)
        }

        override fun onBindViewHolder(holder: PodcastViewHolder, position: Int) {
            val podcast = podcasts[position]
            holder.bind(podcast)
        }

        override fun getItemCount() = podcasts.size

        class PodcastViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val title: TextView = itemView.findViewById(R.id.podcastTitle)
            private val image: ImageView = itemView.findViewById(R.id.podcastImage)

            fun bind(podcast: trendySong) {
                title.text = podcast.title
                image.setImageResource(podcast.imageRes)
            }
        }
    }
