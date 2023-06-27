package com.yelldev.dwij.android.utils

import java.util.logging.FileHandler
import java.util.logging.Logger

class yLog {

	companion object{
		fun log(f_name: String): Logger {
			val yLog = Logger.getLogger(f_name)
			//yLog.addHandler(FileHandler("%t/log._%u_%g.txt",1024 * 1024, 100,true))
			return yLog
		}
	}
}