package com.devwarex.chatapp

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class FirebaseInstanceService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        if(!message.data.isNullOrEmpty()) {
            val activity = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            if (activity.appTasks.isNullOrEmpty()) {
                notify(message, "")
            } else {
                notify(message, activity.appTasks[0].taskInfo.topActivity?.className ?: "")
            }
        }
    }

    @SuppressLint("CheckResult")
    private fun notify(remote: RemoteMessage, activity: String) {
        if (activity.isEmpty() || activity != "com.devwarex.otterack.chat.ChatActivity") {
            val title = remote.data["title"] ?: remote.notification?.title ?: ""
            val body = remote.data["body"] ?: remote.notification?.body ?: "test!"
            /*val defaultIntent = Intent(this, ChatActivity::class.java).apply {
                putExtra(CHAT_ID_KEY, remote.data["id"])

            }
            defaultIntent.action = AccessibilityService.SERVICE_INTERFACE
            val defaultPendingIntent = PendingIntent.getActivity(
                this, 0,
                defaultIntent, PendingIntent.FLAG_UPDATE_CURRENT
            )*/
            var bitmap: Bitmap? = null
            if(this.resources != null){
                bitmap = BitmapFactory.decodeResource(this.resources,R.drawable.user)
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val CHANNEL_ID = "chat:$title"
                val name = "channel name"
                val descriptionText = "channel description"
                val importance = NotificationManager.IMPORTANCE_HIGH
                val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                    description = descriptionText
                    enableLights(true)
                }
                notificationManager.createNotificationChannel(channel)

                val builder = NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.user)
                    .setContentTitle(title)
                    .setContentText(body)
                    .setPriority(NotificationCompat.PRIORITY_MAX)
                    .setCategory(NotificationCompat.CATEGORY_CALL)
                    .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                    //.setContentIntent(defaultPendingIntent)
                    .setAutoCancel(true)
                if (bitmap != null){
                    builder.setLargeIcon(bitmap)
                }

                with(NotificationManagerCompat.from(this)) {
                    // notificationId is a unique int for each notification that you must define
                    notify(7001, builder.build())
                }

            } else {
                val mBuilder = NotificationCompat.Builder(this)
                mBuilder.setContentTitle(title)
                    .setSmallIcon(R.drawable.user)
                    .setContentText(body)
                    //.setContentIntent(defaultPendingIntent)
                    .setAutoCancel(true)
                if (bitmap != null){
                    mBuilder.setLargeIcon(bitmap)
                }

                notificationManager.notify(1002, mBuilder.build())
            }


        }
    }
}