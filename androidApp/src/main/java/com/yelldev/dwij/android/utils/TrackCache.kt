package com.yelldev.dwij.android.utils

import android.net.Uri
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Update
import com.yelldev.dwij.android.KeyStore
import com.yelldev.dwij.android.KeyStore.Companion.TAG
import com.yelldev.dwij.android.KeyStore.Companion.kTrackCacheSize
import com.yelldev.dwij.android.KeyStore.Companion.sDefTrackCache
import com.yelldev.dwij.android.entitis.YaM.YaTrack
import com.yelldev.dwij.android.yMediaStore
import com.yelldev.yandexmusiclib.kot_utils.yTrack
import com.yelldev.yandexmusiclib.yClient
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths

class TrackCache(mStore: yMediaStore) {

	companion object {
		const val YANDEX = "yandex"
		val RESTRICTED  = 1
		const val DEFAULT = 0
	}

	val mCtx = mStore.mCtx
	var mYamClient: yClient? = null
	val db = mStore.db

	val sMp3CacheDir = "${mCtx.dataDir.absolutePath}/mp3_store"

	init {
		val dir = File(sMp3CacheDir)
		if (!dir.exists())
			dir.mkdirs()
		GlobalScope.launch(Dispatchers.IO){
			mYamClient =mStore.getYamClient()

		}
	}


	@Entity(tableName = "tracks_cache")
	data class yTrackCash(@PrimaryKey
						  val mId: String,
						  var mPath: String,
						  var mLastCall: Long,
						  @ColumnInfo(defaultValue = DEFAULT.toString())
						  var mType: Int,
						  @ColumnInfo(defaultValue = YANDEX)
						  var mSource: String
		){
		@Dao
		interface TrackCacheDao {

			@Query("SELECT * FROM tracks_cache WHERE mLastCall = ( SELECT MIN(mLastCall) FROM tracks_cache )")
			fun getLast(): yTrackCash

			@Query("SELECT * FROM tracks_cache WHERE mId IN (:plIds)")
			fun loadById(plIds: String): yTrackCash

			@Update
			fun updateRow(fPlList: yTrackCash)

			@Insert
			fun insertAll(vararg users: yTrackCash)

			@Delete
			fun delete(fTr: yTrackCash)
		}
	}

	fun getCachedTrack(fYTrack: YaTrack,
					   fOnCached: (String,Boolean) -> Unit,
					   fOnMissed: (Uri) -> Unit)
	{
		val fDaoCache = db.TrackCacheDao()
		var fCached = fDaoCache.loadById(fYTrack.mId)
		if (fCached == null){
			if (mYamClient == null)
				throw NoYandexLoginExceprion()
			val fLink = yTrack.mp3Link(mYamClient!!,fYTrack.mId)?.get()
			val fUri = Uri.parse(fLink)
			fOnMissed(fUri)
//если нет, запускаем обновление новым потоком и возвращаем нуль
			Single.create<Unit> { subscriber ->
				try {
					val url = URL(fLink)
					val fPath = "$sMp3CacheDir/ya_${fYTrack.mId}.mp3"

					url.openStream().use {

						try{
							Files.copy(it, Paths.get(fPath))
						}catch (e: Exception){
							Log.e(TAG,e.stackTraceToString())
						}
						fCached = yTrackCash(fYTrack.mId,fPath,System.currentTimeMillis(),
							0, YANDEX)
						fDaoCache.insertAll(fCached)
						var fCacheSize = dirSize(File(sMp3CacheDir))
						val fMaxSize = mCtx.getSharedPreferences(
							KeyStore.s_preff,
							AppCompatActivity.MODE_PRIVATE
						).getLong(kTrackCacheSize,sDefTrackCache)
						while (fCacheSize > fMaxSize) {
							val fLast = fDaoCache.getLast()
							File(fLast.mPath).delete()
							fDaoCache.delete(fLast)
							fCacheSize = dirSize(File(sMp3CacheDir))
//							if (fLast.mType!= RESTRICTED){
//								try {
//									val fLink = yTrack.mp3Link(mYamClient!!, fLast.mId)?.get()
//									val fUri = Uri.parse(fLink)
//									fOnMissed(fUri)
//									val url = URL(fLink)
//									url.openStream().use {
//
//									}
//									File(fLast.mPath).delete()
//									fDaoCache.delete(fLast)
//									fCacheSize = dirSize(File(sMp3CacheDir))
//								}catch (e: Exception){
//									fLast.mType = RESTRICTED
//									fLast.mLastCall = System.currentTimeMillis()
//									fDaoCache.updateRow(fLast)
//								}

//							}
//							File(fLast.mPath).delete()
//							fDaoCache.delete(fLast)
//							fCacheSize = dirSize(File(sMp3CacheDir))
						}


						val fds = 0
					}
				} catch (e: Exception) {
					subscriber.onError(e)
				}
			}
				.observeOn(Schedulers.newThread())
				.subscribeOn(Schedulers.newThread())
				.subscribe()

		}else{
			fOnCached(fCached.mPath, fCached.mType == RESTRICTED)
			fCached.mLastCall = System.currentTimeMillis()
			fDaoCache.updateRow(fCached)
			/*проверяем размер кеша если больше разрешенного:
				ищем "где-то" запись с самым древним обращением и удаляем
				do while*/
		}
	}

	private fun dirSize(dir: File): Long {
		if (dir.exists()) {
			var result: Long = 0
			val fileList = dir.listFiles()
			if (fileList != null) {
				for (i in fileList.indices) {
					// Recursive call if it's a directory
					result += if (fileList[i].isDirectory) {
						dirSize(fileList[i])
					} else {
						// Sum the file size in bytes
						fileList[i].length()
					}
				}
			}
			return result // return the file size in bytes
		}
		return 0
	}
}