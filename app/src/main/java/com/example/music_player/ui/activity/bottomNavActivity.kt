package com.example.music_player.ui.activity

import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.Fragment
import com.example.music_player.R
import com.example.music_player.databinding.ActivityBottomNavBinding
import com.example.music_player.ui.fragment.PlaylistFragment
import com.example.music_player.ui.fragment.homeFragment
import com.example.music_player.ui.fragment.loginFragment
import com.example.music_player.ui.fragment.musicPlayerFragment
import com.example.music_player.ui.fragment.onlineSongFragment
import com.example.music_player.ui.fragment.profileFragment
import com.example.music_player.ui.fragment.signupFragment
// Corrected class name (ProfileFragment)
import com.example.music_player.utils.PermissionUtils

class bottomNavActivity : AppCompatActivity() {  // Corrected class name (BottomNavActivity)
    private lateinit var binding: ActivityBottomNavBinding

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allGranted = permissions.all { it.value }
            if (!allGranted) {
                Toast.makeText(this, "Permission denied. Cannot fetch songs.", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Permission Allowed.", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityBottomNavBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Check and request permissions if necessary
        if (!PermissionUtils.hasPermissions(this)) {
            PermissionUtils.requestPermissions(permissionLauncher)
        }

        // Set up the Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.apply {
            title = "Music Player"
            setDisplayHomeAsUpEnabled(false) // If you want a back button, set to true
        }

        // Set up the BottomNavigationView listener
        binding.bottomNavigation.setOnItemSelectedListener {
            val selectedFragment: Fragment = when (it.itemId) {
                R.id.nav_home -> homeFragment()  // Corrected fragment class name
                R.id.nav_search -> musicPlayerFragment()  // Corrected fragment class name
                R.id.nav_profile -> PlaylistFragment()  // Corrected fragment class name
                R.id.nav_user -> onlineSongFragment()
                else -> homeFragment()  // Default to homeFragment
            }

            // Replace fragment using the helper function
            replaceFragment(selectedFragment)

            true
        }

        // Set the default fragment when the activity is first loaded
        if (savedInstanceState == null) {
            binding.bottomNavigation.selectedItemId = R.id.nav_home
        }

        // Apply window insets for edge-to-edge support
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    // Helper method to replace the fragment
     fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.framelayout, fragment)  // Ensure frameLayout is the correct container ID
            .commit()
    }
}
