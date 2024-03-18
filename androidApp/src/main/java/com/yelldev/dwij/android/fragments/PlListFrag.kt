package com.yelldev.dwij.android.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.yelldev.dwij.android.KeyStore
import com.yelldev.dwij.android.MainActivity
import com.yelldev.dwij.android.R
import com.yelldev.dwij.android.entitis.YaM.YaPlaylist
import com.yelldev.dwij.android.entitis.YaM.YaTrack
import com.yelldev.dwij.android.entitis.iPlaylist
import com.yelldev.dwij.android.models.ListPlListModel
import com.yelldev.dwij.android.utils.yLog
import com.yelldev.dwij.android.utils.yTimer
import com.yelldev.dwij.android.yMediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PlListFrag() : Fragment(R.layout.frag_list_pllist) {

	companion object {
		val PLAYLIST_ACTION = "playlist_action"
		val ACTION_ADDTRACK = "add_track"
		val ACTION_DATA = "action_data"

	}

	val LOG = yLog.log(PlListFrag::class.java.name)


	lateinit var mMainActivity: MainActivity
	lateinit var v_Recycl: RecyclerView

	var mGridSize = 0

	var mOnIteClick: (iPlaylist) -> Unit = {
			fPl: iPlaylist ->
		val snack = Snackbar.make(requireActivity().findViewById(android.R.id.content),
			"Start playlist ${fPl.mTitle}", Snackbar.LENGTH_LONG)

		snack.show()
		(requireActivity() as MainActivity).playThisList(fPl.mId) }

	var mPickedTrack: String = "-1"
	var mTrackObj: YaTrack? = null

	lateinit var model: ListPlListModel

	@SuppressLint("CheckResult", "NotifyDataSetChanged")
	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

		model = ViewModelProvider(this).get(ListPlListModel::class.java)

		val displayMetrics = DisplayMetrics()


		mMainActivity = activity as MainActivity

		mMainActivity.windowManager.defaultDisplay.getMetrics(displayMetrics)

		val width = displayMetrics.widthPixels
//		var height = displayMetrics.heightPixels
		mGridSize = width /3

		if(arguments != null){
			val fAction = requireArguments().getString(PLAYLIST_ACTION)
			val fTrackId = requireArguments().getString(ACTION_DATA)

			if(fAction == ACTION_ADDTRACK && fTrackId != null){
//				model.viewModelScope.launch {
				val fStore = yMediaStore.store(mMainActivity)
				model.viewModelScope.launch(Dispatchers.IO) {
					mTrackObj = fStore.getTrack(fTrackId)
					withContext(Dispatchers.Main) {
						model.getAdapter().setTrack(mTrackObj!!)
					}
				}
					mPickedTrack = fTrackId
					mOnIteClick = {
							fPl: iPlaylist ->
						GlobalScope.launch(Dispatchers.Default){
							(fPl as YaPlaylist).addTrack(fStore,mTrackObj!!)
							withContext(Dispatchers.Main) {
								val snack = Snackbar.make(view.rootView.findViewById(android.R.id.content),
								"${mTrackObj!!.mTitle} added to ${fPl.mTitle}",Snackbar.LENGTH_LONG)
								(activity as MainActivity).mNavController.popBackStack()
								snack.show()
							}
						}.start()
//					}
				}

//				Thread{
//					val fStore = yMediaStore.store(mMainActivity)
//					mTrackObj = fStore.getTrack(fTrackId)
//				}.start()

			}
		}else {
			mPickedTrack = "-1"
			mTrackObj = null
		}


		v_Recycl = view.findViewById<RecyclerView>(R.id.fr_ls_plls_recycl)

		model.getAdapter(
			mTrackObj
		)
		model.getAdapter().onClick = mOnIteClick
		model.getAdapter().mGridSize = mGridSize
		model.getAdapter().onCreatePlClick = {
			(activity as MainActivity).openFrame(CreateListFrag())
		}
		model.getAdapter().onLongItemClick = { fPlist ->
			val builder: AlertDialog.Builder = AlertDialog.Builder(requireContext())
			builder
				.setMessage("Удалить плейлист?!!")
				.setTitle("Точно?")
				.setPositiveButton("Yes,remove") { fD, o ->
					fD.dismiss()
					removePlList(fPlist)
				}
				.setNegativeButton("nenada") { fD, o -> fD.dismiss() }

			val dialog: AlertDialog = builder.create()
			dialog.show()

		}

		v_Recycl.adapter = model.getAdapter()
		v_Recycl.layoutManager = GridLayoutManager(context,3)
		model.viewModelScope.launch(Dispatchers.IO) {
			val fStore = yMediaStore.store(requireContext())
			fStore.getYaMPlaylistsList() {
				yTimer.timing(TAG,"notify adapter, size = ${it.size}")

				model.getAdapter ().setList(it as ArrayList<iPlaylist>)
				val fDEBAG = it[0]
				model.getAdapter ().notifyDataSetChanged()
				yTimer.timing(TAG,"notify adapter finish")
			}

		}


//		if (mTrackObj==null)
//			Thread{
//				val fLiked = fStore.getLikedTracks()
//				v_Recycl.post { f_adapt.dataSet.add(fLiked)
//				f_adapt.notifyDataSetChanged()}
//
//			}.start()


//		view.findViewById<Button>(R.id.fr_ls_plls_btn_sd)
//			.setOnClickListener { loadYaTracks() }
		view.findViewById<View>(R.id.fr_list_pllist_back).setOnClickListener {
			mMainActivity.mNavController.popBackStack()
		}

//		view.findViewById<View>(R.id.fr_ls_plls_btn_create).setOnClickListener {
//			(activity as MainActivity).openFrame(CreateListFrag())
//		}

	}

	fun removePlList(fPlist: iPlaylist) {
		model.viewModelScope.launch(Dispatchers.IO) {
			val fStore = yMediaStore.store(requireContext())
			val fRes = fStore.deletePllist(fPlist as YaPlaylist)
			withContext(Dispatchers.Main){
				if (fRes)
					model.getAdapter().removeItem(fPlist)
				else
					Snackbar.make(requireView(),KeyStore.s_network_error,Snackbar.LENGTH_LONG)
						.show()
			}

		}
	}

	fun loadYaTracks(){

	}


}