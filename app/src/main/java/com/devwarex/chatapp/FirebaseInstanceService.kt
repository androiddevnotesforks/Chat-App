package com.devwarex.chatapp

import android.Manifest
import android.accessibilityservice.AccessibilityService
import android.app.ActivityManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.devwarex.chatapp.repos.UpdateTokenRepo
import com.devwarex.chatapp.ui.chat.ChatsActivity
import com.devwarex.chatapp.ui.conversation.ConversationActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class FirebaseInstanceService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        UpdateTokenRepo.setDeviceToken(uid = Firebase.auth.uid ?: "", token = token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        if (!message.data.isNullOrEmpty()) {
            val activity = getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            if (activity.appTasks.isNullOrEmpty()) {
                notify(message)
            } else {
                //notify(message, activity.appTasks[0].taskInfo.topActivity?.className ?: "")
                Intent().also { intent ->
                    intent.action = "main_app_action_id"
                    intent.putExtra("payload_title", message.data["title"])
                    intent.putExtra("payload_body", message.data["body"])
                    intent.putExtra("payload_id", message.data["id"])
                    sendBroadcast(intent)
                }
            }
        }
    }

    private fun notify(remote: RemoteMessage) {
        Log.e("TAG_BROAD_CHAT", "notify message service")

        val title = remote.data["title"] ?: remote.notification?.title ?: ""
        val body = remote.data["body"] ?: remote.notification?.body ?: "test!"
        val chatId = remote.data["id"] ?: ""
        var defaultIntent = Intent(this, ConversationActivity::class.java)
        if (chatId.isNotEmpty()) {
            defaultIntent.putExtra("chat_id", chatId)
        } else {
            defaultIntent = Intent(this, ChatsActivity::class.java)
        }
        defaultIntent.action = AccessibilityService.SERVICE_INTERFACE
        val defaultPendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getActivity(
                this, 0,
                defaultIntent, PendingIntent.FLAG_MUTABLE
            )
        } else {
            PendingIntent.getActivity(
                this, 0,
                defaultIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )
        }
        var bitmap: Bitmap? = null
        if (this.resources != null) {
            bitmap = BitmapFactory.decodeResource(this.resources, R.drawable.user)
        }
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val CHANNEL_ID = "chat:$chatId"
        val name = "channel name"
        val descriptionText = "channel description"
        val importance = NotificationManager.IMPORTANCE_HIGH
        val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
            description = descriptionText
            enableLights(true)
        }
        notificationManager.createNotificationChannel(channel)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_message)
            .setContentTitle(title)
            .setContentText(body)
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setContentIntent(defaultPendingIntent)
            .setAutoCancel(true)
        if (bitmap != null) {
            builder.setLargeIcon(bitmap)
        }
        if (checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
            == PackageManager.PERMISSION_GRANTED) {
            with(NotificationManagerCompat.from(this)) {
                // notificationId is a unique int for each notification that you must define
                notify(7001, builder.build())
            }
        }

    }
}
