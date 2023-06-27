package com.yelldev.dwij.android.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.StatFs
import android.util.Log
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import com.yelldev.dwij.android.KeyStore
import com.yelldev.dwij.android.KeyStore.Companion.k_ya_login
import com.yelldev.dwij.android.KeyStore.Companion.k_ya_token
import com.yelldev.dwij.android.KeyStore.Companion.s_preff
import com.yelldev.dwij.android.R
import com.yelldev.yandexmusiclib.Account
import com.yelldev.yandexmusiclib.kot_utils.yAuth


class SettingsAct: Activity() {

	companion object{

		lateinit var mYaAuth: yAuth
		fun authYa(fAct: Activity){
			mYaAuth = yAuth(fAct)
			mYaAuth.login()
		}

		fun saveToken(fToken: String, fAct: Activity, param: (String) -> Unit = {}){
			val sharedPref = fAct.getSharedPreferences(s_preff,MODE_PRIVATE)
			with (sharedPref.edit()) {
				putString(com.yelldev.dwij.android.KeyStore.k_ya_token, fToken)
				apply()
			}

			Thread {
				var f_res = Account.showInformAccount(fToken).get()
				Log.i("DWIJ_TAG", f_res.toString())
				var fLogin = f_res
					.getJSONObject("result")
					.getJSONObject("account")
					.getString("login")
				fAct.runOnUiThread {
					val sharedPref = fAct.getSharedPreferences(s_preff,MODE_PRIVATE)
					with (sharedPref.edit()) {

						putString(com.yelldev.dwij.android.KeyStore.k_ya_login, fLogin)
						apply()
					}
					param(fLogin)
				}
			}.start()
		}

		fun onYamResult(resultCode: Int, data: Intent?, fAct: Activity, param: (String) -> Unit = {}): String {
			val fToken = mYaAuth.onResult(resultCode,data)
			saveToken(fToken,fAct,param)
			return fToken
		}
	}

	lateinit var vYaLoginText: TextView
	lateinit var vYaLoginBtn: Button
	var isYaLogin = false



	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.lay_settings)

		vYaLoginBtn = findViewById(R.id.ya_m_btn)
		vYaLoginText = findViewById(R.id.ya_m_auth_text)

		val sharedPref = getSharedPreferences(s_preff,MODE_PRIVATE)

		val fKey = sharedPref.getString(k_ya_token,"")

		if (fKey.equals("")){
			setNoYaAuth()
		}else{
			val fLogin = sharedPref.getString(k_ya_login,"nologin")!!
			setYaAuth(fLogin)
		}

		vYaLoginBtn.setOnClickListener {
			if (isYaLogin){
				val sharedPref = getSharedPreferences(s_preff,MODE_PRIVATE)
				with (sharedPref.edit()) {
					remove(k_ya_token)
					apply()
				}
				isYaLogin = false
				setNoYaAuth()
			}else{
				authYa(this)
			}
		}

		initCacheStoreSize()

	}

	fun initCacheStoreSize(){
		val fvSeekBar = findViewById<SeekBar>(R.id.act_sett_store_progress)
		val fvMin = findViewById<TextView>(R.id.act_sett_store_min)
		val fvMax = findViewById<TextView>(R.id.act_sett_store_max)
		val fvCur = findViewById<TextView>(R.id.act_sett_store_cur)

		val KILOBYTE = 1024

		val externalStatFs = StatFs(Environment.getExternalStorageDirectory().absolutePath)
		var externalTotal: Long
		var externalFree: Int
		externalTotal = ( externalStatFs.blockCountLong * externalStatFs.blockSizeLong) / ( KILOBYTE * KILOBYTE )
		externalFree = (( externalStatFs.availableBlocksLong * externalStatFs.blockSizeLong) / ( KILOBYTE * KILOBYTE )).toInt()

		val fMin = 200
		val fMax = externalFree
		fvSeekBar.max = fMax
		fvSeekBar.min = fMin
		fvMax.text = "${fMax/KILOBYTE}Gb"

		val fCur = getSharedPreferences(
			s_preff,
			AppCompatActivity.MODE_PRIVATE
		).getLong(KeyStore.kTrackCacheSize, KeyStore.sDefTrackCache)
		val fCurMb = (fCur / KILOBYTE / KILOBYTE ).toInt()
		if (fCurMb > KILOBYTE)
			fvCur.text = "${(fCurMb / KILOBYTE)}Gb"
		else
			fvCur.text = "${fCurMb}Mb"

		fvSeekBar.progress = fCurMb


		fvSeekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
			override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
				if (b) {
					if(i<KILOBYTE){
						fvCur.text = "${i}Mb"
					}else
						fvCur.text = "${i/KILOBYTE}Gb"
					val dsds =5
				}
			}

			override fun onStartTrackingTouch(seekBar: SeekBar) {
			}

			override fun onStopTrackingTouch(seekBar: SeekBar) {
				val fCurChanged = seekBar.progress
				if (fCurChanged < fCurMb){
					Snackbar.make(seekBar,"Память очистится в след. кешировании =_=",Snackbar.LENGTH_LONG)
						.show()
				}
				getSharedPreferences(
					s_preff,
					AppCompatActivity.MODE_PRIVATE)
					.edit()
					.putLong(KeyStore.kTrackCacheSize, (fCurChanged.toLong() * KILOBYTE * KILOBYTE))
					.apply()

				val dsf =0
			}
		})

	}

	private fun setYaAuth(fLogin: String) {
		vYaLoginBtn.text = getText(R.string.auth_btn_exit)
		isYaLogin = true
		vYaLoginText.text = fLogin
	}

	private fun setNoYaAuth() {
		vYaLoginText.text = getText(R.string.no_auth)
		vYaLoginBtn.text = getText(R.string.auth_btn)
	}


override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
	yAuth.REQUEST_LOGIN_SDK
	if (requestCode == yAuth.REQUEST_LOGIN_SDK) {
		val f_token = onYamResult(resultCode,data,this) { it -> setYaAuth(it) }

	} else {
		super.onActivityResult(requestCode, resultCode, data)
	}
}

}