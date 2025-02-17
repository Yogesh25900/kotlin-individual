package com.example.music_player

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.cloudinary.android.MediaManager
import com.cloudinary.android.callback.ErrorInfo
import com.cloudinary.android.callback.UploadCallback
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    private lateinit var reference: DatabaseReference
    private lateinit var uploadButton: Button

    // Declare a constant for the file picker request code
    private val PICK_MUSIC_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize Firebase reference to the "songs" node
        reference = FirebaseDatabase.getInstance().getReference("songs")

        // Initialize the upload button
        uploadButton = findViewById(R.id.btnUploadMusic)

        // Initialize Cloudinary with your credentials
        val config = hashMapOf<String, String>(
            "cloud_name" to "drykew7pu",   // Your Cloudinary cloud name
            "api_key" to "891342176588327", // Your Cloudinary API key
            "api_secret" to "-7N8kuVvR0FNLLPYFModBB_03UM" // Your Cloudinary API secret
        )
        MediaManager.init(this, config)

        // Set onClickListener to trigger music upload
        uploadButton.setOnClickListener {
            // Open file manager to choose a music file
            openFilePicker()
        }
    }

    // Open the file picker to choose a music file
    private fun openFilePicker() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "audio/*"  // Filter for audio files (MP3, WAV, etc.)
        startActivityForResult(intent, PICK_MUSIC_REQUEST_CODE)
    }


    // Handle the result of the file picker
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_MUSIC_REQUEST_CODE && resultCode == RESULT_OK) {
            // Get the URI of the selected music file
            val musicUri: Uri? = data?.data

            if (musicUri != null) {
                // If URI is not null, proceed to upload the song to Cloudinary
                uploadSongAsVideoToCloudinary(musicUri)
            } else {
                Toast.makeText(this, "Please select a song to upload", Toast.LENGTH_SHORT).show()
            }
        }
    }



    private fun uploadSongAsVideoToCloudinary(musicUri: Uri) {
        try {
            // Upload the audio file as a video to Cloudinary
            val uploadRequest = MediaManager.get().upload(musicUri)
                .option("resource_type", "video")  // Treat the audio file as a video
                .callback(object : UploadCallback {

                    override fun onStart(requestId: String) {
                        // Optional: Show loading indicator
                    }

                    override fun onProgress(requestId: String, bytes: Long, totalBytes: Long) {
                        // Optional: Update progress bar if you need to show upload progress
                    }

                    override fun onSuccess(requestId: String, resultData: Map<*, *>) {
                        val songUrl = resultData["secure_url"] as? String
                        Log.d("Cloudinary", "Audio upload successful as video: $songUrl")

                        // If the upload is successful, save the audio URL (or perform other actions)
                        if (songUrl != null) {
//                            saveSongToFirebase(songUrl)
                        }
                    }

                    override fun onError(requestId: String?, error: ErrorInfo?) {
                        // Handle error during upload
                        Log.e("Cloudinary", "Error uploading audio: ${error?.description}")
                        Toast.makeText(
                            this@MainActivity,
                            "Error uploading audio",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    override fun onReschedule(requestId: String?, error: ErrorInfo?) {
                        // Handle rescheduling if needed
                    }
                })

            // Dispatch the upload request to Cloudinary
            uploadRequest.dispatch()

        } catch (e: Exception) {
            Log.e("AudioUpload", "Error uploading audio: ${e.message}")
            Toast.makeText(this, "Error uploading audio: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }


    // Function to save the song details to Firebase
//    private fun saveSongToFirebase(songUrl: String) {
//        // Create a new SongModel with the song details
//        val songModel = songModel(
//            title = reference.push().key, // Generate a unique ID for the song
//            songTitle = "My Song Title",  // Replace with the actual song title
//            songUrl = songUrl,            // The Cloudinary URL of the uploaded song
//            artist = "Artist Name"        // Optional: Add artist info
//        )
//
//        // Save the song model to Firebase
//        reference.child(songModel.songId!!).setValue(songModel)
//            .addOnCompleteListener {
//                if (it.isSuccessful) {
//                    Log.d("Firebase", "Song successfully added to Firebase!")
//                    Toast.makeText(this, "Song uploaded successfully", Toast.LENGTH_SHORT).show()
//                } else {
//                    Log.e("Firebase", "Failed to add song: ${it.exception?.message}")
//                    Toast.makeText(this, "Failed to upload song", Toast.LENGTH_SHORT).show()
//                }
//            }
//    }
}
