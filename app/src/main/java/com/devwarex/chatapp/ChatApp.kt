package com.devwarex.chatapp

import android.app.Application
import android.content.BroadcastReceiver
import android.content.IntentFilter
import com.devwarex.chatapp.utility.BroadCastUtility
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class ChatApp : Application() {

    override fun onCreate() {
        super.onCreate()
        val br: BroadcastReceiver = ChatAppBroadCastReceiver()
        val filter = IntentFilter(BroadCastUtility.CHAT_ID).apply {
            addAction(BroadCastUtility.MAIN_APP_ACTION_ID)
        }
        registerReceiver(br, filter)
    }
}