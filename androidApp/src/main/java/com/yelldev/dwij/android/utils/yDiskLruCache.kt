package com.yelldev.dwij.android.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.FileOutputStream
import java.util.concurrent.locks.Condition
import java.util.concurrent.locks.ReentrantLock


private const val DISK_CACHE_SIZE = 1024 * 1024 * 10 // 10MB
private const val DISK_CACHE_SUBDIR = "thumbnails"
private val diskCacheLock = ReentrantLock()
private val diskCacheLockCondition: Condition = diskCacheLock.newCondition()
private var diskCacheStarting = true

private const val COMPRESS_QUALITY = 100


class yDiskLruCache(mCtx: Context) {

	val cacheDir: File
	val mCacheSize = 1024 * 1024 * 100

	init{
		// Initialize memory cache
//		...
		// Initialize disk cache on background thread

		cacheDir = File(mCtx.applicationContext.cacheDir, DISK_CACHE_SUBDIR)
		cacheDir.mkdirs();

//		InitDiskCacheTask().execute(cacheDir)
	}
	fun adaptKey(key: String): String {
		return key.replace("/","_")
	}

	fun addBitmapToCache(key: String, bitmap: Bitmap) {
		val key2 = adaptKey(key)
		val stream = FileOutputStream(cacheDir.absolutePath + "/" + key2)
		bitmap.compress(Bitmap.CompressFormat.JPEG, COMPRESS_QUALITY, stream)
		stream.close()
	}

	fun getBitmapFromDiskCache(key: String): Bitmap? {
		val key2 = adaptKey(key)

		val bitmap = BitmapFactory.decodeFile(cacheDir.absolutePath + "/"+ key2)
		return bitmap
	}
}