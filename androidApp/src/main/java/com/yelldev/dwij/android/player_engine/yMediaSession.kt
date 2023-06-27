package com.yelldev.dwij.android.player_engine

import android.content.Intent
import android.media.MediaMetadata
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.media.session.MediaButtonReceiver
import com.yelldev.dwij.android.entitis.iTrack

class yMediaSession(val mService: PlayerService,) {

	lateinit var mSession: MediaSessionCompat

	val token: MediaSessionCompat.Token
		get() = mSession.sessionToken

	val stateBuilder = PlaybackStateCompat.Builder()
		.setActions(
			PlaybackStateCompat.ACTION_PLAY
					or PlaybackStateCompat.ACTION_STOP
					or PlaybackStateCompat.ACTION_PAUSE
					or PlaybackStateCompat.ACTION_PLAY_PAUSE
					or PlaybackStateCompat.ACTION_SKIP_TO_NEXT
					or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
		)

	init {
		mSession = MediaSessionCompat(mService, "MusicService").apply {
			setCallback(MediaSessionCallback(mService))
//            setMediaButtonBroadcastReceiver()
//            setSessionActivity(f_p_intent)
//            setMediaButtonReceiver(f_p_intent)
			//setCaptioningEnabled()

			setFlags(
				MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS or MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS
			)

			PlayerService.LOG.info("media session init")
		}
		mSession.isActive = true
	}

	fun handleIntent(intent: Intent){
		MediaButtonReceiver.handleIntent(mSession,intent)
	}

	fun setTrack(fTrack: iTrack){
		mSession.setMetadata(
			MediaMetadataCompat.Builder()
			.putString(MediaMetadata.METADATA_KEY_TITLE, fTrack.mTitle)
			.putString(MediaMetadata.METADATA_KEY_AUTHOR, fTrack.mArtist)
			.putString(MediaMetadata.METADATA_KEY_ARTIST, fTrack.mArtist)
			//.putLong(MediaMetadata.METADATA_KEY_DURATION, mMediaPlayer.duration.toLong())
			.build())

		mSession.setPlaybackState(stateBuilder.setState(
			PlaybackStateCompat.STATE_PLAYING,
			PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1F
		).build())
	}

	fun play(){
		if (!mSession.isActive) {
			mSession.isActive = true
			PlayerService.LOG.info("media session active")
		}
	}

	fun pause(){
		mSession.setPlaybackState(stateBuilder.setState(PlaybackStateCompat.STATE_PAUSED,
			PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN, 1F
		).build())
	}

	class MediaSessionCallback(val fService: PlayerService) : MediaSessionCompat.Callback() {

		override fun onPause() {
			fService.playAudio()
		}

		override fun onPlay() {
			fService.playAudio()
		}

		override fun onSkipToNext() {
			fService.nextTrack()
		}

		override fun onSkipToPrevious() {
			fService.prevTrack()
		}

		override fun onMediaButtonEvent(mediaButtonIntent: Intent): Boolean {
			return false
		}
	}
}