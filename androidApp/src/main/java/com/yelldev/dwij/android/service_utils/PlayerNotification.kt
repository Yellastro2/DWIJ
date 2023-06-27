package com.yelldev.dwij.android.service_utils

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.media.session.MediaButtonReceiver
import com.yelldev.dwij.android.MainActivity
import com.yelldev.dwij.android.player_engine.PlayerService
import com.yelldev.dwij.android.R

class PlayerNotification {
    companion object {
        val NOTIFY_CHAN_ID = "DWIJ_NOTIFY_ID"
        val NOTIFI_CHAN_NAME = "DWIIIJJJ"



        lateinit var mNotyBuilder: NotificationCompat.Builder

        fun getNotify(f_Service: PlayerService, f_token: MediaSessionCompat.Token,
					  f_Title: String, f_Artist: String,isPause: Boolean = false): Notification {

            var fPlayBtn = R.drawable.play
            if(isPause)
                fPlayBtn = R.drawable.stop

            PlaybackStateCompat.ACTION_PLAY_PAUSE

            val pendingIntent: PendingIntent =
                Intent(f_Service, MainActivity::class.java).let { notificationIntent ->
                    notificationIntent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP;
                    PendingIntent.getActivity(f_Service, 0, notificationIntent,
                        PendingIntent.FLAG_MUTABLE or
                        PendingIntent.FLAG_UPDATE_CURRENT //or
                                //PendingIntent.FLAG_NO_CREATE
                    )
                }


            mNotyBuilder = NotificationCompat.Builder(f_Service, NOTIFY_CHAN_ID)

            val notification: Notification = mNotyBuilder
                .setStyle(
                    androidx.media.app.NotificationCompat.MediaStyle()
                        .setShowActionsInCompactView(0,1,2)
                        .setMediaSession(f_token)
//                        .setShowCancelButton(true)
//                        .setCancelButtonIntent(
//                            MediaButtonReceiver.buildMediaButtonPendingIntent(
//                                f_Service,
//                                PlaybackStateCompat.ACTION_STOP))
                    )
                .setContentTitle(f_Title)
                .setContentText(f_Artist)
                .setSmallIcon(R.drawable.logo)
//                .setOngoing(true)

                //.setLargeIcon(R.drawable.logo)
                .setContentIntent(pendingIntent)
                //.setDeleteIntent(MediaButtonReceiver.buildMediaButtonPendingIntent(
                //    f_Service, PlaybackStateCompat.ACTION_PAUSE))
                //.setStyle(Notification.DecoratedCustomViewStyle())
                //.setCustomContentView(notificationLayout)
                //.setCustomBigContentView(notificationLayoutExpanded)
                .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)

                .setPriority(NotificationCompat.PRIORITY_MAX)
                //.setCategory(Notification.CATEGORY_SERVICE)
                //.setTicker(getText(R.string.app_name))
                .addAction(NotificationCompat.Action(
                    R.drawable.previous,
                    "prew",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        f_Service,
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT)
                ))
                .addAction(NotificationCompat.Action(
                    fPlayBtn,
                    "play",
                    MediaButtonReceiver.buildMediaButtonPendingIntent(
                        f_Service,
                        PlaybackStateCompat.ACTION_PLAY_PAUSE)
                ))
                .addAction(NotificationCompat.Action(
                    R.drawable.next,
                    "next",
                MediaButtonReceiver.buildMediaButtonPendingIntent(
                    f_Service,
                    PlaybackStateCompat.ACTION_SKIP_TO_NEXT)
                ))

                .build()


            createNotificationChannel(f_Service,NOTIFY_CHAN_ID,NOTIFI_CHAN_NAME)

            return notification
        }

        private fun createNotificationChannel(f_Service: PlayerService, channelId: String, channelName: String){
            val chan = NotificationChannel(channelId,
                channelName, NotificationManager.IMPORTANCE_LOW)

            val manager = f_Service.getSystemService(NotificationManager::class.java)
            manager!!.createNotificationChannel(chan)
        }
    }

}