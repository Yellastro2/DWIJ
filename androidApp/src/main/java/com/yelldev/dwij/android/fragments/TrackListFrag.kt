package com.yelldev.dwij.android.fragments


import android.Manifest
import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.coroutineScope
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.yelldev.dwij.android.KeyStore.Companion.STORAGE_TRACKLIST
import com.yelldev.dwij.android.KeyStore.Companion.YANDEX_TRACKLIST
import com.yelldev.dwij.android.MainActivity
import com.yelldev.dwij.android.R
import com.yelldev.dwij.android.entitis.iTrack
import com.yelldev.dwij.android.entitis.iTrackList
import com.yelldev.dwij.android.models.PlayerModel
import com.yelldev.dwij.android.models.TrackListModel
import com.yelldev.dwij.android.utils.CashManager
import com.yelldev.dwij.android.utils.NoYandexLoginExceprion
import com.yelldev.dwij.android.utils.PermissionManager
import com.yelldev.dwij.android.yMediaStore
import com.yelldev.dwij.android.yStorageStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class TrackListFrag() : Fragment(R.layout.frag_tracklist) {

	val PLAYLIST_ID = "playlist_id"

//	var mList: ArrayList<iTrack> = ArrayList<iTrack>()
	lateinit var mMain: MainActivity
	var mvRecycl: RecyclerView? = null
//	var mvAdapter: CustomAdapter? = null
//	var mTrackList: iTrackList = MainActivity.SomeTrackList(mList, STORAGE_TRACKLIST)
	lateinit var mvTitle: TextView
	lateinit var mvStoreBtn: TextView
	lateinit var mvYamBtn: TextView
	lateinit var mModel: TrackListModel

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		mModel = ViewModelProvider(this).get(
			TrackListModel::class.java)

		mMain = activity as MainActivity

		mvTitle = view.findViewById(R.id.fr_tracklist_title)
		mvStoreBtn = view.findViewById(R.id.fr_tracklist_storage)
		mvYamBtn = view.findViewById(R.id.fr_tracklist_yam)

		mvRecycl = view.findViewById(R.id.fr_tracklist_recycl)
		mvRecycl!!.adapter = mModel.getAdapter(mMain)
		mvRecycl!!.layoutManager = LinearLayoutManager(context)

		if(arguments != null) {
			val fType = requireArguments().getString(TRACKLIST_TYPE)

			if (fType == STORAGE_LIST){

				mvTitle.visibility = View.GONE
				view.findViewById<View>(R.id.fr_tracklist_titlebar).visibility = View.VISIBLE
				mvYamBtn.setOnClickListener { setYamAllTracks()}
				mvStoreBtn.setOnClickListener { setStorageTracks()}
				if (mModel.mType==mModel.EMPTY_TYPE)
					setStorageTracks()
				else{
					selectMode(if (mModel.mType == STORAGE_LIST) mvStoreBtn else mvYamBtn)
				}

			} else if(fType == LIST_OF_ALL){
				view.findViewById<View>(R.id.fr_tracklist_titlebar).visibility = View.GONE
				mvTitle.visibility = View.VISIBLE
				mvTitle.text = "ваще всё"
				if (mModel.mType==mModel.EMPTY_TYPE){
					mModel.mType = LIST_OF_ALL
					mModel.getAdapter(mMain).setList(
						MainActivity.SomeTrackList(
						yStorageStore(requireContext()).getAllTracks(),
						"ALL_ALL"))
//					mTrackList = MainActivity.SomeTrackList(mList,STORAGE_TRACKLIST)
					CoroutineScope(Dispatchers.IO).launch {
						yMediaStore.store(requireContext()).getAllTracks ({
							mModel.getAdapter(mMain).addToList(it)
						},{})
					}
				}


			}
		}else {
			view.findViewById<View>(R.id.fr_tracklist_titlebar).visibility = View.GONE
			mvTitle.visibility = View.VISIBLE
			if (mModel.mType==mModel.EMPTY_TYPE)
				if (savedInstanceState == null) {
					mModel.mType = "somelist"
					mMain.mPlayer?.let {
//						mList = it.mList
//						mTrackList = it.mTrackList!!
						mvTitle.text = it.mTrackList!!.getTitle()
						mModel.getAdapter(mMain).setList(it.mTrackList!!)
					}
				} else {
					val fListId = savedInstanceState.getString(PLAYLIST_ID)
				}
		}




//		view.findViewById<TextView>(R.id.fr_tracklist_ya_btn)
//			.setOnClickListener { loadYaTracks() }

		view.findViewById<View>(R.id.fr_tracklist_back).setOnClickListener {
//			(activity as MainActivity).openFrame(PlListFrag())
//			mMain.onBackPressed()
			mMain.mNavController.popBackStack()
		}

	}

	var mSelectedView: View? = null



	private fun setStorageTracks() {
		PermissionManager(requireActivity())
			.setupPermissions(Manifest.permission.READ_EXTERNAL_STORAGE){
				CashManager.ScanMedia(requireActivity()){
					view?.post {
						mModel.getAdapter(mMain)
						.setList(MainActivity.SomeTrackList(it,STORAGE_TRACKLIST)) }

				}}
//		val fTrackArray = yStorageStore.store(requireContext()).getAllTracks()
//		mModel.getAdapter(mMain).setList(MainActivity.SomeTrackList(fTrackArray,STORAGE_TRACKLIST))
		mModel.mType = STORAGE_TRACKLIST
		selectMode(mvStoreBtn)
	}


	private fun setYamAllTracks() {
		mModel.mType = YANDEX_TRACKLIST
		// плашка о загрузке
		val snack = Snackbar.make(
			requireView(),
			"loading ALL tracks..", Snackbar.LENGTH_INDEFINITE
		)
		snack.show()
		// обьект со списком треков и типом, пока пустой
		val fList = MainActivity.SomeTrackList(ArrayList<iTrack>(),YANDEX_TRACKLIST)
		// корутина в IO потоке грузит все треки из яндекса и добавляет порциями в адаптер списка
		// на экране
		try {
			CoroutineScope(Dispatchers.IO).launch {
				try {
					yMediaStore.store(requireContext()).getAllTracks({
						// этот блок вызывается на каждом плейлисте и добавляет в адаптер порцию треков
						mModel.getAdapter(mMain).addToList(it)
					}, {
						// этот блок сработает кода все плейлисты вызовут предыдущий блок (конец сбора)
						snack.dismiss()
					})
				} catch (e: Exception) {
					withContext(Dispatchers.Main) {
						snack.dismiss()
						mMain.noYandexLoginError()
					}
				}

			}
		}
		catch (e: Exception){
			snack.dismiss()
			mMain.noYandexLoginError()
		}
		// меняет на экране выделение кнопочек яндекс/сдкарта
		selectMode(mvYamBtn)

		// добавляет обьект списка треков в адаптер списка на экране
		mModel.getAdapter(mMain).setList(fList)
	}

	private fun selectMode(fBtn: TextView) {
		mSelectedView?.background = null
		fBtn.setBackgroundColor(resources.getColor(R.color.colorAccent2))
		mSelectedView = fBtn
	}


	fun loadYaTracks(){
		val snack = Snackbar.make(requireView(),
			"in develop..", Snackbar.LENGTH_INDEFINITE)

		snack.show()
	}




	companion object {
		val LIST_OF_ALL: String = "LISTOFALL"
		val STORAGE_LIST: String = "storage_list"
		val TRACKLIST_TYPE: String = "tracklist_type"
	}

}