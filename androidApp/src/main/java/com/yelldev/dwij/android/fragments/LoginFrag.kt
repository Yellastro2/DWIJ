package com.yelldev.dwij.android.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.yelldev.dwij.android.KeyStore
import com.yelldev.dwij.android.KeyStore.Companion.DWIJ_ACC_TOKEN
import com.yelldev.dwij.android.MainActivity
import com.yelldev.dwij.android.R
import com.yelldev.dwij.android.activities.SettingsAct
import com.yelldev.dwij.android.activities.SettingsAct.Companion.authYa
import com.yelldev.yandexmusiclib.kot_utils.yAuth

val RC_SIGN_IN = 235433
val TAG = "account frag"

class LoginFrag: Fragment(R.layout.fr_login)  {
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		view.findViewById<View>(R.id.fr_login_google).setOnClickListener { googleAuth() }
		view.findViewById<View>(R.id.fr_login_yandex).setOnClickListener { authYa(requireActivity()) }
	}

	fun googleAuth(){
		val gso: GoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
			.requestEmail()
			.build()
		var mGoogleSignInClient = GoogleSignIn.getClient(requireActivity(), gso);
		val signInIntent = mGoogleSignInClient.signInIntent


		startActivityForResult(signInIntent, RC_SIGN_IN)
	}

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)

		if (requestCode == RC_SIGN_IN) {
			val task = GoogleSignIn.getSignedInAccountFromIntent(data)
			handleSignInResult(task)
		}
		if (requestCode == yAuth.REQUEST_LOGIN_SDK) {
			val f_token =
				SettingsAct.onYamResult(resultCode, data, requireActivity()) {
					(activity as MainActivity).mNavController.navigate(R.id.accountFrag)
				}
//			Token.token = f_token
		}
	}

	private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {
		try {
			val account = completedTask.getResult(ApiException::class.java)

			val fMail = account.email!!
			Thread{
				val sharedPref = requireActivity().getSharedPreferences(KeyStore.s_preff, AppCompatActivity.MODE_PRIVATE)
				sharedPref.edit().putString(DWIJ_ACC_TOKEN,fMail).apply()
//				val fToken = sharedPref.getString(DWIJ_ACC_TOKEN,"")
//				val fRes = Api.googleAuth(fMail)
//				if (fRes.getInt("code") == 200){
//					requireActivity().getSharedPreferences(EntCodeFrag.PREF, Context.MODE_PRIVATE)
//						.edit()
//						.putString(EntCodeFrag.TOKEN,fRes.getString("token"))
//						.putString(MainActivity.LOGIN,fMail)
//						.apply()
//					view?.post {
//						val navHostFragment =
//							requireActivity().supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
//						val navController = navHostFragment.navController
//						navController.navigate(R.id.action_loginFrag_to_homeFrag) }
//				}else{
					view?.post { (activity as MainActivity).mNavController.navigate(R.id.accountFrag)}
//				}
			}.start()
		} catch (e: ApiException) {
			Log.w(TAG, "signInResult:failed code=" + e.statusCode)
			Snackbar.make(requireView(),getString(R.string.snack_google_error), Snackbar.LENGTH_LONG)
				.show()
		}
	}
}