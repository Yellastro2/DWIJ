package com.yelldev.dwij.android

import android.Manifest

class KeyStore {
	companion object {
        val USER = "user"
        val TAG = "DWIJ_TAG"

		val sPermission = Manifest.permission.READ_EXTERNAL_STORAGE

		val k_ya_token = "ya_token"
		val k_ya_login = "ya_login"
		val k_ya_id = "ya_id"
		val s_preff = "preff"
		val kTrackCacheSize = "track_cache_size"
		val sDefTrackCache = 1024 * 1024 * 1024 * 2L

		val COLOR_PINK = "#E91E63"

		val DWIJ_ACC_TOKEN = "dwijacctoken"

		val s_network_error = "Проблемы интернета"

		val STORAGE_TRACKLIST = "storage"
		val YANDEX_TRACKLIST = "Yam tracks"
		val TYPE = "type"
		val VALUE = "value"
	}
}