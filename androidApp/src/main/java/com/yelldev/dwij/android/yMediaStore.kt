package com.yelldev.dwij.android

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import com.yelldev.dwij.android.entitis.YaM.YaArtist
import com.yelldev.dwij.android.entitis.YaM.YaLikedTracks
import com.yelldev.dwij.android.entitis.YaM.YaPlaylist
import com.yelldev.dwij.android.entitis.YaM.YaTrack
import com.yelldev.dwij.android.entitis.YaM.yWave
import com.yelldev.dwij.android.entitis.iPlaylist
import com.yelldev.dwij.android.entitis.iTrack
import com.yelldev.dwij.android.entitis.iTrackList
import com.yelldev.dwij.android.entitis.yEntity
import com.yelldev.dwij.android.utils.NoYandexLoginExceprion
import com.yelldev.dwij.android.utils.TrackCache
import com.yelldev.dwij.android.utils.yDiskLruCache
import com.yelldev.dwij.android.utils.yTimer
import com.yelldev.yandexmusiclib.Account
import com.yelldev.yandexmusiclib.yClient
import com.yelldev.yandexmusiclib.yUtils.yUtils.Companion.getArray
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap


class yMediaStore(val mCtx: Context) {

//	@Entity(primaryKeys = ["mUId", "mId"])
//	data class PlaylistSongCrossRef(
//		val mUId: String,
//		val songId: Int
//	)

	val TAG = "yMediaStore"

	@Database(entities = [YaPlaylist::class,YaTrack::class,
		TrackCache.yTrackCash::class], version = 2,
		exportSchema = true,
		autoMigrations = [
			AutoMigration (from = 1, to = 2)
		]
	)
	abstract class AppDatabase : RoomDatabase() {
		abstract fun playlistsDao(): YaPlaylist.PlaylistDao
		abstract fun tracksDao(): YaTrack.TrackDao
		abstract fun TrackCacheDao(): TrackCache.yTrackCash.TrackCacheDao


	}


//	нужен какой то таймер (раз в день), по которому запускается рескан
//	рескан в фоне грузит все плейлисты, которые было уже видно у пользователя(или грузит их у пользователя заново)
//	и правит связи в треках между ними\плейлистами
//	+ запускать этот скан когда идет апдейт плейлиста
//

	companion object {
		@SuppressLint("StaticFieldLeak")
		private var sStore: yMediaStore? = null
		fun store(mCtx: Context): yMediaStore {
			if(sStore == null)
				sStore = yMediaStore(mCtx)
			return sStore!!
		}
	}

	val MAX_RAM_ARRAY_SIZE = 200

	interface yObserver{
		fun onUpdate(fProg: Int,fMax: Int)
		fun onCompteate()
	}

	val mPlaylistUpdateObservers = HashMap<String,ArrayList<yObserver>>()

//	val mTrackMap = LinkedHashMap<String,YaTrack>()
	val mTrackMap = ConcurrentHashMap<String,YaTrack>()
	val mPlListMap = ConcurrentHashMap<String,YaPlaylist>()


	var mYamClient: yClient? = null

	suspend fun getYamClient(): yClient? {
		if (mYamClient == null) {
			val fJob = CoroutineScope(Dispatchers.IO)
				.async {
					mYamClient = init_YaM(mCtx)
				}
			fJob.await()
		}
		return mYamClient
	}


	val mapper = jacksonObjectMapper()


	val db: AppDatabase
	val LRUMemory: yDiskLruCache
	val mTrackMemory: TrackCache

	var mLikedTracksList: YaLikedTracks? = null

	val plDao: YaPlaylist.PlaylistDao


	init {
		var fStart = Instant.now().epochSecond
		Log.i(TAG,"init()")
		mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
		db = Room.databaseBuilder(
			mCtx,
			AppDatabase::class.java, "mediastore_db"
		).build()
		plDao = db.playlistsDao()


		LRUMemory = yDiskLruCache(mCtx)
		mTrackMemory = TrackCache(this)
		Log.i(TAG,"init() finish: ${Instant.now().epochSecond - fStart}")
	}

	suspend fun likeTrack(fId: String){
//		withContt(Dispatchers.IO){
			val isLiked = isTrackLiked(fId)
			Log.i(TAG,"isLike: $isLiked")
			getYamClient()?.likeAction(
				"track",
				fId,
				remove = isLiked
			)
			if (mLikedTracksList == null || mLikedTracksList!!.mTrackList == null)
				getLikedTracks()
			if (isLiked)
				do while (mLikedTracksList?.mTrackList?.remove(fId)!!)

			else
				if (!mLikedTracksList!!.mTrackList!!.contains(fId))
					mLikedTracksList?.mTrackList?.add(fId)
			getLikedTracks()
//		}
	}

	suspend fun init_YaM(fCtx: Context): yClient? {



		val sharedPref = fCtx.getSharedPreferences(KeyStore.s_preff, AppCompatActivity.MODE_PRIVATE)

		val fToken = sharedPref.getString(KeyStore.k_ya_token,"")!!
		var fId = sharedPref.getString(KeyStore.k_ya_id,"")!!

		if (fToken.equals("")){

			withContext(Dispatchers.IO) {
				MainActivity.LOG.info("no YandexMusic login")
			}
			return null
		}else{
//			val fLogin = sharedPref.getString(KeyStore.k_ya_login,"nologin")!!
//            var f_id = sharedPref.getString(KeyStore.k_ya_id,"")!!

            if (fId.equals("")){
//                Thread {
                    var f_res = Account.showInformAccountFromToken(fToken).get()
                    Log.i("DWIJ_TAG", f_res.toString())
				fId = f_res
                        .getJSONObject("result")
                        .getJSONObject("account")
                        .getString("uid")
//                    runOnUiThread {
//                        val sharedPref = getSharedPreferences(KeyStore.s_preff, MODE_PRIVATE)
                        with (sharedPref.edit()) {

                            putString(KeyStore.k_ya_id, fId )
                            apply()
                        }
//                        init_YaM()

//                }.start()
			}
			return yClient(fToken, fId)

		}
	}

	var mLastLikeUpdate: Long = 0

	suspend fun getLikedTracks(): YaLikedTracks {
		var isNew = false
		var fResult: YaLikedTracks
		if(mLikedTracksList==null)
			fResult = withContext(Dispatchers.IO){

			val plDao = db.playlistsDao()
			var fList: YaLikedTracks? = null
			val fListSome = plDao.loadByKind(YaLikedTracks.LIKED_ID)
			if (fListSome == null)
			{
				val fRes = getYamClient()!!.getLiked("track")
//				result -> {JSONObject@26173} "{"library":{"uid":1729972566,"revision":176,"playlistUuid":"3ae8e560-bfa1-54fb-a23a-6a5d7873b394","tracks":[{"id":"109140203","albumId":"24375438","timestamp":"2023-10-14T22:51:33+00:00"},{"id":"116611570","albumId":"27059544","timestamp":"2023-10-14T11:40:29+00:00"},{"id":"117446018","albumId":"27410628","timestamp":"2023-10-14T11:21:51+00:00"},{"id":"65677930","albumId":"10645498","timestamp":"2023-10-13T21:54:08+00:00"},{"id":"92952131","albumId":"18679651","timestamp":"2023-10-13T21:53:48+00:00"},{"id":"61338809","albumId":"9468918","timestamp":"2023-10-13T15:19:53+00:00"},{"id":"114335602","albumId":"26084605","timestamp":"2023-10-10T00:23:38+00:00"},{"id":"117608971","albumId":"27479601","timestamp":"2023-10-02T20:59:13+00:00"},{"id":"89325779","albumId":"17510096","timestamp":"2023-10-02T20:59:13+00:00"},{"id":"43724201","albumId":"5827408","timestamp":"2023-09-27T13:28:27+00:00"},{"id":"94298678","albumId":"19097174","timestamp":"2023-09-26T23:32:40+00:00"},{"id":"113351342","a"
				val fLib = fRes.getJSONObject("result").getJSONObject("library")
				fList = mapper.readValue(fLib.toString(), YaLikedTracks::class.java)
				fList.postInit()
				loadTraks(fList.mTrackList,fList.mId)
				isNew = true
			}else
				fList = YaLikedTracks.from(fListSome)

			fList!!.postInit()
			val fOld = plDao.loadByKind(YaLikedTracks.LIKED_ID)
			if (isNew) {
				plDao.insertAll(fList)
			}
			mLikedTracksList = fList
			Log.i(TAG,mLikedTracksList.toString())
			return@withContext fList!!
		} else
			fResult = mLikedTracksList!!
		if (!isNew){
			var fCurentTime = System.currentTimeMillis()
			if(fCurentTime - 1000 * 10 > mLastLikeUpdate) {
				mLastLikeUpdate = fCurentTime
				CoroutineScope(Dispatchers.IO).launch {
					val fRes = getYamClient()!!.getLiked("track")
					//				result -> {JSONObject@26173} "{"library":{"uid":1729972566,"revision":176,"playlistUuid":"3ae8e560-bfa1-54fb-a23a-6a5d7873b394","tracks":[{"id":"109140203","albumId":"24375438","timestamp":"2023-10-14T22:51:33+00:00"},{"id":"116611570","albumId":"27059544","timestamp":"2023-10-14T11:40:29+00:00"},{"id":"117446018","albumId":"27410628","timestamp":"2023-10-14T11:21:51+00:00"},{"id":"65677930","albumId":"10645498","timestamp":"2023-10-13T21:54:08+00:00"},{"id":"92952131","albumId":"18679651","timestamp":"2023-10-13T21:53:48+00:00"},{"id":"61338809","albumId":"9468918","timestamp":"2023-10-13T15:19:53+00:00"},{"id":"114335602","albumId":"26084605","timestamp":"2023-10-10T00:23:38+00:00"},{"id":"117608971","albumId":"27479601","timestamp":"2023-10-02T20:59:13+00:00"},{"id":"89325779","albumId":"17510096","timestamp":"2023-10-02T20:59:13+00:00"},{"id":"43724201","albumId":"5827408","timestamp":"2023-09-27T13:28:27+00:00"},{"id":"94298678","albumId":"19097174","timestamp":"2023-09-26T23:32:40+00:00"},{"id":"113351342","a"
					val fLib = fRes.getJSONObject("result").getJSONObject("library")
					val fNewOnlineList =
						mapper.readValue(fLib.toString(), YaLikedTracks::class.java)
					if (fNewOnlineList.mRevision != mLikedTracksList!!.mRevision) {
						fNewOnlineList.postInit()
						fNewOnlineList.postInit()
						loadTraks(fNewOnlineList.mTrackList, fNewOnlineList.mId)
						plDao.updatePlaylist(fNewOnlineList)
						mLikedTracksList = fNewOnlineList
					}
				}
			}
		}
//		loadTraks(fResult.mTrackList,fResult.mId)
		return fResult
	}

	private suspend fun loadTraks(fTraks: List<String>, fPlaylistId: String = "") {
		CoroutineScope(Dispatchers.IO).launch {
			var fIdList = JSONArray()
			for (i in 0 until fTraks.size) {
				fIdList.put(fTraks[i])
			}
			val fRes = getYamClient()!!.getObjList(yClient.TYPE_TRACK, fIdList)
			fIdList = fRes.getJSONArray("result")
			loadTracksThread(fIdList, fPlaylistId)
		}
	}

	fun isTrackLiked(fId: String): Boolean{
		val fList = mLikedTracksList
		if (mLikedTracksList == null) return false

		val isIn = mLikedTracksList!!.mTrackList.indexOf(fId)
		return isIn >-1
	}

	@SuppressLint("CheckResult")
	suspend fun _loadYamPlaylists(fClb: (List<iPlaylist>) -> Unit): List<iPlaylist> {
		Log.i(TAG,"_loadYamPlaylists()")
		var fList: ArrayList<YaPlaylist> = ArrayList(mPlListMap.values)
		var isNew = false

		val fLikeAsync = CoroutineScope(Dispatchers.IO).async {
			return@async getLikedTracks()
		}

		if (fList.size < 1) {

			fList = plDao.getAll() as ArrayList<YaPlaylist>

			if (fList.size < 1) {
				isNew = true
				if (getYamClient() == null) throw NoYandexLoginExceprion()
				fList = adaptPlListArray(
					getYamClient()!!.getUserListPllistsJSON()
				) as ArrayList<YaPlaylist>
				plDao.insertAll(*(fList).toTypedArray())
			}
			for (qPl in fList) {
				qPl.postInit()
				mPlListMap.put(qPl.mId, qPl)
			}
		}
		if(isNew){
			syncPlListWithRam(fList)
		}

		val fRemList = ArrayList<Any>()
		for (qList in fList) {
			if (qList.mKindId == "liked") {
				fRemList.add(qList)
//				break
			}
		}
		for (qL in fRemList){
			fList.remove(qL)
		}

		if(!isNew){
			CoroutineScope(Dispatchers.IO).launch(Dispatchers.IO){
				try {
					val fLstList = adaptPlListArray(
						getYamClient()!!.getUserListPllistsJSON()
					)
					print(fLstList.toString())
					syncPlListWithRam(fLstList)
					var fUpd = false
					for (qL in fLstList){
						var qHas = false
						for (qL2 in fList){
							if (qL2.mId == qL.mId) {
								qHas = true
								break
							}
						}
						if (!qHas) fUpd = true
					}
					if (fUpd)
						withContext(Dispatchers.Main){
							fLstList.add(0,fLikeAsync.await())
							fClb(fLstList)
						}

				}catch (e: Exception){
					e.printStackTrace()
				}
			}
		}

		fList.sortBy { -Integer.parseInt(it.mKindId) }

		fList.add(0,fLikeAsync.await())
		return fList
	}

	private suspend fun syncPlListWithRam(fYaList: List<YaPlaylist>) {
		withContext(Dispatchers.IO) {
			val plDao = db.playlistsDao()
			if (getYamClient() != null) {
				for( qpl in fYaList){
					val qRamPlList = mPlListMap[qpl.mId]
					if (qRamPlList == null ||
						qpl.mRevision != qRamPlList.mRevision ||
						qRamPlList.mIsnodata)
						updatePlayList(plDao,qpl.mKindId)
				}
			}
		}
	}



	@SuppressLint("CheckResult")
	suspend fun getYaMPlaylistsList(fClb: (List<iPlaylist>) -> Unit){
		try {
			val fRes = _loadYamPlaylists(fClb)
			withContext(Dispatchers.Main) {
				fClb(fRes)
			}
//			subsciber.onSuccess(fRes)
		}catch (e: Exception){
			if (e::class.java == NoYandexLoginExceprion::class.java)
				throw e
			e.printStackTrace()
		}
	}

	suspend fun getPlaylist(fKind: String, fUser: String): YaPlaylist? {
		val f_new_pllist = getYamClient()!!.getPlaylistJSON(fKind,fUser)
		val fNewPlList =
			mapper.readValue(f_new_pllist.toString(), YaPlaylist::class.java)
		fNewPlList.postInit()
		mPlListMap[fNewPlList.mId] = fNewPlList
//		TODO в базе все пл считаются текущего юзера
//		if (plDao.loadById(fNewPlList.mId) == null)
//			plDao.insertAll(fNewPlList)
//		else
////TODO if has update
//			plDao.updatePlaylist(fNewPlList)
		if(!mPlaylistUpdateObservers.containsKey(fNewPlList.mId))
			mPlaylistUpdateObservers[fNewPlList.mId] = ArrayList()

		loadTracksThread(f_new_pllist.getJSONArray("tracks"),fNewPlList.mId)

		return fNewPlList
	}


	//Внутренний метод этого класса
	private suspend fun updatePlayList(plDao: YaPlaylist.PlaylistDao, fKind: String): YaPlaylist {
//		TODO update in 3 places: ram, bd, and ui
//		TODO blokirovka, error ALLERT!!!!
//		TODO update track list to

		val f_new_pllist = getYamClient()!!.getPlaylistJSON(fKind)
		val fNewPlList =
			mapper.readValue(f_new_pllist.toString(), YaPlaylist::class.java)
		fNewPlList.postInit()
		mPlListMap[fNewPlList.mId] = fNewPlList
		if (plDao.loadById(fNewPlList.mId) == null)
			plDao.insertAll(fNewPlList)
		else
//TODO if has update
			plDao.updatePlaylist(fNewPlList)
		if(!mPlaylistUpdateObservers.containsKey(fNewPlList.mId))
			mPlaylistUpdateObservers[fNewPlList.mId] = ArrayList()

		loadTracksThread(f_new_pllist.getJSONArray("tracks"),fNewPlList.mId)

		return fNewPlList
	}

	fun loadTracksThread(fTrackList: JSONArray,fPlayListId: String = ""){
		CoroutineScope(Dispatchers.IO).launch {
//		}
//		Thread{
			val trDao = db.tracksDao()
			var fProg = 0
			val fArray = getArray<JSONObject>(fTrackList)
			for (qTrackJSON in fArray) {
				loadTrack(trDao,qTrackJSON,fPlayListId)
				fProg ++
				if (fPlayListId != "")
					if (mPlaylistUpdateObservers.containsKey(fPlayListId))
						for (qObs in mPlaylistUpdateObservers[fPlayListId]!!)
							qObs.onUpdate(fProg,fArray.size)
			}
			if (fPlayListId != ""){
				if (mPlaylistUpdateObservers.containsKey(fPlayListId))
					for (qObs in mPlaylistUpdateObservers[fPlayListId]!!)
						qObs.onCompteate()
				mPlaylistUpdateObservers.remove(fPlayListId)
			}

		}
//			.start()
	}


//	TODO этот метод вызывает через updatePlayList() новые потоки, на которые слушатели
//	вешаются в месте, где этот метод вызывается..
suspend fun getYamPlaylist(fId: String): YaPlaylist? {
//		get playlist from mPlaylists
//		if playlist.traklist = 0 => pl = client.getplaylist
//			db.update(pl)
//		else return playlist

		try {
			var fPlList = mPlListMap[fId]
			val plDao = db.playlistsDao()
			if(fPlList == null){
				fPlList = plDao.loadById(fId)
	//			TODO NE DOLJEN B'IT'
			}
			if (fPlList.mIsnodata){
				fPlList.postInit()
			}
	//		TODO else async check revision and update if !=
	//				return fPlList
			if (fPlList.mKindId == YaLikedTracks.LIKED_ID){
				fPlList = getLikedTracks()
			}
			return withContext(Dispatchers.Main) {
				fPlList
			}
		}catch (e: Exception){
			e.printStackTrace()
			return null
		}

	}

	public suspend fun syncTrack(fDao: YaTrack.TrackDao, fTrack: YaTrack): YaTrack {
		val fTrackId = fTrack.mId
		var fTrackDb = fDao.loadById(fTrackId)
		if (fTrackDb != null) {
			fTrackDb.postInit(this)
			putTrackToMap(fTrackDb)
			return fTrackDb
		}else{
			fTrack.postInit(this)
			putTrackToMap(fTrack)
			return fTrack
		}
	}

	private suspend fun loadTrack(fDao: YaTrack.TrackDao,
								  fTrackId: JSONObject,
								  fPlId: String = "") {
		var fTrack = fDao.loadById(fTrackId.getString("id"))
		var isFromDB = true
		if (fTrack == null) {
			isFromDB = false
			if(fTrackId.has("track"))
				fTrack =
					mapper.readValue(fTrackId.getJSONObject("track").toString(), YaTrack::class.java)
			else{
				fTrack =
					mapper.readValue(fTrackId.toString(), YaTrack::class.java)
			}
		}
		fTrack.postInit(this)
		if(fPlId!="" && !fTrack.mPlaylists.contains(fPlId)) {
			fTrack.mPlaylists.add(fPlId)
			fTrack.mPlaylistString += ";$fPlId"
			fTrack.mPlaylistString = fTrack.mPlaylistString.removePrefix(";")
		}
		if(isFromDB)
			fDao.updatePlaylist(fTrack)
		else
			fDao.insertAll(fTrack)
		putTrackToMap(fTrack)
	}

	suspend fun getTrackRequest(fTrackId: String): YaTrack? {
//		"""Получение трека/треков.
//
//        Args:
//            track_ids (:obj:`str` | :obj:`int` | :obj:`list` из :obj:`str` | :obj:`list` из :obj:`int`): Уникальный
//                идентификатор трека или треков.
//            with_positions (:obj:`bool`, optional): С позициями TODO.
//            **kwargs (:obj:`dict`, optional): Произвольные аргументы (будут переданы в запрос).
//
//        Returns:
//            :obj:`list` из :obj:`yandex_music.Track`: Трек или Треки.
//
//        Raises:
//            :class:`yandex_music.exceptions.YandexMusicError`: Базовое исключение библиотеки.
//        """
//        return self._get_list('track', track_ids, {'with-positions': str(with_positions)}, *args, **kwargs)

		val fTrackJson = JSONArray("[$fTrackId]")
		val fTrack = getYamClient()?.getObjList("track",fTrackJson)

		if (fTrack != null) {
			val qTrack = mapper.readValue(fTrack.getJSONArray("result").getJSONObject(0).toString(), YaTrack::class.java)
			return qTrack
		}
		return null
	}

	private fun putTrackToMap(fTrack: YaTrack){
		mTrackMap.put(fTrack.mId,fTrack)
		checkMapSize(mTrackMap as ConcurrentHashMap<Any,Any>)
	}

	private fun checkMapSize(fMap: ConcurrentHashMap<Any,Any>){
		if(fMap.size > MAX_RAM_ARRAY_SIZE)
			for( i in 0..fMap.size - MAX_RAM_ARRAY_SIZE)
				fMap.remove(fMap.keys.toList()[0])
	}

//	TODO это вызвается после getYamPlaylist(), как раз за слушателем или сразу
suspend fun getTrackList(fTrackList: ArrayList<String>): ArrayList<iTrack> {

			val fTrackResult = ArrayList<iTrack>()
			val trDao = db.tracksDao()
			for (qId in fTrackList)
				fTrackResult.add(getTrack(qId))

			return fTrackResult
	}

	private fun adaptPlListArray(f_first_list: JSONArray): ArrayList<YaPlaylist> {
		yTimer.timing(TAG, "adaptPlListArray() start")
		val f_result_list = ArrayList<YaPlaylist>()
		for (q_pllist in getArray<JSONObject>(f_first_list)) {
			val qObj = mapper.readValue<YaPlaylist>(q_pllist.toString())
			f_result_list.add(qObj)
		}
		yTimer.timing(TAG, "adaptPlListArray() for")
		return f_result_list
	}

	private suspend fun _getCover(fImage: String, fSize: Int): Bitmap? {
//		avatars.yandex.net/get-music-content/1781407/a49e1148.a.9976672-1/%%
		val fKey = "${fImage}_$fSize"
		val fCached = LRUMemory.getBitmapFromDiskCache(fKey)
		if (fCached == null) {
			if (getYamClient() == null) throw (Exception("no Yandex Client attached!"))
			getYamClient()!!.getStream(fImage, fSize).let {
				val bmp = BitmapFactory.decodeStream(it)
				LRUMemory.addBitmapToCache(fKey, bmp)
			}
			return getYamClient()!!.getCover(fImage, fSize)
		} else return fCached
	}

	suspend fun getCoverAsync(fImage: String, fSize: Int): Bitmap? {
		return _getCover(fImage,fSize)
	}

	suspend fun getTrack(fTrackId: String): YaTrack {
		val trDao = db.tracksDao()

		if (mTrackMap.containsKey(fTrackId)){
			return mTrackMap.get(fTrackId)!!
		}else{
			var qTrack = trDao.loadById(fTrackId)
			if( qTrack == null){
				if (mTrackDataStore.contains(fTrackId)){
					val qData = mTrackDataStore.get(fTrackId)
					qTrack = mapper.readValue(qData.toString(), YaTrack::class.java)
				}else{
					qTrack = getTrackRequest(fTrackId)!!
					qTrack.postInit(this)
					trDao.insertAll(qTrack)
				}

			}
			qTrack.postInit(this)
			putTrackToMap(qTrack)
			return qTrack
		}
	}

	val mTrackDataStore = ConcurrentHashMap<String,JSONObject>()


// new 488,284,082,003,476
// old
	suspend fun getWaveNextTrack(fWave: yWave,fPos: Int): ArrayList<String> {
		val fPrevTrack = fWave.mTracks[fPos - 1].mTrack.mId
//	java.lang.IndexOutOfBoundsException: Index 17 out of bounds for length 17 TODO
//	at com.yelldev.dwij.android.yMediaStore.getWaveNextTrack(yMediaStore.kt:620)
//  at com.yelldev.dwij.android.player_engine.PlayerService$updWave$1.invokeSuspend(PlayerService.kt:416)
		val fData = getYamClient()!!
			.getWaveNextTrack(fWave.mId,
				fPrevTrack,
				getDuration(fPrevTrack) / 1000,
				fWave.mTracks[fPos].mTrack.mId)

		val dbg =0
		val fWaveJson = fData.getJSONObject("result")
		val fTrackData = fWaveJson.getJSONArray("sequence")
		val fResult = ArrayList<String>()
		for (i in 0 until fTrackData.length())
		{
			val qTrackContainer = fTrackData.getJSONObject(i)
			val qTrackData = qTrackContainer.getJSONObject("track")
			val qId = qTrackData.getString("id")
			if(!mTrackDataStore.containsKey(qId))
				mTrackDataStore.put(qId,qTrackData)

			if(!fWave.getList().contains(qId)) {
				fWave.mTracks.add(
					mapper.readValue(qTrackContainer.toString(), yWave.yWaveTrackContainer::class.java)
				)
				fResult.add(qId)
			}

		}
		Thread {checkMapSize(mTrackDataStore as ConcurrentHashMap<Any,Any>)}.start()

		return fResult
	}

	private suspend fun getDuration(fTrackId: String): Int {

		return getTrack(fTrackId).mDuration
	}

	suspend fun getWave(fObject: yEntity): iTrackList? {
//		Например, станцией для запуска потока по треку будет `track:1234`
		val fLogin = mCtx.getSharedPreferences(KeyStore.s_preff, Activity.MODE_PRIVATE)
			.getString(com.yelldev.dwij.android.KeyStore.k_ya_login, "")
		var fTag = ""
		if (fObject is YaPlaylist){
			fTag = "playlist:${fLogin}_${fObject.mKindId}"
		}else if (fObject is YaTrack){
			fTag = "track:${fObject.mId}"
		}
		return getWave(fTag,fObject.getTitle())
	}

	suspend fun getWave(fPlaylist: YaPlaylist): iTrackList? {
		val fLogin = mCtx.getSharedPreferences(KeyStore.s_preff, Activity.MODE_PRIVATE)
			.getString(com.yelldev.dwij.android.KeyStore.k_ya_login, "")
		val fTag = "playlist:${fLogin}_${fPlaylist.mKindId}"
		return getWave(fTag,fPlaylist.mTitle)
	}

	suspend fun getWave(fTag: String = "user:onyourwave", fTitle: String = ""): iTrackList? {
//		Запуск потока по сущности сервиса осуществляется через станцию `<type>:<id>`.
//            Например, станцией для запуска потока по треку будет `track:1234`.

//		currentIndex":0,"context":{"type":"radio","id":"playlist:Lizamishieva_1003","description":"Классинг","options":{"radioOptions":{"sessionId":"KFvb-b5I4Nd6UUipzNx-NoBW"}}},"tracks":[],"from":"web-own_playlists-playlist-radio-saved"}
//		{"currentIndex":0,"context":{"type":"radio","id":"playlist:Yellastro2_1047","description":"Vok","options":{"radioOptions":{"sessionId":"iYV5-9HUuKaeNTC9RzF-bbIt"}}},"tracks":[{"from":"web-own_playlists-playlist-radio-main","trackId":"112819428","albumId":25436021},{"from":"web-own_playlists-playlist-radio-main","trackId":"59046322","albumId":9021658},{"from":"web-own_playlists-playlist-radio-main","trackId":"35549638","albumId":4438695},{"from":"web-own_playlists-playlist-radio-main","trackId":"366400","albumId":37137},{"from":"web-own_playlists-playlist-radio-main","trackId":"80790738","albumId":14843907}],"from":"web-own_playlists-playlist-radio-saved"}
		val fData = getYamClient()!!.wave(fTag)
		val fWaveJson = fData.getJSONObject("result")
		val fTrackData = fWaveJson.getJSONArray("sequence")
		for (i in 0 until fTrackData.length())
		{
			val qTrackData = fTrackData.getJSONObject(i).getJSONObject("track")
			mTrackDataStore.put(qTrackData.getString("id"),qTrackData)
		}
		Thread {checkMapSize(mTrackDataStore as ConcurrentHashMap<Any,Any>)}.start()


		val fWave =
			mapper.readValue(fWaveJson.toString(), yWave::class.java)

		withContext(Dispatchers.IO){
			getYamClient()?.rotorStationFBRadioStarted(fWave.mId,fTag)
		}
//		Thread {
//			val fStartRes = getYamClient()?.rotorStationFBRadioStarted(fWave.mId,fTag)
//			val dbf = 0
//		}.start()
		//fWave.syncData(this)
		val sdf =0
		fWave.mStation = fTag
		if(fTitle!= "")
			fWave.mTitle = fTitle
		return fWave
	}

	suspend fun getAllTracks(clb: (Collection<iTrack>) -> Unit,
							 finClb: () -> Unit,
							 errorClb: (e: Exception) -> Unit = { e -> }) {
		val fResult = HashSet<iTrack>()
		// достает из лайв списка/базы/инета список плейлистов с треками в виде ток айдишников
		getYaMPlaylistsList() {
			// счетчик что бы отсчитать конец цикла для калбека конца
			var fCount = it.size
			for (qPl in it) {
				CoroutineScope(Dispatchers.IO).launch{
					// достает из списка айдишников список треков-обьектов (с обложками, именами итп)
					// из лайв списка/базы/инета
					var qRes = getTrackList(qPl.mTrackList)
					CoroutineScope(Dispatchers.Main).launch(){
						// убирает треки, которые уже были отправлены в калбек ранее
						qRes = qRes.filter { !fResult.contains(it) } as ArrayList<iTrack>
						fResult.addAll(qRes)
						// отправляет список треков - обьектов в калбек
						clb(qRes)
						fCount--
						if (fCount < 1)
							finClb()
					}
				}
			}
		}
	}

	suspend fun deletePllist(fPlist: YaPlaylist): Boolean {
		val fRes = getYamClient()?.removePlaylist(fPlist.mKindId)
		if (fRes != null && fRes){
			mPlListMap.remove(fPlist.mId)
			plDao.delete(fPlist)
			return true
		}
		return false
	}

	suspend fun getArtist(fArtistId: String): YaArtist? {
		val fArtistJson = JSONArray("[$fArtistId]")
		val fArtist = getYamClient()?.getObjList("artist",fArtistJson)

		print(fArtist)
		if (fArtist != null) {
			val fArtistObj = mapper.readValue(fArtist.getJSONArray("result").getJSONObject(0).toString(),
				YaArtist::class.java)
			return fArtistObj
		}
		return null
	}


}