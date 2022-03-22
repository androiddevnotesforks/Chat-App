package com.devwarex.chatapp.repos

import android.util.Log
import com.devwarex.chatapp.api.NotificationClient
import com.devwarex.chatapp.models.MessageNotificationModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.schedulers.Schedulers

class PushNotificationRepo {

    companion object{
        fun push(fcm: MessageNotificationModel){
            NotificationClient.create().pushNotification(fcm)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({Log.i("PUSH_NOTIFY","${it.message()}\n ${it.body()?.success}")},{ Log.e("PUSH_NOTIFY",it.message!!)})
        }
    }
}