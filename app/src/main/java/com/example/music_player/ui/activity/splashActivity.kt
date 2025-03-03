package com.example.music_player.ui.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.airbnb.lottie.LottieAnimationView
import com.example.music_player.R

class splashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        val lottieView = findViewById<LottieAnimationView>(R.id.lottieView)

        // Load Lottie animation from raw folder
        lottieView.setAnimation(R.raw.musicani) // Replace with your actual file name
        lottieView.loop(true)  // Loop the animation indefinitely
        lottieView.playAnimation()  // Start animation

        // Delay for 3 seconds before switching to BottomNavActivity
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this@splashActivity, bottomNavActivity::class.java))
            finish() // Finish SplashActivity so user can't go back to it
        }, 3000) // 3000 milliseconds = 3 seconds

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }
}
