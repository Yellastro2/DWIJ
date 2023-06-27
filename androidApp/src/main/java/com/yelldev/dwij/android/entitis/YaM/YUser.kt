package com.yelldev.dwij.android.entitis.YaM

import kotlin.random.Random

class yUser(val mId: String,
			val mLogin: String,
			val mDesk: String,
			val mAva: String) {

	fun getOnAir(): Boolean{
		return 0 == Random.nextInt(2)
	}
}