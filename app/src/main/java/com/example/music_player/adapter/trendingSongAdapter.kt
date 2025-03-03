package com.example.music_player.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.music_player.R
import com.example.music_player.model.trendySong
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.music_player.utils.BorderTransformation

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
        private val title: TextView = itemView.findViewById(R.id.songTitle)
        private val image: ImageView = itemView.findViewById(R.id.songImage)

        fun bind(podcast: trendySong) {
            title.text = podcast.title

            // Use Glide to load the image and apply circular transformation
            Glide.with(itemView.context)
                .load(podcast.imageRes)  // Assuming imageRes is a URL or resource ID
                .transform(BorderTransformation(10f, Color.RED))  // Add border with width 10px and color RED

                .apply(RequestOptions.circleCropTransform())  // Apply the circular crop transformation
                .into(image)
        }
    }
}
