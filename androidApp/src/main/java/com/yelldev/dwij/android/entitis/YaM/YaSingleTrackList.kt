package com.yelldev.dwij.android.entitis.YaM

import com.yelldev.dwij.android.entitis.iTrack
import com.yelldev.dwij.android.entitis.iTrackList
import com.yelldev.dwij.android.yMediaStore

class YaSingleTrackList(val mTrack: iTrack): iTrackList {
    override fun getList(): ArrayList<String> = arrayListOf(mTrack.mId)

    override fun addTracks(fTracks: ArrayList<iTrack>) {
        TODO("Not yet implemented")
    }

    override fun getTitle(): String =mTrack.mTitle

    override fun getType(): String = "single"

    override fun getId(): String {
        TODO("Not yet implemented")
    }

    override fun isRepeat(): Boolean = false

    override suspend fun getTracks(fStore: yMediaStore): ArrayList<iTrack> {
        return arrayListOf(mTrack)
    }
}