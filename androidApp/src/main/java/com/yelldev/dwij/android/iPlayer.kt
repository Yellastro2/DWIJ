package com.yelldev.dwij.android

interface iPlayer {
	fun play()
	fun pause()
	fun next()
	fun previous()
	fun isPlay()
	fun isRandom()
	fun setRandom()
	fun duration()
	fun position()
	fun setOnPrepareListener(fOnPrepare: ()-> Unit)
	fun setOnCompleteListener(fOnComplete: ()-> Unit)
}