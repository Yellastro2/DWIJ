package com.yelldev.dwij.android.utils

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.media.MediaMetadataRetriever
import android.provider.MediaStore
import android.util.Log
import android.view.View
import com.google.android.material.snackbar.Snackbar
import com.yelldev.dwij.android.KeyStore
import com.yelldev.dwij.android.MainActivity
import com.yelldev.dwij.android.R
import com.yelldev.dwij.android.entitis.iTrack
import com.yelldev.dwij.android.entitis.sdTrack
import java.io.File

class CashManager {
	companion object{

		val TAG = "CashManager"
	fun ScanMedia(context: Activity,onScanFinish:(ArrayList<iTrack>)-> Unit){
		yTimer.timing(TAG,"ScanMedia()")
//		Log.i(KeyStore.TAG,"scan")
		val fTitleHolder = context.resources.getString(R.string.title_placehold)
		var snack: Snackbar?= null
		Thread{
			val fNewList = ArrayList<iTrack>()

			val f_start_timer = System.currentTimeMillis()
			val dbHelper = MediaDB(context)
			val f_List = sdTrack.getAll(dbHelper) as ArrayList<iTrack>
			yTimer.timing(TAG,"ScanMedia: db.getAll()")
			if (f_List.size>0) {
				MainActivity.LOG.info("find cached tracks: ${f_List.size}")
				onScanFinish(f_List)
			}else {
				MainActivity.LOG.info("cache not found")
				context.runOnUiThread {
					snack = Snackbar.make(
						context.findViewById<View>(android.R.id.content),
						"Scan media storage", Snackbar.LENGTH_INDEFINITE
					)

					snack?.show()
				}
			}

			val proj = arrayOf(
				MediaStore.Audio.Media.DATA
			)

			val select = MediaStore.Audio.Media.IS_MUSIC + "!=0"
			val contentResolver1 = ContextWrapper(context).contentResolver
			contentResolver1.query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
				proj,
				select,
				null,
				MediaStore.Audio.Media.DEFAULT_SORT_ORDER
			)?.use { cursor ->
				try{
					Log.i("scan",cursor.count.toString() + "s")
					while (cursor.moveToNext()) {
						val qPath = cursor.getString(
							(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA))
						)

						val mmr = MediaMetadataRetriever();
						try {
							mmr.setDataSource(qPath);

						}catch (e: Exception){
							MainActivity.LOG.info("set data sourse: $qPath")
							MainActivity.LOG.warning(e.stackTraceToString())
							continue
						}

						val q_dur =
							mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
						val name = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
						val desc = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
						val q_id = "$name._$desc._$q_dur"

						var q_Track = sdTrack.fromBD(dbHelper,q_id)
						if(q_Track==null){
							q_Track = sdTrack(
								"0",
								qPath,
								name?: fTitleHolder,
								desc?: fTitleHolder,
								mHash = q_id
							)
							q_Track.putToDB(dbHelper)
							fNewList.add(q_Track)
						}
					}
				}catch (e: Exception){
					MainActivity.LOG.warning(e.toString())
				}
			}
			yTimer.timing(TAG,"ScanMedi: cursor.moveToNext() fin")
			context.runOnUiThread {
				snack?.dismiss()
				if(fNewList.size>0){

					val snack = Snackbar.make(context.findViewById<View>(android.R.id.content),
						"Found ${fNewList.size} new tracks",Snackbar.LENGTH_LONG)
					snack.show()
					onScanFinish(fNewList)
				}
			}
		}.start()

	}}


}