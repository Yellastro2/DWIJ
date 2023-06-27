package com.yelldev.dwij.android.entitis

import com.yelldev.dwij.android.yMediaStore

interface iTrackList {


	fun size(): Int = getList().size

	fun getList(): ArrayList<String>

//	fun getTracks(): ArrayList<iTrack>

	fun addTracks(fTracks: ArrayList<iTrack>)

	fun getTitle(): String
	fun getType(): String
	fun getId(): String
	fun isRepeat(): Boolean
	suspend fun getTracks(fStore: yMediaStore): ArrayList<iTrack>
}