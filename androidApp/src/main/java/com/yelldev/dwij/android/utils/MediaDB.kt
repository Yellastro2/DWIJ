package com.yelldev.dwij.android.utils

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import com.yelldev.dwij.android.entitis.sdTrack

class MediaDB(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {


	override fun onCreate(db: SQLiteDatabase) {
		db.execSQL(sdTrack.SQL_CREATE_ENTRIES)
	}
	override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
		// This database is only a cache for online data, so its upgrade policy is
		// to simply to discard the data and start over
		db.execSQL(SQL_DELETE_ENTRIES)
		onCreate(db)
	}
	override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
		onUpgrade(db, oldVersion, newVersion)
	}
	object FeedReaderContract {
		// Table contents are grouped together in an anonymous object.

	}

	companion object {
		object FeedEntry : BaseColumns {
			const val TABLE_NAME = "entry"
			const val COLUMN_NAME_TITLE = "title"
			const val COLUMN_NAME_SUBTITLE = "subtitle"
		}

		// If you change the database schema, you must increment the database version.
		const val DATABASE_VERSION = 1
		const val DATABASE_NAME = "FeedReader.db"
		private const val SQL_CREATE_ENTRIES =
			"CREATE TABLE ${FeedEntry.TABLE_NAME} (" +
					"${BaseColumns._ID} INTEGER PRIMARY KEY," +
					"${FeedEntry.COLUMN_NAME_TITLE} TEXT," +
					"${FeedEntry.COLUMN_NAME_SUBTITLE} TEXT)"

		private const val SQL_DELETE_ENTRIES = "DROP TABLE IF EXISTS ${FeedEntry.TABLE_NAME}"
	}
}