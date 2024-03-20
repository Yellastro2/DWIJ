package com.yelldev.dwij.android.entitis.YaM

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.Drawable
import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import androidx.room.Query
import com.fasterxml.jackson.annotation.JsonProperty
import com.yelldev.dwij.android.KeyStore.Companion.COLOR_PINK
import com.yelldev.dwij.android.R
import com.yelldev.dwij.android.entitis.iTrack
import com.yelldev.dwij.android.yMediaStore

const val LIKED_ID = "liked"

//@Entity(tableName = "playlists")
@Entity
class YaLikedTracks(
	@JsonProperty("playlistUuid")
	@PrimaryKey
	override val mId: String,
	@JsonProperty("trackCount") override var mCount: Int,
	@JsonProperty("durationMs") override var mDuration: Int,
	@JsonProperty("revision") override var mRevision: Int):
	YaPlaylist(mKindId = LIKED_ID,
		mId = mId,
		mTitle = "Liked",
		mCount,
		mDuration,
		"",
		mRevision) {


	@Ignore
	override var mIsnodata = true

	override fun postInit(){
		super.postInit()
		mCount = mTrackList.size
	}

	override suspend fun getImage(fClient: yMediaStore): Bitmap? {

		val fRes = BitmapFactory.decodeResource(fClient.mCtx.resources,R.drawable.img_like)
		return fRes

	}

	override fun getType(): String {
		return LIKED_ID
	}

	override fun getInfo(): String {
		return "Like info"
	}

//	override suspend fun getTracks(fStore: yMediaStore): ArrayList<iTrack> {
//		TODO("Not yet implemented")
//	}

	override fun addTracks(fTracks: ArrayList<iTrack>) {
		TODO("Not yet implemented")
	}


	@Dao
	interface LikeListDao {
		@Query("SELECT * FROM playlists WHERE mId != 'liked'")
		fun getAll(): List<YaPlaylist>

		@Query("SELECT * FROM playlists WHERE mId IN (:plIds)")
		fun loadAllByIds(plIds: IntArray): List<YaPlaylist>

		@Query("SELECT * FROM playlists WHERE mId IN (:plIds)")
		fun loadById(plIds: String): YaPlaylist

		@Query("SELECT * FROM playlists WHERE mKindId IN (:kind)")
		fun loadByKind(kind: String, ): YaLikedTracks

	}

//	@Ignore
//	override var mTrackList = ArrayList<String>()
//
//	override var mTrackString: String = ""

	companion object {
		fun from(fFrom: YaPlaylist): YaLikedTracks? {
			val fTo = YaLikedTracks(fFrom.mId,
				fFrom.mCount,
				fFrom.mDuration,
				fFrom.mRevision)
			fTo.mTrackString = fFrom.mTrackString
			return fTo
		}

		val LIKED_ID: String = "liked"
	}
}