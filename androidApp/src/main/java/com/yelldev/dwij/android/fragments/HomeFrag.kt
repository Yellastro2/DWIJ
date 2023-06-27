package com.yelldev.dwij.android.fragments

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.yelldev.dwij.android.KeyStore
import com.yelldev.dwij.android.KeyStore.Companion.DWIJ_ACC_TOKEN
import com.yelldev.dwij.android.MainActivity
import com.yelldev.dwij.android.R
import com.yelldev.dwij.android.activities.SettingsAct
import com.yelldev.dwij.android.entitis.YaM.yWave
import com.yelldev.dwij.android.yMediaStore
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeFrag: Fragment(R.layout.fr_home) {



	@SuppressLint("CheckResult")
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		view.findViewById<View>(R.id.fr_home_pllist).setOnClickListener {
			(activity as MainActivity).mNavController.navigate(R.id.action_homeFrag_to_plListFrag)
//			(activity as MainActivity).openFrame(PlListFrag())
		}

		view.findViewById<View>(R.id.fr_home_tracks).setOnClickListener {
			val fBndl = Bundle()
			fBndl.putString(TrackListFrag.TRACKLIST_TYPE,TrackListFrag.STORAGE_LIST)
			(activity as MainActivity).mNavController
				.navigate(R.id.action_homeFrag_to_trackListFrag,fBndl)
		}

		view.findViewById<View>(R.id.fr_home_totalall_btn).setOnClickListener {
			openALLTracks()
		}

		view.findViewById<ImageButton>(R.id.fr_home_settngs).setOnClickListener {
			(activity as MainActivity).mNavController.navigate(R.id.action_homeFrag_to_settingsAct)
		}

		val mvSearch = view.findViewById<AutoCompleteTextView>(R.id.fr_home_search)

		view.findViewById<View>(R.id.fr_home_wave).setOnClickListener {
			showProgress()
			GlobalScope.launch(Dispatchers.IO){
				val fWave = yMediaStore.store(requireContext().applicationContext).getWave()
				withContext(Dispatchers.Main){
					finishProgress()
					if (fWave != null) {
						(activity as MainActivity).playWave(fWave)
					}
				}
			}
		}

		view.findViewById<View>(R.id.fr_home_acc).setOnClickListener {
			val sharedPref = requireActivity().getSharedPreferences(KeyStore.s_preff, AppCompatActivity.MODE_PRIVATE)
			val fToken = sharedPref.getString(DWIJ_ACC_TOKEN,"")
			val fYaLogin = sharedPref.getString(com.yelldev.dwij.android.KeyStore.k_ya_login, "")
			if (fToken.isNullOrEmpty()&& fYaLogin.isNullOrEmpty())
				(activity as MainActivity).mNavController.navigate(R.id.loginFrag)
			else
				(activity as MainActivity).mNavController.navigate(R.id.accountFrag)
		}
	}

	fun openALLTracks(){
		val fBndl = Bundle()
		fBndl.putString(TrackListFrag.TRACKLIST_TYPE,TrackListFrag.LIST_OF_ALL)
		(activity as MainActivity).mNavController
			.navigate(R.id.action_homeFrag_to_trackListFrag,fBndl)
	}

	lateinit var mDialog: AlertDialog

	private fun finishProgress() {
		mDialog.dismiss()
	}

	private fun showProgress() {
		val fDialBuilder = AlertDialog.Builder(requireContext())
		fDialBuilder.setTitle("Loading wave")
		fDialBuilder.setMessage("wait plz")
		mDialog = fDialBuilder.show()

//		mDialog?.setMessage("Done $fProg of $fMax")
	}
}