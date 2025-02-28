package com.example.music_player.model

import android.os.Parcel
import android.os.Parcelable
data class Playlist(
    val id: String? = null,
    val name: String? = null,
    val songIds: Map<String, Boolean>? = null // Store songIds as a Map
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readHashMap(Boolean::class.java.classLoader) as? Map<String, Boolean> // Read the Map
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeMap(songIds) // Write the Map
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Playlist> {
        override fun createFromParcel(parcel: Parcel): Playlist {
            return Playlist(parcel)
        }

        override fun newArray(size: Int): Array<Playlist?> {
            return arrayOfNulls(size)
        }
    }
}
