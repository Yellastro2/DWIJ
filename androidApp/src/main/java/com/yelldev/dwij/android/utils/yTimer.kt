package com.yelldev.dwij.android.utils

import android.util.Log

class yTimer {

	companion object {
		private var mTimer = 0

		private var mLast = System.currentTimeMillis()

		fun timing(fTag: String = "yTimer", fMessage: String = ""){
			val fNow = System.currentTimeMillis()
			var fMessageVar = fMessage
			if (fMessage == "") fMessageVar = "Timer point $mTimer"
			Log.i(fTag,"$fMessageVar: ${fNow - mLast}")
			mLast = fNow
			mTimer++
		}
	}
}