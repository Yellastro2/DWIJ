package com.yelldev.dwij.android

import android.annotation.SuppressLint
import android.content.Context
import com.yelldev.dwij.android.entitis.iTrack
import com.yelldev.dwij.android.entitis.sdTrack
import com.yelldev.dwij.android.utils.MediaDB

class yStorageStore(val mCtx: Context) {

	val TAG = "yStorageStore"

	companion object {
		@SuppressLint("StaticFieldLeak")
		var sStore: yStorageStore? = null
		fun store(mCtx: Context): yStorageStore {
			if(sStore == null)
				sStore = yStorageStore(mCtx)
			return sStore!!
		}
	}

	fun getAllTracks(): ArrayList<iTrack> {
		val dbHelper = MediaDB(mCtx)
		val fList = sdTrack.getAll(dbHelper) as ArrayList<iTrack>
//		var mList = MainActivity.sdTrackList(f_List)
		return fList
	}
}