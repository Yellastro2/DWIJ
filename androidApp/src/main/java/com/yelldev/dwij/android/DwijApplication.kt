package com.yelldev.dwij.android

import android.app.Application
import com.yelldev.dwij.android.utils.RoboErrorReporter

class DwijApplication: Application() {
	override fun onCreate() {
		RoboErrorReporter.bindReporter(this)
		super.onCreate()

	}
}