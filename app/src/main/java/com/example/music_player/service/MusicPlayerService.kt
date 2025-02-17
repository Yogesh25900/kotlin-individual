package com.example.music_player.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.music_player.R
class MusicPlayerService : Service() {

    private lateinit var notificationManager: NotificationManager
    private val CHANNEL_ID = "music_player_channel"
    private var currentSong: String = "Unknown Song" // Default song name

    override fun onCreate() {
        super.onCreate()

        // Create Notification Channel (required for Android 8.0 and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Music Player",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        // Initial notification creation
        startForeground(1, createNotification())
    }

    private fun createNotification(): Notification {
        val playPauseAction = PendingIntent.getService(
            this,
            0,
            Intent(this, MusicPlayerService::class.java).apply {
                action = "PLAY_PAUSE"
            },
            PendingIntent.FLAG_IMMUTABLE // Or FLAG_MUTABLE depending on your use case
        )

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Music Player")
            .setContentText(currentSong)  // Dynamically set song name here
            .setSmallIcon(R.drawable.ic_music)
            .addAction(R.drawable.ic_play, "Play/Pause", playPauseAction)
            .setOngoing(true)
            .build()
    }

    // Update the notification with the new song name
    fun updateNotification(songName: String) {
        currentSong = songName
        val notification = createNotification()
        notificationManager.notify(1, notification)  // Update the existing notification
    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        // Logic to play the song goes here
        // For example, when song is played, you can call updateNotification
        val songName = intent.getStringExtra("SONG_NAME")
        if (!songName.isNullOrEmpty()) {
            updateNotification(songName)  // Update notification when song changes
        }

        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}
