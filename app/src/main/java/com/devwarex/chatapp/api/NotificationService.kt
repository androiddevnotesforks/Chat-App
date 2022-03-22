package com.devwarex.chatapp.api

import com.devwarex.chatapp.models.MessageNotificationModel
import com.devwarex.chatapp.models.NotificationResponse
import io.reactivex.rxjava3.core.Single
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface NotificationService {

    @Headers(
        "Content-Type:application/json",
        "Authorization:key=AAAAEC2hmPk:APA91bGulOjlDnRfXnmOp7nyK3yeArhigfxA4R8oKo_3Z9uyQxivQ0l3ud_pzb1Td7IyAWpYi9ZvOGNHjWmKU6ZnQbWH13QcdZItbOrft4DSzyNOe0be3vgxys9B0yYsZkn40TR5rQt9")
    @POST("fcm/send")
    fun pushNotification(@Body message: MessageNotificationModel) : Single<Response<NotificationResponse>>
}