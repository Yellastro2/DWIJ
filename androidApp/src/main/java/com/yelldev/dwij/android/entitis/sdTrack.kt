package com.yelldev.dwij.android.entitis

import android.content.ContentValues
import android.content.Context
import android.database.AbstractCursor
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.provider.BaseColumns
import android.text.TextUtils.split
import android.util.Log
import com.yelldev.dwij.android.entitis.sdTrack.Companion.SdTrackEntity.COLUMN_NAME_TITLE
import com.yelldev.dwij.android.entitis.sdTrack.Companion.SdTrackEntity.TABLE_NAME
import com.yelldev.dwij.android.utils.MediaDB
import com.yelldev.dwij.android.yMediaStore
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers

val divider = "%S%"

class sdTrack (
    override val mId: String,
    override val mPath: String,
    override val mTitle: String = "",
    override val mArtist: String = "",
    override val mCover: String = "",
    val mHash: String
) : iTrack{

    override var mPlaylists = LinkedHashSet<String>()
    override var mAlbums = ArrayList<String>()

    fun getUri(): Uri {
        return Uri.parse(mPath)
    }

    override fun setToPlayer(f_Player: MediaPlayer,f_Ctx: Context,f_clb: ()-> Int) {
        f_Player
            .setDataSource(f_Ctx, getUri())
        f_clb()
    }

    override suspend fun set_Cover_toView(fStore: yMediaStore, f_size: Int): Bitmap? {
        try {
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(mPath)
            val data = retriever.getEmbeddedPicture()
            val opt = BitmapFactory.Options()
            if (data == null) {
                Log.e("sdTrack","data = null")
                return null
            }

            val opt_bounds = BitmapFactory.Options()

            val bitmap_bounds = BitmapFactory.decodeByteArray(data, 0, data!!.size, opt_bounds)
            val btm_size = Math.min(bitmap_bounds.height, bitmap_bounds.width)
            val f_scaling = btm_size / f_size

            opt.inSampleSize = f_scaling

            val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size, opt)
            retriever.release()
            return bitmap
        }catch (e: Exception){e.printStackTrace()
        return null}
//        return Single.create{subscriber ->
//            try {
//                val retriever = MediaMetadataRetriever()
//                retriever.setDataSource(mPath)
//                val data = retriever.getEmbeddedPicture()
//                val opt = BitmapFactory.Options()
//                if (data == null) {
//                    subscriber.onError(Exception("data = null"))
//                    return@create
//                }
//
//                val opt_bounds = BitmapFactory.Options()
//
//                val bitmap_bounds = BitmapFactory.decodeByteArray(data, 0, data!!.size, opt_bounds)
//                val btm_size = Math.min(bitmap_bounds.height, bitmap_bounds.width)
//                val f_scaling = btm_size / f_size
//
//                opt.inSampleSize = f_scaling
//
//                val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size, opt)
//                retriever.release()
//                subscriber.onSuccess(bitmap)
//            }catch (e: Exception){subscriber.onError(e)}
//        }
//            .observeOn(AndroidSchedulers.mainThread())
//            .subscribeOn(Schedulers.newThread())
    }

    override fun addPlaylist(fStore: yMediaStore, fPlId: String) {
        TODO("Not yet implemented")
    }

    override fun toString(): String {
        return mPath + divider + mTitle+ divider +mArtist+ divider +mCover +
                divider + mId
    }

    fun putToDB(dbHelper: MediaDB){
// Gets the data repository in write mode
        val db = dbHelper.writableDatabase

// Create a new map of values, where column names are the keys
        val values = ContentValues().apply {
            put(SdTrackEntity.COLUMN_NAME_TITLE, mTitle)
            put(SdTrackEntity.COLUMN_NAME_ARTIST, mArtist)
            put(SdTrackEntity.COLUMN_NAME_PATH, mPath)
            put(SdTrackEntity.COLUMN_NAME_COVER, mCover)
            put(SdTrackEntity.COLUMN_NAME_IDENT,mHash)
        }

// Insert the new row, returning the primary key value of the new row
        val newRowId = db?.insert(SdTrackEntity.TABLE_NAME, null, values)
    }

    companion object {

        object SdTrackEntity : BaseColumns {
            const val TABLE_NAME = "sd_tracks"
            const val COLUMN_NAME_TITLE = "title"
            const val COLUMN_NAME_ARTIST = "artist"
            const val COLUMN_NAME_PATH = "path"
            const val COLUMN_NAME_COVER = "cover"
            const val COLUMN_NAME_IDENT = "ident"
        }

        const val SQL_CREATE_ENTRIES =
            "CREATE TABLE ${SdTrackEntity.TABLE_NAME} (" +
                    "${BaseColumns._ID} INTEGER PRIMARY KEY," +
                    "${SdTrackEntity.COLUMN_NAME_TITLE} TEXT," +
                    "${SdTrackEntity.COLUMN_NAME_ARTIST} TEXT," +
                    "${SdTrackEntity.COLUMN_NAME_PATH} TEXT," +
                    "${SdTrackEntity.COLUMN_NAME_COVER} TEXT," +
                    "${SdTrackEntity.COLUMN_NAME_IDENT} TEXT)"

        fun getProj(): Array<String> {
            val projection = arrayOf(BaseColumns._ID,
                SdTrackEntity.COLUMN_NAME_TITLE,
                SdTrackEntity.COLUMN_NAME_ARTIST,
                SdTrackEntity.COLUMN_NAME_PATH,
                SdTrackEntity.COLUMN_NAME_COVER,
                SdTrackEntity.COLUMN_NAME_IDENT)
            return projection
        }

        fun fromCursor(cursor: Cursor): sdTrack? {
            var fTrack: sdTrack? = null
            with (cursor){
                fTrack = sdTrack(
                    "0",
                    getString(getColumnIndexOrThrow(SdTrackEntity.COLUMN_NAME_PATH)),
                    getString(getColumnIndexOrThrow(SdTrackEntity.COLUMN_NAME_TITLE)),
                    getString(getColumnIndexOrThrow(SdTrackEntity.COLUMN_NAME_ARTIST)),
                    getString(getColumnIndexOrThrow(SdTrackEntity.COLUMN_NAME_COVER)),
                    getString(getColumnIndexOrThrow(SdTrackEntity.COLUMN_NAME_IDENT))
                )
            }
            return fTrack
        }

        fun fromBD(dbHelper: MediaDB,fId: String): sdTrack?{
            val db = dbHelper.readableDatabase
            val projection = getProj()
            val selection = "${SdTrackEntity.COLUMN_NAME_IDENT} = ?"
            val selectionArgs = arrayOf(fId)
            val cursor = db.query(
                SdTrackEntity.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null               // The sort order
            )

            var fTrack: sdTrack? = null
                while (cursor.moveToNext()) {
                     fTrack= fromCursor(cursor)

                }
            cursor.close()
            return fTrack
        }

        fun getAll(dbHelper: MediaDB): ArrayList<sdTrack>{
            val db = dbHelper.readableDatabase
            val projection = getProj()
            val sortOrder = "${SdTrackEntity.COLUMN_NAME_TITLE} DESC"
            val cursor = db.query(
                SdTrackEntity.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null               // The sort order
            )

            val fTracks = ArrayList<sdTrack>()
            while (cursor.moveToNext()) {
                fromCursor(cursor)?.let {
                    fTracks.add(it) }

            }
            cursor.close()
            return fTracks
        }
    }


}

