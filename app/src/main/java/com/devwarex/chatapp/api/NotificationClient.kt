package com.devwarex.chatapp.api


import com.devwarex.chatapp.util.Paths.Companion.FCM_URL
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class NotificationClient {

    companion object{
        fun create() : NotificationService =
            Retrofit.Builder()
                .baseUrl(FCM_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(NotificationService::class.java)
    }
}