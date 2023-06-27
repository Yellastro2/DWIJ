package com.yelldev.dwij.android.entitis.YaM

import com.fasterxml.jackson.annotation.JsonProperty
import com.yelldev.dwij.android.entitis.iTrack
import com.yelldev.dwij.android.entitis.iTrackList
import com.yelldev.dwij.android.yMediaStore
import org.json.JSONObject

class yWave(
	@JsonProperty("radioSessionId")
	val mId: String,
	@JsonProperty("sequence")
	val mTracks: ArrayList<yWaveTrackContainer>
): iTrackList {

	class yWaveTrackContainer(
		@JsonProperty("track")
		val mTrack: yWaveTrackId)
	class yWaveTrackId(
		@JsonProperty("id")
		val mId: String)

	var mTitle: String = ""
	val mTrackList = ArrayList<YaTrack>()
	var mStation: String = ""

// unused now
	override fun getList(): ArrayList<String> {
		var fRes = ArrayList<String>()
		for (qTr in mTracks){
			fRes.add(qTr.mTrack.mId)
		}
		return fRes
	}

	override suspend fun getTracks(fStore: yMediaStore): ArrayList<iTrack> {
		TODO("Not yet implemented")
	}

	override fun addTracks(fTracks: ArrayList<iTrack>) {
		TODO("Not yet implemented")
	}

//	fun getTrackList(): ArrayList<YaTrack> {
//		var fRes = ArrayList<YaTrack>()
//		for (qTr in mTracks){
//			fRes.add(qTr.mTrack)
//		}
//		return fRes
//	}
//
//	fun syncData(fStore: yMediaStore){
//		val trDao = fStore.db.tracksDao()
//		for (qTrack in getTrackList()){
//			val qNew = fStore.syncTrack(trDao,qTrack)
//			mTrackList.add(qNew)
//		}
//
//	}

	override fun getTitle(): String {
		if(mTitle!="")
			return "$mTitle wave"
		return "Wave"
	}

	override fun getType(): String {
		return type
	}

	override fun getId(): String {
		return mId
	}

	override fun isRepeat(): Boolean {
		return false
	}

	companion object {
		val type = "wawe"
	}

//	{
//    "radioSessionId": "r1mi-ze9kHz32qYWyXs-fm0k",
//    "sequence": [{
//        "track": {
//            "id": "116222321",
//            "realId": "116222321",
//            "title": "Miu Miu",
//            "major": {
//                "id": 123,
//                "name": "IRICOM"
//            },
//            "available": true,
//            "availableForPremiumUsers": true,
//            "availableFullWithoutPermission": false,
//            "availableForOptions": ["bookmate"],
//            "disclaimers": [],
//            "storageDir": "",
//            "durationMs": 112500,
//            "fileSize": 0,
//            "r128": {
//                "i": -7.92,
//                "tp": -0.3
//            },
//            "fade": {
//                "inStart": 0.6,
//                "inStop": 1.5,
//                "outStart": 111,
//                "outStop": 112.3
//            },
//            "previewDurationMs": 30000,
//            "artists": [{
//                "id": 17339829,
//                "name": "Немила",
//                "various": false,
//                "composer": false,
//                "cover": {
//                    "type": "from-artist-photos",
//                    "uri": "avatars.yandex.net\/get-music-content\/8123381\/7ffefdda.p.17339829\/%%",
//                    "prefix": "7ffefdda.p.17339829\/"
//                },
//                "genres": [],
//                "disclaimers": []
//            }],
//            "albums": [{
//                "id": 26886360,
//                "title": "Miu Miu",
//                "metaType": "music",
//                "year": 2023,
//                "releaseDate": "2023-08-11T00:00:00+03:00",
//                "coverUri": "avatars.yandex.net\/get-music-content\/9837405\/2d9691b8.a.26886360-1\/%%",
//                "ogImage": "avatars.yandex.net\/get-music-content\/9837405\/2d9691b8.a.26886360-1\/%%",
//                "genre": "pop",
//                "trackCount": 1,
//                "likesCount": 59,
//                "recent": false,
//                "veryImportant": false,
//                "artists": [{
//                    "id": 17339829,
//                    "name": "Немила",
//                    "various": false,
//                    "composer": false,
//                    "cover": {
//                        "type": "from-artist-photos",
//                        "uri": "avatars.yandex.net\/get-music-content\/8123381\/7ffefdda.p.17339829\/%%",
//                        "prefix": "7ffefdda.p.17339829\/"
//                    },
//                    "genres": [],
//                    "disclaimers": []
//                }],
//                "labels": [{
//                    "id": 4866885,
//                    "name": "VK Records"
//                }],
//                "available": true,
//                "availableForPremiumUsers": true,
//                "availableForOptions": ["bookmate"],
//                "availableForMobile": true,
//                "availablePartially": false,
//                "bests": [],
//                "disclaimers": [],
//                "trackPosition": {
//                    "volume": 1,
//                    "index": 1
//                }
//            }],
//            "coverUri": "avatars.yandex.net\/get-music-content\/9837405\/2d9691b8.a.26886360-1\/%%",
//            "derivedColors": {
//                "average": {
//                    "color": "#34312A"
//                },
//                "vibeText": {
//                    "color": "#CCCCCC"
//                }
//            },
//            "ogImage": "avatars.yandex.net\/get-music-content\/9837405\/2d9691b8.a.26886360-1\/%%",
//            "lyricsAvailable": false,
//            "type": "music",
//            "rememberPosition": false,
//            "trackSharingFlag": "VIDEO_ALLOWED",
//            "lyricsInfo": {
//                "hasAvailableSyncLyrics": false,
//                "hasAvailableTextLyrics": false
//            },
//            "trackSource": "OWN"
//        },
//        "liked": false,
//        "trackParameters": {
//            "bpm": 120,
//            "hue": 280,
//            "energy": 0.5
//        },
//        "type": "track"
//    }, ],
//    "batchId": "1697549969342711-15374295024395023530.ik4F",
//    "pumpkin": false,
//    "descriptionSeed": {
//        "value": "onyourwave",
//        "tag": "onyourwave",
//        "type": "user"
//    },
//    "acceptedSeeds": [{
//        "value": "onyourwave",
//        "tag": "onyourwave",
//        "type": "user"
//    }]
//}
}