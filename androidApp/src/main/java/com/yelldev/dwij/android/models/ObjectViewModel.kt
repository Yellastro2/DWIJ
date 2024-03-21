package com.yelldev.dwij.android.models

import androidx.lifecycle.ViewModel
import com.yelldev.dwij.android.KeyStore
import com.yelldev.dwij.android.MainActivity
import com.yelldev.dwij.android.adapters.TrackListAdapter
import com.yelldev.dwij.android.entitis.iTrack
import com.yelldev.dwij.android.entitis.yEntity

class ObjectViewModel : ViewModel() {
    var mUser: String? = null
    var mDataObject: yEntity? = null
    var mValue: String = ""

    // TODO: Implement the ViewModel
    var mType: String = ""

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