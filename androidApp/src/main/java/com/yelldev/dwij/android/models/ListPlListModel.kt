package com.yelldev.dwij.android.models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yelldev.dwij.android.adapters.ListPlLsAdapter
import com.yelldev.dwij.android.entitis.YaM.YaTrack
import com.yelldev.dwij.android.entitis.iPlaylist
import com.yelldev.dwij.android.fragments.PlListFrag

class ListPlListModel: ViewModel() {

	var mAdapter: ListPlLsAdapter? = null

	fun getAdapter(fTrack: YaTrack? = null): ListPlLsAdapter {
		if(mAdapter == null) mAdapter = ListPlLsAdapter(fTrack)

		val fAdapter = mAdapter!!

		if (fAdapter.mTrack != fTrack && fTrack!=null) fAdapter.mTrack = fTrack

		fAdapter.mScope = viewModelScope


		return fAdapter
	}
}