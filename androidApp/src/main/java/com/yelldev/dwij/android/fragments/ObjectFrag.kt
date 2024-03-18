package com.yelldev.dwij.android.fragments

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.yelldev.dwij.android.KeyStore
import com.yelldev.dwij.android.R
import com.yelldev.dwij.android.models.ObjectViewModel
import com.yelldev.dwij.android.yMediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ObjectFrag : Fragment(R.layout.fragment_object) {

    companion object {
        val PLAYLIST = "playlust"

        fun newInstance() = ObjectFrag()
    }

    private val mViewModel: ObjectViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(arguments != null) {
            mViewModel.mType = requireArguments().getString(KeyStore.TYPE)!!
            mViewModel.mValue = requireArguments().getString(KeyStore.VALUE)!!

            if (mViewModel.mType == PLAYLIST){
                mViewModel.viewModelScope.launch {
                    mViewModel.mDataObject = yMediaStore.store(requireContext())
                        .getYamPlaylist(mViewModel.mValue)

                    loadObject()
                }
            }
        }




        view.findViewById<View>(R.id.fr_obj_recycler)
    }

    private fun loadObject() {

        mViewModel.mDataObject?.let {
            lifecycleScope.launch(Dispatchers.Default){
                val fRes = it.getImage(yMediaStore.store(requireContext()))
                withContext(Dispatchers.Main){
                    requireView().findViewById<ImageView>(R.id.fr_object_image)
                        .setImageBitmap(fRes)
                }
            }
            requireView().findViewById<TextView>(R.id.fr_object_title)
                .text = it.getTitle()
            requireView().findViewById<TextView>(R.id.fr_object_title2)
                .text = it.getInfo()
        }
    }

}