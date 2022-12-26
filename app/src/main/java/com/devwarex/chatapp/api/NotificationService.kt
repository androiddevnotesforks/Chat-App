package com.devwarex.chatapp.api

import com.devwarex.chatapp.BuildConfig
import com.devwarex.chatapp.models.notification.MessageNotificationModel
import com.devwarex.chatapp.models.notification.NotificationResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface NotificationService {

    @Headers(
        "Content-Type:application/json",
        BuildConfig.NOTIFICATION_KEY
    )
    @POST("fcm/send")
    suspend fun pushNotification(@Body message: MessageNotificationModel) : Response<NotificationResponse>
}