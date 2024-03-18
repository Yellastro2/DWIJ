package com.yelldev.dwij.android.fragments

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewPropertyAnimator
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.recyclerview.widget.RecyclerView
import com.yelldev.dwij.android.KeyStore.Companion.COLOR_PINK
import com.yelldev.dwij.android.MainActivity
import com.yelldev.dwij.android.R
import com.yelldev.dwij.android.entitis.iPlaylist
import com.yelldev.dwij.android.entitis.iTrack
import com.yelldev.dwij.android.entitis.iTrackList
import com.yelldev.dwij.android.yMediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class BigPlayerFrag() :
	PlayerAbs() {

	val sPrevieAlpha = 0.3F

	lateinit var mvMainTitle: TextView
	lateinit var mvTrackList: Button
	lateinit var mvPlListFlexbox: RecyclerView
	lateinit var mvToPlaylist: View
	lateinit var mvAlbum: TextView
	lateinit var mvLike: ImageView

	var mTrack: iTrack? = null

	var isLiked = false

	@SuppressLint("MissingInflatedId")
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View? {


		val view = inflater.inflate(R.layout.frag_player,container,false)

		mvSeekBar = view.findViewById(R.id.seekBar)
		mvTitle = view.findViewById(R.id.txt_title)
		mvArtist = view.findViewById(R.id.txt_artist)
		mvCover = view.findViewById(R.id.fr_player_cover)
		mvPlay = view.findViewById<ImageButton>(R.id.btn_play)
		mvPrev = view.findViewById<ImageButton>(R.id.btn_prev)
		mvNext = view.findViewById<ImageButton>(R.id.btn_next)
		mvRandom = view.findViewById<ImageButton>(R.id.fr_player_random)
		mvMainTitle = view.findViewById(R.id.bigplayer_main_title)
		mvAlbum = view.findViewById<TextView>(R.id.fr_player_album_name)
		mvLike = view.findViewById<ImageView>(R.id.fr_player_like)



		var doubleClick = false

		mvLike.alpha = 0.0F
		mvAlbum.alpha = 0.0F

		showPreviewLike()

		var isLike = false

		mvCover.setOnClickListener {
			val fAnims = showPreviewLike()

			if (doubleClick!!) {
				val fStore = yMediaStore.store(requireContext())
				mModel.viewModelScope.launch(Dispatchers.IO){
					fStore.likeTrack(mTrackId)
					withContext(Dispatchers.Main){
						setLikedState(fStore)
					}
				}
				isLike = !isLike
				fAnims.forEach { it.cancel() }
				mvAlbum.animate()
					.alpha(0.0F)
					.setDuration(300)
				var fFirstDur = 300
				var fSecDur = 200
				if (isLike){
					fFirstDur = 100
					fSecDur = 400
				}
				val fSecondAnim = mvLike.animate()
					.alpha(1F)
					.setDuration(fFirstDur.toLong())
					.withEndAction { mvLike.animate()
						.alpha(0.0F)
						.setDuration(fSecDur.toLong())
					}
			}else
				doubleClick = true
			Handler().postDelayed({ doubleClick = false }, 500)
		}

		view.findViewById<View>(R.id.fr_bg_player_btn_close).setOnClickListener {
			(activity as MainActivity).mNavController.navigate(R.id.action_bigPlayerFrag_to_trackListFrag)
		}

		val fvRepeat = view.findViewById<ImageView>(R.id.fr_player_cycle)
		fvRepeat.setOnClickListener {
			mPlayer?.let {
				mPlayer?.isRepeat = !mPlayer?.isRepeat!!
				if (mPlayer?.isRepeat!!)
					fvRepeat.setBackgroundColor(Color.GREEN)
				else
					fvRepeat.background = null
			}

		}

		if (mPlayer?.isRepeat!!)
			fvRepeat.setBackgroundColor(Color.GREEN)
		else
			fvRepeat.background = null



		mvSeekBar?.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
			override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
				if (b) {
					mPlayer?.mMediaPlayer?.seekTo(i )
					val dsds =5
				}
			}

			override fun onStartTrackingTouch(seekBar: SeekBar) {
			}

			override fun onStopTrackingTouch(seekBar: SeekBar) {
			}
		})
//		initializeSeekBar()
		mvToPlaylist = view.findViewById<View>(R.id.fr_bigplay_topl_text)
		mvToPlaylist.setOnClickListener{
			toPlaylist()
		}

		mvPlListFlexbox = view.findViewById(R.id.fr_bg_player_pllist_flex)
		mvPlListFlexbox.setOnClickListener {
			toPlaylist()
		}
		val displayMetrics = DisplayMetrics()
		requireActivity().windowManager.defaultDisplay.getMetrics(displayMetrics)

		var width = displayMetrics.widthPixels
		mvCover.layoutParams = ConstraintLayout.LayoutParams(width,width)

		if (mTrack != null){
			setTrack(mTrack!!,null)
		}

		return view
	}


	override fun onResume() {
		super.onResume()
		attachToService()
	}

	private fun toPlaylist() {
		val fPlFrag = PlListFrag()
		val fBndl = Bundle()
		fBndl.putString(PlListFrag.PLAYLIST_ACTION,PlListFrag.ACTION_ADDTRACK)
		if(mPlayer != null){
//			val fTr = mPlayer!!.mList.get(mPlayer!!.m_CurentTrack).mId
			fBndl.putString(PlListFrag.ACTION_DATA,mTrackId)

		}

		fPlFrag.arguments = fBndl
//			(activity as MainActivity).openFrame(fPlFrag)
		(activity as MainActivity).mNavController.navigate(R.id.add_trackTo_plList,fBndl)
	}


	override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
		super.onViewCreated(view, savedInstanceState)
	}

	fun setLikedState(fStore: yMediaStore){
		val fisLiked = fStore.isTrackLiked(mTrackId)
		if (fisLiked != isLiked){
			if (fisLiked)
				mvLike.drawable.setTint(Color.parseColor(COLOR_PINK))
			else
				mvLike.drawable.setTint(Color.parseColor("#FFFFFF"))
		}
		isLiked = fisLiked
	}

	override fun setTrack(fTrack: iTrack, fTrackList: iTrackList?) {

		super.setTrack(fTrack, fTrackList)
		mTrack = fTrack
		val fStore = yMediaStore.store(requireContext())
		lifecycleScope.launch(Dispatchers.Default){
			val fCashed = fStore.getTrack(fTrack.mId)
			if (fCashed != fTrack) {
				withContext(Dispatchers.Main){
					fTrack.mPlaylists = fCashed.mPlaylists
					setPlaylists(fTrack,fStore)
				}

			}
		}



		setLikedState(fStore)


		setPlaylists(fTrack,fStore)
		mvAlbum.text = ""
		fTrack.mAlbums.forEach {
			mvAlbum.text = if (mvAlbum.text == "") it else "${mvAlbum.text}, $it"   }

		val fFirstAnimAlbum = mvAlbum.animate()
			.alpha(sPrevieAlpha)
			.setDuration(1200)
			.withEndAction { mvAlbum.animate()
				.alpha(0.0F)
				.setDuration(500)
			}


		if (fTrackList != null) {
			mvMainTitle.text = fTrackList.getTitle()
		}
	}

	private fun setPlaylists(fTrack: iTrack,fStore: yMediaStore) {
		val fPlLists = ArrayList<iPlaylist>()
		mvToPlaylist.visibility = View.VISIBLE
		mvPlListFlexbox.adapter = CustomAdapter(fPlLists,
			activity as MainActivity
		)
		for(qPl in fTrack.mPlaylists)
			mModel.viewModelScope.launch(Dispatchers.IO){
				val fRes = fStore.getYamPlaylist(qPl)
				withContext(Dispatchers.Main){
					if (fRes != null) {
						fPlLists.add(fRes)
						updPlList(fPlLists)
					}
				}
			}
	}

	fun showPreviewLike(): List<ViewPropertyAnimator> {

		val fFirstAnim = mvLike.animate()
			.alpha(sPrevieAlpha)
			.setDuration(800)
			.withEndAction { mvLike.animate()
				.alpha(0.0F)
				.setDuration(300)
			}
		val fFirstAnimAlbum = mvAlbum.animate()
			.alpha(sPrevieAlpha)
			.setDuration(1200)
			.withEndAction { mvAlbum.animate()
				.alpha(0.0F)
				.setDuration(500)
			}

		return listOf(fFirstAnim,fFirstAnimAlbum)
	}

	private fun updPlList(fPlLists: ArrayList<iPlaylist>) {
		if(fPlLists.size>0){
			mvPlListFlexbox.adapter = CustomAdapter(fPlLists,
				activity as MainActivity
			)
			mvToPlaylist.visibility = View.GONE
		}else{
			mvToPlaylist.visibility = View.VISIBLE
		}
	}

	class CustomAdapter(private val dataSet: ArrayList<iPlaylist>,
						val mMain: MainActivity) :
		RecyclerView.Adapter<CustomAdapter.ViewHolder>() {

		lateinit var mRecyclerView: RecyclerView




		override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
			super.onAttachedToRecyclerView(recyclerView)
			mRecyclerView = recyclerView
		}

		class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
			val vTitle: TextView

			init {
				vTitle = view.findViewById(R.id.it_pllist_flex_title)

			}
		}

		override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
			val view = LayoutInflater.from(viewGroup.context)
				.inflate(R.layout.it_pllist_flex, viewGroup, false)

			view.setOnClickListener {
				mRecyclerView.callOnClick() }
			return ViewHolder(view)
		}

		override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
//			viewHolder.itemView.setOnClickListener {
////				TODO
//				  }
			viewHolder.vTitle.text = dataSet[position].mTitle
//			viewHolder.itemView.setOnClickListener {
//				mRecyclerView.callOnClick() }
		}

		override fun getItemCount() = dataSet.size



	}
}