package com.yelldev.dwij.android

import android.Manifest
import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.annotation.SuppressLint
import android.content.ComponentName
import android.content.Intent
import android.content.ServiceConnection
import android.database.Cursor
import android.graphics.drawable.AnimationDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import androidx.navigation.fragment.NavHostFragment
import com.google.android.material.snackbar.Snackbar
import com.yelldev.dwij.android.entitis.YaM.yWave
import com.yelldev.dwij.android.entitis.iTrack
import com.yelldev.dwij.android.entitis.iTrackList
import com.yelldev.dwij.android.fragments.BigPlayerFrag
import com.yelldev.dwij.android.fragments.HomeFrag
import com.yelldev.dwij.android.fragments.LilPlayerFrag
import com.yelldev.dwij.android.fragments.ObjectFrag
import com.yelldev.dwij.android.fragments.PlListFrag
import com.yelldev.dwij.android.fragments.PlayerAbs
import com.yelldev.dwij.android.fragments.TrackListFrag
import com.yelldev.dwij.android.player_engine.PlayerService
import com.yelldev.dwij.android.utils.CashManager
import com.yelldev.dwij.android.utils.PermissionManager
import com.yelldev.dwij.android.utils.yLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.logging.Logger


class MainActivity : AppCompatActivity() {

    companion object {
        val FIRST_TRACKLIST = "tracklist"
        val FIRST_HOME = "home"
        val FIRST_PLAYER = "player"
        val FIRST_PLLIST = "playlist"
        val FIRST_TYPES = listOf<String>(FIRST_PLAYER, FIRST_PLLIST, FIRST_TRACKLIST)

        lateinit var LOG: Logger
        val RECORD_REQUEST_CODE = 31437

    }

//    https://music.yandex.ru/album/19297693/track/90842069

    var mList: iTrackList? = null
    var mSDList = ArrayList<iTrack>()

    var mPlayer: PlayerService? = null
    var serviceBound = false
    var is_first_tap = true

    val mPermsManager = PermissionManager(this)
    lateinit var mNavController: NavController

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.lay_main)
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.fragmentContainerView) as NavHostFragment
        mNavController = navHostFragment.navController
        mNavController.addOnDestinationChangedListener(){controller, destination, arguments ->
            if(destination.id == R.id.bigPlayerFrag){
                closeLilPlayer()

            }else{
                if(mPlayer?.mTrackList!= null)
                    showLilPlayer()
            }
        }
        LOG = yLog.log(
            KeyStore.TAG)

        mPermsManager.setupPermissions(Manifest.permission.READ_EXTERNAL_STORAGE){scanMedia()}
//        getPlaylists()
        initPlayer()
        intent?.let {
            print(it)
            if (it.action == Intent.ACTION_VIEW){
                val fUrl = it.data
                val fPaths = fUrl!!.pathSegments
                val sdf = fPaths
                if (fPaths[0] == "album"){
                    val fAlbum = fPaths[1]
                    if (fPaths[2] == "track"){
                        val fTrack = fPaths[3]
                        openTrackInfo(fTrack)
                        return
                    }
                }
            }
        }
        openTopAct()
    }

    @SuppressLint("Range")
    private fun getPlaylists() {
        var cursor: Cursor? = null
        val projection1 = arrayOf(
            MediaStore.Audio.Playlists._ID,
            MediaStore.Audio.Playlists.NAME
        )
        cursor = managedQuery(
            MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI,
            projection1,
            null,
            null,
            null
        )
        startManagingCursor(cursor)
        val fResult = ArrayList<String>()
        if (cursor!=null)
        while(cursor!!.moveToNext()){
            val id = cursor.getLong(cursor.getColumnIndex(MediaStore.Audio.Playlists._ID))
            val name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Playlists.NAME))
            val fl = "${name}_$id"
            fResult.add(fl)
            // Теперь у вас есть ID и имя плейлиста, вы можете использовать их в соответствии с вашими потребностями
            // Например, вы можете получить список треков, принадлежащих каждому плейлисту
        }

//        cManager(cursor, 2, 1)
        cursor.close();
    }

//*********************************************
//    FRAG NAVIGATIONS

    fun openTrackInfo(fTrackId: String) {
        val fBndl = Bundle()
        fBndl.putString(KeyStore.TYPE,ObjectFrag.TRACK)
        fBndl.putString(KeyStore.VALUE,fTrackId)
        mNavController.navigate(R.id.objectFrag,fBndl)
    }

    fun openFrame(fFrag: Fragment,isBackstack: Boolean = true){
        val fTrans = supportFragmentManager.beginTransaction()
        if (is_first_tap) {
            if(fFrag is PlListFrag) {
                setFirstType(FIRST_PLLIST)

            }
            else if(fFrag is TrackListFrag)
                setFirstType(FIRST_TRACKLIST)
            else if(fFrag is HomeFrag)
                setFirstType(FIRST_HOME)
        }
        if (isBackstack)
            fTrans.addToBackStack("")
        if (!isBackstack)
            fTrans.disallowAddToBackStack()
        fTrans.replace(R.id.fragmentContainerView,fFrag,"")


        fTrans.commit()
        if(fFrag !is BigPlayerFrag)
            showLilPlayer()
        val brand = Build.BRAND
        val build = Build()
        val some = Build.MODEL
        val sdfs = 5

    }

    val LILFRAG_TAG = "lilfrag_tag"
    fun closePlayer(){
        openTrackList(false)
//        showLilPlayer()
    }
    fun showLilPlayer(){
        val f_lil_player = supportFragmentManager.fragments.find { it is LilPlayerFrag }
        if (f_lil_player == null){
            mPlayer?.let {
                val f_lil_player = LilPlayerFrag()
                supportFragmentManager.beginTransaction()
                    .add(R.id.main_frag_bott, f_lil_player, LILFRAG_TAG)
                    .commit()
            }
        }
    }
    val someBundle = bundleOf("some_int" to 0)
    fun openPlayer(isBackstack: Boolean = true){
//        openFrame(BigPlayerFrag(mPlayer),isBackstack)
        mNavController.navigate(R.id.bigPlayerFrag,
            null,
//        mNavController.navigate("com.yelldev.dwij.android.fragments.BigPlayerFrag",
            NavOptions.Builder()
                .setPopUpTo(R.id.bigPlayerFrag, true)
                .setEnterAnim(R.anim.slide_up)
//                .setExitAnim(R.anim.slide_down)
//                .setPopEnterAnim(R.anim.slide_up) //background frame
                .setPopExitAnim(R.anim.slide_down)
                .build())
//        mNavController.na
        closeLilPlayer()
    }

    fun closeLilPlayer(){
        val f_lil_player = supportFragmentManager.fragments.find { it is LilPlayerFrag }
        f_lil_player?.let {
            supportFragmentManager.beginTransaction()
                .remove(f_lil_player)
                .commit()
        }
    }

    fun openTrackList(isBackstack: Boolean = true) {
        openFrame(TrackListFrag(),isBackstack)

    }

    fun openTopAct(){
        val sharedPref = getSharedPreferences(KeyStore.s_preff, MODE_PRIVATE)
        val FIRST_TYPE_RATE = "first_type_rate"
        val fKey = sharedPref.getString(FIRST_TYPE_RATE, "")!!
        val fTop = ArrayList(fKey.split("%"))
        val fMax = mutableMapOf(FIRST_HOME to 0, FIRST_TRACKLIST to 0, FIRST_PLLIST to 0)
        for (qTop in FIRST_TYPES){
            fMax[qTop] =
                fTop.count {it == qTop}
        }
        var fMaxVal = 0
        var fResult = FIRST_HOME
        for ((key, value) in fMax){
            if(fMaxVal< value) {
                fMaxVal = value
                fResult = key
            }
        }
//        if(fResult == FIRST_HOME) openFrame(HomeFrag())
//        if (fResult == FIRST_TRACKLIST) mNavController.navigate(R.id.action_homeFrag_to_trackListFrag)
//        else if (fResult == FIRST_PLLIST) mNavController.navigate(R.id.action_homeFrag_to_plListFrag)
        is_first_tap = true
    }

    fun setFirstType(key: String){
        is_first_tap = false
        val sharedPref = getSharedPreferences(KeyStore.s_preff, MODE_PRIVATE)
        val FIRST_TYPE_RATE = "first_type_rate"
        val fKey = sharedPref.getString(FIRST_TYPE_RATE, "")!!
        val fTop = ArrayList(fKey.split("%"))
        fTop.add(key)
        if (fTop.size>5) fTop.removeAt(0)
        var fRes = ""
        for (qas in fTop) fRes += qas + "%"
        fRes = fRes.removeSuffix("%").removePrefix("%")
        sharedPref.edit().putString(FIRST_TYPE_RATE,fRes)
            .apply()
    }

//*********************************************
//    PLAYER MANIPULATIONS

    fun playWave(fWave: iTrackList){
        if(mPlayer == null){
            initPlayer()
        }
        mNavController.navigate(R.id.bigPlayerFrag)
        mPlayer?.setWaveList(fWave as yWave)
    }
    fun showPlaylist(fPlayList: String, f_track: Int = 0) {
        if(mPlayer == null){
            initPlayer()
        }
        val fBndl = Bundle()
        fBndl.putString(KeyStore.TYPE,ObjectFrag.PLAYLIST)
        fBndl.putString(KeyStore.VALUE,fPlayList)
        mNavController.navigate(R.id.action_plListFrag_to_objectFrag,fBndl)
//        mNavController.navigate(R.id.action_plListFrag_to_bigPlayerFrag)
//        mPlayer?.setList(fPlayList)
    }

    fun openTrackList(){

    }


    fun getPlayerFrag(): PlayerAbs? {

        if( supportFragmentManager.fragments[0] is PlayerAbs)
            return (supportFragmentManager.fragments[0] as PlayerAbs)
        val fLilPlayer = supportFragmentManager.fragments.find { it is LilPlayerFrag } as PlayerAbs?
        if(fLilPlayer == null)
            showLilPlayer()
        return null
    }


    suspend fun setTrack(fPosition: Int, fList: iTrackList){
        if (fList != mPlayer?.mTrackList){
            mPlayer?.mTrackList = fList
            mPlayer?.startPlayList(fList.getTracks(yMediaStore.store(this)),fPosition)
        }else
            mPlayer?.setTrack(fPosition)
        withContext(Dispatchers.Main){
            openPlayer()
        }



    }

    fun setTrack_toFrag(fTrack: iTrack, fTrackList: iTrackList?){
        getPlayerFrag()?.setTrack(fTrack, fTrackList)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
    }

//*********************************************
//   INITERS VSYAKIE
    fun init_wallpaper(){
        val animation = AnimationDrawable()
        animation.isOneShot = false
        val f_name_patrn = "background/back1_1"
        for (i in 0..300) {
            val q_name = f_name_patrn + i.toString().padStart(3, '0')+".jpg";
            val ims = assets.open(q_name)
            // загружаем как Drawable
            // загружаем как Drawable
            val d = Drawable.createFromStream(ims, null)!!
            animation.addFrame(d,1000/25)
        }
        findViewById<RelativeLayout>(R.id.main_lay).setBackgroundDrawable(animation)
        animation.start()
    }


    val serviceConnection: ServiceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName, service: IBinder) {
//            лишнее
//            val binder: PlayerService.LocalBinder = service as PlayerService.LocalBinder
//            mPlayer это поле в MainActivity в которое сохраняем
//            прям сам обьект сервиса как есть (так вроде нехорошо делать)
            mPlayer = (service as PlayerService.LocalBinder).service
//            в самом сервисе в это поле сохраняем ссылку на эту Активити
//            (так тоже делать нехорошо)
            mPlayer!!.mActivity = this@MainActivity
//            если в менеджере фрагментов на первом плане найдется фраг, который является плеером
//            (с кнопочками плей\стоп, инфой о треке), то ему так же записываем в поле
//            этот сервис
            if (supportFragmentManager.fragments[0] is PlayerAbs)
                (supportFragmentManager.fragments[0] as PlayerAbs).mPlayer = mPlayer
//            TODO ???
//            если в сервисе уже загружен какой то трек(на готове или играет)
//            то тыкаем функцию, которая загрузит инфу трека на фрагмент, который
//            под это заточен(экран плеера или маленький плеер внизу)
            if(mPlayer!!.mTrackList!=null &&mPlayer!!.mTrackList!!.size()>0) {
                setTrack_toFrag(mPlayer!!.getCurent(),mPlayer!!.mTrackList)
            }
            serviceBound = true
        }

        override fun onServiceDisconnected(name: ComponentName) {
            serviceBound = false
            try {
                val snack = Snackbar.make(
                    this@MainActivity.findViewById(android.R.id.content),
                    "Media Player not initialized", Snackbar.LENGTH_LONG
                )
                snack.show()
            }catch (e: Exception) {
                LOG.warning(e.stackTraceToString())
            }
        }
    }

    fun initPlayer(){
        if (!serviceBound) {
            val playerIntent = Intent(this, PlayerService::class.java)
            startForegroundService(playerIntent)
            bindService(playerIntent, serviceConnection, BIND_AUTO_CREATE)
        } else {
            LOG.info("player init already")
        }
    }

    fun scanMedia(){
        Log.i("scan","scan")
//        val snack = Snackbar.make(this.findViewById(R.id.main_lay),
//            "Scan media storage",Snackbar.LENGTH_INDEFINITE)
//        snack.show()

        CashManager.ScanMedia(this@MainActivity){
            mSDList.addAll(it)
            runOnUiThread {
//                snack.dismiss()
                onScanFinish(it.size)
            }
        }
    }

    class SomeTrackList(val mList: ArrayList<iTrack>,val mType: String): iTrackList{
        override fun getList(): ArrayList<String> {
            return mList.map { it.mId } as ArrayList<String>
        }

        override suspend fun getTracks(fStore: yMediaStore): ArrayList<iTrack> {
            return mList
        }

        override fun addTracks(fTracks: ArrayList<iTrack>) {
            mList.addAll(fTracks)
        }

        override fun getTitle(): String {
            return mType
        }

        override fun getType(): String {
            return mType
        }

        override fun getId(): String {
            TODO("Not yet implemented")
        }

        override fun isRepeat(): Boolean {
            TODO("Not yet implemented")
        }

    }


    fun onScanFinish(f_new: Int = 0){
        if (mSDList.size>0){
            if (mList == null){
                mList = SomeTrackList(mSDList, KeyStore.STORAGE_TRACKLIST)
                initPlayer()
            }
        }
        val fCur2 = mNavController.currentDestination
        val fCurFragment = supportFragmentManager.fragments[0]
        if(fCur2?.label == "TrackListFrag"){
            val navHost = supportFragmentManager.findFragmentById(R.id.fragmentContainerView)
            navHost?.let { navFragment ->
                navFragment.childFragmentManager.primaryNavigationFragment?.let {fragment->
                    val fTrackListFrag = fragment as TrackListFrag
                    if (fTrackListFrag.mModel.mType == KeyStore.STORAGE_TRACKLIST)
                        fTrackListFrag.mModel.getAdapter(this).setList(
                            SomeTrackList(mSDList, KeyStore.STORAGE_TRACKLIST))
                    else if (fTrackListFrag.mModel.mType == TrackListFrag.LIST_OF_ALL)
                        fTrackListFrag.mModel.getAdapter(this).addToList(
                            mSDList)
                }
            }
        }
//            val fTrackListFrag = (supportFragmentManager.fragments[0] as TrackListFrag)
//            if (fTrackListFrag.mModel.mType == KeyStore.STORAGE_TRACKLIST)
//                fTrackListFrag.mModel.getAdapter(this).setList(
//                    SomeTrackList(mSDList, KeyStore.STORAGE_TRACKLIST))
//            else if (fTrackListFrag.mModel.mType == TrackListFrag.LIST_OF_ALL)
//            fTrackListFrag.mModel.getAdapter(this).addToList(
//                mSDList)
//            if (fTrackListFrag.mList == mSDList)
//                fTrackListFrag.mvRecycl?.adapter?.notifyDataSetChanged()
//        }
        is_sd_scanned = true
    }

//*********************************************
//  NIZKOUROVNEVOE VSYAKOE

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        mPermsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
    var is_sd_scanned = false
    override fun onResume() {
        super.onResume()
        val fmang = supportFragmentManager.fragments
//      TODO on yamusic login, on grand permison
        Log.i("DWIJ_TAG","MainActivity onResume")
        if(mPlayer != null) try{
            mPlayer!!.mMediaPlayer.isPlaying
        }catch (e: Exception){
            LOG.warning(e.stackTraceToString())
            mPlayer!!.initMediaPlayer()
        }
//        if (mPlayer==null){
//            initPlayer()
//        }

    }

    override fun onRestart() {
        super.onRestart()
        if (!is_sd_scanned){
            mPermsManager.setupPermissions(READ_EXTERNAL_STORAGE){scanMedia()}
        }
//        if (m_Ya_Cli == null){
//            init_YaM()
//        }
    }
    override fun onDestroy() {
        super.onDestroy()
        if (serviceBound) {
            unbindService(serviceConnection)
            //service is active
            mPlayer!!.mActivity = null
        }
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        savedInstanceState.putBoolean("ServiceState", serviceBound)
        super.onSaveInstanceState(savedInstanceState)
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        serviceBound = savedInstanceState.getBoolean("ServiceState")
    }

    fun noYandexLoginError() {
        Snackbar.make(
            this@MainActivity.findViewById(android.R.id.content),
            "Error load track: No Yandex Music login", Snackbar.LENGTH_LONG
        ).show()
    }


//    fun init_YaM(){
//        val sharedPref = getSharedPreferences(KeyStore.s_preff, MODE_PRIVATE)
//
//        val fKey = sharedPref.getString(k_ya_token,"")!!
//
//        if (fKey.equals("")){
//            LOG.info("no YandexMusic login")
//        }else{
//            val fLogin = sharedPref.getString(k_ya_login,"nologin")!!
//            val f_id = sharedPref.getString(k_ya_id,"")!!
//
//            if (f_id.equals("")){
//                Thread {
//                    var f_res = Account.showInformAccount().get()
//                    Log.i("DWIJ_TAG", f_res.toString())
//                    var f_ID = f_res
//                        .getJSONObject("result")
//                        .getJSONObject("account")
//                        .getString("uid")
//                    runOnUiThread {
//                        val sharedPref = getSharedPreferences(KeyStore.s_preff, MODE_PRIVATE)
//                        with (sharedPref.edit()) {
//
//                            putString(k_ya_id, f_ID)
//                            apply()
//                        }
//                        init_YaM()
//                    }
//                }.start()
//            }else {
//                m_Ya_Cli = yClient(fKey, f_id)
//                runOnUiThread { onYaMInit() }
//
//            }
//        }
//    }

//    fun onYaMInit(){
////TODO if fragment == playlist and mode == yandex then refresh it
//    }

//    fun getTrack(fTrackId: String): yTrack? {
//        var fRes: yTrack? = null
//
//        mList.forEach  { if( it.mId == fTrackId ) fRes = it as yTrack}
//        return fRes
//    }
}