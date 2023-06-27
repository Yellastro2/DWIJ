package com.yelldev.dwij.android.utils

import android.R
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import android.util.Log
import android.widget.TextView
import java.io.BufferedReader
import java.io.File
import java.io.FileWriter
import java.io.IOException
import java.io.InputStreamReader
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date


class CrashCatcher(val context: Context, val chained: Boolean): Thread.UncaughtExceptionHandler {
	private val formatter: DateFormat = SimpleDateFormat("dd.MM.yy HH:mm")
	private val fileFormatter: DateFormat = SimpleDateFormat("dd-MM-yy")
	private var versionName = "0"
	private var versionCode = 0
	private var stacktraceDir: String? = null
	private var previousHandler: Thread.UncaughtExceptionHandler? = null

	init  {
		val mPackManager = context.packageManager
		val mPackInfo: PackageInfo
		try {
			mPackInfo = mPackManager.getPackageInfo(context.packageName, 0)
			versionName = mPackInfo.versionName
			versionCode = mPackInfo.versionCode
		} catch (e: PackageManager.NameNotFoundException) {
			// ignore
		}
		previousHandler =
			if (chained) Thread.getDefaultUncaughtExceptionHandler() else null
		stacktraceDir = String.format("/crash_logs/", context.packageName)
	}

	fun inContext(context: Context): CrashCatcher? {
		return CrashCatcher(context, true)
	}

	fun reportOnlyHandler(context: Context): CrashCatcher? {
		return CrashCatcher(context, false)
	}

	override fun uncaughtException(thread: Thread, exception: Throwable?) {
		val state = Environment.getExternalStorageState()
		val dumpDate = Date(System.currentTimeMillis())
		if (Environment.MEDIA_MOUNTED == state) {
			var fLog = ""
			try {
				val process = Runtime.getRuntime().exec("logcat -d")
				val bufferedReader = BufferedReader(
					InputStreamReader(process.inputStream)
				)
				val log = java.lang.StringBuilder()
				var line: String? = ""
//				var fLineCount = 0
//				val fLines = bufferedReader.lines()
//				val fCoung = fLines.count()
//
//				for( qLine in fLines){
//					if (fCoung - fLineCount < 200)
//						log.append(line + "\n")
//					fLineCount++
//				}
				val fListOfFknLogs = ArrayList<String>()
				while (bufferedReader.readLine().also { line = it } != null) {
					fListOfFknLogs.add(line + "\n")
//					log.append(line + "\n")
//					fLineCount++
				}
				val sfsd = 0
				for (i in fListOfFknLogs.size - 60 until fListOfFknLogs.size)
					fLog += fListOfFknLogs[i]
//				fLog = log.toString()
			} catch (e: IOException) {
			}
			val reportBuilder = StringBuilder()
			reportBuilder
				.append("\n\n\n")
				.append("brand: ${Build.BRAND}\n" +
						"${Build.DEVICE}\n" +
						"${Build.MODEL}\n\n")
				.append(formatter.format(dumpDate)).append("\n")
				.append(String.format("Version: %s (%d)\n", versionName, versionCode))
				.append(thread.toString()).append("\n")
				.append(fLog)
			thread.run {sendReport(reportBuilder.toString())}
			processThrowable(exception, reportBuilder)
//			val sd = context.cacheDir
//			val stacktrace = File(
//				sd.path + stacktraceDir,
//				java.lang.String.format(
//					"stacktrace-%s.txt",
//					fileFormatter.format(dumpDate)
//				)
//			)
//			val dumpdir = stacktrace.parentFile
//			val dirReady = dumpdir.isDirectory || dumpdir.mkdirs()
//			if (dirReady) {
//				var writer: FileWriter? = null
//				try {
//					writer = FileWriter(stacktrace, true)
//					writer.write(reportBuilder.toString())
//
//					}
//				} catch (e: IOException) {
//					e.printStackTrace()
//				} finally {
//					try {
//						writer?.close()
//					} catch (e: IOException) {
//						e.printStackTrace()
//					}
//				}
//			}
		}
		previousHandler?.uncaughtException(thread, exception)
	}

	private fun sendReport(fReport: String){
		Log.e("TAG",fReport)
		Log.i("TAG","Try to send report")
		val sendIntent: Intent = Intent().apply {
			action = Intent.ACTION_SEND
//            putExtra(Intent.EXTRA_TEXT, "This is my text to send.")
//            type = "text/plain"
			putExtra(Intent.EXTRA_TEXT, fReport)
			type = "text/plain"
		}
		val shareIntent = Intent.createChooser(sendIntent,
			"Ошибочка вышла. Отправьте пожалуйста ее мне")
		context.startActivity(sendIntent)
//		val intent = Intent(context, SettingsAct::class.java)
//		intent.setClass(context, SettingsAct::class.java)
//		intent.action = SettingsAct::class.java.getName()
//		intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
//		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
//		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//		context.startActivity(intent)

//		val builder = AlertDialog.Builder(context)
//		builder.setTitle("Androidly Alert")
//		builder.setMessage("We have a message")
////builder.setPositiveButton("OK", DialogInterface.OnClickListener(function = x))
//
//		builder.setPositiveButton(android.R.string.yes) { dialog, which ->
//			Toast.makeText(context,
//				android.R.string.yes, Toast.LENGTH_SHORT).show()
//		}
//
//		builder.setNegativeButton(android.R.string.no) { dialog, which ->
//			Toast.makeText(context,
//				android.R.string.no, Toast.LENGTH_SHORT).show()
//		}
//
//		builder.setNeutralButton("Maybe") { dialog, which ->
//			Toast.makeText(context,
//				"Maybe", Toast.LENGTH_SHORT).show()
//		}
//		builder.show()
		if(Build.BRAND == "TECNO"|| Build.BRAND == "POCO")
			System.exit(0)
	}

	private fun processThrowable(exception: Throwable?, builder: StringBuilder) {
		if (exception == null) return
		val stackTraceElements = exception.stackTrace
		builder
			.append("Exception: ").append(exception.javaClass.name).append("\n")
			.append("Message: ").append(exception.message).append("\nStacktrace:\n")
		for (element in stackTraceElements) {
			builder.append("\t").append(element.toString()).append("\n")
		}
		processThrowable(exception.cause, builder)
	}
}