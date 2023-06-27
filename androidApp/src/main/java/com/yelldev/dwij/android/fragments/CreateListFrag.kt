package com.yelldev.dwij.android.fragments

import android.os.Bundle
import android.view.View
import android.widget.CheckBox
import android.widget.EditText
import androidx.fragment.app.Fragment
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.yelldev.dwij.android.MainActivity
import com.yelldev.dwij.android.R
import com.yelldev.dwij.android.yMediaStore

class CreateListFrag: Fragment(R.layout.fr_create_list) {

	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
		val fvTitle = view.findViewById<EditText>(R.id.fr_createlist_title)
		val fvPrivat = view.findViewById<CheckBox>(R.id.fr_create_list_priv_box)
		view.findViewById<View>(R.id.fr_create_list_btn).setOnClickListener {
			Thread(){
				yMediaStore.store(requireContext()).mYamClient?.create_playlist(

				fvTitle.text.toString(),!fvPrivat.isChecked)
				view.post(){Snackbar.make(view.findViewById(android.R.id.content),
					"Playlist sozdan", Snackbar.LENGTH_LONG)
					.show()}

			}.start()
			activity?.onBackPressed()
		}

	}
}