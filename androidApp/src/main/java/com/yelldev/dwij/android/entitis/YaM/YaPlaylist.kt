package com.yelldev.dwij.android.entitis.YaM

import android.graphics.Bitmap
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import com.fasterxml.jackson.annotation.JsonProperty
import com.yelldev.dwij.android.entitis.iPlaylist
import com.yelldev.dwij.android.entitis.iTrack
import com.yelldev.dwij.android.yMediaStore
import com.yelldev.yandexmusiclib.yUtils.Differenc
import org.json.JSONObject

//(@JsonProperty("kind")mId: String,
//				 @JsonProperty("title")mTitle: String,
//				 @JsonProperty("trackCount")mCount: Int,
//				 @JsonProperty("durationMs")mDuration: Int,
//				 @JsonProperty("cover")mCover: JSONObject,
//				 @JsonProperty("tracks") mTracklist: ArrayList<String>,
//				 @JsonProperty("revision") mRevision: Int
//)
//mId: String,
//				 mTitle: String,
//				 mCount: Int,
//				 mDuration: Int,
//				 mCover: JSONObject,
//				 mTracklist: ArrayList<String>,
//				 mRevision: Int)
@Entity(tableName = "playlists")
open//@JsonIgnoreProperties(ignoreUnknown=true)
class YaPlaylist(
	@JsonProperty("kind")
	val mKindId: String,
	@JsonProperty("playlistUuid")
	@PrimaryKey
	override val mId: String,
	@JsonProperty("title") override val mTitle: String,
	@JsonProperty("trackCount") override var mCount: Int,
	@JsonProperty("durationMs") override var mDuration: Int,
	//				 tak nelza
	//				 @Ignore
	//				 @JsonProperty("cover")  val mCover: yCover,
	@JsonProperty("ogImage")  val mImage: String,
	@JsonProperty("revision") open var mRevision: Int

)	: iPlaylist
{

	@Entity(tableName = "covers")
	class yCover(@JsonProperty("type")val mType: String,
				 @JsonProperty("error")val mErros: String,
				 @JsonProperty("itemsUri")val mItems: List<String>
	)

	class idTracks(@JsonProperty("id") val mId: String)


	@Ignore
	@JsonProperty("tracks")
	lateinit var mTracklistObjs: ArrayList<idTracks>



	@Ignore
	override var mIsnodata = true

	@Ignore
	override var mTrackList = ArrayList<String>()

	var mTrackString: String = ""

	open fun postInit() {
		mIsnodata = false
		if (mTrackString.length > 0) {
			val fTracks = mTrackString.split(";")
			for (qOb in fTracks) {
				mTrackList.add(qOb)
//				mTrackString += qOb.mId.toString() + ";"
			}
		}else
		if(this::mTracklistObjs.isInitialized && mTracklistObjs.size>0){
			for (qOb in mTracklistObjs) {
				mTrackList.add(qOb.mId)
				mTrackString += qOb.mId.toString() + ";"
			}
			mTrackString = mTrackString.substring(0,mTrackString.length-1)
			val fdsf = 0
		}else
			mIsnodata = true
	}

	fun updListString(fDao: PlaylistDao,fNew: String,isAdd: Boolean = true){
		if (isAdd)
			mTrackString = "$fNew;$mTrackString"
		else{
			val fInd = mTrackString.indexOf(fNew)
			if (mTrackString.length >= fInd + fNew.length)
				mTrackString = mTrackString.replace(fNew,"")
			else
				mTrackString = mTrackString.replace("$fNew;","")
		}
		fDao.updatePlaylist(this)
	}


//	override fun getCoverBtm(fClient: yMediaStore): Single<Bitmap> {
//		return fClient.getCover(mImage,150)
//	}

	override suspend fun getCoverBtmAsync(fClient: yMediaStore): Bitmap? {
		return fClient.getCoverAsync(mImage,200)
	}

	@Ignore
	override fun getList(): ArrayList<String> {
		return mTrackList
	}

	override suspend fun getTracks(fStore: yMediaStore): ArrayList<iTrack> {
		return fStore.getTrackList(getList())
	}

	override fun addTracks(fTracks: ArrayList<iTrack>) {
		TODO("Not yet implemented")
	}

	@Ignore
	override fun getTitle(): String {
		return mTitle
	}
	@Ignore
	override fun getType(): String {
		return "yandex playlist"
	}
	@Ignore
	override fun getId(): String {
		return mId
	}

	override fun isRepeat(): Boolean {
		return false
	}


	fun update(fJson: JSONObject){
//		mTitle = fJson.getString("title")
		mCount = fJson.getInt("trackCount")
		mDuration = fJson.getInt("durationMs")
//		mId = fJson.getString("kind")
//		mCover = fJson.getJSONObject("cover")
		mRevision = fJson.getInt("revision")

//		if(fJson.has("tracks")){
//			mIsnodata = false
//			if (mTracklist == null) mTracklist = ArrayList<String>()
//			var m_trackList_json = fJson.getJSONArray("tracks")
//			for ( q_json in Utils.getArray(m_trackList_json))
//				mTracklist!!.add((q_json  as JSONObject).getString("id"))
//		} else{
//			mIsnodata = true
//		}
	}


//	fun getTrackList(): List<yTrack>{
//		return ArrayList()
//	}

	fun removeTrack(fStore: yMediaStore,fTrack: YaTrack): Boolean {
		val fNum = mTrackList.indexOf(fTrack.mId)
		val fDif = Differenc().addDelete(fNum, fNum+1)
		try {
			fStore.mYamClient?.let {

				val fRes = it.changePlaylist(mKindId, fDif.toJSON(), mRevision)
				update(fRes.getJSONObject("result"))
				mTrackList.remove(fTrack.mId)
				fTrack.removeFromPlList(fStore.db.tracksDao(),this)
//				fTrack.mPlaylists.remove(mId)
				updListString(fStore.plDao,fTrack.mId,false)

//				fTrack.addPlaylist(fStore,mId)
				return true
			}
		}catch (e: Exception){
			e.printStackTrace()
		}

		return false
	}

	fun addTrack(fStore: yMediaStore,fTrack: YaTrack): Boolean {
//		TODO
		val fDif = Differenc().addInsert(0, fTrack.mId,fTrack.mAlbums[0])
		try {
			fStore.mYamClient?.let {

				val fRes = it.changePlaylist(mKindId, fDif.toJSON(), mRevision)
				update(fRes.getJSONObject("result"))
				mTrackList.add(0,fTrack.mId)
				updListString(fStore.plDao,fTrack.mId,false)
				fTrack.addPlaylist(fStore,mId)
				return true
			}
		}catch (e: Exception){
			e.printStackTrace()
		}

		return false
	}


//	override val m_yTrackList: ArrayList<iTrack>
//		get() = adaptTrackstoDwij(mTracklist)

//	fun adaptTrackstoDwij(f_first_list: List<yTrack>): ArrayList<iTrack> {
//		val f_result_list = ArrayList<iTrack>()
//		for (q_track in f_first_list) f_result_list.add(YaTrack(q_track))
//
//		return f_result_list
//	}

	override fun toString(): String {
		return "${this.javaClass.name}: $mTitle; $mKindId; $mId; $mDuration"
	}


	@Dao
	interface PlaylistDao {
		@Query("SELECT * FROM playlists WHERE mId != 'liked'")
		fun getAll(): List<YaPlaylist>

		@Query("SELECT * FROM playlists WHERE mId IN (:plIds)")
		fun loadAllByIds(plIds: IntArray): List<YaPlaylist>

		@Query("SELECT * FROM playlists WHERE mId IN (:plIds)")
		fun loadById(plIds: String): YaPlaylist

		@Query("SELECT * FROM playlists WHERE mKindId IN (:kind)")
		fun  loadByKind(kind: String): YaPlaylist

//		@Query("UPDATE playlists SET mTrackList = :fTr WHERE mId = :fId")
//		fun updateTrackList(fId: String,fTr: List<Int>)

		@Update
		fun updatePlaylist(fPlList: YaPlaylist)

//		@Query("SELECT * FROM user WHERE first_name LIKE :first AND " +
//				"last_name LIKE :last LIMIT 1")
//		fun findByName(first: String, last: String): YaPlaylist

		@Insert
		fun insertAll(vararg users: YaPlaylist)

		@Delete
		fun delete(user: YaPlaylist)
	}

//	override fun parseTrack_andAdd(qJson: JSONObject) {
//		val f_track_json = qJson.getJSONObject("track")
//		mTracklist.add(yTrack(mClient, f_track_json))
//	}
//	override val m_ID: String = f_new_pllist.m_ID
//	override val m_isNodata: Boolean = f_new_pllist.m_isNodata
//	override val m_Title: String = f_new_pllist.m_Title
//	override val m_Duration: Int = f_new_pllist.m_Duration
//	override val m_Count: Int = f_new_pllist.m_Count
//	override val m_Cover_btm: Bitmap?
	companion object {
		val TYPE = "yandex playlist"
	}
}