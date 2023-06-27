package com.yelldev.dwij.android.utils

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.yelldev.dwij.android.MainActivity

class PermissionManager(val context: Activity) {
	val mCallbacks = HashMap<String,(Boolean)->Unit>()

	fun setupPermissions(fPermission: String,onGranted:(Boolean)->Unit) {
		val permission = ContextCompat.checkSelfPermission(context,
			fPermission)

		if (permission != PackageManager.PERMISSION_GRANTED) {
			MainActivity.LOG.info( "Permission to storage denied")
			makeRequest(fPermission)
			mCallbacks.put(fPermission,onGranted)
		}else{
			onGranted(true)
		}
	}
	private fun makeRequest(fPermission: String) {
		ActivityCompat.requestPermissions(context,
			arrayOf(fPermission),
			MainActivity.RECORD_REQUEST_CODE
		)
	}
	fun onRequestPermissionsResult(requestCode: Int,
											permissions: Array<String>, grantResults: IntArray) {
		when (requestCode) {
			MainActivity.RECORD_REQUEST_CODE -> {

				if (grantResults.isEmpty() || grantResults[0] != PackageManager.PERMISSION_GRANTED) {

					MainActivity.LOG.info( "Permission has been denied by user")
					val snack = Snackbar.make(
						context.findViewById<View>(android.R.id.content),
						"Need access to read media",
						Snackbar.LENGTH_LONG)
					snack.setAction("GRANT"
					) {
						val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
						val uri = Uri.fromParts("package", context.packageName, null)
						intent.data = uri
						context.startActivity(intent)
					}
					snack.show()
				} else {
					MainActivity.LOG.info( "Permission has been granted by user")
					mCallbacks.get(permissions[0])
				}
			}
		}
	}
}