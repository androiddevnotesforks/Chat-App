package com.devwarex.chatapp.api


import com.devwarex.chatapp.util.Paths.Companion.FCM_URL
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory


class NotificationClient {

    companion object{
        fun create() : NotificationService =
            Retrofit.Builder()
                .baseUrl(FCM_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
                .build()
                .create(NotificationService::class.java)
    }
}