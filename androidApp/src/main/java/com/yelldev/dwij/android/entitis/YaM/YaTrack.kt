package com.yelldev.dwij.android.entitis.YaM

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import com.fasterxml.jackson.annotation.JsonProperty
import com.yelldev.dwij.android.entitis.iTrack
import com.yelldev.dwij.android.yMediaStore

@Entity(tableName = "tracks")
class YaTrack(
	@PrimaryKey
	@JsonProperty("id")
	override val mId: String,
	@JsonProperty("title")
	override val mTitle: String,
	@JsonProperty("coverUri")
	override val mCover: String?,
	@JsonProperty("available")
	val isAvaibale: Boolean,
	@JsonProperty("durationMs")
	val mDuration: Int
) : iTrack {

	companion object {
		val TAG = "YaTrack"
	}

	class YaArtists(@JsonProperty("name")
					val mName: String)

	class YaIdEntity(@JsonProperty("id")
					val mId: String)

	@Ignore
	@JsonProperty("artists")
	lateinit var _mArtists: List<YaArtists>

	var mArtString = ""

	@get:Ignore
	override val mPath: String get() = "TODO"

	@get:Ignore
	override val mArtist: String get() {
		if (mArtString.isNullOrEmpty()&&this::_mArtists.isInitialized){
			for (q_artist in _mArtists)
				mArtString += q_artist.mName + ", "

			mArtString = mArtString.removeSuffix(", ")
		}
		return mArtString
	}
	@Ignore
	override var mPlaylists = LinkedHashSet<String>()

	var mPlaylistString = ""

	var mAlbumString = ""
	@Ignore
	override var mAlbums = ArrayList<String>()

	@Ignore
	@JsonProperty("albums")
	lateinit var _mAlbumsObj: List<YaIdEntity>

	suspend fun postInit(fStore: yMediaStore){
		if(this::_mArtists.isInitialized){
			for (q_artist in _mArtists)
				mArtString += q_artist.mName + ", "

			mArtString = mArtString.removeSuffix(", ")
		}
		if(mPlaylistString.length > 0 && mPlaylists.size < 1){
			val fList = mPlaylistString.split(";")
			var isDissinch = false
			for (qPl in fList){
				mPlaylists.add(qPl)
				val qObj = fStore.getYamPlaylist(qPl)
				if (qObj != null){
					if (!qObj!!.mTrackList.contains(mId))
						removeFromPlList(fStore.db.tracksDao(),qObj)
				}

			}

		}
		if(this::_mAlbumsObj.isInitialized){
			for (q_artist in _mAlbumsObj)
				mAlbumString += q_artist.mId + ";"

			mAlbumString = mAlbumString.removeSuffix(";")
		}
		if(mAlbumString.length > 0)
			mAlbums.addAll(mAlbumString.split(';'))
		val dsfs = 0
//		else if (mTrackString.length > 0){
//			val fTracks = mTrackString.split(";")
//			for (qOb in fTracks) {
//				mTrackList.add(qOb.toInt())
////				mTrackString += qOb.mId.toString() + ";"
//			}
//			val fdsf = 0
//
//		}
	}

	fun removeFromPlList(fDao: TrackDao,fPlaylist: YaPlaylist) {
		mPlaylists.remove(fPlaylist.mId)
		mPlaylistString.replace("${fPlaylist.mId};","")
		mPlaylistString.replace(fPlaylist.mId,"")
		fDao.updatePlaylist(this)
//		mPlaylistString.indexOf(fPlaylist.mId)
	}

	override fun setToPlayer(fPlayer: MediaPlayer, f_Ctx: Context,f_clb: ()-> Int) {
		Thread{
			val fStore = yMediaStore.store(f_Ctx)

			fStore.mTrackMemory.getCachedTrack(this, {fPath ->
				Log.i("DWIJ_DEBUG", "play cached track: $fPath")
				fPlayer.setDataSource(fPath)
			},{
				fUrl: Uri ->
				try {
					Log.i("DWIJ_DEBUG", "play url track: $fUrl")
					fPlayer.setDataSource(f_Ctx, fUrl)
				}catch (e: Exception) {
					Log.e("DWIJ_DEBUG", "setToPlayer()\n ${mTitle} - ${mArtist} \n${fUrl}")
					val f_msg = e.message + "\nsetToPlayer()\n ${mTitle} - ${mArtist} \n${fUrl}"
					throw Exception(f_msg, e)
				}
			})
			f_clb()

		}.start()

	}

	override suspend fun set_Cover_toView(fStore: yMediaStore, f_size:Int): Bitmap? {
		if(mCover == null)
			return null
		return fStore.getCoverAsync(mCover,f_size)
	}

	override fun addPlaylist(fStore: yMediaStore, fPlId: String) {
		mPlaylists.add(fPlId)
		mPlaylistString += ";${fPlId}"
		fStore.db.tracksDao().updatePlaylist(this)
	}


	@Dao
	interface TrackDao {
		@Query("SELECT * FROM tracks")
		fun getAll(): List<YaTrack>

		@Query("SELECT * FROM tracks WHERE mId IN (:plIds)")
		fun loadAllByIds(plIds: IntArray): List<YaTrack>

		@Query("SELECT * FROM tracks WHERE mId IN (:plIds)")
		fun loadById(plIds: String): YaTrack


		@Update
		fun updatePlaylist(fPlList: YaTrack)



		@Insert
		fun insertAll(vararg users: YaTrack)

		@Delete
		fun delete(user: YaTrack)
	}

}