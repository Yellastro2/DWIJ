package com.yelldev.dwij.android.entitis.YaM

import androidx.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonProperty

class YaArtist(
    @PrimaryKey
    @JsonProperty("id")
    val mId: String,
    @JsonProperty("title")
    val mName: String,
    @JsonProperty("coverUri")
    var mCover: String?,
    @JsonProperty("available")
    val isAvaibale: Boolean,
    @JsonProperty("durationMs")
    val mDuration: Int
) {
}