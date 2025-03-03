package com.example.music_player.ui.fragment

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.PopupMenu
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.music_player.R
import com.example.music_player.adapter.OnlineSongAdapter
import com.example.music_player.adapter.trendingSongAdapter
import com.example.music_player.databinding.FragmentOnlineSongBinding
import com.example.music_player.model.onlineSong
import com.example.music_player.model.trendySong
import com.example.music_player.repository.MusicPlayerRepositoryImp
import com.example.music_player.repository.userAuthRepositoryImp
import com.example.music_player.viewModel.OnlineSongViewModel
import com.example.music_player.viewModel.userAuthViewModel
import kotlin.math.log


class onlineSongFragment : Fragment() {

    private lateinit var binding: FragmentOnlineSongBinding // Declare the binding
    private lateinit var songAdapter: OnlineSongAdapter
    private lateinit var songViewModel: OnlineSongViewModel
    private lateinit var biggestHitsView: RecyclerView
    lateinit var greeting: TextView
    lateinit var userViewModel: userAuthViewModel
    private var mediaPlayer: MediaPlayer? = null
    private var userId: String = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout using ViewBinding
        binding = FragmentOnlineSongBinding.inflate(inflater, container, false)

        // Initialize RecyclerView for Online Songs
        binding.recyclerViewOnlineSongs.layoutManager = LinearLayoutManager(requireContext())

        // Find RecyclerView for Biggest Hits and set layout
        biggestHitsView = binding.biggestHitsRecyclerView
        biggestHitsView.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        // Example song list for trending songs
        val songList = listOf(
            trendySong("Tu hai toh", R.drawable.tuhaitoh),
            trendySong("Arijit Hits", R.drawable.arijit),
            trendySong("Tum mile", R.drawable.tummile),
            trendySong("Maahi", R.drawable.maahi),
            trendySong("Perfect", R.drawable.perfect),
            trendySong("Ajab Si", R.drawable.ajabsi),
            trendySong("RIHA", R.drawable.riha),
            trendySong("Afsos", R.drawable.afsos),
            trendySong("Jo Tum mere ho", R.drawable.jotummereho)
        )

        // Set Adapter for the RecyclerView
        val adapter = trendingSongAdapter(songList)
        biggestHitsView.adapter = adapter

        userViewModel = userAuthViewModel(userAuthRepositoryImp())

        // Observe user details
        userViewModel.userDetails.observe(viewLifecycleOwner, Observer { userDetails ->
            userDetails?.let {
                val userId = userViewModel.userUid.value ?: ""

                // Only update the UI if there's a valid userId
                if (userId.isNotEmpty()) {
                    val userInfo = it[userId] as? Map<String, String>

                    if (userInfo != null) {
                        val userName = userInfo["name"] ?: "Unknown"
                        val userEmail = userInfo["email"] ?: "No email"

                        // Update greeting text only if the username has changed
                        if (binding.greetingText.text != "Hello, $userName! 👋") {
                            binding.greetingText.text = "Hello, $userName! 👋"
                        }
                    }
                }
            }
        })

        // Observe user ID (UID)
        userViewModel.userUid.observe(viewLifecycleOwner, Observer { uid ->
            if (uid.isNotEmpty()) {
                userId = uid
                Log.d("userid", uid)

            } else {
                Log.d("userid", "UID is empty")
            }
        })

        // Trigger `getCurrentUser()` and `fetchUserDetailsById()`
        userViewModel.getCurrentUser()
        userViewModel.fetchUserDetailsById(userId)

        songViewModel = OnlineSongViewModel(MusicPlayerRepositoryImp(requireContext()))

        // Initialize Adapter
        songAdapter = OnlineSongAdapter(
            onClick = { song -> playSong(song) },
            onCloseSelection = { closeSelection() },
            songViewModel = songViewModel
        )

        // Set the RecyclerView with the song adapter
        binding.recyclerViewOnlineSongs.adapter = songAdapter

        // Fetch songs from the repository
        songViewModel.fetchSongs()

        // Observe error LiveData for songs
        songViewModel.error.observe(viewLifecycleOwner, Observer { exception ->
            exception?.let {
                Toast.makeText(requireContext(), "Error fetching songs: ${it.message}", Toast.LENGTH_SHORT).show()
            }
        })

        // Handle profile image click and show menu
        binding.profileImage.setOnClickListener {
            val popupMenu = PopupMenu(requireContext(), binding.profileImage)
            val inflater = popupMenu.menuInflater
            inflater.inflate(R.menu.profile_menu, popupMenu.menu)

            // Handle menu item clicks
            popupMenu.setOnMenuItemClickListener { item ->
                when (item.itemId) {
                    R.id.edit_profile -> {
                        Toast.makeText(requireContext(), "Edit Profile", Toast.LENGTH_SHORT).show()
                        true
                    }
                    R.id.logout -> {
                        Toast.makeText(requireContext(), "Logout", Toast.LENGTH_SHORT).show()
                        userViewModel.logout()
                        clearLoginStatus()
                        replaceFragment(loginFragment()) // Assuming replaceFragment handles fragment replacement
                        true
                    }
                    else -> false
                }
            }

            // Show the menu
            popupMenu.show()
        }

        return binding.root
    }

    private fun playSong(song: onlineSong) {
        stopMediaPlayer() // Stop any currently playing song before playing a new one

        if (song.songUrl.isEmpty()) {
            Toast.makeText(requireContext(), "Invalid song URL", Toast.LENGTH_SHORT).show()
            return
        }

        mediaPlayer = MediaPlayer().apply {
            setDataSource(requireContext(), Uri.parse(song.songUrl))
            setOnPreparedListener { start() }
            setOnCompletionListener {
                Toast.makeText(requireContext(), "Song finished playing", Toast.LENGTH_SHORT).show()
            }
            setOnErrorListener { _, what, extra ->
                Toast.makeText(requireContext(), "MediaPlayer error: $what, $extra", Toast.LENGTH_SHORT).show()
                false
            }
            prepareAsync()
        }
    }

    private fun stopMediaPlayer() {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onDestroyView() {
        super.onDestroyView()
        stopMediaPlayer() // Release resources when fragment is destroyed
    }
    override fun onStart() {
        super.onStart()

        // Check if the user is logged in
        val sharedPreferences = requireActivity().getSharedPreferences("userLogPrefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        if (!isLoggedIn) {
            // Redirect to LoginFragment
            replaceFragment(loginFragment())
        }
    }
    override fun onResume() {
        super.onResume()

        // Check if the user is logged in
        val sharedPreferences = requireActivity().getSharedPreferences("userLogPrefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("isLoggedIn", false)

        if (!isLoggedIn) {
            // Redirect to LoginFragment
            replaceFragment(loginFragment()) // Assuming replaceFragment is your method to handle fragment transactions
        }
    }

    private fun closeSelection() {
        // Handle closing selection mode
    }

    private fun clearLoginStatus() {
        val sharedPreferences = requireActivity().getSharedPreferences("userLogPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.clear() // Clears all stored preferences (or use editor.remove("key") to remove specific entries)
        editor.apply() // Apply the changes
    }

    private fun replaceFragment(fragment: Fragment) {
        val fragmentTransaction = parentFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.framelayout, fragment) // Replace the container with LoginFragment
        fragmentTransaction.addToBackStack(null) // Optional: if you want to add it to the back stack
        fragmentTransaction.commit()
    }
}


