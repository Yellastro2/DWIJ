package com.yelldev.dwij.android.adapters

import android.annotation.SuppressLint
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.yelldev.dwij.android.MainActivity
import com.yelldev.dwij.android.R
import com.yelldev.dwij.android.entitis.iTrack
import com.yelldev.dwij.android.entitis.iTrackList
import com.yelldev.dwij.android.yMediaStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TrackListAdapter(val mMain: MainActivity,
                                           var mTrackList: iTrackList
) :
    RecyclerView.Adapter<TrackListAdapter.ViewHolder>() {


    var mListOfObj: ArrayList<iTrack> = ArrayList<iTrack>()

    var mInitJob:  Deferred<Unit>? = null

    fun setList(allTracks: iTrackList) {
        mTrackList = allTracks
        mInitJob= CoroutineScope(Dispatchers.Default).async {
            mListOfObj = mTrackList.getTracks(yMediaStore.store(mMain))
            mInitJob = null
            withContext(Dispatchers.Main){notifyDataSetChanged()}

        }

    }

    fun addToList(fTracks: Collection<iTrack>) {

        mTrackList.addTracks(fTracks as ArrayList<iTrack>)
        notifyItemRangeInserted(
            mTrackList.getList().size - fTracks.size,
            mTrackList.getList().size - 1)

    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val vTitle: TextView
        val vAutor: TextView
        val vImg: ImageView
        var mId: Int = -1

        init {
            vTitle = view.findViewById(R.id.it_track_title)
            vAutor = view.findViewById(R.id.it_track_autor)
            vImg = view.findViewById(R.id.it_track_img)
        }
    }

    override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.it_track, viewGroup, false)


        return ViewHolder(view)
    }

    @SuppressLint("SuspiciousIndentation")
    override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
        viewHolder.mId = position
        viewHolder.itemView.setOnClickListener {
            CoroutineScope(Dispatchers.Default).async {
                mMain.setTrack(position, mTrackList)
            }}


        viewHolder.vTitle.text = mListOfObj[position].mTitle
        viewHolder.vAutor.text = mListOfObj[position].mArtist

        CoroutineScope(Dispatchers.Default)
            .async(Dispatchers.Default) {
                // если так не делать, при быстром скроле списка на одном вьюхолдере
                // будут висеть сразу несколько асинхов - загрузятся поочередно несколько картинок
                if (viewHolder.mId != position)
                    return@async
                val fSingle = mListOfObj[position].set_Cover_toView(
                    yMediaStore.store(viewHolder.vAutor.context),400)
                if (viewHolder.mId != position)
                    return@async
                CoroutineScope(Dispatchers.Main).async {
                    if (viewHolder.mId != position)
                        return@async
                    if (fSingle!=null)
                        viewHolder.vImg.setImageBitmap(fSingle)
                    else
                        Log.w("DWIJ_DEBUG",fSingle)
                }
            }




        val f_name_patrn = "back1_1"
        val i = 0//Random.nextInt(300)
        val name = f_name_patrn + (i.toString().padStart(3, '0'));
        val globeId = viewHolder.itemView.resources.getIdentifier(name, "drawable",
            viewHolder.itemView.context.getPackageName());
        viewHolder.vImg.setImageResource(globeId)
        GlobalScope.launch(Dispatchers.IO){

        }
//			val fSingle = dataSet[position].set_Cover_toView(
//				yMediaStore.store(viewHolder.vAutor.context),400)
//			if(fSingle != null)
//				fSingle.subscribe({viewHolder.vImg.setImageBitmap(it)},{Log.w("DWIJ_DEBUG",it)})

//				Thread {
//					val bitmap = dataSet[position].set_Cover_toView(yMediaStore.store(viewHolder.vAutor.context))
//					if (bitmap != null){
//						viewHolder.itemView.post { viewHolder.vImg.setImageBitmap(bitmap) }
//					}
//				}.start()
    }

    override fun getItemCount() = mListOfObj.size



}