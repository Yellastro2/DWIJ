package com.yelldev.dwij.android.models

import androidx.lifecycle.ViewModel
import com.yelldev.dwij.android.KeyStore
import com.yelldev.dwij.android.MainActivity
import com.yelldev.dwij.android.adapters.TrackListAdapter
import com.yelldev.dwij.android.entitis.iTrack
import com.yelldev.dwij.android.fragments.TrackListFrag

class TrackListModel: ViewModel() {

    val EMPTY_TYPE: String = "empty"

    var mType: String = EMPTY_TYPE

    private var mAdapter: TrackListAdapter? = null

    fun getAdapter(fMain: MainActivity): TrackListAdapter {
        if(mAdapter == null)
            mAdapter = TrackListAdapter(fMain,
                MainActivity.SomeTrackList(ArrayList<iTrack>(),
                    KeyStore.STORAGE_TRACKLIST
            ))

        return mAdapter!!
    }

}