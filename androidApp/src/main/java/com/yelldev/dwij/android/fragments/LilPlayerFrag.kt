package com.yelldev.dwij.android.fragments

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.yelldev.dwij.android.MainActivity
import com.yelldev.dwij.android.R

class LilPlayerFrag() :
	PlayerAbs() {

	@SuppressLint("MissingInflatedId")
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {
		val view = inflater.inflate(R.layout.fr_lil_player,container,false)

		//mvSeekBar = view.findViewById(R.id.seekBar)
		mvTitle = view.findViewById(R.id.fr_lil_player_title)
		mvArtist = view.findViewById(R.id.fr_lil_player_artist)
		mvCover = view.findViewById(R.id.fr_lil_pl_image)
		mvPlay = view.findViewById(R.id.fr_lil_pl_play)
		mvPrev = view.findViewById(R.id.fr_lil_pl_prev)
		mvNext = view.findViewById(R.id.fr_lil_pl_next)
		view.setOnClickListener { (activity as MainActivity).openPlayer() }
//		mvRandom = view.findViewById<ImageButton>(R.id.fr_player_random)

		return view
	}
}