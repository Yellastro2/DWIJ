package com.yelldev.yandexmusiclib.kot_utils

import android.graphics.Bitmap
import androidx.annotation.WorkerThread
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import com.yelldev.yandexmusiclib.yClient
import com.yelldev.yandexmusiclib.yUtils.Differenc
import com.yelldev.yandexmusiclib.yUtils.yUtils.Companion.getArray
import org.json.JSONArray
import org.json.JSONObject

//@JsonProperty("kind") open val mId: String,
//					 @JsonProperty("title") open val mTitle: String,
//					 @JsonProperty("trackCount") open var mCount: Int,
//					 @JsonProperty("durationMs") open var mDuration: Int,
//					 @JsonProperty("cover") open val mCover: JSONObject,
//					 @JsonProperty("tracks") open var mTracklist: ArrayList<String>?,
//					 @JsonProperty("revision") open var mRevision: Int

@JsonIgnoreProperties(ignoreUnknown=true)
abstract class yPlayList(
					 )
{
	abstract val mId: String
	abstract val mTitle: String
	abstract var mCount: Int
	abstract var mDuration: Int
	abstract val mCover: JSONObject
	abstract var mTrackList: ArrayList<String>
	abstract var mRevision: Int
	abstract val mKindId: String
	fun getCoverBtm(fClient: yClient): Bitmap? {
		return fClient.getCover(mCover,200)
	}


	open var mIsnodata = true


	open fun update(fJson: JSONObject){
//		mTitle = fJson.getString("title")
		mCount = fJson.getInt("trackCount")
		mDuration = fJson.getInt("durationMs")
//		mId = fJson.getString("kind")
//		mCover = fJson.getJSONObject("cover")
		mRevision = fJson.getInt("revision")

		if(fJson.has("tracks")){
			mIsnodata = false
			if (mTrackList == null) mTrackList = ArrayList<String>()
			var m_trackList_json = fJson.getJSONArray("tracks")
			for ( q_json in getArray<JSONObject>(m_trackList_json))
				mTrackList!!.add((q_json  as JSONObject).getString("id"))
		} else{
			mIsnodata = true
		}
	}


	fun getTrackList(): List<yTrack>{
		return ArrayList()
	}

	fun addTrack(fClient: yClient,fTrack: yTrack){
//		TODO
		val fDif = Differenc().addInsert(0, fTrack.mId,fTrack.mAlbums[0])
		val fRes = fClient.changePlaylist(mKindId, fDif.toJSON(), mRevision)
		update(fRes.getJSONObject("result"))
		mTrackList.add(0,fTrack.mId)
	}

	fun removeTrack(fClient: yClient,fTrack: yTrack): Boolean {
		val fNum = mTrackList.indexOf(fTrack.mId)
		val fDif = Differenc().addDelete(fNum, fNum+1)
		try {
			fClient.let {

				val fRes = it.changePlaylist(mKindId, fDif.toJSON(), mRevision)
				update(fRes.getJSONObject("result"))
				mTrackList.remove(fTrack.mId)

				return true
			}
		}catch (e: Exception){
			e.printStackTrace()
		}

		return false
	}
}

//	"result": [{
//        "owner": {
//            "uid": 1729972566,
//            "login": "Yellastro2",
//            "name": "Yellastro2",
//            "sex": "male",
//            "verified": false
//        },
//        "playlistUuid": "80987136-e464-7cab-902c-06e22a451b1a",
//        "available": true,
//        "uid": 1729972566,
//        "kind": 1000,
//        "title": "Новый плейлист",
//        "revision": 4,
//        "snapshot": 4,
//        "trackCount": 3,
//        "visibility": "public",
//        "collective": false,
//        "created": "2023-06-01T21:34:17+00:00",
//        "modified": "2023-06-02T09:46:11+00:00",
//        "isBanner": false,
//        "isPremiere": false,
//        "durationMs": 575940,
//        "cover": {
//            "type": "mosaic",
//            "itemsUri": ["avatars.yandex.net/get-music-content/1781407/a49e1148.a.9976672-1/%%", "avatars.yandex.net/get-music-content/5503671/001702f9.a.21335470-1/%%"],
//            "custom": false
//        },
//        "ogImage": "avatars.yandex.net/get-music-content/1781407/a49e1148.a.9976672-1/%%",
//        "tags": [],
//        "customWave": {
//            "title": "Новый плейлист",
//            "animationUrl": "https://music-custom-wave-media.s3.yandex.net/base.json",
//            "position": "default",
//            "header": "Моя волна по плейлисту"
//        }
//    }]