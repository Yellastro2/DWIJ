package com.yelldev.dwij.android.entitis

import android.graphics.Bitmap
import com.yelldev.dwij.android.yMediaStore
import io.reactivex.rxjava3.core.Single

interface iPlaylist: iTrackList {
	var mTrackList: ArrayList<String>

	val mId: String
	val mIsnodata: Boolean
	val mTitle: String
	var mDuration: Int
	var mCount: Int
//	fun getCoverBtm(fSome: yMediaStore): Single<Bitmap>
	suspend fun getCoverBtmAsync(fClient: yMediaStore): Bitmap?
}