package com.yelldev.dwij.android.entitis

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.yelldev.dwij.android.R
import com.yelldev.dwij.android.yMediaStore

class PlaylistCreateItem: iPlaylist {

	companion object {
		val PLAY_CREATE_ITEM_ID = "create_playlist_id"
	}

	override var mTrackList: ArrayList<String> = ArrayList<String>()
	override val mId: String = PLAY_CREATE_ITEM_ID
	override val mIsnodata: Boolean = true
	override val mTitle: String = ""
	override var mDuration: Int = 0
	override var mCount: Int = 0
	override suspend fun getImage(fClient: yMediaStore): Bitmap? {
		val fRes = BitmapFactory.decodeResource(fClient.mCtx.resources, R.drawable.img_plus)
		return fRes
	}

	override fun getList(): ArrayList<String> {
		TODO("Not yet implemented")
	}

	override suspend fun getTracks(fStore: yMediaStore): ArrayList<iTrack> {
		TODO("Not yet implemented")
	}


	override fun getInfo(): String {
		TODO("Not yet implemented")
	}

	override fun addTracks(fTracks: ArrayList<iTrack>) {
		TODO("Not yet implemented")
	}

	override fun getTitle(): String {
		TODO("Not yet implemented")
	}

	override fun getType(): String {
		TODO("Not yet implemented")
	}

	override fun getId(): String {
		TODO("Not yet implemented")
	}

	override fun isRepeat(): Boolean {
		TODO("Not yet implemented")
	}
}