package com.devwarex.chatapp.repos

import android.util.Log
import com.devwarex.chatapp.api.NotificationClient
import com.devwarex.chatapp.models.MessageNotificationModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PushNotificationRepo {

    companion object{
        fun push(fcm: MessageNotificationModel) =  CoroutineScope(Dispatchers.Main).launch {
            try {
                val response = NotificationClient.create().pushNotification(fcm)
                Log.d(
                    "push_notification",
                    "success: ${response.isSuccessful},sent: ${response.body()?.success}"
                )
            }catch (e: Exception){
                Log.e("notification",e.message.toString())
            }
        }

    }
}