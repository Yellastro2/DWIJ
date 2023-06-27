package com.yelldev.dwij.android.entitis

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaPlayer
import com.yelldev.dwij.android.yMediaStore
import io.reactivex.rxjava3.core.Single

interface iTrack {

	var mAlbums: ArrayList<String>
	val mId: String
	val mPath: String
	val mTitle: String
	val mArtist: String
	val mCover: String?
	var mPlaylists: LinkedHashSet<String>

	fun setToPlayer(f_Player: MediaPlayer, f_Ctx: Context, f_clb: () -> Int)
	suspend fun set_Cover_toView(fStore: yMediaStore, f_size:Int = 200): Bitmap?
	fun addPlaylist(fStore: yMediaStore, fPlId: String)
}