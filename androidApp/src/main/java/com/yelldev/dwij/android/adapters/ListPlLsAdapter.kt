package com.yelldev.dwij.android.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.yelldev.dwij.android.KeyStore
import com.yelldev.dwij.android.MainActivity
import com.yelldev.dwij.android.R
import com.yelldev.dwij.android.entitis.PlaylistCreateItem
import com.yelldev.dwij.android.entitis.YaM.YaLikedTracks
import com.yelldev.dwij.android.entitis.YaM.YaPlaylist
import com.yelldev.dwij.android.entitis.YaM.YaTrack
import com.yelldev.dwij.android.entitis.iPlaylist
import com.yelldev.dwij.android.fragments.PlListFrag
import com.yelldev.dwij.android.yMediaStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random

class ListPlLsAdapter(var mTrack: YaTrack?) :
RecyclerView.Adapter<ListPlLsAdapter.ViewHolder>() {

	var mGridSize = 3
	var onClick: (iPlaylist)-> Unit = {}

	var onCreatePlClick: () -> Unit = {}

	var onLongItemClick: (iPlaylist) -> Unit = {}

	var mScope: CoroutineScope? = null

	private var mList: ArrayList<iPlaylist> = ArrayList()
	fun setList(fList: ArrayList<iPlaylist>){
		mList = ArrayList()
		if (mTrack != null){
			fList.removeAll(
				fList.filter { it.getType()==YaLikedTracks.LIKED_ID })
		}
		mList.add(PlaylistCreateItem())
		mList.addAll(fList)
		notifyDataSetChanged()
	}

	fun getList() = mList

	fun init() {
		mList.add(PlaylistCreateItem())
	}

	fun setTrack(fTr: YaTrack){
		mTrack = fTr
		notifyDataSetChanged()
	}

	class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
		val vTitle: TextView
		val vAutor: TextView
		val vImg: ImageView

		init {
			vTitle = view.findViewById(R.id.it_pl_grid_title)
			vAutor = view.findViewById(R.id.it_pl_grid_body)
			vImg = view.findViewById(R.id.it_pl_grid_img)
		}
	}

	override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
		val view = LayoutInflater.from(viewGroup.context)
			.inflate(R.layout.it_playlist_grid, viewGroup, false)
		view.layoutParams = ViewGroup.LayoutParams(mGridSize,mGridSize)

		return ViewHolder(view)
	}

	@SuppressLint("CheckResult")
	override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

		mScope?.launch(Dispatchers.IO) {
			try{
				val fRes = mList[position]
					.getImage(yMediaStore.store(viewHolder.vAutor.context))
				withContext(Dispatchers.Main){
					viewHolder.vImg.setImageBitmap(fRes)
				}
			}catch (e: Exception){
				val fData = mList[position].mTitle
				Log.w("DWIJ_DEBUG",Exception("${fData}\n\n" +
						"${e.stackTraceToString()}"))
			}

		}

		if (mList[position].mId == PlaylistCreateItem.PLAY_CREATE_ITEM_ID){
			viewHolder.vTitle.text = "Создать плейлист"
			viewHolder.vAutor.text = ""
			viewHolder.itemView.setOnClickListener { onCreatePlClick() }
			return
		}

		val s_many = 2
		val s_fev = 1
		val s_one = 0

		val f_size = mList[position].mCount
		val f_dur = mList[position].mDuration
		var f_numb = 0
		if(f_dur>60){

		}else{
			var f_end_type = 0
			var f_end_lettr = ""
			val f_end_num = (f_dur -
					Math.round((f_dur/10).toDouble())).toInt()
			if (f_end_num>4 || f_end_num == 0) f_end_lettr = "ов"
			else if (f_end_num>1) f_end_lettr = "а"

		}

		var f_end_lettr = ""
		val f_end_num = (f_size -
				Math.round((f_size/10).toDouble())).toInt()
		if (f_end_num>4 || f_end_num == 0) f_end_lettr = "ов"
		else if (f_end_num>1) f_end_lettr = "а"
		viewHolder.vTitle.text = mList[position].mTitle
		viewHolder.vAutor.text = "$f_size трек$f_end_lettr - $f_dur секунд"


		val f_name_patrn = "back1_1"
		val i = Random.nextInt(300)
		val name = f_name_patrn + (i.toString().padStart(3, '0'));
		val globeId = viewHolder.itemView.resources.getIdentifier(name, "drawable",
			viewHolder.itemView.context.getPackageName());
		viewHolder.vImg.setImageResource(globeId)



		viewHolder.itemView.setOnLongClickListener {
			onLongItemClick(mList[position])
			return@setOnLongClickListener true
		}

		if(mTrack != null && mTrack!!.mPlaylists.contains(mList[position].mId)){
			viewHolder.vTitle.setBackgroundColor(0xD0080E75.toInt())
			viewHolder.itemView.setOnClickListener {
				val builder: AlertDialog.Builder = AlertDialog.Builder(viewHolder.itemView.context)
				builder
					.setMessage("Удалить трек?!!")
					.setTitle("Удалить трек из плейлиста?")
					.setPositiveButton("Yes,remove") { fD, o ->
						fD.dismiss()
						CoroutineScope(Dispatchers.IO).launch {
							val fRes = (mList[position] as YaPlaylist).removeTrack(
								yMediaStore.store(viewHolder.itemView.context),
								mTrack!!
							)
							withContext(Dispatchers.Main){
								if (fRes){
									notifyItemChanged(position)
								}else{
									Snackbar.make(viewHolder.itemView,
										KeyStore.s_network_error,Snackbar.LENGTH_LONG)
										.show()
								}
							}


						}


					}
					.setNegativeButton("nenada") { fD, o -> fD.dismiss() }

				val dialog: AlertDialog = builder.create()
				dialog.show()
				Snackbar.make(viewHolder.itemView.rootView.findViewById(android.R.id.content),
					"Track already in", Snackbar.LENGTH_SHORT).show()}
		}else {
			viewHolder.vTitle.setBackgroundColor(0x7AD5A54F.toInt())
			viewHolder.itemView.setOnClickListener {onClick(mList[position])
			}
		}

	}

	override fun getItemCount() = mList.size
	fun removeItem(fPlist: YaPlaylist) {
		val fPos = mList.indexOf(fPlist)
		mList.remove(fPlist)
		notifyItemRemoved(fPos)
	}

}