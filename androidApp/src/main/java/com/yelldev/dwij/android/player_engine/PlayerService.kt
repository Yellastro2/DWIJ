package com.yelldev.dwij.android.player_engine

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.media.MediaPlayer
import android.media.MediaPlayer.OnBufferingUpdateListener
import android.media.MediaPlayer.OnCompletionListener
import android.media.MediaPlayer.OnPreparedListener
import android.media.MediaPlayer.OnSeekCompleteListener
import android.os.Binder
import android.os.IBinder
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import com.google.android.material.snackbar.Snackbar
import com.yelldev.dwij.android.KeyStore
import com.yelldev.dwij.android.MainActivity
import com.yelldev.dwij.android.entitis.YaM.YaPlaylist
import com.yelldev.dwij.android.entitis.YaM.YaSingleTrackList
import com.yelldev.dwij.android.entitis.YaM.YaTrack
import com.yelldev.dwij.android.entitis.YaM.yWave
import com.yelldev.dwij.android.entitis.iTrack
import com.yelldev.dwij.android.entitis.iTrackList
import com.yelldev.dwij.android.fragments.PlayerAbs
import com.yelldev.dwij.android.service_utils.PlayerNotification
import com.yelldev.dwij.android.utils.NoYandexLoginExceprion
import com.yelldev.dwij.android.utils.yTimer
import com.yelldev.dwij.android.yMediaStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.random.Random


class PlayerService : Service(), OnCompletionListener, OnPreparedListener,
    MediaPlayer.OnErrorListener, OnSeekCompleteListener, MediaPlayer.OnInfoListener,
    OnBufferingUpdateListener, OnAudioFocusChangeListener {
    companion object {
        val LOG = MainActivity.LOG
        val TAG = "PlayerService"

        val RANDOM_MODE = "random_mode"

    }


    var isRepeat: Boolean = false
    var mPlayerFrag: PlayerAbs? = null
    private val iBinder: IBinder = LocalBinder()

    var is_Focused = false


    override fun onBind(intent: Intent?): IBinder {
        return iBinder
    }

    lateinit var ySession: yMediaSession
    var isInit = false
    lateinit var mMediaPlayer: MediaPlayer
    var isMediaNull: Boolean = true
    var isWaitForFocus = false
    private var _mList = ArrayList<iTrack>()
    var mList: ArrayList<iTrack> = ArrayList<iTrack>()
        get() {
            return _mList
        }
    var mTrackList: iTrackList? = null


    var m_CurentTrack = 0
    var is_random = false

    var _mAct: MainActivity? = null
    var mActivity: MainActivity?
        get() = _mAct
        set(value) {
            _mAct = value
        }

    val NOTIFICATION_ID = 235345



    override fun onCreate() {
        super.onCreate()

        val f_p_intent: PendingIntent =
            Intent(this@PlayerService, PlayerService::class.java).let { notificationIntent ->

                PendingIntent.getActivity(this@PlayerService, 0, notificationIntent,
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
            }

        ySession = yMediaSession(this)

        initMediaPlayer()

        registerBecomingNoisyReceiver();
        val sharedPref = getSharedPreferences(KeyStore.s_preff, MODE_PRIVATE)
        is_random =
            sharedPref.getBoolean(RANDOM_MODE, false)

    }

    override fun onRebind(intent: Intent?) {
        super.onRebind(intent)
        LOG.info("on REBIND")
    }

    fun initMediaPlayer() {
        LOG.info("init mediaPlayer")
        mMediaPlayer = MediaPlayer()
        mMediaPlayer.setOnCompletionListener(this)
        mMediaPlayer.setOnErrorListener(this)
        mMediaPlayer.setOnPreparedListener(this)
        mMediaPlayer.setOnBufferingUpdateListener(this)
        mMediaPlayer.setOnSeekCompleteListener(this)
        mMediaPlayer.setOnInfoListener(this)
        mPlayerState = COMPLETE
        isMediaNull = false

    }

    override fun onStartCommand(intent: Intent, flags: Int, startId: Int): Int {
        if(isInit){
            try {
                ySession.handleIntent(intent)
            }catch (e: Exception){
                LOG.warning(e.stackTraceToString())
            }
            return START_NOT_STICKY
        }
        LOG.info("start command")

        val f_notify = PlayerNotification.getNotify(this@PlayerService,ySession.token,
        "","")

        startForeground(NOTIFICATION_ID, f_notify)

        isInit = true
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        LOG.info("PlayerService ONDESTROY")
        if (!isMediaNull) {
            mMediaPlayer.release()
        }
        removeAudioFocus()
        if (phoneStateListener != null) {
            telephonyManager?.listen(phoneStateListener, PhoneStateListener.LISTEN_NONE);
        }
        removeNotification();
        unregisterReceiver(becomingNoisyReceiver);
    }

    private fun removeNotification() {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }

    @SuppressLint("CheckResult")
    fun setWaveList(fWave: yWave){
        is_random = false
//        TODO сделать универс. для iTrackList, реализацией должен заниматься yMediaStore
        Thread{
            val fStore = yMediaStore.store(applicationContext)
            mTrackList = fWave
            CoroutineScope(Dispatchers.IO).launch {
                val fRes = fStore.getTrackList(fWave.getList())
                withContext(Dispatchers.Main){
                    startPlayList(fRes)
                    mPlayerFrag?.finishWaveDialog()
                }
            }
//            fStore.getTrackList(fWave.getList()).subscribe{
//                    fList -> startPlayList(fList)
//                mPlayerFrag?.finishWaveDialog()
//            }
        }.start()
    }

    fun updTrackList(fTrack: iTrack){
        _mList.add(fTrack)
    }

    fun startPlayList(fList: ArrayList<iTrack>,fPos: Int = -1) {
        mLastTracks.clear()
        _mList = fList
        if(_mList.size>0){
            var fPos1 = 0
            if (fPos == -1)
                fPos1 = if (is_random) Random.nextInt(fList.size) else 0
            else
                fPos1 = fPos
            m_CurentTrack = fPos1
            setTrack(fList[m_CurentTrack])
        }
    }

    @SuppressLint("CheckResult")
    fun setList(fId: String){
//        TODO сделать универс. для iTrackList, реализацией должен заниматься yMediaStore
//        Thread{
            val fStore = yMediaStore.store(applicationContext)
        GlobalScope.async(Dispatchers.Default){
            val fList = fStore.getYamPlaylist(fId)
            withContext(Dispatchers.Main){
                if (fList != null){
                    mTrackList = fList
                    if (fStore.mPlaylistUpdateObservers.containsKey(fList.mId))
                        fStore.mPlaylistUpdateObservers[fList.mId]?.add(object: yMediaStore.yObserver {
                            override fun onUpdate(fProg: Int,fMax: Int){
                                mPlayerFrag?.load(fProg,fMax)
                            }
                            override fun onCompteate(){
                                CoroutineScope(Dispatchers.IO).launch {
                                    val fRes = fStore.getTrackList(fList!!.getList() as ArrayList<String>)
                                    startPlayList(fRes)
                                    mPlayerFrag?.loadCompleate()
                                }
//                                fStore.getTrackList(fList!!.getList() as ArrayList<String>).subscribe{
//                                        fListSinge -> startPlayList(fListSinge)
//                                    mPlayerFrag?.loadCompleate()
//                                }
                            }
                        })
                    else
                        CoroutineScope(Dispatchers.IO).launch {
                            val fRes = fStore.getTrackList(fList!!.getList() as ArrayList<String>)
                            startPlayList(fRes)
                        }
//                        fStore.getTrackList(fList!!.getList() as ArrayList<String>).subscribe{
//                                fListSinge -> startPlayList(fListSinge)}
                }
            }
        }
//            fStore.getYamPlaylist(fId).subscribe {fList ->
//                mTrackList = fList
//                if (fStore.mPlaylistUpdateObservers.containsKey(fList.mId))
//                    fStore.mPlaylistUpdateObservers[fList.mId]?.add(object: yMediaStore.yObserver {
//                        override fun onUpdate(fProg: Int,fMax: Int){
//                            mPlayerFrag?.load(fProg,fMax)
//                        }
//                        override fun onCompteate(){
//                            fStore.getTrackList(fList!!.getList() as ArrayList<String>).subscribe{
//                                    fListSinge -> startPlayList(fListSinge)
//                                mPlayerFrag?.loadCompleate()
//                            }
//                        }
//                    })
//                else
//                    fStore.getTrackList(fList!!.getList() as ArrayList<String>).subscribe{
//                            fListSinge -> startPlayList(fListSinge)}
//            }
    }

    fun setTrack(fPos: Int){
        if(fPos< _mList.size){
            setTrack(_mList[fPos])
            m_CurentTrack = fPos
        }
    }

    fun setTrack(fTrack: iTrack){
        yTimer.timing(TAG,"setTrack()")
        LOG.info("set track ${fTrack.mId}")
//         нужен что бы вьюха плеера отвязалась от текущего состояния трека,
//         потому что там двухсторонняя связь между ползунком и прогрессом плеера,
//         если не отвязать, всё взорвется нахуй
        onCustCompletionListener()
        if(fTrack is YaTrack){
            if(!fTrack.isAvaibale){
                mActivity?.let {
                    val snack = Snackbar.make(
                        it.findViewById(android.R.id.content),
                        "${fTrack.mTitle} not avaible cuz of war", Snackbar.LENGTH_LONG
                    )
                    snack.show()
                }

                nextTrack()
                return
            }
        }
        try {
            Log.i("DWIJ_DEBUG","setTrack reset() Player")
            mMediaPlayer.reset()
            mPlayerState = COMPLETE
        }catch(e: Exception) {
            LOG.warning(e.stackTraceToString())
            initMediaPlayer()
        }
//      то есть обьект трека сам себя запускает в плеере. потому что яндекс трек и
//      трек с устройства будут делать это по разному (из сети или из файла)
        try {
            fTrack.setToPlayer(mMediaPlayer,applicationContext
            ) {//state 2
                yTimer.timing(TAG,"setTrack(): setToPlayer()")
                mMediaPlayer.prepareAsync()
                Log.i("DWIJ_DEBUG","setTrack prepareAsync() Player")

                LOG.info( "setTrack prepare player")
                mActivity?.runOnUiThread {
                    mPlayerFrag?.setTrack(fTrack,mTrackList) }

                yTimer.timing(TAG,"setTrack(): setToPlayer() end")
                return@setToPlayer 0
            }
        }catch (e: NoYandexLoginExceprion){

            mActivity?.noYandexLoginError()
            nextTrack()
        }

        ySession.setTrack(fTrack)

        val f_notify = PlayerNotification.getNotify(this@PlayerService,ySession.token,
            fTrack.mTitle,fTrack.mArtist)

        startForeground(NOTIFICATION_ID, f_notify)
        yTimer.timing(TAG,"setTrack() end")
    }
//TODO короч когда первый раз выбираешь плейлист, толи фокус не запрашивается,
//    толи еще какая хуйня, но короч авто плей не происходит из за фокуса, потому что когда нажимаешь
//    второй раз плейлист, авто плей срабатывает норм
    fun playAudio(){
        LOG.info("play audio")
        val fTrack = _mList[m_CurentTrack]
        if (isMediaNull){
            initMediaPlayer()
            if (requestAudioFocus() == false) {
                Log.i("DWIJ_DEBUG","playAudio() audiofocus FALSE")
                stopSelf()
            }
        }
        if( mMediaPlayer.isPlaying ){

            val f_notify = PlayerNotification.getNotify(this@PlayerService,ySession.token,
                fTrack.mTitle,fTrack.mArtist,true)
            startForeground(NOTIFICATION_ID, f_notify)

            mMediaPlayer.pause()
            mPlayerFrag?.setPause()
            isWaitForFocus = false
            ySession.pause()
        }else{
            if (requestAudioFocus() == false) {
                LOG.info("on startCom when focus false")
                stopSelf()
                return
            }
            val f_notify = PlayerNotification.getNotify(this@PlayerService,ySession.token,
                fTrack.mTitle,fTrack.mArtist,false)
            startForeground(NOTIFICATION_ID, f_notify)
            ySession.play()
            mMediaPlayer.start()
            mPlayerFrag?.setPlay()
            isWaitForFocus = true

        }
    }

    fun nextTrack(){
        yTimer.timing(TAG,"nextTrack()")
        if (is_random && mTrackList?.getType()!=yWave.type){
            var f_new = 0
            mLastTracks.add(0,_mList[m_CurentTrack])
            if (mLastTracks.size > _mList.size/2){
                if(isRepeat){
                    mLastTracks.removeLast()
                    yTimer.timing(TAG,"nextTrack(): !=yWave && isRepeat")
                }else
                {
                    val fFreshList = (_mList.clone() as ArrayList<iTrack>)
                    fFreshList.removeAll(mLastTracks)

                    if(fFreshList.size<1){
                        if(isRepeatList()) {
                            mLastTracks.removeAll(mLastTracks.subList(_mList.size / 2,_mList.size-1 ))
                            nextTrack()
                            return
                        }else{
                            playWave()
                            return
                        }
                    }
                    f_new = Random.nextInt(fFreshList.size)
                    val fNewTrack = fFreshList[f_new]
                    f_new = _mList.indexOf(fNewTrack)
                    mLastTracks.add(fNewTrack)
                    m_CurentTrack = f_new
                    yTimer.timing(TAG,"nextTrack(): !=yWave && !=isRepeat")
                    setTrack(_mList[m_CurentTrack])
                    return
                }

//                mLastTracks.removeLast()
            }
            do{
                f_new = Random.nextInt(_mList.size)

            }while ( _mList[f_new] in mLastTracks)
            yTimer.timing(TAG,"nextTrack(): !=yWave do random while ")
            m_CurentTrack = f_new
        }
        else{
            m_CurentTrack ++
            if (m_CurentTrack > _mList.size -2 && mTrackList?.getType() == yWave.type)
                updWave()
            if (m_CurentTrack >= _mList.size)
                if(isRepeat)
                    m_CurentTrack = 0
                else{
                    playWave()
                    return
                }

        }
        yTimer.timing(TAG,"nextTrack(): end")
        setTrack(_mList[m_CurentTrack])
    }

    private fun playWave() {
        if(mTrackList?.getType()==YaPlaylist.TYPE) {
            mPlayerFrag?.setProgress()
            GlobalScope.launch(Dispatchers.IO){
                val fStore = yMediaStore.store(applicationContext)

                setWaveList(fStore.getWave(mTrackList as YaPlaylist) as yWave)
            }
        }else if (mTrackList is YaSingleTrackList){
            mPlayerFrag?.setProgress()
            GlobalScope.launch(Dispatchers.IO){
                val fStore = yMediaStore.store(applicationContext)
                val fTrack = (mTrackList as YaSingleTrackList).mTrack
                setWaveList(fStore
                    .getWave((fTrack as YaTrack) ) as yWave)
            }
        }
    }

    fun updWave(){
        yTimer.timing(TAG,"updWave()")
        if(mTrackList?.getType()==yWave.type)
            GlobalScope.launch(Dispatchers.IO){
                yTimer.timing(TAG,"updWave(): launch")
                val fStore = yMediaStore.store(applicationContext)
                if (mTrackList != null){
                    if (m_CurentTrack>= mTrackList!!.size()) m_CurentTrack = mTrackList!!.size() - 1
                    val fNewTracks = fStore.getWaveNextTrack(mTrackList as yWave,m_CurentTrack)
                    val fRes = fStore.getTrackList(fNewTracks)
//                        .subscribe{fList -> _mList.addAll(fList)}
                    _mList.addAll(fRes)
                }else{
                    Log.e("PlayerService","updWave() ERROR: mTrackList = null")
                    mActivity.let {
                        val snack = Snackbar.make(it!!.findViewById(android.R.id.content),
                            "mTrackList = null",Snackbar.LENGTH_LONG)

                        snack.show()
                    }

                }
                yTimer.timing(TAG,"updWave(): end")

            }
//            Thread{
//                val fStore = yMediaStore.store(applicationContext)
//                val fNewTracks = fStore.getWaveNextTrack(mTrackList as yWave,m_CurentTrack)
//                fStore.getTrackList(fNewTracks).subscribe{fList -> _mList.addAll(fList)}
//            }.start()
    }

    private fun isRepeatList(): Boolean {
        if(mTrackList == null)
            return true
        return mTrackList!!.isRepeat()
    }

    fun forcePrevTrack(){
        if (is_random){
            if(mLastTracks.size>0) {
                m_CurentTrack = _mList.indexOf(mLastTracks[0])
                mLastTracks.removeFirst()
            }else{
                m_CurentTrack = Random.nextInt(_mList.size)
            }
        }else{
            m_CurentTrack--
            if (m_CurentTrack < 0) {
                m_CurentTrack = _mList.size - 1
            }
        }

        setTrack(_mList[m_CurentTrack])
    }
    fun prevTrack() {
        if (mMediaPlayer.currentPosition < 3000 ) {
            forcePrevTrack()
        }else {
            mMediaPlayer.seekTo(0)
        }
    }

    fun pauseAudio(){
        if( mMediaPlayer.isPlaying )
            mMediaPlayer.pause()
        isWaitForFocus = false

    }

    fun resumeAudio(){
        if (!isMediaNull && !mMediaPlayer.isPlaying)
            mMediaPlayer.start()
    }

    val mLastTracks = ArrayList<iTrack>()


    fun setRandomMode(): Boolean {
        is_random = !is_random
        val sharedPref = getSharedPreferences(KeyStore.s_preff, MODE_PRIVATE)
        sharedPref.edit()
            .putBoolean(RANDOM_MODE, is_random)
            .apply()
        if (is_random){

        }
        return is_random
    }
    var mPlayerState = 0
    val PREPARE = 1
    val COMPLETE = 2

    var onCustCompletionListener: ()-> Unit = {}
    override fun onCompletion(mp: MediaPlayer?) {
        Log.i("DWIJ_DEBUG","onCompletion call")
        onCustCompletionListener()
//        val f_dur = mp?.duration
//        val f_prog = mp?.currentPosition
//        if (f_dur == 0 || f_prog == 0) {
//            Log.e("DWIJ_DEBUG","onCompletion duration == 0")
//            return
//        }
        if (mPlayerState == PREPARE){
            mPlayerState = COMPLETE

            nextTrack()
        }

    }

    //Handle errors
    override fun onError(mp: MediaPlayer?, what: Int, extra: Int): Boolean {
        //Invoked when there has been an error during an asynchronous operation
        when (what) {
            MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK -> Log.d(
                "MediaPlayer Error",
                "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK $extra"
            )

            MediaPlayer.MEDIA_ERROR_SERVER_DIED -> Log.d(
                "MediaPlayer Error",
                "MEDIA ERROR SERVER DIED $extra"
            )

            MediaPlayer.MEDIA_ERROR_UNKNOWN -> Log.d(
                "MediaPlayer Error",
                "MEDIA ERROR UNKNOWN $extra"
            )
        }
        return false
    }

    private val becomingNoisyReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            //pause audio on ACTION_AUDIO_BECOMING_NOISY
            if( mMediaPlayer.isPlaying )
                mMediaPlayer.pause()
            //buildNotification(223)
        }
    }

    private fun buildNotification(paused: Any) {
        TODO("Not yet implemented")
    }

    private fun registerBecomingNoisyReceiver() {
        //register after getting audio focus
        val intentFilter = IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY)
        registerReceiver(becomingNoisyReceiver, intentFilter)
    }

    private var _onCustPrepareListener: ()-> Unit = {}
    var onCustPrepareListener: ()-> Unit
        get() {
            return _onCustPrepareListener
        }
        set(value) {
            _onCustPrepareListener = value
            if (mPlayerState == PREPARE)
                value()
        }


    override fun onPrepared(mp: MediaPlayer?) {
        mPlayerState = PREPARE
        LOG.info("on prepared")
        if (is_Focused)
            playAudio()

        onCustPrepareListener()

//        #TODO if wait for play -> play

    }

    lateinit var audioManager: AudioManager

    override fun onBufferingUpdate(mp: MediaPlayer, percent: Int) {
        //Invoked indicating buffering status of
        //a media resource being streamed over the network.
    }

    override fun onInfo(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        //Invoked to communicate some info.
        return false
    }


    override fun onSeekComplete(mp: MediaPlayer) {
//        val f_dur = mp?.duration
//        if (f_dur == 0) Log.e("DWIJ_DEBUG","on seek complete listnr duration == 0")
//        val f_prog = mp?.currentPosition
//        val sdg = 3
        //Invoked indicating the completion of a seek operation.
    }


    override fun onAudioFocusChange(focusChange: Int) {
        when (focusChange) {
            AudioManager.AUDIOFOCUS_GAIN -> {// resume playback
                LOG.info("on listener audiofocus gain")
                if (!isWaitForFocus) return
                if (isMediaNull) initMediaPlayer();
                else if (!mMediaPlayer.isPlaying()) mMediaPlayer.start();
                mMediaPlayer.setVolume(1.0f, 1.0f);
                //break;
                }
            AudioManager.AUDIOFOCUS_LOSS -> {// Lost focus for an unbounded amount of time: stop playback and release media player
//                    if (mMediaPlayer.isPlaying()) mMediaPlayer.stop();
                    LOG.info("on audiofocus loss")
//                    mMediaPlayer.release();
//                    isMediaNull = true
                    if (mMediaPlayer.isPlaying()) mMediaPlayer.pause();
                    //mMediaPlayer = null;
                    //break;
                }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {// Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                LOG.info("on audiofocus transient")
                if (mMediaPlayer.isPlaying()) mMediaPlayer.pause();
                //break;
            }
            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {// Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                LOG.info("on audiofocus duck")
                if (mMediaPlayer.isPlaying()) mMediaPlayer.setVolume(0.1f, 0.1f);
                //break;
            }
        }
    }

    val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
        setAudioAttributes(AudioAttributes.Builder().run {
            setUsage(AudioAttributes.USAGE_MEDIA)
            setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            build()
        })
        setAcceptsDelayedFocusGain(true)
        setOnAudioFocusChangeListener(this@PlayerService)

        build()
    }


    fun requestAudioFocus(): Boolean {
        Log.i("DWIJ_DEBUG","requestAudioFocus()")

        audioManager = getSystemService(AUDIO_SERVICE) as AudioManager

        val focusLock = Any()

        var playbackDelayed = false
        var playbackNowAuthorized = false

        val res = audioManager.requestAudioFocus(focusRequest)
        synchronized(focusLock) {
            playbackNowAuthorized = when (res) {
                AudioManager.AUDIOFOCUS_REQUEST_FAILED -> return false
                AudioManager.AUDIOFOCUS_REQUEST_GRANTED -> {
                    LOG.info("on request audiofocus gain")
                    is_Focused = true
                    return true
                }
                AudioManager.AUDIOFOCUS_REQUEST_DELAYED -> {
                    playbackDelayed = true
                    return false
                }
                else -> return false
            }
        }
        //return false
    }

    private fun removeAudioFocus(): Boolean {
        return AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                audioManager.abandonAudioFocusRequest(focusRequest)
    }

    inner class LocalBinder : Binder() {
        val service: PlayerService
            get() = this@PlayerService
    }

    //Handle incoming phone calls
    private var ongoingCall = false
    private var phoneStateListener: PhoneStateListener? = null
    private var telephonyManager: TelephonyManager? = null

    //Handle incoming phone calls
    private fun callStateListener() {
        // Get the telephony manager
        telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        //Starting listening for PhoneState changes
        phoneStateListener = object : PhoneStateListener() {
            override fun onCallStateChanged(state: Int, incomingNumber: String) {
                when (state) {
                    TelephonyManager.CALL_STATE_OFFHOOK, TelephonyManager.CALL_STATE_RINGING ->
                        if (!isMediaNull) {
                            pauseAudio()
                            ongoingCall = true
                        }

                    TelephonyManager.CALL_STATE_IDLE ->                   // Phone idle. Start playing.
                        if (!isMediaNull) {
                            if (ongoingCall) {
                                ongoingCall = false
                                resumeAudio()
                            }
                        }
                }
            }
        }

        telephonyManager!!.listen(
            phoneStateListener,
            PhoneStateListener.LISTEN_CALL_STATE
        )

    }

    fun getCurent(): iTrack {
        return _mList[m_CurentTrack]
    }


}