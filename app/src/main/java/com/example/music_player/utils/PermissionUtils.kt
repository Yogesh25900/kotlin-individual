package com.example.music_player.utils


import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat

object PermissionUtils {

    // Required permissions based on Android version
    fun getRequiredPermissions(): Array<String> {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(Manifest.permission.READ_MEDIA_AUDIO)
        } else {
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    // Check if all permissions are granted
    fun hasPermissions(context: Context): Boolean {
        return getRequiredPermissions().all {
            ActivityCompat.checkSelfPermission(context, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    // Request permissions
    fun requestPermissions(permissionLauncher: ActivityResultLauncher<Array<String>>) {
        permissionLauncher.launch(getRequiredPermissions())
    }
}
